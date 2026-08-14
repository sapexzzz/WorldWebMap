package com.mentality.fabricwebmap.render;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

/** Bounded-state cursor for a potentially very large fullrender request. */
public final class FullRenderPlan {
    private final ResourceKey<Level> dimension;
    private final int centerX, centerZ, radius;
    private final long total;
    private int x, z;
    private long generated;
    private boolean cancelled;

    public FullRenderPlan(ResourceKey<Level> dimension, int centerX, int centerZ, int radius) {
        this.dimension = dimension; this.centerX = centerX; this.centerZ = centerZ; this.radius = radius;
        long width = 2L * radius + 1L;
        this.total = width * width;
        this.x = centerX - radius; this.z = centerZ - radius;
    }
    public RenderJob peek() {
        if (cancelled || generated >= total) return null;
        return new RenderJob(dimension, x, z, 0, RenderJob.Priority.LOW, false, false);
    }
    /** Advances only after the producer has successfully admitted {@link #peek()}. */
    public void markEnqueued() {
        if (cancelled || generated >= total) return;
        generated++;
        if (++z > centerZ + radius) { z = centerZ - radius; x++; }
    }
    public RenderJob next() { RenderJob job = peek(); if (job != null) markEnqueued(); return job; }
    public void cancel() { cancelled = true; }
    public boolean isCancelled() { return cancelled; }
    public boolean isComplete() { return generated >= total; }
    public long getTotal() { return total; }
    public long getGenerated() { return generated; }
    public long getRemaining() { return total - generated; }
    public int getCenterX() { return centerX; }
    public int getCenterZ() { return centerZ; }
    public int getRadius() { return radius; }
}
