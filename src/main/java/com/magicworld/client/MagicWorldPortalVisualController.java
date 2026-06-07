package com.magicworld.client;

import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraftforge.fml.loading.FMLPaths;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Stream;

public final class MagicWorldPortalVisualController {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String DEFAULT_SHADER_PACK = "MagicWorld_Shaders_Extreme_v1.0_.zip";
    private static final String SCREEN_OVERLAYS_PACK_NAME = "Screen Overlays";
    private static final String SCREEN_OVERLAYS_PACK_ID = "file/" + SCREEN_OVERLAYS_PACK_NAME;
    private static final List<String> BASE_PACKS = List.of("vanilla", "mod_resources");
    private static final List<String> DEFAULT_RESOURCE_PACK_ORDER = List.of(
            "file/MagicWorldResource_1.20.1-bonus.zip",
            "file/MagicWorldResource_1.20.1-addon.zip",
            "file/MagicWorldResource_1.20.1-models.zip",
            "file/MagicWorldResource_1.20.1-256x.zip"
    );

    public record ResourcePackChoice(String id, String name, boolean defaultMagicWorld) {
    }

    public record ShaderPackChoice(String name, boolean defaultMagicWorld) {
    }

    private MagicWorldPortalVisualController() {
    }

    public static void applyPortalSelection(boolean active, boolean resourcePack, boolean shaderPack) {
        applyPortalSelection(active && resourcePack, active && shaderPack, defaultResourcePackIds(), defaultShaderPackName());
    }

    public static void applyPortalSelection(
            boolean resourcePack,
            boolean shaderPack,
            List<String> resourcePackIds,
            String shaderPackName
    ) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null) {
            return;
        }

        boolean changedResourcePacks = applyResourcePacks(minecraft, resourcePack, resourcePackIds);
        applyShader(shaderPack, shaderPackName);

        if (changedResourcePacks) {
            minecraft.reloadResourcePacks();
        }
    }

    public static List<ResourcePackChoice> availableResourcePacks() {
        Path resourcePacks = Minecraft.getInstance().gameDirectory.toPath().resolve("resourcepacks");
        List<ResourcePackChoice> choices = new ArrayList<>();

        if (!Files.isDirectory(resourcePacks)) {
            return choices;
        }

        try (Stream<Path> stream = Files.list(resourcePacks)) {
            stream.filter(MagicWorldPortalVisualController::isResourcePackCandidate)
                    .forEach(path -> {
                        String fileName = path.getFileName().toString();
                        if (SCREEN_OVERLAYS_PACK_NAME.equals(fileName)) {
                            return;
                        }

                        String id = "file/" + fileName;
                        choices.add(new ResourcePackChoice(
                                id,
                                displayName(fileName),
                                DEFAULT_RESOURCE_PACK_ORDER.contains(id)
                        ));
                    });
        } catch (IOException exception) {
            LOGGER.warn("Magic World could not list resourcepacks.", exception);
        }

        choices.sort(Comparator
                .comparingInt((ResourcePackChoice choice) -> defaultResourcePackIndex(choice.id()))
                .thenComparing(ResourcePackChoice::name, String.CASE_INSENSITIVE_ORDER));
        return choices;
    }

    public static List<String> defaultResourcePackIds() {
        Set<String> available = new LinkedHashSet<>();
        for (ResourcePackChoice choice : availableResourcePacks()) {
            available.add(choice.id());
        }

        List<String> defaults = new ArrayList<>();
        for (String pack : DEFAULT_RESOURCE_PACK_ORDER) {
            if (available.contains(pack)) {
                defaults.add(pack);
            }
        }
        return defaults;
    }

    public static List<ShaderPackChoice> availableShaderPacks() {
        Path shaderPacks = Minecraft.getInstance().gameDirectory.toPath().resolve("shaderpacks");
        List<ShaderPackChoice> choices = new ArrayList<>();

        if (!Files.isDirectory(shaderPacks)) {
            return choices;
        }

        try (Stream<Path> stream = Files.list(shaderPacks)) {
            stream.filter(MagicWorldPortalVisualController::isShaderPackCandidate)
                    .forEach(path -> {
                        String fileName = path.getFileName().toString();
                        choices.add(new ShaderPackChoice(fileName, DEFAULT_SHADER_PACK.equals(fileName)));
                    });
        } catch (IOException exception) {
            LOGGER.warn("Magic World could not list shaderpacks.", exception);
        }

        choices.sort(Comparator
                .comparingInt((ShaderPackChoice choice) -> choice.defaultMagicWorld() ? 0 : 1)
                .thenComparing(ShaderPackChoice::name, String.CASE_INSENSITIVE_ORDER));
        return choices;
    }

    public static String defaultShaderPackName() {
        for (ShaderPackChoice choice : availableShaderPacks()) {
            if (choice.defaultMagicWorld()) {
                return choice.name();
            }
        }
        return availableShaderPacks().isEmpty() ? "" : availableShaderPacks().get(0).name();
    }

    private static boolean applyResourcePacks(Minecraft minecraft, boolean active, List<String> requestedPacks) {
        PackRepository repository = minecraft.getResourcePackRepository();
        repository.reload();

        List<String> selected = new ArrayList<>();
        for (String pack : BASE_PACKS) {
            if (repository.isAvailable(pack)) {
                selected.add(pack);
            }
        }

        if (active) {
            for (String pack : orderedUniquePacks(requestedPacks)) {
                if (repository.isAvailable(pack)) {
                    selected.add(pack);
                }
            }
            if (repository.isAvailable(SCREEN_OVERLAYS_PACK_ID)) {
                selected.remove(SCREEN_OVERLAYS_PACK_ID);
                selected.add(SCREEN_OVERLAYS_PACK_ID);
            }
        }

        if (selected.equals(minecraft.options.resourcePacks)) {
            return false;
        }

        repository.setSelected(selected);
        minecraft.options.resourcePacks = selected;
        minecraft.options.incompatibleResourcePacks = new ArrayList<>();
        minecraft.options.updateResourcePacks(repository);
        minecraft.options.save();
        return true;
    }

    private static void applyShader(boolean active, String shaderPackName) {
        String resolvedShaderPack = shaderPackName == null || shaderPackName.isBlank() ? defaultShaderPackName() : shaderPackName;
        writeIrisProperties(active, resolvedShaderPack);

        try {
            Class<?> irisClass = Class.forName("net.irisshaders.iris.Iris");
            Object config = irisClass.getMethod("getIrisConfig").invoke(null);

            config.getClass()
                    .getMethod("setShaderPackName", String.class)
                    .invoke(config, active ? resolvedShaderPack : null);
            config.getClass()
                    .getMethod("setShadersEnabled", boolean.class)
                    .invoke(config, active);
            config.getClass()
                    .getMethod("save")
                    .invoke(config);

            Class<?> apiClass = Class.forName("net.irisshaders.iris.api.v0.IrisApi");
            Object api = apiClass.getMethod("getInstance").invoke(null);
            Method getConfig = apiClass.getMethod("getConfig");
            Object apiConfig = getConfig.invoke(api);
            apiConfig.getClass()
                    .getMethod("setShadersEnabledAndApply", boolean.class)
                    .invoke(apiConfig, active);
        } catch (ReflectiveOperationException exception) {
            LOGGER.warn("Magic World could not apply Oculus/Iris shader state.", exception);
        }
    }

    private static void writeIrisProperties(boolean active, String shaderPackName) {
        Path irisProperties = FMLPaths.CONFIGDIR.get().resolve("iris.properties");
        Properties properties = new Properties();

        try {
            Files.createDirectories(irisProperties.getParent());

            if (Files.exists(irisProperties)) {
                try (InputStream inputStream = Files.newInputStream(irisProperties)) {
                    properties.load(inputStream);
                }
            }

            properties.setProperty("enableShaders", Boolean.toString(active));
            properties.setProperty("shaderPack", active ? shaderPackName : "");

            try (OutputStream outputStream = Files.newOutputStream(irisProperties)) {
                properties.store(outputStream, "Magic World visual mode");
            }
        } catch (IOException exception) {
            LOGGER.warn("Magic World could not write Oculus/Iris visual mode.", exception);
        }
    }

    private static List<String> orderedUniquePacks(List<String> requestedPacks) {
        Set<String> unique = new LinkedHashSet<>();
        if (requestedPacks != null) {
            for (String pack : requestedPacks) {
                if (pack != null && !pack.isBlank() && !SCREEN_OVERLAYS_PACK_ID.equals(pack)) {
                    unique.add(pack);
                }
            }
        }
        return new ArrayList<>(unique);
    }

    private static boolean isResourcePackCandidate(Path path) {
        String fileName = path.getFileName().toString();
        if (Files.isDirectory(path)) {
            return Files.exists(path.resolve("pack.mcmeta"));
        }
        return fileName.toLowerCase().endsWith(".zip");
    }

    private static boolean isShaderPackCandidate(Path path) {
        String fileName = path.getFileName().toString();
        return Files.isDirectory(path) || fileName.toLowerCase().endsWith(".zip");
    }

    private static int defaultResourcePackIndex(String id) {
        int index = DEFAULT_RESOURCE_PACK_ORDER.indexOf(id);
        return index >= 0 ? index : 1000;
    }

    private static String displayName(String fileName) {
        if (fileName.toLowerCase().endsWith(".zip")) {
            return fileName.substring(0, fileName.length() - 4);
        }
        return fileName;
    }
}
