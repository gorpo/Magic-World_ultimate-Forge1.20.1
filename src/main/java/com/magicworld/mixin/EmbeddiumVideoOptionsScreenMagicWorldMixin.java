package com.magicworld.mixin;

import com.magicworld.client.MagicWorldDistantHorizonsButton;
import com.magicworld.client.MagicWorldStaticBackground;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
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

@Mixin(targets = "org.embeddedt.embeddium.gui.EmbeddiumVideoOptionsScreen", remap = false)
public abstract class EmbeddiumVideoOptionsScreenMagicWorldMixin extends Screen {
    @Unique
    private static final int MAGICWORLD_LEFT_FOOTER_HEIGHT = 68;
    @Unique
    private static final int MAGICWORLD_DH_BUTTON_HEIGHT = 18;
    @Unique
    private static final int MAGICWORLD_ACTION_BUTTON_HEIGHT = 18;
    @Unique
    private static final int MAGICWORLD_BUTTON_GAP = 4;

    @Unique
    private MagicWorldDistantHorizonsButton magicworld$distantHorizonsButton;

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
        MagicWorldStaticBackground.draw(graphics, this.width, this.height);
        graphics.fill(0, 0, this.width, this.height, 0xE6050A14);
        callback.cancel();
    }

    @Inject(method = "m_7856_", at = @At("TAIL"), require = 0, remap = false)
    private void magicworld$hideDonationButtonsAfterInit(CallbackInfo callback) {
        magicworld$hideDonationButtons();
        magicworld$layoutFooterButtons();
        magicworld$ensureDistantHorizonsButton();
    }

    @Inject(method = "parentBasicFrameBuilder", at = @At("HEAD"), require = 0, remap = false)
    private void magicworld$hideDonationButtonsBeforeFrameBuild(CallbackInfoReturnable<?> callback) {
        magicworld$hideDonationButtons();
    }

    @Inject(method = "m_88315_", at = @At("TAIL"), require = 0, remap = false)
    private void magicworld$renderDistantHorizonsButton(
            GuiGraphics graphics,
            int mouseX,
            int mouseY,
            float partialTick,
            CallbackInfo callback
    ) {
        magicworld$ensureDistantHorizonsButton();
        if (this.magicworld$distantHorizonsButton != null) {
            int buttonX = this.magicworld$distantHorizonsButton.getX();
            int buttonY = this.magicworld$distantHorizonsButton.getY();
            int buttonWidth = this.magicworld$distantHorizonsButton.getWidth();
            graphics.fill(buttonX - 4, buttonY - 4, buttonX + buttonWidth + 4, this.height - 4, 0xCC00111F);
            graphics.renderOutline(buttonX - 4, buttonY - 4, buttonWidth + 8, this.height - buttonY, 0xCC00A9D6);
            this.magicworld$distantHorizonsButton.render(graphics, mouseX, mouseY, partialTick);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        magicworld$ensureDistantHorizonsButton();
        if (this.magicworld$distantHorizonsButton != null
                && this.magicworld$distantHorizonsButton.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void magicworld$ensureDistantHorizonsButton() {
        if (this.magicworld$distantHorizonsButton != null) {
            return;
        }

        int buttonX = magicworld$contentX() + 5;
        int buttonY = Math.max(8, this.height - MAGICWORLD_LEFT_FOOTER_HEIGHT);
        int buttonWidth = magicworld$leftPanelWidth() - 10;

        if (buttonWidth >= 70) {
            this.magicworld$distantHorizonsButton = this.addRenderableWidget(new MagicWorldDistantHorizonsButton(
                    buttonX,
                    buttonY,
                    buttonWidth,
                    MAGICWORLD_DH_BUTTON_HEIGHT,
                    (Screen) (Object) this
            ));
        }
    }

    private void magicworld$layoutFooterButtons() {
        int x = magicworld$contentX() + 5;
        int width = magicworld$leftPanelWidth() - 10;
        int gap = MAGICWORLD_BUTTON_GAP;
        int half = Math.max(40, (width - gap) / 2);
        int firstRowY = Math.max(28, this.height - 46);
        int secondRowY = Math.max(50, firstRowY + MAGICWORLD_ACTION_BUTTON_HEIGHT + gap);

        magicworld$setWidgetBounds("closeButton", x, firstRowY, half, MAGICWORLD_ACTION_BUTTON_HEIGHT);
        magicworld$setWidgetBounds("applyButton", x + half + gap, firstRowY, width - half - gap, MAGICWORLD_ACTION_BUTTON_HEIGHT);
        magicworld$setWidgetBounds("undoButton", x, secondRowY, width, MAGICWORLD_ACTION_BUTTON_HEIGHT);
    }

    private void magicworld$hideDonationButtons() {
        magicworld$setWidgetVisible("donateButton", false);
        magicworld$setWidgetVisible("hideDonateButton", false);
    }

    private void magicworld$setWidgetVisible(String fieldName, boolean visible) {
        Class<?> type = this.getClass();
        while (type != null) {
            try {
                Field field = type.getDeclaredField(fieldName);
                field.setAccessible(true);
                Object widget = field.get(this);
                if (widget == null) {
                    return;
                }

                Method setVisible = widget.getClass().getMethod("setVisible", boolean.class);
                setVisible.invoke(widget, visible);
                return;
            } catch (NoSuchFieldException ignored) {
                type = type.getSuperclass();
            } catch (ReflectiveOperationException ignored) {
                return;
            }
        }
    }

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

    private int magicworld$contentWidth() {
        return this.width > 550 ? Math.max(550, (int) (this.height * 1.25F)) : this.width;
    }

    private int magicworld$contentX() {
        return (this.width - magicworld$contentWidth()) / 2;
    }

    private int magicworld$leftPanelWidth() {
        return Math.max(120, (int) (magicworld$contentWidth() * 0.35F));
    }
}
