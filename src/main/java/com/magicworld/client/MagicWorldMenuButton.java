package com.magicworld.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

public class MagicWorldMenuButton extends AbstractWidget {
    private final Runnable action;
    private final Icon icon;

    public MagicWorldMenuButton(
            int x,
            int y,
            int width,
            int height,
            Component message,
            Runnable action
    ) {
        super(x, y, width, height, message);
        this.action = action;
        this.icon = Icon.fromText(message.getString());
    }

    @Override
    protected void extractWidgetRenderState(
            GuiGraphicsExtractor graphics,
            int mouseX,
            int mouseY,
            float partialTick
    ) {
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

        drawIcon(
                graphics,
                getX() + 9,
                getY() + getHeight() / 2,
                icon,
                isHoveredOrFocused()
        );
    }

    @Override
    public void onClick(
            MouseButtonEvent event,
            boolean doubleClick
    ) {
        action.run();
    }

    @Override
    protected void updateWidgetNarration(
            NarrationElementOutput narration
    ) {
        defaultButtonNarrationText(narration);
    }

    private static void drawIcon(
            GuiGraphicsExtractor graphics,
            int x,
            int centerY,
            Icon icon,
            boolean active
    ) {
        int color =
                active
                        ? MagicWorldMenuTheme.GOLD
                        : MagicWorldMenuTheme.BLUE;

        int shadow =
                0xAA000000;

        drawIconShape(
                graphics,
                x + 1,
                centerY + 1,
                icon,
                shadow
        );

        drawIconShape(
                graphics,
                x,
                centerY,
                icon,
                color
        );
    }

    private static void drawIconShape(
            GuiGraphicsExtractor graphics,
            int x,
            int centerY,
            Icon icon,
            int color
    ) {
        switch (icon) {
            case PLAY -> {
                graphics.fill(x, centerY - 5, x + 2, centerY + 6, color);
                graphics.fill(x + 2, centerY - 4, x + 4, centerY + 5, color);
                graphics.fill(x + 4, centerY - 3, x + 6, centerY + 4, color);
                graphics.fill(x + 6, centerY - 2, x + 8, centerY + 3, color);
            }
            case MULTIPLAYER -> {
                graphics.outline(x, centerY - 6, 5, 5, color);
                graphics.outline(x + 7, centerY - 6, 5, 5, color);
                graphics.outline(x + 3, centerY + 1, 6, 5, color);
                graphics.horizontalLine(x + 2, x + 10, centerY, color);
            }
            case OPTIONS -> {
                graphics.outline(x, centerY - 5, 10, 10, color);
                graphics.fill(x + 3, centerY - 2, x + 7, centerY + 2, color);
                graphics.horizontalLine(x - 2, x + 12, centerY, color);
                graphics.verticalLine(x + 5, centerY - 7, centerY + 7, color);
            }
            case MODS -> {
                graphics.outline(x, centerY - 6, 5, 5, color);
                graphics.outline(x + 7, centerY - 6, 5, 5, color);
                graphics.outline(x, centerY + 1, 5, 5, color);
                graphics.outline(x + 7, centerY + 1, 5, 5, color);
            }
            case QUIT -> {
                graphics.horizontalLine(x, x + 10, centerY - 5, color);
                graphics.horizontalLine(x, x + 10, centerY + 5, color);
                graphics.verticalLine(x, centerY - 5, centerY + 5, color);
                graphics.verticalLine(x + 10, centerY - 5, centerY + 5, color);
                graphics.horizontalLine(x + 4, x + 13, centerY, color);
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

        private static Icon fromText(
                String text
        ) {
            return switch (text) {
                case "JOGAR" -> PLAY;
                case "MULTIPLAYER" -> MULTIPLAYER;
                case "OP\u00c7\u00d5ES" -> OPTIONS;
                case "MODS" -> MODS;
                default -> QUIT;
            };
        }
    }
}
