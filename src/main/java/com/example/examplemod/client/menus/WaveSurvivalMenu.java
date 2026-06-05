package com.example.examplemod.client.menus;

import com.example.examplemod.client.PremiumEntry;
import com.example.examplemod.client.PremiumMenuScreen;
import net.minecraft.world.item.Items;

import java.util.List;

public final class WaveSurvivalMenu {

    private WaveSurvivalMenu() {
    }

    public static void add(List<PremiumEntry> entries) {
        wave(entries, "Horda zombie", "Zombie Wave", Items.ZOMBIE_HEAD, "summon minecraft:zombie ~ ~ ~ {CustomName:'{\"text\":\"Wave Zombie\"}'}");
        wave(entries, "Horda skeleton", "Skeleton Wave", Items.SKELETON_SKULL, "summon minecraft:skeleton ~ ~ ~ {CustomName:'{\"text\":\"Wave Skeleton\"}'}");
        wave(entries, "Horda pillager", "Pillager Wave", Items.CROSSBOW, "summon minecraft:pillager ~ ~ ~ {CustomName:'{\"text\":\"Wave Pillager\"}'}");
        wave(entries, "Mini boss", "Mini Boss", Items.NETHERITE_SWORD, "summon minecraft:zombie ~ ~ ~ {CustomName:'{\"text\":\"Mini Boss\"}',Attributes:[{Name:\"generic.max_health\",Base:80}],Health:80f,HandItems:[{id:\"minecraft:netherite_sword\",Count:1b},{}]}");
    }

    private static void wave(List<PremiumEntry> entries, String name, String englishName, net.minecraft.world.item.Item icon, String command) {
        MenuEntryFactory.command(entries, PremiumMenuScreen.MenuTab.WAVE_SURVIVAL, name, englishName, "Horda", command, "Clique varias vezes para montar a horda.", icon, command);
    }
}
