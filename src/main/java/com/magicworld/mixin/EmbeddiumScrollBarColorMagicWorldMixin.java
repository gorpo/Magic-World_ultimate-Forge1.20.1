package com.magicworld.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(targets = "org.embeddedt.embeddium.gui.frame.components.ScrollBarComponent", remap = false)
public abstract class EmbeddiumScrollBarColorMagicWorldMixin {
    private static final int MAGICWORLD_CYAN = 0xFF00D9FF;

    @ModifyConstant(method = "m_88315_", constant = @Constant(intValue = -5592406), require = 2, remap = false)
    private int magicworld$replaceTrackAndThumb(int original) {
        return MAGICWORLD_CYAN;
    }

    @ModifyConstant(method = "m_88315_", constant = @Constant(intValue = -1), require = 1, remap = false)
    private int magicworld$replaceFocusedBorder(int original) {
        return MAGICWORLD_CYAN;
    }
}
