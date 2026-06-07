package com.magicworld.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.magicworld.client.menus.BiomeTeleportMenu;
import com.magicworld.client.menus.BossControlMenu;
import com.magicworld.client.menus.DimensionMenu;
import com.magicworld.client.menus.DungeonSpawnerMenu;
import com.magicworld.client.menus.GraphicsProfilesMenu;
import com.magicworld.client.menus.LuckyBlockMenu;
import com.magicworld.client.menus.VarinhaMagicaControlCenter;
import com.magicworld.client.menus.MobSpawnerMenu;
import com.magicworld.client.menus.NPCMenu;
import com.magicworld.client.menus.ParticleEffectsMenu;
import com.magicworld.client.menus.PortalMenu;
import com.magicworld.client.menus.PremiumArmorMenu;
import com.magicworld.client.menus.PremiumCompanionMenu;
import com.magicworld.client.menus.PremiumMountsMenu;
import com.magicworld.client.menus.PremiumPowersMenu;
import com.magicworld.client.menus.PremiumToolsMenu;
import com.magicworld.client.menus.StructureRainMenu;
import com.magicworld.client.menus.TimeControlMenu;
import com.magicworld.client.menus.TransformationEncyclopedia;
import com.magicworld.client.menus.TrollMenu;
import com.magicworld.client.menus.WaveSurvivalMenu;
import com.magicworld.client.menus.WeatherControlMenu;
import com.magicworld.client.menus.WorldEventsMenu;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.nio.file.Files;
import java.nio.file.Path;
import net.minecraft.Util;
import net.minecraftforge.fml.ModList;

public class PremiumMenuScreen extends Screen {

    // Palette sampled from the paired MagicWorld resource pack GUI textures.
    private static final int NEON =
            0xFF22D3FF;

    private static final int WHITE =
            0xFFE2DBFF;

    private static final int PANEL =
            0xDD050814;

    private static final int STONE =
            0xFF120D29;

    private static final int STONE_LIGHT =
            0xFFA96DF2;

    private static final int STONE_DARK =
            0xFF050814;

    private static final int PREMIUM_GLASS =
            0xFF2F346D;

    private static final int VANILLA_BUTTON =
            0xFF0C0D1F;

    private static final int VANILLA_BUTTON_LIGHT =
            0xFF22D3FF;

    private static final int VANILLA_BUTTON_DARK =
            0xFF140E2A;

    private static final int BUTTON_HOVER =
            0xFF170F2F;

    private static final int BORDER =
            0xFFB373FF;

    private static final int INSET =
            0xDD050814;

    private static final int SHADOW =
            0x88000000;

    private static final int TEXT_SHADOW =
            0xFF050814;

    private static final int SLOT_HOVER =
            0x6622D3FF;

    private static final int SEARCH_FOCUS =
            0x6622D3FF;

    private static final int SEARCH_HOVER =
            0x33140E2A;

    private static final int PLACEHOLDER =
            0xFF6F87E7;

    private static final int CLEAR_TEXT =
            0xFFFF8FD0;

    private static final int LIST_ROW =
            0x880C0D1F;

    private static final int LIST_ROW_HOVER =
            0xAA140E2A;

    private static final int PREVIEW_TEXTURE_SIZE =
            256;
    private static final int TAB_HEIGHT =
            16;
    private static final int TAB_ROW_GAP =
            3;
    private static final int TAB_MIN_WIDTH =
            58;

    private PremiumList premiumList;

    private final List<PremiumEntry> premiumEntries =
            new ArrayList<>();

    private final MenuTab activeTab;

    private static boolean commandMenusEnabled =
            true;

    private static SavedTeleport savedTeleport;

    private int itemGridScroll =
            0;

    private int itemCategoryIndex =
            0;

    private String itemSearchText =
            "";

    private boolean itemSearchFocused =
            false;

    private static final String[] ITEM_CATEGORIES =
            new String[] {
                    "Todos",
                    "Blocos",
                    "Tools",
                    "Combate",
                    "Comida",
                    "Redstone",
                    "Ovos"
            };

    public enum MenuTab {
        WAND("Wand"),
        BLOCKS("Blocos"),
        ENEMIES("Inimigos"),
        ANIMALS("Animais"),
        CREATED("Criados"),
        SPAWN_ITEMS("Itens"),
        SPAWN_VILLAGES("Vilas"),
        SPAWN_CONSTRUCTIONS("Constru."),
        COMMANDS_GATE("Cmds"),
        CONTROL_CENTER("Sistema"),
        GRAPHICS_PROFILES("Graficos"),
        MOB_SPAWNER("Mobs"),
        WEATHER_CONTROL("Clima"),
        BIOME_TELEPORT("Biomas"),
        DIMENSION_MENU("Dimensoes"),
        PREMIUM_POWERS("Poderes"),
        STRUCTURE_RAIN("Chuva"),
        WORLD_EVENTS("Eventos"),
        TROLL_MENU("Troll"),
        TRANSFORMATION_ENCYCLOPEDIA("Enciclop."),
        PREMIUM_COMPANION("Pets"),
        BOSS_CONTROL("Bosses"),
        LUCKY_BLOCK("Lucky"),
        TIME_CONTROL("Tempo"),
        PARTICLE_EFFECTS("Particulas"),
        PORTAL_MENU("Portais"),
        DUNGEON_SPAWNER("Dungeons"),
        PREMIUM_TOOLS("Tools"),
        PREMIUM_ARMOR("Armaduras"),
        NPC_MENU("NPCs"),
        WAVE_SURVIVAL("Hordas"),
        PREMIUM_MOUNTS("Montarias");

        private final String title;

        MenuTab(String title) {
            this.title = title;
        }

        public String getTitle() {
            return title;
        }
    }

    public PremiumMenuScreen() {
        this(MenuTab.WAND);
    }

    public PremiumMenuScreen(
            MenuTab activeTab
    ) {
        super(Component.literal("MagicWorld - Magic Wand"));
        this.activeTab = activeTab;
    }

    @Override
    protected void init() {

        super.init();

        premiumEntries.clear();
        addEntries();

        if (activeTab != MenuTab.WAND
                && activeTab != MenuTab.SPAWN_ITEMS
                && activeTab != MenuTab.COMMANDS_GATE) {
            premiumList =
                    new PremiumList(
                            minecraft
                    );

            addRenderableWidget(
                    premiumList
            );
        }
    }

    private void addEntries() {

        if (activeTab == MenuTab.BLOCKS) {
            addBlocks();
        }

        else if (activeTab == MenuTab.ENEMIES) {
            addEnemies();
        }

        else if (activeTab == MenuTab.ANIMALS) {
            addAnimals();
        }

        else if (activeTab == MenuTab.CREATED) {
            addCreatedBlocks();
        }

        else if (activeTab == MenuTab.SPAWN_ITEMS) {
            addSpawnItems();
        }

        else if (activeTab == MenuTab.SPAWN_VILLAGES) {
            addSpawnVillages();
        }

        else if (activeTab == MenuTab.SPAWN_CONSTRUCTIONS) {
            addSpawnConstructions();
        }

        else if (activeTab == MenuTab.CONTROL_CENTER) {
            VarinhaMagicaControlCenter.add(premiumEntries);
        }

        else if (activeTab == MenuTab.GRAPHICS_PROFILES) {
            GraphicsProfilesMenu.add(premiumEntries);
        }

        else if (activeTab == MenuTab.MOB_SPAWNER) {
            MobSpawnerMenu.add(premiumEntries);
        }

        else if (activeTab == MenuTab.WEATHER_CONTROL) {
            WeatherControlMenu.add(premiumEntries);
        }

        else if (activeTab == MenuTab.BIOME_TELEPORT) {
            BiomeTeleportMenu.add(premiumEntries);
        }

        else if (activeTab == MenuTab.DIMENSION_MENU) {
            DimensionMenu.add(premiumEntries);
        }

        else if (activeTab == MenuTab.PREMIUM_POWERS) {
            PremiumPowersMenu.add(premiumEntries);
        }

        else if (activeTab == MenuTab.STRUCTURE_RAIN) {
            StructureRainMenu.add(premiumEntries);
        }

        else if (activeTab == MenuTab.WORLD_EVENTS) {
            WorldEventsMenu.add(premiumEntries);
        }

        else if (activeTab == MenuTab.TROLL_MENU) {
            TrollMenu.add(premiumEntries);
        }

        else if (activeTab == MenuTab.TRANSFORMATION_ENCYCLOPEDIA) {
            TransformationEncyclopedia.add(premiumEntries);
        }

        else if (activeTab == MenuTab.PREMIUM_COMPANION) {
            PremiumCompanionMenu.add(premiumEntries);
        }

        else if (activeTab == MenuTab.BOSS_CONTROL) {
            BossControlMenu.add(premiumEntries);
        }

        else if (activeTab == MenuTab.LUCKY_BLOCK) {
            LuckyBlockMenu.add(premiumEntries);
        }

        else if (activeTab == MenuTab.TIME_CONTROL) {
            TimeControlMenu.add(premiumEntries);
        }

        else if (activeTab == MenuTab.PARTICLE_EFFECTS) {
            ParticleEffectsMenu.add(premiumEntries);
        }

        else if (activeTab == MenuTab.PORTAL_MENU) {
            PortalMenu.add(premiumEntries);
        }

        else if (activeTab == MenuTab.DUNGEON_SPAWNER) {
            DungeonSpawnerMenu.add(premiumEntries);
        }

        else if (activeTab == MenuTab.PREMIUM_TOOLS) {
            PremiumToolsMenu.add(premiumEntries);
        }

        else if (activeTab == MenuTab.PREMIUM_ARMOR) {
            PremiumArmorMenu.add(premiumEntries);
        }

        else if (activeTab == MenuTab.NPC_MENU) {
            NPCMenu.add(premiumEntries);
        }

        else if (activeTab == MenuTab.WAVE_SURVIVAL) {
            WaveSurvivalMenu.add(premiumEntries);
        }

        else if (activeTab == MenuTab.PREMIUM_MOUNTS) {
            PremiumMountsMenu.add(premiumEntries);
        }

        if (isSystemSubMenu(activeTab)) {
            addBackToSystemEntry();
        }
    }

    private void addBackToSystemEntry() {

        premiumEntries.add(
                new PremiumEntry(
                        activeTab,
                        "Voltar",
                        "Sistema",
                        "Navegacao",
                        "Volta para o Sistema premium.",
                        "Nao executa comando.",
                        Items.BARRIER,
                        false,
                        "BACK_MENU",
                        ""
                )
        );
    }

    private void addBlocks() {

        addItem(MenuTab.BLOCKS, "Pedra", "Stone", "Bloco comum",
                "STONE / ANDESITE / DIORITE / GRANITE -> COAL_ORE. Segundo clique: COAL_ORE -> STONE.",
                "Recompensa simples para inicio do mundo.", Items.STONE,
                giveItem("minecraft:stone"),
                giveItem("minecraft:coal_ore"));

        addItem(MenuTab.BLOCKS, "Pedregulho", "Cobblestone", "Bloco comum",
                "COBBLESTONE -> IRON_ORE. Segundo clique: IRON_ORE -> COBBLESTONE.",
                "Upgrade leve sem acelerar demais o progresso.", Items.COBBLESTONE,
                giveItem("minecraft:cobblestone"),
                giveItem("minecraft:iron_ore"));

        addItem(MenuTab.BLOCKS, "Terra", "Dirt", "Natureza",
                "DIRT / GRASS_BLOCK / COARSE_DIRT -> MOSS_BLOCK.",
                "Transformacao verde premium para blocos naturais.", Items.DIRT,
                giveItem("minecraft:dirt"),
                giveItem("minecraft:moss_block"));

        addItem(MenuTab.BLOCKS, "Areia", "Sand", "Vidro",
                "SAND / RED_SAND / SANDSTONE -> GLASS. Segundo clique: GLASS -> SAND.",
                "Areia e derivados viram vidro.", Items.SAND,
                giveItem("minecraft:sand"),
                giveItem("minecraft:glass"));

        addItem(MenuTab.BLOCKS, "Folhas", "Leaves", "Natureza",
                "Todas as folhas de arvore -> EMERALD_BLOCK. Segundo clique: EMERALD_BLOCK -> OAK_LEAVES.",
                "Recompensa verde neon combinando com o tema premium.", Items.OAK_LEAVES,
                giveItem("minecraft:oak_leaves"),
                giveItem("minecraft:emerald_block"));

        addItem(MenuTab.BLOCKS, "Troncos", "Logs", "Premium",
                "Todos os LOGS -> GOLD_BLOCK. Segundo clique: GOLD_BLOCK -> OAK_LOG.",
                "Transformacao original aprovada do mod.", Items.OAK_LOG,
                giveItem("minecraft:oak_log"),
                giveItem("minecraft:gold_block"));

        addItem(MenuTab.BLOCKS, "Minerios simples", "Ores", "Upgrade",
                "COAL -> DIAMOND, COPPER -> IRON, REDSTONE -> LAPIS, LAPIS -> EMERALD.",
                "Minerios sobem de nivel mantendo progressao controlada.", Items.COAL_ORE,
                giveItem("minecraft:coal_ore"),
                giveItem("minecraft:diamond_ore"));

        addItem(MenuTab.BLOCKS, "Minerios profundos", "Deepslate Ores", "Upgrade",
                "Versoes DEEPSLATE seguem a mesma logica dos minerios normais.",
                "Mantem compatibilidade com cavernas profundas.", Items.DEEPSLATE_COAL_ORE,
                giveItem("minecraft:deepslate_coal_ore"),
                giveItem("minecraft:deepslate_diamond_ore"));
    }

    private void addEnemies() {

        addEnemy("Blaze", "Chicken", Items.BLAZE_ROD, "Set ouro + 10x64 GOLD_BLOCK", "minecraft:blaze", "minecraft:chicken");
        addEnemy("Cave Spider", "Rabbit", Items.SPIDER_EYE, "Set ferro + 10x64 IRON_BLOCK", "minecraft:cave_spider", "minecraft:rabbit");
        addEnemy("Creeper", "Cow", Items.GUNPOWDER, "Set diamante + 10x64 DIAMOND_BLOCK", "minecraft:creeper", "minecraft:cow");
        addEnemy("Drowned", "Turtle", Items.TRIDENT, "Set ferro + 10x64 IRON_BLOCK", "minecraft:drowned", "minecraft:turtle");
        addEnemy("Elder Guardian", "Turtle", Items.PRISMARINE_SHARD, "Set diamante + 10x64 SEA_LANTERN", "minecraft:elder_guardian", "minecraft:turtle");
        addEnemy("Ender Dragon", "Horse", Items.DRAGON_EGG, "Set netherite + 10x64 NETHERITE_BLOCK", "minecraft:ender_dragon", "minecraft:horse");
        addEnemy("Enderman", "Horse", Items.ENDER_PEARL, "Set diamante + 10x64 DIAMOND_BLOCK", "minecraft:enderman", "minecraft:horse");
        addEnemy("Endermite", "Rabbit", Items.ENDER_PEARL, "Set chainmail + 10x64 SLIME_BLOCK", "minecraft:endermite", "minecraft:rabbit");
        addEnemy("Evoker", "Villager", Items.TOTEM_OF_UNDYING, "Set diamante + 10x64 EMERALD_BLOCK", "minecraft:evoker", "minecraft:villager");
        addEnemy("Ghast", "Bat", Items.GHAST_TEAR, "Set ouro + 10x64 GOLD_BLOCK", "minecraft:ghast", "minecraft:bat");
        addEnemy("Giant", "Iron Golem", Items.ROTTEN_FLESH, "Set diamante + 10x64 EMERALD_BLOCK", "minecraft:giant", "minecraft:iron_golem");
        addEnemy("Guardian", "Squid", Items.PRISMARINE_SHARD, "Set diamante + 10x64 DIAMOND_BLOCK", "minecraft:guardian", "minecraft:squid");
        addEnemy("Hoglin", "Pig", Items.PORKCHOP, "Set ouro + 10x64 GOLD_BLOCK", "minecraft:hoglin", "minecraft:pig");
        addEnemy("Husk", "Camel", Items.ROTTEN_FLESH, "Set ferro + 10x64 IRON_BLOCK", "minecraft:husk", "minecraft:camel");
        addEnemy("Illusioner", "Villager", Items.BOW, "Set diamante + 10x64 EMERALD_BLOCK", "minecraft:illusioner", "minecraft:villager");
        addEnemy("Magma Cube", "Chicken", Items.MAGMA_CREAM, "Set ouro + 10x64 GOLD_BLOCK", "minecraft:magma_cube", "minecraft:chicken");
        addEnemy("Phantom", "Bat", Items.PHANTOM_MEMBRANE, "Set diamante + 10x64 DIAMOND_BLOCK", "minecraft:phantom", "minecraft:bat");
        addEnemy("Piglin", "Pig", Items.GOLD_INGOT, "Set ouro + 10x64 GOLD_BLOCK", "minecraft:piglin", "minecraft:pig");
        addEnemy("Piglin Brute", "Pig", Items.GOLDEN_AXE, "Set netherite + 10x64 NETHERITE_BLOCK", "minecraft:piglin_brute", "minecraft:pig");
        addEnemy("Pillager", "Villager", Items.CROSSBOW, "Set diamante + 10x64 EMERALD_BLOCK", "minecraft:pillager", "minecraft:villager");
        addEnemy("Ravager", "Cow", Items.SADDLE, "Set diamante + 10x64 EMERALD_BLOCK", "minecraft:ravager", "minecraft:cow");
        addEnemy("Shulker", "Turtle", Items.SHULKER_SHELL, "Set diamante + 10x64 DIAMOND_BLOCK", "minecraft:shulker", "minecraft:turtle");
        addEnemy("Silverfish", "Rabbit", Items.STONE, "Set chainmail + 10x64 SLIME_BLOCK", "minecraft:silverfish", "minecraft:rabbit");
        addEnemy("Skeleton", "Horse", Items.BONE, "Set ferro + 10x64 IRON_BLOCK", "minecraft:skeleton", "minecraft:horse");
        addEnemy("Slime", "Chicken", Items.SLIME_BALL, "Set chainmail + 10x64 SLIME_BLOCK", "minecraft:slime", "minecraft:chicken");
        addEnemy("Spider", "Rabbit", Items.STRING, "Set ferro + 10x64 IRON_BLOCK", "minecraft:spider", "minecraft:rabbit");
        addEnemy("Stray", "Snow Golem", Items.ARROW, "Set ferro + 10x64 IRON_BLOCK", "minecraft:stray", "minecraft:snow_golem");
        addEnemy("Vex", "Bat", Items.IRON_SWORD, "Set diamante + 10x64 EMERALD_BLOCK", "minecraft:vex", "minecraft:bat");
        addEnemy("Vindicator", "Villager", Items.IRON_AXE, "Set diamante + 10x64 EMERALD_BLOCK", "minecraft:vindicator", "minecraft:villager");
        addEnemy("Warden", "Iron Golem", Items.ECHO_SHARD, "Set netherite + 10x64 NETHERITE_BLOCK", "minecraft:warden", "minecraft:iron_golem");
        addEnemy("Witch", "Cat", Items.GLASS_BOTTLE, "Set diamante + 10x64 DIAMOND_BLOCK", "minecraft:witch", "minecraft:cat");
        addEnemy("Wither", "Snow Golem", Items.NETHER_STAR, "Set netherite + 10x64 NETHERITE_BLOCK", "minecraft:wither", "minecraft:snow_golem");
        addEnemy("Wither Skeleton", "Skeleton Horse", Items.WITHER_SKELETON_SKULL, "Set netherite + 10x64 NETHERITE_BLOCK", "minecraft:wither_skeleton", "minecraft:skeleton_horse");
        addEnemy("Zoglin", "Pig", Items.ROTTEN_FLESH, "Set netherite + 10x64 NETHERITE_BLOCK", "minecraft:zoglin", "minecraft:pig");
        addEnemy("Zombie", "Villager", Items.ROTTEN_FLESH, "Set ferro + 10x64 IRON_BLOCK", "minecraft:zombie", "minecraft:villager");
        addEnemy("Zombie Villager", "Villager", Items.ROTTEN_FLESH, "Set ferro + 10x64 IRON_BLOCK", "minecraft:zombie_villager", "minecraft:villager");
        addEnemy("Zombified Piglin", "Pig", Items.GOLD_NUGGET, "Set ouro + 10x64 GOLD_BLOCK", "minecraft:zombified_piglin", "minecraft:pig");
    }

    private void addAnimals() {

        addMob("Allay", "Allay", "Utilidade", "Modo premium com bonus de encantamento.", "+ Bonus encantamento",
                summonEntity("minecraft:allay"),
                summonPremiumEntity("minecraft:allay", effect(24, 0)));
        addMob("Axolotl", "Axolotl", "Utilidade", "Axolotl premium gera recompensas aquaticas.", "+ Kit prismarine",
                summonEntity("minecraft:axolotl"),
                summonEntity("minecraft:frog"));
        addMob("Morcego", "Bat", "Utilidade", "Morcego premium melhora recompensas de mineracao.", "+ Minerios",
                summonEntity("minecraft:bat"),
                summonPremiumEntity("minecraft:bat", effect(1, 2), effect(10, 2)));
        addMob("Abelha", "Bee", "Natureza", "Abelha premium vira utilidade natural.", "+ Honey / natureza",
                summonEntity("minecraft:bee"),
                summonEntity("minecraft:allay"));
        addMob("Camelo", "Camel", "Casa", "Camelo premium entrega materiais de deserto.", "+ Sandstone",
                summonEntity("minecraft:camel"),
                summonPremiumEntity("minecraft:camel", effect(1, 2), effect(10, 2)));
        addMob("Gato", "Cat", "Casa", "Gato premium entrega blocos claros decorativos.", "+ Quartz / lanternas",
                summonEntity("minecraft:cat"),
                summonPremiumEntity("minecraft:ocelot", effect(1, 2), effect(10, 2)));
        addMob("Galinha", "Chicken", "Metal", "Galinha premium entrega set diamante.", "+ Set diamante",
                summonEntity("minecraft:chicken"),
                summonPremiumEntity("minecraft:parrot", effect(1, 2)));
        addMob("Vaca", "Cow", "Metal", "Vaca premium entrega set ferro.", "+ Set ferro",
                summonEntity("minecraft:cow"),
                summonPremiumEntity("minecraft:mooshroom", effect(1, 2), effect(10, 2)));
        addMob("Creeper", "Creeper", "Utilidade", "Creeper premium entrega TNT e fogo.", "+ TNT",
                summonEntity("minecraft:creeper"),
                summonPremiumEntity("minecraft:creeper", effect(1, 2), effect(5, 2)));
        addMob("Raposa", "Fox", "Casa", "Raposa premium entrega blocos rusticos.", "+ Madeira / pedra",
                summonEntity("minecraft:fox"),
                summonPremiumEntity("minecraft:fox", effect(1, 2), effect(10, 2)));
        addMob("Sapo", "Frog", "Casa", "Sapo premium entrega blocos de pantano.", "+ Mangrove / mud",
                summonEntity("minecraft:frog"),
                summonPremiumEntity("minecraft:frog", effect(8, 2), effect(1, 2)));
        addMob("Cabra", "Goat", "Casa", "Cabra premium entrega blocos de montanha.", "+ Stone bricks",
                summonEntity("minecraft:goat"),
                summonPremiumEntity("minecraft:goat", effect(5, 2), effect(1, 2)));
        addMob("Cavalo", "Horse", "Utilidade", "Cavalo premium entrega redstone.", "+ Redstone kit",
                summonEntity("minecraft:horse"),
                summonPremiumEntity("minecraft:zombie_horse", effect(1, 2)));
        addMob("Iron Golem", "Iron Golem", "Utilidade", "Iron Golem premium entrega kit ferro.", "+ Set ferro",
                summonEntity("minecraft:iron_golem"),
                summonPremiumEntity("minecraft:iron_golem", effect(5, 2), effect(1, 2)));
        addMob("Panda", "Panda", "Casa", "Panda premium entrega bambu e decoracao.", "+ Bamboo",
                summonEntity("minecraft:panda"),
                summonPremiumEntity("minecraft:panda", effect(5, 2), effect(10, 2)));
        addMob("Porco", "Pig", "Metal", "Porco premium entrega set ouro.", "+ Set ouro",
                summonEntity("minecraft:pig"),
                summonPremiumEntity("minecraft:hoglin", effect(1, 2), effect(10, 2)));
        addMob("Coelho", "Rabbit", "Utilidade", "Coelho premium entrega kit plantacao.", "+ Fazenda",
                summonEntity("minecraft:rabbit"),
                summonPremiumEntity("minecraft:rabbit", effect(1, 2), effect(10, 2)));
        addMob("Ovelha", "Sheep", "Metal", "Ovelha premium entrega netherite.", "+ Set netherite",
                summonEntity("minecraft:sheep"),
                summonPremiumEntity("minecraft:llama", effect(1, 2)));
        addMob("Snow Golem", "Snow Golem", "Utilidade", "Snow Golem premium entrega gelo e neve.", "+ Gelo",
                summonEntity("minecraft:snow_golem"),
                summonPremiumEntity("minecraft:snow_golem", effect(10, 2)));
        addMob("Lula", "Squid", "Utilidade", "Lula premium entrega blocos escuros e tinta.", "+ Tinted glass",
                summonEntity("minecraft:squid"),
                summonPremiumEntity("minecraft:squid", effect(30, 2)));
        addMob("Tartaruga", "Turtle", "Casa", "Tartaruga premium entrega blocos aquaticos.", "+ Prismarine",
                summonEntity("minecraft:turtle"),
                summonPremiumEntity("minecraft:turtle", effect(11, 2), effect(10, 2)));
        addMob("Villager", "Villager", "Utilidade", "Villager premium entrega kit comercio.", "+ Emeralds",
                summonEntity("minecraft:villager"),
                summonEntity("minecraft:wandering_trader"));
        addMob("Lobo", "Wolf", "Casa", "Lobo premium entrega kit casa inicial.", "+ Madeira / vidro",
                summonEntity("minecraft:wolf"),
                summonPremiumEntity("minecraft:wolf", effect(1, 2), effect(5, 2), effect(10, 2), effect(12, 2)));
    }

    private void addCreatedBlocks() {

        addItem(MenuTab.CREATED, "Pranchas", "Planks", "Criado",
                "Todas as PLANKS / BAMBOO_MOSAIC -> HONEYCOMB_BLOCK. Volta: HONEYCOMB_BLOCK -> OAK_PLANKS.",
                "Decorativo premium amarelo sem usar ouro.", Items.OAK_PLANKS,
                giveItem("minecraft:oak_planks"),
                giveItem("minecraft:honeycomb_block"));

        addItem(MenuTab.CREATED, "Cercas", "Fences", "Criado",
                "Todas as FENCES -> IRON_BARS. Volta: IRON_BARS -> OAK_FENCE.",
                "Cerca comum vira cerca premium de metal.", Items.OAK_FENCE,
                giveItem("minecraft:oak_fence"),
                giveItem("minecraft:iron_bars"));

        addItem(MenuTab.CREATED, "Portoes", "Fence Gates", "Criado",
                "Todos os FENCE_GATES -> CHAIN. Volta: CHAIN -> OAK_FENCE_GATE.",
                "Portoes viram corrente decorativa.", Items.OAK_FENCE_GATE,
                giveItem("minecraft:oak_fence_gate"),
                giveItem("minecraft:chain"));

        addItem(MenuTab.CREATED, "Portas", "Doors", "Criado",
                "Todas as DOORS -> IRON_DOOR. Volta: IRON_DOOR -> OAK_DOOR.",
                "Porta comum vira porta de ferro.", Items.OAK_DOOR,
                giveItem("minecraft:oak_door"),
                giveItem("minecraft:iron_door"));

        addItem(MenuTab.CREATED, "Alcapoes", "Trapdoors", "Criado",
                "Todos os TRAPDOORS -> IRON_TRAPDOOR. Volta: IRON_TRAPDOOR -> OAK_TRAPDOOR.",
                "Alcapao comum vira alcapao de ferro.", Items.OAK_TRAPDOOR,
                giveItem("minecraft:oak_trapdoor"),
                giveItem("minecraft:iron_trapdoor"));

        addItem(MenuTab.CREATED, "Escadas", "Stairs", "Criado",
                "Todas as STAIRS de madeira -> STONE_BRICK_STAIRS. Volta: STONE_BRICK_STAIRS -> OAK_STAIRS.",
                "Escada comum vira pedra trabalhada.", Items.OAK_STAIRS,
                giveItem("minecraft:oak_stairs"),
                giveItem("minecraft:stone_brick_stairs"));

        addItem(MenuTab.CREATED, "Lajes", "Slabs", "Criado",
                "Todas as SLABS de madeira -> STONE_BRICK_SLAB. Volta: STONE_BRICK_SLAB -> OAK_SLAB.",
                "Laje comum vira laje de pedra trabalhada.", Items.OAK_SLAB,
                giveItem("minecraft:oak_slab"),
                giveItem("minecraft:stone_brick_slab"));

        addItem(MenuTab.CREATED, "Botoes", "Buttons", "Criado",
                "Todos os BUTTONS -> STONE_BUTTON. Volta: STONE_BUTTON -> OAK_BUTTON.",
                "Botao de madeira vira botao de pedra.", Items.OAK_BUTTON,
                giveItem("minecraft:oak_button"),
                giveItem("minecraft:stone_button"));

        addItem(MenuTab.CREATED, "Placas de pressao", "Pressure Plates", "Criado",
                "Todas as PRESSURE_PLATES -> LIGHT_WEIGHTED_PRESSURE_PLATE. Volta: LIGHT_WEIGHTED_PRESSURE_PLATE -> OAK_PRESSURE_PLATE.",
                "Placa comum vira placa de ouro.", Items.OAK_PRESSURE_PLATE,
                giveItem("minecraft:oak_pressure_plate"),
                giveItem("minecraft:light_weighted_pressure_plate"));

        addItem(MenuTab.CREATED, "Tochas", "Torches", "Luz",
                "TORCH / WALL_TORCH / SOUL_TORCH -> GLOWSTONE. Volta: GLOWSTONE -> TORCH.",
                "Luz pequena vira bloco luminoso premium.", Items.TORCH,
                giveItem("minecraft:torch"),
                giveItem("minecraft:glowstone"));

        addItem(MenuTab.CREATED, "Plantas pequenas", "Small Plants", "Natureza",
                "Gramas, samambaias, vines, cactus, sugar cane, bamboo, cogumelos, melon e pumpkin -> HAY_BLOCK.",
                "Volta: HAY_BLOCK -> GRASS.", Items.GRASS,
                giveItem("minecraft:grass"),
                giveItem("minecraft:hay_block"));

        addItem(MenuTab.CREATED, "Flores e vasos", "Flowers and Pots", "Decoracao",
                "Flores -> SPORE_BLOSSOM. Vasos -> DECORATED_POT.",
                "Voltam para POPPY ou FLOWER_POT padrao.", Items.POPPY,
                giveItem("minecraft:poppy"),
                giveItem("minecraft:spore_blossom"));

        addItem(MenuTab.CREATED, "Trilhos", "Rails", "Redstone",
                "RAIL / POWERED_RAIL / DETECTOR_RAIL / ACTIVATOR_RAIL -> REDSTONE_BLOCK.",
                "Volta: REDSTONE_BLOCK -> RAIL.", Items.RAIL,
                giveItem("minecraft:rail"),
                giveItem("minecraft:redstone_block"));

        addItem(MenuTab.CREATED, "Redstone simples", "Redstone", "Redstone",
                "REDSTONE_TORCH / REPEATER / COMPARATOR / LEVER / DAYLIGHT_DETECTOR -> REDSTONE_LAMP.",
                "Volta: REDSTONE_LAMP -> REDSTONE_TORCH.", Items.REDSTONE_TORCH,
                giveItem("minecraft:redstone_torch"),
                giveItem("minecraft:redstone_lamp"));

        addItem(MenuTab.CREATED, "Mesas e utilitarios", "Work Blocks", "Utilidade",
                "CRAFTING_TABLE, FURNACE, SMOKER, TABLES, ANVIL, LOOM, STONECUTTER e outros trocam em pares.",
                "Cuidado: alguns blocos com dados podem perder conteudo.", Items.CRAFTING_TABLE,
                giveItem("minecraft:crafting_table"),
                giveItem("minecraft:smithing_table"));
    }

    private void addSpawnItems() {

        for (Item item : BuiltInRegistries.ITEM) {
            if (item == Items.AIR) {
                continue;
            }

            ResourceLocation itemId =
                    BuiltInRegistries.ITEM.getKey(item);

            addItem(
                    MenuTab.SPAWN_ITEMS,
                    new ItemStack(item).getHoverName().getString(),
                    itemId.toString(),
                    itemId.getNamespace(),
                    "Item disponivel no registro criativo.",
                    "Clique para receber 1 unidade.",
                    item,
                    giveItem(itemId.toString()),
                    ""
            );
        }
    }

    private void addSpawnVillages() {

        addStructure(MenuTab.SPAWN_VILLAGES, "Vila das planicies", "Plains Village", Items.GRASS_BLOCK,
                "minecraft:village_plains", "Vila classica de planicie.");
        addStructure(MenuTab.SPAWN_VILLAGES, "Vila do deserto", "Desert Village", Items.SANDSTONE,
                "minecraft:village_desert", "Vila de deserto.");
        addStructure(MenuTab.SPAWN_VILLAGES, "Vila da savana", "Savanna Village", Items.ACACIA_LOG,
                "minecraft:village_savanna", "Vila de savana.");
        addStructure(MenuTab.SPAWN_VILLAGES, "Vila nevada", "Snowy Village", Items.SNOW_BLOCK,
                "minecraft:village_snowy", "Vila de bioma frio.");
        addStructure(MenuTab.SPAWN_VILLAGES, "Vila da taiga", "Taiga Village", Items.SPRUCE_LOG,
                "minecraft:village_taiga", "Vila de taiga.");
    }

    private void addSpawnConstructions() {

        addStructure(MenuTab.SPAWN_CONSTRUCTIONS, "Cidade antiga", "Ancient City", Items.SCULK,
                "minecraft:ancient_city", "Construcao subterranea grande.");
        addStructure(MenuTab.SPAWN_CONSTRUCTIONS, "Bastiao", "Bastion Remnant", Items.BLACKSTONE,
                "minecraft:bastion_remnant", "Construcao do Nether.");
        addStructure(MenuTab.SPAWN_CONSTRUCTIONS, "Templo do deserto", "Desert Pyramid", Items.CHISELED_SANDSTONE,
                "minecraft:desert_pyramid", "Templo de deserto.");
        addStructure(MenuTab.SPAWN_CONSTRUCTIONS, "Cidade do End", "End City", Items.PURPUR_BLOCK,
                "minecraft:end_city", "Construcao do End.");
        addStructure(MenuTab.SPAWN_CONSTRUCTIONS, "Fortaleza do Nether", "Nether Fortress", Items.NETHER_BRICKS,
                "minecraft:fortress", "Fortaleza do Nether.");
        addStructure(MenuTab.SPAWN_CONSTRUCTIONS, "Iglu", "Igloo", Items.BLUE_ICE,
                "minecraft:igloo", "Construcao pequena de neve.");
        addStructure(MenuTab.SPAWN_CONSTRUCTIONS, "Templo da selva", "Jungle Pyramid", Items.MOSSY_COBBLESTONE,
                "minecraft:jungle_pyramid", "Templo de selva.");
        addStructure(MenuTab.SPAWN_CONSTRUCTIONS, "Mansao", "Woodland Mansion", Items.DARK_OAK_LOG,
                "minecraft:mansion", "Mansao da floresta.");
        addStructure(MenuTab.SPAWN_CONSTRUCTIONS, "Mina abandonada", "Mineshaft", Items.RAIL,
                "minecraft:mineshaft", "Tunel de mina.");
        addStructure(MenuTab.SPAWN_CONSTRUCTIONS, "Monumento oceanico", "Ocean Monument", Items.PRISMARINE,
                "minecraft:monument", "Construcao oceanica.");
        addStructure(MenuTab.SPAWN_CONSTRUCTIONS, "Posto pillager", "Pillager Outpost", Items.CROSSBOW,
                "minecraft:pillager_outpost", "Posto de saqueadores.");
        addStructure(MenuTab.SPAWN_CONSTRUCTIONS, "Portal arruinado", "Ruined Portal", Items.OBSIDIAN,
                "minecraft:ruined_portal", "Portal arruinado.");
        addStructure(MenuTab.SPAWN_CONSTRUCTIONS, "Navio naufragado", "Shipwreck", Items.OAK_BOAT,
                "minecraft:shipwreck", "Navio naufragado.");
        addStructure(MenuTab.SPAWN_CONSTRUCTIONS, "Stronghold", "Stronghold", Items.END_PORTAL_FRAME,
                "minecraft:stronghold", "Fortaleza do portal do End.");
        addStructure(MenuTab.SPAWN_CONSTRUCTIONS, "Cabana do pantano", "Swamp Hut", Items.CAULDRON,
                "minecraft:swamp_hut", "Cabana de pantano.");
        addStructure(MenuTab.SPAWN_CONSTRUCTIONS, "Ruinas de trilha", "Trail Ruins", Items.SUSPICIOUS_GRAVEL,
                "minecraft:trail_ruins", "Ruinas arqueologicas.");
    }

    private void addEnemy(
            String enemy,
            String peaceful,
            Item icon,
            String reward,
            String enemyId,
            String peacefulId
    ) {

        premiumEntries.add(
                new PremiumEntry(
                        MenuTab.ENEMIES,
                        enemy,
                        peaceful,
                        "Inimigo pacificado",
                        enemy + " -> " + peaceful + ". Segundo clique no pacifico volta para " + enemy + ".",
                        "Ao matar pacifico: " + reward + ".",
                        icon,
                        hasPeacefulTexture(peaceful),
                        summonEntity(enemyId),
                        summonPremiumEnemy(peacefulId, enemyId)
                )
        );
    }

    private boolean hasPeacefulTexture(
            String peaceful
    ) {

        return !peaceful.equals("Skeleton Horse");
    }

    private void addMob(
            String name,
            String englishName,
            String category,
            String transformation,
            String attributes,
            String normalCommand,
            String modifiedCommand
    ) {

        premiumEntries.add(
                new PremiumEntry(
                        MenuTab.ANIMALS,
                        name,
                        englishName,
                        category,
                        transformation,
                        attributes,
                        Items.EMERALD,
                        true,
                        normalCommand,
                        modifiedCommand
                )
        );
    }

    private void addItem(
            MenuTab tab,
            String name,
            String englishName,
            String category,
            String transformation,
            String attributes,
            Item icon,
            String normalCommand,
            String modifiedCommand
    ) {

        premiumEntries.add(
                new PremiumEntry(
                        tab,
                        name,
                        englishName,
                        category,
                        transformation,
                        attributes,
                        icon,
                        false,
                        normalCommand,
                        modifiedCommand
                )
        );
    }

    private void addStructure(
            MenuTab tab,
            String name,
            String englishName,
            Item icon,
            String structureId,
            String attributes
    ) {

        addItem(
                tab,
                name,
                englishName,
                "Spawn por comando",
                "/place structure " + structureId + " ~ ~ ~",
                attributes + " Clique para spawnar ao seu lado.",
                icon,
                placeStructure(structureId),
                ""
        );
    }

    private String giveItem(
            String itemId
    ) {

        return "give @s "
                + itemId
                + " 1";
    }

    private String summonEntity(
            String entityId
    ) {

        return "summon "
                + entityId
                + " ~ ~ ~";
    }

    private String placeStructure(
            String structureId
    ) {

        return "place structure "
                + structureId
                + " ~ ~ ~";
    }

    private String summonPremiumEnemy(
            String peacefulId,
            String originalEnemyId
    ) {

        return "summon "
                + peacefulId
                + " ~ ~ ~ {Tags:[\"VarinhaMagicaPremiumEnemy\",\"VarinhaMagicaOriginalEnemy="
                + originalEnemyId
                + "\"],ActiveEffects:["
                + effect(24, 0)
                + "]}";
    }

    private String summonPremiumEntity(
            String entityId,
            String... effects
    ) {

        if (effects.length == 0) {
            return summonEntity(entityId);
        }

        return summonEntity(entityId)
                + " {ActiveEffects:["
                + String.join(",", effects)
                + "]}";
    }

    private String effect(
            int id,
            int amplifier
    ) {

        return "{Id:"
                + id
                + "b,Amplifier:"
                + amplifier
                + "b,Duration:999999}";
    }

    @Override
    public void render(
            GuiGraphics guiGraphics,
            int mouseX,
            int mouseY,
            float partialTick
    ) {

        int panelWidth =
                getPanelWidth();

        int panelHeight =
                getPanelHeight();

        int x =
                getPanelX();

        int y =
                getPanelY();

        // BACKGROUND FUTURO:
        // ResourceLocation background = ResourceLocation.tryBuild(
        //         "magicworld",
        //         "textures/gui/background/background.jpg"
        // );
        // guiGraphics.blit(background, x, y, 0, 0, panelWidth, panelHeight, panelWidth, panelHeight);

        drawMinecraftPanel(
                guiGraphics,
                x,
                y,
                panelWidth,
                panelHeight
        );

        guiGraphics.drawCenteredString(
                font,
                "MagicWorld - Magic Wand",
                width / 2,
                y + 8,
                NEON
        );

        guiGraphics.drawCenteredString(
                font,
                subtitle(),
                width / 2,
                y + 20,
                NEON
        );

        drawTabs(guiGraphics, mouseX, mouseY);
        drawCloseButton(guiGraphics, mouseX, mouseY);

        if (activeTab == MenuTab.WAND) {
            drawWandTab(guiGraphics, mouseX, mouseY);
        }

        if (activeTab == MenuTab.SPAWN_ITEMS) {
            drawItemGrid(guiGraphics, mouseX, mouseY);
        }

        if (activeTab == MenuTab.COMMANDS_GATE) {
            drawCommandsGate(guiGraphics, mouseX, mouseY);
        }

        super.render(
                guiGraphics,
                mouseX,
                mouseY,
                partialTick
        );
    }

    private String subtitle() {

        if (activeTab == MenuTab.WAND) return "Magic Wand";
        if (activeTab == MenuTab.BLOCKS) return "Blocos naturais e minerios";
        if (activeTab == MenuTab.ENEMIES) return "Inimigos pacificados";
        if (activeTab == MenuTab.ANIMALS) return "Animais premium";
        if (activeTab == MenuTab.CREATED) return "Objetos criados pelo jogador";
        if (activeTab == MenuTab.SPAWN_ITEMS) return "Itens do modo criativo";
        if (activeTab == MenuTab.SPAWN_VILLAGES) return "Vilas spawnaveis";
        if (activeTab == MenuTab.SPAWN_CONSTRUCTIONS) return "Construcoes spawnaveis";
        if (activeTab == MenuTab.COMMANDS_GATE) return "Ativar menus que usam comandos";
        if (activeTab == MenuTab.CONTROL_CENTER) return "Sistema premium";
        if (activeTab == MenuTab.GRAPHICS_PROFILES) return "Perfis graficos, resource packs e shaders";
        return activeTab.getTitle();
    }

    private void drawItemGrid(
            GuiGraphics guiGraphics,
            int mouseX,
            int mouseY
    ) {

        int left =
                getPanelX() + 16;

        drawItemCategoryTabs(guiGraphics, mouseX, mouseY);
        drawItemSearch(guiGraphics, mouseX, mouseY);

        List<PremiumEntry> visibleEntries =
                filteredItemEntries();

        int top =
                getItemGridTop();

        int columns =
                Math.max(1, (getPanelWidth() - 42) / 24);

        int slotSize =
                22;

        int visibleRows =
                Math.max(1, (getListBottom() - top - 4) / slotSize);

        int startIndex =
                itemGridScroll * columns;

        int hoveredIndex =
                -1;

        for (int row = 0; row < visibleRows; row++) {
            for (int column = 0; column < columns; column++) {
                int index =
                        startIndex + row * columns + column;

                if (index >= visibleEntries.size()) {
                    break;
                }

                int slotX =
                        left + column * slotSize;

                int slotY =
                        top + row * slotSize;

                boolean hovered =
                        mouseX >= slotX
                                && mouseX <= slotX + 20
                                && mouseY >= slotY
                                && mouseY <= slotY + 20;

                drawInsetBox(
                        guiGraphics,
                        slotX,
                        slotY,
                        20,
                        20
                );

                if (hovered) {
                    guiGraphics.fill(
                            slotX + 2,
                            slotY + 2,
                            slotX + 18,
                            slotY + 18,
                            SLOT_HOVER
                    );

                    hoveredIndex = index;
                }

                guiGraphics.renderItem(
                        new ItemStack(
                                visibleEntries.get(index).getIconItem()
                        ),
                        slotX + 2,
                        slotY + 2
                );
            }
        }

        if (hoveredIndex >= 0) {
            guiGraphics.renderTooltip(
                    font,
                    Component.literal(
                            visibleEntries.get(hoveredIndex).getDisplayName()
                    ),
                    mouseX,
                    mouseY
            );
        }
    }

    private void drawItemSearch(
            GuiGraphics guiGraphics,
            int mouseX,
            int mouseY
    ) {

        int x =
                getItemSearchX();

        int y =
                getItemSearchY();

        int width =
                getItemSearchWidth();

        boolean hovered =
                mouseX >= x
                        && mouseX <= x + width
                        && mouseY >= y
                        && mouseY <= y + 18;

        drawInsetBox(
                guiGraphics,
                x,
                y,
                width,
                18
        );

        if (hovered || itemSearchFocused) {
            guiGraphics.fill(
                    x + 1,
                    y + 1,
                    x + width - 1,
                    y + 17,
                    itemSearchFocused ? SEARCH_FOCUS : SEARCH_HOVER
            );
        }

        String text =
                itemSearchText.isEmpty()
                        ? "Pesquisar item..."
                        : itemSearchText;

        int color =
                itemSearchText.isEmpty()
                        ? PLACEHOLDER
                        : WHITE;

        guiGraphics.drawString(
                font,
                text,
                x + 6,
                y + 5,
                color
        );

        if (itemSearchFocused
                && (System.currentTimeMillis() / 500L) % 2L == 0L) {
            int cursorX =
                    x + 6 + font.width(itemSearchText);

            guiGraphics.fill(
                    cursorX,
                    y + 4,
                    cursorX + 1,
                    y + 14,
                    WHITE
            );
        }

        if (!itemSearchText.isEmpty()) {
            guiGraphics.drawString(
                    font,
                    "x",
                    x + width - 12,
                    y + 5,
                    CLEAR_TEXT
            );
        }
    }

    private void drawItemCategoryTabs(
            GuiGraphics guiGraphics,
            int mouseX,
            int mouseY
    ) {

        int x =
                getPanelX() + 16;

        int y =
                getListTop() + 4;

        int buttonWidth =
                Math.max(44, (getPanelWidth() - 32) / ITEM_CATEGORIES.length);

        for (int i = 0; i < ITEM_CATEGORIES.length; i++) {
            drawMinecraftButton(
                    guiGraphics,
                    x + i * buttonWidth,
                    y,
                    buttonWidth - 2,
                    16,
                    ITEM_CATEGORIES[i],
                    mouseX,
                    mouseY,
                    i == itemCategoryIndex
            );
        }
    }

    private List<PremiumEntry> filteredItemEntries() {

        List<PremiumEntry> entries =
                new ArrayList<>();

        for (PremiumEntry entry : premiumEntries) {
            if (isItemInCategory(entry.getIconItem())
                    && matchesItemSearch(entry)) {
                entries.add(entry);
            }
        }

        return entries;
    }

    private boolean matchesItemSearch(PremiumEntry entry) {
        String query =
                itemSearchText.trim().toLowerCase(Locale.ROOT);

        if (query.isEmpty()) {
            return true;
        }

        Item item =
                entry.getIconItem();

        String itemId =
                BuiltInRegistries.ITEM.getKey(item).toString();

        return entry.getName().toLowerCase(Locale.ROOT).contains(query)
                || entry.getEnglishName().toLowerCase(Locale.ROOT).contains(query)
                || entry.getCategory().toLowerCase(Locale.ROOT).contains(query)
                || itemId.toLowerCase(Locale.ROOT).contains(query);
    }

    private boolean isItemInCategory(
            Item item
    ) {

        if (itemCategoryIndex == 0) {
            return true;
        }

        String itemId =
                BuiltInRegistries.ITEM.getKey(item).toString();

        if (itemCategoryIndex == 1) {
            return item instanceof BlockItem;
        }

        if (itemCategoryIndex == 2) {
            return itemId.contains("_pickaxe")
                    || itemId.contains("_axe")
                    || itemId.contains("_shovel")
                    || itemId.contains("_hoe")
                    || itemId.contains("shears")
                    || itemId.contains("fishing_rod")
                    || itemId.contains("flint_and_steel");
        }

        if (itemCategoryIndex == 3) {
            return itemId.contains("_sword")
                    || itemId.contains("_helmet")
                    || itemId.contains("_chestplate")
                    || itemId.contains("_leggings")
                    || itemId.contains("_boots")
                    || itemId.contains("bow")
                    || itemId.contains("trident")
                    || itemId.contains("shield")
                    || itemId.contains("arrow");
        }

        if (itemCategoryIndex == 4) {
            return item.isEdible();
        }

        if (itemCategoryIndex == 5) {
            return itemId.contains("redstone")
                    || itemId.contains("piston")
                    || itemId.contains("observer")
                    || itemId.contains("dispenser")
                    || itemId.contains("dropper")
                    || itemId.contains("repeater")
                    || itemId.contains("comparator")
                    || itemId.contains("hopper")
                    || itemId.contains("rail")
                    || itemId.contains("lever")
                    || itemId.contains("button")
                    || itemId.contains("pressure_plate");
        }

        return itemId.endsWith("spawn_egg");
    }

    private void drawCommandsGate(
            GuiGraphics guiGraphics,
            int mouseX,
            int mouseY
    ) {

        int x =
                getPanelX() + 28;

        int y =
                getListTop() + 18;

        int boxWidth =
                getPanelWidth() - 56;

        drawInsetBox(
                guiGraphics,
                x,
                y,
                boxWidth,
                74
        );

        guiGraphics.drawCenteredString(
                font,
                commandMenusEnabled
                        ? "Menus por comando ativados"
                        : "Menus por comando ocultos",
                x + boxWidth / 2,
                y + 16,
                WHITE
        );

        drawMinecraftButton(
                guiGraphics,
                getCommandsButtonX(),
                getCommandsButtonY(),
                156,
                20,
                commandMenusEnabled ? "Desativar comandos" : "Ativar comandos",
                mouseX,
                mouseY,
                commandMenusEnabled
        );
    }

    private void drawWandTab(
            GuiGraphics guiGraphics,
            int mouseX,
            int mouseY
    ) {

        int x =
                getPanelX();

        int y =
                getPanelY();

        int boxX =
                x + 16;

        int boxY =
                y + 58;

        int iconSize =
                42;

        drawInsetBox(
                guiGraphics,
                boxX,
                boxY,
                iconSize,
                iconSize
        );

        guiGraphics.pose().pushPose();
        guiGraphics.pose().scale(2.0F, 2.0F, 1F);
        guiGraphics.renderItem(
                new ItemStack(Items.NETHER_STAR),
                (boxX + 5) / 2,
                (boxY + 5) / 2
        );
        guiGraphics.pose().popPose();

        int textX =
                boxX + 56;

        guiGraphics.drawString(
                font,
                "Varinha magica do MagicWorld.",
                textX,
                boxY + 2,
                WHITE
        );

        guiGraphics.drawString(
                font,
                "Clique esquerdo transforma mobs/blocos.",
                textX,
                boxY + 16,
                WHITE
        );

        guiGraphics.drawString(
                font,
                "Shift destroi blocos e alvos.",
                textX,
                boxY + 30,
                WHITE
        );

        int bx =
                getWandButtonX();

        int by =
                getWandButtonY();

        drawMinecraftButton(
                guiGraphics,
                bx,
                by,
                104,
                20,
                "Spawnar varinha",
                mouseX,
                mouseY,
                false
        );

    }

    private void drawCloseButton(
            GuiGraphics guiGraphics,
            int mouseX,
            int mouseY
    ) {

        drawMinecraftButton(
                guiGraphics,
                getCloseButtonX(),
                getCloseButtonY(),
                46,
                16,
                "Fechar",
                mouseX,
                mouseY,
                false
        );
    }

    private void drawTabs(
            GuiGraphics guiGraphics,
            int mouseX,
            int mouseY
    ) {

        MenuTab[] tabs =
                visibleTabs();

        for (int i = 0; i < tabs.length; i++) {
            int tabX =
                    getTabX(i);

            int tabY =
                    getTabY(i);

            int tabWidth =
                    getTabWidth();

            boolean selected =
                    tabs[i] == activeTab;

            boolean hovered =
                    mouseX >= tabX
                            && mouseX <= tabX + tabWidth
                            && mouseY >= tabY
                            && mouseY <= tabY + TAB_HEIGHT;

            drawMinecraftButton(
                    guiGraphics,
                    tabX,
                    tabY,
                    tabWidth,
                    TAB_HEIGHT,
                    tabs[i].getTitle(),
                    mouseX,
                    mouseY,
                    selected
            );
        }
    }

    @Override
    public boolean mouseClicked(
            double mouseX,
            double mouseY,
            int button
    ) {

        MenuTab clickedTab =
                getClickedTab(mouseX, mouseY);

        if (clickedTab != null) {
            minecraft.setScreen(
                    new PremiumMenuScreen(clickedTab)
            );

            return true;
        }

        if (mouseX >= getCloseButtonX()
                && mouseX <= getCloseButtonX() + 46
                && mouseY >= getCloseButtonY()
                && mouseY <= getCloseButtonY() + 16) {
            minecraft.setScreen(null);
            return true;
        }

        if (activeTab == MenuTab.WAND
                && mouseX >= getWandButtonX()
                && mouseX <= getWandButtonX() + 104
                && mouseY >= getWandButtonY()
                && mouseY <= getWandButtonY() + 20) {

            spawnWand();
            return true;
        }

        if (activeTab == MenuTab.SPAWN_ITEMS) {
            if (isInsideItemSearch(mouseX, mouseY)) {
                itemSearchFocused =
                        true;

                if (!itemSearchText.isEmpty()
                        && mouseX >= getItemSearchX() + getItemSearchWidth() - 18) {
                    itemSearchText =
                            "";

                    itemGridScroll =
                            0;
                }

                return true;
            }

            itemSearchFocused =
                    false;

            int clickedCategory =
                    getClickedItemCategory(mouseX, mouseY);

            if (clickedCategory >= 0) {
                itemCategoryIndex =
                        clickedCategory;

                itemGridScroll =
                        0;

                return true;
            }

            int clickedIndex =
                    getClickedItemGridIndex(mouseX, mouseY);

            List<PremiumEntry> visibleEntries =
                    filteredItemEntries();

            if (clickedIndex >= 0
                    && clickedIndex < visibleEntries.size()) {
                runMenuCommand(
                        visibleEntries.get(clickedIndex).getNormalCommand()
                );

                return true;
            }
        }

        if (activeTab == MenuTab.COMMANDS_GATE
                && mouseX >= getCommandsButtonX()
                && mouseX <= getCommandsButtonX() + 156
                && mouseY >= getCommandsButtonY()
                && mouseY <= getCommandsButtonY() + 20) {

            commandMenusEnabled =
                    !commandMenusEnabled;

            minecraft.setScreen(
                    new PremiumMenuScreen(MenuTab.COMMANDS_GATE)
            );

            return true;
        }

        return super.mouseClicked(
                mouseX,
                mouseY,
                button
        );
    }

    private void spawnWand() {

        if (minecraft == null
                || minecraft.player == null
                || minecraft.getConnection() == null) {
            return;
        }

        minecraft.getConnection().sendCommand(
                "give @s magicworld:varinha_magica 1"
        );
    }

    private MenuTab getClickedTab(
            double mouseX,
            double mouseY
    ) {

        MenuTab[] tabs =
                visibleTabs();

        for (int i = 0; i < tabs.length; i++) {
            int tabX =
                    getTabX(i);

            int tabY =
                    getTabY(i);

            if (mouseX >= tabX
                    && mouseX <= tabX + getTabWidth()
                    && mouseY >= tabY
                    && mouseY <= tabY + TAB_HEIGHT) {

                return tabs[i];
            }
        }

        return null;
    }

    private int getPanelWidth() {
        return Math.min(520, width - 24);
    }

    private int getPanelHeight() {
        return Math.min(260, height - 24);
    }

    private int getPanelX() {
        return (width - getPanelWidth()) / 2;
    }

    private int getPanelY() {
        return (height - getPanelHeight()) / 2;
    }

    private int getListTop() {
        return getPanelY() + 56 + (getTabRows() - 1) * (TAB_HEIGHT + TAB_ROW_GAP);
    }

    private int getListBottom() {
        return getPanelY() + getPanelHeight() - 14;
    }

    private int getTabWidth() {
        return (getPanelWidth() - 20) / getTabsPerRow();
    }

    private int getTabX(
            int index
    ) {
        return getPanelX() + 10 + (index % getTabsPerRow()) * getTabWidth();
    }

    private int getTabY(int index) {
        return getPanelY() + 34 + (index / getTabsPerRow()) * (TAB_HEIGHT + TAB_ROW_GAP);
    }

    private int getTabsPerRow() {
        int tabCount =
                visibleTabs().length;

        int availableWidth =
                Math.max(TAB_MIN_WIDTH, getPanelWidth() - 20);

        int maxTabs =
                Math.max(1, availableWidth / TAB_MIN_WIDTH);

        return Math.max(1, Math.min(tabCount, maxTabs));
    }

    private int getTabRows() {
        int tabCount =
                visibleTabs().length;

        int tabsPerRow =
                getTabsPerRow();

        return Math.max(1, (tabCount + tabsPerRow - 1) / tabsPerRow);
    }

    private MenuTab[] visibleTabs() {

        if (commandMenusEnabled) {
            return new MenuTab[] {
                    MenuTab.WAND,
                    MenuTab.BLOCKS,
                    MenuTab.ENEMIES,
                    MenuTab.ANIMALS,
                    MenuTab.CREATED,
                    MenuTab.SPAWN_ITEMS,
                    MenuTab.COMMANDS_GATE,
                    MenuTab.SPAWN_VILLAGES,
                    MenuTab.SPAWN_CONSTRUCTIONS,
                    MenuTab.CONTROL_CENTER
            };
        }

        return new MenuTab[] {
                MenuTab.WAND,
                MenuTab.BLOCKS,
                MenuTab.ENEMIES,
                MenuTab.ANIMALS,
                MenuTab.CREATED,
                MenuTab.SPAWN_ITEMS,
                MenuTab.COMMANDS_GATE
        };
    }

    private int getClickedItemGridIndex(
            double mouseX,
            double mouseY
    ) {

        int left =
                getPanelX() + 16;

        int top =
                getItemGridTop();

        int columns =
                Math.max(1, (getPanelWidth() - 42) / 24);

        int slotSize =
                22;

        if (mouseX < left
                || mouseY < top) {
            return -1;
        }

        int column =
                (int) ((mouseX - left) / slotSize);

        int row =
                (int) ((mouseY - top) / slotSize);

        int slotX =
                left + column * slotSize;

        int slotY =
                top + row * slotSize;

        if (column < 0
                || column >= columns
                || row < 0
                || mouseX > slotX + 20
                || mouseY > slotY + 20
                || slotY + 20 > getListBottom()) {
            return -1;
        }

        return itemGridScroll * columns
                + row * columns
                + column;
    }

    private int getClickedItemCategory(
            double mouseX,
            double mouseY
    ) {

        int x =
                getPanelX() + 16;

        int y =
                getListTop() + 4;

        int buttonWidth =
                Math.max(44, (getPanelWidth() - 32) / ITEM_CATEGORIES.length);

        if (mouseY < y
                || mouseY > y + 16) {
            return -1;
        }

        for (int i = 0; i < ITEM_CATEGORIES.length; i++) {
            int buttonX =
                    x + i * buttonWidth;

            if (mouseX >= buttonX
                    && mouseX <= buttonX + buttonWidth - 2) {
                return i;
            }
        }

        return -1;
    }

    private int getItemSearchX() {
        return getPanelX() + 16;
    }

    private int getItemSearchY() {
        return getListTop() + 24;
    }

    private int getItemSearchWidth() {
        return getPanelWidth() - 32;
    }

    private int getItemGridTop() {
        return getListTop() + 48;
    }

    private boolean isInsideItemSearch(
            double mouseX,
            double mouseY
    ) {

        return mouseX >= getItemSearchX()
                && mouseX <= getItemSearchX() + getItemSearchWidth()
                && mouseY >= getItemSearchY()
                && mouseY <= getItemSearchY() + 18;
    }

    private int getCommandsButtonX() {
        return getPanelX() + getPanelWidth() / 2 - 78;
    }

    private int getCommandsButtonY() {
        return getListTop() + 60;
    }

    @Override
    public boolean mouseScrolled(
            double mouseX,
            double mouseY,
            double delta
    ) {

        if (activeTab == MenuTab.SPAWN_ITEMS) {
            int columns =
                    Math.max(1, (getPanelWidth() - 42) / 24);

            int visibleRows =
                    Math.max(1, (getListBottom() - getItemGridTop() - 4) / 22);

            int visibleEntries =
                    filteredItemEntries().size();

            int maxScroll =
                    Math.max(
                            0,
                            (visibleEntries + columns - 1) / columns - visibleRows
                    );

            itemGridScroll =
                    Math.max(
                            0,
                            Math.min(
                                    maxScroll,
                                    itemGridScroll - (int) Math.signum(delta)
                            )
                    );

            return true;
        }

        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    private int getWandButtonX() {
        return width / 2 - 52;
    }

    private int getWandButtonY() {
        return getPanelY() + getPanelHeight() - 42;
    }

    private int getCloseButtonX() {
        return getPanelX() + getPanelWidth() - 58;
    }

    private int getCloseButtonY() {
        return getPanelY() + 8;
    }

    @Override
    public boolean keyPressed(
            int keyCode,
            int scanCode,
            int modifiers
    ) {

        if (KeyBindings.OPEN_MENU_KEY != null
                && KeyBindings.OPEN_MENU_KEY.matches(keyCode, scanCode)) {
            minecraft.setScreen(null);
            return true;
        }

        if (activeTab == MenuTab.SPAWN_ITEMS
                && itemSearchFocused) {
            if (keyCode == 259) {
                if (!itemSearchText.isEmpty()) {
                    itemSearchText =
                            itemSearchText.substring(
                                    0,
                                    itemSearchText.length() - 1
                            );

                    itemGridScroll =
                            0;
                }

                return true;
            }

            if (keyCode == 257
                    || keyCode == 335) {
                itemSearchFocused =
                        false;

                return true;
            }
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(
            char codePoint,
            int modifiers
    ) {

        if (activeTab == MenuTab.SPAWN_ITEMS
                && itemSearchFocused
                && isAllowedSearchCharacter(codePoint)
                && itemSearchText.length() < 48) {
            itemSearchText += codePoint;
            itemGridScroll =
                    0;

            return true;
        }

        return super.charTyped(codePoint, modifiers);
    }

    private boolean isAllowedSearchCharacter(char codePoint) {
        return Character.isLetterOrDigit(codePoint)
                || codePoint == ' '
                || codePoint == '_'
                || codePoint == ':'
                || codePoint == '-';
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    class PremiumList extends ObjectSelectionList<PremiumList.Entry> {

        public PremiumList(
                Minecraft minecraft
        ) {

            super(
                    minecraft,
                    PremiumMenuScreen.this.width,
                    PremiumMenuScreen.this.getListBottom()
                            - PremiumMenuScreen.this.getListTop(),
                    PremiumMenuScreen.this.getListTop(),
                    PremiumMenuScreen.this.getListBottom(),
                    38
            );

            setRenderBackground(false);
            setRenderTopAndBottom(false);
            setRenderSelection(false);

            for (PremiumEntry entry : premiumEntries) {
                addEntry(new Entry(entry));
            }
        }

        @Override
        public int getRowLeft() {
            return getPanelX() + 12;
        }

        @Override
        public int getRowWidth() {
            return getPanelWidth() - 36;
        }

        @Override
        protected int getScrollbarPosition() {
            return getPanelX() + getPanelWidth() - 17;
        }

        @Override
        protected void renderDecorations(
                GuiGraphics guiGraphics,
                int mouseX,
                int mouseY
        ) {

            int scrollX =
                    premiumList.getScrollbarPosition() + 2;

            int top =
                    premiumList.y0 + 4;

            int bottom =
                    premiumList.y1 - 4;

            int trackHeight =
                    bottom - top;

            double maxScroll =
                    premiumList.getMaxScroll();

            int thumbHeight =
                    24;

            int thumbY =
                    top;

            if (maxScroll > 0) {
                thumbY += (int) (
                        (premiumList.getScrollAmount() / maxScroll)
                                * (trackHeight - thumbHeight)
                );
            }

            drawInsetBox(
                    guiGraphics,
                    scrollX - 4,
                    top - 4,
                    10,
                    trackHeight + 8
            );

            drawRaisedBox(
                    guiGraphics,
                    scrollX - 3,
                    thumbY - 4,
                    8,
                    thumbHeight + 8,
                    PREMIUM_GLASS
            );
        }

        class Entry extends ObjectSelectionList.Entry<Entry> {

            private final PremiumEntry entry;
            private LivingEntity previewEntity;
            private String previewEntityId =
                    "";

            public Entry(PremiumEntry entry) {
                this.entry = entry;
            }

            @Override
            public void render(
                    GuiGraphics guiGraphics,
                    int index,
                    int top,
                    int left,
                    int width,
                    int height,
                    int mouseX,
                    int mouseY,
                    boolean hovered,
                    float partialTick
            ) {

                guiGraphics.fill(
                        left + 3,
                        top + 3,
                        left + width + 3,
                        top + height + 1,
                        SHADOW
                );

                guiGraphics.fill(
                        left,
                        top,
                        left + width,
                        top + height - 2,
                        hovered ? LIST_ROW_HOVER : LIST_ROW
                );

                guiGraphics.fill(left, top, left + width, top + 1, STONE_LIGHT);
                guiGraphics.fill(left, top, left + 1, top + height - 2, STONE_LIGHT);
                guiGraphics.fill(left, top + height - 3, left + width, top + height - 2, STONE_DARK);
                guiGraphics.fill(left + width - 1, top, left + width, top + height - 2, STONE_DARK);

                int iconSize =
                        32;

                int iconX =
                        left + 4;

                int iconY =
                        top + (height - iconSize) / 2 - 1;

                drawInsetBox(
                        guiGraphics,
                        iconX,
                        iconY,
                        iconSize,
                        iconSize
                );

                drawEntryIcon(
                        guiGraphics,
                        iconX,
                        iconY,
                        iconSize,
                        mouseX,
                        mouseY
                );

                guiGraphics.drawString(
                        font,
                        entry.getDisplayName(),
                        left + 45,
                        top + 8,
                        TEXT_SHADOW
                );

                guiGraphics.drawString(
                        font,
                        entry.getDisplayName(),
                        left + 44,
                        top + 7,
                        WHITE
                );

                guiGraphics.drawString(
                        font,
                        entry.getCategory(),
                        left + 45,
                        top + 22,
                        TEXT_SHADOW
                );

                guiGraphics.drawString(
                        font,
                        entry.getCategory(),
                        left + 44,
                        top + 21,
                        WHITE
                );
            }

            private void drawEntryIcon(
                    GuiGraphics guiGraphics,
                    int x,
                    int y,
                    int size,
                    int mouseX,
                    int mouseY
            ) {

                String entityId =
                        entityIdFromCommand(
                                entry.getNormalCommand()
                        );

                if ((entry.getTab() == MenuTab.ANIMALS
                        || entry.getTab() == MenuTab.ENEMIES)
                        && !entityId.isEmpty()) {

                    LivingEntity livingEntity =
                            getPreviewEntity(entityId);

                    if (livingEntity != null) {
                        guiGraphics.enableScissor(
                                x + 2,
                                y + 2,
                                x + size - 2,
                                y + size - 2
                        );

                        InventoryScreen.renderEntityInInventoryFollowsMouse(
                                guiGraphics,
                                x + size / 2 + entityXOffset(entityId),
                                y + entityBottom(entityId, size),
                                entityScale(entityId, 16),
                                entityMouseX(entityId),
                                entityMouseY(entityId),
                                livingEntity
                        );

                        guiGraphics.disableScissor();
                        return;
                    }
                }

                if (entry.usesMobTexture()) {
                    drawMobImage(
                            guiGraphics,
                            x + 4,
                            y + 4
                    );
                    return;
                }

                String previewImage =
                        previewImageName();

                if (!previewImage.isEmpty()) {
                    drawPreviewImage(
                            guiGraphics,
                            x,
                            y,
                            size,
                            previewImage
                    );
                    return;
                }

                guiGraphics.enableScissor(
                        x + 2,
                        y + 2,
                        x + size - 2,
                        y + size - 2
                );

                renderItemPreview(
                        guiGraphics,
                        new ItemStack(entry.getIconItem()),
                        x + size / 2,
                        y + size / 2,
                        1.2F
                );

                guiGraphics.disableScissor();
            }

            private String previewImageName() {

                if (entry.getTab() == MenuTab.SPAWN_VILLAGES
                        || entry.getTab() == MenuTab.SPAWN_CONSTRUCTIONS) {
                    return structurePreviewName(
                            entry.getNormalCommand()
                    );
                }

                if (entry.getTab() == MenuTab.CONTROL_CENTER
                        && entry.getNormalCommand().startsWith("OPEN_MENU:")) {
                    return entry.getNormalCommand()
                            .substring("OPEN_MENU:".length())
                            .toLowerCase();
                }

                return "";
            }

            private String structurePreviewName(
                    String command
            ) {

                String prefix =
                        "place structure minecraft:";

                if (command == null
                        || !command.startsWith(prefix)) {
                    return "";
                }

                int start =
                        prefix.length();

                int end =
                        command.indexOf(" ", start);

                if (end <= start) {
                    return command.substring(start);
                }

                return command.substring(start, end);
            }

            private void drawPreviewImage(
                    GuiGraphics guiGraphics,
                    int x,
                    int y,
                    int size,
                    String fileName
            ) {

                ResourceLocation texture =
                        ResourceLocation.tryParse(
                                "magicworld:textures/gui/previews/"
                                        + fileName
                                        + ".png"
                        );

                RenderSystem.enableBlend();

                guiGraphics.blit(
                        texture,
                        x + 2,
                        y + 2,
                        size - 4,
                        size - 4,
                        0.0F,
                        0.0F,
                        PREVIEW_TEXTURE_SIZE,
                        PREVIEW_TEXTURE_SIZE,
                        PREVIEW_TEXTURE_SIZE,
                        PREVIEW_TEXTURE_SIZE
                );
            }

            private LivingEntity getPreviewEntity(
                    String entityId
            ) {

                if (!entityId.equals(previewEntityId)) {
                    previewEntityId = entityId;
                    previewEntity =
                            createPreviewEntity(entityId);
                }

                return previewEntity;
            }

            private void drawMobImage(
                    GuiGraphics guiGraphics,
                    int x,
                    int y
            ) {

                String fileName =
                        entry.getEnglishName()
                                .toLowerCase()
                                .replace(" ", "_");

                ResourceLocation texture =
                        ResourceLocation.tryParse(
                                "magicworld:textures/gui/mobs/"
                                        + fileName
                                        + ".png"
                        );

                RenderSystem.enableBlend();

                guiGraphics.blit(
                        texture,
                        x,
                        y,
                        0,
                        0,
                        24,
                        24,
                        28,
                        28
                );
            }

            @Override
            public boolean mouseClicked(
                    double mouseX,
                    double mouseY,
                    int button
            ) {

                if (entry.getNormalCommand().startsWith("OPEN_MENU:")) {
                    minecraft.setScreen(
                            new PremiumMenuScreen(
                                    MenuTab.valueOf(
                                            entry.getNormalCommand()
                                                    .substring("OPEN_MENU:".length())
                                    )
                            )
                    );

                    return true;
                }

                if (entry.getNormalCommand().equals("BACK_MENU")) {
                    minecraft.setScreen(
                            new PremiumMenuScreen(MenuTab.CONTROL_CENTER)
                    );

                    return true;
                }

                if (isDirectCommandTab(entry.getTab())) {
                    runMenuCommand(entry.getNormalCommand());
                    return true;
                }

                minecraft.setScreen(
                        new PremiumDetailsScreen(entry)
                );

                return true;
            }

            @Override
            public Component getNarration() {
                return Component.literal(
                        entry.getDisplayName()
                );
            }
        }
    }

    private boolean isDirectCommandTab(
            MenuTab tab
    ) {

        return tab == MenuTab.SPAWN_ITEMS
                || tab == MenuTab.SPAWN_VILLAGES
                || tab == MenuTab.SPAWN_CONSTRUCTIONS
                || tab == MenuTab.GRAPHICS_PROFILES
                || tab == MenuTab.MOB_SPAWNER
                || tab == MenuTab.WEATHER_CONTROL
                || tab == MenuTab.BIOME_TELEPORT
                || tab == MenuTab.DIMENSION_MENU
                || tab == MenuTab.PREMIUM_POWERS
                || tab == MenuTab.STRUCTURE_RAIN
                || tab == MenuTab.WORLD_EVENTS
                || tab == MenuTab.TROLL_MENU
                || tab == MenuTab.PREMIUM_COMPANION
                || tab == MenuTab.BOSS_CONTROL
                || tab == MenuTab.LUCKY_BLOCK
                || tab == MenuTab.TIME_CONTROL
                || tab == MenuTab.PARTICLE_EFFECTS
                || tab == MenuTab.PORTAL_MENU
                || tab == MenuTab.DUNGEON_SPAWNER
                || tab == MenuTab.PREMIUM_TOOLS
                || tab == MenuTab.PREMIUM_ARMOR
                || tab == MenuTab.NPC_MENU
                || tab == MenuTab.WAVE_SURVIVAL
                || tab == MenuTab.PREMIUM_MOUNTS;
    }

    private boolean isSystemSubMenu(
            MenuTab tab
    ) {

        return tab == MenuTab.MOB_SPAWNER
                || tab == MenuTab.GRAPHICS_PROFILES
                || tab == MenuTab.WEATHER_CONTROL
                || tab == MenuTab.BIOME_TELEPORT
                || tab == MenuTab.DIMENSION_MENU
                || tab == MenuTab.PREMIUM_POWERS
                || tab == MenuTab.STRUCTURE_RAIN
                || tab == MenuTab.WORLD_EVENTS
                || tab == MenuTab.TROLL_MENU
                || tab == MenuTab.TRANSFORMATION_ENCYCLOPEDIA
                || tab == MenuTab.PREMIUM_COMPANION
                || tab == MenuTab.BOSS_CONTROL
                || tab == MenuTab.LUCKY_BLOCK
                || tab == MenuTab.TIME_CONTROL
                || tab == MenuTab.PARTICLE_EFFECTS
                || tab == MenuTab.PORTAL_MENU
                || tab == MenuTab.DUNGEON_SPAWNER
                || tab == MenuTab.PREMIUM_TOOLS
                || tab == MenuTab.PREMIUM_ARMOR
                || tab == MenuTab.NPC_MENU
                || tab == MenuTab.WAVE_SURVIVAL
                || tab == MenuTab.PREMIUM_MOUNTS;
    }

    private void runMenuCommand(
            String command
    ) {

        if (command == null
                || command.trim().isEmpty()
                || minecraft == null) {
            return;
        }

        if (command.startsWith("GRAPHICS_PROFILE:")) {
            applyGraphicsProfile(command.substring("GRAPHICS_PROFILE:".length()));
            return;
        }

        if (command.equals("OPEN_RESOURCEPACKS_FOLDER")) {
            openRunFolder("resourcepacks");
            return;
        }

        if (command.equals("OPEN_SHADERPACKS_FOLDER")) {
            openRunFolder("shaderpacks");
            return;
        }

        if (command.equals("CHECK_SHADER_LOADER")) {
            checkShaderLoader();
            return;
        }

        if (minecraft.player == null
                || minecraft.getConnection() == null) {
            return;
        }

        if (command.startsWith("BIOME_TELEPORT:")) {
            teleportToBiome(
                    command.substring("BIOME_TELEPORT:".length())
            );
            return;
        }

        if (command.equals("RETURN_TELEPORT")) {
            returnToSavedTeleport();
            return;
        }

        minecraft.getConnection().sendCommand(command);
    }

    private void applyGraphicsProfile(String profileName) {
        try {
            MagicWorldGraphicsProfile.valueOf(profileName)
                    .apply(minecraft);
        } catch (IllegalArgumentException exception) {
            sendClientMessage("Perfil grafico invalido: " + profileName);
        }
    }

    private void openRunFolder(String folderName) {
        Path folder =
                minecraft.gameDirectory.toPath()
                        .resolve(folderName);

        try {
            Files.createDirectories(folder);
            Util.getPlatform()
                    .openFile(folder.toFile());
            sendClientMessage("Pasta aberta: " + folder);
        } catch (Exception exception) {
            sendClientMessage("Nao foi possivel abrir a pasta: " + folder);
        }
    }

    private void checkShaderLoader() {
        boolean hasOculus =
                ModList.get()
                        .isLoaded("oculus");
        boolean hasIris =
                ModList.get()
                        .isLoaded("iris");

        if (hasOculus || hasIris) {
            sendClientMessage("Loader de shaders detectado. Use o menu dele para ativar o shaderpack.");
            return;
        }

        sendClientMessage("Nenhum loader de shaders detectado. Forge 1.20.1 precisa de Oculus/Iris compat para usar shaderpacks.");
    }

    private void teleportToBiome(String biomeId) {
        MinecraftServer server =
                minecraft.getSingleplayerServer();

        if (server == null
                || minecraft.player == null) {
            sendClientMessage("Teleporte direto so funciona no mundo singleplayer/integrado.");
            return;
        }

        ServerPlayer serverPlayer =
                server.getPlayerList().getPlayer(
                        minecraft.player.getUUID()
                );

        if (serverPlayer == null) {
            sendClientMessage("Jogador nao encontrado no servidor integrado.");
            return;
        }

        ResourceLocation biomeLocation =
                ResourceLocation.tryParse(biomeId);

        if (biomeLocation == null) {
            sendClientMessage("Bioma invalido: " + biomeId);
            return;
        }

        ResourceKey<Biome> biomeKey =
                ResourceKey.create(
                        Registries.BIOME,
                        biomeLocation
                );

        ServerLevel level =
                levelForBiome(server, biomeId);

        if (level == null) {
            sendClientMessage("Dimensao do bioma nao esta carregada.");
            return;
        }

        savedTeleport =
                SavedTeleport.from(serverPlayer);

        Pair<BlockPos, net.minecraft.core.Holder<Biome>> result =
                level.findClosestBiome3d(
                        holder -> holder.is(biomeKey),
                        serverPlayer.blockPosition(),
                        6400,
                        32,
                        64
                );

        if (result == null) {
            sendClientMessage("Nao encontrei esse bioma num raio de 6400 blocos.");
            return;
        }

        BlockPos biomePos =
                result.getFirst();

        BlockPos safePos =
                safeTeleportPos(level, biomePos);

        serverPlayer.teleportTo(
                level,
                safePos.getX() + 0.5D,
                safePos.getY(),
                safePos.getZ() + 0.5D,
                serverPlayer.getYRot(),
                serverPlayer.getXRot()
        );

        minecraft.setScreen(null);
        sendClientMessage("Teleportado para " + biomeId + ". Use Voltar para retornar.");
    }

    private BlockPos safeTeleportPos(
            ServerLevel level,
            BlockPos biomePos
    ) {

        int x =
                biomePos.getX();

        int z =
                biomePos.getZ();

        level.getChunk(
                x >> 4,
                z >> 4
        );

        BlockPos surfacePos =
                level.getHeightmapPos(
                        Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                        new BlockPos(x, biomePos.getY(), z)
                );

        int minY =
                level.getMinBuildHeight() + 1;

        int maxY =
                level.getMaxBuildHeight() - 2;

        int startY =
                Math.max(
                        minY,
                        Math.min(surfacePos.getY(), maxY)
                );

        BlockPos.MutableBlockPos feet =
                new BlockPos.MutableBlockPos();

        for (int y = startY; y <= maxY; y++) {
            feet.set(x, y, z);

            if (isSafeTeleportFeet(level, feet)) {
                return feet.immutable();
            }
        }

        for (int y = startY - 1; y >= minY; y--) {
            feet.set(x, y, z);

            if (isSafeTeleportFeet(level, feet)) {
                return feet.immutable();
            }
        }

        return new BlockPos(
                x,
                startY,
                z
        );
    }

    private boolean isSafeTeleportFeet(
            ServerLevel level,
            BlockPos feet
    ) {

        BlockPos head =
                feet.above();

        BlockPos below =
                feet.below();

        return isClearForPlayer(level, feet)
                && isClearForPlayer(level, head)
                && isStableTeleportSupport(level, below);
    }

    private boolean isClearForPlayer(
            ServerLevel level,
            BlockPos pos
    ) {

        return level.getBlockState(pos)
                .getCollisionShape(level, pos)
                .isEmpty()
                && level.getFluidState(pos).isEmpty();
    }

    private boolean isStableTeleportSupport(
            ServerLevel level,
            BlockPos pos
    ) {

        return !level.getBlockState(pos)
                .getCollisionShape(level, pos)
                .isEmpty()
                || level.getFluidState(pos).is(FluidTags.WATER);
    }

    private void returnToSavedTeleport() {
        MinecraftServer server =
                minecraft.getSingleplayerServer();

        if (server == null
                || minecraft.player == null) {
            sendClientMessage("Voltar so funciona no mundo singleplayer/integrado.");
            return;
        }

        if (savedTeleport == null) {
            sendClientMessage("Nenhuma posicao anterior salva.");
            return;
        }

        ServerPlayer serverPlayer =
                server.getPlayerList().getPlayer(
                        minecraft.player.getUUID()
                );

        if (serverPlayer == null) {
            sendClientMessage("Jogador nao encontrado no servidor integrado.");
            return;
        }

        ServerLevel level =
                server.getLevel(savedTeleport.dimension);

        if (level == null) {
            sendClientMessage("Dimensao anterior nao esta carregada.");
            return;
        }

        serverPlayer.teleportTo(
                level,
                savedTeleport.x,
                savedTeleport.y,
                savedTeleport.z,
                savedTeleport.yRot,
                savedTeleport.xRot
        );

        minecraft.setScreen(null);
        sendClientMessage("Voltou para a posicao anterior.");
    }

    private ServerLevel levelForBiome(
            MinecraftServer server,
            String biomeId
    ) {

        if (biomeId.contains("nether")
                || biomeId.contains("basalt")
                || biomeId.contains("crimson")
                || biomeId.contains("warped")
                || biomeId.contains("soul_sand")) {
            return server.getLevel(
                    net.minecraft.world.level.Level.NETHER
            );
        }

        if (biomeId.contains("end")
                || biomeId.contains("the_void")) {
            return server.getLevel(
                    net.minecraft.world.level.Level.END
            );
        }

        return server.getLevel(
                net.minecraft.world.level.Level.OVERWORLD
        );
    }

    private void sendClientMessage(String message) {
        if (minecraft != null && minecraft.gui != null) {
            minecraft.gui.setOverlayMessage(Component.literal(message), false);
        }
    }

    private record SavedTeleport(
            ResourceKey<net.minecraft.world.level.Level> dimension,
            double x,
            double y,
            double z,
            float yRot,
            float xRot
    ) {

        static SavedTeleport from(ServerPlayer player) {
            return new SavedTeleport(
                    player.serverLevel().dimension(),
                    player.getX(),
                    player.getY(),
                    player.getZ(),
                    player.getYRot(),
                    player.getXRot()
            );
        }
    }

    private LivingEntity createPreviewEntity(
            String entityId
    ) {

        if (minecraft == null
                || minecraft.level == null
                || entityId.isEmpty()) {
            return null;
        }

        EntityType<?> entityType =
                EntityType.byString(entityId)
                        .orElse(null);

        if (entityType == null) {
            return null;
        }

        Entity entity =
                entityType.create(minecraft.level);

        if (entity instanceof LivingEntity livingEntity) {
            return livingEntity;
        }

        return null;
    }

    private String entityIdFromCommand(
            String command
    ) {

        if (command == null
                || !command.startsWith("summon ")) {
            return "";
        }

        int start =
                "summon ".length();

        int end =
                command.indexOf(" ", start);

        if (end <= start) {
            return "";
        }

        return command.substring(
                start,
                end
        );
    }

    private int entityScale(
            String entityId,
            int baseScale
    ) {

        if (entityId.contains("ender_dragon")) return Math.max(4, baseScale / 4);
        if (entityId.contains("giant")) return Math.max(4, baseScale / 4);
        if (entityId.contains("ghast")) return Math.max(6, baseScale / 2);
        if (entityId.contains("wither")) return Math.max(6, baseScale / 2);
        if (entityId.contains("ravager")) return Math.max(7, baseScale / 2);
        if (entityId.contains("warden")) return Math.max(7, baseScale / 2);
        if (entityId.contains("elder_guardian")) return Math.max(7, baseScale / 2);
        if (entityId.contains("iron_golem")) return Math.max(8, baseScale - 7);
        if (entityId.contains("camel")) return Math.max(8, baseScale - 7);
        if (entityId.contains("horse")) return Math.max(8, baseScale - 6);
        if (entityId.contains("hoglin")) return Math.max(8, baseScale - 6);

        return baseScale;
    }

    private int entityBottom(
            String entityId,
            int size
    ) {

        if (entityId.contains("bee")) return size - 8;
        if (entityId.contains("bat")) return size - 8;
        if (entityId.contains("vex")) return size - 8;
        if (entityId.contains("allay")) return size - 8;
        if (entityId.contains("ghast")) return size - 7;
        if (entityId.contains("squid")) return size - 7;
        if (entityId.contains("ender_dragon")) return size - 7;
        if (entityId.contains("giant")) return size - 6;
        if (entityId.contains("ravager")) return size - 5;
        if (entityId.contains("warden")) return size - 5;

        return size - 4;
    }

    private int entityXOffset(
            String entityId
    ) {

        if (entityId.contains("ender_dragon")) return -1;
        if (entityId.contains("squid")) return -1;

        return 0;
    }

    private float entityMouseX(
            String entityId
    ) {

        float movement =
                (float) Math.sin(System.currentTimeMillis() / 1100.0D) * 18.0F;

        if (entityId.contains("ender_dragon")
                || entityId.contains("giant")
                || entityId.contains("ghast")
                || entityId.contains("wither")
                || entityId.contains("ravager")
                || entityId.contains("warden")
                || entityId.contains("elder_guardian")) {
            return movement * 0.45F;
        }

        return movement;
    }

    private float entityMouseY(
            String entityId
    ) {

        float movement =
                (float) Math.cos(System.currentTimeMillis() / 1600.0D) * 3.0F;

        if (entityId.contains("ender_dragon")
                || entityId.contains("giant")
                || entityId.contains("ghast")
                || entityId.contains("wither")
                || entityId.contains("ravager")
                || entityId.contains("warden")
                || entityId.contains("elder_guardian")) {
            return movement * 0.35F;
        }

        return movement;
    }

    private void renderItemPreview(
            GuiGraphics guiGraphics,
            ItemStack itemStack,
            int x,
            int y,
            float scale
    ) {

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(x, y, 120);
        guiGraphics.pose().scale(scale, scale, 1.0F);
        guiGraphics.renderItem(itemStack, -8, -8);
        guiGraphics.pose().popPose();
    }

    private void drawThinGreenBox(
            GuiGraphics guiGraphics,
            int x,
            int y,
            int width,
            int height
    ) {

        guiGraphics.fill(x, y, x + width, y + 1, STONE_LIGHT);
        guiGraphics.fill(x, y, x + 1, y + height, STONE_LIGHT);
        guiGraphics.fill(x + width - 1, y, x + width, y + height, STONE_DARK);
        guiGraphics.fill(x, y + height - 1, x + width, y + height, STONE_DARK);
    }

    private void drawMinecraftPanel(
            GuiGraphics guiGraphics,
            int x,
            int y,
            int width,
            int height
    ) {

        guiGraphics.fill(x + 6, y + 7, x + width + 6, y + height + 7, SHADOW);
        guiGraphics.fill(x + 2, y + 2, x + width - 2, y + height - 2, PANEL);

        guiGraphics.fill(x, y, x + width, y + 2, STONE_LIGHT);
        guiGraphics.fill(x, y, x + 2, y + height, STONE_LIGHT);
        guiGraphics.fill(x, y + height - 2, x + width, y + height, STONE_DARK);
        guiGraphics.fill(x + width - 2, y, x + width, y + height, STONE_DARK);

        drawBorder(guiGraphics, x + 4, y + 4, width - 8, height - 8);
    }

    private void drawMinecraftButton(
            GuiGraphics guiGraphics,
            int x,
            int y,
            int width,
            int height,
            String text,
            int mouseX,
            int mouseY,
            boolean selected
    ) {

        boolean hovered =
                mouseX >= x
                        && mouseX <= x + width
                        && mouseY >= y
                        && mouseY <= y + height;

        int fill =
                selected
                        ? PREMIUM_GLASS
                        : hovered
                                ? BUTTON_HOVER
                                : VANILLA_BUTTON;

        guiGraphics.fill(x + 2, y + 2, x + width + 2, y + height + 2, SHADOW);
        guiGraphics.fill(x, y, x + width, y + height, fill);
        guiGraphics.fill(x, y, x + width, y + 1, VANILLA_BUTTON_LIGHT);
        guiGraphics.fill(x, y, x + 1, y + height, VANILLA_BUTTON_LIGHT);
        guiGraphics.fill(x, y + height - 1, x + width, y + height, VANILLA_BUTTON_DARK);
        guiGraphics.fill(x + width - 1, y, x + width, y + height, VANILLA_BUTTON_DARK);

        if (selected || hovered) {
            drawBorder(guiGraphics, x + 2, y + 2, width - 4, height - 4);
        }

        guiGraphics.drawCenteredString(
                font,
                text,
                x + width / 2 + 1,
                y + height / 2 - 3 + 1,
                TEXT_SHADOW
        );

        guiGraphics.drawCenteredString(
                font,
                text,
                x + width / 2,
                y + height / 2 - 3,
                WHITE
        );
    }

    private void drawRaisedBox(
            GuiGraphics guiGraphics,
            int x,
            int y,
            int width,
            int height,
            int fill
    ) {

        guiGraphics.fill(x + 2, y + 2, x + width + 2, y + height + 2, SHADOW);
        guiGraphics.fill(x, y, x + width, y + height, fill);
        guiGraphics.fill(x, y, x + width, y + 1, STONE_LIGHT);
        guiGraphics.fill(x, y, x + 1, y + height, STONE_LIGHT);
        guiGraphics.fill(x, y + height - 1, x + width, y + height, STONE_DARK);
        guiGraphics.fill(x + width - 1, y, x + width, y + height, STONE_DARK);
    }

    private void drawInsetBox(
            GuiGraphics guiGraphics,
            int x,
            int y,
            int width,
            int height
    ) {

        guiGraphics.fill(x, y, x + width, y + height, INSET);
        guiGraphics.fill(x, y, x + width, y + 1, STONE_DARK);
        guiGraphics.fill(x, y, x + 1, y + height, STONE_DARK);
        guiGraphics.fill(x, y + height - 1, x + width, y + height, STONE_LIGHT);
        guiGraphics.fill(x + width - 1, y, x + width, y + height, STONE_LIGHT);
        drawBorder(guiGraphics, x + 2, y + 2, width - 4, height - 4);
    }

    private void drawBorder(
            GuiGraphics guiGraphics,
            int x,
            int y,
            int width,
            int height
    ) {

        guiGraphics.fill(x, y, x + width, y + 1, BORDER);
        guiGraphics.fill(x, y, x + 1, y + height, BORDER);
        guiGraphics.fill(x + width - 1, y, x + width, y + height, BORDER);
        guiGraphics.fill(x, y + height - 1, x + width, y + height, BORDER);
    }

    private void drawCornerTopLeft(GuiGraphics g, int x, int y) {
        g.fill(x, y, x + 6, y + 1, WHITE);
        g.fill(x, y, x + 1, y + 6, WHITE);
    }

    private void drawCornerTopRight(GuiGraphics g, int x, int y) {
        g.fill(x - 5, y, x + 1, y + 1, WHITE);
        g.fill(x, y, x + 1, y + 6, WHITE);
    }

    private void drawCornerBottomLeft(GuiGraphics g, int x, int y) {
        g.fill(x, y, x + 6, y + 1, WHITE);
        g.fill(x, y - 5, x + 1, y + 1, WHITE);
    }

    private void drawCornerBottomRight(GuiGraphics g, int x, int y) {
        g.fill(x - 5, y, x + 1, y + 1, WHITE);
        g.fill(x, y - 5, x + 1, y + 1, WHITE);
    }
}
