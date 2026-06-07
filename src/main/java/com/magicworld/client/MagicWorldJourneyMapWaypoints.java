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

        String worldName = "Magic World";
        if (minecraft.getSingleplayerServer() != null) {
            worldName = minecraft.getSingleplayerServer().getWorldData().getLevelName();
        }

        String safeId = sanitize("magicworld-" + id);
        Path waypointsDir = minecraft.gameDirectory.toPath()
                .resolve("journeymap")
                .resolve("data")
                .resolve("sp")
                .resolve(worldName)
                .resolve("waypoints");

        try {
            Files.createDirectories(waypointsDir);
            deleteOldWaypointFiles(waypointsDir, safeId + "_");
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

    private static void deleteOldWaypointFiles(Path waypointsDir, String prefix) throws IOException {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(waypointsDir, prefix + "*.json")) {
            for (Path old : stream) {
                Files.deleteIfExists(old);
            }
        }
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
