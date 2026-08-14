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
    private final SurfaceHeightResolver surfaceHeights = new SurfaceHeightResolver();

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

        // Resolve each intersecting 16x16 chunk once, then sample its local columns.
        int endX = startX + tileSize, endZ = startZ + tileSize;
        for (int chunkZ = Math.floorDiv(startZ, 16); chunkZ <= Math.floorDiv(endZ - 1, 16); chunkZ++) {
            for (int chunkX = Math.floorDiv(startX, 16); chunkX <= Math.floorDiv(endX - 1, 16); chunkX++) {
                LevelChunk chunk = level.getChunkSource().getChunkNow(chunkX, chunkZ);
                if (chunk == null && loadChunks) {
                    ChunkAccess ca = level.getChunkSource().getChunk(chunkX, chunkZ, ChunkStatus.FULL, true);
                    if (ca instanceof LevelChunk lc) chunk = lc;
                }
                int fromX = Math.max(startX, chunkX * 16), toX = Math.min(endX, chunkX * 16 + 16);
                int fromZ = Math.max(startZ, chunkZ * 16), toZ = Math.min(endZ, chunkZ * 16 + 16);
                for (int blockZ = fromZ; blockZ < toZ; blockZ++) for (int blockX = fromX; blockX < toX; blockX++) {
                    int px = blockX - startX, pz = blockZ - startZ, idx = px + pz * tileSize;
                    if (chunk == null) { colors[idx] = 0xFF222222; heights[idx] = 64; loadedMask[idx] = false; continue; }
                    int surfaceY = surfaceHeights.resolve(level, chunk, blockX, blockZ);
                    mutablePos.set(blockX, surfaceY, blockZ);
                    BlockState state = chunk.getBlockState(mutablePos);
                    colors[idx] = 0xFF000000 | (BlockColorResolver.resolve(state) & 0x00FFFFFF);
                    heights[idx] = surfaceY; loadedMask[idx] = true;
                }
            }
        }

        return new TileSnapshot(level.dimension(), tileX, tileZ, zoom, tileSize, colors, heights, loadedMask);
    }
}
