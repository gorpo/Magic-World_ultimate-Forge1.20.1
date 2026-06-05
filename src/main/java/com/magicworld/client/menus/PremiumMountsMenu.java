package com.magicworld.client.menus;

import com.magicworld.client.PremiumEntry;
import com.magicworld.client.PremiumMenuScreen;
import net.minecraft.world.item.Items;

import java.util.List;

public final class PremiumMountsMenu {

    private PremiumMountsMenu() {
    }

    public static void add(List<PremiumEntry> entries) {
        mount(entries, "Cavalo premium", "Premium Horse", Items.SADDLE, "summon minecraft:horse ~ ~ ~ {Tame:1b,SaddleItem:{id:\"minecraft:saddle\",Count:1b},CustomName:'{\"text\":\"Premium Horse\"}',ActiveEffects:[{Id:1b,Amplifier:2b,Duration:999999}]}");
        mount(entries, "Camelo premium", "Premium Camel", Items.CAMEL_SPAWN_EGG, "summon minecraft:camel ~ ~ ~ {CustomName:'{\"text\":\"Premium Camel\"}',ActiveEffects:[{Id:1b,Amplifier:1b,Duration:999999}]}");
        mount(entries, "Porco foguete", "Rocket Pig", Items.CARROT_ON_A_STICK, "summon minecraft:pig ~ ~ ~ {Saddle:1b,CustomName:'{\"text\":\"Rocket Pig\"}',ActiveEffects:[{Id:1b,Amplifier:4b,Duration:999999}]}");
        mount(entries, "Strider premium", "Premium Strider", Items.WARPED_FUNGUS_ON_A_STICK, "summon minecraft:strider ~ ~ ~ {Saddle:1b,CustomName:'{\"text\":\"Premium Strider\"}'}");
    }

    private static void mount(List<PremiumEntry> entries, String name, String englishName, net.minecraft.world.item.Item icon, String command) {
        MenuEntryFactory.command(entries, PremiumMenuScreen.MenuTab.PREMIUM_MOUNTS, name, englishName, "Montaria premium", command, "Spawna montaria especial.", icon, command);
    }
}
