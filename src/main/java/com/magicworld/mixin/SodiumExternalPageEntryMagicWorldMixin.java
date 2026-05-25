package com.magicworld.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(targets = "net.caffeinemc.mods.sodium.client.gui.widgets.PageListWidget$ExternalPageEntryWidget", remap = false)
public abstract class SodiumExternalPageEntryMagicWorldMixin {
    @ModifyConstant(method = "<init>", constant = @Constant(stringValue = "?"), require = 0, remap = false)
    private String magicworld$removeExternalPrefix(String original) {
        return "";
    }
}
