package com.mentality.forgewebmap.web;

/** Keeps resolved surface selection and biome read coupled in the production API path. */
final class BiomeSurfaceLookup {
    @FunctionalInterface interface SurfaceY { int resolve(String dimension, int x, int z); }
    @FunctionalInterface interface BiomeAt { String read(String dimension, int x, int y, int z); }
    static String lookup(String dimension, int x, int z, SurfaceY surfaceY, BiomeAt biomeAt) {
        return biomeAt.read(dimension, x, surfaceY.resolve(dimension, x, z), z);
    }
    private BiomeSurfaceLookup() { }
}
