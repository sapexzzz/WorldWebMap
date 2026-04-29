package com.mentality.forgewebmap.commands;

import com.mentality.forgewebmap.ForgeWebMapMod;
import com.mentality.forgewebmap.config.WebMapConfig;
import com.mentality.forgewebmap.player.PlayerMarkerService;
import com.mentality.forgewebmap.render.RenderJob;
import com.mentality.forgewebmap.render.TileRenderManager;
import com.mentality.forgewebmap.web.WebServerService;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

/**
 * Registers all /webmap subcommands via Brigadier.
 * Requires operator level 2.
 */
public final class WebMapCommands {

    private WebMapCommands() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher,
                                WebMapConfig config,
                                TileRenderManager renderManager,
                                PlayerMarkerService playerMarkerService) {

        dispatcher.register(Commands.literal("webmap")
            .requires(src -> src.hasPermission(2))

            // /webmap status
            .then(Commands.literal("status")
                .executes(ctx -> executeStatus(ctx, config, renderManager)))

            // /webmap render <tileX> <tileZ>
            .then(Commands.literal("render")
                .then(Commands.argument("tileX", IntegerArgumentType.integer())
                .then(Commands.argument("tileZ", IntegerArgumentType.integer())
                    .executes(ctx -> executeRender(ctx, renderManager)))))

            // /webmap render-area <minX> <minZ> <maxX> <maxZ>
            .then(Commands.literal("render-area")
                .then(Commands.argument("minTileX", IntegerArgumentType.integer())
                .then(Commands.argument("minTileZ", IntegerArgumentType.integer())
                .then(Commands.argument("maxTileX", IntegerArgumentType.integer())
                .then(Commands.argument("maxTileZ", IntegerArgumentType.integer())
                    .executes(ctx -> executeRenderArea(ctx, renderManager)))))))

            // /webmap fullrender <radiusInTiles>
            .then(Commands.literal("fullrender")
                .then(Commands.argument("radius", IntegerArgumentType.integer(1, 1000))
                    .executes(ctx -> executeFullrender(ctx, renderManager))))

            // /webmap stoprender
            .then(Commands.literal("stoprender")
                .executes(ctx -> executeStoprender(ctx, renderManager)))

            // /webmap reload
            .then(Commands.literal("reload")
                .executes(ctx -> executeReload(ctx)))
        );
    }

    // ── /webmap status ─────────────────────────────────────────────────────────

    private static int executeStatus(CommandContext<CommandSourceStack> ctx,
                                     WebMapConfig config,
                                     TileRenderManager renderManager) {
        CommandSourceStack src = ctx.getSource();
        WebServerService ws = ForgeWebMapMod.getWebServer();

        boolean wsRunning = ws != null && ws.isRunning();
        String addr = ws != null ? ws.getAddress() : "N/A";

        send(src, "§6=== ForgeWebMap Status ===");
        send(src, "§7Mod enabled: §f" + config.isEnabled());
        send(src, "§7Web server: §f" + (wsRunning ? "§arunning" : "§cstopped") + " §7@ §f" + addr);
        send(src, "§7Queued jobs: §f" + renderManager.getQueueSize());
        send(src, "§7Active workers: §f" + renderManager.getActiveWorkers());
        send(src, "§7Rendered tiles: §f" + renderManager.getRenderedTiles());
        send(src, "§7Render threads: §f" + config.getRenderThreads());
        send(src, "§7Render state: §f" + renderManager.getState());

        return 1;
    }

    // ── /webmap render <tileX> <tileZ> ────────────────────────────────────────

    private static int executeRender(CommandContext<CommandSourceStack> ctx,
                                     TileRenderManager renderManager) {
        int tileX = IntegerArgumentType.getInteger(ctx, "tileX");
        int tileZ = IntegerArgumentType.getInteger(ctx, "tileZ");

        RenderJob job = new RenderJob(Level.OVERWORLD, tileX, tileZ, 0, RenderJob.Priority.HIGH, true, true);
        boolean queued = renderManager.enqueue(job, true);

        if (queued) {
            send(ctx.getSource(), "§aQueued render for tile (" + tileX + ", " + tileZ + ")");
        } else {
            send(ctx.getSource(), "§cFailed to queue tile (render manager stopped?)");
        }
        return 1;
    }

    // ── /webmap render-area <minX> <minZ> <maxX> <maxZ> ──────────────────────

    private static int executeRenderArea(CommandContext<CommandSourceStack> ctx,
                                         TileRenderManager renderManager) {
        int minX = IntegerArgumentType.getInteger(ctx, "minTileX");
        int minZ = IntegerArgumentType.getInteger(ctx, "minTileZ");
        int maxX = IntegerArgumentType.getInteger(ctx, "maxTileX");
        int maxZ = IntegerArgumentType.getInteger(ctx, "maxTileZ");

        // Normalise
        if (minX > maxX) { int t = minX; minX = maxX; maxX = t; }
        if (minZ > maxZ) { int t = minZ; minZ = maxZ; maxZ = t; }

        long total = (long)(maxX - minX + 1) * (maxZ - minZ + 1);
        if (total > 10_000) {
            send(ctx.getSource(), "§cArea too large (" + total + " tiles). Max 10000. Use /webmap fullrender for big areas.");
            return 0;
        }

        int queued = 0;
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                RenderJob job = new RenderJob(Level.OVERWORLD, x, z, 0, RenderJob.Priority.NORMAL, true, true);
                if (renderManager.enqueue(job)) queued++;
            }
        }

        send(ctx.getSource(), "§aQueued " + queued + " tiles for render-area.");
        return 1;
    }

    // ── /webmap fullrender <radius> ────────────────────────────────────────────

    private static int executeFullrender(CommandContext<CommandSourceStack> ctx,
                                          TileRenderManager renderManager) {
        int radius = IntegerArgumentType.getInteger(ctx, "radius");

        int queued = 0;
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                RenderJob job = new RenderJob(Level.OVERWORLD, x, z, 0, RenderJob.Priority.LOW, false, true);
                if (renderManager.enqueue(job)) queued++;
            }
        }

        send(ctx.getSource(), "§aFullrender queued " + queued + " tiles (radius=" + radius + ").");
        return 1;
    }

    // ── /webmap stoprender ────────────────────────────────────────────────────

    private static int executeStoprender(CommandContext<CommandSourceStack> ctx,
                                          TileRenderManager renderManager) {
        int oldSize = renderManager.getQueueSize();
        renderManager.clearQueue();
        send(ctx.getSource(), "§aRender queue cleared. Removed " + oldSize + " pending jobs.");
        return 1;
    }

    // ── /webmap reload ────────────────────────────────────────────────────────

    private static int executeReload(CommandContext<CommandSourceStack> ctx) {
        ForgeWebMapMod.reload();
        send(ctx.getSource(), "§aForgeWebMap config reloaded.");
        return 1;
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    private static void send(CommandSourceStack src, String message) {
        src.sendSuccess(() -> Component.literal(message), false);
    }
}
