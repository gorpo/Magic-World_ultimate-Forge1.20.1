package com.magicworld.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(
        targets = {
                "org.embeddedt.embeddium.gui.frame.AbstractFrame",
                "org.embeddedt.embeddium.gui.frame.ScrollableFrame",
                "org.embeddedt.embeddium.gui.frame.components.SearchTextFieldComponent"
        },
        remap = false
)
public abstract class EmbeddiumLineColorMagicWorldMixin {
    private static final int MAGICWORLD_LINE = 0xFF00D9FF;

    @ModifyConstant(method = "m_88315_", constant = @Constant(intValue = -5592406), require = 0, remap = false)
    private int magicworld$replaceGrayLines(int original) {
        return MAGICWORLD_LINE;
    }
}
