package com.magicworld.event;

import com.magicworld.integration.MagicWorldLocationManager;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class MagicWorldTeleportGuard {
    private static final String ESTATE_CREATED_KEY = "MagicWorldForgeStarterEstateCreated";
    private static final int LOGIN_GUARD_TICKS = 20 * 30;
    private static final double EXTERNAL_TELEPORT_DISTANCE_SQR = 384.0D * 384.0D;
    private static final Map<UUID, LoginWatch> WATCHES = new HashMap<>();

    private record LoginWatch(ResourceKey<Level> dimension, Vec3 position, int ticksLeft) {
    }

    @SubscribeEvent
    public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || player.level().isClientSide()) {
            return;
        }

        if (player.getPersistentData().getBoolean(ESTATE_CREATED_KEY)) {
            startWatch(player);
            MagicWorldLocationManager.syncJourneyMapWaypoints(player);
            MagicWorldLocationManager.sendStartupHelp(player);
        }
    }

    public static void startWatch(ServerPlayer player) {
        WATCHES.put(player.getUUID(), new LoginWatch(player.level().dimension(), player.position(), LOGIN_GUARD_TICKS));
    }

    @SubscribeEvent
    public void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        WATCHES.remove(event.getEntity().getUUID());
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END
                || !(event.player instanceof ServerPlayer player)
                || player.level().isClientSide()) {
            return;
        }

        LoginWatch watch = WATCHES.get(player.getUUID());
        if (watch == null) {
            return;
        }

        if (watch.ticksLeft <= 0) {
            WATCHES.remove(player.getUUID());
            return;
        }

        if (MagicWorldLocationManager.hasAuthorizedTeleport(player)) {
            WATCHES.put(player.getUUID(), new LoginWatch(player.level().dimension(), player.position(), watch.ticksLeft - 1));
            return;
        }

        boolean changedDimension = !player.level().dimension().equals(watch.dimension);
        boolean movedTooFar = player.position().distanceToSqr(watch.position) > EXTERNAL_TELEPORT_DISTANCE_SQR;
        if (changedDimension || movedTooFar) {
            WATCHES.remove(player.getUUID());
            MagicWorldLocationManager.handleExternalTeleport(player);
            return;
        }

        WATCHES.put(player.getUUID(), new LoginWatch(watch.dimension, watch.position, watch.ticksLeft - 1));
    }
}
