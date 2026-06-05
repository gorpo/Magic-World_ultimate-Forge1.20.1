package com.example.examplemod.client.menus;

import com.example.examplemod.client.PremiumEntry;
import com.example.examplemod.client.PremiumMenuScreen;
import net.minecraft.world.item.Items;

import java.util.List;

public final class ParticleEffectsMenu {

    private ParticleEffectsMenu() {
    }

    public static void add(List<PremiumEntry> entries) {
        particle(entries, "Coracoes", "Hearts", Items.POPPY, "particle minecraft:heart ~ ~1 ~ 0.5 0.8 0.5 0.1 20 force");
        particle(entries, "Chamas", "Flames", Items.BLAZE_POWDER, "particle minecraft:flame ~ ~1 ~ 0.6 0.8 0.6 0.02 40 force");
        particle(entries, "Portal", "Portal", Items.ENDER_PEARL, "particle minecraft:portal ~ ~1 ~ 0.6 1 0.6 0.1 80 force");
        particle(entries, "Totem", "Totem", Items.TOTEM_OF_UNDYING, "particle minecraft:totem_of_undying ~ ~1 ~ 0.6 1 0.6 0.1 80 force");
    }

    private static void particle(List<PremiumEntry> entries, String name, String englishName, net.minecraft.world.item.Item icon, String command) {
        MenuEntryFactory.command(entries, PremiumMenuScreen.MenuTab.PARTICLE_EFFECTS, name, englishName, "Efeito visual", command, "Cria particulas perto do jogador.", icon, command);
    }
}
