package com.magicworld.client.menus;

import com.magicworld.client.PremiumEntry;
import com.magicworld.client.PremiumMenuScreen;
import net.minecraft.world.item.Item;

import java.util.List;

final class MenuEntryFactory {

    private MenuEntryFactory() {
    }

    static void command(
            List<PremiumEntry> entries,
            PremiumMenuScreen.MenuTab tab,
            String name,
            String englishName,
            String category,
            String description,
            String attributes,
            Item icon,
            String command
    ) {

        entries.add(
                new PremiumEntry(
                        tab,
                        name,
                        englishName,
                        category,
                        description,
                        attributes,
                        icon,
                        false,
                        command,
                        ""
                )
        );
    }

    static void open(
            List<PremiumEntry> entries,
            PremiumMenuScreen.MenuTab target,
            String name,
            String englishName,
            Item icon
    ) {

        command(
                entries,
                PremiumMenuScreen.MenuTab.CONTROL_CENTER,
                name,
                "",
                "Menu premium",
                "Abre " + name + ".",
                "Clique para abrir este menu.",
                icon,
                "OPEN_MENU:" + target.name()
        );
    }
}
