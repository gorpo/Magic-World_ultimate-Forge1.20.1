package com.example.examplemod.client.menus;

import com.example.examplemod.client.PremiumEntry;
import com.example.examplemod.client.PremiumMenuScreen;
import net.minecraft.world.item.Items;

import java.util.List;

public final class DimensionMenu {

    private DimensionMenu() {
    }

    public static void add(List<PremiumEntry> entries) {
        dim(entries, "Overworld", "minecraft:overworld", Items.GRASS_BLOCK, "execute in minecraft:overworld run tp @s ~ ~ ~");
        dim(entries, "Nether", "minecraft:the_nether", Items.NETHERRACK, "execute in minecraft:the_nether run tp @s ~ ~ ~");
        dim(entries, "The End", "minecraft:the_end", Items.END_STONE, "execute in minecraft:the_end run tp @s ~ ~ ~");
    }

    private static void dim(List<PremiumEntry> entries, String name, String englishName, net.minecraft.world.item.Item icon, String command) {
        MenuEntryFactory.command(entries, PremiumMenuScreen.MenuTab.DIMENSION_MENU, name, englishName, "Teleportar dimensao", command, "Clique para trocar de dimensao.", icon, command);
    }
}
