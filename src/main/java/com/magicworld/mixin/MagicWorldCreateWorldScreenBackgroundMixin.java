package com.magicworld.mixin;

import com.magicworld.client.MagicWorldStaticBackground;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CreateWorldScreen.class)
public abstract class MagicWorldCreateWorldScreenBackgroundMixin {
    @Inject(method = "renderDirtBackground(Lnet/minecraft/client/gui/GuiGraphics;)V", at = @At("HEAD"), cancellable = true)
    private void magicworld$renderDirtBackground(GuiGraphics graphics, CallbackInfo callback) {
        CreateWorldScreen screen = (CreateWorldScreen) (Object) this;
        MagicWorldStaticBackground.draw(graphics, screen.width, screen.height);
        callback.cancel();
    }
}
