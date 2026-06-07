package com.magicworld.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

public class MagicWorldMenuButton extends AbstractWidget {
    private final Runnable action;
    private final Icon icon;

    public MagicWorldMenuButton(int x, int y, int width, int height, Component message, Runnable action) {
        super(x, y, width, height, message);
        this.action = action;
        this.icon = Icon.fromText(message.getString());
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        MagicWorldMenuTheme.drawButton(
                graphics,
                Minecraft.getInstance().font,
                getX(),
                getY(),
                getWidth(),
                getHeight(),
                getMessage(),
                mouseX,
                mouseY,
                isHoveredOrFocused()
        );

        drawIcon(graphics, getX() + 9, getY() + getHeight() / 2, icon, isHoveredOrFocused());
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        action.run();
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narration) {
        defaultButtonNarrationText(narration);
    }

    private static void drawIcon(GuiGraphics graphics, int x, int centerY, Icon icon, boolean active) {
        int color = active ? MagicWorldMenuTheme.GOLD : MagicWorldMenuTheme.BLUE;
        drawIconShape(graphics, x + 1, centerY + 1, icon, 0xAA000000);
        drawIconShape(graphics, x, centerY, icon, color);
    }

    private static void drawIconShape(GuiGraphics graphics, int x, int centerY, Icon icon, int color) {
        switch (icon) {
            case PLAY -> {
                graphics.fill(x + 1, centerY - 7, x + 4, centerY + 8, color);
                graphics.fill(x + 4, centerY - 6, x + 7, centerY + 7, color);
                graphics.fill(x + 7, centerY - 5, x + 10, centerY + 6, color);
                graphics.fill(x + 10, centerY - 3, x + 13, centerY + 4, color);
            }
            case MULTIPLAYER -> {
                graphics.renderOutline(x, centerY - 6, 5, 5, color);
                graphics.renderOutline(x + 7, centerY - 6, 5, 5, color);
                graphics.renderOutline(x + 3, centerY + 1, 6, 5, color);
                graphics.hLine(x + 2, x + 10, centerY, color);
            }
            case OPTIONS -> {
                graphics.renderOutline(x, centerY - 5, 10, 10, color);
                graphics.fill(x + 3, centerY - 2, x + 7, centerY + 2, color);
                graphics.hLine(x - 2, x + 12, centerY, color);
                graphics.vLine(x + 5, centerY - 7, centerY + 7, color);
            }
            case MODS -> {
                graphics.renderOutline(x, centerY - 6, 5, 5, color);
                graphics.renderOutline(x + 7, centerY - 6, 5, 5, color);
                graphics.renderOutline(x, centerY + 1, 5, 5, color);
                graphics.renderOutline(x + 7, centerY + 1, 5, 5, color);
            }
            case QUIT -> {
                graphics.hLine(x, x + 10, centerY - 5, color);
                graphics.hLine(x, x + 10, centerY + 5, color);
                graphics.vLine(x, centerY - 5, centerY + 5, color);
                graphics.vLine(x + 10, centerY - 5, centerY + 5, color);
                graphics.hLine(x + 4, x + 13, centerY, color);
                graphics.fill(x + 11, centerY - 2, x + 14, centerY + 3, color);
            }
        }
    }

    private enum Icon {
        PLAY,
        MULTIPLAYER,
        OPTIONS,
        MODS,
        QUIT;

        private static Icon fromText(String text) {
            String normalized = text.toUpperCase();
            if (normalized.contains("JOGAR") || normalized.contains("UM JOGADOR")) {
                return PLAY;
            }
            if (normalized.contains("MULTI")) {
                return MULTIPLAYER;
            }
            if (normalized.contains("OP")) {
                return OPTIONS;
            }
            if (normalized.contains("MOD")) {
                return MODS;
            }
            return QUIT;
        }
    }
}
