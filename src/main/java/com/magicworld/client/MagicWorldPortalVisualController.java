package com.magicworld.client;

import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.fml.loading.FMLPaths;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public final class MagicWorldPortalVisualController {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String SHADER_PACK = "MagicWorld_Shaders_Extreme_v1.0_Iris";
    private static final List<String> BASE_PACKS = List.of("vanilla", "mod_resources");
    private static final List<String> PREMIUM_PACKS = List.of(
            "file/MagicWorldResource_1.20.1-256x.zip",
            "file/MagicWorldResource_1.20.1-models.zip",
            "file/MagicWorldResource_1.20.1-addon.zip",
            "file/MagicWorldResource_1.20.1-bonus.zip"
    );
    private static boolean initializedWorld;
    private static boolean premiumActive;

    private MagicWorldPortalVisualController() {
    }

    public static void onClientTick(Minecraft minecraft) {
        if (minecraft.player == null || minecraft.level == null) {
            initializedWorld = false;
            return;
        }

        if (!initializedWorld) {
            initializedWorld = true;
            applyVisualMode(minecraft, false, false, false, false);
        }
    }

    public static void applyPortalSelection(boolean active, boolean resourcePack, boolean shaderPack) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            return;
        }

        applyVisualMode(minecraft, active, resourcePack, shaderPack, true);
    }

    private static void applyVisualMode(Minecraft minecraft, boolean active, boolean resourcePack, boolean shaderPack, boolean showMessage) {
        boolean changedResourcePacks = applyResourcePacks(minecraft, active && resourcePack);
        applyIrisShader(active && shaderPack);

        premiumActive = active;

        if (showMessage) {
            minecraft.player.sendOverlayMessage(
                    Component.literal(active
                            ? "Magic World: modo premium aplicado."
                            : "Magic World: modo simples ativado.")
            );
        }

        if (changedResourcePacks) {
            minecraft.reloadResourcePacks();
        }
    }

    private static boolean applyResourcePacks(Minecraft minecraft, boolean active) {
        PackRepository repository = minecraft.getResourcePackRepository();
        repository.reload();

        List<String> selected = new ArrayList<>();
        for (String pack : BASE_PACKS) {
            if (repository.isAvailable(pack)) {
                selected.add(pack);
            }
        }

        if (active) {
            for (String pack : PREMIUM_PACKS) {
                if (repository.isAvailable(pack)) {
                    selected.add(pack);
                }
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

    private static void applyIrisShader(boolean active) {
        writeIrisProperties(active);

        try {
            Class<?> irisClass = Class.forName("net.irisshaders.iris.Iris");
            Object config = irisClass.getMethod("getIrisConfig").invoke(null);

            config.getClass()
                    .getMethod("setShaderPackName", String.class)
                    .invoke(config, active ? SHADER_PACK : null);
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
            LOGGER.warn("Magic World could not apply Iris shader state through Iris API.", exception);
        }
    }

    private static void writeIrisProperties(boolean active) {
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
            properties.setProperty("shaderPack", active ? SHADER_PACK : "");

            try (OutputStream outputStream = Files.newOutputStream(irisProperties)) {
                properties.store(outputStream, "Magic World visual mode");
            }
        } catch (IOException exception) {
            LOGGER.warn("Magic World could not write Iris visual mode.", exception);
        }
    }
}
