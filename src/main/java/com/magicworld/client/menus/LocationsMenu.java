package com.magicworld.client.menus;

import com.magicworld.client.PremiumEntry;
import com.magicworld.client.PremiumMenuScreen;
import java.util.List;
import net.minecraft.world.item.Items;

public final class LocationsMenu {
    private LocationsMenu() {
    }

    public static void add(List<PremiumEntry> entries) {
        location(entries, "Minha casa", "Home", "Spawn seguro", "Volta para o lado da cama da casa inicial.", Items.RED_BED, "location_teleport_home");
        location(entries, "Santuario", "Sanctuary", "Estrutura", "Volta para o santuario abaixo do castelo.", Items.AMETHYST_BLOCK, "location_teleport_sanctuary");
        location(entries, "Praca de portais", "Portal Plaza", "Portais", "Volta para a praca dos portais funcionais.", Items.OBSIDIAN, "location_teleport_portal_plaza");
        location(entries, "Castelo", "Castle", "Estrutura", "Volta para o castelo Magic World.", Items.BELL, "location_teleport_castle");
        location(entries, "Registrar aqui", "Register Here", "Marcador", "Salva sua posicao atual como marcador manual.", Items.MAP, "location_register_current");
        location(entries, "Marcador manual", "Manual Marker", "Teleporte", "Volta ao ultimo marcador manual salvo.", Items.FILLED_MAP, "location_teleport_manual");
        location(entries, "Ultima colonia", "Last Colony", "MineColonies", "Volta para a ultima colonia registrada.", Items.COMPASS, "location_teleport_last_colony");
        location(entries, "Town Hall", "Town Hall", "MineColonies", "Volta para o ultimo Town Hall clicado.", Items.OAK_DOOR, "location_teleport_town_hall");
        location(entries, "Ultima construcao", "Last Building", "MineColonies", "Volta para a ultima construcao MineColonies usada.", Items.BRICKS, "location_teleport_last_building");
        location(entries, "Ultimo teleporte externo", "External Teleport", "Protecao", "Volta ao ponto salvo quando MCA/MineColonies moveu voce.", Items.RECOVERY_COMPASS, "location_teleport_external");
        location(entries, "Atualizar JourneyMap", "JourneyMap Waypoints", "Mapa", "Cria/atualiza waypoints oficiais Magic World.", Items.WRITABLE_BOOK, "location_update_waypoints");
        location(entries, "Por que fui movido?", "Teleport Help", "Ajuda", "Explica teleporte externo e como voltar.", Items.BOOK, "location_explain_external_teleport");
    }

    private static void location(
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
                PremiumMenuScreen.MenuTab.LOCATIONS,
                name,
                englishName,
                category,
                description,
                "Coordenada salva no Magic World. JourneyMap recebe waypoint quando instalado.",
                icon,
                "PANEL_ACTION:" + action
        );
    }
}
