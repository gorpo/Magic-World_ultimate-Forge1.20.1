package com.magicworld.mixin;

import com.magicworld.client.MagicWorldEntityCulling;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityRenderer.class)
public abstract class MagicWorldEntityCullingMixin {
    @Inject(
            method = {
                    "shouldRender(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/client/renderer/culling/Frustum;DDD)Z",
                    "m_5523_(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/client/renderer/culling/Frustum;DDD)Z"
            },
            at = @At("HEAD"),
            cancellable = true,
            require = 0,
            remap = false
    )
    private void magicworld$skipOccludedEntity(
            Entity entity,
            Frustum frustum,
            double cameraX,
            double cameraY,
            double cameraZ,
            CallbackInfoReturnable<Boolean> callback
    ) {
        if (MagicWorldEntityCulling.shouldSkip(entity, cameraX, cameraY, cameraZ)) {
            callback.setReturnValue(false);
        }
    }
}
