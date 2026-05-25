package com.magicworld.event;

import com.magicworld.MagicWorld;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

public class CraftEvents {

    public void onLeftClickBlock(
            PlayerInteractEvent.LeftClickBlock event
    ) {

        Level level = event.getLevel();

        if (event.getHand() != InteractionHand.MAIN_HAND)
            return;

        ItemStack held =
                event.getEntity().getMainHandItem();

        if (held.getItem()
                != MagicWorld.VARINHA_MAGICA.get())
            return;

        event.setCanceled(true);

        if (level.isClientSide()) return;

        BlockPos pos = event.getPos();

        var state =
                level.getBlockState(pos);

        if (event.getEntity().isShiftKeyDown()) {
            if (!state.isAir()) {
                boolean destroyed =
                        level.destroyBlock(
                                pos,
                                true,
                                event.getEntity()
                        );

                if (!destroyed) {
                    level.setBlock(
                            pos,
                            Blocks.AIR.defaultBlockState(),
                            3
                    );
                }

                MagicWorld.effects(
                        (ServerLevel) level,
                        pos
                );
            }

            return;
        }

        if (PremiumBlocks.transform(
                level,
                pos,
                state
        )) {

            MagicWorld.effects(
                    (ServerLevel) level,
                    pos
            );

            return;
        }

        if (state.is(Blocks.STONE)
                || state.is(Blocks.ANDESITE)
                || state.is(Blocks.DIORITE)
                || state.is(Blocks.GRANITE)) {

            level.setBlock(
                    pos,
                    Blocks.COAL_ORE.defaultBlockState(),
                    3
            );
        }

        else if (state.is(Blocks.COAL_ORE)) {

            level.setBlock(
                    pos,
                    Blocks.STONE.defaultBlockState(),
                    3
            );
        }

        else if (state.is(Blocks.COBBLESTONE)) {

            level.setBlock(
                    pos,
                    Blocks.IRON_ORE.defaultBlockState(),
                    3
            );
        }

        else if (state.is(Blocks.IRON_ORE)) {

            level.setBlock(
                    pos,
                    Blocks.COBBLESTONE.defaultBlockState(),
                    3
            );
        }

        else if (state.is(Blocks.DIRT)
                || state.is(Blocks.GRASS_BLOCK)
                || state.is(Blocks.COARSE_DIRT)) {

            level.setBlock(
                    pos,
                    Blocks.MOSS_BLOCK.defaultBlockState(),
                    3
            );
        }

        else if (state.is(Blocks.SAND)
                || state.is(Blocks.RED_SAND)
                || state.is(Blocks.SANDSTONE)
                || state.is(Blocks.CUT_SANDSTONE)
                || state.is(Blocks.SMOOTH_SANDSTONE)
                || state.is(Blocks.RED_SANDSTONE)
                || state.is(Blocks.CUT_RED_SANDSTONE)
                || state.is(Blocks.SMOOTH_RED_SANDSTONE)) {

            level.setBlock(
                    pos,
                    Blocks.GLASS.defaultBlockState(),
                    3
            );
        }

        else if (state.is(Blocks.GLASS)) {

            level.setBlock(
                    pos,
                    Blocks.SAND.defaultBlockState(),
                    3
            );
        }

        else if (state.is(Blocks.OAK_LEAVES)
                || state.is(Blocks.SPRUCE_LEAVES)
                || state.is(Blocks.BIRCH_LEAVES)
                || state.is(Blocks.JUNGLE_LEAVES)
                || state.is(Blocks.ACACIA_LEAVES)
                || state.is(Blocks.DARK_OAK_LEAVES)
                || state.is(Blocks.MANGROVE_LEAVES)
                || state.is(Blocks.CHERRY_LEAVES)
                || state.is(Blocks.AZALEA_LEAVES)
                || state.is(Blocks.FLOWERING_AZALEA_LEAVES)) {

            level.setBlock(
                    pos,
                    Blocks.EMERALD_BLOCK.defaultBlockState(),
                    3
            );
        }

        else if (state.is(Blocks.EMERALD_BLOCK)) {

            level.setBlock(
                    pos,
                    Blocks.OAK_LEAVES.defaultBlockState(),
                    3
            );
        }

        else if (state.is(BlockTags.LOGS)) {

            level.setBlock(
                    pos,
                    Blocks.GOLD_BLOCK.defaultBlockState(),
                    3
            );
        }

        else if (state.is(Blocks.GOLD_BLOCK)) {

            level.setBlock(
                    pos,
                    Blocks.OAK_LOG.defaultBlockState(),
                    3
            );
        }

        else if (state.is(Blocks.DEEPSLATE_COAL_ORE)) {

            level.setBlock(
                    pos,
                    Blocks.DEEPSLATE_DIAMOND_ORE.defaultBlockState(),
                    3
            );
        }

        else if (state.is(Blocks.COPPER_ORE)) {

            level.setBlock(
                    pos,
                    Blocks.IRON_ORE.defaultBlockState(),
                    3
            );
        }

        else if (state.is(Blocks.DEEPSLATE_COPPER_ORE)) {

            level.setBlock(
                    pos,
                    Blocks.DEEPSLATE_IRON_ORE.defaultBlockState(),
                    3
            );
        }

        else if (state.is(Blocks.DEEPSLATE_IRON_ORE)) {

            level.setBlock(
                    pos,
                    Blocks.DEEPSLATE_GOLD_ORE.defaultBlockState(),
                    3
            );
        }

        else if (state.is(Blocks.GOLD_ORE)) {

            level.setBlock(
                    pos,
                    Blocks.EMERALD_ORE.defaultBlockState(),
                    3
            );
        }

        else if (state.is(Blocks.DEEPSLATE_GOLD_ORE)) {

            level.setBlock(
                    pos,
                    Blocks.DEEPSLATE_EMERALD_ORE.defaultBlockState(),
                    3
            );
        }

        else if (state.is(Blocks.REDSTONE_ORE)) {

            level.setBlock(
                    pos,
                    Blocks.LAPIS_ORE.defaultBlockState(),
                    3
            );
        }

        else if (state.is(Blocks.DEEPSLATE_REDSTONE_ORE)) {

            level.setBlock(
                    pos,
                    Blocks.DEEPSLATE_LAPIS_ORE.defaultBlockState(),
                    3
            );
        }

        else if (state.is(Blocks.LAPIS_ORE)) {

            level.setBlock(
                    pos,
                    Blocks.EMERALD_ORE.defaultBlockState(),
                    3
            );
        }

        else if (state.is(Blocks.DEEPSLATE_LAPIS_ORE)) {

            level.setBlock(
                    pos,
                    Blocks.DEEPSLATE_EMERALD_ORE.defaultBlockState(),
                    3
            );
        }

        if (!level.getBlockState(pos).equals(state)) {
            MagicWorld.effects(
                    (ServerLevel) level,
                    pos
            );
        }
    }
}
