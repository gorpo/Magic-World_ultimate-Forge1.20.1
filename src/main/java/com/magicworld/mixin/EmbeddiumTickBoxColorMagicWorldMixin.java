package com.magicworld.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(targets = "me.jellysquid.mods.sodium.client.gui.options.control.TickBoxControl$TickBoxControlElement", remap = false)
public abstract class EmbeddiumTickBoxColorMagicWorldMixin {
    private static final int MAGICWORLD_CYAN = 0xFF00D9FF;
    private static final int MAGICWORLD_CYAN_DISABLED = 0x9900D9FF;

    @ModifyConstant(method = "m_88315_", constant = @Constant(intValue = -3179338), require = 1, remap = false)
    private int magicworld$replaceSelectedColor(int original) {
        return MAGICWORLD_CYAN;
    }

    @ModifyConstant(method = "m_88315_", constant = @Constant(intValue = -1), require = 1, remap = false)
    private int magicworld$replaceUnselectedColor(int original) {
        return MAGICWORLD_CYAN;
    }

    @ModifyConstant(method = "m_88315_", constant = @Constant(intValue = -5592406), require = 1, remap = false)
    private int magicworld$replaceDisabledColor(int original) {
        return MAGICWORLD_CYAN_DISABLED;
    }
}
