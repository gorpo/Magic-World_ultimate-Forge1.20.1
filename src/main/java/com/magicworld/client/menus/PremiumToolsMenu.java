package com.magicworld.client.menus;

import com.magicworld.client.PremiumEntry;
import com.magicworld.client.PremiumMenuScreen;
import net.minecraft.world.item.Items;

import java.util.List;

public final class PremiumToolsMenu {

    private PremiumToolsMenu() {
    }

    public static void add(List<PremiumEntry> entries) {
        tool(entries, "Picareta premium", "Premium Pickaxe", Items.NETHERITE_PICKAXE, "give @s minecraft:netherite_pickaxe{Enchantments:[{id:\"minecraft:efficiency\",lvl:5s},{id:\"minecraft:unbreaking\",lvl:3s},{id:\"minecraft:fortune\",lvl:3s}]} 1");
        tool(entries, "Machado premium", "Premium Axe", Items.NETHERITE_AXE, "give @s minecraft:netherite_axe{Enchantments:[{id:\"minecraft:efficiency\",lvl:5s},{id:\"minecraft:unbreaking\",lvl:3s}]} 1");
        tool(entries, "Pa premium", "Premium Shovel", Items.NETHERITE_SHOVEL, "give @s minecraft:netherite_shovel{Enchantments:[{id:\"minecraft:efficiency\",lvl:5s},{id:\"minecraft:unbreaking\",lvl:3s}]} 1");
        tool(entries, "Espada premium", "Premium Sword", Items.NETHERITE_SWORD, "give @s minecraft:netherite_sword{Enchantments:[{id:\"minecraft:sharpness\",lvl:5s},{id:\"minecraft:unbreaking\",lvl:3s},{id:\"minecraft:looting\",lvl:3s}]} 1");
        tool(entries, "Arco premium", "Premium Bow", Items.BOW, "give @s minecraft:bow{Enchantments:[{id:\"minecraft:power\",lvl:5s},{id:\"minecraft:infinity\",lvl:1s},{id:\"minecraft:unbreaking\",lvl:3s}]} 1");
    }

    private static void tool(List<PremiumEntry> entries, String name, String englishName, net.minecraft.world.item.Item icon, String command) {
        MenuEntryFactory.command(entries, PremiumMenuScreen.MenuTab.PREMIUM_TOOLS, name, englishName, "Ferramenta premium", command, "Entrega ferramenta especial.", icon, command);
    }
}
