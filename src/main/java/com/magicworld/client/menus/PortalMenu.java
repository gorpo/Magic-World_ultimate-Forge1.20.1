package com.magicworld.client.menus;

import com.magicworld.client.PremiumEntry;
import com.magicworld.client.PremiumMenuScreen;
import net.minecraft.world.item.Items;

import java.util.List;

public final class PortalMenu {

    private PortalMenu() {
    }

    public static void add(List<PremiumEntry> entries) {
        portal(entries, "Portal do Nether", "Nether Portal Block", Items.OBSIDIAN, "setblock ~ ~ ~ minecraft:nether_portal");
        portal(entries, "Bloco do End Portal", "End Portal Block", Items.END_PORTAL_FRAME, "setblock ~ ~ ~ minecraft:end_portal");
        portal(entries, "Portal gateway", "End Gateway", Items.END_CRYSTAL, "setblock ~ ~ ~ minecraft:end_gateway");
        portal(entries, "Limpar portal", "Clear Portal", Items.BARRIER, "setblock ~ ~ ~ minecraft:air");
    }

    private static void portal(List<PremiumEntry> entries, String name, String englishName, net.minecraft.world.item.Item icon, String command) {
        MenuEntryFactory.command(entries, PremiumMenuScreen.MenuTab.PORTAL_MENU, name, englishName, "Portal instantaneo", command, "Coloca/remover portal no seu bloco.", icon, command);
    }
}
