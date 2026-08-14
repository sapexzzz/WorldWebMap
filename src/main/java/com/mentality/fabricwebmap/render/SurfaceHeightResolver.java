package com.mentality.fabricwebmap.render;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;

/** Selects the visible map surface. Nether scans below the roof and accepts solid or liquid cavern surfaces. */
public final class SurfaceHeightResolver {
    public int resolve(ServerLevel level, int x, int z) {
        int surface = Math.max(level.getMinBuildHeight(), level.getHeight(Heightmap.Types.WORLD_SURFACE, x, z) - 1);
        return selectSurface(level.dimension().equals(Level.NETHER), level.getMinBuildHeight(), surface, level.getMaxBuildHeight() - 6,
                y -> level.getBlockState(new BlockPos(x, y, z)).is(Blocks.BEDROCK),
                y -> level.getBlockState(new BlockPos(x, y, z)).isAir());
    }
    /** Resolves from a chunk already known to be loaded; never performs a level lookup. */
    public int resolve(ServerLevel level, LevelChunk chunk, int x, int z) {
        int surface = Math.max(level.getMinBuildHeight(), chunk.getHeight(Heightmap.Types.WORLD_SURFACE, x & 15, z & 15) - 1);
        return selectSurface(level.dimension().equals(Level.NETHER), level.getMinBuildHeight(), surface, level.getMaxBuildHeight() - 6,
                y -> chunk.getBlockState(new BlockPos(x, y, z)).is(Blocks.BEDROCK),
                y -> chunk.getBlockState(new BlockPos(x, y, z)).isAir());
    }
    static int selectSurface(boolean nether, int minY, int surface, int netherTopY, java.util.function.IntPredicate bedrock, java.util.function.IntPredicate air) {
        return nether ? selectNetherSurface(minY, netherTopY, bedrock, air, surface) : surface;
    }
    static int selectNetherSurface(int minY, int topY, java.util.function.IntPredicate bedrock, java.util.function.IntPredicate air, int fallback) {
        for (int y = topY; y >= minY; y--) if (!bedrock.test(y) && !air.test(y)) return y;
        return fallback;
    }
}
