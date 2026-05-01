package com.mentality.fabricwebmap.render;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.IdentityHashMap;
import java.util.Map;

/**
 * Maps BlockState -> RGB color for top-down map rendering.
 * Returns 0x555555 for unknown blocks.
 */
public final class BlockColorResolver {

    private static final int FALLBACK = 0x555555;

    private static final Map<Block, Integer> COLOR_MAP = new IdentityHashMap<>();

    static {
        // ── Terrain ──────────────────────────────────────────────────────────
        put(Blocks.GRASS_BLOCK,      0x5FA845);
        put(Blocks.DIRT,             0x8B6040);
        put(Blocks.COARSE_DIRT,      0x7A5535);
        put(Blocks.PODZOL,           0x6B4423);
        put(Blocks.MYCELIUM,         0x7B6B7B);
        put(Blocks.ROOTED_DIRT,      0x8B6040);

        put(Blocks.STONE,            0x808080);
        put(Blocks.COBBLESTONE,      0x737373);
        put(Blocks.MOSSY_COBBLESTONE,0x607060);
        put(Blocks.DEEPSLATE,        0x505060);
        put(Blocks.COBBLED_DEEPSLATE,0x484858);

        put(Blocks.GRAVEL,           0x908070);
        put(Blocks.SAND,             0xDDC87A);
        put(Blocks.RED_SAND,         0xBE6220);
        put(Blocks.SANDSTONE,        0xD4C073);
        put(Blocks.RED_SANDSTONE,    0xBD6020);

        put(Blocks.BEDROCK,          0x333333);

        // ── Water / Lava / Ice ───────────────────────────────────────────────
        put(Blocks.WATER,            0x2255AA);
        put(Blocks.LAVA,             0xCC4400);
        put(Blocks.ICE,              0x99C4E8);
        put(Blocks.PACKED_ICE,       0x7DB2D8);
        put(Blocks.BLUE_ICE,         0x74ADDE);
        put(Blocks.SNOW,             0xEEEEFF);
        put(Blocks.SNOW_BLOCK,       0xEEEEFF);
        put(Blocks.POWDER_SNOW,      0xDDDDEE);

        // ── Ores ─────────────────────────────────────────────────────────────
        put(Blocks.COAL_ORE,         0x606060);
        put(Blocks.IRON_ORE,         0xA08060);
        put(Blocks.COPPER_ORE,       0xC07040);
        put(Blocks.GOLD_ORE,         0xD4A017);
        put(Blocks.DIAMOND_ORE,      0x50D0D0);
        put(Blocks.EMERALD_ORE,      0x44CC77);
        put(Blocks.LAPIS_ORE,        0x3344AA);
        put(Blocks.REDSTONE_ORE,     0xAA3333);

        // ── Logs / Wood ───────────────────────────────────────────────────────
        put(Blocks.OAK_LOG,          0x6B4E2A);
        put(Blocks.BIRCH_LOG,        0xC8C87A);
        put(Blocks.SPRUCE_LOG,       0x503020);
        put(Blocks.JUNGLE_LOG,       0x7A6030);
        put(Blocks.ACACIA_LOG,       0x7A5030);
        put(Blocks.DARK_OAK_LOG,     0x3A2010);
        put(Blocks.MANGROVE_LOG,     0x5A2020);
        put(Blocks.CHERRY_LOG,       0x8B4E4E);

        // ── Leaves ────────────────────────────────────────────────────────────
        put(Blocks.OAK_LEAVES,       0x2D6E1A);
        put(Blocks.BIRCH_LEAVES,     0x5E8832);
        put(Blocks.SPRUCE_LEAVES,    0x1E4E1E);
        put(Blocks.JUNGLE_LEAVES,    0x1A6E1A);
        put(Blocks.ACACIA_LEAVES,    0x3E7E20);
        put(Blocks.DARK_OAK_LEAVES,  0x1A4E10);
        put(Blocks.MANGROVE_LEAVES,  0x2A6E20);
        put(Blocks.CHERRY_LEAVES,    0xE884B0);
        put(Blocks.AZALEA_LEAVES,    0x4A7A30);
        put(Blocks.FLOWERING_AZALEA_LEAVES, 0xD06090);

        // ── Planks ───────────────────────────────────────────────────────────
        put(Blocks.OAK_PLANKS,       0xC09050);
        put(Blocks.BIRCH_PLANKS,     0xD8C88A);
        put(Blocks.SPRUCE_PLANKS,    0x7A5030);
        put(Blocks.JUNGLE_PLANKS,    0xA07040);
        put(Blocks.ACACIA_PLANKS,    0xBB6030);
        put(Blocks.DARK_OAK_PLANKS,  0x4A2A10);
        put(Blocks.MANGROVE_PLANKS,  0x7A3030);
        put(Blocks.CHERRY_PLANKS,    0xE0B0A0);
        put(Blocks.BAMBOO_PLANKS,    0xB8A050);
        put(Blocks.CRIMSON_PLANKS,   0x8B2252);
        put(Blocks.WARPED_PLANKS,    0x2B8B7A);

        // ── Building blocks ───────────────────────────────────────────────────
        put(Blocks.BRICKS,           0xAA5533);
        put(Blocks.STONE_BRICKS,     0x787878);
        put(Blocks.MOSSY_STONE_BRICKS, 0x6A7860);
        put(Blocks.CRACKED_STONE_BRICKS, 0x707070);
        put(Blocks.GLASS,            0xBBDDEE);
        put(Blocks.GLASS_PANE,       0xBBDDEE);
        put(Blocks.WHITE_CONCRETE,   0xE0E0E0);
        put(Blocks.GRAY_CONCRETE,    0x606060);
        put(Blocks.BLACK_CONCRETE,   0x252525);
        put(Blocks.RED_CONCRETE,     0xAA2222);
        put(Blocks.GREEN_CONCRETE,   0x336622);
        put(Blocks.BLUE_CONCRETE,    0x224488);
        put(Blocks.YELLOW_CONCRETE,  0xDDCC22);
        put(Blocks.ORANGE_CONCRETE,  0xCC7722);
        put(Blocks.LIME_CONCRETE,    0x44CC33);
        put(Blocks.CYAN_CONCRETE,    0x226688);
        put(Blocks.PURPLE_CONCRETE,  0x663388);
        put(Blocks.MAGENTA_CONCRETE, 0xCC44AA);
        put(Blocks.PINK_CONCRETE,    0xEE88AA);
        put(Blocks.LIGHT_BLUE_CONCRETE, 0x5599DD);
        put(Blocks.LIGHT_GRAY_CONCRETE, 0xAAAAAA);
        put(Blocks.BROWN_CONCRETE,   0x7A4A22);

        put(Blocks.OBSIDIAN,         0x1A1020);
        put(Blocks.CRYING_OBSIDIAN,  0x2A0A40);
        put(Blocks.NETHERRACK,       0x882222);
        put(Blocks.NETHER_BRICKS,    0x441111);
        put(Blocks.BASALT,           0x555565);
        put(Blocks.BLACKSTONE,       0x282830);
        put(Blocks.SOUL_SAND,        0x604840);
        put(Blocks.SOUL_SOIL,        0x504030);
        put(Blocks.GLOWSTONE,        0xDDAA44);
        put(Blocks.NETHER_WART_BLOCK,0xAA1111);
        put(Blocks.SHROOMLIGHT,      0xDDA044);

        put(Blocks.END_STONE,        0xDDDD99);
        put(Blocks.END_STONE_BRICKS, 0xCCCC88);
        put(Blocks.PURPUR_BLOCK,     0xAA77AA);

        // ── Misc ─────────────────────────────────────────────────────────────
        put(Blocks.AIR,              0x000000);
        put(Blocks.CAVE_AIR,         0x000000);
        put(Blocks.VOID_AIR,         0x000000);
        put(Blocks.FARMLAND,         0x7A5030);
        put(Blocks.GRASS,            0x7AC850);
        put(Blocks.TALL_GRASS,       0x7AC850);
        put(Blocks.FERN,             0x6AAA40);
        put(Blocks.LARGE_FERN,       0x6AAA40);
        put(Blocks.CLAY,             0xA0A8B8);
        put(Blocks.MUD,              0x4A3828);
        put(Blocks.TERRACOTTA,       0xA05030);
        put(Blocks.WHITE_TERRACOTTA, 0xD0B8A8);
        put(Blocks.DIRT_PATH,        0xB09060);
    }

    private static void put(Block block, int rgb) {
        COLOR_MAP.put(block, rgb);
    }

    public static int resolve(BlockState state) {
        if (state == null) return FALLBACK;
        Block block = state.getBlock();
        return COLOR_MAP.getOrDefault(block, FALLBACK);
    }
}
