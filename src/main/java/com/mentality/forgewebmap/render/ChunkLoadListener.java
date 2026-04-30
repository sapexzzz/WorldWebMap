package com.mentality.forgewebmap.render;

import com.mentality.forgewebmap.config.WebMapConfig;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.LevelAccessor;
import net.minecraftforge.event.level.ChunkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * Listens to chunk load events and queues tiles for render.
 * Registered on MinecraftForge.EVENT_BUS after server start.
 * Safe: only queues jobs (no world access here). Actual world sampling
 * happens later on the main server thread inside TileRenderManager.
 */
public class ChunkLoadListener {

    private final WebMapConfig config;
    private final TileRenderManager renderManager;

    public ChunkLoadListener(WebMapConfig config, TileRenderManager renderManager) {
        this.config = config;
        this.renderManager = renderManager;
    }

    @SubscribeEvent
    public void onChunkLoad(ChunkEvent.Load event) {
        LevelAccessor level = event.getLevel();
        // Only handle server-side chunks (ignore client world on integrated server)
        if (!(level instanceof ServerLevel serverLevel)) return;

        int chunkX = event.getChunk().getPos().x;
        int chunkZ = event.getChunk().getPos().z;
        int tileSize = config.getTileSize(); // default 256

        // 1 tile = tileSize blocks, 1 chunk = 16 blocks
        int tileX = Math.floorDiv(chunkX * 16, tileSize);
        int tileZ = Math.floorDiv(chunkZ * 16, tileSize);

        // Debounce: delay rendering until the tile area stops loading chunks.
        // This prevents rendering the tile with gray "unloaded" patches when only
        // a few of the tile's 16x16 chunks have arrived so far.
        renderManager.markChunkLoaded(serverLevel.dimension(), tileX, tileZ);
    }
}
