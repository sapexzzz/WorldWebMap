package com.mentality.fabricwebmap.render;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

/**
 * Immutable snapshot of chunk data needed to render one tile.
 * Safe to pass to worker threads — contains no live Minecraft objects.
 *
 * Layout of int[] arrays: index = px + pz * tileSize
 *   colors[]  - packed ARGB color per pixel
 *   heights[] - world surface Y per pixel (for height shading)
 */
public final class TileSnapshot {

    public final ResourceKey<Level> dimension;
    public final int tileX;
    public final int tileZ;
    public final int zoom;
    public final int tileSize;

    public final int[] colors;
    public final int[] heights;

    /**
     * True for each pixel whose chunk was loaded at snapshot time.
     * False = chunk was not in memory; pixel color is a placeholder.
     */
    public final boolean[] loadedMask;

    public TileSnapshot(ResourceKey<Level> dimension,
                        int tileX, int tileZ, int zoom, int tileSize,
                        int[] colors, int[] heights, boolean[] loadedMask) {
        this.dimension = dimension;
        this.tileX = tileX;
        this.tileZ = tileZ;
        this.zoom = zoom;
        this.tileSize = tileSize;
        this.colors = colors;
        this.heights = heights;
        this.loadedMask = loadedMask;
    }

    public int pixelIndex(int px, int pz) {
        return px + pz * tileSize;
    }
}
