package com.example.examplemod.network;

import com.example.examplemod.ExampleMod;
import com.example.examplemod.client.InitialLoadNoticeScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
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
            new ResourceLocation(ExampleMod.MODID, "main"),
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
    }

    public static void openInitialLoadNotice(ServerPlayer player) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new OpenInitialLoadNoticePacket());
    }

    public static void sendInitialLoadProgress(ServerPlayer player, int progress, String message, boolean complete) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new InitialLoadProgressPacket(progress, message, complete));
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
}
