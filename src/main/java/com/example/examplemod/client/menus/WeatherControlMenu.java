package com.example.examplemod.client.menus;

import com.example.examplemod.client.PremiumEntry;
import com.example.examplemod.client.PremiumMenuScreen;
import net.minecraft.world.item.Items;

import java.util.List;

public final class WeatherControlMenu {

    private WeatherControlMenu() {
    }

    public static void add(List<PremiumEntry> entries) {
        weather(entries, "Clima limpo", "Clear", Items.SUNFLOWER, "weather clear");
        weather(entries, "Chuva", "Rain", Items.WATER_BUCKET, "weather rain");
        weather(entries, "Tempestade", "Thunder", Items.TRIDENT, "weather thunder");
        weather(entries, "Neblina escura", "Fog", Items.GRAY_DYE, "effect give @s minecraft:blindness 20 0 true");
        weather(entries, "Remover neblina", "Clear Fog", Items.MILK_BUCKET, "effect clear @s minecraft:blindness");
    }

    private static void weather(List<PremiumEntry> entries, String name, String englishName, net.minecraft.world.item.Item icon, String command) {
        MenuEntryFactory.command(entries, PremiumMenuScreen.MenuTab.WEATHER_CONTROL, name, englishName, "Controle de clima", command, "Clique para aplicar.", icon, command);
    }
}
