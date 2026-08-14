package com.mentality.forgewebmap.web;

import com.mentality.forgewebmap.config.WebMapConfig;
import com.mentality.forgewebmap.player.PlayerMarkerService;
import com.mentality.forgewebmap.render.TileRenderManager;
import com.mentality.forgewebmap.storage.TileStorage;
import com.mojang.logging.LogUtils;
import com.sun.net.httpserver.HttpServer;
import org.slf4j.Logger;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.Paths;
import java.util.concurrent.*;

/**
 * Embedded HTTP server using com.sun.net.httpserver (no extra dependencies).
 * Registers all URL handlers.
 */
public class WebServerService {

    private static final Logger LOGGER = LogUtils.getLogger();

    private final WebMapConfig config;
    private final TileRenderManager renderManager;
    private final PlayerMarkerService playerMarkerService;

    private HttpServer httpServer;
    private boolean running = false;
    private ApiBiomeHandler biomeHandler;
    private ThreadPoolExecutor executor;
    private net.minecraft.server.MinecraftServer server;

    public WebServerService(WebMapConfig config,
                            TileRenderManager renderManager,
                            PlayerMarkerService playerMarkerService) {
        this.config = config;
        this.renderManager = renderManager;
        this.playerMarkerService = playerMarkerService;
    }

    public void start() throws IOException {
        InetSocketAddress address = new InetSocketAddress(config.getBindAddress(), config.getPort());
        httpServer = HttpServer.create(address, 50);

        biomeHandler = new ApiBiomeHandler();
        if (server != null) biomeHandler.setServer(server);
        java.nio.file.Path worldRoot=biomeHandler.isServerAvailable()?server.getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT):Paths.get("world"); TileStorage tileStorage=TileStorage.forWorld(config,worldRoot);

        // Static frontend files
        StaticFileHandler staticHandler = new StaticFileHandler();
        httpServer.createContext("/", staticHandler);

        // Tile PNG endpoint
        TileHttpHandler tileHandler = new TileHttpHandler(tileStorage);
        httpServer.createContext("/tiles/", tileHandler);

        // API endpoints
        httpServer.createContext("/api/status", new ApiStatusHandler(config, renderManager, this::isRunning, this::hasServer));
        httpServer.createContext("/api/players", new ApiPlayersHandler(playerMarkerService));
        httpServer.createContext("/api/config", new ApiConfigHandler(config));
        httpServer.createContext("/api/biome", biomeHandler);

        // Use a small thread pool for HTTP; keep it separate from render workers
        executor = HttpExecutorFactory.create();
        httpServer.setExecutor(executor);

        httpServer.start();
        running = true;
        LOGGER.info("Web server started on {}:{}", config.getBindAddress(), config.getPort());
    }

    public void stop() {
        if (httpServer != null) {
            httpServer.stop(1);
            httpServer = null;
        }
        running = false;
        if (executor != null) { executor.shutdown(); try { executor.awaitTermination(2, TimeUnit.SECONDS); } catch (InterruptedException e) { Thread.currentThread().interrupt(); } if (!executor.isTerminated()) executor.shutdownNow(); executor = null; }
        LOGGER.info("Web server stopped.");
    }

    public boolean isRunning() {
        return running;
    }

    /** Pass the server reference to the biome handler after server start. */
    public void setServer(net.minecraft.server.MinecraftServer server) {
        this.server=server; if (biomeHandler != null) biomeHandler.setServer(server);
    }
    boolean hasServer() { return biomeHandler != null && biomeHandler.isServerAvailable(); }

    public String getAddress() {
        return config.getBindAddress() + ":" + config.getPort();
    }
    ThreadPoolExecutor ownedExecutorForTesting() { return executor; }
}
