package com.mentality.forgewebmap.render;

import com.mentality.forgewebmap.config.WebMapConfig;
import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.server.level.ServerLevel;
import org.slf4j.Logger;

/**
 * Builds a TileSnapshot on the MAIN SERVER THREAD.
 * Must NOT be called from worker threads.
 *
 * For each pixel (px, pz) in the tile:
 *   blockX = tileX * tileSize + px
 *   blockZ = tileZ * tileSize + pz
 *   y = level.getHeight(WORLD_SURFACE, blockX, blockZ)
 *   topBlock = level.getBlockState(blockX, y-1, blockZ)
 *   color = BlockColorResolver.resolve(topBlock)
 *   height = y
 */
public class TileSnapshotBuilder {

    private static final Logger LOGGER = LogUtils.getLogger();

    private final WebMapConfig config;

    public TileSnapshotBuilder(WebMapConfig config) {
        this.config = config;
    }

    /**
     * Build snapshot. Call only on the main server thread.
     *
     * @param level  the ServerLevel (safe on main thread)
     * @param tileX  tile X coordinate
     * @param tileZ  tile Z coordinate
     * @param zoom   zoom level (0 = 1px per block)
     * @return TileSnapshot safe for worker thread consumption
     */
    public TileSnapshot build(ServerLevel level, int tileX, int tileZ, int zoom) {
        return build(level, tileX, tileZ, zoom, false);
    }

    /**
     * Build snapshot. Call only on the main server thread.
     *
     * @param loadChunks if true, unloaded chunks are loaded from disk before sampling;
     *                   if false, unloaded chunks produce placeholder pixels (composited later)
     */
    public TileSnapshot build(ServerLevel level, int tileX, int tileZ, int zoom, boolean loadChunks) {
        int tileSize = config.getTileSize();
        int[] colors = new int[tileSize * tileSize];
        int[] heights = new int[tileSize * tileSize];
        boolean[] loadedMask = new boolean[tileSize * tileSize];

        int startX = tileX * tileSize;
        int startZ = tileZ * tileSize;

        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();

        for (int pz = 0; pz < tileSize; pz++) {
            for (int px = 0; px < tileSize; px++) {
                int blockX = startX + px;
                int blockZ = startZ + pz;

                int idx = px + pz * tileSize;
                int chunkX = blockX >> 4;
                int chunkZ = blockZ >> 4;

                LevelChunk chunk = level.getChunkSource().getChunkNow(chunkX, chunkZ);
                if (chunk == null && loadChunks) {
                    // Force-load chunk from disk (synchronous, main thread only).
                    // Will return null for chunks that have never been generated.
                    ChunkAccess ca = level.getChunkSource().getChunk(chunkX, chunkZ, ChunkStatus.FULL, true);
                    if (ca instanceof LevelChunk lc) chunk = lc;
                }

                if (chunk == null) {
                    colors[idx] = 0xFF222222;
                    heights[idx] = 64;
                    loadedMask[idx] = false;
                    continue;
                }

                int y = level.getHeight(Heightmap.Types.WORLD_SURFACE, blockX, blockZ);
                int surfaceY = Math.max(y - 1, level.getMinBuildHeight());

                mutablePos.set(blockX, surfaceY, blockZ);
                BlockState state = level.getBlockState(mutablePos);

                int color = BlockColorResolver.resolve(state);
                colors[idx] = 0xFF000000 | (color & 0x00FFFFFF);
                heights[idx] = surfaceY;
                loadedMask[idx] = true;
            }
        }

        return new TileSnapshot(level.dimension(), tileX, tileZ, zoom, tileSize, colors, heights, loadedMask);
    }
}
