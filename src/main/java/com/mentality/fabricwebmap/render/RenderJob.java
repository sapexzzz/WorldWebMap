package com.mentality.fabricwebmap.render;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import java.time.Instant;

/**
 * Describes one tile to be rendered.
 */
public final class RenderJob {

    public enum Priority { HIGH, NORMAL, LOW }

    public final ResourceKey<Level> dimension;
    public final int tileX;
    public final int tileZ;
    public final int zoom;
    public final Priority priority;
    public final Instant createdAt;

    /** If true, re-render even if the tile file already exists. */
    public final boolean force;

    /**
     * If true, unloaded chunks will be loaded from disk before sampling.
     * Use for command-triggered renders. Keep false for auto-renders.
     */
    public final boolean loadChunks;

    public RenderJob(ResourceKey<Level> dimension, int tileX, int tileZ, int zoom,
                     Priority priority, boolean force, boolean loadChunks) {
        this.dimension = dimension;
        this.tileX = tileX;
        this.tileZ = tileZ;
        this.zoom = zoom;
        this.priority = priority;
        this.force = force;
        this.loadChunks = loadChunks;
        this.createdAt = Instant.now();
    }

    public RenderJob(ResourceKey<Level> dimension, int tileX, int tileZ, int zoom,
                     Priority priority, boolean force) {
        this(dimension, tileX, tileZ, zoom, priority, force, false);
    }

    public RenderJob(ResourceKey<Level> dimension, int tileX, int tileZ) {
        this(dimension, tileX, tileZ, 0, Priority.NORMAL, false);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RenderJob other)) return false;
        return tileX == other.tileX && tileZ == other.tileZ && zoom == other.zoom
                && dimension.equals(other.dimension);
    }

    @Override
    public int hashCode() {
        int result = dimension.hashCode();
        result = 31 * result + tileX;
        result = 31 * result + tileZ;
        result = 31 * result + zoom;
        return result;
    }

    @Override
    public String toString() {
        return "RenderJob[" + tileX + "," + tileZ + " zoom=" + zoom + "]";
    }
}
