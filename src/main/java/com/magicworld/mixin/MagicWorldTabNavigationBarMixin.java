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
        method = {
            "render(Lnet/minecraft/client/gui/GuiGraphics;IIF)V",
            "m_88315_(Lnet/minecraft/client/gui/GuiGraphics;IIF)V"
        },
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/GuiGraphics;fill(IIIII)V"
        ),
        require = 0,
        remap = false
    )
    private void magicworld$skipOfficialBlackHeaderFill(GuiGraphics graphics, int left, int top, int right, int bottom, int color) {
    }

    @Redirect(
        method = {
            "render(Lnet/minecraft/client/gui/GuiGraphics;IIF)V",
            "m_88315_(Lnet/minecraft/client/gui/GuiGraphics;IIF)V"
        },
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/GuiGraphics;m_280509_(IIIII)V"
        ),
        require = 0,
        remap = false
    )
    private void magicworld$skipSrgBlackHeaderFill(GuiGraphics graphics, int left, int top, int right, int bottom, int color) {
    }

    @Redirect(
        method = {
            "render(Lnet/minecraft/client/gui/GuiGraphics;IIF)V",
            "m_88315_(Lnet/minecraft/client/gui/GuiGraphics;IIF)V"
        },
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/GuiGraphics;blit(Lnet/minecraft/resources/ResourceLocation;IIFFIIII)V"
        ),
        require = 0,
        remap = false
    )
    private void magicworld$skipOfficialVanillaHeaderSeparator(
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

    @Redirect(
        method = {
            "render(Lnet/minecraft/client/gui/GuiGraphics;IIF)V",
            "m_88315_(Lnet/minecraft/client/gui/GuiGraphics;IIF)V"
        },
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/GuiGraphics;m_280163_(Lnet/minecraft/resources/ResourceLocation;IIFFIIII)V"
        ),
        require = 0,
        remap = false
    )
    private void magicworld$skipSrgVanillaHeaderSeparator(
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
