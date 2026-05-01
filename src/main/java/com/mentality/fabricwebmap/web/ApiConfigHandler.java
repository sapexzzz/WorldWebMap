package com.mentality.fabricwebmap.web;

import com.mentality.fabricwebmap.config.WebMapConfig;
import com.mentality.fabricwebmap.util.JsonUtil;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** GET /api/config */
public class ApiConfigHandler implements HttpHandler {

    private final WebMapConfig config;

    public ApiConfigHandler(WebMapConfig config) {
        this.config = config;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(405, -1);
            return;
        }

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("tileSize", config.getTileSize());
        body.put("minZoom", -4);
        body.put("maxZoom", 4);
        body.put("dimensions", List.of("overworld", "the_nether", "the_end"));

        byte[] data = JsonUtil.toJson(body).getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.sendResponseHeaders(200, data.length);
        try (OutputStream out = exchange.getResponseBody()) {
            out.write(data);
        }
    }
}
