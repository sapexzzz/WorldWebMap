package com.mentality.fabricwebmap.util;

import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.Level;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Optional;

/**
 * Converts between Minecraft dimension ResourceKeys and web-safe names.
 */
public final class DimensionUtil {

    private DimensionUtil() {}

    public static String toWebName(ResourceKey<Level> dimension) {
        return toWebName(dimension.location().toString());
    }
    static String toWebName(String id) {
        return switch (id) { case "minecraft:overworld" -> "overworld"; case "minecraft:the_nether" -> "the_nether"; case "minecraft:the_end" -> "the_end"; default -> encodeCustomId(id); };
    }

    public static ResourceKey<Level> fromWebName(String name) {
        return switch (name) {
            case "overworld"  -> Level.OVERWORLD;
            case "the_nether" -> Level.NETHER;
            case "the_end"    -> Level.END;
            default -> decodeCustomId(name).map(id -> ResourceKey.<Level>create(Registries.DIMENSION, new ResourceLocation(id))).orElseThrow(() -> new IllegalArgumentException("Unknown dimension: " + name));
        };
    }
    /** URL/path-safe, unambiguous representation of a non-vanilla resource location. */
    public static String encodeCustomId(String id) { return "custom-" + Base64.getUrlEncoder().withoutPadding().encodeToString(id.getBytes(StandardCharsets.UTF_8)); }
    public static Optional<String> decodeCustomId(String encoded) { try { if (!encoded.startsWith("custom-") || encoded.contains("/") || encoded.contains("\\") || encoded.contains("..")) return Optional.empty(); String id=new String(Base64.getUrlDecoder().decode(encoded.substring(7)),StandardCharsets.UTF_8); return id.matches("[a-z0-9_.-]+:[a-z0-9_./-]+") && !id.contains("..") ? Optional.of(id) : Optional.empty(); } catch (IllegalArgumentException e) { return Optional.empty(); } }
}
