package com.mentality.forgewebmap.commands;
final class RenderAreaBounds {
    static boolean isAllowed(int minX, int minZ, int maxX, int maxZ) {
        long width = (long) maxX - minX + 1L, height = (long) maxZ - minZ + 1L;
        return width > 0 && height > 0 && width <= 10_000L / height;
    }
    private RenderAreaBounds() {}
}
