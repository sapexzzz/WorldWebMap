package com.mentality.fabricwebmap;

import com.mentality.fabricwebmap.commands.WebMapCommands;
import com.mentality.fabricwebmap.config.WebMapConfig;
import com.mentality.fabricwebmap.player.PlayerMarkerService;
import com.mentality.fabricwebmap.lifecycle.WebMapLifecycleCoordinator;
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
import net.minecraft.server.MinecraftServer;

public class FabricWebMapMod implements ModInitializer {

    public static final String MOD_ID = "fabricwebmap";
    public static final String MOD_VERSION = "0.2.2";
    private static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static WebMapConfig config;
    private static WebServerService webServer;
    private static TileRenderManager renderManager;
    private static PlayerMarkerService playerMarkerService;
    private static ChunkLoadListener chunkLoadListener;
    private static MinecraftServer currentServer;
    private static WebMapLifecycleCoordinator lifecycle;

    @Override
    public void onInitialize() {
        LOGGER.info("World Web Map initializing...");

        // Config lives in <server>/config/
        Path configDir = FabricLoader.getInstance().getConfigDir();
        config = new WebMapConfig(configDir);
        config.load();
        playerMarkerService = new PlayerMarkerService();
        renderManager = new TileRenderManager(config);
        lifecycle = createLifecycle();
        chunkLoadListener = new ChunkLoadListener(config, renderManager);

        // Server started — start web server and render workers
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            currentServer = server;
            playerMarkerService.setServer(server);
            try {
                lifecycle.setServer(server);
                lifecycle.startOrStop();
                LOGGER.info("World Web Map started. Web map available at http://{}:{}",
                        config.getBindAddress().equals("0.0.0.0") ? "server-ip" : config.getBindAddress(),
                        config.getPort());
            } catch (Exception e) {
                LOGGER.error("Failed to start FabricWebMap web server", e);
            }

            if (config.isEnableAutoRender()) {
                LOGGER.info("World Web Map auto-render enabled: tiles will be queued as players explore.");
            }
            LOGGER.info("World Web Map started successfully.");
        });

        // Server stopping — shut everything down
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            LOGGER.info("World Web Map stopping...");
            if (lifecycle != null) lifecycle.stop();
            webServer = null;
            LOGGER.info("World Web Map stopped.");
        });

        // Server tick — drives the render queue (replaces Forge TickEvent)
        ServerTickEvents.END_SERVER_TICK.register(server -> { playerMarkerService.refresh(config.isEnablePlayerMarkers()); renderManager.onServerTick(server); });

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

        try {
            lifecycle.reload(oldBind, oldPort);
        } catch (Exception e) { LOGGER.error("Failed to apply web server lifecycle after reload", e); }
        LOGGER.info("World Web Map reload complete.");
    }

    private static WebMapLifecycleCoordinator createLifecycle() {
        WebMapLifecycleCoordinator.Settings settings = new WebMapLifecycleCoordinator.Settings() {
            public boolean enabled() { return config.isEnabled(); }
            public String bindAddress() { return config.getBindAddress(); }
            public int port() { return config.getPort(); }
        };
        WebMapLifecycleCoordinator.RenderService render = new WebMapLifecycleCoordinator.RenderService() {
            public void setServer(Object server) { renderManager.setServer((MinecraftServer) server); }
            public void start() { renderManager.start(); }
            public void stop() { renderManager.stop(); }
            public boolean isRunning() { return renderManager.getState() == TileRenderManager.State.RUNNING; }
        };
        return new WebMapLifecycleCoordinator(settings, render, () -> new WebMapLifecycleCoordinator.WebService() {
            private final WebServerService service = webServer = new WebServerService(config, renderManager, playerMarkerService);
            public void setServer(Object server) { service.setServer((MinecraftServer) server); }
            public void start() throws Exception { service.start(); }
            public void stop() { service.stop(); }
            public boolean isRunning() { return service.isRunning(); }
        });
    }
}
