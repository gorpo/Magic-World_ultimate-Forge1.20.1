package com.example.examplemod.client.menus;

import com.example.examplemod.client.PremiumEntry;
import com.example.examplemod.client.PremiumMenuScreen;
import net.minecraft.world.item.Items;

import java.util.List;

public final class BossControlMenu {

    private BossControlMenu() {
    }

    public static void add(List<PremiumEntry> entries) {
        boss(entries, "Ender Dragon", "Ender Dragon", Items.DRAGON_HEAD, "summon minecraft:ender_dragon ~ ~10 ~");
        boss(entries, "Wither", "Wither", Items.NETHER_STAR, "summon minecraft:wither ~ ~3 ~");
        boss(entries, "Warden", "Warden", Items.ECHO_SHARD, "summon minecraft:warden ~ ~ ~");
        boss(entries, "Dificuldade facil", "Easy", Items.WOODEN_SWORD, "difficulty easy");
        boss(entries, "Dificuldade dificil", "Hard", Items.NETHERITE_SWORD, "difficulty hard");
        boss(entries, "Dificuldade pacifica", "Peaceful", Items.POPPY, "difficulty peaceful");
    }

    private static void boss(List<PremiumEntry> entries, String name, String englishName, net.minecraft.world.item.Item icon, String command) {
        MenuEntryFactory.command(entries, PremiumMenuScreen.MenuTab.BOSS_CONTROL, name, englishName, "Controle de boss", command, "Clique para executar.", icon, command);
    }
}
