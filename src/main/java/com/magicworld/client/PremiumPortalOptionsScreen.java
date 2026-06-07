package com.magicworld.client;

import com.magicworld.network.MagicWorldNetwork;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class PremiumPortalOptionsScreen extends Screen {
    public PremiumPortalOptionsScreen() {
        super(Component.literal("Portal Premium"));
    }

    @Override
    protected void init() {
        int panelWidth = Math.min(340, width - 32);
        int left = width / 2 - panelWidth / 2;
        int top = Math.max(30, height / 2 - 100);
        int buttonWidth = panelWidth - 64;
        int buttonLeft = left + 32;

        addRenderableWidget(Button.builder(
                        Component.literal("ResourcePack"),
                        button -> confirm(true, false, false)
                )
                .bounds(buttonLeft, top + 54, buttonWidth, 22)
                .build());

        addRenderableWidget(Button.builder(
                        Component.literal("ShaderPack"),
                        button -> confirm(false, true, false)
                )
                .bounds(buttonLeft, top + 84, buttonWidth, 22)
                .build());

        addRenderableWidget(Button.builder(
                        Component.literal("Shader + Resource"),
                        button -> confirm(false, false, true)
                )
                .bounds(buttonLeft, top + 114, buttonWidth, 22)
                .build());

        addRenderableWidget(Button.builder(
                        Component.literal("Cancelar"),
                        button -> onClose()
                )
                .bounds(buttonLeft, top + 154, buttonWidth, 20)
                .build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);

        int panelWidth = Math.min(340, width - 32);
        int panelHeight = 200;
        int left = width / 2 - panelWidth / 2;
        int top = Math.max(30, height / 2 - 100);

        graphics.fill(0, 0, width, height, 0xAA030611);
        graphics.fill(left, top, left + panelWidth, top + panelHeight, 0xEE0B1020);
        graphics.renderOutline(left, top, panelWidth, panelHeight, 0xCC8C5CFF);
        graphics.renderOutline(left + 4, top + 4, panelWidth - 8, panelHeight - 8, 0x773AD7FF);

        graphics.drawCenteredString(font, Component.literal("ATIVAR MODO PREMIUM"), width / 2, top + 14, 0xFFFFFFFF);
        graphics.drawCenteredString(font, Component.literal("Escolha o que carregar no Magic World."), width / 2, top + 31, 0xFFBFD7FF);

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    private void confirm(boolean resourcePack, boolean shaderPack, boolean completePack) {
        MagicWorldNetwork.confirmPremiumPortalOptions(resourcePack, shaderPack, completePack);
        onClose();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
