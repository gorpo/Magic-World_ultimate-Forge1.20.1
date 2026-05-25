package com.magicworld.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public final class MagicWorldMenuTheme {
    public static final int BLUE = 0xFF22D3FF;
    public static final int BLUE_DARK = 0xFF071D4A;
    public static final int GOLD = 0xFFD9A441;
    public static final int GOLD_DARK = 0xFF6B3F10;
    public static final int WHITE = 0xFFE8F2FF;
    public static final int PANEL = 0xB8050814;
    public static final int PANEL_HOVER = 0xD80B1740;
    public static final int SHADOW = 0xAA000000;
    private static final int BACKDROP_WIDTH = 1672;
    private static final int BACKDROP_HEIGHT = 941;

    private static final Identifier BACKDROP =
            Identifier.fromNamespaceAndPath(
                    "magicworld",
                    "textures/gui/menu_background.png"
            );

    private MagicWorldMenuTheme() {
    }

    public static void drawBackdrop(
            GuiGraphicsExtractor graphics,
            int width,
            int height
    ) {
        float screenRatio =
                width / (float) height;

        float backdropRatio =
                BACKDROP_WIDTH / (float) BACKDROP_HEIGHT;

        int sourceX =
                0;

        int sourceY =
                0;

        int sourceWidth =
                BACKDROP_WIDTH;

        int sourceHeight =
                BACKDROP_HEIGHT;

        if (screenRatio > backdropRatio) {
            sourceHeight =
                    Math.max(1, (int) (BACKDROP_WIDTH / screenRatio));

            sourceY =
                    (BACKDROP_HEIGHT - sourceHeight) / 2;
        } else {
            sourceWidth =
                    Math.max(1, (int) (BACKDROP_HEIGHT * screenRatio));

            sourceX =
                    (BACKDROP_WIDTH - sourceWidth) / 2;
        }

        graphics.blit(
                RenderPipelines.GUI_TEXTURED,
                BACKDROP,
                0,
                0,
                sourceX,
                sourceY,
                width,
                height,
                sourceWidth,
                sourceHeight,
                BACKDROP_WIDTH,
                BACKDROP_HEIGHT
        );

        graphics.fill(0, 0, width, height, 0x66000000);
        graphics.fill(0, 0, width, 1, GOLD_DARK);
        graphics.fill(0, height - 1, width, height, GOLD_DARK);
        graphics.fill(0, 0, 1, height, GOLD_DARK);
        graphics.fill(width - 1, 0, width, height, GOLD_DARK);
    }

    public static void drawFrame(
            GuiGraphicsExtractor graphics,
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
            GuiGraphicsExtractor graphics,
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
        boolean hovered =
                mouseX >= x
                        && mouseX <= x + width
                        && mouseY >= y
                        && mouseY <= y + height;

        int fill =
                selected || hovered
                        ? PANEL_HOVER
                        : PANEL;

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

        graphics.centeredText(
                font,
                text,
                x + width / 2 + 7,
                y + height / 2 - 4 + 1,
                SHADOW
        );

        graphics.centeredText(
                font,
                text,
                x + width / 2 + 6,
                y + height / 2 - 4,
                selected || hovered ? BLUE : WHITE
        );
    }

    public static void toast(
            String message
    ) {
        Minecraft minecraft =
                Minecraft.getInstance();

        if (minecraft.player != null) {
            minecraft.player.sendSystemMessage(
                    Component.literal(message)
            );
        }
    }
}
