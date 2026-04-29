package com.mentality.forgewebmap.render;

import com.mentality.forgewebmap.storage.TileStorage;
import com.mentality.forgewebmap.util.DimensionUtil;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * Renders a TileSnapshot to a PNG file.
 * Safe to run in a worker thread — only receives immutable TileSnapshot.
 */
public class TileRenderer {

    private static final Logger LOGGER = LogUtils.getLogger();

    private final TileStorage tileStorage;

    public TileRenderer(TileStorage tileStorage) {
        this.tileStorage = tileStorage;
    }

    /**
     * Render the snapshot and write the PNG to disk.
     *
     * @param snapshot immutable tile data
     */
    public void render(TileSnapshot snapshot) {
        int size = snapshot.tileSize;
        String dimension = DimensionUtil.toWebName(snapshot.dimension);

        // Check if any pixel belongs to an unloaded chunk.
        // If yes, load the existing tile from disk so we can composite over it,
        // preserving previously rendered areas instead of overwriting them with gray.
        boolean hasUnloaded = false;
        for (boolean loaded : snapshot.loadedMask) {
            if (!loaded) { hasUnloaded = true; break; }
        }

        BufferedImage existing = null;
        if (hasUnloaded && tileStorage.exists(dimension, snapshot.zoom, snapshot.tileX, snapshot.tileZ)) {
            try {
                existing = tileStorage.read(dimension, snapshot.zoom, snapshot.tileX, snapshot.tileZ);
            } catch (IOException e) {
                // Could not load existing tile — proceed without compositing
                existing = null;
            }
        }

        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);

        for (int pz = 0; pz < size; pz++) {
            for (int px = 0; px < size; px++) {
                int idx = snapshot.pixelIndex(px, pz);

                if (!snapshot.loadedMask[idx]) {
                    // Chunk was not in memory at snapshot time.
                    // Use the existing tile's pixel to preserve previously rendered data.
                    if (existing != null) {
                        image.setRGB(px, pz, existing.getRGB(px, pz));
                    } else {
                        // No previous render exists yet — use transparent so the browser
                        // shows nothing rather than an ugly gray block.
                        image.setRGB(px, pz, 0x00000000);
                    }
                    continue;
                }

                int baseColor = snapshot.colors[idx];
                int height = snapshot.heights[idx];

                int neighborHeight = (pz > 0) ? snapshot.heights[snapshot.pixelIndex(px, pz - 1)] : height;
                int shadedColor = applyHeightShading(baseColor, height, neighborHeight);

                image.setRGB(px, pz, shadedColor);
            }
        }

        try {
            tileStorage.write(dimension, snapshot.zoom, snapshot.tileX, snapshot.tileZ, image);
        } catch (IOException e) {
            LOGGER.error("Failed to write tile {}/{}/{}/{}: {}",
                    dimension, snapshot.zoom, snapshot.tileX, snapshot.tileZ, e.getMessage());
        }
    }

    /**
     * Apply simple height-based shading.
     * Lighter when current pixel is higher than its northern neighbor, darker when lower.
     *
     * @param baseColor     packed ARGB color
     * @param currentHeight Y of this pixel
     * @param neighborHeight Y of the pixel to the north
     * @return shaded ARGB color
     */
    static int applyHeightShading(int baseColor, int currentHeight, int neighborHeight) {
        int diff = currentHeight - neighborHeight;
        if (diff == 0) return baseColor;

        float factor;
        if (diff > 0) {
            // Higher → lighten, max +30%
            factor = Math.min(diff * 0.05f, 0.30f);
        } else {
            // Lower → darken, max -40%
            factor = Math.max(diff * 0.07f, -0.40f);
        }

        int a = (baseColor >> 24) & 0xFF;
        int r = (baseColor >> 16) & 0xFF;
        int g = (baseColor >> 8)  & 0xFF;
        int b =  baseColor        & 0xFF;

        r = clamp((int)(r + r * factor));
        g = clamp((int)(g + g * factor));
        b = clamp((int)(b + b * factor));

        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    private static int clamp(int v) {
        return Math.max(0, Math.min(255, v));
    }
}
