package com.magicworld.client.menus;

import com.magicworld.client.PremiumEntry;
import com.magicworld.client.PremiumMenuScreen;
import net.minecraft.world.item.Items;

import java.util.List;

public final class StructureRainMenu {

    private StructureRainMenu() {
    }

    public static void add(List<PremiumEntry> entries) {
        rain(entries, "Chuva de TNT", "TNT Rain", Items.TNT, "summon minecraft:tnt ~ ~12 ~ {Fuse:80}");
        rain(entries, "Chuva de diamantes", "Diamond Rain", Items.DIAMOND, "summon minecraft:item ~ ~5 ~ {Item:{id:\"minecraft:diamond\",Count:16b}}");
        rain(entries, "Chuva de zumbis", "Zombie Rain", Items.ZOMBIE_HEAD, "summon minecraft:zombie ~ ~8 ~");
        rain(entries, "Chuva de galinhas", "Chicken Rain", Items.EGG, "summon minecraft:chicken ~ ~8 ~");
        rain(entries, "Mini estrutura", "Mini Structure", Items.COBBLESTONE, "place structure minecraft:igloo ~ ~ ~");
    }

    private static void rain(List<PremiumEntry> entries, String name, String englishName, net.minecraft.world.item.Item icon, String command) {
        MenuEntryFactory.command(entries, PremiumMenuScreen.MenuTab.STRUCTURE_RAIN, name, englishName, "Evento de chuva", command, "Clique varias vezes para intensificar.", icon, command);
    }
}
