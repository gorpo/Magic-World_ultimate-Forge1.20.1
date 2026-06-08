package com.magicworld.mixin;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "forge.net.mca.client.gui.VillagerEditorScreen", remap = false)
public abstract class MagicWorldMcaPlayerModelMixin {
    private static final int MCA_PLAYER_MODEL_PLAYER = 1;

    @Unique
    private boolean magicworld$defaultGenderApplied;

    @Unique
    private boolean magicworld$defaultModelApplied;

    @Inject(method = "drawGender", at = @At("HEAD"), require = 0, remap = false)
    private void magicworld$selectMaleGenderByDefault(int x, int y, CallbackInfo callback) {
        if (!magicworld$defaultGenderApplied) {
            magicworld$defaultGenderApplied = true;
            magicworld$forceMaleGender();
            magicworld$forcePlayerModel();
        }
    }

    @Inject(method = "addModelSelectionWidgets", at = @At("HEAD"), require = 0, remap = false)
    private void magicworld$selectPlayerModelByDefault(int x, int y, CallbackInfo callback) {
        if (!magicworld$defaultModelApplied) {
            magicworld$defaultModelApplied = true;
            magicworld$forcePlayerModel();
        }
    }

    @Inject(
            method = "syncVillagerData",
            at = @At(
                    value = "INVOKE",
                    target = "Lforge/net/mca/cobalt/network/NetworkHandler;sendToServer(Lforge/net/mca/cobalt/network/Message;)V",
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

    private void magicworld$forceMaleGender() {
        Object villager = magicworld$getField(this, "villager");
        if (villager == null) {
            return;
        }

        Object genetics = magicworld$invokeNoArg(villager, "getGenetics");
        Object male = magicworld$enumConstant("forge.net.mca.entity.ai.relationship.Gender", "MALE");
        if (genetics != null && male != null) {
            magicworld$invokeSetGender(genetics, male);
            magicworld$setField(this, "filterGender", male);
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

    private static void magicworld$setField(Object target, String fieldName, Object value) {
        Class<?> type = target.getClass();
        while (type != null) {
            try {
                Field field = type.getDeclaredField(fieldName);
                field.setAccessible(true);
                field.set(target, value);
                return;
            } catch (ReflectiveOperationException ignored) {
                type = type.getSuperclass();
            }
        }
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

    private static Object magicworld$invokeNoArg(Object target, String methodName) {
        Class<?> type = target.getClass();
        while (type != null) {
            try {
                Method method = type.getDeclaredMethod(methodName);
                method.setAccessible(true);
                return method.invoke(target);
            } catch (ReflectiveOperationException ignored) {
                type = type.getSuperclass();
            }
        }
        return null;
    }

    private static void magicworld$invokeSetGender(Object genetics, Object gender) {
        Class<?> type = genetics.getClass();
        while (type != null) {
            try {
                Method method = type.getDeclaredMethod("setGender", gender.getClass());
                method.setAccessible(true);
                method.invoke(genetics, gender);
                return;
            } catch (ReflectiveOperationException ignored) {
                type = type.getSuperclass();
            }
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private static Object magicworld$enumConstant(String className, String constant) {
        try {
            Class<?> type = Class.forName(className);
            if (Enum.class.isAssignableFrom(type)) {
                return Enum.valueOf((Class<? extends Enum>) type.asSubclass(Enum.class), constant);
            }
        } catch (IllegalArgumentException | ReflectiveOperationException ignored) {
        }
        return null;
    }
}
