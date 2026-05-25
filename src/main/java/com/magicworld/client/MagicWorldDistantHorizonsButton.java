package com.magicworld.client;

import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.client.gui.screens.Screen;
import org.slf4j.Logger;

import java.lang.reflect.Method;

public class MagicWorldDistantHorizonsButton extends AbstractWidget {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int MAGICWORLD_BLUE = 0xFF00D9FF;
    private static final int MAGICWORLD_BLUE_DIM = 0x6600D9FF;
    private static final int MAGICWORLD_PANEL = 0x2200294D;
    private static final int MAGICWORLD_PANEL_HOVER = 0x3300D9FF;
    private static final Identifier LOGO = Identifier.fromNamespaceAndPath(
            "magicworld",
            "textures/gui/magic_world_graphics_logo.png"
    );

    private final Screen parent;

    public MagicWorldDistantHorizonsButton(int x, int y, int width, int height, Screen parent) {
        super(x, y, width, height, Component.literal("Horizontes Distantes"));
        this.parent = parent;
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        handleCursor(graphics);

        int x = getX();
        int y = getY();
        int right = getRight();
        int bottom = getBottom();
        int logoSize = Math.max(12, Math.min(16, getHeight() - 4));
        int logoX = x + 4;
        int logoY = y + ((getHeight() - logoSize) / 2);
        int textX = logoX + logoSize + 5;
        int background = isHoveredOrFocused() ? MAGICWORLD_PANEL_HOVER : MAGICWORLD_PANEL;
        Component label = getMessage();

        graphics.fill(x, y, right, bottom, background);
        graphics.fill(x, bottom - 1, right, bottom, MAGICWORLD_BLUE_DIM);
        graphics.blit(
                RenderPipelines.GUI_TEXTURED,
                LOGO,
                logoX,
                logoY,
                0.0F,
                0.0F,
                logoSize,
                logoSize,
                64,
                64,
                64,
                64
        );

        if (Minecraft.getInstance().font.width(label) > right - textX - 4) {
            label = Component.literal("Horiz. Distantes");
        }

        graphics.text(
                Minecraft.getInstance().font,
                label,
                textX,
                y + ((getHeight() - 9) / 2),
                MAGICWORLD_BLUE
        );
    }

    @Override
    public void onClick(MouseButtonEvent event, boolean doubleClick) {
        openDistantHorizonsScreen();
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narration) {
        defaultButtonNarrationText(narration);
    }

    private void openDistantHorizonsScreen() {
        try {
            Class<?> configScreenClass = Class.forName(
                    "com.seibel.distanthorizons.common.wrappers.gui.GetConfigScreen"
            );
            Method getScreen = configScreenClass.getMethod("getScreen", Screen.class);
            Object screen = getScreen.invoke(null, parent);

            if (screen instanceof Screen distantHorizonsScreen) {
                Minecraft.getInstance().setScreen(distantHorizonsScreen);
            }
        } catch (ReflectiveOperationException exception) {
            LOGGER.warn("Magic World could not open the Distant Horizons configuration screen.", exception);
        }
    }
}
