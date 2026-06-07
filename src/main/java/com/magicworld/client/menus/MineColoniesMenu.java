package com.magicworld.client.menus;

import com.magicworld.client.PremiumEntry;
import com.magicworld.client.PremiumMenuScreen;
import net.minecraft.world.item.Items;

import java.util.List;

public final class MineColoniesMenu {

    private MineColoniesMenu() {
    }

    public static void add(List<PremiumEntry> entries) {
        colony(entries, "Voltar para casa", "Home", "Retorno seguro", "Teleporta para a Casa do Ultimo Farol.", Items.RED_BED, "teleport_home");
        colony(entries, "Registrar colonia atual", "Save Colony", "Marcador manual", "Salva sua posicao atual como colonia ativa.", Items.MAP, "minecolonies_register_current");
        colony(entries, "Ir para ultima colonia", "Last Colony", "Teleporte", "Volta para a ultima colonia registrada.", Items.COMPASS, "minecolonies_teleport_last_colony");
        colony(entries, "Ir para Town Hall", "Town Hall", "Teleporte", "Volta para o ultimo Town Hall clicado.", Items.BELL, "minecolonies_teleport_town_hall");
        colony(entries, "Ultima construcao", "Last Building", "Teleporte", "Volta para o ultimo bloco MineColonies usado.", Items.BRICKS, "minecolonies_teleport_last_building");
    }

    private static void colony(
            List<PremiumEntry> entries,
            String name,
            String englishName,
            String category,
            String description,
            net.minecraft.world.item.Item icon,
            String action
    ) {
        MenuEntryFactory.command(
                entries,
                PremiumMenuScreen.MenuTab.MINECOLONIES,
                name,
                englishName,
                category,
                description,
                "Integracao opcional Magic World + MineColonies.",
                icon,
                "PANEL_ACTION:" + action
        );
    }
}
