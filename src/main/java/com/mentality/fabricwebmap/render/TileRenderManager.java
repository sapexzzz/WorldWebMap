package com.mentality.fabricwebmap.render;

import com.mentality.fabricwebmap.config.WebMapConfig;
import com.mentality.fabricwebmap.storage.TileStorage;
import com.mentality.fabricwebmap.util.DimensionUtil;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
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

    public enum State { IDLE, RUNNING, STOPPED }

    private final WebMapConfig config;
    private TileStorage tileStorage;
    private TileSnapshotBuilder snapshotBuilder;
    private TileRenderer renderer;
    private ExecutorService workerPool;

    private MinecraftServer server;

    private final ConcurrentLinkedDeque<RenderJob> queue = new ConcurrentLinkedDeque<>();
    private final Set<RenderJob> enqueuedSet = Collections.newSetFromMap(new ConcurrentHashMap<>());

    private volatile State state = State.IDLE;
    private final AtomicLong renderedTiles = new AtomicLong(0);
    private final AtomicInteger activeWorkers = new AtomicInteger(0);

    private int tickCounter = 0;
    private final ConcurrentHashMap<String, PendingTile> pendingChunkTiles = new ConcurrentHashMap<>();

    public TileRenderManager(WebMapConfig config) {
        this.config = config;
    }

    public void setServer(MinecraftServer server) {
        this.server = server;
    }

    public void start() {
        tileStorage = new TileStorage(config);
        snapshotBuilder = new TileSnapshotBuilder(config);
        renderer = new TileRenderer(tileStorage);

        int threads = config.getRenderThreads();
        workerPool = Executors.newFixedThreadPool(threads, r -> {
            Thread t = new Thread(r, "fabricwebmap-render");
            t.setDaemon(true);
            t.setPriority(Thread.MIN_PRIORITY);
            return t;
        });

        state = State.RUNNING;
        // NOTE: Tick event is registered once in FabricWebMapMod.onInitialize()
        // and gated by the state check inside onServerTick().
        LOGGER.info("TileRenderManager started with {} worker thread(s).", threads);
    }

    public void stop() {
        state = State.STOPPED;
        clearQueue();
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
        if (!pendingChunkTiles.isEmpty()) {
            long now = System.currentTimeMillis();
            long debounceMs = config.getChunkRenderDebounceMs();
            pendingChunkTiles.entrySet().removeIf(e -> {
                PendingTile p = e.getValue();
                if (now - p.lastTouchedMs >= debounceMs) {
                    // For tiles that have never been rendered, force-load chunks from disk
                    // so the first render is complete even if the player has moved away.
                    // For existing tiles, rely on compositing to preserve old pixels —
                    // this avoids expensive synchronous disk I/O on every re-render.
                    boolean tileExists = tileStorage.exists(p.dimension, 0, p.tileX, p.tileZ);
                    RenderJob job = new RenderJob(p.dimension, p.tileX, p.tileZ, 0,
                            RenderJob.Priority.LOW, true, !tileExists);
                    enqueue(job, true);
                    return true;
                }
                return false;
            });
        }

        int interval = Math.max(1, config.getTicksBetweenRenders());
        if (tickCounter % interval != 0) return;

        int limit = config.getMaxTilesPerTick();
        int processed = 0;

        while (processed < limit && !queue.isEmpty()) {
            RenderJob job = queue.poll();
            if (job == null) break;
            enqueuedSet.remove(job);
            processed++;

            if (!job.force && tileStorage.exists(job.dimension, job.zoom, job.tileX, job.tileZ)) {
                continue;
            }

            ServerLevel level = server.getLevel(job.dimension);
            if (level == null) {
                LOGGER.warn("Dimension {} not found for render job, skipping.", DimensionUtil.toWebName(job.dimension));
                continue;
            }

            TileSnapshot snapshot;
            try {
                snapshot = snapshotBuilder.build(level, job.tileX, job.tileZ, job.zoom, job.loadChunks);
            } catch (Exception e) {
                LOGGER.error("Failed to build snapshot for tile {}/{}: {}", job.tileX, job.tileZ, e.getMessage());
                continue;
            }

            activeWorkers.incrementAndGet();
            workerPool.submit(() -> {
                try {
                    renderer.render(snapshot);
                    long count = renderedTiles.incrementAndGet();
                    if (config.isLogRenderProgress() && count % LOG_EVERY_N_TILES == 0) {
                        LOGGER.info("World Web Map: {} tiles rendered so far.", count);
                    }
                } catch (Exception e) {
                    LOGGER.error("Worker error rendering tile {}/{}: {}", job.tileX, job.tileZ, e.getMessage());
                } finally {
                    activeWorkers.decrementAndGet();
                }
            });
        }
    }

    // ── Queue management ───────────────────────────────────────────────────────

    public boolean enqueue(RenderJob job, boolean force) {
        if (state == State.STOPPED) return false;
        if (!force && queue.size() >= MAX_QUEUE_SIZE) return false;
        if (enqueuedSet.contains(job)) return false;
        enqueuedSet.add(job);
        queue.offer(job);
        return true;
    }

    public boolean enqueue(RenderJob job) {
        return enqueue(job, job.force);
    }

    public void clearQueue() {
        queue.clear();
        enqueuedSet.clear();
        pendingChunkTiles.clear();
    }

    /**
     * Called by ChunkLoadListener when a chunk loads.
     * Debounces the render so we don't render a half-loaded tile area.
     */
    public void markChunkLoaded(ResourceKey<Level> dimension, int tileX, int tileZ) {
        if (state == State.STOPPED) return;
        if (!config.isEnableAutoRender()) return;
        String key = dimension.location().toString() + ":" + tileX + ":" + tileZ;
        pendingChunkTiles.compute(key, (k, existing) -> {
            if (existing == null) return new PendingTile(dimension, tileX, tileZ);
            existing.lastTouchedMs = System.currentTimeMillis();
            return existing;
        });
    }

    // ── Accessors ──────────────────────────────────────────────────────────────

    public int getQueueSize() { return queue.size(); }
    public int getActiveWorkers() { return activeWorkers.get(); }
    public long getRenderedTiles() { return renderedTiles.get(); }
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
