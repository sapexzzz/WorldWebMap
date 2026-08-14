package com.mentality.fabricwebmap.render;

import com.mentality.fabricwebmap.storage.TileStorage;
import com.mentality.fabricwebmap.util.DimensionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * Renders a TileSnapshot to a PNG file.
 * Safe to run in a worker thread — only receives immutable TileSnapshot.
 */
public class TileRenderer {
    public enum Result { WRITTEN, EMPTY, SKIPPED_CORRUPT_PARTIAL, FAILED }

    private static final Logger LOGGER = LoggerFactory.getLogger("fabricwebmap");

    private final TileStorage tileStorage;

    public TileRenderer(TileStorage tileStorage) {
        this.tileStorage = tileStorage;
    }

    public Result render(TileSnapshot snapshot) {
        int size = snapshot.tileSize;
        String dimension = snapshot.webDimension != null ? snapshot.webDimension : DimensionUtil.toWebName(snapshot.dimension);

        // Read the existing tile so we can composite unloaded-chunk pixels from it.
        // We attempt this even when all chunks appear loaded — if the read succeeds it
        // costs little, and it guards against edge-cases where loadedMask is wrong.
        BufferedImage existing = null; boolean corrupt = false;
        if (tileStorage.exists(dimension, snapshot.zoom, snapshot.tileX, snapshot.tileZ)) {
            try {
                BufferedImage read = tileStorage.read(dimension, snapshot.zoom, snapshot.tileX, snapshot.tileZ);
                // ImageIO.read() can silently return null for a valid-but-re-written file;
                // treat that as "no existing tile" rather than crash.
                if (read != null) existing = read; else corrupt = true;
            } catch (IOException e) {
                corrupt = true;
            }
        }
        boolean complete=true; for(boolean loaded:snapshot.loadedMask) if(!loaded){complete=false;break;}
        if(corrupt && !complete){ try { tileStorage.quarantine(dimension,snapshot.zoom,snapshot.tileX,snapshot.tileZ); } catch(IOException e){ LOGGER.warn("Could not quarantine corrupt tile",e); } LOGGER.warn("Skipping partial replacement of corrupt tile {}/{}/{}",dimension,snapshot.tileX,snapshot.tileZ); return Result.SKIPPED_CORRUPT_PARTIAL; }
        if(corrupt) try { tileStorage.quarantine(dimension,snapshot.zoom,snapshot.tileX,snapshot.tileZ); } catch(IOException e){ LOGGER.warn("Could not quarantine corrupt tile",e); return Result.FAILED; }

        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);

        for (int pz = 0; pz < size; pz++) {
            for (int px = 0; px < size; px++) {
                int idx = snapshot.pixelIndex(px, pz);

                if (!snapshot.loadedMask[idx]) {
                    if (existing != null) {
                        image.setRGB(px, pz, existing.getRGB(px, pz));
                    } else {
                        image.setRGB(px, pz, 0x00000000);
                    }
                    continue;
                }

                int baseColor = snapshot.colors[idx];
                int height = snapshot.heights[idx];

                // Use the north neighbor's height for shading only if that pixel
                // was actually loaded. If it was unloaded (placeholder height=64),
                // the shading would be wrong and create visible seam artifacts.
                int neighborHeight = height;
                if (pz > 0) {
                    int nIdx = snapshot.pixelIndex(px, pz - 1);
                    if (snapshot.loadedMask[nIdx]) {
                        neighborHeight = snapshot.heights[nIdx];
                    }
                }
                int shadedColor = applyHeightShading(baseColor, height, neighborHeight);

                image.setRGB(px, pz, shadedColor);
            }
        }

        // Guard: if every pixel in the new image is transparent (snapshot had no loaded
        // chunks AND there was no existing tile to composite from), discard the result.
        // Writing an all-transparent tile would permanently erase the previous render.
        boolean allTransparent = true;
        outer:
        for (int pz = 0; pz < size; pz++) {
            for (int px = 0; px < size; px++) {
                if ((image.getRGB(px, pz) >>> 24) != 0) { allTransparent = false; break outer; }
            }
        }
        if (allTransparent) {
            LOGGER.debug("Skipping write for tile {}/{}/{}/{}: snapshot was entirely empty.",
                    dimension, snapshot.zoom, snapshot.tileX, snapshot.tileZ);
            return Result.EMPTY;
        }

        try {
            tileStorage.write(dimension, snapshot.zoom, snapshot.tileX, snapshot.tileZ, image); return Result.WRITTEN;
        } catch (IOException e) {
            LOGGER.error("Failed to write tile {}/{}/{}/{}: {}",
                    dimension, snapshot.zoom, snapshot.tileX, snapshot.tileZ, e.getMessage());
        } return Result.FAILED;
    }

    static int applyHeightShading(int baseColor, int currentHeight, int neighborHeight) {
        int diff = currentHeight - neighborHeight;
        if (diff == 0) return baseColor;

        float factor;
        if (diff > 0) {
            factor = Math.min(diff * 0.05f, 0.30f);
        } else {
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
