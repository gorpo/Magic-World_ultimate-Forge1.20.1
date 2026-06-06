package com.magicworld.mixin;

import com.magicworld.MagicWorld;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

@Mixin(targets = "me.jellysquid.mods.sodium.client.gui.widgets.FlatButtonWidget", remap = false)
public abstract class EmbeddiumDistantHorizonsTabIconMagicWorldMixin {
    @Unique
    private static final ResourceLocation MAGICWORLD_DH_ICON =
            new ResourceLocation(MagicWorld.MODID, "textures/gui/magic_world_graphics_logo.png");

    @Inject(method = "getLeftAlignedTextOffset", at = @At("RETURN"), cancellable = true, require = 0, remap = false)
    private void magicworld$reserveDistantHorizonsIcon(CallbackInfoReturnable<Integer> callback) {
        if (magicworld$isDistantHorizonsEntry()) {
            callback.setReturnValue(callback.getReturnValue() + 12);
        }
    }

    @Inject(method = "m_88315_", at = @At("TAIL"), require = 0, remap = false)
    private void magicworld$renderDistantHorizonsIcon(
            GuiGraphics graphics,
            int mouseX,
            int mouseY,
            float partialTick,
            CallbackInfo callback
    ) {
        if (!magicworld$isDistantHorizonsEntry()) {
            return;
        }

        Object dim = magicworld$getField("dim");
        int x = magicworld$invokeInt(dim, "x", 0) + 5;
        int y = magicworld$invokeInt(dim, "getCenterY", 0) - 5;
        graphics.blit(MAGICWORLD_DH_ICON, x, y, 10, 10, 0, 0, 64, 64, 64, 64);
    }

    @Unique
    private boolean magicworld$isDistantHorizonsEntry() {
        Object label = magicworld$getField("label");
        return label instanceof Component component
                && "Horizontes Distantes".equals(component.getString());
    }

    @Unique
    private Object magicworld$getField(String name) {
        Class<?> type = this.getClass();
        while (type != null) {
            try {
                Field field = type.getDeclaredField(name);
                field.setAccessible(true);
                return field.get(this);
            } catch (NoSuchFieldException ignored) {
                type = type.getSuperclass();
            } catch (IllegalAccessException ignored) {
                return null;
            }
        }
        return null;
    }

    @Unique
    private static int magicworld$invokeInt(Object target, String methodName, int fallback) {
        if (target == null) {
            return fallback;
        }
        try {
            Method method = target.getClass().getMethod(methodName);
            Object value = method.invoke(target);
            return value instanceof Number number ? number.intValue() : fallback;
        } catch (ReflectiveOperationException ignored) {
            return fallback;
        }
    }
}
