package com.example.examplemod.client;

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
        int panelWidth = Math.min(360, width - 32);
        int panelHeight = 200;
        int left = width / 2 - panelWidth / 2;
        int top = Math.max(22, height / 2 - 92);
        int progressLeft = left + 28;
        int progressTop = top + 122;
        int progressWidth = panelWidth - 56;
        int filledWidth = progressWidth * currentProgress / 100;

        MagicWorldStaticBackground.draw(graphics, width, height);
        graphics.fill(0, 0, width, height, 0x66030611);
        graphics.fill(left, top, left + panelWidth, top + panelHeight, 0xEE101018);
        graphics.renderOutline(left, top, panelWidth, panelHeight, 0xAADDAD55);
        graphics.drawCenteredString(font, Component.literal("MAGIC WORLD"), width / 2, top + 14, 0xFFFFFFFF);
        graphics.drawCenteredString(font, Component.literal("Primeira criacao de mapa"), width / 2, top + 32, 0xFFFFE0A0);
        graphics.drawCenteredString(font, Component.literal("pode demorar alguns minutos."), width / 2, top + 48, 0xFFFFE0A0);
        graphics.drawCenteredString(font, Component.literal("Casa, fazendas, portal, castelo"), width / 2, top + 72, 0xFFE8F2FF);
        graphics.drawCenteredString(font, Component.literal("e dragao carregam em etapas."), width / 2, top + 88, 0xFFE8F2FF);
        graphics.drawCenteredString(font, Component.literal(currentMessage), width / 2, top + 106, 0xFFBFD7FF);

        graphics.fill(progressLeft, progressTop, progressLeft + progressWidth, progressTop + 12, 0xFF070B13);
        graphics.renderOutline(progressLeft, progressTop, progressWidth, 12, 0xAADDAD55);
        graphics.fill(progressLeft + 2, progressTop + 2, progressLeft + 2 + Math.max(0, filledWidth - 4), progressTop + 10, 0xFF8B5CFF);
        graphics.drawCenteredString(font, Component.literal(currentProgress + "%"), width / 2, progressTop + 18, 0xFFFFE0A0);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
