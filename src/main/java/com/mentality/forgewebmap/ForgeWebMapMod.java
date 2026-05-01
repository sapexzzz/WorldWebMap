package com.mentality.forgewebmap;

import com.mentality.forgewebmap.commands.WebMapCommands;
import com.mentality.forgewebmap.config.WebMapConfig;
import com.mentality.forgewebmap.player.PlayerMarkerService;
import com.mentality.forgewebmap.render.ChunkLoadListener;
import com.mentality.forgewebmap.render.TileRenderManager;
import com.mentality.forgewebmap.web.WebServerService;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;

import java.nio.file.Path;
import java.nio.file.Paths;

@Mod(ForgeWebMapMod.MOD_ID)
public class ForgeWebMapMod {

    public static final String MOD_ID = "forgewebmap";
    public static final String MOD_VERSION = "0.1.0";
    private static final Logger LOGGER = LogUtils.getLogger();

    private static WebMapConfig config;
    private static WebServerService webServer;
    private static TileRenderManager renderManager;
    private static PlayerMarkerService playerMarkerService;
    private static ChunkLoadListener chunkLoadListener;

    public ForgeWebMapMod() {
        LOGGER.info("World Web Map initializing...");

        // Load config early so other services can use it
        Path configDir = Paths.get("config");
        config = new WebMapConfig(configDir);
        config.load();

        playerMarkerService = new PlayerMarkerService();
        renderManager = new TileRenderManager(config);

        // Register Forge event bus listeners
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onServerStarted(ServerStartedEvent event) {
        if (!config.isEnabled()) {
            LOGGER.info("World Web Map is disabled in config, skipping web server start.");
            return;
        }

        // Pass server reference to services that need it
        playerMarkerService.setServer(event.getServer());
        renderManager.setServer(event.getServer());

        // Start web server
        webServer = new WebServerService(config, renderManager, playerMarkerService);
        try {
            webServer.start();
            webServer.setServer(event.getServer());
            LOGGER.info("World Web Map started. Web map available at http://{}:{}",
                    config.getBindAddress().equals("0.0.0.0") ? "server-ip" : config.getBindAddress(),
                    config.getPort());
        } catch (Exception e) {
            LOGGER.error("Failed to start ForgeWebMap web server", e);
        }

        // Start render manager worker threads
        renderManager.start();

        // Register chunk load listener for auto-render
        chunkLoadListener = new ChunkLoadListener(config, renderManager);
        MinecraftForge.EVENT_BUS.register(chunkLoadListener);
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
        if (renderManager != null) {
            renderManager.stop();
        }
        if (webServer != null) {
            webServer.stop();
        }
        LOGGER.info("World Web Map stopped.");
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
