package com.magicworld.client.menus;

import com.magicworld.client.PremiumEntry;
import com.magicworld.client.PremiumMenuScreen;
import net.minecraft.world.item.Items;

import java.util.List;

public final class VarinhaMagicaControlCenter {

    private VarinhaMagicaControlCenter() {
    }

    public static void add(
            List<PremiumEntry> entries
    ) {

        MenuEntryFactory.open(entries, PremiumMenuScreen.MenuTab.GRAPHICS_PROFILES, "Graficos", "GraphicsProfilesMenu", Items.SPYGLASS);
        MenuEntryFactory.open(entries, PremiumMenuScreen.MenuTab.MOB_SPAWNER, "Mob Spawner", "MobSpawnerMenu", Items.SPAWNER);
        MenuEntryFactory.open(entries, PremiumMenuScreen.MenuTab.WEATHER_CONTROL, "Controle de clima", "WeatherControlMenu", Items.WATER_BUCKET);
        MenuEntryFactory.open(entries, PremiumMenuScreen.MenuTab.BIOME_TELEPORT, "Teleportar bioma", "BiomeTeleportMenu", Items.COMPASS);
        MenuEntryFactory.open(entries, PremiumMenuScreen.MenuTab.DIMENSION_MENU, "Dimensoes", "DimensionMenu", Items.ENDER_EYE);
        MenuEntryFactory.open(entries, PremiumMenuScreen.MenuTab.PREMIUM_POWERS, "Poderes premium", "PremiumPowersMenu", Items.NETHER_STAR);
        MenuEntryFactory.open(entries, PremiumMenuScreen.MenuTab.STRUCTURE_RAIN, "Chuva especial", "StructureRainMenu", Items.TNT);
        MenuEntryFactory.open(entries, PremiumMenuScreen.MenuTab.WORLD_EVENTS, "Eventos globais", "WorldEventsMenu", Items.BEACON);
        MenuEntryFactory.open(entries, PremiumMenuScreen.MenuTab.TROLL_MENU, "Troll", "TrollMenu", Items.CREEPER_HEAD);
        MenuEntryFactory.open(entries, PremiumMenuScreen.MenuTab.TRANSFORMATION_ENCYCLOPEDIA, "Enciclopedia", "TransformationEncyclopedia", Items.BOOK);
        MenuEntryFactory.open(entries, PremiumMenuScreen.MenuTab.PREMIUM_COMPANION, "Companheiros", "PremiumCompanionMenu", Items.BONE);
        MenuEntryFactory.open(entries, PremiumMenuScreen.MenuTab.BOSS_CONTROL, "Bosses", "BossControlMenu", Items.DRAGON_HEAD);
        MenuEntryFactory.open(entries, PremiumMenuScreen.MenuTab.LUCKY_BLOCK, "Lucky Block", "LuckyBlockMenu", Items.GOLD_BLOCK);
        MenuEntryFactory.open(entries, PremiumMenuScreen.MenuTab.TIME_CONTROL, "Tempo", "TimeControlMenu", Items.CLOCK);
        MenuEntryFactory.open(entries, PremiumMenuScreen.MenuTab.PARTICLE_EFFECTS, "Particulas", "ParticleEffectsMenu", Items.AMETHYST_SHARD);
        MenuEntryFactory.open(entries, PremiumMenuScreen.MenuTab.PORTAL_MENU, "Portais", "PortalMenu", Items.OBSIDIAN);
        MenuEntryFactory.open(entries, PremiumMenuScreen.MenuTab.DUNGEON_SPAWNER, "Dungeons", "DungeonSpawnerMenu", Items.MOSSY_COBBLESTONE);
        MenuEntryFactory.open(entries, PremiumMenuScreen.MenuTab.PREMIUM_TOOLS, "Ferramentas", "PremiumToolsMenu", Items.NETHERITE_PICKAXE);
        MenuEntryFactory.open(entries, PremiumMenuScreen.MenuTab.PREMIUM_ARMOR, "Armaduras", "PremiumArmorMenu", Items.NETHERITE_CHESTPLATE);
        MenuEntryFactory.open(entries, PremiumMenuScreen.MenuTab.NPC_MENU, "NPCs", "NPCMenu", Items.VILLAGER_SPAWN_EGG);
        MenuEntryFactory.open(entries, PremiumMenuScreen.MenuTab.WAVE_SURVIVAL, "Hordas", "WaveSurvivalMenu", Items.ZOMBIE_HEAD);
        MenuEntryFactory.open(entries, PremiumMenuScreen.MenuTab.PREMIUM_MOUNTS, "Montarias", "PremiumMountsMenu", Items.SADDLE);
    }
}
