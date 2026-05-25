package com.magicworld.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

public class MagicWorldIconButton extends AbstractWidget {
    private final Icon icon;
    private final Runnable action;

    public MagicWorldIconButton(
            int x,
            int y,
            int size,
            Component message,
            Icon icon,
            Runnable action
    ) {
        super(x, y, size, size, message);
        this.icon = icon;
        this.action = action;
    }

    @Override
    protected void extractWidgetRenderState(
            GuiGraphicsExtractor graphics,
            int mouseX,
            int mouseY,
            float partialTick
    ) {
        boolean active =
                isHoveredOrFocused();

        int fill =
                active
                        ? 0xD80B1740
                        : 0xB8050814;

        graphics.fill(getX() + 2, getY() + 2, getX() + getWidth() + 2, getY() + getHeight() + 2, MagicWorldMenuTheme.SHADOW);
        graphics.fill(getX(), getY(), getX() + getWidth(), getY() + getHeight(), fill);
        graphics.outline(getX(), getY(), getWidth(), getHeight(), active ? MagicWorldMenuTheme.GOLD : MagicWorldMenuTheme.GOLD_DARK);
        graphics.outline(getX() + 3, getY() + 3, getWidth() - 6, getHeight() - 6, active ? MagicWorldMenuTheme.BLUE : MagicWorldMenuTheme.BLUE_DARK);

        int centerX =
                getX() + getWidth() / 2;

        int centerY =
                getY() + getHeight() / 2;

        drawIcon(
                graphics,
                centerX + 1,
                centerY + 1,
                icon,
                0xAA000000
        );

        drawIcon(
                graphics,
                centerX,
                centerY,
                icon,
                active ? MagicWorldMenuTheme.GOLD : MagicWorldMenuTheme.BLUE
        );

        if (active) {
            graphics.centeredText(
                    Minecraft.getInstance().font,
                    getMessage(),
                    centerX,
                    getY() - 11,
                    MagicWorldMenuTheme.WHITE
            );
        }
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
            int centerX,
            int centerY,
            Icon icon,
            int color
    ) {
        switch (icon) {
            case MULTIPLAYER -> {
                graphics.outline(centerX - 8, centerY - 7, 6, 6, color);
                graphics.outline(centerX + 2, centerY - 7, 6, 6, color);
                graphics.outline(centerX - 3, centerY + 2, 6, 6, color);
                graphics.horizontalLine(centerX - 5, centerX + 5, centerY, color);
                graphics.verticalLine(centerX, centerY, centerY + 2, color);
            }
            case LANGUAGE -> {
                graphics.outline(centerX - 7, centerY - 7, 14, 14, color);
                graphics.verticalLine(centerX, centerY - 7, centerY + 7, color);
                graphics.horizontalLine(centerX - 7, centerX + 7, centerY, color);
                graphics.horizontalLine(centerX - 5, centerX + 5, centerY - 4, color);
                graphics.horizontalLine(centerX - 5, centerX + 5, centerY + 4, color);
            }
            case CONTROLS -> {
                graphics.fill(centerX - 2, centerY - 8, centerX + 3, centerY + 9, color);
                graphics.fill(centerX - 8, centerY - 2, centerX + 9, centerY + 3, color);
                graphics.fill(centerX - 6, centerY - 6, centerX - 3, centerY - 3, color);
                graphics.fill(centerX + 4, centerY + 4, centerX + 7, centerY + 7, color);
            }
            case RESOURCE_PACKS -> {
                graphics.outline(centerX - 8, centerY - 7, 7, 14, color);
                graphics.outline(centerX + 1, centerY - 7, 7, 14, color);
                graphics.verticalLine(centerX, centerY - 6, centerY + 6, color);
                graphics.horizontalLine(centerX - 6, centerX - 2, centerY - 3, color);
                graphics.horizontalLine(centerX + 2, centerX + 6, centerY - 3, color);
            }
            case ACCESSIBILITY -> {
                graphics.outline(centerX - 2, centerY - 8, 5, 5, color);
                graphics.horizontalLine(centerX - 8, centerX + 8, centerY - 1, color);
                graphics.verticalLine(centerX, centerY - 1, centerY + 8, color);
                graphics.fill(centerX - 6, centerY + 5, centerX - 3, centerY + 8, color);
                graphics.fill(centerX + 4, centerY + 5, centerX + 7, centerY + 8, color);
            }
        }
    }

    public enum Icon {
        MULTIPLAYER,
        LANGUAGE,
        CONTROLS,
        RESOURCE_PACKS,
        ACCESSIBILITY
    }
}
