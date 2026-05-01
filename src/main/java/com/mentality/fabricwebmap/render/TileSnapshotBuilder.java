package com.mentality.fabricwebmap.render;

import com.mentality.fabricwebmap.config.WebMapConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Builds a TileSnapshot on the MAIN SERVER THREAD.
 * Must NOT be called from worker threads.
 */
public class TileSnapshotBuilder {

    private static final Logger LOGGER = LoggerFactory.getLogger("fabricwebmap");

    private final WebMapConfig config;

    public TileSnapshotBuilder(WebMapConfig config) {
        this.config = config;
    }

    public TileSnapshot build(ServerLevel level, int tileX, int tileZ, int zoom) {
        return build(level, tileX, tileZ, zoom, false);
    }

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
