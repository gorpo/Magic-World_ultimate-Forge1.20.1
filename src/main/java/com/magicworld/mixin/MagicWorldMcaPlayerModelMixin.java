package com.magicworld.mixin;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "fabric.net.mca.client.gui.VillagerEditorScreen", remap = false)
public abstract class MagicWorldMcaPlayerModelMixin {
    private static final int MCA_PLAYER_MODEL_PLAYER = 1;

    @Inject(method = "addModelSelectionWidgets", at = @At("HEAD"), require = 0, remap = false)
    private void magicworld$selectPlayerModelByDefault(int x, int y, CallbackInfo callback) {
        magicworld$forcePlayerModel();
    }

    @Inject(
            method = "syncVillagerData",
            at = @At(
                    value = "INVOKE",
                    target = "Lfabric/net/mca/cobalt/network/NetworkHandler;sendToServer(Lfabric/net/mca/cobalt/network/Message;)V",
                    shift = At.Shift.BEFORE
            ),
            require = 0,
            remap = false
    )
    private void magicworld$forcePlayerModelBeforeSync(CallbackInfo callback) {
        magicworld$forcePlayerModel();
    }

    private void magicworld$forcePlayerModel() {
        Object villagerData = magicworld$getField(this, "villagerData");
        if (villagerData != null) {
            magicworld$putInt(villagerData, "playerModel", MCA_PLAYER_MODEL_PLAYER);
        }
    }

    private static Object magicworld$getField(Object target, String fieldName) {
        Class<?> type = target.getClass();
        while (type != null) {
            try {
                Field field = type.getDeclaredField(fieldName);
                field.setAccessible(true);
                return field.get(target);
            } catch (ReflectiveOperationException ignored) {
                type = type.getSuperclass();
            }
        }
        return null;
    }

    private static void magicworld$putInt(Object compoundTag, String key, int value) {
        for (String methodName : new String[] { "putInt", "method_10569" }) {
            if (magicworld$invokePutInt(compoundTag, methodName, key, value)) {
                return;
            }
        }
    }

    private static boolean magicworld$invokePutInt(Object target, String methodName, String key, int value) {
        Class<?> type = target.getClass();
        while (type != null) {
            try {
                Method method = type.getDeclaredMethod(methodName, String.class, int.class);
                method.setAccessible(true);
                method.invoke(target, key, value);
                return true;
            } catch (ReflectiveOperationException ignored) {
                type = type.getSuperclass();
            }
        }
        return false;
    }
}
