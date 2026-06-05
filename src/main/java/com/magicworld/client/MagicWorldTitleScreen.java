package com.magicworld.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.OptionsScreen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.client.gui.ModListScreen;

public class MagicWorldTitleScreen extends Screen {
    private static final int BUTTON_WIDTH = 148;
    private static final int BUTTON_HEIGHT = 20;
    private static final int BUTTON_GAP = 6;
    private static final int MENU_WIDTH = 300;

    public MagicWorldTitleScreen() {
        super(Component.literal("Magic World"));
    }

    @Override
    protected void init() {
        int x = getMenuX() + (MENU_WIDTH - BUTTON_WIDTH) / 2;
        int y = getButtonY();

        addRenderableWidget(new MagicWorldMenuButton(
                x,
                y,
                BUTTON_WIDTH,
                BUTTON_HEIGHT,
                Component.literal("JOGAR"),
                () -> minecraft.setScreen(new SelectWorldScreen(this))
        ));

        addRenderableWidget(new MagicWorldMenuButton(
                x,
                y + step(1),
                BUTTON_WIDTH,
                BUTTON_HEIGHT,
                Component.literal("MULTIPLAYER"),
                () -> minecraft.setScreen(new JoinMultiplayerScreen(this))
        ));

        addRenderableWidget(new MagicWorldMenuButton(
                x,
                y + step(2),
                BUTTON_WIDTH,
                BUTTON_HEIGHT,
                Component.literal("MODS"),
                () -> minecraft.setScreen(new ModListScreen(this))
        ));

        addRenderableWidget(new MagicWorldMenuButton(
                x,
                y + step(3),
                BUTTON_WIDTH,
                BUTTON_HEIGHT,
                Component.literal("OPCOES"),
                () -> minecraft.setScreen(new OptionsScreen(this, minecraft.options))
        ));

        addRenderableWidget(new MagicWorldMenuButton(
                x,
                y + step(4),
                BUTTON_WIDTH,
                BUTTON_HEIGHT,
                Component.literal("SAIR"),
                () -> minecraft.stop()
        ));
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        MagicWorldStaticBackground.draw(graphics, width, height);
        drawLogo(graphics);
        drawVersionText(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void renderBackground(GuiGraphics graphics) {
        MagicWorldStaticBackground.draw(graphics, width, height);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private static int step(int index) {
        return index * (BUTTON_HEIGHT + BUTTON_GAP);
    }

    private int getMenuX() {
        return Math.max(18, (width - MENU_WIDTH) / 2);
    }

    private int getLogoY() {
        return Math.max(12, Math.round(height * 0.08F));
    }

    private int getButtonY() {
        int logoHeight = getLogoWidth() * MagicWorldStaticBackground.LOGO_HEIGHT / MagicWorldStaticBackground.LOGO_WIDTH;
        int preferred = getLogoY() + logoHeight + 16;
        int max = height - step(5) - BUTTON_HEIGHT - 38;
        return Math.max(84, Math.min(preferred, max));
    }

    private int getLogoWidth() {
        return Math.max(180, Math.min(300, width / 5));
    }

    private void drawLogo(GuiGraphics graphics) {
        int logoWidth = getLogoWidth();
        int logoHeight = logoWidth * MagicWorldStaticBackground.LOGO_HEIGHT / MagicWorldStaticBackground.LOGO_WIDTH;
        int x = getMenuX() + (MENU_WIDTH - logoWidth) / 2;
        int y = getLogoY();

        graphics.blit(
                MagicWorldStaticBackground.FULL_LOGO,
                x,
                y,
                0,
                0,
                logoWidth,
                logoHeight,
                MagicWorldStaticBackground.LOGO_WIDTH,
                MagicWorldStaticBackground.LOGO_HEIGHT
        );
    }

    private void drawVersionText(GuiGraphics graphics) {
        int left = 6;
        int bottom = height - 30;
        graphics.drawString(font, "Forge 47.4.10", left, bottom, 0xFFFFFFFF);
        graphics.drawString(font, "Minecraft 1.20.1", left, bottom + 10, 0xFFFFFFFF);
        graphics.drawString(font, "Magic World", left, bottom + 20, 0xFFFFFFFF);
    }
}
