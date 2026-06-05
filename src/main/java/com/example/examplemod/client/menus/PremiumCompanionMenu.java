package com.example.examplemod.client.menus;

import com.example.examplemod.client.PremiumEntry;
import com.example.examplemod.client.PremiumMenuScreen;
import net.minecraft.world.item.Items;

import java.util.List;

public final class PremiumCompanionMenu {

    private PremiumCompanionMenu() {
    }

    public static void add(List<PremiumEntry> entries) {
        pet(entries, "Lobo premium", "Premium Wolf", Items.BONE, "summon minecraft:wolf ~ ~ ~ {CustomName:'{\"text\":\"Premium Wolf\"}',ActiveEffects:[{Id:1b,Amplifier:1b,Duration:999999},{Id:10b,Amplifier:1b,Duration:999999}]}");
        pet(entries, "Gato premium", "Premium Cat", Items.COD, "summon minecraft:cat ~ ~ ~ {CustomName:'{\"text\":\"Premium Cat\"}',ActiveEffects:[{Id:1b,Amplifier:1b,Duration:999999}]}");
        pet(entries, "Allay premium", "Premium Allay", Items.AMETHYST_SHARD, "summon minecraft:allay ~ ~ ~ {CustomName:'{\"text\":\"Premium Allay\"}',ActiveEffects:[{Id:24b,Amplifier:0b,Duration:999999}]}");
        pet(entries, "Golem guarda", "Guard Golem", Items.IRON_BLOCK, "summon minecraft:iron_golem ~ ~ ~ {CustomName:'{\"text\":\"Guard Golem\"}'}");
    }

    private static void pet(List<PremiumEntry> entries, String name, String englishName, net.minecraft.world.item.Item icon, String command) {
        MenuEntryFactory.command(entries, PremiumMenuScreen.MenuTab.PREMIUM_COMPANION, name, englishName, "Companheiro", command, "Spawn de aliado premium.", icon, command);
    }
}
