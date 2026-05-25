package com.magicworld.mixin;

import net.minecraft.client.renderer.CubeMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(CubeMap.class)
public abstract class CubeMapMagicWorldDepthMixin {
    @ModifyConstant(
            method = "render",
            constant = @Constant(floatValue = 85.0F)
    )
    private float magicworld$widerTitlePanoramaFov(
            float original
    ) {
        return 108.0F;
    }
}
