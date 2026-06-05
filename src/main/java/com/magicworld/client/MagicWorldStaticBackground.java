package com.magicworld.client;

import com.magicworld.MagicWorld;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

public final class MagicWorldStaticBackground {
    public static final ResourceLocation TITLE_BACKGROUND =
            new ResourceLocation(MagicWorld.MODID, "textures/gui/title/title_background_static.png");
    public static final ResourceLocation FULL_LOGO =
            new ResourceLocation(MagicWorld.MODID, "textures/gui/title/logo_full.png");

    public static final int BACKGROUND_WIDTH = 2560;
    public static final int BACKGROUND_HEIGHT = 1440;
    public static final int LOGO_WIDTH = 2172;
    public static final int LOGO_HEIGHT = 724;

    private MagicWorldStaticBackground() {
    }

    public static void draw(GuiGraphics graphics, int screenWidth, int screenHeight) {
        drawCoverTexture(
                graphics,
                TITLE_BACKGROUND,
                screenWidth,
                screenHeight,
                BACKGROUND_WIDTH,
                BACKGROUND_HEIGHT
        );
        graphics.fill(0, 0, screenWidth, screenHeight, 0x33000000);
    }

    public static void drawCoverTexture(
            GuiGraphics graphics,
            ResourceLocation texture,
            int screenWidth,
            int screenHeight,
            int textureWidth,
            int textureHeight
    ) {
        float screenRatio = screenWidth / (float) screenHeight;
        float textureRatio = textureWidth / (float) textureHeight;
        int sourceX = 0;
        int sourceY = 0;
        int sourceWidth = textureWidth;
        int sourceHeight = textureHeight;

        if (screenRatio > textureRatio) {
            sourceHeight = Math.max(1, (int) (textureWidth / screenRatio));
            sourceY = (textureHeight - sourceHeight) / 2;
        } else {
            sourceWidth = Math.max(1, (int) (textureHeight * screenRatio));
            sourceX = (textureWidth - sourceWidth) / 2;
        }

        graphics.blit(
                texture,
                0,
                0,
                screenWidth,
                screenHeight,
                sourceX,
                sourceY,
                sourceWidth,
                sourceHeight,
                textureWidth,
                textureHeight
        );
    }
}
