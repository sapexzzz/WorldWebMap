package com.mentality.forgewebmap.web;

import com.mentality.forgewebmap.storage.TileStorage;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Handles GET /tiles/{dimension}/{zoom}/{x}/{y}.png
 *
 * Security: all paths are resolved relative to tilesRoot and validated
 * to prevent path traversal attacks.
 */
public class TileHttpHandler implements HttpHandler {

    private final TileStorage tileStorage;

    public TileHttpHandler(TileStorage tileStorage) {
        this.tileStorage = tileStorage;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendStatus(exchange, 405);
            return;
        }

        // URI example: /tiles/overworld/0/3/-2.png
        String uri = exchange.getRequestURI().getPath();

        // Strip leading /tiles/
        String relative = uri.startsWith("/tiles/") ? uri.substring("/tiles/".length()) : uri;

        // Parse: dimension/zoom/x_z.png  (file name is "x_z.png")
        // URL format: dimension/zoom/x/z.png  (4 parts)
        String[] parts = relative.split("/");
        if (parts.length != 4) {
            sendStatus(exchange, 404);
            return;
        }

        String dimension = parts[0];
        String zoomStr = parts[1];
        String xStr = parts[2];
        String zStr = parts[3].replace(".png", "");

        int zoom, tileX, tileZ;
        try {
            zoom = Integer.parseInt(zoomStr);
            tileX = Integer.parseInt(xStr);
            tileZ = Integer.parseInt(zStr);
        } catch (NumberFormatException e) {
            sendStatus(exchange, 400);
            return;
        }

        // Resolve and validate path (anti path-traversal)
        Path tilesRoot = tileStorage.getTilesRoot();
        Path tilePath = tileStorage.getTilePath(dimension, zoom, tileX, tileZ).normalize();

        if (!tilePath.startsWith(tilesRoot)) {
            // Path traversal attempt
            sendStatus(exchange, 403);
            return;
        }

        if (!Files.exists(tilePath)) {
            sendStatus(exchange, 404);
            return;
        }

        byte[] data;
        try {
            data = Files.readAllBytes(tilePath);
        } catch (IOException e) {
            sendStatus(exchange, 500);
            return;
        }

        exchange.getResponseHeaders().set("Content-Type", "image/png");
        exchange.getResponseHeaders().set("Cache-Control", "no-cache");
        exchange.sendResponseHeaders(200, data.length);
        try (OutputStream out = exchange.getResponseBody()) {
            out.write(data);
        }
    }

    private static void sendStatus(HttpExchange exchange, int code) throws IOException {
        byte[] msg = String.valueOf(code).getBytes();
        exchange.sendResponseHeaders(code, msg.length);
        try (OutputStream out = exchange.getResponseBody()) {
            out.write(msg);
        }
    }
}
