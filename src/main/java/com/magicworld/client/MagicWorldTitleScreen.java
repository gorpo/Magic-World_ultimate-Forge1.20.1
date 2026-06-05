package com.magicworld.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.AccessibilityOptionsScreen;
import net.minecraft.client.gui.screens.LanguageSelectScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.controls.ControlsScreen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.OptionsScreen;
import net.minecraft.client.gui.screens.packs.PackSelectionScreen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.client.gui.ModListScreen;

public class MagicWorldTitleScreen extends Screen {
    private static final int BUTTON_WIDTH = 148;
    private static final int BUTTON_HEIGHT = 20;
    private static final int BUTTON_GAP = 6;
    private static final int MENU_WIDTH = 300;
    private static final int QUICK_BUTTON_SIZE = 24;
    private static final int QUICK_BUTTON_GAP = 9;

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
                Component.literal("OPCOES"),
                () -> minecraft.setScreen(new OptionsScreen(this, minecraft.options))
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
                Component.literal("SAIR"),
                () -> minecraft.stop()
        ));

        addQuickButtons();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        MagicWorldStaticBackground.draw(graphics, width, height);
        graphics.fill(0, 0, width, height, 0x1E010610);
        drawLogo(graphics);
        drawScreenOrnaments(graphics);
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
        return Math.max(12, Math.round(height * 0.09F));
    }

    private int getButtonY() {
        int logoHeight = getLogoWidth() * MagicWorldStaticBackground.LOGO_HEIGHT / MagicWorldStaticBackground.LOGO_WIDTH;
        int preferred = getLogoY() + logoHeight + 18;
        int max = height - step(4) - BUTTON_HEIGHT - QUICK_BUTTON_SIZE - 44;
        return Math.max(84, Math.min(preferred, max));
    }

    private int getLogoWidth() {
        return Math.max(180, Math.min(MENU_WIDTH, width / 5));
    }

    private int getQuickButtonY() {
        return getButtonY() + step(4) + 14;
    }

    private void addQuickButtons() {
        int count = 5;
        int totalWidth = count * QUICK_BUTTON_SIZE + (count - 1) * QUICK_BUTTON_GAP;
        int startX = getMenuX() + (MENU_WIDTH - totalWidth) / 2;
        int y = getQuickButtonY();

        addRenderableWidget(new MagicWorldIconButton(
                startX,
                y,
                QUICK_BUTTON_SIZE,
                Component.literal("Multiplayer"),
                MagicWorldIconButton.Icon.MULTIPLAYER,
                () -> minecraft.setScreen(new JoinMultiplayerScreen(this))
        ));

        addRenderableWidget(new MagicWorldIconButton(
                startX + quickStep(1),
                y,
                QUICK_BUTTON_SIZE,
                Component.literal("Idioma"),
                MagicWorldIconButton.Icon.LANGUAGE,
                () -> minecraft.setScreen(new LanguageSelectScreen(this, minecraft.options, minecraft.getLanguageManager()))
        ));

        addRenderableWidget(new MagicWorldIconButton(
                startX + quickStep(2),
                y,
                QUICK_BUTTON_SIZE,
                Component.literal("Controles"),
                MagicWorldIconButton.Icon.CONTROLS,
                () -> minecraft.setScreen(new ControlsScreen(this, minecraft.options))
        ));

        addRenderableWidget(new MagicWorldIconButton(
                startX + quickStep(3),
                y,
                QUICK_BUTTON_SIZE,
                Component.literal("Pacotes"),
                MagicWorldIconButton.Icon.RESOURCE_PACKS,
                () -> minecraft.setScreen(new PackSelectionScreen(
                        minecraft.getResourcePackRepository(),
                        repository -> {
                            minecraft.options.updateResourcePacks(repository);
                            minecraft.setScreen(this);
                        },
                        minecraft.getResourcePackDirectory(),
                        Component.translatable("resourcePack.title")
                ))
        ));

        addRenderableWidget(new MagicWorldIconButton(
                startX + quickStep(4),
                y,
                QUICK_BUTTON_SIZE,
                Component.literal("Acessibilidade"),
                MagicWorldIconButton.Icon.ACCESSIBILITY,
                () -> minecraft.setScreen(new AccessibilityOptionsScreen(this, minecraft.options))
        ));
    }

    private static int quickStep(int index) {
        return index * (QUICK_BUTTON_SIZE + QUICK_BUTTON_GAP);
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
                logoWidth,
                logoHeight,
                0,
                0,
                MagicWorldStaticBackground.LOGO_WIDTH,
                MagicWorldStaticBackground.LOGO_HEIGHT,
                MagicWorldStaticBackground.LOGO_WIDTH,
                MagicWorldStaticBackground.LOGO_HEIGHT
        );
    }

    private void drawVersionText(GuiGraphics graphics) {
        int left = 6;
        int bottom = height - 30;
        graphics.drawString(font, "Forge 47.4.10", left, bottom, 0xFFFFFFFF);
        graphics.drawString(font, "Minecraft 1.20.1", left, bottom + 10, 0xFFFFFFFF);
        graphics.drawString(font, "Magic World 1.0.0.1", left, bottom + 20, 0xFFFFFFFF);
    }

    private void drawScreenOrnaments(GuiGraphics graphics) {
        graphics.fill(0, 0, width, 1, MagicWorldMenuTheme.GOLD_DARK);
        graphics.fill(0, height - 1, width, height, MagicWorldMenuTheme.GOLD_DARK);
        graphics.fill(0, 0, 1, height, MagicWorldMenuTheme.GOLD_DARK);
        graphics.fill(width - 1, 0, width, height, MagicWorldMenuTheme.GOLD_DARK);

        drawCorner(graphics, 1, 1, 1, 1);
        drawCorner(graphics, width - 1, 1, -1, 1);
        drawCorner(graphics, 1, height - 1, 1, -1);
        drawCorner(graphics, width - 1, height - 1, -1, -1);
    }

    private void drawCorner(GuiGraphics graphics, int x, int y, int xDirection, int yDirection) {
        fillOriented(graphics, x, y, x + 18 * xDirection, y + yDirection, MagicWorldMenuTheme.GOLD);
        fillOriented(graphics, x, y, x + xDirection, y + 18 * yDirection, MagicWorldMenuTheme.GOLD);
        fillOriented(graphics, x + 5 * xDirection, y + 5 * yDirection, x + 14 * xDirection, y + 6 * yDirection, MagicWorldMenuTheme.BLUE);
        fillOriented(graphics, x + 5 * xDirection, y + 5 * yDirection, x + 6 * xDirection, y + 14 * yDirection, MagicWorldMenuTheme.BLUE);
    }

    private void fillOriented(GuiGraphics graphics, int x1, int y1, int x2, int y2, int color) {
        graphics.fill(Math.min(x1, x2), Math.min(y1, y2), Math.max(x1, x2), Math.max(y1, y2), color);
    }
}
