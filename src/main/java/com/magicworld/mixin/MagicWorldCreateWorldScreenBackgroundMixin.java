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
    @Inject(
        method = {
                "render(Lnet/minecraft/client/gui/GuiGraphics;IIF)V",
                "m_88315_(Lnet/minecraft/client/gui/GuiGraphics;IIF)V"
        },
        at = @At("HEAD"),
        require = 0,
        remap = false
    )
    private void magicworld$renderBeforeWidgets(GuiGraphics graphics, int mouseX, int mouseY, float partialTick, CallbackInfo callback) {
        CreateWorldScreen screen = (CreateWorldScreen) (Object) this;
        MagicWorldStaticBackground.draw(graphics, screen.width, screen.height);
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
        CreateWorldScreen screen = (CreateWorldScreen) (Object) this;
        MagicWorldStaticBackground.draw(graphics, screen.width, screen.height);
        callback.cancel();
    }
}
