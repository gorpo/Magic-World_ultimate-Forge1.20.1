package com.magicworld.client;

import com.mojang.logging.LogUtils;
import net.minecraftforge.fml.loading.FMLPaths;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class MagicWorldClientCompat {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String DH_CLIENT_HEADER = "[client]";
    private static final String DH_EXPERIMENTAL_GRAPHICS_HEADER = "[client.advanced.graphics.experimental]";

    private MagicWorldClientCompat() {
    }

    public static void prepareDistantHorizonsConfig() {
        Path configPath = FMLPaths.CONFIGDIR.get().resolve("DistantHorizons.toml");
        try {
            Files.createDirectories(configPath.getParent());
            String config = Files.exists(configPath) ? Files.readString(configPath, StandardCharsets.UTF_8) : "";
            String updated = forceConfigEntry(config, DH_CLIENT_HEADER,
                    "showDhOptionsButtonInMinecraftUi", "false", "\t", false);
            updated = forceConfigEntry(updated, DH_EXPERIMENTAL_GRAPHICS_HEADER,
                    "renderingApi", "\"OPEN_GL\"", "\t\t\t\t", true);
            if (!updated.equals(config)) {
                Files.writeString(configPath, updated, StandardCharsets.UTF_8);
                LOGGER.info("Magic World prepared Distant Horizons menu and rendering compatibility.");
            }
        } catch (IOException exception) {
            LOGGER.warn("Magic World could not prepare Distant Horizons configuration.", exception);
        }
    }

    private static String forceConfigEntry(
            String config,
            String sectionHeader,
            String key,
            String value,
            String defaultIndent,
            boolean removeDuplicateSections
    ) {
        String normalized = config == null ? "" : config;
        String separator = System.lineSeparator();
        String[] lines = normalized.isEmpty() ? new String[0] : normalized.split("\\R", -1);
        List<String> updated = new ArrayList<>();
        boolean foundSection = false;
        boolean inSection = false;
        boolean skipDuplicateSection = false;
        boolean wroteEntry = false;
        boolean changed = false;

        for (String line : lines) {
            String trimmed = line.trim();
            boolean section = trimmed.startsWith("[") && trimmed.endsWith("]");
            boolean targetSection = sectionHeader.equals(trimmed);

            if (skipDuplicateSection) {
                if (!section) {
                    changed = true;
                    continue;
                }
                skipDuplicateSection = false;
            }

            if (section) {
                if (inSection && !wroteEntry) {
                    updated.add(defaultIndent + key + " = " + value);
                    wroteEntry = true;
                    changed = true;
                }
                inSection = false;
                if (targetSection) {
                    if (foundSection && removeDuplicateSections) {
                        skipDuplicateSection = true;
                        changed = true;
                        continue;
                    }
                    foundSection = true;
                    inSection = true;
                }
            }

            if (inSection && (trimmed.startsWith(key + " ") || trimmed.startsWith(key + "="))) {
                String indent = line.replaceFirst("\\S.*$", "");
                String replacement = (indent.isBlank() ? defaultIndent : indent) + key + " = " + value;
                updated.add(replacement);
                wroteEntry = true;
                changed = changed || !replacement.equals(line);
                continue;
            }
            updated.add(line);
        }

        if (inSection && !wroteEntry) {
            updated.add(defaultIndent + key + " = " + value);
            wroteEntry = true;
            changed = true;
        }
        if (!foundSection) {
            if (!updated.isEmpty() && !updated.get(updated.size() - 1).isBlank()) {
                updated.add("");
            }
            updated.add(sectionHeader);
            updated.add(defaultIndent + key + " = " + value);
            wroteEntry = true;
            changed = true;
        }

        String result = String.join(separator, updated).stripTrailing() + separator;
        return wroteEntry && changed ? result : normalized;
    }
}
