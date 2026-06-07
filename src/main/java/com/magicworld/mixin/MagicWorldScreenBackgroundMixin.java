package com.magicworld.mixin;

import com.magicworld.client.MagicWorldScreenBackgrounds;
import com.magicworld.client.MagicWorldStaticBackground;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Screen.class)
public abstract class MagicWorldScreenBackgroundMixin {
    @Inject(
            method = {
                    "renderBackground(Lnet/minecraft/client/gui/GuiGraphics;)V",
                    "m_280273_(Lnet/minecraft/client/gui/GuiGraphics;)V"
            },
            at = @At("HEAD"),
            cancellable = true,
            require = 0,
            remap = false
    )
    private void magicworld$renderBackground(GuiGraphics graphics, CallbackInfo callback) {
        Screen screen = (Screen) (Object) this;
        if (!MagicWorldScreenBackgrounds.shouldUseStaticBackground(screen)) {
            return;
        }

        MagicWorldStaticBackground.draw(graphics, screen.width, screen.height);
        callback.cancel();
    }

    @Inject(
            method = {
                    "renderDirtBackground(Lnet/minecraft/client/gui/GuiGraphics;)V",
                    "m_280039_(Lnet/minecraft/client/gui/GuiGraphics;)V"
            },
            at = @At("HEAD"),
            cancellable = true,
            require = 0,
            remap = false
    )
    private void magicworld$renderDirtBackground(GuiGraphics graphics, CallbackInfo callback) {
        Screen screen = (Screen) (Object) this;
        if (!MagicWorldScreenBackgrounds.shouldUseStaticBackground(screen)) {
            return;
        }

        MagicWorldStaticBackground.draw(graphics, screen.width, screen.height);
        callback.cancel();
    }
}
