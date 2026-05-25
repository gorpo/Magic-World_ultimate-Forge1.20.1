package com.magicworld.client.menus;

import com.magicworld.client.PremiumEntry;
import com.magicworld.client.PremiumMenuScreen;
import net.minecraft.world.item.Items;

import java.util.List;

public final class PremiumArmorMenu {

    private PremiumArmorMenu() {
    }

    public static void add(List<PremiumEntry> entries) {
        armor(entries, "Capacete premium", "Premium Helmet", Items.NETHERITE_HELMET, "give @s minecraft:netherite_helmet{Enchantments:[{id:\"minecraft:protection\",lvl:4s},{id:\"minecraft:respiration\",lvl:3s},{id:\"minecraft:unbreaking\",lvl:3s}]} 1");
        armor(entries, "Peitoral premium", "Premium Chestplate", Items.NETHERITE_CHESTPLATE, "give @s minecraft:netherite_chestplate{Enchantments:[{id:\"minecraft:protection\",lvl:4s},{id:\"minecraft:unbreaking\",lvl:3s}]} 1");
        armor(entries, "Calca premium", "Premium Leggings", Items.NETHERITE_LEGGINGS, "give @s minecraft:netherite_leggings{Enchantments:[{id:\"minecraft:protection\",lvl:4s},{id:\"minecraft:unbreaking\",lvl:3s}]} 1");
        armor(entries, "Botas premium", "Premium Boots", Items.NETHERITE_BOOTS, "give @s minecraft:netherite_boots{Enchantments:[{id:\"minecraft:protection\",lvl:4s},{id:\"minecraft:feather_falling\",lvl:4s},{id:\"minecraft:unbreaking\",lvl:3s}]} 1");
        armor(entries, "Habilidade armadura", "Armor Ability", Items.NETHER_STAR, "effect give @s minecraft:resistance 999999 1 true");
    }

    private static void armor(List<PremiumEntry> entries, String name, String englishName, net.minecraft.world.item.Item icon, String command) {
        MenuEntryFactory.command(entries, PremiumMenuScreen.MenuTab.PREMIUM_ARMOR, name, englishName, "Armadura premium", command, "Entrega armadura ou habilidade.", icon, command);
    }
}
