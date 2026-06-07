package com.magicworld.client;

import com.magicworld.network.MagicWorldNetwork;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class MagicWorldCentralPauseScreen extends Screen {
    private final Screen parent;

    public MagicWorldCentralPauseScreen(Screen parent) {
        super(Component.literal("Central Magic World"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int panelWidth = Math.min(430, width - 28);
        int left = width / 2 - panelWidth / 2;
        int panelHeight = Math.min(390, height - 18);
        int top = Math.max(8, height / 2 - panelHeight / 2);
        int buttonWidth = (panelWidth - 36 - 8) / 2;
        int y = top + 74;

        addRenderableWidget(new MagicWorldMenuButton(left + 12, y, panelWidth - 24, 20,
                Component.literal("Menu secreto do Minecraft"),
                () -> Minecraft.getInstance().setScreen(new MagicWorldSecretMinecraftScreen(this))));
        y += 28;

        addRenderableWidget(new MagicWorldMenuButton(left + 12, y, panelWidth - 24, 20,
                Component.literal("Abrir menu completo da varinha"),
                () -> Minecraft.getInstance().setScreen(new PremiumMenuScreen())));
        y += 28;

        addRenderableWidget(new MagicWorldMenuButton(left + 12, y, panelWidth - 24, 20,
                Component.literal("Locais Magic World"),
                () -> Minecraft.getInstance().setScreen(new PremiumMenuScreen(PremiumMenuScreen.MenuTab.LOCATIONS))));
        y += 28;

        addAction(left + 12, y, buttonWidth, "Receber varinha", "magic_wand", true);
        addAction(left + 20 + buttonWidth, y, buttonWidth, "Minha casa", "location_teleport_home", true);
        y += 28;

        addAction(left + 12, y, buttonWidth, "Santuario", "location_teleport_sanctuary", true);
        addAction(left + 20 + buttonWidth, y, buttonWidth, "Praca portais", "location_teleport_portal_plaza", true);
        y += 28;

        addAction(left + 12, y, buttonWidth, "Ultimo externo", "location_teleport_external", true);
        addAction(left + 20 + buttonWidth, y, buttonWidth, "Waypoints", "location_update_waypoints", false);
        y += 28;

        addAction(left + 12, y, buttonWidth, "Dia", "time_day", true);
        addAction(left + 20 + buttonWidth, y, buttonWidth, "Noite", "time_night", true);
        y += 28;

        addAction(left + 12, y, buttonWidth, "Sol", "weather_clear", true);
        addAction(left + 20 + buttonWidth, y, buttonWidth, "Chuva", "weather_rain", true);
        y += 34;

        addRenderableWidget(new MagicWorldMenuButton(left + 12, y, buttonWidth, 20,
                Component.literal("Voltar"),
                () -> Minecraft.getInstance().setScreen(parent)));
        addRenderableWidget(new MagicWorldMenuButton(left + 20 + buttonWidth, y, buttonWidth, 20,
                Component.literal("Continuar"),
                () -> Minecraft.getInstance().setScreen(null)));
    }

    private void addAction(int x, int y, int width, String label, String action, boolean closeAfterClick) {
        addRenderableWidget(new MagicWorldMenuButton(x, y, width, 20, Component.literal(label), () -> {
            MagicWorldNetwork.sendPanelAction(action);
            if (closeAfterClick) {
                Minecraft.getInstance().setScreen(null);
            }
        }));
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        int panelWidth = Math.min(430, width - 28);
        int panelHeight = Math.min(390, height - 18);
        int left = width / 2 - panelWidth / 2;
        int top = Math.max(8, height / 2 - panelHeight / 2);
        int logoWidth = Math.min(120, Math.max(92, panelWidth / 3));
        int logoHeight = logoWidth * MagicWorldStaticBackground.LOGO_HEIGHT / MagicWorldStaticBackground.LOGO_WIDTH;

        MagicWorldStaticBackground.draw(graphics, width, height);
        graphics.fill(0, 0, width, height, 0x77030611);
        graphics.fill(left, top, left + panelWidth, top + panelHeight, 0xE8050916);
        graphics.renderOutline(left, top, panelWidth, panelHeight, MagicWorldMenuTheme.GOLD);
        graphics.renderOutline(left + 5, top + 5, panelWidth - 10, panelHeight - 10, 0x66316B9F);
        graphics.blit(
                MagicWorldStaticBackground.FULL_LOGO,
                width / 2 - logoWidth / 2,
                top + 10,
                logoWidth,
                logoHeight,
                0,
                0,
                MagicWorldStaticBackground.LOGO_WIDTH,
                MagicWorldStaticBackground.LOGO_HEIGHT,
                MagicWorldStaticBackground.LOGO_WIDTH,
                MagicWorldStaticBackground.LOGO_HEIGHT
        );
        graphics.drawCenteredString(font, title, width / 2, top + 48, 0xFFFFFFFF);
        graphics.drawCenteredString(font, Component.literal("Atalhos rapidos, locais seguros e menu secreto"), width / 2, top + 64, 0xFFFFE0A3);
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
