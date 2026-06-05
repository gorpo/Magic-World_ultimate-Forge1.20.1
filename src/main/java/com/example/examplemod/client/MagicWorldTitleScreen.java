package com.example.examplemod.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.OptionsScreen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.client.gui.ModListScreen;

public class MagicWorldTitleScreen extends Screen {
    private static final int BUTTON_WIDTH = 200;
    private static final int BUTTON_HEIGHT = 20;
    private static final int BUTTON_GAP = 6;

    public MagicWorldTitleScreen() {
        super(Component.literal("Magic World"));
    }

    @Override
    protected void init() {
        int x = width / 2 - BUTTON_WIDTH / 2;
        int y = Math.max(118, Math.min(height - 132, height / 2 - 6));

        addRenderableWidget(Button.builder(
                        Component.literal("Um jogador"),
                        button -> minecraft.setScreen(new SelectWorldScreen(this))
                )
                .bounds(x, y, BUTTON_WIDTH, BUTTON_HEIGHT)
                .build());

        addRenderableWidget(Button.builder(
                        Component.literal("Multijogador"),
                        button -> minecraft.setScreen(new JoinMultiplayerScreen(this))
                )
                .bounds(x, y + step(1), BUTTON_WIDTH, BUTTON_HEIGHT)
                .build());

        addRenderableWidget(Button.builder(
                        Component.literal("Mods"),
                        button -> minecraft.setScreen(new ModListScreen(this))
                )
                .bounds(x, y + step(2), 98, BUTTON_HEIGHT)
                .build());

        addRenderableWidget(Button.builder(
                        Component.literal("Opcoes..."),
                        button -> minecraft.setScreen(new OptionsScreen(this, minecraft.options))
                )
                .bounds(x, y + step(3) + 10, 98, BUTTON_HEIGHT)
                .build());

        addRenderableWidget(Button.builder(
                        Component.literal("Sair do jogo"),
                        button -> minecraft.stop()
                )
                .bounds(x + 102, y + step(3) + 10, 98, BUTTON_HEIGHT)
                .build());
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

    private void drawLogo(GuiGraphics graphics) {
        int logoWidth = Math.max(260, Math.min(520, width - 56));
        int logoHeight = logoWidth * MagicWorldStaticBackground.LOGO_HEIGHT / MagicWorldStaticBackground.LOGO_WIDTH;
        int x = width / 2 - logoWidth / 2;
        int y = Math.max(18, Math.min(64, height / 8));

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
        graphics.drawString(font, "Magic World", left, bottom + 20, 0xFFFFFFFF);
    }
}
