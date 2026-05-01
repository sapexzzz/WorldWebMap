package com.mentality.fabricwebmap.web;

import com.mentality.fabricwebmap.config.WebMapConfig;
import com.mentality.fabricwebmap.player.PlayerMarkerService;
import com.mentality.fabricwebmap.render.TileRenderManager;
import com.mentality.fabricwebmap.storage.TileStorage;
import com.sun.net.httpserver.HttpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

/**
 * Embedded HTTP server using com.sun.net.httpserver (no extra dependencies).
 * Registers all URL handlers.
 */
public class WebServerService {

    private static final Logger LOGGER = LoggerFactory.getLogger("fabricwebmap");

    private final WebMapConfig config;
    private final TileRenderManager renderManager;
    private final PlayerMarkerService playerMarkerService;

    private HttpServer httpServer;
    private boolean running = false;
    private ApiBiomeHandler biomeHandler;

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

        httpServer.createContext("/", new StaticFileHandler());
        httpServer.createContext("/tiles/", new TileHttpHandler(tileStorage));
        httpServer.createContext("/api/status", new ApiStatusHandler(config, renderManager));
        httpServer.createContext("/api/players", new ApiPlayersHandler(playerMarkerService));
        httpServer.createContext("/api/config", new ApiConfigHandler(config));
        biomeHandler = new ApiBiomeHandler();
        httpServer.createContext("/api/biome", biomeHandler);

        httpServer.setExecutor(Executors.newFixedThreadPool(4, r -> {
            Thread t = new Thread(r, "fabricwebmap-http");
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

    public void setServer(net.minecraft.server.MinecraftServer server) {
        if (biomeHandler != null) biomeHandler.setServer(server);
    }

    public String getAddress() {
        return config.getBindAddress() + ":" + config.getPort();
    }
}
