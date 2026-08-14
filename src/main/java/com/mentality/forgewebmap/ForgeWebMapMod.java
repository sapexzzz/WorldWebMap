package com.mentality.forgewebmap;

import com.mentality.forgewebmap.commands.WebMapCommands;
import com.mentality.forgewebmap.config.WebMapConfig;
import com.mentality.forgewebmap.player.PlayerMarkerService;
import com.mentality.forgewebmap.lifecycle.WebMapLifecycleCoordinator;
import com.mentality.forgewebmap.render.ChunkLoadListener;
import com.mentality.forgewebmap.render.TileRenderManager;
import com.mentality.forgewebmap.web.WebServerService;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;

import java.nio.file.Path;
import java.nio.file.Paths;

@Mod(ForgeWebMapMod.MOD_ID)
public class ForgeWebMapMod {

    public static final String MOD_ID = "forgewebmap";
    public static final String MOD_VERSION = "0.2.2";
    private static final Logger LOGGER = LogUtils.getLogger();

    private static WebMapConfig config;
    private static WebServerService webServer;
    private static TileRenderManager renderManager;
    private static PlayerMarkerService playerMarkerService;
    private static ChunkLoadListener chunkLoadListener;
    private static net.minecraft.server.MinecraftServer currentServer;
    private static WebMapLifecycleCoordinator lifecycle;

    public ForgeWebMapMod() {
        LOGGER.info("World Web Map initializing...");

        // Load config early so other services can use it
        Path configDir = Paths.get("config");
        config = new WebMapConfig(configDir);
        config.load();
        playerMarkerService = new PlayerMarkerService();
        renderManager = new TileRenderManager(config);
        lifecycle = createLifecycle();

        // Register Forge event bus listeners
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onServerStarted(ServerStartedEvent event) {
        currentServer = event.getServer();
        playerMarkerService.setServer(event.getServer());
        try {
            lifecycle.setServer(event.getServer());
            lifecycle.startOrStop();
            LOGGER.info("World Web Map started. Web map available at http://{}:{}",
                    config.getBindAddress().equals("0.0.0.0") ? "server-ip" : config.getBindAddress(),
                    config.getPort());
        } catch (Exception e) {
            LOGGER.error("Failed to start ForgeWebMap web server", e);
        }

        // Register chunk load listener for auto-render
        if (config.isEnabled()) { chunkLoadListener = new ChunkLoadListener(config, renderManager); MinecraftForge.EVENT_BUS.register(chunkLoadListener); }
        if (config.isEnableAutoRender()) {
            LOGGER.info("World Web Map auto-render enabled: tiles will be queued as players explore.");
        }

        LOGGER.info("World Web Map started successfully.");
    }

    @SubscribeEvent
    public void onServerStopping(ServerStoppingEvent event) {
        LOGGER.info("World Web Map stopping...");
        if (chunkLoadListener != null) {
            MinecraftForge.EVENT_BUS.unregister(chunkLoadListener);
        }
        if (lifecycle != null) lifecycle.stop();
        webServer = null;
        LOGGER.info("World Web Map stopped.");
    }

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END && playerMarkerService != null) playerMarkerService.refresh(config.isEnablePlayerMarkers());
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        WebMapCommands.register(event.getDispatcher(), config, renderManager, playerMarkerService);
    }

    // ── Static accessors for use by commands / handlers ──────────────────────

    public static WebMapConfig getConfig() {
        return config;
    }

    public static WebServerService getWebServer() {
        return webServer;
    }

    public static TileRenderManager getRenderManager() {
        return renderManager;
    }

    public static PlayerMarkerService getPlayerMarkerService() {
        return playerMarkerService;
    }

    /**
     * Reload config and optionally restart web server if network settings changed.
     */
    public static void reload() {
        LOGGER.info("Reloading ForgeWebMap config...");
        String oldBind = config.getBindAddress();
        int oldPort = config.getPort();

        config.load();

        try { lifecycle.reload(oldBind, oldPort); } catch (Exception e) { LOGGER.error("Failed to apply web server lifecycle after reload", e); }
        if (!config.isEnabled() && chunkLoadListener != null) { MinecraftForge.EVENT_BUS.unregister(chunkLoadListener); chunkLoadListener = null; }
        if (config.isEnabled() && chunkLoadListener == null && currentServer != null) { chunkLoadListener = new ChunkLoadListener(config, renderManager); MinecraftForge.EVENT_BUS.register(chunkLoadListener); }
        LOGGER.info("World Web Map reload complete.");
    }

    private static WebMapLifecycleCoordinator createLifecycle() {
        WebMapLifecycleCoordinator.Settings settings = new WebMapLifecycleCoordinator.Settings() { public boolean enabled() { return config.isEnabled(); } public String bindAddress() { return config.getBindAddress(); } public int port() { return config.getPort(); } };
        WebMapLifecycleCoordinator.RenderService render = new WebMapLifecycleCoordinator.RenderService() { public void setServer(Object server) { renderManager.setServer((net.minecraft.server.MinecraftServer) server); } public void start() { renderManager.start(); } public void stop() { renderManager.stop(); } public boolean isRunning() { return renderManager.getState() == TileRenderManager.State.RUNNING; } };
        return new WebMapLifecycleCoordinator(settings, render, () -> new WebMapLifecycleCoordinator.WebService() {
            private final WebServerService service = webServer = new WebServerService(config, renderManager, playerMarkerService);
            public void setServer(Object server) { service.setServer((net.minecraft.server.MinecraftServer) server); }
            public void start() throws Exception { service.start(); }
            public void stop() { service.stop(); }
            public boolean isRunning() { return service.isRunning(); }
        });
    }
}
