package com.magicworld.client.menus;

import com.magicworld.client.PremiumEntry;
import com.magicworld.client.PremiumMenuScreen;
import net.minecraft.world.item.Items;

import java.util.List;

public final class DungeonSpawnerMenu {

    private DungeonSpawnerMenu() {
    }

    public static void add(List<PremiumEntry> entries) {
        dungeon(entries, "Mina abandonada", "Mineshaft", Items.RAIL, "place structure minecraft:mineshaft ~ ~ ~");
        dungeon(entries, "Stronghold", "Stronghold", Items.END_PORTAL_FRAME, "place structure minecraft:stronghold ~ ~ ~");
        dungeon(entries, "Cidade antiga", "Ancient City", Items.SCULK, "place structure minecraft:ancient_city ~ ~ ~");
        dungeon(entries, "Templo selva", "Jungle Pyramid", Items.MOSSY_COBBLESTONE, "place structure minecraft:jungle_pyramid ~ ~ ~");
        dungeon(entries, "Templo deserto", "Desert Pyramid", Items.CHISELED_SANDSTONE, "place structure minecraft:desert_pyramid ~ ~ ~");
    }

    private static void dungeon(List<PremiumEntry> entries, String name, String englishName, net.minecraft.world.item.Item icon, String command) {
        MenuEntryFactory.command(entries, PremiumMenuScreen.MenuTab.DUNGEON_SPAWNER, name, englishName, "Dungeon", command, "Spawna estrutura tipo dungeon.", icon, command);
    }
}
