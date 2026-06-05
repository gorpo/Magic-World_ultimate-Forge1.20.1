package com.magicworld.client.menus;

import com.magicworld.client.PremiumEntry;
import com.magicworld.client.PremiumMenuScreen;
import net.minecraft.world.item.Items;

import java.util.List;

public final class TimeControlMenu {

    private TimeControlMenu() {
    }

    public static void add(List<PremiumEntry> entries) {
        time(entries, "Dia", "Day", Items.SUNFLOWER, "time set day");
        time(entries, "Noite", "Night", Items.BLACK_BED, "time set night");
        time(entries, "Meia noite", "Midnight", Items.CLOCK, "time set midnight");
        time(entries, "Adicionar 1000", "Add Time", Items.REDSTONE, "time add 1000");
        time(entries, "Parar ciclo", "Stop Cycle", Items.BARRIER, "gamerule doDaylightCycle false");
        time(entries, "Ativar ciclo", "Start Cycle", Items.CLOCK, "gamerule doDaylightCycle true");
    }

    private static void time(List<PremiumEntry> entries, String name, String englishName, net.minecraft.world.item.Item icon, String command) {
        MenuEntryFactory.command(entries, PremiumMenuScreen.MenuTab.TIME_CONTROL, name, englishName, "Controle de tempo", command, "Clique para alterar o tempo.", icon, command);
    }
}
