package com.mentality.forgewebmap.render;

import com.mentality.forgewebmap.config.WebMapConfig;
import com.mentality.forgewebmap.storage.TileStorage;
import com.mentality.forgewebmap.util.DimensionUtil;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
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

    public enum State { IDLE, RUNNING, STOPPED }

    private final WebMapConfig config;
    private TileStorage tileStorage;
    private TileSnapshotBuilder snapshotBuilder;
    private TileRenderer renderer;
    private ExecutorService workerPool;

    private MinecraftServer server;

    // Queue of pending jobs (main-thread consumption)
    private final ConcurrentLinkedDeque<RenderJob> queue = new ConcurrentLinkedDeque<>();
    // Set for dedup check
    private final Set<RenderJob> enqueuedSet = Collections.newSetFromMap(new ConcurrentHashMap<>());

    private volatile State state = State.IDLE;
    private final AtomicLong renderedTiles = new AtomicLong(0);
    private final AtomicInteger activeWorkers = new AtomicInteger(0);

    // Tick counter for throttling render queue processing
    private int tickCounter = 0;
    // Debounce map: pending chunk-load triggered tiles (tileKey -> PendingTile)
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
            Thread t = new Thread(r, "forgewebmap-render");
            t.setDaemon(true);
            t.setPriority(Thread.MIN_PRIORITY); // low priority to not starve the server
            return t;
        });

        state = State.RUNNING;
        // Register server tick listener
        MinecraftForge.EVENT_BUS.register(this);
        LOGGER.info("TileRenderManager started with {} worker thread(s).", threads);
    }

    public void stop() {
        state = State.STOPPED;
        clearQueue();
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
        // Scanned every tick (cheap), but rendering only starts after debounce delay.
        if (!pendingChunkTiles.isEmpty()) {
            long now = System.currentTimeMillis();
            long debounceMs = config.getChunkRenderDebounceMs();
            pendingChunkTiles.entrySet().removeIf(e -> {
                PendingTile p = e.getValue();
                if (now - p.lastTouchedMs >= debounceMs) {
                    // force=true: re-render even if the file already exists
                    // (new chunks may have loaded into the tile area since last render)
                    RenderJob job = new RenderJob(p.dimension, p.tileX, p.tileZ, 0,
                            RenderJob.Priority.LOW, true);
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

        int limit = config.getMaxTilesPerTick();
        int processed = 0;

        while (processed < limit && !queue.isEmpty()) {
            RenderJob job = queue.poll();
            if (job == null) break;
            enqueuedSet.remove(job);
            processed++;

            // Skip if file exists and not forced
            if (!job.force && tileStorage.exists(job.dimension, job.zoom, job.tileX, job.tileZ)) {
                continue;
            }

            // Get the appropriate ServerLevel (main thread only)
            ServerLevel level = server.getLevel(job.dimension);
            if (level == null) {
                LOGGER.warn("Dimension {} not found for render job, skipping.", DimensionUtil.toWebName(job.dimension));
                continue;
            }

            // Build snapshot on main thread
            TileSnapshot snapshot;
            try {
                snapshot = snapshotBuilder.build(level, job.tileX, job.tileZ, job.zoom, job.loadChunks);
            } catch (Exception e) {
                LOGGER.error("Failed to build snapshot for tile {}/{}: {}", job.tileX, job.tileZ, e.getMessage());
                continue;
            }

            // Submit rendering to worker pool
            activeWorkers.incrementAndGet();
            workerPool.submit(() -> {
                try {
                    renderer.render(snapshot);
                    long count = renderedTiles.incrementAndGet();
                    if (config.isLogRenderProgress() && count % LOG_EVERY_N_TILES == 0) {
                        LOGGER.info("ForgeWebMap: {} tiles rendered so far.", count);
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

    /**
     * Enqueue a render job.
     *
     * @param job   the job to enqueue
     * @param force if true, bypass queue size limit
     * @return true if enqueued, false if rejected (queue full or duplicate)
     */
    public boolean enqueue(RenderJob job, boolean force) {
        if (state == State.STOPPED) return false;
        if (!force && queue.size() >= MAX_QUEUE_SIZE) return false;
        if (enqueuedSet.contains(job)) return false; // dedup
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
     * Called by ChunkLoadListener when a chunk loads in the world.
     * Debounces the render: the tile will be re-rendered only after
     * no new chunks have loaded in that tile area for chunkRenderDebounceMs.
     * Uses force=true so already-rendered (partially gray) tiles get updated.
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
