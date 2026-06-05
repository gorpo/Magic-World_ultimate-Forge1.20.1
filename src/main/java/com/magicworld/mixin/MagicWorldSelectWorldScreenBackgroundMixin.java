package com.magicworld.mixin;

import com.magicworld.client.MagicWorldStaticBackground;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.client.gui.screens.worldselection.WorldSelectionList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SelectWorldScreen.class)
public abstract class MagicWorldSelectWorldScreenBackgroundMixin {
    @Shadow
    private WorldSelectionList list;

    @Inject(method = "init", at = @At("TAIL"))
    private void magicworld$init(CallbackInfo callback) {
        if (list != null) {
            list.setRenderBackground(false);
            list.setRenderTopAndBottom(false);
        }
    }

    @Inject(method = "render", at = @At("HEAD"))
    private void magicworld$render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick, CallbackInfo callback) {
        SelectWorldScreen screen = (SelectWorldScreen) (Object) this;
        MagicWorldStaticBackground.draw(graphics, screen.width, screen.height);
    }
}
