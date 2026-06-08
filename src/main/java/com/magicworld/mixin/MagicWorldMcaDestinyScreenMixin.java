package com.magicworld.mixin;

import com.magicworld.client.MagicWorldStaticBackground;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "forge.net.mca.client.gui.DestinyScreen", remap = false)
public abstract class MagicWorldMcaDestinyScreenMixin {
    @Inject(method = "m_280273_", at = @At("HEAD"), cancellable = true, require = 0, remap = false)
    private void magicworld$renderMagicWorldBackground(GuiGraphics graphics, CallbackInfo callback) {
        Object self = this;
        if (self instanceof net.minecraft.client.gui.screens.Screen screen) {
            MagicWorldStaticBackground.draw(graphics, screen.width, screen.height);
            callback.cancel();
        }
    }

    @ModifyArg(
            method = "m_88315_",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/network/chat/Component;m_237115_(Ljava/lang/String;)Lnet/minecraft/network/chat/MutableComponent;",
                    remap = false
            ),
            index = 0,
            require = 0,
            remap = false
    )
    private String magicworld$translateWhoAreYou(String key) {
        if ("gui.destiny.whoareyou".equals(key)) {
            return "magicworld.gui.destiny.whoareyou";
        }
        return key;
    }
}
