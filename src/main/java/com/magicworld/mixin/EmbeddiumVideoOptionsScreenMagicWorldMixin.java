package com.magicworld.mixin;

import com.google.common.collect.Multimap;
import com.magicworld.client.MagicWorldDistantHorizonsButton;
import com.magicworld.client.MagicWorldStaticBackground;
import com.mojang.logging.LogUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.function.Supplier;

@Mixin(targets = "org.embeddedt.embeddium.gui.EmbeddiumVideoOptionsScreen", remap = false)
public abstract class EmbeddiumVideoOptionsScreenMagicWorldMixin extends Screen {
    @Unique
    private static final Logger MAGICWORLD_LOGGER = LogUtils.getLogger();
    @Unique
    private static final int MAGICWORLD_ACTION_BUTTON_HEIGHT = 18;
    @Unique
    private static final int MAGICWORLD_BUTTON_GAP = 4;

    protected EmbeddiumVideoOptionsScreenMagicWorldMixin(Component title) {
        super(title);
    }

    @ModifyConstant(
            method = "<clinit>",
            constant = @Constant(stringValue = "embeddium"),
            require = 0,
            remap = false
    )
    private static String magicworld$replaceLogoNamespace(String original) {
        return "magicworld";
    }

    @ModifyConstant(
            method = "<clinit>",
            constant = @Constant(stringValue = "textures/embeddium/gui/logo_transparent.png"),
            require = 0,
            remap = false
    )
    private static String magicworld$replaceLogoPath(String original) {
        return "textures/gui/magic_world_graphics_logo.png";
    }

    @ModifyConstant(
            method = "m_88315_",
            constant = @Constant(intValue = 256),
            require = 0,
            remap = false
    )
    private int magicworld$useMagicWorldLogoSize(int original) {
        return 64;
    }

    @ModifyConstant(
            method = "<init>",
            constant = @Constant(stringValue = "Embeddium Options"),
            require = 0,
            remap = false
    )
    private static String magicworld$renameScreen(String original) {
        return "Magic World - Graficos";
    }

    @Inject(method = "m_280273_", at = @At("HEAD"), cancellable = true, require = 0, remap = false)
    private void magicworld$renderBackground(GuiGraphics graphics, CallbackInfo callback) {
        magicworld$drawOpaqueBackground(graphics);
        callback.cancel();
    }

    @Inject(method = "m_88315_", at = @At("HEAD"), require = 0, remap = false)
    private void magicworld$renderGuaranteedBackground(
            GuiGraphics graphics,
            int mouseX,
            int mouseY,
            float partialTick,
            CallbackInfo callback
    ) {
        magicworld$drawOpaqueBackground(graphics);
    }

    @Inject(method = "createShaderPackButton", at = @At("TAIL"), require = 0, remap = false)
    private void magicworld$addDistantHorizonsTab(
            Multimap<String, Object> tabs,
            CallbackInfo callback
    ) {
        try {
            Class<?> tabClass = Class.forName("org.embeddedt.embeddium.gui.frame.tab.Tab");
            Class<?> identifierClass = Class.forName(
                    "org.embeddedt.embeddium.client.gui.options.OptionIdentifier"
            );
            Object builder = tabClass.getMethod("createBuilder").invoke(null);
            Object identifier = identifierClass.getMethod("create", String.class, String.class)
                    .invoke(null, "distanthorizons", "settings");
            Supplier<Boolean> openDistantHorizons = () -> {
                MagicWorldDistantHorizonsButton.openDistantHorizonsScreen((Screen) (Object) this);
                return false;
            };

            builder = builder.getClass().getMethod("setTitle", Component.class)
                    .invoke(builder, Component.literal("Horizontes Distantes"));
            builder = builder.getClass().getMethod("setId", identifierClass)
                    .invoke(builder, identifier);
            builder = builder.getClass().getMethod("setOnSelectFunction", Supplier.class)
                    .invoke(builder, openDistantHorizons);
            Object distantHorizonsTab = builder.getClass().getMethod("build").invoke(builder);

            String shaderGroup = tabs.containsKey("oculus") ? "oculus" : "iris";
            tabs.put(shaderGroup, distantHorizonsTab);
        } catch (ReflectiveOperationException exception) {
            MAGICWORLD_LOGGER.warn("Magic World could not add the Distant Horizons graphics tab.", exception);
        }
    }

    @Inject(method = "m_7856_", at = @At("TAIL"), require = 0, remap = false)
    private void magicworld$hideDonationButtonsAfterInit(CallbackInfo callback) {
        magicworld$hideDonationButtons();
        magicworld$layoutFooterButtons();
    }

    @Inject(method = "parentBasicFrameBuilder", at = @At("HEAD"), require = 0, remap = false)
    private void magicworld$hideDonationButtonsBeforeFrameBuild(CallbackInfoReturnable<?> callback) {
        magicworld$hideDonationButtons();
    }

    @Unique
    private void magicworld$drawOpaqueBackground(GuiGraphics graphics) {
        MagicWorldStaticBackground.draw(graphics, this.width, this.height);
        graphics.fill(0, 0, this.width, this.height, 0xD9050A14);
    }

    @Unique
    private void magicworld$layoutFooterButtons() {
        int x = magicworld$contentX() + 5;
        int width = magicworld$leftPanelWidth() - 10;
        int gap = MAGICWORLD_BUTTON_GAP;
        int half = Math.max(40, (width - gap) / 2);
        int firstRowY = Math.max(28, this.height - 46);
        int secondRowY = Math.max(50, firstRowY + MAGICWORLD_ACTION_BUTTON_HEIGHT + gap);

        magicworld$setWidgetBounds("closeButton", x, firstRowY, half, MAGICWORLD_ACTION_BUTTON_HEIGHT);
        magicworld$setWidgetBounds(
                "applyButton",
                x + half + gap,
                firstRowY,
                width - half - gap,
                MAGICWORLD_ACTION_BUTTON_HEIGHT
        );
        magicworld$setWidgetBounds("undoButton", x, secondRowY, width, MAGICWORLD_ACTION_BUTTON_HEIGHT);
    }

    @Unique
    private void magicworld$hideDonationButtons() {
        magicworld$hideWidget("donateButton");
        magicworld$hideWidget("hideDonateButton");
    }

    @Unique
    private void magicworld$hideWidget(String fieldName) {
        magicworld$setWidgetVisible(fieldName, false);
        magicworld$setWidgetEnabled(fieldName, false);
        magicworld$setWidgetLabel(fieldName, Component.empty());
        magicworld$setWidgetBounds(fieldName, -1000, -1000, 0, 0);
    }

    @Unique
    private void magicworld$setWidgetEnabled(String fieldName, boolean enabled) {
        Object widget = magicworld$getFieldValue(fieldName);
        if (widget == null) {
            return;
        }
        try {
            Method setEnabled = widget.getClass().getMethod("setEnabled", boolean.class);
            setEnabled.invoke(widget, enabled);
        } catch (ReflectiveOperationException ignored) {
            // Optional Embeddium internals differ between versions.
        }
    }

    @Unique
    private void magicworld$setWidgetLabel(String fieldName, Component label) {
        Object widget = magicworld$getFieldValue(fieldName);
        if (widget == null) {
            return;
        }
        try {
            Method setLabel = widget.getClass().getMethod("setLabel", Component.class);
            setLabel.invoke(widget, label);
        } catch (ReflectiveOperationException ignored) {
            // Optional Embeddium internals differ between versions.
        }
    }

    @Unique
    private void magicworld$setWidgetVisible(String fieldName, boolean visible) {
        Object widget = magicworld$getFieldValue(fieldName);
        if (widget == null) {
            return;
        }
        try {
            Method setVisible = widget.getClass().getMethod("setVisible", boolean.class);
            setVisible.invoke(widget, visible);
        } catch (ReflectiveOperationException ignored) {
            // Optional Embeddium internals differ between versions.
        }
    }

    @Unique
    private void magicworld$setWidgetBounds(String fieldName, int x, int y, int width, int height) {
        Object widget = magicworld$getFieldValue(fieldName);
        if (widget == null) {
            return;
        }

        try {
            Class<?> dimClass = Class.forName("me.jellysquid.mods.sodium.client.util.Dim2i");
            Object dim = dimClass.getConstructor(int.class, int.class, int.class, int.class)
                    .newInstance(x, y, width, height);
            Class<?> type = widget.getClass();
            while (type != null) {
                try {
                    Field field = type.getDeclaredField("dim");
                    field.setAccessible(true);
                    field.set(widget, dim);
                    return;
                } catch (NoSuchFieldException ignored) {
                    type = type.getSuperclass();
                }
            }
        } catch (ReflectiveOperationException ignored) {
            // Optional Embeddium internals differ between versions.
        }
    }

    @Unique
    private Object magicworld$getFieldValue(String fieldName) {
        Class<?> type = this.getClass();
        while (type != null) {
            try {
                Field field = type.getDeclaredField(fieldName);
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
    private int magicworld$contentWidth() {
        return this.width > 550 ? Math.max(550, (int) (this.height * 1.25F)) : this.width;
    }

    @Unique
    private int magicworld$contentX() {
        return (this.width - magicworld$contentWidth()) / 2;
    }

    @Unique
    private int magicworld$leftPanelWidth() {
        return Math.max(120, (int) (magicworld$contentWidth() * 0.35F));
    }
}
