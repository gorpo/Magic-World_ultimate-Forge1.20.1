package com.example.examplemod.client.menus;

import com.example.examplemod.client.PremiumEntry;
import com.example.examplemod.client.PremiumMenuScreen;
import net.minecraft.world.item.Items;

import java.util.List;

public final class LuckyBlockMenu {

    private LuckyBlockMenu() {
    }

    public static void add(List<PremiumEntry> entries) {
        lucky(entries, "Diamantes", "Diamond Reward", Items.DIAMOND_BLOCK, "give @s minecraft:diamond 16");
        lucky(entries, "Ouro", "Gold Reward", Items.GOLD_BLOCK, "give @s minecraft:gold_ingot 32");
        lucky(entries, "XP", "Experience", Items.EXPERIENCE_BOTTLE, "xp add @s 10 levels");
        lucky(entries, "Creeper azarado", "Bad Luck Creeper", Items.CREEPER_HEAD, "summon minecraft:creeper ~ ~ ~");
        lucky(entries, "TNT azarada", "Bad Luck TNT", Items.TNT, "summon minecraft:tnt ~ ~2 ~ {Fuse:60}");
    }

    private static void lucky(List<PremiumEntry> entries, String name, String englishName, net.minecraft.world.item.Item icon, String command) {
        MenuEntryFactory.command(entries, PremiumMenuScreen.MenuTab.LUCKY_BLOCK, name, englishName, "Evento lucky", command, "Escolha um evento/recompensa.", icon, command);
    }
}
