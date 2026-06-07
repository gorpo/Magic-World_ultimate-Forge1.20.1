package com.magicworld.mixin;

import com.magicworld.client.MagicWorldStaticBackground;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.client.gui.screens.worldselection.WorldSelectionList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Field;

@Mixin(SelectWorldScreen.class)
public abstract class MagicWorldSelectWorldScreenBackgroundMixin {
    @Inject(
            method = {"init", "m_7856_"},
            at = @At("TAIL"),
            require = 0,
            remap = false
    )
    private void magicworld$init(CallbackInfo callback) {
        WorldSelectionList list = magicworld$getWorldSelectionList();
        if (list != null) {
            list.setRenderBackground(false);
            list.setRenderTopAndBottom(false);
        }
    }

    @Inject(
            method = {
                    "render(Lnet/minecraft/client/gui/GuiGraphics;IIF)V",
                    "m_88315_(Lnet/minecraft/client/gui/GuiGraphics;IIF)V"
            },
            at = @At("HEAD"),
            require = 0,
            remap = false
    )
    private void magicworld$render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick, CallbackInfo callback) {
        SelectWorldScreen screen = (SelectWorldScreen) (Object) this;
        MagicWorldStaticBackground.draw(graphics, screen.width, screen.height);
    }

    @Unique
    private WorldSelectionList magicworld$getWorldSelectionList() {
        for (String fieldName : new String[]{"list", "f_101336_"}) {
            try {
                Field field = SelectWorldScreen.class.getDeclaredField(fieldName);
                field.setAccessible(true);
                Object value = field.get(this);
                if (value instanceof WorldSelectionList worldSelectionList) {
                    return worldSelectionList;
                }
            } catch (ReflectiveOperationException ignored) {
                // Try the next runtime name.
            }
        }
        return null;
    }
}
