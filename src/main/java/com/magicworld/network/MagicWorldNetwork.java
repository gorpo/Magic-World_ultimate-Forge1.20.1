package com.magicworld.network;

import com.magicworld.MagicWorld;
import com.magicworld.client.MagicWorldJourneyMapWaypoints;
import com.magicworld.client.MagicWorldPortalVisualController;
import com.magicworld.client.InitialLoadNoticeScreen;
import com.magicworld.client.PremiumPortalOptionsScreen;
import com.magicworld.event.StarterPortalEvents;
import com.magicworld.integration.MagicWorldLocationManager;
import com.magicworld.integration.MagicWorldMineColoniesIntegration;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public final class MagicWorldNetwork {
    private static final String PROTOCOL = "1";
    private static int packetId;

    private static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(MagicWorld.MODID, "main"),
            () -> PROTOCOL,
            PROTOCOL::equals,
            PROTOCOL::equals
    );

    private MagicWorldNetwork() {
    }

    public static void register() {
        CHANNEL.registerMessage(
                packetId++,
                OpenInitialLoadNoticePacket.class,
                OpenInitialLoadNoticePacket::encode,
                OpenInitialLoadNoticePacket::decode,
                OpenInitialLoadNoticePacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT)
        );

        CHANNEL.registerMessage(
                packetId++,
                InitialLoadProgressPacket.class,
                InitialLoadProgressPacket::encode,
                InitialLoadProgressPacket::decode,
                InitialLoadProgressPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT)
        );

        CHANNEL.registerMessage(
                packetId++,
                OpenPremiumPortalOptionsPacket.class,
                OpenPremiumPortalOptionsPacket::encode,
                OpenPremiumPortalOptionsPacket::decode,
                OpenPremiumPortalOptionsPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT)
        );

        CHANNEL.registerMessage(
                packetId++,
                ApplyPremiumPortalVisualPacket.class,
                ApplyPremiumPortalVisualPacket::encode,
                ApplyPremiumPortalVisualPacket::decode,
                ApplyPremiumPortalVisualPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT)
        );

        CHANNEL.registerMessage(
                packetId++,
                ConfirmPremiumPortalOptionsPacket.class,
                ConfirmPremiumPortalOptionsPacket::encode,
                ConfirmPremiumPortalOptionsPacket::decode,
                ConfirmPremiumPortalOptionsPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_SERVER)
        );

        CHANNEL.registerMessage(
                packetId++,
                MagicWorldPanelActionPacket.class,
                MagicWorldPanelActionPacket::encode,
                MagicWorldPanelActionPacket::decode,
                MagicWorldPanelActionPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_SERVER)
        );

        CHANNEL.registerMessage(
                packetId++,
                MagicWorldWaypointPacket.class,
                MagicWorldWaypointPacket::encode,
                MagicWorldWaypointPacket::decode,
                MagicWorldWaypointPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT)
        );
    }

    public static void openInitialLoadNotice(ServerPlayer player) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new OpenInitialLoadNoticePacket());
    }

    public static void sendInitialLoadProgress(ServerPlayer player, int progress, String message, boolean complete) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new InitialLoadProgressPacket(progress, message, complete));
    }

    public static void openPremiumPortalOptions(ServerPlayer player) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new OpenPremiumPortalOptionsPacket());
    }

    public static void applyPremiumPortalVisual(
            ServerPlayer player,
            boolean active,
            boolean resourcePack,
            boolean shaderPack,
            List<String> resourcePacks,
            String shaderPackName
    ) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new ApplyPremiumPortalVisualPacket(
                active,
                resourcePack,
                shaderPack,
                resourcePacks,
                shaderPackName
        ));
    }

    public static void confirmPremiumPortalOptions(
            boolean resourcePack,
            boolean shaderPack,
            boolean completePack,
            List<String> resourcePacks,
            String shaderPackName
    ) {
        CHANNEL.sendToServer(new ConfirmPremiumPortalOptionsPacket(
                resourcePack,
                shaderPack,
                completePack,
                resourcePacks,
                shaderPackName
        ));
    }

    public static void sendPanelAction(String action) {
        CHANNEL.sendToServer(new MagicWorldPanelActionPacket(action));
    }

    public static void sendJourneyMapWaypoint(
            ServerPlayer player,
            String id,
            String label,
            String dimension,
            int x,
            int y,
            int z,
            int red,
            int green,
            int blue
    ) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new MagicWorldWaypointPacket(
                id,
                label,
                dimension,
                x,
                y,
                z,
                red,
                green,
                blue
        ));
    }

    public static final class OpenInitialLoadNoticePacket {
        public static void encode(OpenInitialLoadNoticePacket packet, FriendlyByteBuf buffer) {
        }

        public static OpenInitialLoadNoticePacket decode(FriendlyByteBuf buffer) {
            return new OpenInitialLoadNoticePacket();
        }

        public static void handle(OpenInitialLoadNoticePacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
            NetworkEvent.Context context = contextSupplier.get();
            context.enqueueWork(() -> {
                Minecraft minecraft = Minecraft.getInstance();
                InitialLoadNoticeScreen.resetProgress();
                minecraft.setScreen(new InitialLoadNoticeScreen());
            });
            context.setPacketHandled(true);
        }
    }

    public record InitialLoadProgressPacket(int progress, String message, boolean complete) {
        public static void encode(InitialLoadProgressPacket packet, FriendlyByteBuf buffer) {
            buffer.writeInt(packet.progress);
            buffer.writeUtf(packet.message);
            buffer.writeBoolean(packet.complete);
        }

        public static InitialLoadProgressPacket decode(FriendlyByteBuf buffer) {
            return new InitialLoadProgressPacket(
                    buffer.readInt(),
                    buffer.readUtf(),
                    buffer.readBoolean()
            );
        }

        public static void handle(InitialLoadProgressPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
            NetworkEvent.Context context = contextSupplier.get();
            context.enqueueWork(() -> InitialLoadNoticeScreen.updateProgress(
                    Minecraft.getInstance(),
                    packet.progress,
                    packet.message,
                    packet.complete
            ));
            context.setPacketHandled(true);
        }
    }

    public static final class OpenPremiumPortalOptionsPacket {
        public static void encode(OpenPremiumPortalOptionsPacket packet, FriendlyByteBuf buffer) {
        }

        public static OpenPremiumPortalOptionsPacket decode(FriendlyByteBuf buffer) {
            return new OpenPremiumPortalOptionsPacket();
        }

        public static void handle(OpenPremiumPortalOptionsPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
            NetworkEvent.Context context = contextSupplier.get();
            context.enqueueWork(() -> {
                Minecraft minecraft = Minecraft.getInstance();
                if (minecraft.player != null && minecraft.level != null) {
                    minecraft.setScreen(new PremiumPortalOptionsScreen());
                }
            });
            context.setPacketHandled(true);
        }
    }

    public record ApplyPremiumPortalVisualPacket(
            boolean active,
            boolean resourcePack,
            boolean shaderPack,
            List<String> resourcePacks,
            String shaderPackName
    ) {
        public static void encode(ApplyPremiumPortalVisualPacket packet, FriendlyByteBuf buffer) {
            buffer.writeBoolean(packet.active);
            buffer.writeBoolean(packet.resourcePack);
            buffer.writeBoolean(packet.shaderPack);
            writeStringList(buffer, packet.resourcePacks);
            buffer.writeUtf(packet.shaderPackName == null ? "" : packet.shaderPackName);
        }

        public static ApplyPremiumPortalVisualPacket decode(FriendlyByteBuf buffer) {
            return new ApplyPremiumPortalVisualPacket(
                    buffer.readBoolean(),
                    buffer.readBoolean(),
                    buffer.readBoolean(),
                    readStringList(buffer),
                    buffer.readUtf()
            );
        }

        public static void handle(ApplyPremiumPortalVisualPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
            NetworkEvent.Context context = contextSupplier.get();
            context.enqueueWork(() -> MagicWorldPortalVisualController.applyPortalSelection(
                    packet.active && packet.resourcePack,
                    packet.active && packet.shaderPack,
                    packet.resourcePacks,
                    packet.shaderPackName
            ));
            context.setPacketHandled(true);
        }
    }

    public record ConfirmPremiumPortalOptionsPacket(
            boolean resourcePack,
            boolean shaderPack,
            boolean completePack,
            List<String> resourcePacks,
            String shaderPackName
    ) {
        public static void encode(ConfirmPremiumPortalOptionsPacket packet, FriendlyByteBuf buffer) {
            buffer.writeBoolean(packet.resourcePack);
            buffer.writeBoolean(packet.shaderPack);
            buffer.writeBoolean(packet.completePack);
            writeStringList(buffer, packet.resourcePacks);
            buffer.writeUtf(packet.shaderPackName == null ? "" : packet.shaderPackName);
        }

        public static ConfirmPremiumPortalOptionsPacket decode(FriendlyByteBuf buffer) {
            return new ConfirmPremiumPortalOptionsPacket(
                    buffer.readBoolean(),
                    buffer.readBoolean(),
                    buffer.readBoolean(),
                    readStringList(buffer),
                    buffer.readUtf()
            );
        }

        public static void handle(ConfirmPremiumPortalOptionsPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
            NetworkEvent.Context context = contextSupplier.get();
            context.enqueueWork(() -> {
                ServerPlayer player = context.getSender();
                if (player != null) {
                    StarterPortalEvents.confirmPremiumPortalOptions(
                            player,
                            packet.resourcePack,
                            packet.shaderPack,
                            packet.completePack,
                            packet.resourcePacks,
                            packet.shaderPackName
                    );
                }
            });
            context.setPacketHandled(true);
        }
    }

    private static void writeStringList(FriendlyByteBuf buffer, List<String> values) {
        List<String> safeValues = values == null ? List.of() : values;
        buffer.writeInt(safeValues.size());
        for (String value : safeValues) {
            buffer.writeUtf(value == null ? "" : value);
        }
    }

    private static List<String> readStringList(FriendlyByteBuf buffer) {
        int size = Math.max(0, Math.min(64, buffer.readInt()));
        List<String> values = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            values.add(buffer.readUtf());
        }
        return values;
    }

    public record MagicWorldPanelActionPacket(String action) {
        public static void encode(MagicWorldPanelActionPacket packet, FriendlyByteBuf buffer) {
            buffer.writeUtf(packet.action);
        }

        public static MagicWorldPanelActionPacket decode(FriendlyByteBuf buffer) {
            return new MagicWorldPanelActionPacket(buffer.readUtf());
        }

        public static void handle(MagicWorldPanelActionPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
            NetworkEvent.Context context = contextSupplier.get();
            context.enqueueWork(() -> {
                ServerPlayer player = context.getSender();
                if (player != null) {
                    handlePanelAction(player, packet.action);
                }
            });
            context.setPacketHandled(true);
        }
    }

    public record MagicWorldWaypointPacket(
            String id,
            String label,
            String dimension,
            int x,
            int y,
            int z,
            int red,
            int green,
            int blue
    ) {
        public static void encode(MagicWorldWaypointPacket packet, FriendlyByteBuf buffer) {
            buffer.writeUtf(packet.id);
            buffer.writeUtf(packet.label);
            buffer.writeUtf(packet.dimension);
            buffer.writeInt(packet.x);
            buffer.writeInt(packet.y);
            buffer.writeInt(packet.z);
            buffer.writeInt(packet.red);
            buffer.writeInt(packet.green);
            buffer.writeInt(packet.blue);
        }

        public static MagicWorldWaypointPacket decode(FriendlyByteBuf buffer) {
            return new MagicWorldWaypointPacket(
                    buffer.readUtf(),
                    buffer.readUtf(),
                    buffer.readUtf(),
                    buffer.readInt(),
                    buffer.readInt(),
                    buffer.readInt(),
                    buffer.readInt(),
                    buffer.readInt(),
                    buffer.readInt()
            );
        }

        public static void handle(MagicWorldWaypointPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
            NetworkEvent.Context context = contextSupplier.get();
            context.enqueueWork(() -> MagicWorldJourneyMapWaypoints.writeWaypoint(
                    packet.id,
                    packet.label,
                    packet.dimension,
                    packet.x,
                    packet.y,
                    packet.z,
                    packet.red,
                    packet.green,
                    packet.blue
            ));
            context.setPacketHandled(true);
        }
    }

    private static void handlePanelAction(ServerPlayer player, String action) {
        ServerLevel level = player.serverLevel();
        if (action.startsWith("secret_give:")) {
            giveSecretItem(player, action.substring("secret_give:".length()));
            return;
        }

        if (MagicWorldLocationManager.handleAction(player, action)) {
            return;
        }

        if (MagicWorldMineColoniesIntegration.handleAction(player, action)) {
            return;
        }

        switch (action) {
            case "magic_wand" -> player.getInventory().add(new ItemStack(MagicWorld.VARINHA_MAGICA.get()));
            case "teleport_home" -> teleportHome(player);
            case "time_day" -> level.setDayTime(1000L);
            case "time_night" -> level.setDayTime(13000L);
            case "weather_clear" -> level.setWeatherParameters(6000, 0, false, false);
            case "weather_rain" -> level.setWeatherParameters(0, 6000, true, false);
            case "secret_god" -> applyGodMode(player);
            case "secret_protection" -> applyProtection(player);
            case "secret_speed" -> applySpeed(player);
            case "secret_clear_effects" -> player.removeAllEffects();
            case "secret_gamemode_creative" -> runSilentCommand(player, "gamemode creative @s");
            case "secret_gamemode_survival" -> runSilentCommand(player, "gamemode survival @s");
            case "secret_keep_inventory_on" -> level.getGameRules().getRule(GameRules.RULE_KEEPINVENTORY).set(true, player.server);
            case "secret_keep_inventory_off" -> level.getGameRules().getRule(GameRules.RULE_KEEPINVENTORY).set(false, player.server);
            case "secret_tick_normal" -> level.getGameRules().getRule(GameRules.RULE_RANDOMTICKING).set(3, player.server);
            case "secret_tick_fast" -> level.getGameRules().getRule(GameRules.RULE_RANDOMTICKING).set(40, player.server);
            case "secret_tick_turbo" -> level.getGameRules().getRule(GameRules.RULE_RANDOMTICKING).set(120, player.server);
            case "secret_tick_slow" -> level.getGameRules().getRule(GameRules.RULE_RANDOMTICKING).set(1, player.server);
            case "secret_spawn_here" -> runSilentCommand(player, "spawnpoint @s");
            case "secret_spawn_home" -> setSpawnHome(player);
            case "secret_tp_up" -> teleportUp(player);
            case "secret_kit_nether" -> giveNetherKit(player);
            case "secret_kit_end" -> giveEndKit(player);
            default -> {
            }
        }
    }

    private static void giveSecretItem(ServerPlayer player, String itemId) {
        ResourceLocation id = ResourceLocation.tryParse(itemId);
        if (id == null) {
            return;
        }
        Item item = BuiltInRegistries.ITEM.get(id);
        if (item == Items.AIR) {
            return;
        }
        ItemStack stack = new ItemStack(item);
        stack.setCount(Math.max(1, stack.getMaxStackSize()));
        player.addItem(stack);
    }

    private static void applyGodMode(ServerPlayer player) {
        applyProtection(player);
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, -1, 4, false, false, false));
        player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, -1, 2, false, false, false));
        player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, -1, 4, false, false, false));
    }

    private static void applyProtection(ServerPlayer player) {
        player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, -1, 1, false, false, false));
        player.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, -1, 1, false, false, false));
        player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, -1, 1, false, false, false));
        player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, -1, 1, false, false, false));
    }

    private static void applySpeed(ServerPlayer player) {
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, -1, 2, false, false, false));
        player.addEffect(new MobEffectInstance(MobEffects.JUMP, -1, 2, false, false, false));
        player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, -1, 3, false, false, false));
        player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, -1, 1, false, false, false));
    }

    private static void setSpawnHome(ServerPlayer player) {
        BlockPos base = getEstateBase(player);
        if (base == null) {
            return;
        }
        runSilentCommand(player, "spawnpoint @s " + base.getX() + " " + (base.getY() + 1) + " " + base.getZ());
    }

    private static void teleportUp(ServerPlayer player) {
        double targetY = Math.min(player.level().getMaxBuildHeight() - 2.0D, player.getY() + 64.0D);
        player.teleportTo((ServerLevel) player.level(), player.getX(), targetY, player.getZ(), player.getYRot(), player.getXRot());
    }

    private static void giveNetherKit(ServerPlayer player) {
        player.addItem(new ItemStack(Items.OBSIDIAN, 64));
        player.addItem(new ItemStack(Items.FLINT_AND_STEEL, 1));
        player.addItem(new ItemStack(Items.FIRE_CHARGE, 16));
    }

    private static void giveEndKit(ServerPlayer player) {
        player.addItem(new ItemStack(Items.END_PORTAL_FRAME, 12));
        player.addItem(new ItemStack(Items.ENDER_EYE, 16));
        player.addItem(new ItemStack(Items.END_STONE, 64));
    }

    private static void teleportHome(ServerPlayer player) {
        BlockPos base = getEstateBase(player);
        if (base == null) {
            return;
        }
        ServerLevel overworld = player.server.getLevel(Level.OVERWORLD);
        if (overworld == null) {
            return;
        }
        player.teleportTo(overworld, base.getX() + 0.5D, base.getY() + 1.0D, base.getZ() + 0.5D, player.getYRot(), player.getXRot());
    }

    private static BlockPos getEstateBase(ServerPlayer player) {
        if (!player.getPersistentData().contains("MagicWorldForgeStarterEstateBaseX")) {
            return null;
        }
        return new BlockPos(
                player.getPersistentData().getInt("MagicWorldForgeStarterEstateBaseX"),
                player.getPersistentData().getInt("MagicWorldForgeStarterEstateBaseY"),
                player.getPersistentData().getInt("MagicWorldForgeStarterEstateBaseZ")
        );
    }

    private static void runSilentCommand(ServerPlayer player, String command) {
        if (player.server == null) {
            return;
        }
        player.server.getCommands().performPrefixedCommand(player.createCommandSourceStack().withSuppressedOutput(), command);
    }
}
