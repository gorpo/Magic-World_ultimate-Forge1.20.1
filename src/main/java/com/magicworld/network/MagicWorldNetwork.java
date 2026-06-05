package com.magicworld.network;

import com.magicworld.MagicWorld;
import com.magicworld.client.InitialLoadNoticeScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

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
                MagicWorldPanelActionPacket.class,
                MagicWorldPanelActionPacket::encode,
                MagicWorldPanelActionPacket::decode,
                MagicWorldPanelActionPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_SERVER)
        );
    }

    public static void openInitialLoadNotice(ServerPlayer player) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new OpenInitialLoadNoticePacket());
    }

    public static void sendInitialLoadProgress(ServerPlayer player, int progress, String message, boolean complete) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new InitialLoadProgressPacket(progress, message, complete));
    }

    public static void sendPanelAction(String action) {
        CHANNEL.sendToServer(new MagicWorldPanelActionPacket(action));
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

    private static void handlePanelAction(ServerPlayer player, String action) {
        ServerLevel level = player.serverLevel();
        switch (action) {
            case "magic_wand" -> player.getInventory().add(new ItemStack(MagicWorld.VARINHA_MAGICA.get()));
            case "teleport_home" -> teleportHome(player);
            case "time_day" -> level.setDayTime(1000L);
            case "time_night" -> level.setDayTime(13000L);
            case "weather_clear" -> level.setWeatherParameters(6000, 0, false, false);
            case "weather_rain" -> level.setWeatherParameters(0, 6000, true, false);
            default -> {
            }
        }
    }

    private static void teleportHome(ServerPlayer player) {
        if (!player.getPersistentData().contains("MagicWorldForgeStarterEstateBaseX")) {
            return;
        }
        ServerLevel overworld = player.server.getLevel(Level.OVERWORLD);
        if (overworld == null) {
            return;
        }
        int x = player.getPersistentData().getInt("MagicWorldForgeStarterEstateBaseX");
        int y = player.getPersistentData().getInt("MagicWorldForgeStarterEstateBaseY");
        int z = player.getPersistentData().getInt("MagicWorldForgeStarterEstateBaseZ");
        player.teleportTo(overworld, x + 0.5D, y + 1.0D, z + 0.5D, player.getYRot(), player.getXRot());
    }
}
