package com.magicworld;

public final class MagicWorldWorldOptions {
    public enum StartingGameMode {
        SURVIVAL("Normal", "survival", "sobrevivencia"),
        CREATIVE("Criativo", "creative", "criativo");

        private final String label;
        private final String[] labelMatches;

        StartingGameMode(String label, String... labelMatches) {
            this.label = label;
            this.labelMatches = labelMatches;
        }

        public String label() {
            return label;
        }

        public String[] labelMatches() {
            return labelMatches;
        }
    }

    public enum StartingDifficulty {
        PEACEFUL("Pacifico", "peaceful", "pacifico"),
        EASY("Facil", "easy", "facil"),
        NORMAL("Normal", "normal"),
        HARD("Dificil", "hard", "dificil");

        private final String label;
        private final String[] labelMatches;

        StartingDifficulty(String label, String... labelMatches) {
            this.label = label;
            this.labelMatches = labelMatches;
        }

        public String label() {
            return label;
        }

        public String[] labelMatches() {
            return labelMatches;
        }
    }

    private static boolean starterEstateEnabled = true;
    private static boolean castlesEnabled = true;
    private static boolean farmsEnabled = true;
    private static boolean auraEnabled = true;
    private static boolean commandsEnabled = true;
    private static int hardwareProfileIndex = 3;
    private static StartingGameMode startingGameMode = StartingGameMode.SURVIVAL;
    private static StartingDifficulty startingDifficulty = StartingDifficulty.NORMAL;
    private static String customSeed = "";
    private static int presetSeedIndex = 0;

    private MagicWorldWorldOptions() {
    }

    public static boolean isStarterEstateEnabled() {
        return starterEstateEnabled;
    }

    public static void setStarterEstateEnabled(boolean enabled) {
        starterEstateEnabled = enabled;
    }

    public static boolean toggleStarterEstateEnabled() {
        starterEstateEnabled = !starterEstateEnabled;
        return starterEstateEnabled;
    }

    public static boolean isCastlesEnabled() {
        return castlesEnabled;
    }

    public static void setCastlesEnabled(boolean enabled) {
        castlesEnabled = enabled;
    }

    public static boolean toggleCastlesEnabled() {
        castlesEnabled = !castlesEnabled;
        return castlesEnabled;
    }

    public static boolean isFarmsEnabled() {
        return farmsEnabled;
    }

    public static void setFarmsEnabled(boolean enabled) {
        farmsEnabled = enabled;
    }

    public static boolean toggleFarmsEnabled() {
        farmsEnabled = !farmsEnabled;
        return farmsEnabled;
    }

    public static boolean isAuraEnabled() {
        return auraEnabled;
    }

    public static void setAuraEnabled(boolean enabled) {
        auraEnabled = enabled;
    }

    public static boolean toggleAuraEnabled() {
        auraEnabled = !auraEnabled;
        return auraEnabled;
    }

    public static boolean isCommandsEnabled() {
        return commandsEnabled || hasCommandRequiredOptionEnabled();
    }

    public static void setCommandsEnabled(boolean enabled) {
        commandsEnabled = enabled;
    }

    public static boolean hasCommandRequiredOptionEnabled() {
        return starterEstateEnabled || castlesEnabled || farmsEnabled || auraEnabled;
    }

    public static int hardwareProfileIndex() {
        return hardwareProfileIndex;
    }

    public static void setHardwareProfileIndex(int index) {
        hardwareProfileIndex = Math.max(0, index);
    }

    public static int nextHardwareProfileIndex(int profileCount) {
        if (profileCount <= 0) {
            hardwareProfileIndex = 0;
            return hardwareProfileIndex;
        }
        hardwareProfileIndex = (hardwareProfileIndex + 1) % profileCount;
        return hardwareProfileIndex;
    }

    public static StartingGameMode startingGameMode() {
        return startingGameMode;
    }

    public static StartingGameMode nextStartingGameMode() {
        StartingGameMode[] modes = StartingGameMode.values();
        startingGameMode = modes[(startingGameMode.ordinal() + 1) % modes.length];
        return startingGameMode;
    }

    public static void setStartingGameMode(StartingGameMode mode) {
        startingGameMode = mode;
    }

    public static StartingDifficulty startingDifficulty() {
        return startingDifficulty;
    }

    public static StartingDifficulty nextStartingDifficulty() {
        StartingDifficulty[] difficulties = StartingDifficulty.values();
        startingDifficulty = difficulties[(startingDifficulty.ordinal() + 1) % difficulties.length];
        return startingDifficulty;
    }

    public static void setStartingDifficulty(StartingDifficulty difficulty) {
        startingDifficulty = difficulty;
    }

    public static String customSeed() {
        return customSeed;
    }

    public static void setCustomSeed(String seed) {
        customSeed = seed == null ? "" : seed.trim();
    }

    public static int presetSeedIndex() {
        return presetSeedIndex;
    }

    public static void setPresetSeedIndex(int index, int presetCount) {
        if (presetCount <= 0) {
            presetSeedIndex = 0;
            return;
        }
        presetSeedIndex = Math.max(0, Math.min(index, presetCount - 1));
    }

    public static int nextPresetSeedIndex(int presetCount) {
        if (presetCount <= 0) {
            presetSeedIndex = 0;
            return presetSeedIndex;
        }
        presetSeedIndex = (presetSeedIndex + 1) % presetCount;
        return presetSeedIndex;
    }
}
