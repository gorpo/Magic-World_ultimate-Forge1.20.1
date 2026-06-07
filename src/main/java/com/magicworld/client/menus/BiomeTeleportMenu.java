package com.magicworld.client.menus;

import com.magicworld.client.PremiumEntry;
import com.magicworld.client.PremiumMenuScreen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;

import java.util.List;

public final class BiomeTeleportMenu {

    private static final String[] VANILLA_BIOMES =
            new String[] {
                    "minecraft:badlands",
                    "minecraft:bamboo_jungle",
                    "minecraft:basalt_deltas",
                    "minecraft:beach",
                    "minecraft:birch_forest",
                    "minecraft:cherry_grove",
                    "minecraft:cold_ocean",
                    "minecraft:crimson_forest",
                    "minecraft:dark_forest",
                    "minecraft:deep_cold_ocean",
                    "minecraft:deep_dark",
                    "minecraft:deep_frozen_ocean",
                    "minecraft:deep_lukewarm_ocean",
                    "minecraft:deep_ocean",
                    "minecraft:desert",
                    "minecraft:dripstone_caves",
                    "minecraft:end_barrens",
                    "minecraft:end_highlands",
                    "minecraft:end_midlands",
                    "minecraft:eroded_badlands",
                    "minecraft:flower_forest",
                    "minecraft:forest",
                    "minecraft:frozen_ocean",
                    "minecraft:frozen_peaks",
                    "minecraft:frozen_river",
                    "minecraft:grove",
                    "minecraft:ice_spikes",
                    "minecraft:jagged_peaks",
                    "minecraft:jungle",
                    "minecraft:lukewarm_ocean",
                    "minecraft:lush_caves",
                    "minecraft:mangrove_swamp",
                    "minecraft:meadow",
                    "minecraft:mushroom_fields",
                    "minecraft:nether_wastes",
                    "minecraft:ocean",
                    "minecraft:old_growth_birch_forest",
                    "minecraft:old_growth_pine_taiga",
                    "minecraft:old_growth_spruce_taiga",
                    "minecraft:plains",
                    "minecraft:river",
                    "minecraft:savanna",
                    "minecraft:savanna_plateau",
                    "minecraft:small_end_islands",
                    "minecraft:snowy_beach",
                    "minecraft:snowy_plains",
                    "minecraft:snowy_slopes",
                    "minecraft:snowy_taiga",
                    "minecraft:soul_sand_valley",
                    "minecraft:sparse_jungle",
                    "minecraft:stony_peaks",
                    "minecraft:stony_shore",
                    "minecraft:sunflower_plains",
                    "minecraft:swamp",
                    "minecraft:taiga",
                    "minecraft:the_end",
                    "minecraft:the_void",
                    "minecraft:warm_ocean",
                    "minecraft:warped_forest",
                    "minecraft:windswept_forest",
                    "minecraft:windswept_gravelly_hills",
                    "minecraft:windswept_hills",
                    "minecraft:windswept_savanna",
                    "minecraft:wooded_badlands"
            };

    private BiomeTeleportMenu() {
    }

    public static void add(List<PremiumEntry> entries) {
        MenuEntryFactory.command(
                entries,
                PremiumMenuScreen.MenuTab.BIOME_TELEPORT,
                "Voltar ao ponto anterior",
                "Return",
                "Teleporte",
                "Volta para a posicao salva antes do ultimo teleporte por bioma.",
                "Funciona depois de usar qualquer bioma desta tela.",
                Items.RECOVERY_COMPASS,
                "RETURN_TELEPORT"
        );

        for (String biomeId : VANILLA_BIOMES) {
            biome(entries, ResourceLocation.tryParse(biomeId));
        }
    }

    private static void biome(List<PremiumEntry> entries, ResourceLocation biomeId) {
        if (biomeId == null) {
            return;
        }

        MenuEntryFactory.command(
                entries,
                PremiumMenuScreen.MenuTab.BIOME_TELEPORT,
                displayName(biomeId),
                biomeId.toString(),
                "Teleporte de bioma",
                "Procura o bioma no servidor integrado e teleporta direto.",
                "A posicao atual fica salva para o botao Voltar.",
                iconFor(biomeId.toString()),
                "BIOME_TELEPORT:" + biomeId
        );
    }

    private static String displayName(ResourceLocation biomeId) {
        String[] parts = biomeId.getPath().split("_");
        StringBuilder name = new StringBuilder();

        for (String part : parts) {
            if (part.isEmpty()) {
                continue;
            }

            if (name.length() > 0) {
                name.append(" ");
            }

            name.append(Character.toUpperCase(part.charAt(0)));

            if (part.length() > 1) {
                name.append(part.substring(1));
            }
        }

        return name.toString();
    }

    private static net.minecraft.world.item.Item iconFor(String biomeId) {
        if (biomeId.contains("desert")) return Items.SAND;
        if (biomeId.contains("badlands")) return Items.RED_SAND;
        if (biomeId.contains("jungle")) return Items.JUNGLE_SAPLING;
        if (biomeId.contains("taiga")) return Items.SPRUCE_SAPLING;
        if (biomeId.contains("swamp")) return Items.MANGROVE_ROOTS;
        if (biomeId.contains("mushroom")) return Items.RED_MUSHROOM_BLOCK;
        if (biomeId.contains("ocean")) return Items.WATER_BUCKET;
        if (biomeId.contains("river")) return Items.KELP;
        if (biomeId.contains("snow") || biomeId.contains("frozen") || biomeId.contains("ice")) return Items.SNOW_BLOCK;
        if (biomeId.contains("nether") || biomeId.contains("basalt") || biomeId.contains("crimson") || biomeId.contains("warped") || biomeId.contains("soul")) return Items.NETHERRACK;
        if (biomeId.contains("end")) return Items.END_STONE;
        if (biomeId.contains("cherry")) return Items.CHERRY_SAPLING;
        if (biomeId.contains("dark_forest")) return Items.DARK_OAK_SAPLING;
        if (biomeId.contains("birch")) return Items.BIRCH_SAPLING;
        if (biomeId.contains("savanna")) return Items.ACACIA_SAPLING;
        if (biomeId.contains("meadow")) return Items.POPPY;
        if (biomeId.contains("cave")) return Items.DEEPSLATE;
        return Items.GRASS_BLOCK;
    }
}
