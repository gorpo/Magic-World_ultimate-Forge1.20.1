package com.example.examplemod.client.menus;

import com.example.examplemod.client.PremiumEntry;
import com.example.examplemod.client.PremiumMenuScreen;
import net.minecraft.world.item.Items;

import java.util.List;

public final class PremiumPowersMenu {

    private PremiumPowersMenu() {
    }

    public static void add(List<PremiumEntry> entries) {
        power(entries, "Velocidade premium", "Speed", Items.SUGAR, "effect give @s minecraft:speed 999999 2 true");
        power(entries, "Forca premium", "Strength", Items.BLAZE_POWDER, "effect give @s minecraft:strength 999999 2 true");
        power(entries, "Respirar agua", "Water Breathing", Items.TURTLE_HELMET, "effect give @s minecraft:water_breathing 999999 0 true");
        power(entries, "Visao noturna", "Night Vision", Items.GOLDEN_CARROT, "effect give @s minecraft:night_vision 999999 0 true");
        power(entries, "Limpar poderes", "Clear Powers", Items.MILK_BUCKET, "effect clear @s");
    }

    private static void power(List<PremiumEntry> entries, String name, String englishName, net.minecraft.world.item.Item icon, String command) {
        MenuEntryFactory.command(entries, PremiumMenuScreen.MenuTab.PREMIUM_POWERS, name, englishName, "Poder especial", command, "Clique para ativar/desativar.", icon, command);
    }
}
