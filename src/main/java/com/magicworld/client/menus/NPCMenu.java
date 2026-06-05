package com.magicworld.client.menus;

import com.magicworld.client.PremiumEntry;
import com.magicworld.client.PremiumMenuScreen;
import net.minecraft.world.item.Items;

import java.util.List;

public final class NPCMenu {

    private NPCMenu() {
    }

    public static void add(List<PremiumEntry> entries) {
        npc(entries, "Aldeao premium", "Premium Villager", Items.VILLAGER_SPAWN_EGG, "summon minecraft:villager ~ ~ ~ {CustomName:'{\"text\":\"Premium Villager\"}',VillagerData:{profession:\"minecraft:librarian\",level:5,type:\"minecraft:plains\"}}");
        npc(entries, "Ferreiro premium", "Premium Smith", Items.ANVIL, "summon minecraft:villager ~ ~ ~ {CustomName:'{\"text\":\"Premium Smith\"}',VillagerData:{profession:\"minecraft:weaponsmith\",level:5,type:\"minecraft:plains\"}}");
        npc(entries, "Mercador premium", "Premium Trader", Items.EMERALD, "summon minecraft:wandering_trader ~ ~ ~ {CustomName:'{\"text\":\"Premium Trader\"}'}");
        npc(entries, "Guarda premium", "Premium Guard", Items.IRON_SWORD, "summon minecraft:vindicator ~ ~ ~ {CustomName:'{\"text\":\"Premium Guard\"}'}");
    }

    private static void npc(List<PremiumEntry> entries, String name, String englishName, net.minecraft.world.item.Item icon, String command) {
        MenuEntryFactory.command(entries, PremiumMenuScreen.MenuTab.NPC_MENU, name, englishName, "NPC custom", command, "Spawna NPC configurado.", icon, command);
    }
}
