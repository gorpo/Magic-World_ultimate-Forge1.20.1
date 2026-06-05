package com.example.examplemod;

public final class MagicWorldWorldOptions {
    private static boolean starterEstateEnabled = true;

    private MagicWorldWorldOptions() {
    }

    public static boolean isStarterEstateEnabled() {
        return starterEstateEnabled;
    }

    public static boolean toggleStarterEstateEnabled() {
        starterEstateEnabled = !starterEstateEnabled;
        return starterEstateEnabled;
    }
}
