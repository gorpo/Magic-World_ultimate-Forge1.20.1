package com.magicworld.mixin;

import com.magicworld.client.MagicWorldMenuTheme;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Screen.class)
public abstract class MagicWorldScreenBackgroundMixin {
    @Shadow
    public int width;

    @Shadow
    public int height;

    @Inject(
            method = "extractMenuBackground(Lnet/minecraft/client/gui/GuiGraphicsExtractor;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void magicworld$extractMenuBackground(
            GuiGraphicsExtractor graphics,
            CallbackInfo callback
    ) {
        MagicWorldMenuTheme.drawBackdrop(
                graphics,
                width,
                height
        );

        callback.cancel();
    }

    @Inject(
            method = "extractMenuBackground(Lnet/minecraft/client/gui/GuiGraphicsExtractor;IIII)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void magicworld$extractMenuBackgroundArea(
            GuiGraphicsExtractor graphics,
            int x,
            int y,
            int width,
            int height,
            CallbackInfo callback
    ) {
        MagicWorldMenuTheme.drawBackdrop(
                graphics,
                this.width,
                this.height
        );

        callback.cancel();
    }
}
