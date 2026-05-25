package com.magicworld.mixin;

import net.minecraft.client.renderer.texture.TextureManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.caffeinemc.mods.sodium.client.gui.SodiumConfigBuilder", remap = false)
public abstract class SodiumConfigBuilderMagicWorldMixin {
    private static final int MAGICWORLD_THEME = 0xFF00D9FF;
    private static final int MAGICWORLD_THEME_LIGHT = 0xFF00D9FF;
    private static final int MAGICWORLD_THEME_DARK = 0xFF00D9FF;

    @Inject(method = "registerIcon", at = @At("HEAD"), cancellable = true, remap = false)
    private static void magicworld$skipOriginalLogo(TextureManager textureManager, CallbackInfo callback) {
        callback.cancel();
    }

    @ModifyConstant(method = "createModOptionsBuilder", constant = @Constant(stringValue = "Sodium"), remap = false)
    private static String magicworld$renamePerformanceMenu(String original) {
        return "Magic World";
    }

    @ModifyConstant(method = "buildFullConfig", constant = @Constant(intValue = -7019309), remap = false)
    private int magicworld$themePrimary(int original) {
        return MAGICWORLD_THEME;
    }

    @ModifyConstant(method = "buildFullConfig", constant = @Constant(intValue = -3342866), remap = false)
    private int magicworld$themeLight(int original) {
        return MAGICWORLD_THEME_LIGHT;
    }

    @ModifyConstant(method = "buildFullConfig", constant = @Constant(intValue = -8741218), remap = false)
    private int magicworld$themeDark(int original) {
        return MAGICWORLD_THEME_DARK;
    }
}
