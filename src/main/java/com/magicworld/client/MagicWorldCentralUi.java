package com.magicworld.client;

import net.minecraft.client.gui.GuiGraphics;

public final class MagicWorldCentralUi {
    private MagicWorldCentralUi() {
    }

    public static int drawLogo(GuiGraphics graphics, int centerX, int y, int maxWidth, int maxHeight) {
        float ratio = MagicWorldStaticBackground.LOGO_WIDTH / (float) MagicWorldStaticBackground.LOGO_HEIGHT;
        int drawWidth = Math.max(96, Math.min(maxWidth, Math.round(maxHeight * ratio)));
        int drawHeight = Math.max(1, Math.round(drawWidth / ratio));
        if (drawHeight > maxHeight) {
            drawHeight = maxHeight;
            drawWidth = Math.round(drawHeight * ratio);
        }
        graphics.blit(
                MagicWorldStaticBackground.FULL_LOGO,
                centerX - drawWidth / 2,
                y,
                drawWidth,
                drawHeight,
                0,
                0,
                MagicWorldStaticBackground.LOGO_WIDTH,
                MagicWorldStaticBackground.LOGO_HEIGHT,
                MagicWorldStaticBackground.LOGO_WIDTH,
                MagicWorldStaticBackground.LOGO_HEIGHT
        );
        return drawHeight;
    }
}
