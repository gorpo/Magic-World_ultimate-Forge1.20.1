package com.magicworld.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Mixin(targets = "org.embeddedt.embeddium.gui.frame.tab.TabFrame", remap = false)
public abstract class EmbeddiumTabFrameCircularScrollMagicWorldMixin {
    @Inject(method = "m_6050_", at = @At("HEAD"), cancellable = true, require = 0, remap = false)
    private void magicworld$cycleTabsAtPageLimit(
            double mouseX,
            double mouseY,
            double delta,
            CallbackInfoReturnable<Boolean> callback
    ) {
        Object frameSection = magicworld$getField(this, "frameSection");
        if (!magicworld$containsCursor(frameSection, mouseX, mouseY)) {
            return;
        }

        Object selectedFrame = magicworld$getField(this, "selectedFrame");
        Object scrollBar = magicworld$getField(selectedFrame, "verticalScrollBar");
        if (scrollBar == null) {
            magicworld$cycle(delta, callback);
            return;
        }

        int offset = magicworld$getInt(scrollBar, "offset", magicworld$invokeInt(scrollBar, "getOffset", 0));
        int maxOffset = magicworld$getInt(scrollBar, "maxScrollBarOffset", 0);
        if (maxOffset <= 0) {
            magicworld$cycle(delta, callback);
            return;
        }
        if ((delta < 0.0D && offset >= maxOffset) || (delta > 0.0D && offset <= 0)) {
            magicworld$cycle(delta, callback);
        }
    }

    @Unique
    private void magicworld$cycle(double delta, CallbackInfoReturnable<Boolean> callback) {
        Object selected = magicworld$getField(this, "selectedTab");
        Object multimap = magicworld$getField(this, "tabs");
        if (selected == null || multimap == null) {
            return;
        }

        try {
            Method valuesMethod = multimap.getClass().getMethod("values");
            Object values = valuesMethod.invoke(multimap);
            if (!(values instanceof Collection<?> collection) || collection.isEmpty()) {
                return;
            }

            List<?> tabs = new ArrayList<>(collection);
            int current = tabs.indexOf(selected);
            int direction = delta < 0.0D ? 1 : -1;
            Object next = tabs.get(Math.floorMod(current + direction, tabs.size()));
            Method setTab = this.getClass().getMethod(
                    "setTab",
                    Class.forName("org.embeddedt.embeddium.gui.frame.tab.Tab")
            );
            setTab.invoke(this, next);
            callback.setReturnValue(true);
        } catch (ReflectiveOperationException ignored) {
            // Optional Embeddium internals differ between versions.
        }
    }

    @Unique
    private static boolean magicworld$containsCursor(Object dim, double mouseX, double mouseY) {
        if (dim == null) {
            return false;
        }
        try {
            Method contains = dim.getClass().getMethod("containsCursor", double.class, double.class);
            return Boolean.TRUE.equals(contains.invoke(dim, mouseX, mouseY));
        } catch (ReflectiveOperationException ignored) {
            return false;
        }
    }

    @Unique
    private static int magicworld$getInt(Object target, String name, int fallback) {
        Object value = magicworld$getField(target, name);
        return value instanceof Number number ? number.intValue() : fallback;
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

    @Unique
    private static Object magicworld$getField(Object target, String name) {
        if (target == null) {
            return null;
        }
        Class<?> type = target.getClass();
        while (type != null) {
            try {
                Field field = type.getDeclaredField(name);
                field.setAccessible(true);
                return field.get(target);
            } catch (NoSuchFieldException ignored) {
                type = type.getSuperclass();
            } catch (IllegalAccessException ignored) {
                return null;
            }
        }
        return null;
    }
}
