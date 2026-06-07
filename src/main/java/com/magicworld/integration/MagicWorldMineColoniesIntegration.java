package com.magicworld.integration;

import com.magicworld.MagicWorld;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;

public final class MagicWorldMineColoniesIntegration {
    private static final String MOD_ID = "minecolonies";
    private static final String LAST_COLONY = "MagicWorldMineColoniesLastColony";
    private static final String TOWN_HALL = "MagicWorldMineColoniesTownHall";
    private static final String LAST_BUILDING = "MagicWorldMineColoniesLastBuilding";

    @SubscribeEvent
    public void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || event.getLevel().isClientSide()) {
            return;
        }

        recordMineColoniesBlock(player, event.getLevel().getBlockState(event.getPos()), event.getPos());
    }

    @SubscribeEvent
    public void onBlockPlaced(BlockEvent.EntityPlaceEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || event.getLevel().isClientSide()) {
            return;
        }

        recordMineColoniesBlock(player, event.getPlacedBlock(), event.getPos());
    }

    private static void recordMineColoniesBlock(ServerPlayer player, BlockState state, BlockPos pos) {
        ResourceLocation blockId = BuiltInRegistries.BLOCK.getKey(state.getBlock());
        if (!MOD_ID.equals(blockId.getNamespace())) {
            return;
        }

        String path = blockId.getPath();
        if (path.equals("blockhuttownhall")) {
            saveTarget(player, TOWN_HALL, pos, player.level().dimension(), "Town Hall");
            saveTarget(player, LAST_COLONY, pos, player.level().dimension(), "Colonia");
            return;
        }

        if (isColonyBuilding(path)) {
            saveTarget(player, LAST_BUILDING, pos, player.level().dimension(), readableName(path));
            saveTarget(player, LAST_COLONY, pos, player.level().dimension(), "Colonia");
        }
    }

    public static boolean handleAction(ServerPlayer player, String action) {
        return switch (action) {
            case "minecolonies_register_current" -> {
                registerCurrentPosition(player);
                yield true;
            }
            case "minecolonies_teleport_last_colony" -> {
                teleportToSavedTarget(player, LAST_COLONY);
                yield true;
            }
            case "minecolonies_teleport_town_hall" -> {
                teleportToSavedTarget(player, TOWN_HALL);
                yield true;
            }
            case "minecolonies_teleport_last_building" -> {
                teleportToSavedTarget(player, LAST_BUILDING);
                yield true;
            }
            default -> false;
        };
    }

    public static boolean isMineColoniesLoaded() {
        return ModList.get().isLoaded(MOD_ID);
    }

    private static void registerCurrentPosition(ServerPlayer player) {
        saveTarget(player, LAST_COLONY, player.blockPosition(), player.level().dimension(), "Colonia manual");
        saveTarget(player, LAST_BUILDING, player.blockPosition(), player.level().dimension(), "Marcador manual");
        MagicWorld.effects(player.serverLevel(), player.blockPosition());
    }

    private static boolean isColonyBuilding(String path) {
        return path.startsWith("blockhut")
                || path.equals("supplycamp")
                || path.equals("supplychest")
                || path.equals("decorationcontroller")
                || path.equals("colonysign")
                || path.equals("blockwaypoint");
    }

    private static void saveTarget(
            ServerPlayer player,
            String key,
            BlockPos pos,
            ResourceKey<Level> dimension,
            String label
    ) {
        CompoundTag tag = player.getPersistentData();
        CompoundTag target = new CompoundTag();
        target.putString("Dimension", dimension.location().toString());
        target.putString("Label", label);
        target.putInt("X", pos.getX());
        target.putInt("Y", pos.getY());
        target.putInt("Z", pos.getZ());
        tag.put(key, target);
    }

    private static void teleportToSavedTarget(ServerPlayer player, String key) {
        CompoundTag target = player.getPersistentData().getCompound(key);
        if (!target.contains("Dimension") || !target.contains("X")) {
            return;
        }

        ResourceLocation dimensionId = ResourceLocation.tryParse(target.getString("Dimension"));
        if (dimensionId == null) {
            return;
        }

        ServerLevel level = player.server.getLevel(ResourceKey.create(Registries.DIMENSION, dimensionId));
        if (level == null) {
            return;
        }

        BlockPos savedPos = new BlockPos(target.getInt("X"), target.getInt("Y"), target.getInt("Z"));
        BlockPos safePos = findSafeTeleportPos(level, savedPos);
        player.teleportTo(
                level,
                safePos.getX() + 0.5D,
                safePos.getY(),
                safePos.getZ() + 0.5D,
                player.getYRot(),
                player.getXRot()
        );
        MagicWorld.effects(level, safePos);
    }

    private static BlockPos findSafeTeleportPos(ServerLevel level, BlockPos origin) {
        for (int radius = 0; radius <= 6; radius++) {
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    BlockPos base = origin.offset(dx, 0, dz);
                    BlockPos safe = findSafeColumn(level, base);
                    if (safe != null) {
                        return safe;
                    }
                }
            }
        }
        return origin.above();
    }

    private static BlockPos findSafeColumn(ServerLevel level, BlockPos base) {
        int minY = Math.max(level.getMinBuildHeight() + 1, base.getY() - 8);
        int maxY = Math.min(level.getMaxBuildHeight() - 2, base.getY() + 8);
        for (int y = maxY; y >= minY; y--) {
            BlockPos feet = new BlockPos(base.getX(), y, base.getZ());
            if (canStandAt(level, feet)) {
                return feet;
            }
        }
        return null;
    }

    private static boolean canStandAt(ServerLevel level, BlockPos feet) {
        return level.getBlockState(feet.below()).isSolidRender(level, feet.below())
                && level.getBlockState(feet).getCollisionShape(level, feet).isEmpty()
                && level.getBlockState(feet.above()).getCollisionShape(level, feet.above()).isEmpty();
    }

    private static String readableName(String path) {
        String cleaned = path
                .replace("blockhut", "")
                .replace("block", "")
                .replace('_', ' ');
        if (cleaned.isBlank()) {
            return "Construcao";
        }
        return cleaned.substring(0, 1).toUpperCase() + cleaned.substring(1);
    }
}
