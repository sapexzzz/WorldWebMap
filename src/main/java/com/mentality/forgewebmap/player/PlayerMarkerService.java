package com.mentality.forgewebmap.player;

import com.mojang.logging.LogUtils;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;

import java.util.*;

/**
 * Provides a list of connected players and their locations.
 * All data is read on the main server thread via getPlayers(), which is
 * called by the HTTP handler (thread-safe snapshot approach).
 */
public class PlayerMarkerService {

    private static final Logger LOGGER = LogUtils.getLogger();

    private volatile MinecraftServer server;
    private volatile List<PlayerInfo> snapshot = List.of();

    public void setServer(MinecraftServer server) {
        this.server = server;
    }
    public void setEnabled(boolean enabled) { if (!enabled) snapshot = List.of(); }

    /**
     * Returns a snapshot of all connected players.
     * This is called from the HTTP thread; accessing server player list
     * is generally safe for reads, but we copy to avoid CME.
     */
    public void refresh(boolean enabled) {
        if (!enabled) { snapshot = List.of(); return; }
        MinecraftServer srv = server;
        if (srv == null) { snapshot = List.of(); return; }

        List<PlayerInfo> result = new ArrayList<>();
        // getPlayerList() is thread-safe for iteration in 1.20.1 Forge
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
    public void publish(List<PlayerInfo> players) { snapshot = List.copyOf(players); }
    public List<PlayerInfo> getPlayers() { return snapshot; }

    // ── Inner record ──────────────────────────────────────────────────────────

    public record PlayerInfo(String name, String dimension,
                              double x, double y, double z, float yaw) {}
}
