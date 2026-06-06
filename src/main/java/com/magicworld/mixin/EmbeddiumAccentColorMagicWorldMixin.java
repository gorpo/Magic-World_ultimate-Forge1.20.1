package com.magicworld.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(
        targets = {
                "me.jellysquid.mods.sodium.client.gui.options.control.TickBoxControl$TickBoxControlElement",
                "me.jellysquid.mods.sodium.client.gui.widgets.FlatButtonWidget",
                "org.embeddedt.embeddium.gui.EmbeddiumVideoOptionsScreen",
                "org.embeddedt.embeddium.gui.frame.OptionPageFrame"
        },
        remap = false
)
public abstract class EmbeddiumAccentColorMagicWorldMixin {
    private static final int MAGICWORLD_CYAN = 0xFF00D9FF;

    @ModifyConstant(method = "m_88315_", constant = @Constant(intValue = -3179338), require = 0, remap = false)
    private int magicworld$replacePinkAccent(int original) {
        return MAGICWORLD_CYAN;
    }

    @ModifyConstant(method = "renderOptionTooltip", constant = @Constant(intValue = -3179338), require = 0, remap = false)
    private int magicworld$replaceTooltipAccent(int original) {
        return MAGICWORLD_CYAN;
    }

    @ModifyConstant(method = "m_88315_", constant = @Constant(intValue = -698654), require = 0, remap = false)
    private int magicworld$replaceShaderPink(int original) {
        return MAGICWORLD_CYAN;
    }

    @ModifyConstant(method = "m_88315_", constant = @Constant(intValue = -7019309), require = 0, remap = false)
    private int magicworld$replaceThemePrimary(int original) {
        return MAGICWORLD_CYAN;
    }

    @ModifyConstant(method = "m_88315_", constant = @Constant(intValue = -3342866), require = 0, remap = false)
    private int magicworld$replaceThemeLight(int original) {
        return MAGICWORLD_CYAN;
    }

    @ModifyConstant(method = "m_88315_", constant = @Constant(intValue = -8741218), require = 0, remap = false)
    private int magicworld$replaceThemeDark(int original) {
        return 0xCC00A9D6;
    }
}
