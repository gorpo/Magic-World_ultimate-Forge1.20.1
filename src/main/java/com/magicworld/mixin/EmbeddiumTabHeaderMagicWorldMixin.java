package com.magicworld.mixin;

import com.magicworld.MagicWorld;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "org.embeddedt.embeddium.gui.frame.tab.TabHeaderWidget", remap = false)
public abstract class EmbeddiumTabHeaderMagicWorldMixin {
    private static final ResourceLocation MAGICWORLD_ICON =
            new ResourceLocation(MagicWorld.MODID, "textures/gui/embeddium_magicworld_icon.png");

    @Shadow
    @Final
    @Mutable
    private ResourceLocation logoTexture;

    @Inject(method = "getLabel", at = @At("HEAD"), cancellable = true, require = 0, remap = false)
    private static void magicworld$renameHeader(
            String modId,
            CallbackInfoReturnable<MutableComponent> callback
    ) {
        if ("sodium".equals(modId) || "embeddium".equals(modId)) {
            callback.setReturnValue(Component.literal("Magic World").withStyle(style -> style.withUnderlined(true)));
        } else if ("oculus".equals(modId) || "iris".equals(modId)) {
            callback.setReturnValue(Component.literal("Magic World Shaders").withStyle(style -> style.withUnderlined(true)));
        }
    }

    @Inject(method = "<init>", at = @At("TAIL"), require = 0, remap = false)
    private void magicworld$replaceHeaderIcon(CallbackInfo callback) {
        this.logoTexture = MAGICWORLD_ICON;
    }
}
