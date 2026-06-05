package com.magicworld.client.menus;

import com.magicworld.client.PremiumEntry;
import com.magicworld.client.PremiumMenuScreen;
import net.minecraft.world.item.Items;

import java.util.List;

public final class WorldEventsMenu {

    private WorldEventsMenu() {
    }

    public static void add(List<PremiumEntry> entries) {
        event(entries, "Bencao global", "Global Blessing", Items.BEACON, "effect give @a minecraft:regeneration 60 1 true");
        event(entries, "Noite global", "Global Night", Items.CLOCK, "time set night");
        event(entries, "Tempestade global", "Global Storm", Items.TRIDENT, "weather thunder");
        event(entries, "Alerta premium", "Premium Alert", Items.BELL, "title @a title {\"text\":\"MagicWorld - Magic Wand\",\"color\":\"green\"}");
    }

    private static void event(List<PremiumEntry> entries, String name, String englishName, net.minecraft.world.item.Item icon, String command) {
        MenuEntryFactory.command(entries, PremiumMenuScreen.MenuTab.WORLD_EVENTS, name, englishName, "Evento global", command, "Afeta o mundo ou jogadores.", icon, command);
    }
}
