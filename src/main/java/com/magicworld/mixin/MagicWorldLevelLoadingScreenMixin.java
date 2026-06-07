package com.magicworld.mixin;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.LevelLoadingScreen;
import net.minecraft.server.level.progress.StoringChunkProgressListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LevelLoadingScreen.class)
public abstract class MagicWorldLevelLoadingScreenMixin {
    @Redirect(
        method = {
            "render(Lnet/minecraft/client/gui/GuiGraphics;IIF)V",
            "m_88315_(Lnet/minecraft/client/gui/GuiGraphics;IIF)V"
        },
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/screens/LevelLoadingScreen;renderChunks(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/server/level/progress/StoringChunkProgressListener;IIII)V"
        ),
        require = 0,
        remap = false
    )
    private void magicworld$skipOfficialChunkProgressMap(
            GuiGraphics graphics,
            StoringChunkProgressListener progressListener,
            int centerX,
            int centerY,
            int cellSize,
            int cellGap
    ) {
    }

    @Redirect(
        method = {
            "render(Lnet/minecraft/client/gui/GuiGraphics;IIF)V",
            "m_88315_(Lnet/minecraft/client/gui/GuiGraphics;IIF)V"
        },
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/screens/LevelLoadingScreen;m_96149_(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/server/level/progress/StoringChunkProgressListener;IIII)V"
        ),
        require = 0,
        remap = false
    )
    private void magicworld$skipSrgChunkProgressMap(
            GuiGraphics graphics,
            StoringChunkProgressListener progressListener,
            int centerX,
            int centerY,
            int cellSize,
            int cellGap
    ) {
    }
}
