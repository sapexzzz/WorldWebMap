package com.mentality.fabricwebmap.render;

import com.mentality.fabricwebmap.config.WebMapConfig;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.LevelChunk;

/**
 * Listens to chunk load events and queues tiles for render.
 * Registered via ServerChunkEvents.CHUNK_LOAD in FabricWebMapMod.onInitialize().
 * Safe: only queues jobs (no world access). Actual world sampling
 * happens later on the main server thread inside TileRenderManager.
 */
public class ChunkLoadListener {

    private final WebMapConfig config;
    private final TileRenderManager renderManager;

    public ChunkLoadListener(WebMapConfig config, TileRenderManager renderManager) {
        this.config = config;
        this.renderManager = renderManager;
    }

    /**
     * Called by ServerChunkEvents.CHUNK_LOAD (always server-side in Fabric).
     * Equivalent to Forge's ChunkEvent.Load handler.
     */
    public void onChunkLoad(ServerLevel world, LevelChunk chunk) {
        int chunkX = chunk.getPos().x;
        int chunkZ = chunk.getPos().z;
        int tileSize = config.getTileSize();

        int tileX = Math.floorDiv(chunkX * 16, tileSize);
        int tileZ = Math.floorDiv(chunkZ * 16, tileSize);

        renderManager.markChunkLoaded(world.dimension(), tileX, tileZ);
    }
}
