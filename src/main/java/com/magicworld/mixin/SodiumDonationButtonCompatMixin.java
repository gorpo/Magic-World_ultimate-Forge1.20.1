package com.magicworld.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net.caffeinemc.mods.sodium.client.gui.widgets.DonationButtonWidget", remap = false)
public abstract class SodiumDonationButtonCompatMixin {
    @ModifyVariable(
            method = "updateDisplay(Lnet/caffeinemc/mods/sodium/client/gui/VideoSettingsScreen;Z)V",
            at = @At("HEAD"),
            argsOnly = true,
            ordinal = 0,
            remap = false
    )
    private boolean magicworld$hideDonationButton(boolean showDonationButton) {
        return false;
    }

    @Inject(method = "getWidth", at = @At("HEAD"), cancellable = true, remap = false)
    private void magicworld$hideDonationButtonWidth(CallbackInfoReturnable<Integer> callback) {
        callback.setReturnValue(0);
    }
}
