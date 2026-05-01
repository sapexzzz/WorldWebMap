package com.mentality.fabricwebmap;

import com.mentality.fabricwebmap.commands.WebMapCommands;
import com.mentality.fabricwebmap.config.WebMapConfig;
import com.mentality.fabricwebmap.player.PlayerMarkerService;
import com.mentality.fabricwebmap.render.ChunkLoadListener;
import com.mentality.fabricwebmap.render.TileRenderManager;
import com.mentality.fabricwebmap.web.WebServerService;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

public class FabricWebMapMod implements ModInitializer {

    public static final String MOD_ID = "fabricwebmap";
    public static final String MOD_VERSION = "0.2.1";
    private static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static WebMapConfig config;
    private static WebServerService webServer;
    private static TileRenderManager renderManager;
    private static PlayerMarkerService playerMarkerService;
    private static ChunkLoadListener chunkLoadListener;

    @Override
    public void onInitialize() {
        LOGGER.info("World Web Map initializing...");

        // Config lives in <server>/config/
        Path configDir = FabricLoader.getInstance().getConfigDir();
        config = new WebMapConfig(configDir);
        config.load();

        playerMarkerService = new PlayerMarkerService();
        renderManager = new TileRenderManager(config);
        chunkLoadListener = new ChunkLoadListener(config, renderManager);

        // Server started — start web server and render workers
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            if (!config.isEnabled()) {
                LOGGER.info("World Web Map is disabled in config, skipping web server start.");
                return;
            }

            playerMarkerService.setServer(server);
            renderManager.setServer(server);

            webServer = new WebServerService(config, renderManager, playerMarkerService);
            try {
                webServer.start();
                webServer.setServer(server);
                LOGGER.info("World Web Map started. Web map available at http://{}:{}",
                        config.getBindAddress().equals("0.0.0.0") ? "server-ip" : config.getBindAddress(),
                        config.getPort());
            } catch (Exception e) {
                LOGGER.error("Failed to start FabricWebMap web server", e);
            }

            renderManager.start();

            if (config.isEnableAutoRender()) {
                LOGGER.info("World Web Map auto-render enabled: tiles will be queued as players explore.");
            }
            LOGGER.info("World Web Map started successfully.");
        });

        // Server stopping — shut everything down
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            LOGGER.info("World Web Map stopping...");
            if (renderManager != null) renderManager.stop();
            if (webServer != null) webServer.stop();
            LOGGER.info("World Web Map stopped.");
        });

        // Server tick — drives the render queue (replaces Forge TickEvent)
        ServerTickEvents.END_SERVER_TICK.register(server -> renderManager.onServerTick(server));

        // Chunk load — auto-render new chunks (replaces Forge ChunkEvent.Load)
        ServerChunkEvents.CHUNK_LOAD.register((world, chunk) -> chunkLoadListener.onChunkLoad(world, chunk));

        // Commands (replaces Forge RegisterCommandsEvent)
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                WebMapCommands.register(dispatcher, config, renderManager, playerMarkerService));

        LOGGER.info("World Web Map initialization complete.");
    }

    // ── Static accessors (used by commands and handlers) ──────────────────────

    public static WebMapConfig getConfig() { return config; }

    public static WebServerService getWebServer() { return webServer; }

    public static TileRenderManager getRenderManager() { return renderManager; }

    public static PlayerMarkerService getPlayerMarkerService() { return playerMarkerService; }

    /**
     * Reload config and optionally restart the web server if network settings changed.
     */
    public static void reload() {
        LOGGER.info("Reloading FabricWebMap config...");
        String oldBind = config.getBindAddress();
        int oldPort = config.getPort();

        config.load();

        boolean networkChanged = !config.getBindAddress().equals(oldBind) || config.getPort() != oldPort;
        if (networkChanged && webServer != null) {
            LOGGER.info("Network config changed, restarting web server...");
            webServer.stop();
            webServer = new WebServerService(config, renderManager, playerMarkerService);
            try {
                webServer.start();
            } catch (Exception e) {
                LOGGER.error("Failed to restart web server after reload", e);
            }
        }
        LOGGER.info("World Web Map reload complete.");
    }
}
