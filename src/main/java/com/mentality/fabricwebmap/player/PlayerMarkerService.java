package com.mentality.fabricwebmap.player;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Provides a list of connected players and their locations.
 */
public class PlayerMarkerService {

    private static final Logger LOGGER = LoggerFactory.getLogger("fabricwebmap");

    private volatile MinecraftServer server;
    private volatile List<PlayerInfo> snapshot = List.of();

    public void setServer(MinecraftServer server) {
        this.server = server;
    }
    public void setEnabled(boolean enabled) { if (!enabled) snapshot = List.of(); }

    /** Call only from the server thread; HTTP handlers read the published immutable snapshot. */
    public void refresh(boolean enabled) {
        if (!enabled) { snapshot = List.of(); return; }
        MinecraftServer srv = server;
        if (srv == null) { snapshot = List.of(); return; }

        List<PlayerInfo> result = new ArrayList<>();
        for (ServerPlayer player : srv.getPlayerList().getPlayers()) {
            try {
                Vec3 pos = player.position();
                String dim = player.level().dimension().location().getPath();
                float yaw = player.getYRot();
                result.add(new PlayerInfo(player.getGameProfile().getName(), dim,
                        pos.x, pos.y, pos.z, yaw));
            } catch (Exception e) {
                LOGGER.warn("Failed to read position for player {}: {}", player.getName().getString(), e.getMessage());
            }
        }
        publish(result);
    }

    /** Production publication boundary between the server tick and HTTP threads. */
    public void publish(List<PlayerInfo> players) { snapshot = List.copyOf(players); }

    public List<PlayerInfo> getPlayers() { return snapshot; }

    public record PlayerInfo(String name, String dimension,
                              double x, double y, double z, float yaw) {}
}
