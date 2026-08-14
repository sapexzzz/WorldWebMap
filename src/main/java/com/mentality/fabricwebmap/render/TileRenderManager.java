package com.mentality.fabricwebmap.render;

import com.mentality.fabricwebmap.config.WebMapConfig;
import com.mentality.fabricwebmap.storage.TileStorage;
import com.mentality.fabricwebmap.util.DimensionUtil;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.LevelResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Manages the render queue and worker threads.
 *
 * Flow:
 *  1. Commands call enqueue(RenderJob).
 *  2. Each server tick, onServerTick() is called by FabricWebMapMod via
 *     ServerTickEvents.END_SERVER_TICK. It picks up to maxTilesPerTick jobs,
 *     calls TileSnapshotBuilder to sample the world (main-thread safe),
 *     then submits TileRenderer.render(snapshot) to a worker ExecutorService.
 *
 * ServerLevel is ONLY accessed on the main server thread.
 */
public class TileRenderManager {

    private static final Logger LOGGER = LoggerFactory.getLogger("fabricwebmap");
    private static final int MAX_QUEUE_SIZE = 100_000;
    private static final int LOG_EVERY_N_TILES = 50;
    private static final int FULL_RENDER_PRODUCE_PER_TICK = 32;

    public enum State { IDLE, RUNNING, STOPPED }

    private final WebMapConfig config;
    private TileStorage tileStorage;
    private TileSnapshotBuilder snapshotBuilder;
    private TileRenderer renderer;
    private ThreadPoolExecutor workerPool;
    private RenderAdmission workerSlots;

    private MinecraftServer server;

    private final BoundedRenderQueue<RenderJob> queue = new BoundedRenderQueue<>(MAX_QUEUE_SIZE,
            job -> job.priority.ordinal());

    private volatile State state = State.IDLE;
    private final AtomicLong renderedTiles = new AtomicLong(0);
    private final AtomicInteger activeWorkers = new AtomicInteger(0);

    private int tickCounter = 0;
    private final PendingChunkTracker<PendingTile> pendingChunkTiles = new PendingChunkTracker<>();
    private volatile FullRenderPlan fullRenderPlan;
    private final SnapshotBudget.Clock monotonicClock = System::nanoTime;

    public TileRenderManager(WebMapConfig config) {
        this.config = config;
    }

    public void setServer(MinecraftServer server) {
        this.server = server;
    }

    public void start() {
        java.nio.file.Path worldRoot = server == null ? java.nio.file.Paths.get("world") : server.getWorldPath(LevelResource.ROOT);
        tileStorage = TileStorage.forWorld(config, worldRoot);
        snapshotBuilder = new TileSnapshotBuilder(config);
        renderer = new TileRenderer(tileStorage);

        int threads = config.getRenderThreads();
        workerPool = new ThreadPoolExecutor(threads, threads, 0L, TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<>(threads), r -> {
            Thread t = new Thread(r, "fabricwebmap-render");
            t.setDaemon(true);
            t.setPriority(Thread.MIN_PRIORITY);
            return t;
        }, new ThreadPoolExecutor.AbortPolicy());
        // One permit for each running worker and each bounded executor backlog slot.
        workerSlots = new RenderAdmission(threads);

        state = State.RUNNING;
        // NOTE: Tick event is registered once in FabricWebMapMod.onInitialize()
        // and gated by the state check inside onServerTick().
        LOGGER.info("TileRenderManager started with {} worker thread(s).", threads);
    }

    public void stop() {
        state = State.STOPPED;
        stopRendering();
        if (workerPool != null) {
            workerPool.shutdown();
            try {
                workerPool.awaitTermination(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        LOGGER.info("TileRenderManager stopped.");
    }

    // ── Tick processing (MAIN SERVER THREAD) ──────────────────────────────────
    // Called by FabricWebMapMod via ServerTickEvents.END_SERVER_TICK

    public void onServerTick(MinecraftServer ignored) {
        if (state != State.RUNNING) return;
        if (server == null) return;

        tickCounter++;

        // Flush debounced chunk-load tiles into the render queue
        if (!pendingChunkTiles.entries().isEmpty()) {
            long now = System.currentTimeMillis();
            long debounceMs = config.getChunkRenderDebounceMs();
            pendingChunkTiles.entries().entrySet().removeIf(e -> {
                PendingTile p = e.getValue();
                if (now - p.lastTouchedMs >= debounceMs) {
                    // Rendering must never generate terrain. Unloaded chunks remain visibly
                    // marked until a player/server load makes their data available.
                    RenderJob job = new RenderJob(p.dimension, p.tileX, p.tileZ, 0,
                            RenderJob.Priority.LOW, true, false);
                    enqueue(job, true);
                    return true;
                }
                return false;
            });
        }

        int interval = Math.max(1, config.getTicksBetweenRenders());
        if (tickCounter % interval != 0) return;

        produceFullRenderJobs(FULL_RENDER_PRODUCE_PER_TICK);

        int limit = config.getMaxTilesPerTick();
        int processed = 0;
        SnapshotBudget budget = new SnapshotBudget(monotonicClock, TimeUnit.MILLISECONDS.toNanos(config.getMaxSnapshotMillisPerTick()));
        budget.beginTick();

        while (processed < limit && !budget.exhausted()) {
            // Do not build a main-thread snapshot unless a renderer slot was reserved.
            if (!workerSlots.tryAcquire()) break;
            RenderJob job = queue.pollForRender();
            if (job == null) {
                workerSlots.release();
                break;
            }
            processed++;

            if (!job.force && tileStorage.exists(job.dimension, job.zoom, job.tileX, job.tileZ)) {
                queue.complete(job);
                workerSlots.release();
                continue;
            }

            ServerLevel level = server.getLevel(job.dimension);
            if (level == null) {
                LOGGER.warn("Dimension {} not found for render job, skipping.", DimensionUtil.toWebName(job.dimension));
                queue.complete(job);
                workerSlots.release();
                continue;
            }

            TileSnapshot snapshot;
            try {
                snapshot = snapshotBuilder.build(level, job.tileX, job.tileZ, job.zoom, job.loadChunks);
            } catch (Exception e) {
                LOGGER.error("Failed to build snapshot for tile {}/{}: {}", job.tileX, job.tileZ, e.getMessage());
                queue.complete(job);
                workerSlots.release();
                continue;
            }

            try {
                activeWorkers.incrementAndGet();
                workerPool.execute(() -> {
                    try {
                        TileRenderer.Result result=renderer.render(snapshot);
                        long count = recordRenderResult(result);
                        if (config.isLogRenderProgress() && count % LOG_EVERY_N_TILES == 0) {
                            LOGGER.info("World Web Map: {} tiles rendered so far.", count);
                        }
                    } catch (Exception e) {
                        LOGGER.error("Worker error rendering tile {}/{}: {}", job.tileX, job.tileZ, e.getMessage());
                    } finally {
                        activeWorkers.decrementAndGet();
                        queue.complete(job);
                        workerSlots.release();
                    }
                });
            } catch (RejectedExecutionException e) {
                activeWorkers.decrementAndGet();
                queue.complete(job);
                workerSlots.release();
                LOGGER.warn("Render worker queue is full; dropping tile {}/{}.", job.tileX, job.tileZ);
            }
        }
    }

    // ── Queue management ───────────────────────────────────────────────────────

    public boolean enqueue(RenderJob job, boolean force) {
        if (state == State.STOPPED) return false;
        // force controls file-existence behavior only; it never bypasses capacity.
        return queue.offer(job);
    }

    public boolean enqueue(RenderJob job) {
        return enqueue(job, job.force);
    }

    public void clearQueue() {
        queue.clearPending();
        pendingChunkTiles.clear();
    }

    /** Starts a lazy spawn-centred fullrender; no jobs are materialized here. */
    public FullRenderPlan startFullRender(ResourceKey<Level> dimension, int centerX, int centerZ, int radius) {
        FullRenderPlan plan = new FullRenderPlan(dimension, centerX, centerZ, radius);
        fullRenderPlan = plan;
        return plan;
    }

    /** Cancels producer work and removes only work that has not started. */
    public int stopRendering() {
        FullRenderPlan plan = fullRenderPlan;
        if (plan != null) plan.cancel();
        fullRenderPlan = null;
        int pending = queue.pendingSize();
        clearQueue();
        return pending;
    }

    int produceFullRenderJobs(int maximum) {
        FullRenderPlan plan = fullRenderPlan;
        if (plan == null || plan.isCancelled() || state != State.RUNNING || workerSlots == null || workerSlots.available() == 0) return 0;
        int produced = 0;
        while (produced < maximum && !plan.isCancelled() && !plan.isComplete()) {
            RenderJob job = plan.peek();
            if (job == null || !queue.offer(job)) break;
            plan.markEnqueued();
            produced++;
        }
        // Keep the plan visible while its already-enqueued jobs are still rendering.
        // Otherwise status reports a completed fullrender before the queue has drained.
        if (plan.isCancelled() || (plan.isComplete() && queue.pendingSize() == 0 && activeWorkers.get() == 0)) {
            fullRenderPlan = null;
        }
        return produced;
    }

    /**
     * Called by ChunkLoadListener when a chunk loads.
     * Debounces the render so we don't render a half-loaded tile area.
     */
    public void markChunkLoaded(ResourceKey<Level> dimension, int tileX, int tileZ) {
        if (!config.isEnableAutoRender()) return;
        String key = dimension.location().toString() + ":" + tileX + ":" + tileZ;
        pendingChunkTiles.recordIfRunning(state == State.RUNNING, key, () -> new PendingTile(dimension, tileX, tileZ), tile -> tile.lastTouchedMs = System.currentTimeMillis());
    }

    // ── Accessors ──────────────────────────────────────────────────────────────

    public int getQueueSize() { return queue.pendingSize(); }
    public FullRenderPlan getFullRenderPlan() { return fullRenderPlan; }
    public long getFullRenderRemaining() { FullRenderPlan p=fullRenderPlan; return p == null ? 0 : p.getRemaining(); }
    public int getActiveWorkers() { return activeWorkers.get(); }
    public long getRenderedTiles() { return renderedTiles.get(); }
    long recordRenderResult(TileRenderer.Result result) { return result == TileRenderer.Result.WRITTEN ? renderedTiles.incrementAndGet() : renderedTiles.get(); }
    public State getState() { return state; }
    public TileStorage getTileStorage() { return tileStorage; }

    // ── Inner classes ──────────────────────────────────────────────────────────

    private static final class PendingTile {
        final ResourceKey<Level> dimension;
        final int tileX;
        final int tileZ;
        volatile long lastTouchedMs;

        PendingTile(ResourceKey<Level> dim, int x, int z) {
            this.dimension = dim;
            this.tileX = x;
            this.tileZ = z;
            this.lastTouchedMs = System.currentTimeMillis();
        }
    }
}
