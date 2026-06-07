package com.magicworld.client;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public final class MagicWorldMenuTheme {
    public static final int BLUE = 0xFF22D3FF;
    public static final int BLUE_DARK = 0xFF071D4A;
    public static final int GOLD = 0xFFD9A441;
    public static final int GOLD_DARK = 0xFF6B3F10;
    public static final int WHITE = 0xFFE8F2FF;
    public static final int PANEL = 0xB8050814;
    public static final int PANEL_HOVER = 0xD80B1740;
    public static final int SHADOW = 0xAA000000;

    private MagicWorldMenuTheme() {
    }

    public static void drawFrame(
            GuiGraphics graphics,
            int x,
            int y,
            int width,
            int height
    ) {
        graphics.fill(x + 4, y + 5, x + width + 4, y + height + 5, SHADOW);
        graphics.fill(x, y, x + width, y + height, PANEL);
        graphics.fill(x, y, x + width, y + 1, GOLD);
        graphics.fill(x, y, x + 1, y + height, GOLD);
        graphics.fill(x, y + height - 1, x + width, y + height, GOLD_DARK);
        graphics.fill(x + width - 1, y, x + width, y + height, GOLD_DARK);
        graphics.fill(x + 3, y + 3, x + width - 3, y + 4, BLUE);
        graphics.fill(x + 3, y + height - 4, x + width - 3, y + height - 3, BLUE_DARK);
    }

    public static void drawButton(
            GuiGraphics graphics,
            Font font,
            int x,
            int y,
            int width,
            int height,
            Component text,
            int mouseX,
            int mouseY,
            boolean selected
    ) {
        boolean hovered = mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
        int fill = selected || hovered ? PANEL_HOVER : PANEL;

        graphics.fill(x + 2, y + 2, x + width + 2, y + height + 2, SHADOW);
        graphics.fill(x, y, x + width, y + height, fill);
        graphics.fill(x, y, x + width, y + 1, GOLD);
        graphics.fill(x, y, x + 1, y + height, GOLD_DARK);
        graphics.fill(x, y + height - 1, x + width, y + height, GOLD_DARK);
        graphics.fill(x + width - 1, y, x + width, y + height, GOLD_DARK);

        if (selected || hovered) {
            graphics.fill(x + 3, y + 3, x + width - 3, y + 4, BLUE);
            graphics.fill(x + 3, y + height - 4, x + width - 3, y + height - 3, BLUE);
        } else {
            graphics.fill(x + 3, y + 3, x + width - 3, y + 4, 0xAA0C315C);
        }

        graphics.drawCenteredString(font, text, x + width / 2 + 7, y + height / 2 - 4 + 1, SHADOW);
        graphics.drawCenteredString(font, text, x + width / 2 + 6, y + height / 2 - 4, selected || hovered ? BLUE : WHITE);
    }

    public static void toast(String message) {
        // Intentionally silent: gameplay feedback must not write chat messages.
    }
}
