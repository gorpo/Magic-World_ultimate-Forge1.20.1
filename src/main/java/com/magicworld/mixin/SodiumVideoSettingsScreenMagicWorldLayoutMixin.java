package com.magicworld.mixin;

import com.magicworld.client.MagicWorldDistantHorizonsButton;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

@Mixin(targets = "net.caffeinemc.mods.sodium.client.gui.VideoSettingsScreen", remap = false)
public abstract class SodiumVideoSettingsScreenMagicWorldLayoutMixin extends Screen {
    private static final int MAGICWORLD_LEFT_FOOTER_HEIGHT = 68;
    private static final int MAGICWORLD_RIGHT_FOOTER_HEIGHT = 0;
    private static final int MAGICWORLD_SIDE_MARGIN = 5;
    private static final int MAGICWORLD_DH_BUTTON_HEIGHT = 18;
    private static final int MAGICWORLD_ACTION_BUTTON_HEIGHT = 18;
    private static final int MAGICWORLD_BUTTON_GAP = 4;

    protected SodiumVideoSettingsScreenMagicWorldLayoutMixin(Component title) {
        super(title);
    }

    @ModifyArgs(
            method = "rebuild",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/caffeinemc/mods/sodium/client/util/Dim2i;<init>(IIII)V",
                    ordinal = 1
            ),
            remap = false
    )
    private void magicworld$reserveLeftFooter(Args args) {
        int height = (Integer) args.get(3);
        args.set(3, Math.max(72, height - MAGICWORLD_LEFT_FOOTER_HEIGHT));
    }

    @ModifyArgs(
            method = "rebuild",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/caffeinemc/mods/sodium/client/util/Dim2i;<init>(IIII)V",
                    ordinal = 2
            ),
            remap = false
    )
    private void magicworld$expandRightPanel(Args args) {
        int x = (Integer) args.get(0);
        int height = (Integer) args.get(3);

        args.set(2, Math.max(222, this.width - x - MAGICWORLD_SIDE_MARGIN));
        args.set(3, Math.max(80, height - MAGICWORLD_RIGHT_FOOTER_HEIGHT));
    }

    @ModifyArgs(
            method = "rebuildActionButtons",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/caffeinemc/mods/sodium/client/util/Dim2i;<init>(IIII)V",
                    ordinal = 0
            ),
            remap = false
    )
    private void magicworld$moveCloseButtonToLeftFooter(Args args) {
        int x = magicworld$getFooterX((Integer) args.get(0));
        int y = magicworld$getActionRowY((Integer) args.get(1));
        int halfWidth = magicworld$getHalfFooterWidth((Integer) args.get(2));

        args.set(0, x);
        args.set(1, y);
        args.set(2, halfWidth);
        args.set(3, MAGICWORLD_ACTION_BUTTON_HEIGHT);
    }

    @ModifyArgs(
            method = "rebuildActionButtons",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/caffeinemc/mods/sodium/client/util/Dim2i;<init>(IIII)V",
                    ordinal = 1
            ),
            remap = false
    )
    private void magicworld$moveApplyButtonToLeftFooter(Args args) {
        int x = magicworld$getFooterX((Integer) args.get(0));
        int halfWidth = magicworld$getHalfFooterWidth((Integer) args.get(2));
        int y = magicworld$getActionRowY((Integer) args.get(1));

        args.set(0, x + halfWidth + MAGICWORLD_BUTTON_GAP);
        args.set(1, y);
        args.set(2, halfWidth);
        args.set(3, MAGICWORLD_ACTION_BUTTON_HEIGHT);
    }

    @ModifyArgs(
            method = "rebuildActionButtons",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/caffeinemc/mods/sodium/client/util/Dim2i;<init>(IIII)V",
                    ordinal = 2
            ),
            remap = false
    )
    private void magicworld$moveUndoButtonToLeftFooter(Args args) {
        int x = magicworld$getFooterX((Integer) args.get(0));
        int y = magicworld$getActionRowY((Integer) args.get(1))
                + MAGICWORLD_ACTION_BUTTON_HEIGHT
                + MAGICWORLD_BUTTON_GAP;

        args.set(0, x);
        args.set(1, y);
        args.set(2, magicworld$getFooterWidth((Integer) args.get(2)));
        args.set(3, MAGICWORLD_ACTION_BUTTON_HEIGHT);
    }

    @Inject(method = "rebuild", at = @At("TAIL"), remap = false)
    private void magicworld$addDistantHorizonsButton(CallbackInfo callback) {
        Object pageList = magicworld$getField("pageList");
        if (pageList == null) {
            return;
        }

        int x = magicworld$getFooterX(MAGICWORLD_SIDE_MARGIN);
        int y = magicworld$invokeInt(pageList, "getLimitY", this.height - 26) + 2;
        int width = magicworld$getFooterWidth(116);

        if (y + MAGICWORLD_DH_BUTTON_HEIGHT > this.height - 4) {
            y = this.height - MAGICWORLD_DH_BUTTON_HEIGHT - 4;
        }

        if (width < 70 || y < 0) {
            return;
        }

        this.addRenderableWidget(
                new MagicWorldDistantHorizonsButton(
                        x,
                        y,
                        width,
                        MAGICWORLD_DH_BUTTON_HEIGHT,
                        (Screen) (Object) this
                )
        );
    }

    @Override
    @Shadow
    public abstract <T extends GuiEventListener & Renderable & NarratableEntry> T addRenderableWidget(T widget);

    private Object magicworld$getField(String name) {
        Class<?> type = ((Object) this).getClass();

        while (type != null) {
            try {
                Field field = type.getDeclaredField(name);
                field.setAccessible(true);
                return field.get(this);
            } catch (NoSuchFieldException ignored) {
                type = type.getSuperclass();
            } catch (IllegalAccessException exception) {
                return null;
            }
        }

        return null;
    }

    private int magicworld$getFooterX(int fallback) {
        Object pageList = magicworld$getField("pageList");
        return magicworld$invokeInt(pageList, "getX", fallback) + 2;
    }

    private int magicworld$getFooterWidth(int fallback) {
        Object pageList = magicworld$getField("pageList");
        return Math.max(72, magicworld$invokeInt(pageList, "getWidth", fallback) - 9);
    }

    private int magicworld$getHalfFooterWidth(int fallback) {
        int width = magicworld$getFooterWidth(fallback * 2 + MAGICWORLD_BUTTON_GAP);
        return Math.max(34, (width - MAGICWORLD_BUTTON_GAP) / 2);
    }

    private int magicworld$getActionRowY(int fallback) {
        Object pageList = magicworld$getField("pageList");
        int y = magicworld$invokeInt(pageList, "getLimitY", fallback)
                + MAGICWORLD_DH_BUTTON_HEIGHT
                + MAGICWORLD_BUTTON_GAP
                + 2;
        int maxY = this.height
                - (MAGICWORLD_ACTION_BUTTON_HEIGHT * 2)
                - MAGICWORLD_BUTTON_GAP
                - 4;

        return Math.min(y, maxY);
    }

    private static int magicworld$invokeInt(Object target, String methodName, int fallback) {
        if (target == null) {
            return fallback;
        }

        try {
            Method method = target.getClass().getMethod(methodName);
            Object value = method.invoke(target);

            if (value instanceof Number number) {
                return number.intValue();
            }
        } catch (ReflectiveOperationException exception) {
            return fallback;
        }

        return fallback;
    }
}
