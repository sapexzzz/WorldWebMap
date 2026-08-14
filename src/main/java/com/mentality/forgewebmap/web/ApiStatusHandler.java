package com.mentality.forgewebmap.web;

import com.mentality.forgewebmap.ForgeWebMapMod;
import com.mentality.forgewebmap.config.WebMapConfig;
import com.mentality.forgewebmap.render.TileRenderManager;
import com.mentality.forgewebmap.util.JsonUtil;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BooleanSupplier;

/** GET /api/status */
public class ApiStatusHandler implements HttpHandler {

    private final WebMapConfig config;
    private final TileRenderManager renderManager;
    private final BooleanSupplier webRunning, serverAvailable;

    public ApiStatusHandler(WebMapConfig config, TileRenderManager renderManager) {
        this(config, renderManager, () -> ForgeWebMapMod.getWebServer() != null && ForgeWebMapMod.getWebServer().isRunning(), () -> ForgeWebMapMod.getWebServer() != null && ForgeWebMapMod.getWebServer().hasServer());
    }
    ApiStatusHandler(WebMapConfig config, TileRenderManager renderManager, BooleanSupplier webRunning, BooleanSupplier serverAvailable) { this.config=config; this.renderManager=renderManager; this.webRunning=webRunning; this.serverAvailable=serverAvailable; }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(405, -1);
            return;
        }

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("mod", "World Web Map");
        body.put("version", ForgeWebMapMod.MOD_VERSION);
        body.put("enabled", config.isEnabled());
        body.put("renderState", renderManager.getState().toString());
        body.put("serverRunning", webRunning.getAsBoolean() && serverAvailable.getAsBoolean());
        body.put("renderQueueSize", renderManager.getQueueSize());
        body.put("activeWorkers", renderManager.getActiveWorkers());
        body.put("renderedTiles", renderManager.getRenderedTiles());
        body.put("fullRenderRemaining", renderManager.getFullRenderRemaining());
        body.put("fullRenderActive", renderManager.getFullRenderPlan() != null);
        body.put("dimensions", List.of("overworld", "the_nether", "the_end"));

        sendJson(exchange, body);
    }

    static void sendJson(HttpExchange exchange, Map<String, Object> body) throws IOException {
        byte[] data = JsonUtil.toJson(body).getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.sendResponseHeaders(200, data.length);
        try (OutputStream out = exchange.getResponseBody()) {
            out.write(data);
        }
    }

    static void sendJson(HttpExchange exchange, String json) throws IOException {
        byte[] data = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.sendResponseHeaders(200, data.length);
        try (OutputStream out = exchange.getResponseBody()) {
            out.write(data);
        }
    }
}
