package com.magicworld.client;

import net.minecraft.client.gui.screens.AlertScreen;
import net.minecraft.client.gui.screens.BackupConfirmScreen;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.GenericDirtMessageScreen;
import net.minecraft.client.gui.screens.LevelLoadingScreen;
import net.minecraft.client.gui.screens.OptionsScreen;
import net.minecraft.client.gui.screens.ProgressScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.packs.PackSelectionScreen;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraftforge.client.gui.ModListScreen;

public final class MagicWorldScreenBackgrounds {
    private MagicWorldScreenBackgrounds() {
    }

    public static boolean shouldUseStaticBackground(Screen screen) {
        if (screen == null || screen instanceof MagicWorldTitleScreen) {
            return false;
        }

        return screen instanceof CreateWorldScreen
                || screen instanceof SelectWorldScreen
                || screen instanceof GenericDirtMessageScreen
                || screen instanceof ProgressScreen
                || screen instanceof ConfirmScreen
                || screen instanceof AlertScreen
                || screen instanceof BackupConfirmScreen
                || screen instanceof JoinMultiplayerScreen
                || screen instanceof OptionsScreen
                || screen instanceof PackSelectionScreen
                || screen instanceof ModListScreen
                || screen instanceof LevelLoadingScreen
                || isWorldCreationScreen(screen);
    }

    private static boolean isWorldCreationScreen(Screen screen) {
        String className = screen.getClass().getName().toLowerCase();
        return className.contains("worldselection")
                || className.contains("worldcreation")
                || className.contains("createworld");
    }
}
