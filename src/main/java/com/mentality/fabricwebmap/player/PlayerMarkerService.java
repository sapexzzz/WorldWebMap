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

    public void setServer(MinecraftServer server) {
        this.server = server;
    }

    public List<PlayerInfo> getPlayers() {
        MinecraftServer srv = server;
        if (srv == null) return Collections.emptyList();

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
        return result;
    }

    public record PlayerInfo(String name, String dimension,
                              double x, double y, double z, float yaw) {}
}
