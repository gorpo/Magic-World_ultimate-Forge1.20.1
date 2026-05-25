package com.magicworld.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashMap;
import java.util.Map;

@Mixin(RenderSetup.class)
public abstract class RenderSetupSamplerCompatMixin {
    @Inject(method = "getTextures", at = @At("RETURN"), cancellable = true)
    private void magicworld$provideMissingIrisSamplers(
            CallbackInfoReturnable<Map<String, RenderSetup.TextureAndSampler>> callback
    ) {
        Map<String, RenderSetup.TextureAndSampler> textures = callback.getReturnValue();
        if (textures.containsKey("Sampler1") && textures.containsKey("Sampler2")) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        Map<String, RenderSetup.TextureAndSampler> completedTextures = new HashMap<>(textures);
        completedTextures.putIfAbsent("Sampler1", new RenderSetup.TextureAndSampler(
                minecraft.gameRenderer.overlayTexture().getTextureView(),
                RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR)
        ));
        completedTextures.putIfAbsent("Sampler2", new RenderSetup.TextureAndSampler(
                minecraft.gameRenderer.lightmap(),
                RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR)
        ));
        callback.setReturnValue(completedTextures);
    }
}
