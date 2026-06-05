package com.example.examplemod.client.menus;

import com.example.examplemod.client.PremiumEntry;
import com.example.examplemod.client.PremiumMenuScreen;
import net.minecraft.world.item.Items;

import java.util.List;

public final class TransformationEncyclopedia {

    private TransformationEncyclopedia() {
    }

    public static void add(List<PremiumEntry> entries) {
        info(entries, "Pedra", "Stone", Items.STONE, "STONE / ANDESITE / DIORITE / GRANITE -> COAL_ORE.");
        info(entries, "Pedregulho", "Cobblestone", Items.COBBLESTONE, "COBBLESTONE -> IRON_ORE.");
        info(entries, "Terra", "Dirt", Items.DIRT, "DIRT / GRASS_BLOCK / COARSE_DIRT -> MOSS_BLOCK.");
        info(entries, "Areia", "Sand", Items.SAND, "SAND / RED_SAND / SANDSTONE -> GLASS.");
        info(entries, "Folhas", "Leaves", Items.OAK_LEAVES, "LEAVES -> EMERALD_BLOCK.");
        info(entries, "Troncos", "Logs", Items.OAK_LOG, "LOGS -> GOLD_BLOCK.");
        info(entries, "Minerios", "Ores", Items.COAL_ORE, "Minerios sobem de nivel.");
        info(entries, "Criados", "Created Blocks", Items.CRAFTING_TABLE, "Objetos criados viram versoes premium.");
    }

    private static void info(List<PremiumEntry> entries, String name, String englishName, net.minecraft.world.item.Item icon, String text) {
        MenuEntryFactory.command(entries, PremiumMenuScreen.MenuTab.TRANSFORMATION_ENCYCLOPEDIA, name, englishName, "Transformacao", text, "Consulta informativa.", icon, "");
    }
}
