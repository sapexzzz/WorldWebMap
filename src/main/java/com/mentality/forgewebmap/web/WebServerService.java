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
import java.util.concurrent.Executors;

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

        TileStorage tileStorage = new TileStorage(config);

        // Static frontend files
        StaticFileHandler staticHandler = new StaticFileHandler();
        httpServer.createContext("/", staticHandler);

        // Tile PNG endpoint
        TileHttpHandler tileHandler = new TileHttpHandler(tileStorage);
        httpServer.createContext("/tiles/", tileHandler);

        // API endpoints
        httpServer.createContext("/api/status", new ApiStatusHandler(config, renderManager));
        httpServer.createContext("/api/players", new ApiPlayersHandler(playerMarkerService));
        httpServer.createContext("/api/config", new ApiConfigHandler(config));

        // Use a small thread pool for HTTP; keep it separate from render workers
        httpServer.setExecutor(Executors.newFixedThreadPool(4, r -> {
            Thread t = new Thread(r, "forgewebmap-http");
            t.setDaemon(true);
            return t;
        }));

        httpServer.start();
        running = true;
        LOGGER.info("Web server started on {}:{}", config.getBindAddress(), config.getPort());
    }

    public void stop() {
        if (httpServer != null) {
            httpServer.stop(1);
            running = false;
            LOGGER.info("Web server stopped.");
        }
    }

    public boolean isRunning() {
        return running;
    }

    public String getAddress() {
        return config.getBindAddress() + ":" + config.getPort();
    }
}
