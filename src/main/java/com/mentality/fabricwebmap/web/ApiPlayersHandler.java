package com.mentality.fabricwebmap.web;

import com.mentality.fabricwebmap.player.PlayerMarkerService;
import com.mentality.fabricwebmap.util.JsonUtil;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** GET /api/players */
public class ApiPlayersHandler implements HttpHandler {

    private final PlayerMarkerService playerMarkerService;

    public ApiPlayersHandler(PlayerMarkerService playerMarkerService) {
        this.playerMarkerService = playerMarkerService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(405, -1);
            return;
        }

        List<PlayerMarkerService.PlayerInfo> players = playerMarkerService.getPlayers();
        List<Map<String, Object>> list = new ArrayList<>();

        for (PlayerMarkerService.PlayerInfo p : players) {
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("name", p.name());
            entry.put("dimension", p.dimension());
            entry.put("x", p.x());
            entry.put("y", p.y());
            entry.put("z", p.z());
            entry.put("yaw", p.yaw());
            list.add(entry);
        }

        byte[] data = JsonUtil.toJson(list).getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.sendResponseHeaders(200, data.length);
        try (OutputStream out = exchange.getResponseBody()) {
            out.write(data);
        }
    }
}
