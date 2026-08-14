package com.mentality.forgewebmap.render;

import com.mentality.forgewebmap.config.WebMapConfig;
import com.mentality.forgewebmap.storage.TileStorage;
import com.mentality.forgewebmap.util.DimensionUtil;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.common.MinecraftForge;
import org.slf4j.Logger;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Manages the render queue and worker threads.
 *
 * Flow:
 *  1. Commands call enqueue(RenderJob).
 *  2. Each server tick, the main thread picks up to maxTilesPerTick jobs,
 *     calls TileSnapshotBuilder to sample the world (main-thread safe),
 *     then submits TileRenderer.render(snapshot) to a worker ExecutorService.
 *
 * This ensures ServerLevel is ONLY accessed on the main server thread.
 */
public class TileRenderManager {

    private static final Logger LOGGER = LogUtils.getLogger();
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

    // Queue of pending jobs (main-thread consumption)
    private final BoundedRenderQueue<RenderJob> queue = new BoundedRenderQueue<>(MAX_QUEUE_SIZE,
            job -> job.priority.ordinal());

    private volatile State state = State.IDLE;
    private final AtomicLong renderedTiles = new AtomicLong(0);
    private final AtomicInteger activeWorkers = new AtomicInteger(0);

    // Tick counter for throttling render queue processing
    private int tickCounter = 0;
    // Debounce map: pending chunk-load triggered tiles (tileKey -> PendingTile)
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
        java.nio.file.Path worldRoot=server==null?java.nio.file.Paths.get("world"):server.getWorldPath(LevelResource.ROOT); tileStorage=TileStorage.forWorld(config,worldRoot);
        snapshotBuilder = new TileSnapshotBuilder(config);
        renderer = new TileRenderer(tileStorage);

        int threads = config.getRenderThreads();
        workerPool = new ThreadPoolExecutor(threads, threads, 0L, TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<>(threads), r -> {
            Thread t = new Thread(r, "forgewebmap-render");
            t.setDaemon(true);
            t.setPriority(Thread.MIN_PRIORITY); // low priority to not starve the server
            return t;
        }, new ThreadPoolExecutor.AbortPolicy());
        workerSlots = new RenderAdmission(threads);

        state = State.RUNNING;
        // Register server tick listener
        MinecraftForge.EVENT_BUS.register(this);
        LOGGER.info("TileRenderManager started with {} worker thread(s).", threads);
    }

    public void stop() {
        state = State.STOPPED;
        stopRendering();
        MinecraftForge.EVENT_BUS.unregister(this);
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

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (state != State.RUNNING) return;
        if (server == null) return;

        tickCounter++;

        // Flush debounced chunk-load tiles into the render queue.
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

        // Throttle: process the render queue only every N ticks to reduce main-thread load.
        // With default ticksBetweenRenders=20 and maxTilesPerTick=1 → 1 tile/second.
        int interval = Math.max(1, config.getTicksBetweenRenders());
        if (tickCounter % interval != 0) return;

        produceFullRenderJobs(FULL_RENDER_PRODUCE_PER_TICK);

        int limit = config.getMaxTilesPerTick();
        int processed = 0;
        SnapshotBudget budget = new SnapshotBudget(monotonicClock, TimeUnit.MILLISECONDS.toNanos(config.getMaxSnapshotMillisPerTick()));
        budget.beginTick();

        while (processed < limit && !budget.exhausted()) {
            if (!workerSlots.tryAcquire()) break;
            RenderJob job = queue.pollForRender();
            if (job == null) { workerSlots.release(); break; }
            processed++;

            // Skip if file exists and not forced
            if (!job.force && tileStorage.exists(job.dimension, job.zoom, job.tileX, job.tileZ)) {
                queue.complete(job); workerSlots.release();
                continue;
            }

            // Get the appropriate ServerLevel (main thread only)
            ServerLevel level = server.getLevel(job.dimension);
            if (level == null) {
                LOGGER.warn("Dimension {} not found for render job, skipping.", DimensionUtil.toWebName(job.dimension));
                queue.complete(job); workerSlots.release();
                continue;
            }

            // Build snapshot on main thread
            TileSnapshot snapshot;
            try {
                snapshot = snapshotBuilder.build(level, job.tileX, job.tileZ, job.zoom, job.loadChunks);
            } catch (Exception e) {
                LOGGER.error("Failed to build snapshot for tile {}/{}: {}", job.tileX, job.tileZ, e.getMessage());
                queue.complete(job); workerSlots.release();
                continue;
            }

            // Submit rendering to worker pool
            try {
            activeWorkers.incrementAndGet();
            workerPool.execute(() -> {
                try {
                        TileRenderer.Result result=renderer.render(snapshot);
                        long count=recordRenderResult(result);
                    if (config.isLogRenderProgress() && count % LOG_EVERY_N_TILES == 0) {
                        LOGGER.info("World Web Map: {} tiles rendered so far.", count);
                    }
                } catch (Exception e) {
                    LOGGER.error("Worker error rendering tile {}/{}: {}", job.tileX, job.tileZ, e.getMessage());
                } finally {
                    activeWorkers.decrementAndGet();
                    queue.complete(job); workerSlots.release();
                }
            });
            } catch (RejectedExecutionException e) {
                activeWorkers.decrementAndGet(); queue.complete(job); workerSlots.release();
                LOGGER.warn("Render worker queue is full; dropping tile {}/{}.", job.tileX, job.tileZ);
            }
        }
    }

    // ── Queue management ───────────────────────────────────────────────────────

    /**
     * Enqueue a render job.
     *
     * @param job   the job to enqueue
     * @param force retained for compatibility; never bypasses capacity
     * @return true if enqueued, false if rejected (queue full or duplicate)
     */
    public boolean enqueue(RenderJob job, boolean force) {
        if (state == State.STOPPED) return false;
        return queue.offer(job);
    }

    public boolean enqueue(RenderJob job) {
        return enqueue(job, job.force);
    }

    public void clearQueue() {
        queue.clearPending();
        pendingChunkTiles.clear();
    }

    public FullRenderPlan startFullRender(ResourceKey<Level> dimension, int centerX, int centerZ, int radius) {
        FullRenderPlan plan = new FullRenderPlan(dimension, centerX, centerZ, radius); fullRenderPlan = plan; return plan;
    }
    public int stopRendering() {
        FullRenderPlan plan=fullRenderPlan; if(plan!=null)plan.cancel(); fullRenderPlan=null;
        int pending=queue.pendingSize(); clearQueue(); return pending;
    }
    int produceFullRenderJobs(int maximum) {
        FullRenderPlan plan=fullRenderPlan;
        if(plan==null||plan.isCancelled()||state!=State.RUNNING||workerSlots==null||workerSlots.available()==0)return 0;
        int produced=0; while(produced<maximum&&!plan.isCancelled()&&!plan.isComplete()){RenderJob job=plan.peek();if(job==null||!queue.offer(job))break;plan.markEnqueued();produced++;}
        // Retain the plan until already-enqueued work has drained so status remains truthful.
        if (plan.isCancelled() || (plan.isComplete() && queue.pendingSize() == 0 && activeWorkers.get() == 0)) fullRenderPlan=null; return produced;
    }

    /**
     * Called by ChunkLoadListener when a chunk loads in the world.
     * Debounces the render: the tile will be re-rendered only after
     * no new chunks have loaded in that tile area for chunkRenderDebounceMs.
     * Uses force=true so already-rendered (partially gray) tiles get updated.
     */
    public void markChunkLoaded(ResourceKey<Level> dimension, int tileX, int tileZ) {
        if (!config.isEnableAutoRender()) return;
        String key = dimension.location().toString() + ":" + tileX + ":" + tileZ;
        pendingChunkTiles.recordIfRunning(state == State.RUNNING, key, () -> new PendingTile(dimension, tileX, tileZ), tile -> tile.lastTouchedMs = System.currentTimeMillis());
    }

    // ── Accessors ──────────────────────────────────────────────────────────────

    public int getQueueSize() { return queue.pendingSize(); }
    public FullRenderPlan getFullRenderPlan() { return fullRenderPlan; }
    public long getFullRenderRemaining() { FullRenderPlan p=fullRenderPlan; return p==null?0:p.getRemaining(); }
    public int getActiveWorkers() { return activeWorkers.get(); }
    public long getRenderedTiles() { return renderedTiles.get(); }
    long recordRenderResult(TileRenderer.Result result) { return result==TileRenderer.Result.WRITTEN?renderedTiles.incrementAndGet():renderedTiles.get(); }
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
