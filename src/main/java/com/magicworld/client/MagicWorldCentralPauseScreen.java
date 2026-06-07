package com.magicworld.client;

import com.magicworld.network.MagicWorldNetwork;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class MagicWorldCentralPauseScreen extends Screen {
    private static final int ROWS = 8;

    private final Screen parent;

    public MagicWorldCentralPauseScreen(Screen parent) {
        super(Component.literal("Central Magic World"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        Layout layout = layout();
        int y = layout.firstRowY();

        addRenderableWidget(new MagicWorldMenuButton(layout.leftButtonX(), y, layout.buttonWidth(), layout.buttonHeight(),
                Component.literal("Menu secreto"),
                () -> Minecraft.getInstance().setScreen(new MagicWorldSecretMinecraftScreen(this))));
        addRenderableWidget(new MagicWorldMenuButton(layout.rightButtonX(), y, layout.buttonWidth(), layout.buttonHeight(),
                Component.literal("Menu varinha"),
                () -> Minecraft.getInstance().setScreen(new PremiumMenuScreen())));
        y += layout.rowGap();

        addRenderableWidget(new MagicWorldMenuButton(layout.leftButtonX(), y, layout.buttonWidth(), layout.buttonHeight(),
                Component.literal("Locais Magic"),
                () -> Minecraft.getInstance().setScreen(new MagicWorldPremiumLocationsScreen(this))));
        addAction(layout.rightButtonX(), y, layout.buttonWidth(), layout.buttonHeight(), "Receber varinha", "magic_wand", true);
        y += layout.rowGap();

        addAction(layout.leftButtonX(), y, layout.buttonWidth(), layout.buttonHeight(), "Minha casa", "location_teleport_home", true);
        addAction(layout.rightButtonX(), y, layout.buttonWidth(), layout.buttonHeight(), "Santuario", "location_teleport_sanctuary", true);
        y += layout.rowGap();

        addAction(layout.leftButtonX(), y, layout.buttonWidth(), layout.buttonHeight(), "Praca portais", "location_teleport_portal_plaza", true);
        addAction(layout.rightButtonX(), y, layout.buttonWidth(), layout.buttonHeight(), "Ultimo externo", "location_teleport_external", true);
        y += layout.rowGap();

        addAction(layout.leftButtonX(), y, layout.buttonWidth(), layout.buttonHeight(), "Waypoints", "location_update_waypoints", false);
        addAction(layout.rightButtonX(), y, layout.buttonWidth(), layout.buttonHeight(), "Dia", "time_day", true);
        y += layout.rowGap();

        addAction(layout.leftButtonX(), y, layout.buttonWidth(), layout.buttonHeight(), "Noite", "time_night", true);
        addAction(layout.rightButtonX(), y, layout.buttonWidth(), layout.buttonHeight(), "Sol", "weather_clear", true);
        y += layout.rowGap();

        addAction(layout.leftButtonX(), y, layout.buttonWidth(), layout.buttonHeight(), "Chuva", "weather_rain", true);
        addAction(layout.rightButtonX(), y, layout.buttonWidth(), layout.buttonHeight(), "Castelo", "location_teleport_castle", true);
        y += layout.rowGap();

        addRenderableWidget(new MagicWorldMenuButton(layout.leftButtonX(), y, layout.buttonWidth(), layout.buttonHeight(),
                Component.literal("Voltar"),
                () -> Minecraft.getInstance().setScreen(parent)));
        addRenderableWidget(new MagicWorldMenuButton(layout.rightButtonX(), y, layout.buttonWidth(), layout.buttonHeight(),
                Component.literal("Continuar"),
                () -> Minecraft.getInstance().setScreen(null)));
    }

    private void addAction(int x, int y, int width, int height, String label, String action, boolean closeAfterClick) {
        addRenderableWidget(new MagicWorldMenuButton(x, y, width, height, Component.literal(label), () -> {
            MagicWorldNetwork.sendPanelAction(action);
            if (closeAfterClick) {
                Minecraft.getInstance().setScreen(null);
            }
        }));
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        Layout layout = layout();
        int logoWidth = Math.min(96, Math.max(74, layout.panelWidth() / 5));
        int logoHeight = logoWidth * MagicWorldStaticBackground.LOGO_HEIGHT / MagicWorldStaticBackground.LOGO_WIDTH;

        MagicWorldStaticBackground.draw(graphics, width, height);
        graphics.fill(0, 0, width, height, 0x77030611);
        graphics.fill(layout.panelX(), layout.panelY(), layout.panelX() + layout.panelWidth(), layout.panelY() + layout.panelHeight(), 0xE8050916);
        graphics.renderOutline(layout.panelX(), layout.panelY(), layout.panelWidth(), layout.panelHeight(), MagicWorldMenuTheme.GOLD);
        graphics.renderOutline(layout.panelX() + 5, layout.panelY() + 5, layout.panelWidth() - 10, layout.panelHeight() - 10, 0x66316B9F);
        graphics.blit(
                MagicWorldStaticBackground.FULL_LOGO,
                width / 2 - logoWidth / 2,
                layout.panelY() + 7,
                logoWidth,
                logoHeight,
                0,
                0,
                MagicWorldStaticBackground.LOGO_WIDTH,
                MagicWorldStaticBackground.LOGO_HEIGHT,
                MagicWorldStaticBackground.LOGO_WIDTH,
                MagicWorldStaticBackground.LOGO_HEIGHT
        );
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private Layout layout() {
        int panelWidth = Math.min(430, width - 18);
        int panelHeight = Math.min(390, height - 12);
        int panelX = width / 2 - panelWidth / 2;
        int panelY = Math.max(6, height / 2 - panelHeight / 2);
        int columnGap = 8;
        int sideMargin = 12;
        int buttonWidth = (panelWidth - sideMargin * 2 - columnGap) / 2;
        int headerHeight = Math.min(44, Math.max(34, panelHeight / 7));
        int bottomMargin = 8;
        int availableRowsHeight = Math.max(ROWS * 12, panelHeight - headerHeight - bottomMargin);
        int buttonHeight = Math.min(18, Math.max(12, availableRowsHeight / ROWS - 2));
        int rowGap = Math.min(22, Math.max(buttonHeight, (availableRowsHeight - buttonHeight) / Math.max(1, ROWS - 1)));
        return new Layout(panelX, panelY, panelWidth, panelHeight, sideMargin, columnGap, buttonWidth, buttonHeight, headerHeight, rowGap);
    }

    private record Layout(
            int panelX,
            int panelY,
            int panelWidth,
            int panelHeight,
            int sideMargin,
            int columnGap,
            int buttonWidth,
            int buttonHeight,
            int headerHeight,
            int rowGap
    ) {
        int firstRowY() {
            return panelY + headerHeight;
        }

        int leftButtonX() {
            return panelX + sideMargin;
        }

        int rightButtonX() {
            return leftButtonX() + buttonWidth + columnGap;
        }
    }
}
