package com.example.examplemod.client.menus;

import com.example.examplemod.client.PremiumEntry;
import com.example.examplemod.client.PremiumMenuScreen;
import net.minecraft.world.item.Items;

import java.util.List;

public final class TrollMenu {

    private TrollMenu() {
    }

    public static void add(List<PremiumEntry> entries) {
        troll(entries, "Tela escura", "Blindness", Items.INK_SAC, "effect give @s minecraft:blindness 10 0 true");
        troll(entries, "Pulo absurdo", "Jump Scare", Items.SLIME_BLOCK, "effect give @s minecraft:jump_boost 20 8 true");
        troll(entries, "Galinha surpresa", "Chicken Surprise", Items.EGG, "summon minecraft:chicken ~ ~ ~ {CustomName:'{\"text\":\"Surpresa\"}'}");
        troll(entries, "Creeper falso", "Fake Creeper", Items.CREEPER_HEAD, "summon minecraft:creeper ~ ~ ~ {Fuse:9999}");
        troll(entries, "Levitar", "Levitation", Items.SHULKER_SHELL, "effect give @s minecraft:levitation 5 1 true");
    }

    private static void troll(List<PremiumEntry> entries, String name, String englishName, net.minecraft.world.item.Item icon, String command) {
        MenuEntryFactory.command(entries, PremiumMenuScreen.MenuTab.TROLL_MENU, name, englishName, "Efeito troll", command, "Efeito caotico e divertido.", icon, command);
    }
}
