package com.magicworld.mixin;

import com.magicworld.client.MagicWorldStaticBackground;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.TabButton;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TabButton.class)
public abstract class MagicWorldTabButtonMixin extends AbstractWidget {
    private MagicWorldTabButtonMixin(int x, int y, int width, int height, Component message) {
        super(x, y, width, height, message);
    }

    @Inject(
            method = {
                    "renderWidget(Lnet/minecraft/client/gui/GuiGraphics;IIF)V",
                    "m_87963_(Lnet/minecraft/client/gui/GuiGraphics;IIF)V"
            },
            at = @At("HEAD"),
            cancellable = true,
            require = 0,
            remap = false
    )
    private void magicworld$renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick, CallbackInfo callback) {
        Minecraft minecraft = Minecraft.getInstance();
        int x = getX();
        int y = getY();
        int width = getWidth();
        int height = getHeight();

        graphics.enableScissor(x, y, x + width, y + height);
        MagicWorldStaticBackground.drawCoverTexture(
                graphics,
                MagicWorldStaticBackground.TITLE_BACKGROUND,
                minecraft.getWindow().getGuiScaledWidth(),
                minecraft.getWindow().getGuiScaledHeight(),
                MagicWorldStaticBackground.BACKGROUND_WIDTH,
                MagicWorldStaticBackground.BACKGROUND_HEIGHT
        );
        graphics.disableScissor();

        boolean selected = ((TabButton) (Object) this).isSelected();
        int panelColor = selected ? 0x66000000 : 0x44000000;
        int borderColor = selected ? 0xFFFFFFFF : isHoveredOrFocused() ? 0xFFE0E0E0 : 0x99FFFFFF;
        int textColor = active ? 0xFFFFFFFF : 0xFFA0A0A0;

        graphics.fill(x, y, x + width, y + height, panelColor);
        graphics.fill(x, y, x + width, y + 1, borderColor);
        graphics.fill(x, y + height - 1, x + width, y + height, borderColor);
        graphics.fill(x, y, x + 1, y + height, borderColor);
        graphics.fill(x + width - 1, y, x + width, y + height, borderColor);

        Font font = minecraft.font;
        graphics.drawCenteredString(font, getMessage(), x + width / 2, y + (height - 8) / 2, textColor);
        if (selected) {
            int underlineWidth = Math.min(font.width(getMessage()), width - 4);
            int underlineX = x + (width - underlineWidth) / 2;
            int underlineY = y + height - 2;
            graphics.fill(underlineX, underlineY, underlineX + underlineWidth, underlineY + 1, textColor);
        }

        callback.cancel();
    }
}
