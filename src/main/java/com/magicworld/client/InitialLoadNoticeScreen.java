package com.magicworld.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class InitialLoadNoticeScreen extends Screen {
    private static final long COMPLETE_CLOSE_DELAY_MS = 5000L;
    private static int currentProgress;
    private static String currentMessage = "Preparando o carregamento inicial...";
    private static boolean currentComplete;
    private static long completedAtMs = -1L;

    public InitialLoadNoticeScreen() {
        super(Component.literal("Carregamento Magic World"));
    }

    public static void resetProgress() {
        currentProgress = 0;
        currentMessage = "Preparando o carregamento inicial...";
        currentComplete = false;
        completedAtMs = -1L;
    }

    public static void updateProgress(Minecraft minecraft, int progress, String message, boolean complete) {
        currentProgress = Math.max(0, Math.min(100, progress));
        currentMessage = message == null || message.isBlank() ? currentMessage : message;
        currentComplete = complete || currentProgress >= 100;

        if (currentComplete && completedAtMs < 0L) {
            completedAtMs = System.currentTimeMillis();
        }
    }

    @Override
    public void tick() {
        if (currentComplete
                && minecraft != null
                && minecraft.player != null
                && minecraft.level != null
                && completedAtMs > 0L
                && System.currentTimeMillis() - completedAtMs >= COMPLETE_CLOSE_DELAY_MS) {
            minecraft.setScreen(null);
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        int panelWidth = Math.min(420, width - 32);
        int panelHeight = 220;
        int left = width / 2 - panelWidth / 2;
        int top = Math.max(18, height / 2 - 112);
        int progressLeft = left + 28;
        int progressTop = top + 148;
        int progressWidth = panelWidth - 56;
        int filledWidth = progressWidth * currentProgress / 100;
        int logoWidth = Math.min(260, Math.max(160, panelWidth - 120));
        int logoHeight = logoWidth * MagicWorldStaticBackground.LOGO_HEIGHT / MagicWorldStaticBackground.LOGO_WIDTH;
        int logoX = width / 2 - logoWidth / 2;
        int logoY = top + 18;

        MagicWorldStaticBackground.draw(graphics, width, height);
        graphics.fill(0, 0, width, height, 0x66030611);
        graphics.fill(left, top, left + panelWidth, top + panelHeight, 0xE8050916);
        graphics.fill(left + 4, top + 4, left + panelWidth - 4, top + panelHeight - 4, 0xAA101B2B);
        graphics.renderOutline(left, top, panelWidth, panelHeight, 0xAADDAD55);
        graphics.renderOutline(left + 5, top + 5, panelWidth - 10, panelHeight - 10, 0x55316B9F);
        graphics.blit(
                MagicWorldStaticBackground.FULL_LOGO,
                logoX,
                logoY,
                logoWidth,
                logoHeight,
                0,
                0,
                MagicWorldStaticBackground.LOGO_WIDTH,
                MagicWorldStaticBackground.LOGO_HEIGHT,
                MagicWorldStaticBackground.LOGO_WIDTH,
                MagicWorldStaticBackground.LOGO_HEIGHT
        );
        graphics.drawCenteredString(font, Component.literal("Primeira criacao de mapa"), width / 2, top + 82, 0xFFFFE0A0);
        graphics.drawCenteredString(font, Component.literal("Casa, fazendas, portais e castelo carregam em etapas."), width / 2, top + 100, 0xFFE8F2FF);
        graphics.drawCenteredString(font, Component.literal(currentMessage), width / 2, top + 122, 0xFFBFD7FF);

        graphics.fill(progressLeft, progressTop, progressLeft + progressWidth, progressTop + 12, 0xFF070B13);
        graphics.renderOutline(progressLeft, progressTop, progressWidth, 12, 0xAADDAD55);
        graphics.fill(progressLeft + 2, progressTop + 2, progressLeft + 2 + Math.max(0, filledWidth - 4), progressTop + 6, 0xFF2DB7FF);
        graphics.fill(progressLeft + 2, progressTop + 6, progressLeft + 2 + Math.max(0, filledWidth - 4), progressTop + 10, 0xFFB8862B);
        graphics.drawCenteredString(font, Component.literal(currentProgress + "%"), width / 2, progressTop + 18, 0xFFFFE0A0);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
