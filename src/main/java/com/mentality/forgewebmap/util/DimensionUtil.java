package com.mentality.forgewebmap.util;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

/**
 * Converts between Minecraft dimension ResourceKeys and web-safe names.
 */
public final class DimensionUtil {

    private DimensionUtil() {}

    public static String toWebName(ResourceKey<Level> dimension) {
        // Use the path of the resource location (e.g. "overworld", "the_nether", "the_end")
        return dimension.location().getPath();
    }

    public static ResourceKey<Level> fromWebName(String name) {
        return switch (name) {
            case "overworld"  -> Level.OVERWORLD;
            case "the_nether" -> Level.NETHER;
            case "the_end"    -> Level.END;
            default           -> Level.OVERWORLD; // fallback
        };
    }
}
