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
    private static final String MAGICWORLD_TAB_FRAME_CLASS = "org.embeddedt.embeddium.gui.frame.tab.TabFrame";

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
        graphics.fill(0, 0, this.width, this.height, 0xF8050A14);
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
            graphics.fill(
                    buttonX - 4,
                    buttonY - 4,
                    buttonX + buttonWidth + 4,
                    buttonY + MAGICWORLD_DH_BUTTON_HEIGHT + 4,
                    0xCC00111F
            );
            graphics.renderOutline(
                    buttonX - 4,
                    buttonY - 4,
                    buttonWidth + 8,
                    MAGICWORLD_DH_BUTTON_HEIGHT + 8,
                    0xCC00A9D6
            );
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
        int buttonX = magicworld$distantHorizonsButtonX();
        int buttonY = magicworld$distantHorizonsButtonY();
        int buttonWidth = magicworld$distantHorizonsButtonWidth();

        if (buttonWidth < 70) {
            return;
        }

        if (this.magicworld$distantHorizonsButton != null) {
            this.magicworld$distantHorizonsButton.setX(buttonX);
            this.magicworld$distantHorizonsButton.setY(buttonY);
            this.magicworld$distantHorizonsButton.setWidth(buttonWidth);
        } else {
            this.magicworld$distantHorizonsButton = this.addRenderableWidget(new MagicWorldDistantHorizonsButton(
                    buttonX,
                    buttonY,
                    buttonWidth,
                    MAGICWORLD_DH_BUTTON_HEIGHT,
                    (Screen) (Object) this
            ));
        }
    }

    private int magicworld$distantHorizonsButtonX() {
        Object tabSection = magicworld$getTabSection();
        return tabSection != null ? magicworld$invokeInt(tabSection, "x", magicworld$contentX()) + 5 : magicworld$contentX() + 5;
    }

    private int magicworld$distantHorizonsButtonY() {
        Object tabSection = magicworld$getTabSection();
        if (tabSection == null) {
            return Math.max(8, this.height - MAGICWORLD_LEFT_FOOTER_HEIGHT);
        }

        Object tabSectionInner = magicworld$getTabSectionInner();
        int sectionY = magicworld$invokeInt(tabSection, "y", 8);
        int innerHeight = magicworld$invokeInt(tabSectionInner, "height", 0);
        int tabLimitY = innerHeight > 0
                ? sectionY + innerHeight
                : magicworld$invokeInt(tabSection, "getLimitY", sectionY + 156);
        int minY = sectionY + MAGICWORLD_BUTTON_GAP;
        int actionRowY = Math.max(28, this.height - 46);
        int maxY = Math.max(minY, actionRowY - MAGICWORLD_DH_BUTTON_HEIGHT - MAGICWORLD_BUTTON_GAP - 2);

        return Math.min(Math.max(tabLimitY + MAGICWORLD_BUTTON_GAP, minY), maxY);
    }

    private int magicworld$distantHorizonsButtonWidth() {
        Object tabSection = magicworld$getTabSection();
        return tabSection != null
                ? magicworld$invokeInt(tabSection, "width", magicworld$leftPanelWidth()) - 10
                : magicworld$leftPanelWidth() - 10;
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
        magicworld$hideWidget("donateButton");
        magicworld$hideWidget("hideDonateButton");
    }

    private void magicworld$hideWidget(String fieldName) {
        magicworld$setWidgetVisible(fieldName, false);
        magicworld$setWidgetEnabled(fieldName, false);
        magicworld$setWidgetLabel(fieldName, Component.empty());
        magicworld$setWidgetBounds(fieldName, -1000, -1000, 0, 0);
    }

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
        return magicworld$getFieldValue(this, fieldName);
    }

    private Object magicworld$getTabSection() {
        Object tabFrame = magicworld$getTabFrame();
        return tabFrame == null ? null : magicworld$getFieldValue(tabFrame, "tabSection");
    }

    private Object magicworld$getTabSectionInner() {
        Object tabFrame = magicworld$getTabFrame();
        return tabFrame == null ? null : magicworld$getFieldValue(tabFrame, "tabSectionInner");
    }

    private Object magicworld$getTabFrame() {
        Object frame = magicworld$getFieldValue("frame");
        return magicworld$findByClassName(frame, MAGICWORLD_TAB_FRAME_CLASS, 0);
    }

    private Object magicworld$findByClassName(Object target, String className, int depth) {
        if (target == null || depth > 4) {
            return null;
        }
        if (target.getClass().getName().equals(className)) {
            return target;
        }

        Object children = magicworld$getFieldValue(target, "children");
        if (children instanceof Iterable<?> iterable) {
            for (Object child : iterable) {
                Object found = magicworld$findByClassName(child, className, depth + 1);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    private int magicworld$invokeInt(Object target, String methodName, int fallback) {
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

    private Object magicworld$getFieldValue(Object target, String fieldName) {
        if (target == null) {
            return null;
        }
        Class<?> type = this.getClass();
        if (target != this) {
            type = target.getClass();
        }
        while (type != null) {
            try {
                Field field = type.getDeclaredField(fieldName);
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
