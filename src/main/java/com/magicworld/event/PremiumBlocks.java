package com.magicworld.event;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.BlockTags;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class PremiumBlocks {

    public static boolean transform(
            Level level,
            BlockPos pos,
            BlockState state
    ) {

        // PLANKS
        if (state.is(Blocks.OAK_PLANKS)
                || state.is(Blocks.SPRUCE_PLANKS)
                || state.is(Blocks.BIRCH_PLANKS)
                || state.is(Blocks.JUNGLE_PLANKS)
                || state.is(Blocks.ACACIA_PLANKS)
                || state.is(Blocks.DARK_OAK_PLANKS)
                || state.is(Blocks.MANGROVE_PLANKS)
                || state.is(Blocks.CHERRY_PLANKS)
                || state.is(Blocks.BAMBOO_PLANKS)
                || state.is(Blocks.BAMBOO_MOSAIC)
                || state.is(Blocks.CRIMSON_PLANKS)
                || state.is(Blocks.WARPED_PLANKS)) {

            change(level, pos, state, Blocks.HONEYCOMB_BLOCK);
            return true;
        }

        else if (state.is(Blocks.HONEYCOMB_BLOCK)) {

            change(level, pos, state, Blocks.OAK_PLANKS);
            return true;
        }

        // FENCES
        else if (state.is(Blocks.OAK_FENCE)
                || state.is(Blocks.SPRUCE_FENCE)
                || state.is(Blocks.BIRCH_FENCE)
                || state.is(Blocks.JUNGLE_FENCE)
                || state.is(Blocks.ACACIA_FENCE)
                || state.is(Blocks.DARK_OAK_FENCE)
                || state.is(Blocks.MANGROVE_FENCE)
                || state.is(Blocks.CHERRY_FENCE)
                || state.is(Blocks.BAMBOO_FENCE)
                || state.is(Blocks.CRIMSON_FENCE)
                || state.is(Blocks.WARPED_FENCE)) {

            change(level, pos, state, Blocks.IRON_BARS);
            return true;
        }

        else if (state.is(Blocks.IRON_BARS)) {

            change(level, pos, state, Blocks.OAK_FENCE);
            return true;
        }

        // FENCE GATES
        else if (state.is(Blocks.OAK_FENCE_GATE)
                || state.is(Blocks.SPRUCE_FENCE_GATE)
                || state.is(Blocks.BIRCH_FENCE_GATE)
                || state.is(Blocks.JUNGLE_FENCE_GATE)
                || state.is(Blocks.ACACIA_FENCE_GATE)
                || state.is(Blocks.DARK_OAK_FENCE_GATE)
                || state.is(Blocks.MANGROVE_FENCE_GATE)
                || state.is(Blocks.CHERRY_FENCE_GATE)
                || state.is(Blocks.BAMBOO_FENCE_GATE)
                || state.is(Blocks.CRIMSON_FENCE_GATE)
                || state.is(Blocks.WARPED_FENCE_GATE)) {

            change(level, pos, state, Blocks.IRON_CHAIN);
            return true;
        }

        else if (state.is(Blocks.IRON_CHAIN)) {

            change(level, pos, state, Blocks.OAK_FENCE_GATE);
            return true;
        }

        // DOORS
        else if (state.is(Blocks.OAK_DOOR)
                || state.is(Blocks.SPRUCE_DOOR)
                || state.is(Blocks.BIRCH_DOOR)
                || state.is(Blocks.JUNGLE_DOOR)
                || state.is(Blocks.ACACIA_DOOR)
                || state.is(Blocks.DARK_OAK_DOOR)
                || state.is(Blocks.MANGROVE_DOOR)
                || state.is(Blocks.CHERRY_DOOR)
                || state.is(Blocks.BAMBOO_DOOR)
                || state.is(Blocks.CRIMSON_DOOR)
                || state.is(Blocks.WARPED_DOOR)) {

            change(level, pos, state, Blocks.IRON_DOOR);
            return true;
        }

        else if (state.is(Blocks.IRON_DOOR)) {

            change(level, pos, state, Blocks.OAK_DOOR);
            return true;
        }

        // TRAPDOORS
        else if (state.is(Blocks.OAK_TRAPDOOR)
                || state.is(Blocks.SPRUCE_TRAPDOOR)
                || state.is(Blocks.BIRCH_TRAPDOOR)
                || state.is(Blocks.JUNGLE_TRAPDOOR)
                || state.is(Blocks.ACACIA_TRAPDOOR)
                || state.is(Blocks.DARK_OAK_TRAPDOOR)
                || state.is(Blocks.MANGROVE_TRAPDOOR)
                || state.is(Blocks.CHERRY_TRAPDOOR)
                || state.is(Blocks.BAMBOO_TRAPDOOR)
                || state.is(Blocks.CRIMSON_TRAPDOOR)
                || state.is(Blocks.WARPED_TRAPDOOR)) {

            change(level, pos, state, Blocks.IRON_TRAPDOOR);
            return true;
        }

        else if (state.is(Blocks.IRON_TRAPDOOR)) {

            change(level, pos, state, Blocks.OAK_TRAPDOOR);
            return true;
        }

        // STAIRS
        else if (state.is(Blocks.OAK_STAIRS)
                || state.is(Blocks.SPRUCE_STAIRS)
                || state.is(Blocks.BIRCH_STAIRS)
                || state.is(Blocks.JUNGLE_STAIRS)
                || state.is(Blocks.ACACIA_STAIRS)
                || state.is(Blocks.DARK_OAK_STAIRS)
                || state.is(Blocks.MANGROVE_STAIRS)
                || state.is(Blocks.CHERRY_STAIRS)
                || state.is(Blocks.BAMBOO_STAIRS)
                || state.is(Blocks.BAMBOO_MOSAIC_STAIRS)
                || state.is(Blocks.CRIMSON_STAIRS)
                || state.is(Blocks.WARPED_STAIRS)) {

            change(level, pos, state, Blocks.STONE_BRICK_STAIRS);
            return true;
        }

        else if (state.is(Blocks.STONE_BRICK_STAIRS)) {

            change(level, pos, state, Blocks.OAK_STAIRS);
            return true;
        }

        // SLABS
        else if (state.is(Blocks.OAK_SLAB)
                || state.is(Blocks.SPRUCE_SLAB)
                || state.is(Blocks.BIRCH_SLAB)
                || state.is(Blocks.JUNGLE_SLAB)
                || state.is(Blocks.ACACIA_SLAB)
                || state.is(Blocks.DARK_OAK_SLAB)
                || state.is(Blocks.MANGROVE_SLAB)
                || state.is(Blocks.CHERRY_SLAB)
                || state.is(Blocks.BAMBOO_SLAB)
                || state.is(Blocks.BAMBOO_MOSAIC_SLAB)
                || state.is(Blocks.CRIMSON_SLAB)
                || state.is(Blocks.WARPED_SLAB)) {

            change(level, pos, state, Blocks.STONE_BRICK_SLAB);
            return true;
        }

        else if (state.is(Blocks.STONE_BRICK_SLAB)) {

            change(level, pos, state, Blocks.OAK_SLAB);
            return true;
        }

        // BUTTONS
        else if (state.is(Blocks.OAK_BUTTON)
                || state.is(Blocks.SPRUCE_BUTTON)
                || state.is(Blocks.BIRCH_BUTTON)
                || state.is(Blocks.JUNGLE_BUTTON)
                || state.is(Blocks.ACACIA_BUTTON)
                || state.is(Blocks.DARK_OAK_BUTTON)
                || state.is(Blocks.MANGROVE_BUTTON)
                || state.is(Blocks.CHERRY_BUTTON)
                || state.is(Blocks.BAMBOO_BUTTON)
                || state.is(Blocks.CRIMSON_BUTTON)
                || state.is(Blocks.WARPED_BUTTON)) {

            change(level, pos, state, Blocks.STONE_BUTTON);
            return true;
        }

        else if (state.is(Blocks.STONE_BUTTON)) {

            change(level, pos, state, Blocks.OAK_BUTTON);
            return true;
        }

        // PRESSURE PLATES
        else if (state.is(Blocks.OAK_PRESSURE_PLATE)
                || state.is(Blocks.SPRUCE_PRESSURE_PLATE)
                || state.is(Blocks.BIRCH_PRESSURE_PLATE)
                || state.is(Blocks.JUNGLE_PRESSURE_PLATE)
                || state.is(Blocks.ACACIA_PRESSURE_PLATE)
                || state.is(Blocks.DARK_OAK_PRESSURE_PLATE)
                || state.is(Blocks.MANGROVE_PRESSURE_PLATE)
                || state.is(Blocks.CHERRY_PRESSURE_PLATE)
                || state.is(Blocks.BAMBOO_PRESSURE_PLATE)
                || state.is(Blocks.CRIMSON_PRESSURE_PLATE)
                || state.is(Blocks.WARPED_PRESSURE_PLATE)) {

            change(level, pos, state, Blocks.LIGHT_WEIGHTED_PRESSURE_PLATE);
            return true;
        }

        else if (state.is(Blocks.LIGHT_WEIGHTED_PRESSURE_PLATE)) {

            change(level, pos, state, Blocks.OAK_PRESSURE_PLATE);
            return true;
        }

        // WALLS
        else if (state.is(Blocks.COBBLESTONE_WALL)
                || state.is(Blocks.MOSSY_COBBLESTONE_WALL)
                || state.is(Blocks.BRICK_WALL)
                || state.is(Blocks.PRISMARINE_WALL)
                || state.is(Blocks.RED_SANDSTONE_WALL)
                || state.is(Blocks.MOSSY_STONE_BRICK_WALL)
                || state.is(Blocks.GRANITE_WALL)
                || state.is(Blocks.STONE_BRICK_WALL)
                || state.is(Blocks.NETHER_BRICK_WALL)
                || state.is(Blocks.ANDESITE_WALL)
                || state.is(Blocks.RED_NETHER_BRICK_WALL)
                || state.is(Blocks.SANDSTONE_WALL)
                || state.is(Blocks.END_STONE_BRICK_WALL)
                || state.is(Blocks.DIORITE_WALL)
                || state.is(Blocks.BLACKSTONE_WALL)
                || state.is(Blocks.POLISHED_BLACKSTONE_WALL)
                || state.is(Blocks.POLISHED_BLACKSTONE_BRICK_WALL)
                || state.is(Blocks.COBBLED_DEEPSLATE_WALL)
                || state.is(Blocks.POLISHED_DEEPSLATE_WALL)
                || state.is(Blocks.DEEPSLATE_BRICK_WALL)
                || state.is(Blocks.DEEPSLATE_TILE_WALL)
                || state.is(Blocks.MUD_BRICK_WALL)) {

            change(level, pos, state, Blocks.OBSIDIAN);
            return true;
        }

        else if (state.is(Blocks.OBSIDIAN)) {

            change(level, pos, state, Blocks.COBBLESTONE_WALL);
            return true;
        }

        // GLASS PANES
        else if (state.is(Blocks.GLASS_PANE)
                || state.is(Blocks.WHITE_STAINED_GLASS_PANE)
                || state.is(Blocks.ORANGE_STAINED_GLASS_PANE)
                || state.is(Blocks.MAGENTA_STAINED_GLASS_PANE)
                || state.is(Blocks.LIGHT_BLUE_STAINED_GLASS_PANE)
                || state.is(Blocks.YELLOW_STAINED_GLASS_PANE)
                || state.is(Blocks.LIME_STAINED_GLASS_PANE)
                || state.is(Blocks.PINK_STAINED_GLASS_PANE)
                || state.is(Blocks.GRAY_STAINED_GLASS_PANE)
                || state.is(Blocks.LIGHT_GRAY_STAINED_GLASS_PANE)
                || state.is(Blocks.CYAN_STAINED_GLASS_PANE)
                || state.is(Blocks.PURPLE_STAINED_GLASS_PANE)
                || state.is(Blocks.BLUE_STAINED_GLASS_PANE)
                || state.is(Blocks.BROWN_STAINED_GLASS_PANE)
                || state.is(Blocks.GREEN_STAINED_GLASS_PANE)
                || state.is(Blocks.RED_STAINED_GLASS_PANE)
                || state.is(Blocks.BLACK_STAINED_GLASS_PANE)) {

            change(level, pos, state, Blocks.TINTED_GLASS);
            return true;
        }

        else if (state.is(Blocks.TINTED_GLASS)) {

            change(level, pos, state, Blocks.GLASS_PANE);
            return true;
        }

        // SIGNS
        else if (state.is(Blocks.OAK_SIGN)
                || state.is(Blocks.SPRUCE_SIGN)
                || state.is(Blocks.BIRCH_SIGN)
                || state.is(Blocks.JUNGLE_SIGN)
                || state.is(Blocks.ACACIA_SIGN)
                || state.is(Blocks.DARK_OAK_SIGN)
                || state.is(Blocks.MANGROVE_SIGN)
                || state.is(Blocks.CHERRY_SIGN)
                || state.is(Blocks.BAMBOO_SIGN)
                || state.is(Blocks.CRIMSON_SIGN)
                || state.is(Blocks.WARPED_SIGN)
                || state.is(Blocks.OAK_WALL_SIGN)
                || state.is(Blocks.SPRUCE_WALL_SIGN)
                || state.is(Blocks.BIRCH_WALL_SIGN)
                || state.is(Blocks.JUNGLE_WALL_SIGN)
                || state.is(Blocks.ACACIA_WALL_SIGN)
                || state.is(Blocks.DARK_OAK_WALL_SIGN)
                || state.is(Blocks.MANGROVE_WALL_SIGN)
                || state.is(Blocks.CHERRY_WALL_SIGN)
                || state.is(Blocks.BAMBOO_WALL_SIGN)
                || state.is(Blocks.CRIMSON_WALL_SIGN)
                || state.is(Blocks.WARPED_WALL_SIGN)) {

            change(level, pos, state, Blocks.BOOKSHELF);
            return true;
        }

        else if (state.is(Blocks.BOOKSHELF)) {

            change(level, pos, state, Blocks.OAK_SIGN);
            return true;
        }

        // HANGING SIGNS
        else if (state.is(Blocks.OAK_HANGING_SIGN)
                || state.is(Blocks.SPRUCE_HANGING_SIGN)
                || state.is(Blocks.BIRCH_HANGING_SIGN)
                || state.is(Blocks.JUNGLE_HANGING_SIGN)
                || state.is(Blocks.ACACIA_HANGING_SIGN)
                || state.is(Blocks.DARK_OAK_HANGING_SIGN)
                || state.is(Blocks.MANGROVE_HANGING_SIGN)
                || state.is(Blocks.CHERRY_HANGING_SIGN)
                || state.is(Blocks.BAMBOO_HANGING_SIGN)
                || state.is(Blocks.CRIMSON_HANGING_SIGN)
                || state.is(Blocks.WARPED_HANGING_SIGN)
                || state.is(Blocks.OAK_WALL_HANGING_SIGN)
                || state.is(Blocks.SPRUCE_WALL_HANGING_SIGN)
                || state.is(Blocks.BIRCH_WALL_HANGING_SIGN)
                || state.is(Blocks.JUNGLE_WALL_HANGING_SIGN)
                || state.is(Blocks.ACACIA_WALL_HANGING_SIGN)
                || state.is(Blocks.DARK_OAK_WALL_HANGING_SIGN)
                || state.is(Blocks.MANGROVE_WALL_HANGING_SIGN)
                || state.is(Blocks.CHERRY_WALL_HANGING_SIGN)
                || state.is(Blocks.BAMBOO_WALL_HANGING_SIGN)
                || state.is(Blocks.CRIMSON_WALL_HANGING_SIGN)
                || state.is(Blocks.WARPED_WALL_HANGING_SIGN)) {

            change(level, pos, state, Blocks.CHISELED_BOOKSHELF);
            return true;
        }

        else if (state.is(Blocks.CHISELED_BOOKSHELF)) {

            change(level, pos, state, Blocks.OAK_HANGING_SIGN);
            return true;
        }

        // WOOL
        else if (state.is(Blocks.WHITE_WOOL)
                || state.is(Blocks.ORANGE_WOOL)
                || state.is(Blocks.MAGENTA_WOOL)
                || state.is(Blocks.LIGHT_BLUE_WOOL)
                || state.is(Blocks.YELLOW_WOOL)
                || state.is(Blocks.LIME_WOOL)
                || state.is(Blocks.PINK_WOOL)
                || state.is(Blocks.GRAY_WOOL)
                || state.is(Blocks.LIGHT_GRAY_WOOL)
                || state.is(Blocks.CYAN_WOOL)
                || state.is(Blocks.PURPLE_WOOL)
                || state.is(Blocks.BLUE_WOOL)
                || state.is(Blocks.BROWN_WOOL)
                || state.is(Blocks.GREEN_WOOL)
                || state.is(Blocks.RED_WOOL)
                || state.is(Blocks.BLACK_WOOL)) {

            change(level, pos, state, Blocks.GREEN_CONCRETE);
            return true;
        }

        else if (state.is(Blocks.GREEN_CONCRETE)) {

            change(level, pos, state, Blocks.WHITE_WOOL);
            return true;
        }

        // CARPETS
        else if (state.is(Blocks.WHITE_CARPET)
                || state.is(Blocks.ORANGE_CARPET)
                || state.is(Blocks.MAGENTA_CARPET)
                || state.is(Blocks.LIGHT_BLUE_CARPET)
                || state.is(Blocks.YELLOW_CARPET)
                || state.is(Blocks.LIME_CARPET)
                || state.is(Blocks.PINK_CARPET)
                || state.is(Blocks.GRAY_CARPET)
                || state.is(Blocks.LIGHT_GRAY_CARPET)
                || state.is(Blocks.CYAN_CARPET)
                || state.is(Blocks.PURPLE_CARPET)
                || state.is(Blocks.BLUE_CARPET)
                || state.is(Blocks.BROWN_CARPET)
                || state.is(Blocks.GREEN_CARPET)
                || state.is(Blocks.RED_CARPET)
                || state.is(Blocks.BLACK_CARPET)) {

            change(level, pos, state, Blocks.MOSS_CARPET);
            return true;
        }

        else if (state.is(Blocks.MOSS_CARPET)) {

            change(level, pos, state, Blocks.WHITE_CARPET);
            return true;
        }

        // BEDS
        else if (state.is(Blocks.WHITE_BED)
                || state.is(Blocks.ORANGE_BED)
                || state.is(Blocks.MAGENTA_BED)
                || state.is(Blocks.LIGHT_BLUE_BED)
                || state.is(Blocks.YELLOW_BED)
                || state.is(Blocks.LIME_BED)
                || state.is(Blocks.PINK_BED)
                || state.is(Blocks.GRAY_BED)
                || state.is(Blocks.LIGHT_GRAY_BED)
                || state.is(Blocks.CYAN_BED)
                || state.is(Blocks.PURPLE_BED)
                || state.is(Blocks.BLUE_BED)
                || state.is(Blocks.BROWN_BED)
                || state.is(Blocks.GREEN_BED)
                || state.is(Blocks.RED_BED)
                || state.is(Blocks.BLACK_BED)) {

            change(level, pos, state, Blocks.SEA_LANTERN);
            return true;
        }

        else if (state.is(Blocks.SEA_LANTERN)) {

            change(level, pos, state, Blocks.WHITE_BED);
            return true;
        }

        // TORCHES
        else if (state.is(Blocks.TORCH)
                || state.is(Blocks.WALL_TORCH)
                || state.is(Blocks.SOUL_TORCH)
                || state.is(Blocks.SOUL_WALL_TORCH)) {

            change(level, pos, state, Blocks.GLOWSTONE);
            return true;
        }

        else if (state.is(Blocks.GLOWSTONE)) {

            change(level, pos, state, Blocks.TORCH);
            return true;
        }

        // LANTERNS
        else if (state.is(Blocks.LANTERN)
                || state.is(Blocks.SOUL_LANTERN)) {

            change(level, pos, state, Blocks.SHROOMLIGHT);
            return true;
        }

        else if (state.is(Blocks.SHROOMLIGHT)) {

            change(level, pos, state, Blocks.LANTERN);
            return true;
        }

        // CANDLES
        else if (state.is(Blocks.CANDLE)
                || state.is(Blocks.WHITE_CANDLE)
                || state.is(Blocks.ORANGE_CANDLE)
                || state.is(Blocks.MAGENTA_CANDLE)
                || state.is(Blocks.LIGHT_BLUE_CANDLE)
                || state.is(Blocks.YELLOW_CANDLE)
                || state.is(Blocks.LIME_CANDLE)
                || state.is(Blocks.PINK_CANDLE)
                || state.is(Blocks.GRAY_CANDLE)
                || state.is(Blocks.LIGHT_GRAY_CANDLE)
                || state.is(Blocks.CYAN_CANDLE)
                || state.is(Blocks.PURPLE_CANDLE)
                || state.is(Blocks.BLUE_CANDLE)
                || state.is(Blocks.BROWN_CANDLE)
                || state.is(Blocks.GREEN_CANDLE)
                || state.is(Blocks.RED_CANDLE)
                || state.is(Blocks.BLACK_CANDLE)
                || state.is(Blocks.CAKE)
                || state.is(Blocks.CANDLE_CAKE)
                || state.is(Blocks.WHITE_CANDLE_CAKE)
                || state.is(Blocks.ORANGE_CANDLE_CAKE)
                || state.is(Blocks.MAGENTA_CANDLE_CAKE)
                || state.is(Blocks.LIGHT_BLUE_CANDLE_CAKE)
                || state.is(Blocks.YELLOW_CANDLE_CAKE)
                || state.is(Blocks.LIME_CANDLE_CAKE)
                || state.is(Blocks.PINK_CANDLE_CAKE)
                || state.is(Blocks.GRAY_CANDLE_CAKE)
                || state.is(Blocks.LIGHT_GRAY_CANDLE_CAKE)
                || state.is(Blocks.CYAN_CANDLE_CAKE)
                || state.is(Blocks.PURPLE_CANDLE_CAKE)
                || state.is(Blocks.BLUE_CANDLE_CAKE)
                || state.is(Blocks.BROWN_CANDLE_CAKE)
                || state.is(Blocks.GREEN_CANDLE_CAKE)
                || state.is(Blocks.RED_CANDLE_CAKE)
                || state.is(Blocks.BLACK_CANDLE_CAKE)) {

            change(level, pos, state, Blocks.END_ROD);
            return true;
        }

        else if (state.is(Blocks.END_ROD)) {

            change(level, pos, state, Blocks.CANDLE);
            return true;
        }

        // FLOWER POTS
        else if (state.is(Blocks.FLOWER_POT)
                || state.is(Blocks.POTTED_OAK_SAPLING)
                || state.is(Blocks.POTTED_SPRUCE_SAPLING)
                || state.is(Blocks.POTTED_BIRCH_SAPLING)
                || state.is(Blocks.POTTED_JUNGLE_SAPLING)
                || state.is(Blocks.POTTED_ACACIA_SAPLING)
                || state.is(Blocks.POTTED_DARK_OAK_SAPLING)
                || state.is(Blocks.POTTED_MANGROVE_PROPAGULE)
                || state.is(Blocks.POTTED_CHERRY_SAPLING)
                || state.is(Blocks.POTTED_BAMBOO)
                || state.is(Blocks.POTTED_AZALEA)
                || state.is(Blocks.POTTED_FLOWERING_AZALEA)
                || state.is(Blocks.POTTED_DANDELION)
                || state.is(Blocks.POTTED_POPPY)
                || state.is(Blocks.POTTED_BLUE_ORCHID)
                || state.is(Blocks.POTTED_ALLIUM)
                || state.is(Blocks.POTTED_AZURE_BLUET)
                || state.is(Blocks.POTTED_RED_TULIP)
                || state.is(Blocks.POTTED_ORANGE_TULIP)
                || state.is(Blocks.POTTED_WHITE_TULIP)
                || state.is(Blocks.POTTED_PINK_TULIP)
                || state.is(Blocks.POTTED_OXEYE_DAISY)
                || state.is(Blocks.POTTED_CORNFLOWER)
                || state.is(Blocks.POTTED_LILY_OF_THE_VALLEY)
                || state.is(Blocks.POTTED_WITHER_ROSE)
                || state.is(Blocks.POTTED_RED_MUSHROOM)
                || state.is(Blocks.POTTED_BROWN_MUSHROOM)
                || state.is(Blocks.POTTED_DEAD_BUSH)
                || state.is(Blocks.POTTED_CACTUS)
                || state.is(Blocks.POTTED_CRIMSON_FUNGUS)
                || state.is(Blocks.POTTED_WARPED_FUNGUS)
                || state.is(Blocks.POTTED_CRIMSON_ROOTS)
                || state.is(Blocks.POTTED_WARPED_ROOTS)
                || state.is(Blocks.POTTED_TORCHFLOWER)) {

            change(level, pos, state, Blocks.DECORATED_POT);
            return true;
        }

        else if (state.is(Blocks.DECORATED_POT)) {

            change(level, pos, state, Blocks.FLOWER_POT);
            return true;
        }

        // SAPLINGS
        else if (state.is(Blocks.OAK_SAPLING)
                || state.is(Blocks.SPRUCE_SAPLING)
                || state.is(Blocks.BIRCH_SAPLING)
                || state.is(Blocks.JUNGLE_SAPLING)
                || state.is(Blocks.ACACIA_SAPLING)
                || state.is(Blocks.DARK_OAK_SAPLING)
                || state.is(Blocks.MANGROVE_PROPAGULE)
                || state.is(Blocks.CHERRY_SAPLING)
                || state.is(Blocks.AZALEA)
                || state.is(Blocks.FLOWERING_AZALEA)) {

            change(level, pos, state, Blocks.HONEY_BLOCK);
            return true;
        }

        else if (state.is(Blocks.HONEY_BLOCK)) {

            change(level, pos, state, Blocks.OAK_SAPLING);
            return true;
        }

        // FLOWERS
        else if (state.is(Blocks.DANDELION)
                || state.is(Blocks.POPPY)
                || state.is(Blocks.BLUE_ORCHID)
                || state.is(Blocks.ALLIUM)
                || state.is(Blocks.AZURE_BLUET)
                || state.is(Blocks.RED_TULIP)
                || state.is(Blocks.ORANGE_TULIP)
                || state.is(Blocks.WHITE_TULIP)
                || state.is(Blocks.PINK_TULIP)
                || state.is(Blocks.OXEYE_DAISY)
                || state.is(Blocks.CORNFLOWER)
                || state.is(Blocks.LILY_OF_THE_VALLEY)
                || state.is(Blocks.WITHER_ROSE)
                || state.is(Blocks.SUNFLOWER)
                || state.is(Blocks.LILAC)
                || state.is(Blocks.ROSE_BUSH)
                || state.is(Blocks.PEONY)
                || state.is(Blocks.TORCHFLOWER)
                || state.is(Blocks.PITCHER_PLANT)) {

            change(level, pos, state, Blocks.SPORE_BLOSSOM);
            return true;
        }

        else if (state.is(Blocks.SPORE_BLOSSOM)) {

            change(level, pos, state, Blocks.POPPY);
            return true;
        }

        // SMALL PLANTS
        else if (state.is(Blocks.SHORT_GRASS)
                || state.is(Blocks.FERN)
                || state.is(Blocks.TALL_GRASS)
                || state.is(Blocks.LARGE_FERN)
                || state.is(Blocks.DEAD_BUSH)
                || state.is(Blocks.SEAGRASS)
                || state.is(Blocks.SEA_PICKLE)
                || state.is(Blocks.LILY_PAD)
                || state.is(Blocks.VINE)
                || state.is(Blocks.GLOW_LICHEN)
                || state.is(Blocks.HANGING_ROOTS)
                || state.is(Blocks.BAMBOO_SAPLING)
                || state.is(Blocks.BAMBOO)
                || state.is(Blocks.BROWN_MUSHROOM)
                || state.is(Blocks.RED_MUSHROOM)
                || state.is(Blocks.BROWN_MUSHROOM_BLOCK)
                || state.is(Blocks.RED_MUSHROOM_BLOCK)
                || state.is(Blocks.CACTUS)
                || state.is(Blocks.SUGAR_CANE)
                || state.is(Blocks.PUMPKIN)
                || state.is(Blocks.MELON)
                || state.is(Blocks.CRIMSON_FUNGUS)
                || state.is(Blocks.WARPED_FUNGUS)
                || state.is(Blocks.CRIMSON_ROOTS)
                || state.is(Blocks.WARPED_ROOTS)
                || state.is(Blocks.NETHER_SPROUTS)
                || state.is(Blocks.WEEPING_VINES)
                || state.is(Blocks.WEEPING_VINES_PLANT)
                || state.is(Blocks.TWISTING_VINES)
                || state.is(Blocks.TWISTING_VINES_PLANT)) {

            change(level, pos, state, Blocks.HAY_BLOCK);
            return true;
        }

        else if (state.is(Blocks.HAY_BLOCK)) {

            change(level, pos, state, Blocks.SHORT_GRASS);
            return true;
        }

        // CROPS
        else if (state.is(Blocks.WHEAT)
                || state.is(Blocks.CARROTS)
                || state.is(Blocks.POTATOES)
                || state.is(Blocks.BEETROOTS)
                || state.is(Blocks.MELON_STEM)
                || state.is(Blocks.PUMPKIN_STEM)
                || state.is(Blocks.ATTACHED_MELON_STEM)
                || state.is(Blocks.ATTACHED_PUMPKIN_STEM)
                || state.is(Blocks.SWEET_BERRY_BUSH)
                || state.is(Blocks.COCOA)
                || state.is(Blocks.TORCHFLOWER_CROP)
                || state.is(Blocks.PITCHER_CROP)
                || state.is(Blocks.NETHER_WART)) {

            change(level, pos, state, Blocks.BONE_BLOCK);
            return true;
        }

        else if (state.is(Blocks.BONE_BLOCK)) {

            change(level, pos, state, Blocks.WHEAT);
            return true;
        }

        // RAILS
        else if (state.is(Blocks.RAIL)
                || state.is(Blocks.POWERED_RAIL)
                || state.is(Blocks.DETECTOR_RAIL)
                || state.is(Blocks.ACTIVATOR_RAIL)) {

            change(level, pos, state, Blocks.REDSTONE_BLOCK);
            return true;
        }

        else if (state.is(Blocks.REDSTONE_BLOCK)) {

            change(level, pos, state, Blocks.RAIL);
            return true;
        }

        // REDSTONE SMALL BLOCKS
        else if (state.is(Blocks.REDSTONE_TORCH)
                || state.is(Blocks.REDSTONE_WALL_TORCH)
                || state.is(Blocks.REPEATER)
                || state.is(Blocks.COMPARATOR)
                || state.is(Blocks.LEVER)
                || state.is(Blocks.DAYLIGHT_DETECTOR)) {

            change(level, pos, state, Blocks.REDSTONE_LAMP);
            return true;
        }

        else if (state.is(Blocks.REDSTONE_LAMP)) {

            change(level, pos, state, Blocks.REDSTONE_TORCH);
            return true;
        }

        // CLIMBING AND SUPPORT BLOCKS
        else if (state.is(Blocks.LADDER)
                || state.is(Blocks.SCAFFOLDING)) {

            change(level, pos, state, Blocks.BAMBOO_BLOCK);
            return true;
        }

        else if (state.is(Blocks.BAMBOO_BLOCK)) {

            change(level, pos, state, Blocks.LADDER);
            return true;
        }

        // WORK BLOCKS
        else if (state.is(Blocks.CRAFTING_TABLE)) {

            change(level, pos, state, Blocks.SMITHING_TABLE);
            return true;
        }

        else if (state.is(Blocks.SMITHING_TABLE)) {

            change(level, pos, state, Blocks.CRAFTING_TABLE);
            return true;
        }

        else if (state.is(Blocks.FURNACE)) {

            change(level, pos, state, Blocks.BLAST_FURNACE);
            return true;
        }

        else if (state.is(Blocks.BLAST_FURNACE)) {

            change(level, pos, state, Blocks.FURNACE);
            return true;
        }

        else if (state.is(Blocks.SMOKER)) {

            change(level, pos, state, Blocks.CAMPFIRE);
            return true;
        }

        else if (state.is(Blocks.CAMPFIRE)) {

            change(level, pos, state, Blocks.SMOKER);
            return true;
        }

        else if (state.is(Blocks.CARTOGRAPHY_TABLE)) {

            change(level, pos, state, Blocks.LECTERN);
            return true;
        }

        else if (state.is(Blocks.LECTERN)) {

            change(level, pos, state, Blocks.CARTOGRAPHY_TABLE);
            return true;
        }

        else if (state.is(Blocks.FLETCHING_TABLE)) {

            change(level, pos, state, Blocks.TARGET);
            return true;
        }

        else if (state.is(Blocks.TARGET)) {

            change(level, pos, state, Blocks.FLETCHING_TABLE);
            return true;
        }

        else if (state.is(Blocks.GRINDSTONE)) {

            change(level, pos, state, Blocks.ANVIL);
            return true;
        }

        else if (state.is(Blocks.ANVIL)) {

            change(level, pos, state, Blocks.GRINDSTONE);
            return true;
        }

        else if (state.is(Blocks.LOOM)) {

            change(level, pos, state, Blocks.NOTE_BLOCK);
            return true;
        }

        else if (state.is(Blocks.NOTE_BLOCK)) {

            change(level, pos, state, Blocks.LOOM);
            return true;
        }

        else if (state.is(Blocks.STONECUTTER)) {

            change(level, pos, state, Blocks.CUT_COPPER);
            return true;
        }

        else if (state.is(Blocks.CUT_COPPER)) {

            change(level, pos, state, Blocks.STONECUTTER);
            return true;
        }

        else if (state.is(Blocks.BREWING_STAND)) {

            change(level, pos, state, Blocks.CAULDRON);
            return true;
        }

        else if (state.is(Blocks.CAULDRON)) {

            change(level, pos, state, Blocks.BREWING_STAND);
            return true;
        }

        else if (state.is(Blocks.COMPOSTER)) {

            change(level, pos, state, Blocks.BEEHIVE);
            return true;
        }

        else if (state.is(Blocks.BEEHIVE)) {

            change(level, pos, state, Blocks.COMPOSTER);
            return true;
        }

        else if (state.is(Blocks.BELL)) {

            change(level, pos, state, Blocks.AMETHYST_BLOCK);
            return true;
        }

        else if (state.is(Blocks.AMETHYST_BLOCK)) {

            change(level, pos, state, Blocks.BELL);
            return true;
        }

        else if (state.is(Blocks.RESPAWN_ANCHOR)) {

            change(level, pos, state, Blocks.NETHERITE_BLOCK);
            return true;
        }

        else if (state.is(Blocks.NETHERITE_BLOCK)) {

            change(level, pos, state, Blocks.RESPAWN_ANCHOR);
            return true;
        }

        // STORAGE BLOCKS
        else if (state.is(Blocks.CHEST)) {

            change(level, pos, state, Blocks.BARREL);
            return true;
        }

        else if (state.is(Blocks.BARREL)) {

            change(level, pos, state, Blocks.CHEST);
            return true;
        }

        else if (state.is(Blocks.TRAPPED_CHEST)) {

            change(level, pos, state, Blocks.ENDER_CHEST);
            return true;
        }

        else if (state.is(Blocks.ENDER_CHEST)) {

            change(level, pos, state, Blocks.TRAPPED_CHEST);
            return true;
        }

        Block fallbackBlock =
                fallbackBlockFor(state);

        if (fallbackBlock != null) {
            change(level, pos, state, fallbackBlock);
            return true;
        }

        return false;
    }

    private static Block fallbackBlockFor(
            BlockState state
    ) {

        Block block =
                state.getBlock();

        if (state.isAir()
                || state.is(Blocks.WATER)
                || state.is(Blocks.LAVA)
                || block == Blocks.MOVING_PISTON
                || block == Blocks.PISTON_HEAD
                || block == Blocks.END_PORTAL
                || block == Blocks.NETHER_PORTAL
                || block == Blocks.END_GATEWAY) {
            return null;
        }

        Identifier blockId =
                BuiltInRegistries.BLOCK.getKey(block);

        String id =
                blockId.toString();

        if (id.contains("sand")
                || id.contains("beach")
                || id.contains("gravel")
                || id.contains("clay")
                || id.contains("terracotta")) {
            return Blocks.GLASS;
        }

        if (state.is(BlockTags.LEAVES)
                || id.contains("leaf")
                || id.contains("leaves")
                || id.contains("vine")
                || id.contains("moss")
                || id.contains("azalea")) {
            return Blocks.EMERALD_BLOCK;
        }

        if (state.is(BlockTags.LOGS)
                || id.contains("stem")
                || id.contains("hyphae")
                || id.contains("wood")) {
            return Blocks.GOLD_BLOCK;
        }

        if (id.contains("ore")
                || id.contains("raw_")
                || id.contains("deepslate")) {
            return Blocks.DIAMOND_ORE;
        }

        if (state.is(BlockTags.MINEABLE_WITH_SHOVEL)
                || id.contains("dirt")
                || id.contains("mud")
                || id.contains("snow")
                || id.contains("ice")) {
            return Blocks.MOSS_BLOCK;
        }

        if (state.is(BlockTags.MINEABLE_WITH_AXE)
                || id.contains("plank")
                || id.contains("bamboo")) {
            return Blocks.HONEYCOMB_BLOCK;
        }

        if (state.is(BlockTags.MINEABLE_WITH_HOE)
                || id.contains("wool")
                || id.contains("sculk")) {
            return Blocks.GREEN_CONCRETE;
        }

        if (id.contains("glass")) {
            return Blocks.SAND;
        }

        if (id.contains("copper")
                || id.contains("iron")
                || id.contains("gold")
                || id.contains("diamond")
                || id.contains("emerald")
                || id.contains("netherite")) {
            return Blocks.AMETHYST_BLOCK;
        }

        if (state.is(BlockTags.MINEABLE_WITH_PICKAXE)
                || id.contains("stone")
                || id.contains("brick")
                || id.contains("quartz")
                || id.contains("blackstone")
                || id.contains("basalt")
                || id.contains("prismarine")
                || id.contains("purpur")
                || id.contains("end_")) {
            return Blocks.AMETHYST_BLOCK;
        }

        return Blocks.SEA_LANTERN;
    }

    private static void change(
            Level level,
            BlockPos pos,
            BlockState currentState,
            Block newBlock
    ) {

        level.setBlock(
                pos,
                copyProperties(
                        currentState,
                        newBlock.defaultBlockState()
                ),
                3
        );
    }

    private static BlockState copyProperties(
            BlockState currentState,
            BlockState newState
    ) {

        if (currentState.hasProperty(BlockStateProperties.HORIZONTAL_FACING)
                && newState.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {

            newState =
                    newState.setValue(
                            BlockStateProperties.HORIZONTAL_FACING,
                            currentState.getValue(BlockStateProperties.HORIZONTAL_FACING)
                    );
        }

        if (currentState.hasProperty(BlockStateProperties.FACING)
                && newState.hasProperty(BlockStateProperties.FACING)) {

            newState =
                    newState.setValue(
                            BlockStateProperties.FACING,
                            currentState.getValue(BlockStateProperties.FACING)
                    );
        }

        if (currentState.hasProperty(BlockStateProperties.AXIS)
                && newState.hasProperty(BlockStateProperties.AXIS)) {

            newState =
                    newState.setValue(
                            BlockStateProperties.AXIS,
                            currentState.getValue(BlockStateProperties.AXIS)
                    );
        }

        if (currentState.hasProperty(BlockStateProperties.HALF)
                && newState.hasProperty(BlockStateProperties.HALF)) {

            newState =
                    newState.setValue(
                            BlockStateProperties.HALF,
                            currentState.getValue(BlockStateProperties.HALF)
                    );
        }

        if (currentState.hasProperty(BlockStateProperties.STAIRS_SHAPE)
                && newState.hasProperty(BlockStateProperties.STAIRS_SHAPE)) {

            newState =
                    newState.setValue(
                            BlockStateProperties.STAIRS_SHAPE,
                            currentState.getValue(BlockStateProperties.STAIRS_SHAPE)
                    );
        }

        if (currentState.hasProperty(BlockStateProperties.SLAB_TYPE)
                && newState.hasProperty(BlockStateProperties.SLAB_TYPE)) {

            newState =
                    newState.setValue(
                            BlockStateProperties.SLAB_TYPE,
                            currentState.getValue(BlockStateProperties.SLAB_TYPE)
                    );
        }

        if (currentState.hasProperty(BlockStateProperties.DOUBLE_BLOCK_HALF)
                && newState.hasProperty(BlockStateProperties.DOUBLE_BLOCK_HALF)) {

            newState =
                    newState.setValue(
                            BlockStateProperties.DOUBLE_BLOCK_HALF,
                            currentState.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF)
                    );
        }

        if (currentState.hasProperty(BlockStateProperties.DOOR_HINGE)
                && newState.hasProperty(BlockStateProperties.DOOR_HINGE)) {

            newState =
                    newState.setValue(
                            BlockStateProperties.DOOR_HINGE,
                            currentState.getValue(BlockStateProperties.DOOR_HINGE)
                    );
        }

        if (currentState.hasProperty(BlockStateProperties.ATTACH_FACE)
                && newState.hasProperty(BlockStateProperties.ATTACH_FACE)) {

            newState =
                    newState.setValue(
                            BlockStateProperties.ATTACH_FACE,
                            currentState.getValue(BlockStateProperties.ATTACH_FACE)
                    );
        }

        if (currentState.hasProperty(BlockStateProperties.OPEN)
                && newState.hasProperty(BlockStateProperties.OPEN)) {

            newState =
                    newState.setValue(
                            BlockStateProperties.OPEN,
                            currentState.getValue(BlockStateProperties.OPEN)
                    );
        }

        if (currentState.hasProperty(BlockStateProperties.POWERED)
                && newState.hasProperty(BlockStateProperties.POWERED)) {

            newState =
                    newState.setValue(
                            BlockStateProperties.POWERED,
                            currentState.getValue(BlockStateProperties.POWERED)
                    );
        }

        if (currentState.hasProperty(BlockStateProperties.WATERLOGGED)
                && newState.hasProperty(BlockStateProperties.WATERLOGGED)) {

            newState =
                    newState.setValue(
                            BlockStateProperties.WATERLOGGED,
                            currentState.getValue(BlockStateProperties.WATERLOGGED)
                    );
        }

        if (currentState.hasProperty(BlockStateProperties.NORTH)
                && newState.hasProperty(BlockStateProperties.NORTH)) {

            newState =
                    newState.setValue(
                            BlockStateProperties.NORTH,
                            currentState.getValue(BlockStateProperties.NORTH)
                    );
        }

        if (currentState.hasProperty(BlockStateProperties.EAST)
                && newState.hasProperty(BlockStateProperties.EAST)) {

            newState =
                    newState.setValue(
                            BlockStateProperties.EAST,
                            currentState.getValue(BlockStateProperties.EAST)
                    );
        }

        if (currentState.hasProperty(BlockStateProperties.SOUTH)
                && newState.hasProperty(BlockStateProperties.SOUTH)) {

            newState =
                    newState.setValue(
                            BlockStateProperties.SOUTH,
                            currentState.getValue(BlockStateProperties.SOUTH)
                    );
        }

        if (currentState.hasProperty(BlockStateProperties.WEST)
                && newState.hasProperty(BlockStateProperties.WEST)) {

            newState =
                    newState.setValue(
                            BlockStateProperties.WEST,
                            currentState.getValue(BlockStateProperties.WEST)
                    );
        }

        return newState;
    }
}
