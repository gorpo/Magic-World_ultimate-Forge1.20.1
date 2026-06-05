package com.magicworld.mixin;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.tabs.TabNavigationBar;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(TabNavigationBar.class)
public abstract class MagicWorldTabNavigationBarMixin {
    @Redirect(
        method = "render(Lnet/minecraft/client/gui/GuiGraphics;IIF)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/GuiGraphics;fill(IIIII)V"
        )
    )
    private void magicworld$skipBlackHeaderFill(GuiGraphics graphics, int left, int top, int right, int bottom, int color) {
    }

    @Redirect(
        method = "render(Lnet/minecraft/client/gui/GuiGraphics;IIF)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/GuiGraphics;blit(Lnet/minecraft/resources/ResourceLocation;IIFFIIII)V"
        )
    )
    private void magicworld$skipVanillaHeaderSeparator(
            GuiGraphics graphics,
            ResourceLocation texture,
            int x,
            int y,
            float uOffset,
            float vOffset,
            int uWidth,
            int vHeight,
            int textureWidth,
            int textureHeight
    ) {
    }
}
