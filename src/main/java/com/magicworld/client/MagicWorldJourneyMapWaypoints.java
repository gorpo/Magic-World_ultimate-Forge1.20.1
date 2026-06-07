package com.magicworld.client;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.ModList;

public final class MagicWorldJourneyMapWaypoints {
    private MagicWorldJourneyMapWaypoints() {
    }

    public static void writeWaypoint(String id, String label, String dimension, int x, int y, int z, int red, int green, int blue) {
        if (!ModList.get().isLoaded("journeymap")) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null || minecraft.level == null) {
            return;
        }

        Path gameDirectory = minecraft.gameDirectory.toPath();
        hideJourneyMapWorldBeacons(gameDirectory);

        String worldName = "Magic World";
        if (minecraft.getSingleplayerServer() != null) {
            worldName = minecraft.getSingleplayerServer().getWorldData().getLevelName();
        }

        String safeId = sanitize("magicworld-" + id);
        Path waypointsDir = gameDirectory
                .resolve("journeymap")
                .resolve("data")
                .resolve("sp")
                .resolve(worldName)
                .resolve("waypoints");

        try {
            Files.createDirectories(waypointsDir);
            deleteOldWaypointFiles(waypointsDir, safeId, label);
            String fileId = safeId + "_" + x + "-" + y + "-" + z;
            Files.writeString(
                    waypointsDir.resolve(fileId + ".json"),
                    waypointJson(fileId, label, dimension, x, y, z, red, green, blue),
                    StandardCharsets.UTF_8
            );
        } catch (IOException ignored) {
            // JourneyMap is optional; a failed local waypoint write must never break gameplay.
        }
    }

    private static void deleteOldWaypointFiles(Path waypointsDir, String safeId, String label) throws IOException {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(waypointsDir, "*.json")) {
            for (Path old : stream) {
                String content = Files.readString(old, StandardCharsets.UTF_8);
                if (isSameMagicWorldWaypoint(content, safeId, label)) {
                    Files.deleteIfExists(old);
                }
            }
        }
    }

    private static boolean isSameMagicWorldWaypoint(String content, String safeId, String label) {
        boolean sameSafeId = content.contains("\"id\": \"" + escape(safeId) + "_");
        boolean sameLegacyName = content.contains("\"origin\": \"magicworld\"")
                && content.contains("\"name\": \"" + escape(label) + "\"");
        return sameSafeId || sameLegacyName;
    }

    private static void hideJourneyMapWorldBeacons(Path gameDirectory) {
        Path configFile = gameDirectory
                .resolve("journeymap")
                .resolve("config")
                .resolve("5.10")
                .resolve("journeymap.waypoint.config");

        try {
            Files.createDirectories(configFile.getParent());
            String config = Files.exists(configFile)
                    ? Files.readString(configFile, StandardCharsets.UTF_8)
                    : defaultWaypointConfig();
            config = setConfigValue(config, "managerEnabled", "true");
            config = setConfigValue(config, "beaconEnabled", "false");
            config = setConfigValue(config, "showTexture", "false");
            config = setConfigValue(config, "showStaticBeam", "false");
            config = setConfigValue(config, "showRotatingBeam", "false");
            config = setConfigValue(config, "showName", "false");
            config = setConfigValue(config, "showDistance", "false");
            config = setConfigValue(config, "showDeviationLabel", "false");
            config = setConfigValue(config, "shaderBeacon", "false");
            Files.writeString(configFile, config, StandardCharsets.UTF_8);
        } catch (IOException ignored) {
            // JourneyMap config is optional; waypoint creation still works if the config write fails.
        }
    }

    private static String setConfigValue(String config, String key, String value) {
        String needle = "\"" + key + "\"";
        int keyIndex = config.indexOf(needle);
        if (keyIndex < 0) {
            return insertConfigValue(config, key, value);
        }

        int colon = config.indexOf(':', keyIndex);
        if (colon < 0) {
            return config;
        }
        int lineEnd = config.indexOf('\n', colon);
        if (lineEnd < 0) {
            lineEnd = config.length();
        }

        String currentLine = config.substring(keyIndex, lineEnd).trim();
        String comma = currentLine.endsWith(",") ? "," : "";
        return config.substring(0, keyIndex)
                + "\"" + key + "\": \"" + value + "\"" + comma
                + config.substring(lineEnd);
    }

    private static String insertConfigValue(String config, String key, String value) {
        String line = "  \"" + key + "\": \"" + value + "\",\n";
        int versionIndex = config.indexOf("  \"configVersion\"");
        if (versionIndex >= 0) {
            return config.substring(0, versionIndex) + line + config.substring(versionIndex);
        }

        int closeIndex = config.lastIndexOf('}');
        if (closeIndex >= 0) {
            return config.substring(0, closeIndex) + line + config.substring(closeIndex);
        }
        return defaultWaypointConfig();
    }

    private static String defaultWaypointConfig() {
        return "// jm.config.file_header_1\n"
                + "// jm.config.file_header_2\n"
                + "// jm.config.file_header_5\n"
                + "{\n"
                + "  \"managerEnabled\": \"true\",\n"
                + "  \"beaconEnabled\": \"false\",\n"
                + "  \"showTexture\": \"false\",\n"
                + "  \"showStaticBeam\": \"false\",\n"
                + "  \"showRotatingBeam\": \"false\",\n"
                + "  \"showName\": \"false\",\n"
                + "  \"showDistance\": \"false\",\n"
                + "  \"autoHideLabel\": \"true\",\n"
                + "  \"showDeviationLabel\": \"false\",\n"
                + "  \"disableStrikeThrough\": \"false\",\n"
                + "  \"boldLabel\": \"false\",\n"
                + "  \"fontScale\": \"2.0\",\n"
                + "  \"textureSmall\": \"true\",\n"
                + "  \"shaderBeacon\": \"false\",\n"
                + "  \"maxDistance\": \"0\",\n"
                + "  \"minDistance\": \"4\",\n"
                + "  \"createDeathpoints\": \"true\",\n"
                + "  \"autoRemoveDeathpoints\": \"false\",\n"
                + "  \"autoRemoveDeathpointDistance\": \"2\",\n"
                + "  \"autoRemoveTempWaypoints\": \"2\",\n"
                + "  \"showDeathpointlabel\": \"true\",\n"
                + "  \"fullscreenDoubleClickToCreate\": \"true\",\n"
                + "  \"teleportCommand\": \"/tp {name} {x} {y} {z}\",\n"
                + "  \"dateFormat\": \"MM-dd-yyyy\",\n"
                + "  \"timeFormat\": \"HH:mm:ss\",\n"
                + "  \"managerDimensionFocus\": \"false\",\n"
                + "  \"configVersion\": \"5.10.3\"\n"
                + "}\n";
    }

    private static String waypointJson(String id, String label, String dimension, int x, int y, int z, int red, int green, int blue) {
        int color = (red << 16) | (green << 8) | blue;
        return "{\n"
                + "  \"id\": \"" + escape(id) + "\",\n"
                + "  \"name\": \"" + escape(label) + "\",\n"
                + "  \"icon\": \"journeymap:ui/img/waypoint-icon.png\",\n"
                + "  \"colorizedIcon\": \"fake:color-" + color + "-waypoint-icon.png\",\n"
                + "  \"x\": " + x + ",\n"
                + "  \"y\": " + y + ",\n"
                + "  \"z\": " + z + ",\n"
                + "  \"r\": " + red + ",\n"
                + "  \"g\": " + green + ",\n"
                + "  \"b\": " + blue + ",\n"
                + "  \"enable\": true,\n"
                + "  \"type\": \"Normal\",\n"
                + "  \"origin\": \"magicworld\",\n"
                + "  \"dimensions\": [\n"
                + "    \"" + escape(dimension) + "\"\n"
                + "  ],\n"
                + "  \"persistent\": true,\n"
                + "  \"showDeviation\": false,\n"
                + "  \"iconColor\": -1,\n"
                + "  \"customIconColor\": false\n"
                + "}\n";
    }

    private static String sanitize(String value) {
        return value.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9_-]+", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");
    }

    private static String escape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
