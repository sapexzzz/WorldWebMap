package com.mentality.fabricwebmap.web;

import com.mentality.fabricwebmap.render.SurfaceHeightResolver;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;

/**
 * GET /api/biome?dim=overworld&x=100&z=200
 *
 * Returns the biome name at the given block coordinates.
 * Response: {"biome": "Plains", "id": "minecraft:plains"}
 */
public class ApiBiomeHandler implements HttpHandler {
    private volatile MinecraftServer server;
    @FunctionalInterface interface BiomeLookup { Future<String> lookup(String dimension, int x, int z) throws Exception; }
    private BiomeLookup lookup;
    private BooleanSupplier serverAvailable;
    private long timeoutMillis;

    public ApiBiomeHandler() { lookup = this::lookupOnServer; serverAvailable = () -> server != null; timeoutMillis = 250; }
    ApiBiomeHandler(BiomeLookup lookup, BooleanSupplier serverAvailable, long timeoutMillis) { this.lookup = lookup; this.serverAvailable = serverAvailable; this.timeoutMillis = timeoutMillis; }

    public void setServer(MinecraftServer server) {
        this.server = server;
    }
    boolean isServerAvailable() { return serverAvailable.getAsBoolean(); }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(405, -1);
            return;
        }

        String query = exchange.getRequestURI().getRawQuery();
        if (query == null) {
            sendError(exchange, 400, "Missing query parameters");
            return;
        }

        String dim = null;
        Integer x = null, z = null;
        for (String part : query.split("&")) {
            String[] kv = part.split("=", 2);
            if (kv.length != 2) continue;
            switch (kv[0]) {
                case "dim" -> dim = kv[1];
                case "x"   -> { try { x = Integer.parseInt(kv[1]); } catch (NumberFormatException ignored) {} }
                case "z"   -> { try { z = Integer.parseInt(kv[1]); } catch (NumberFormatException ignored) {} }
            }
        }

        if (dim == null || x == null || z == null) {
            sendError(exchange, 400, "Required: dim, x, z");
            return;
        }

        if (!isKnownDimension(dim)) { sendError(exchange, 400, "Unknown dimension"); return; }
        if (!serverAvailable.getAsBoolean()) {
            sendError(exchange, 503, "Server not ready");
            return;
        }

        String biomeId;
        try {
            biomeId = lookup.lookup(dim, x, z).get(timeoutMillis, TimeUnit.MILLISECONDS);
        } catch (Exception e) { sendError(exchange, 503, "Biome lookup temporarily unavailable"); return; }
        if (biomeId == null) { sendError(exchange, 404, "Dimension not found"); return; }

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("biome", formatBiomeName(biomeId));
        body.put("id", biomeId);

        ApiStatusHandler.sendJson(exchange, body);
    }
    private Future<String> lookupOnServer(String dimension, int x, int z) throws Exception {
        MinecraftServer srv = server;
        if (srv == null) return java.util.concurrent.CompletableFuture.completedFuture(null);
        return srv.submit(() -> { ServerLevel level = srv.getLevel(switch (dimension) { case "overworld" -> Level.OVERWORLD; case "the_nether" -> Level.NETHER; default -> Level.END; }); if (level == null) return null; SurfaceHeightResolver resolver = new SurfaceHeightResolver(); return BiomeSurfaceLookup.lookup(dimension, x, z, (d, bx, bz) -> resolver.resolve(level, bx, bz), (d, bx, y, bz) -> level.getBiome(new BlockPos(bx, y, bz)).unwrapKey().map(k -> k.location().toString()).orElse("unknown")); });
    }
    private static boolean isKnownDimension(String dim) { return "overworld".equals(dim) || "the_nether".equals(dim) || "the_end".equals(dim); }

    private static String formatBiomeName(String id) {
        if (id == null || id.equals("unknown")) return "Unknown";

        int colon = id.indexOf(':');
        if (colon < 0) return capitalize(id);

        String namespace = id.substring(0, colon);
        String path      = id.substring(colon + 1);

        String[] words = path.replace('_', ' ').split(" ");
        StringBuilder sb = new StringBuilder();
        for (String w : words) {
            if (!w.isEmpty()) {
                if (sb.length() > 0) sb.append(' ');
                sb.append(Character.toUpperCase(w.charAt(0)));
                sb.append(w.substring(1));
            }
        }

        if (!"minecraft".equals(namespace)) {
            return "[" + namespace + "] " + sb;
        }
        return sb.toString();
    }

    private static String capitalize(String s) {
        if (s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1).replace('_', ' ');
    }

    private static void sendError(HttpExchange exchange, int code, String msg) throws IOException {
        byte[] data = ("{\"error\":\"" + msg + "\"}").getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.sendResponseHeaders(code, data.length);
        try (var out = exchange.getResponseBody()) {
            out.write(data);
        }
    }
}
