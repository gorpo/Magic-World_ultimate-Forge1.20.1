package com.magicworld.integration;

import com.magicworld.MagicWorld;
import com.magicworld.network.MagicWorldNetwork;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

public final class MagicWorldLocationManager {
    public static final String HOME = "home";
    public static final String SANCTUARY = "sanctuary";
    public static final String PORTAL_PLAZA = "portal_plaza";
    public static final String CASTLE = "castle";
    public static final String MANUAL = "manual";
    public static final String EXTERNAL_TELEPORT = "external_teleport";
    public static final String MINECOLONIES_LAST_COLONY = "minecolonies_last_colony";
    public static final String MINECOLONIES_TOWN_HALL = "minecolonies_town_hall";
    public static final String MINECOLONIES_LAST_BUILDING = "minecolonies_last_building";

    private static final String LOCATIONS_KEY = "MagicWorldLocations";
    private static final int AUTHORIZED_TELEPORT_TICKS = 100;
    private static final ConcurrentHashMap<UUID, Integer> AUTHORIZED_TELEPORTS = new ConcurrentHashMap<>();

    private MagicWorldLocationManager() {
    }

    public static void registerEstateLocations(
            ServerPlayer player,
            BlockPos home,
            BlockPos sanctuary,
            BlockPos portalPlaza,
            BlockPos castle
    ) {
        saveLocation(player, HOME, "Casa Magic World", home, Level.OVERWORLD, 80, 180, 255);
        saveLocation(player, SANCTUARY, "Santuario Magic World", sanctuary, Level.OVERWORLD, 180, 90, 255);
        saveLocation(player, PORTAL_PLAZA, "Praca de Portais Magic World", portalPlaza, Level.OVERWORLD, 80, 255, 210);
        if (castle != null) {
            saveLocation(player, CASTLE, "Castelo Magic World", castle, Level.OVERWORLD, 255, 210, 90);
        }
    }

    public static boolean handleAction(ServerPlayer player, String action) {
        if (action.startsWith("location_teleport_manual_coords:")) {
            teleportToManualCoordinates(player, action.substring("location_teleport_manual_coords:".length()));
            return true;
        }

        return switch (action) {
            case "teleport_home", "location_teleport_home" -> teleportToLocation(player, HOME);
            case "location_teleport_sanctuary" -> teleportToLocation(player, SANCTUARY);
            case "location_teleport_portal_plaza" -> teleportToLocation(player, PORTAL_PLAZA);
            case "location_teleport_castle" -> teleportToLocation(player, CASTLE);
            case "location_teleport_manual" -> teleportToLocation(player, MANUAL);
            case "location_teleport_external" -> teleportToLocation(player, EXTERNAL_TELEPORT);
            case "location_teleport_last_colony" -> teleportToLocation(player, MINECOLONIES_LAST_COLONY);
            case "location_teleport_town_hall" -> teleportToLocation(player, MINECOLONIES_TOWN_HALL);
            case "location_teleport_last_building" -> teleportToLocation(player, MINECOLONIES_LAST_BUILDING);
            case "location_register_current" -> {
                saveLocation(player, MANUAL, "Marcador manual Magic World", player.blockPosition(), player.level().dimension(), 255, 255, 120);
                sendInfo(player, "Local atual salvo. Use H > Locais para voltar.");
                yield true;
            }
            case "location_update_waypoints" -> {
                syncJourneyMapWaypoints(player);
                sendInfo(player, "Waypoints oficiais enviados ao JourneyMap sem beacons 3D.");
                sendActionbar(player, "JourneyMap atualizado: pontos ativos no mapa/lista, invisiveis no mundo.");
                yield true;
            }
            case "location_explain_external_teleport" -> {
                sendInfo(player, "Se MineColonies/MCA mover voce apos o spawn, o Magic World salva esse ponto e retorna para casa.");
                sendInfo(player, "Use H > Locais > Ultimo teleporte externo para voltar ao local salvo.");
                yield true;
            }
            default -> false;
        };
    }

    private static void teleportToManualCoordinates(ServerPlayer player, String payload) {
        String[] parts = payload.split(":");
        if (parts.length < 3) {
            sendWarning(player, "Coordenada manual invalida. Use X Y Z.");
            return;
        }

        try {
            BlockPos target = new BlockPos(
                    Integer.parseInt(parts[0].trim()),
                    Integer.parseInt(parts[1].trim()),
                    Integer.parseInt(parts[2].trim())
            );
            saveLocation(player, MANUAL, "Coordenada manual Magic World", target, player.level().dimension(), 255, 255, 120);
            teleportToLocation(player, MANUAL);
        } catch (NumberFormatException ignored) {
            sendWarning(player, "Coordenada manual invalida. Use numeros inteiros: X Y Z.");
        }
    }

    public static void saveLocation(
            ServerPlayer player,
            String id,
            String label,
            BlockPos pos,
            ResourceKey<Level> dimension,
            int red,
            int green,
            int blue
    ) {
        if (pos == null || dimension == null) {
            return;
        }

        CompoundTag target = new CompoundTag();
        target.putString("Label", label);
        target.putString("Dimension", dimension.location().toString());
        target.putInt("X", pos.getX());
        target.putInt("Y", pos.getY());
        target.putInt("Z", pos.getZ());
        target.putInt("R", red);
        target.putInt("G", green);
        target.putInt("B", blue);
        locations(player).put(id, target);
        sendJourneyMapWaypoint(player, id, target);
    }

    public static boolean teleportToLocation(ServerPlayer player, String id) {
        CompoundTag target = getLocation(player, id);
        if (target == null) {
            sendWarning(player, "Local ainda nao registrado: " + readableId(id) + ".");
            return true;
        }

        ServerLevel level = levelFor(player, target);
        if (level == null) {
            sendWarning(player, "Dimensao do local nao esta carregada.");
            return true;
        }

        BlockPos saved = new BlockPos(target.getInt("X"), target.getInt("Y"), target.getInt("Z"));
        BlockPos safe = findSafeTeleportPos(level, saved);
        markAuthorizedTeleport(player);
        player.teleportTo(
                level,
                safe.getX() + 0.5D,
                safe.getY(),
                safe.getZ() + 0.5D,
                Set.of(),
                player.getYRot(),
                player.getXRot()
        );
        MagicWorld.effects(level, safe);
        sendInfo(player, "Teleporte: " + target.getString("Label") + ".");
        return true;
    }

    public static void handleExternalTeleport(ServerPlayer player) {
        saveLocation(
                player,
                EXTERNAL_TELEPORT,
                "Ultimo teleporte externo",
                player.blockPosition(),
                player.level().dimension(),
                255,
                90,
                90
        );
        sendWarning(player, "Teleporte externo detectado. Salvamos este local e vamos retornar para sua casa.");
        sendWarning(player, "Use H > Locais > Ultimo teleporte externo para voltar se quiser.");
        teleportToLocation(player, HOME);
    }

    public static void syncJourneyMapWaypoints(ServerPlayer player) {
        CompoundTag all = locations(player);
        for (String id : all.getAllKeys()) {
            CompoundTag target = all.getCompound(id);
            if (target.contains("Dimension") && target.contains("X")) {
                sendJourneyMapWaypoint(player, id, target);
            }
        }
    }

    public static void sendStartupHelp(ServerPlayer player) {
        sendInfo(player, "Locais Magic World ativos: H > Sistema > Locais ou Esc > MagicWorld.");
        sendInfo(player, "Se outro mod mover voce apos entrar, salvamos o ponto e retornamos para casa.");
    }

    public static void markAuthorizedTeleport(ServerPlayer player) {
        AUTHORIZED_TELEPORTS.put(player.getUUID(), player.tickCount + AUTHORIZED_TELEPORT_TICKS);
    }

    public static boolean hasAuthorizedTeleport(ServerPlayer player) {
        Integer until = AUTHORIZED_TELEPORTS.get(player.getUUID());
        if (until == null) {
            return false;
        }
        if (player.tickCount <= until) {
            return true;
        }
        AUTHORIZED_TELEPORTS.remove(player.getUUID());
        return false;
    }

    private static CompoundTag locations(ServerPlayer player) {
        CompoundTag data = player.getPersistentData();
        if (!data.contains(LOCATIONS_KEY)) {
            data.put(LOCATIONS_KEY, new CompoundTag());
        }
        return data.getCompound(LOCATIONS_KEY);
    }

    private static CompoundTag getLocation(ServerPlayer player, String id) {
        CompoundTag all = locations(player);
        if (!all.contains(id)) {
            migrateLegacyLocation(player, id);
            all = locations(player);
            if (!all.contains(id)) {
                return null;
            }
        }
        CompoundTag target = all.getCompound(id);
        if (!target.contains("Dimension") || !target.contains("X")) {
            return null;
        }
        return target;
    }

    private static void migrateLegacyLocation(ServerPlayer player, String id) {
        String legacyKey = switch (id) {
            case MINECOLONIES_LAST_COLONY -> "MagicWorldMineColoniesLastColony";
            case MINECOLONIES_TOWN_HALL -> "MagicWorldMineColoniesTownHall";
            case MINECOLONIES_LAST_BUILDING -> "MagicWorldMineColoniesLastBuilding";
            default -> "";
        };
        if (legacyKey.isBlank() || !player.getPersistentData().contains(legacyKey)) {
            return;
        }

        CompoundTag legacy = player.getPersistentData().getCompound(legacyKey);
        if (!legacy.contains("Dimension") || !legacy.contains("X")) {
            return;
        }

        ResourceLocation dimensionId = ResourceLocation.tryParse(legacy.getString("Dimension"));
        if (dimensionId == null) {
            return;
        }

        String label = legacy.contains("Label") ? legacy.getString("Label") : readableId(id);
        saveLocation(
                player,
                id,
                label,
                new BlockPos(legacy.getInt("X"), legacy.getInt("Y"), legacy.getInt("Z")),
                ResourceKey.create(Registries.DIMENSION, dimensionId),
                80,
                204,
                130
        );
    }

    private static ServerLevel levelFor(ServerPlayer player, CompoundTag target) {
        ResourceLocation dimensionId = ResourceLocation.tryParse(target.getString("Dimension"));
        if (dimensionId == null) {
            return null;
        }
        return player.server.getLevel(ResourceKey.create(Registries.DIMENSION, dimensionId));
    }

    private static BlockPos findSafeTeleportPos(ServerLevel level, BlockPos origin) {
        for (int radius = 0; radius <= 8; radius++) {
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
        int minY = Math.max(level.getMinBuildHeight() + 1, base.getY() - 12);
        int maxY = Math.min(level.getMaxBuildHeight() - 2, base.getY() + 12);
        for (int y = maxY; y >= minY; y--) {
            BlockPos feet = new BlockPos(base.getX(), y, base.getZ());
            if (canStandAt(level, feet)) {
                return feet;
            }
        }
        return null;
    }

    private static boolean canStandAt(ServerLevel level, BlockPos feet) {
        return level.isInWorldBounds(feet)
                && level.isInWorldBounds(feet.above())
                && level.getBlockState(feet.below()).isSolidRender(level, feet.below())
                && level.getBlockState(feet).getCollisionShape(level, feet).isEmpty()
                && level.getBlockState(feet.above()).getCollisionShape(level, feet.above()).isEmpty()
                && level.getFluidState(feet).isEmpty()
                && level.getFluidState(feet.above()).isEmpty();
    }

    private static void sendJourneyMapWaypoint(ServerPlayer player, String id, CompoundTag target) {
        MagicWorldNetwork.sendJourneyMapWaypoint(
                player,
                id,
                target.getString("Label"),
                target.getString("Dimension"),
                target.getInt("X"),
                target.getInt("Y"),
                target.getInt("Z"),
                target.getInt("R"),
                target.getInt("G"),
                target.getInt("B")
        );
    }

    private static String readableId(String id) {
        return id.replace('_', ' ');
    }

    private static void sendInfo(ServerPlayer player, String message) {
        player.displayClientMessage(Component.literal("[Magic World] " + message).withStyle(ChatFormatting.AQUA), false);
    }

    private static void sendWarning(ServerPlayer player, String message) {
        player.displayClientMessage(Component.literal("[Magic World] " + message).withStyle(ChatFormatting.YELLOW), false);
    }

    private static void sendActionbar(ServerPlayer player, String message) {
        player.displayClientMessage(Component.literal("[Magic World] " + message).withStyle(ChatFormatting.AQUA), true);
    }
}
