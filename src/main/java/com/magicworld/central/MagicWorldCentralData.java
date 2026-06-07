package com.magicworld.central;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.player.Player;

import java.util.List;

public final class MagicWorldCentralData {
    private MagicWorldCentralData() {
    }

    public static MagicWorldCentralSnapshot snapshot(Minecraft minecraft) {
        ClientLevel level = minecraft.level;
        Player player = minecraft.player;
        if (level == null || player == null) {
            return fallbackSnapshot("fora do mundo");
        }

        return new MagicWorldCentralSnapshot(
                "Magic World",
                "varinha, fazendas e portais",
                0,
                0,
                0,
                0,
                0,
                plannedResidents(),
                sectors(),
                List.of()
        );
    }

    private static MagicWorldCentralSnapshot fallbackSnapshot(String reason) {
        return new MagicWorldCentralSnapshot(
                "Magic World",
                "central magica / " + reason,
                plannedResidents().size(),
                0,
                0,
                0,
                0,
                plannedResidents(),
                sectors(),
                List.of()
        );
    }

    private static List<MagicWorldCentralResidentPlan> plannedResidents() {
        return List.of();
    }

    private static List<MagicWorldCentralSnapshot.SectorStatus> sectors() {
        return List.of(
                sector(MagicWorldCentralSector.HOUSE, "pronto", "casa inicial e decoracao"),
                sector(MagicWorldCentralSector.CASTLE, "pronto", "castelo como marco central"),
                sector(MagicWorldCentralSector.FARM, "ativo", "lavouras e comida animal"),
                sector(MagicWorldCentralSector.STABLES, "ativo", "currais com portoes"),
                sector(MagicWorldCentralSector.PORTAL, "preservado", "portal premium intacto"),
                sector(MagicWorldCentralSector.DEFENSE, "magico", "aura e protecoes do jogador"),
                sector(MagicWorldCentralSector.SUPPORT, "leve", "menus prontos para novas etapas")
        );
    }

    private static MagicWorldCentralSnapshot.SectorStatus sector(MagicWorldCentralSector sector, String status, String note) {
        return new MagicWorldCentralSnapshot.SectorStatus(sector, status, note);
    }
}
