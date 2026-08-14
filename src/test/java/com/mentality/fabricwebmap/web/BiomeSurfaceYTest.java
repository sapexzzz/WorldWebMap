package com.mentality.fabricwebmap.web;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

/** Exercises the API's production lookup seam; server-side lookup resolves the surface before biome access. */
class BiomeSurfaceYTest {
    private int selectedY(String dimension, int resolvedY) {
        return Integer.parseInt(BiomeSurfaceLookup.lookup(dimension, 16, -16, (d, x, z) -> resolvedY,
                (d, x, y, z) -> Integer.toString(y)));
    }
    @Test void biomeLookupUsesResolvedSurfaceY() { assertEquals(91, selectedY("overworld", 91)); }
    @Test void overworldBiomeLookupUsesSurfaceHeight() { assertEquals(73, selectedY("overworld", 73)); }
    @Test void netherBiomeLookupUsesPlayableSurface() { assertEquals(32, selectedY("the_nether", 32)); }
    @Test void endBiomeLookupUsesSurfaceHeight() { assertEquals(61, selectedY("the_end", 61)); }
}
