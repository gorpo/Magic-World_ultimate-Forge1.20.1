package com.magicworld.client;

import com.magicworld.MagicWorld;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;

import java.lang.reflect.Method;

public class MagicWorldDistantHorizonsButton extends AbstractWidget {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final ResourceLocation ICON =
            new ResourceLocation(MagicWorld.MODID, "textures/gui/magic_world_graphics_logo.png");

    private final Screen parent;

    public MagicWorldDistantHorizonsButton(int x, int y, int width, int height, Screen parent) {
        super(x, y, width, height, Component.literal("Horizontes Distantes"));
        this.parent = parent;
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        int left = getX();
        int top = getY();
        int right = left + getWidth();
        int bottom = top + getHeight();
        int fill = isHoveredOrFocused() ? 0xCC063552 : 0xAA031A2B;
        int iconSize = Math.max(12, Math.min(16, getHeight() - 4));
        int iconY = top + (getHeight() - iconSize) / 2;

        graphics.fill(left, top, right, bottom, fill);
        graphics.renderOutline(left, top, getWidth(), getHeight(), 0xCC00A9D6);
        graphics.fill(left, bottom - 2, right, bottom, 0xFF00D9FF);
        graphics.blit(ICON, left + 4, iconY, iconSize, iconSize, 0, 0, 64, 64, 64, 64);

        Component label = getMessage();
        int textX = left + iconSize + 9;
        if (Minecraft.getInstance().font.width(label) > right - textX - 4) {
            label = Component.literal("Horiz. Distantes");
        }
        graphics.drawString(
                Minecraft.getInstance().font,
                label,
                textX,
                top + (getHeight() - 8) / 2,
                0xFF00D9FF
        );
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        openDistantHorizonsScreen();
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narration) {
        defaultButtonNarrationText(narration);
    }

    private void openDistantHorizonsScreen() {
        for (String className : new String[] {
                "com.seibel.distanthorizons.common.wrappers.gui.GetConfigScreen_forge",
                "com.seibel.distanthorizons.common.wrappers.gui.GetConfigScreen"
        }) {
            try {
                Class<?> configScreenClass = Class.forName(className);
                Method getScreen = configScreenClass.getMethod("getScreen", Screen.class);
                Object screen = getScreen.invoke(null, parent);
                if (screen instanceof Screen distantHorizonsScreen) {
                    Minecraft.getInstance().setScreen(distantHorizonsScreen);
                    return;
                }
            } catch (ReflectiveOperationException ignored) {
                // Try the next class name for another compatible Distant Horizons release.
            }
        }

        LOGGER.warn("Magic World could not open the Distant Horizons configuration screen.");
    }
}
