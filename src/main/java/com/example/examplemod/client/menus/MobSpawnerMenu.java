package com.example.examplemod.client.menus;

import com.example.examplemod.client.PremiumEntry;
import com.example.examplemod.client.PremiumMenuScreen;
import net.minecraft.world.item.Items;

import java.util.List;

public final class MobSpawnerMenu {

    private MobSpawnerMenu() {
    }

    public static void add(List<PremiumEntry> entries) {
        mob(entries, "Zombie", "minecraft:zombie", "summon minecraft:zombie ~ ~ ~", Items.ROTTEN_FLESH);
        mob(entries, "Creeper", "minecraft:creeper", "summon minecraft:creeper ~ ~ ~", Items.GUNPOWDER);
        mob(entries, "Skeleton", "minecraft:skeleton", "summon minecraft:skeleton ~ ~ ~", Items.BONE);
        mob(entries, "Enderman", "minecraft:enderman", "summon minecraft:enderman ~ ~ ~", Items.ENDER_PEARL);
        mob(entries, "Warden", "minecraft:warden", "summon minecraft:warden ~ ~ ~", Items.ECHO_SHARD);
        mob(entries, "Premium Wolf", "minecraft:wolf", "summon minecraft:wolf ~ ~ ~ {CustomName:'{\"text\":\"Premium Wolf\"}',ActiveEffects:[{Id:1b,Amplifier:2b,Duration:999999},{Id:10b,Amplifier:2b,Duration:999999}]}", Items.BONE);
        mob(entries, "Premium Golem", "minecraft:iron_golem", "summon minecraft:iron_golem ~ ~ ~ {CustomName:'{\"text\":\"Premium Golem\"}',ActiveEffects:[{Id:5b,Amplifier:2b,Duration:999999}]}", Items.IRON_BLOCK);
    }

    private static void mob(List<PremiumEntry> entries, String name, String id, String command, net.minecraft.world.item.Item icon) {
        MenuEntryFactory.command(entries, PremiumMenuScreen.MenuTab.MOB_SPAWNER, name, id, "Spawn de mob", command, "Clique para spawnar.", icon, command);
    }
}
