package com.magicworld.network;

import com.magicworld.MagicWorld;
import com.magicworld.event.StarterPortalEvents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class MagicWorldNetwork {
    private MagicWorldNetwork() {
    }

    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(MagicWorldNetwork::registerPayloads);
    }

    private static void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(MagicWorld.MODID).versioned("1");
        registrar.playToClient(OpenPremiumPortalOptionsPayload.TYPE, OpenPremiumPortalOptionsPayload.STREAM_CODEC);
        registrar.playToClient(OpenInitialLoadNoticePayload.TYPE, OpenInitialLoadNoticePayload.STREAM_CODEC);
        registrar.playToClient(InitialLoadProgressPayload.TYPE, InitialLoadProgressPayload.STREAM_CODEC);
        registrar.playToClient(ApplyPremiumPortalVisualPayload.TYPE, ApplyPremiumPortalVisualPayload.STREAM_CODEC);
        registrar.playToServer(ConfirmPremiumPortalOptionsPayload.TYPE, ConfirmPremiumPortalOptionsPayload.STREAM_CODEC, MagicWorldNetwork::handleConfirmOptions);
    }

    private static void handleConfirmOptions(ConfirmPremiumPortalOptionsPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                StarterPortalEvents.confirmPremiumPortalOptions(
                        player,
                        payload.resourcePack(),
                        payload.shaderPack(),
                        payload.completePack()
                );
            }
        });
    }

    public record OpenPremiumPortalOptionsPayload() implements CustomPacketPayload {
        public static final Type<OpenPremiumPortalOptionsPayload> TYPE = new Type<>(
                Identifier.fromNamespaceAndPath(MagicWorld.MODID, "open_premium_portal_options")
        );
        public static final StreamCodec<RegistryFriendlyByteBuf, OpenPremiumPortalOptionsPayload> STREAM_CODEC =
                StreamCodec.unit(new OpenPremiumPortalOptionsPayload());

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record OpenInitialLoadNoticePayload() implements CustomPacketPayload {
        public static final Type<OpenInitialLoadNoticePayload> TYPE = new Type<>(
                Identifier.fromNamespaceAndPath(MagicWorld.MODID, "open_initial_load_notice")
        );
        public static final StreamCodec<RegistryFriendlyByteBuf, OpenInitialLoadNoticePayload> STREAM_CODEC =
                StreamCodec.unit(new OpenInitialLoadNoticePayload());

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record InitialLoadProgressPayload(
            int progress,
            String message,
            boolean complete
    ) implements CustomPacketPayload {
        public static final Type<InitialLoadProgressPayload> TYPE = new Type<>(
                Identifier.fromNamespaceAndPath(MagicWorld.MODID, "initial_load_progress")
        );
        public static final StreamCodec<RegistryFriendlyByteBuf, InitialLoadProgressPayload> STREAM_CODEC =
                StreamCodec.ofMember(InitialLoadProgressPayload::write, InitialLoadProgressPayload::read);

        private void write(RegistryFriendlyByteBuf buffer) {
            buffer.writeInt(progress);
            buffer.writeUtf(message);
            buffer.writeBoolean(complete);
        }

        private static InitialLoadProgressPayload read(RegistryFriendlyByteBuf buffer) {
            return new InitialLoadProgressPayload(
                    buffer.readInt(),
                    buffer.readUtf(),
                    buffer.readBoolean()
            );
        }

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record ConfirmPremiumPortalOptionsPayload(
            boolean resourcePack,
            boolean shaderPack,
            boolean completePack
    ) implements CustomPacketPayload {
        public static final Type<ConfirmPremiumPortalOptionsPayload> TYPE = new Type<>(
                Identifier.fromNamespaceAndPath(MagicWorld.MODID, "confirm_premium_portal_options")
        );
        public static final StreamCodec<RegistryFriendlyByteBuf, ConfirmPremiumPortalOptionsPayload> STREAM_CODEC =
                StreamCodec.ofMember(ConfirmPremiumPortalOptionsPayload::write, ConfirmPremiumPortalOptionsPayload::read);

        private void write(RegistryFriendlyByteBuf buffer) {
            buffer.writeBoolean(resourcePack);
            buffer.writeBoolean(shaderPack);
            buffer.writeBoolean(completePack);
        }

        private static ConfirmPremiumPortalOptionsPayload read(RegistryFriendlyByteBuf buffer) {
            return new ConfirmPremiumPortalOptionsPayload(
                    buffer.readBoolean(),
                    buffer.readBoolean(),
                    buffer.readBoolean()
            );
        }

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record ApplyPremiumPortalVisualPayload(
            boolean active,
            boolean resourcePack,
            boolean shaderPack
    ) implements CustomPacketPayload {
        public static final Type<ApplyPremiumPortalVisualPayload> TYPE = new Type<>(
                Identifier.fromNamespaceAndPath(MagicWorld.MODID, "apply_premium_portal_visual")
        );
        public static final StreamCodec<RegistryFriendlyByteBuf, ApplyPremiumPortalVisualPayload> STREAM_CODEC =
                StreamCodec.ofMember(ApplyPremiumPortalVisualPayload::write, ApplyPremiumPortalVisualPayload::read);

        private void write(RegistryFriendlyByteBuf buffer) {
            buffer.writeBoolean(active);
            buffer.writeBoolean(resourcePack);
            buffer.writeBoolean(shaderPack);
        }

        private static ApplyPremiumPortalVisualPayload read(RegistryFriendlyByteBuf buffer) {
            return new ApplyPremiumPortalVisualPayload(
                    buffer.readBoolean(),
                    buffer.readBoolean(),
                    buffer.readBoolean()
            );
        }

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }
}
