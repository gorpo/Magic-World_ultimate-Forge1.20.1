package com.magicworld.client;

import com.magicworld.network.MagicWorldNetwork;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

public class PremiumPortalOptionsScreen extends Screen {
    private Checkbox resourcePack;
    private Checkbox shaderPack;
    private Checkbox completePack;

    public PremiumPortalOptionsScreen() {
        super(Component.literal("Portal Premium"));
    }

    @Override
    protected void init() {
        int panelWidth = Math.min(320, width - 32);
        int left = width / 2 - panelWidth / 2;
        int top = Math.max(30, height / 2 - 92);
        int checkboxX = left + 32;

        resourcePack = Checkbox.builder(Component.literal("ResourcePack"), font)
                .pos(checkboxX, top + 48)
                .selected(true)
                .maxWidth(panelWidth - 64)
                .build();

        shaderPack = Checkbox.builder(Component.literal("ShaderPack"), font)
                .pos(checkboxX, top + 74)
                .selected(false)
                .maxWidth(panelWidth - 64)
                .build();

        completePack = Checkbox.builder(Component.literal("Pacote completo"), font)
                .pos(checkboxX, top + 100)
                .selected(false)
                .maxWidth(panelWidth - 64)
                .build();

        addRenderableWidget(resourcePack);
        addRenderableWidget(shaderPack);
        addRenderableWidget(completePack);

        addRenderableWidget(Button.builder(
                        Component.literal("CONFIRMAR"),
                        button -> confirm()
                )
                .bounds(width / 2 - 74, top + 138, 148, 20)
                .build());
    }

    @Override
    public void extractRenderState(
            GuiGraphicsExtractor graphics,
            int mouseX,
            int mouseY,
            float partialTick
    ) {
        syncCompletePackSelection();

        int panelWidth = Math.min(320, width - 32);
        int panelHeight = 184;
        int left = width / 2 - panelWidth / 2;
        int top = Math.max(30, height / 2 - 92);

        graphics.fill(0, 0, width, height, 0xAA030611);
        graphics.fill(left, top, left + panelWidth, top + panelHeight, 0xEE0B1020);
        graphics.outline(left, top, panelWidth, panelHeight, 0xAA8C5CFF);
        graphics.outline(left + 4, top + 4, panelWidth - 8, panelHeight - 8, 0x553AD7FF);

        graphics.centeredText(font, Component.literal("ATIVAR MODO PREMIUM"), width / 2, top + 14, 0xFFFFFFFF);
        graphics.centeredText(font, Component.literal("Escolha o que carregar ao sair do Vanilla."), width / 2, top + 29, 0xFFBFD7FF);

        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
    }

    private void confirm() {
        boolean complete = completePack.selected();
        boolean resource = !complete && resourcePack.selected();
        boolean shader = !complete && shaderPack.selected();

        ClientPacketDistributor.sendToServer(
                new MagicWorldNetwork.ConfirmPremiumPortalOptionsPayload(resource, shader, complete)
        );

        onClose();
    }

    private void syncCompletePackSelection() {
        if (resourcePack == null || shaderPack == null || completePack == null) {
            return;
        }

        boolean complete = completePack.selected();
        resourcePack.active = !complete;
        shaderPack.active = !complete;

        if (complete) {
            if (resourcePack.selected()) {
                resourcePack.onPress(null);
            }
            if (shaderPack.selected()) {
                shaderPack.onPress(null);
            }
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
