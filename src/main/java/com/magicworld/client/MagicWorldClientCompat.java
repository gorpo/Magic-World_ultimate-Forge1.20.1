package com.magicworld.client;

import com.mojang.logging.LogUtils;
import net.neoforged.fml.loading.FMLPaths;
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
    private static final String DH_OPTIONS_BUTTON = "showDhOptionsButtonInMinecraftUi";
    private static final String DH_RENDERING_API = "renderingApi";

    private MagicWorldClientCompat() {
    }

    public static void prepareDistantHorizonsConfig() {
        Path configPath = FMLPaths.CONFIGDIR.get().resolve("DistantHorizons.toml");

        try {
            Files.createDirectories(configPath.getParent());

            String config = Files.exists(configPath)
                    ? Files.readString(configPath, StandardCharsets.UTF_8)
                    : "";

            String updatedConfig = forceDistantHorizonsClientOptions(config);
            if (!updatedConfig.equals(config)) {
                Files.writeString(configPath, updatedConfig, StandardCharsets.UTF_8);
                LOGGER.info("Magic World prepared Distant Horizons client options for shader/menu compatibility.");
            }
        } catch (IOException exception) {
            LOGGER.warn("Magic World could not prepare Distant Horizons configuration.", exception);
        }
    }

    private static String forceDistantHorizonsClientOptions(String config) {
        String updatedConfig = forceConfigEntry(
                config,
                DH_CLIENT_HEADER,
                DH_OPTIONS_BUTTON,
                "false",
                "\t",
                false
        );

        return forceConfigEntry(
                updatedConfig,
                DH_EXPERIMENTAL_GRAPHICS_HEADER,
                DH_RENDERING_API,
                "\"OPEN_GL\"",
                "\t\t\t\t",
                true
        );
    }

    private static String forceConfigEntry(
            String config,
            String sectionHeader,
            String key,
            String value,
            String defaultIndent,
            boolean removeDuplicateSections
    ) {
        String normalizedConfig = config == null ? "" : config;
        String lineSeparator = System.lineSeparator();
        String[] lines = normalizedConfig.isEmpty() ? new String[0] : normalizedConfig.split("\\R", -1);
        List<String> updatedLines = new ArrayList<>();
        boolean foundTargetSection = false;
        boolean inTargetSection = false;
        boolean skipDuplicateTargetSection = false;
        boolean wroteEntry = false;
        boolean changed = false;

        for (String line : lines) {
            String trimmedLine = line.trim();
            boolean isSectionHeader = trimmedLine.startsWith("[") && trimmedLine.endsWith("]");
            boolean isTargetSection = sectionHeader.equals(trimmedLine);

            if (skipDuplicateTargetSection) {
                if (!isSectionHeader) {
                    changed = true;
                    continue;
                }

                skipDuplicateTargetSection = false;
            }

            if (isSectionHeader) {
                if (inTargetSection && !wroteEntry) {
                    updatedLines.add(configEntryLine(line, key, value, defaultIndent));
                    wroteEntry = true;
                    changed = true;
                }

                inTargetSection = false;

                if (isTargetSection) {
                    if (foundTargetSection && removeDuplicateSections) {
                        skipDuplicateTargetSection = true;
                        changed = true;
                        continue;
                    }

                    foundTargetSection = true;
                    inTargetSection = true;
                }
            }

            if (inTargetSection && isConfigEntryLine(trimmedLine, key)) {
                String updatedLine = configEntryLine(line, key, value, defaultIndent);
                updatedLines.add(updatedLine);
                wroteEntry = true;
                changed = changed || !updatedLine.equals(line);
                continue;
            }

            updatedLines.add(line);
        }

        if (inTargetSection && !wroteEntry) {
            updatedLines.add(defaultIndent + key + " = " + value);
            wroteEntry = true;
            changed = true;
        }

        if (!foundTargetSection) {
            if (!updatedLines.isEmpty() && !updatedLines.getLast().isBlank()) {
                updatedLines.add("");
            }

            updatedLines.add(sectionHeader);
            updatedLines.add(defaultIndent + key + " = " + value);
            wroteEntry = true;
            changed = true;
        }

        String updatedConfig = String.join(lineSeparator, updatedLines).stripTrailing() + lineSeparator;
        return wroteEntry && changed ? updatedConfig : normalizedConfig;
    }

    private static boolean isConfigEntryLine(String trimmedLine, String key) {
        return trimmedLine.startsWith(key + " ") || trimmedLine.startsWith(key + "=");
    }

    private static String configEntryLine(String referenceLine, String key, String value, String defaultIndent) {
        String indent = referenceLine.replaceFirst("\\S.*$", "");
        if (indent.isBlank()) {
            indent = defaultIndent;
        }

        return indent + key + " = " + value;
    }
}
