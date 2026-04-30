package com.mentality.forgewebmap.web;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/**
 * Serves static files from the /web/ classpath directory:
 *   GET /          -> index.html
 *   GET /app.js    -> app.js
 *   GET /style.css -> style.css
 *
 * Falls through to 404 for unknown paths.
 */
public class StaticFileHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendStatus(exchange, 405);
            return;
        }

        String path = exchange.getRequestURI().getPath();

        // Only serve known static files; block everything else from this handler
        String resource;
        String contentType;

        switch (path) {
            case "/":
            case "/index.html":
                resource = "/web/index.html";
                contentType = "text/html; charset=utf-8";
                break;
            case "/app.js":
                resource = "/web/app.js";
                contentType = "application/javascript; charset=utf-8";
                break;
            case "/style.css":
                resource = "/web/style.css";
                contentType = "text/css; charset=utf-8";
                break;
            default:
                sendStatus(exchange, 404);
                return;
        }

        try (InputStream in = StaticFileHandler.class.getResourceAsStream(resource)) {
            if (in == null) {
                sendStatus(exchange, 404);
                return;
            }
            byte[] bytes = in.readAllBytes();
            exchange.getResponseHeaders().set("Content-Type", contentType);
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream out = exchange.getResponseBody()) {
                out.write(bytes);
            }
        }
    }

    private static void sendStatus(HttpExchange exchange, int code) throws IOException {
        byte[] msg = (code + " " + statusText(code)).getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=utf-8");
        exchange.sendResponseHeaders(code, msg.length);
        try (OutputStream out = exchange.getResponseBody()) {
            out.write(msg);
        }
    }

    private static String statusText(int code) {
        return switch (code) {
            case 404 -> "Not Found";
            case 405 -> "Method Not Allowed";
            default -> "Error";
        };
    }
}
