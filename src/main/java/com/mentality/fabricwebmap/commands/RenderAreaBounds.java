package com.mentality.fabricwebmap.commands;

/** Overflow-safe rectangle policy for /webmap render-area. */
final class RenderAreaBounds {
    static final long MAX_TILES = 10_000L;

    static boolean isAllowed(int minX, int minZ, int maxX, int maxZ) {
        long width = (long) maxX - minX + 1L;
        long height = (long) maxZ - minZ + 1L;
        return width > 0 && height > 0 && width <= MAX_TILES / height;
    }

    private RenderAreaBounds() {}
}
