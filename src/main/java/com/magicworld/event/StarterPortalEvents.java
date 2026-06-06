package com.magicworld.event;

import com.magicworld.MagicWorld;
import com.magicworld.MagicWorldWorldOptions;
import com.magicworld.network.MagicWorldNetwork;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.RelativeMovement;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BeetrootBlock;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.EndPortalFrameBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.NetherPortalBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.WallSignBlock;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SignText;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class StarterPortalEvents {
    private static final String ESTATE_CREATED_KEY = "MagicWorldForgeStarterEstateCreated";
    private static final String ESTATE_BASE_X_KEY = "MagicWorldForgeStarterEstateBaseX";
    private static final String ESTATE_BASE_Y_KEY = "MagicWorldForgeStarterEstateBaseY";
    private static final String ESTATE_BASE_Z_KEY = "MagicWorldForgeStarterEstateBaseZ";
    private static final String ESTATE_REPAIR_VERSION_KEY = "MagicWorldForgeEstateRepairVersion";
    private static final String PREMIUM_UNLOCKED_KEY = "MagicWorldForgePremiumUnlocked";
    private static final String PORTAL_COOLDOWN_KEY = "MagicWorldForgePortalCooldown";
    private static final String RETURN_PORTAL_PREFIX = "MagicWorldForgeReturnPortal";
    private static final String FRIENDLY_WITCH_KEY = "MagicWorldFriendlyWitch";

    private static final ResourceLocation IMPORTED_HOUSE = new ResourceLocation(MagicWorld.MODID, "imported_house");
    private static final ResourceLocation IMPORTED_CASTLE = new ResourceLocation(MagicWorld.MODID, "imported_castle");
    private static final ResourceLocation STARTER_ROAD_END_HOUSE = new ResourceLocation(MagicWorld.MODID, "starter_house_1");

    private static final int START_DELAY_TICKS = 40;
    private static final int STEP_DELAY_TICKS = 80;
    private static final int FINAL_DELAY_TICKS = 80;
    private static final int BREATHING_MARGIN = 8;
    private static final int PORTAL_Z_OFFSET = 70;
    private static final int CASTLE_Z_OFFSET = 90;
    private static final int CASTLE_X_OFFSET = 40;
    private static final int HOUSE_ORIGIN_X = -83;
    private static final int HOUSE_ORIGIN_Y = -4;
    private static final int HOUSE_ORIGIN_Z = -61;
    private static final int IMPORTED_ESTATE_FENCE_MIN_X = -128;
    private static final int IMPORTED_ESTATE_FENCE_MAX_X = 122;
    private static final int IMPORTED_ESTATE_FENCE_MIN_Z = -76;
    private static final int IMPORTED_ESTATE_FENCE_MAX_Z = 80;
    private static final int IMPORTED_HOUSE_SIZE_X = 111;
    private static final int IMPORTED_HOUSE_SIZE_Z = 136;
    private static final int IMPORTED_HOUSE_MAX_X = HOUSE_ORIGIN_X + IMPORTED_HOUSE_SIZE_X;
    private static final int IMPORTED_HOUSE_MAX_Z = HOUSE_ORIGIN_Z + IMPORTED_HOUSE_SIZE_Z;
    private static final int CASTLE_SIZE_X = 265;
    private static final int CASTLE_SIZE_Z = 221;
    private static final int CURRENT_ESTATE_REPAIR_VERSION = 19;
    private static final int GLOBAL_VILLAGER_WORK_RADIUS = 384;

    private static final Map<UUID, EstateTask> TASKS = new HashMap<>();
    private static final Set<UUID> PLAYERS_TOUCHING_STARTER_PORTAL = new HashSet<>();

    private record EstateTask(BlockPos base, int step, int ticksUntilNextStep) {
    }

    private enum FunctionalPortalKind {
        NETHER,
        END_PORTAL,
        END_GATEWAY
    }

    @SubscribeEvent
    public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)
                || player.level().isClientSide()
                || !player.level().dimension().equals(Level.OVERWORLD)) {
            return;
        }

        applyMagicWorldServerSettings(player);

        CompoundTag data = player.getPersistentData();
        if (data.getBoolean(ESTATE_CREATED_KEY)
                && data.getInt(ESTATE_REPAIR_VERSION_KEY) < CURRENT_ESTATE_REPAIR_VERSION) {
            repairExistingEstate(levelFor(player), estateBaseFromPlayer(player));
            data.putInt(ESTATE_REPAIR_VERSION_KEY, CURRENT_ESTATE_REPAIR_VERSION);
            player.sendSystemMessage(Component.literal("Magic World: casa e santuario do fim da rua reposicionados e atualizados."));
        }

        if (!MagicWorldWorldOptions.isStarterEstateEnabled()
                || data.getBoolean(ESTATE_CREATED_KEY)
                || TASKS.containsKey(player.getUUID())) {
            return;
        }

        BlockPos base = findEstateBase(player);
        storeEstateBase(player, base);
        TASKS.put(player.getUUID(), new EstateTask(base, 0, START_DELAY_TICKS));
        MagicWorldNetwork.openInitialLoadNotice(player);
        MagicWorldNetwork.sendInitialLoadProgress(player, 3, "Preparando terreno Magic World...", false);
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END
                || !(event.player instanceof ServerPlayer player)
                || player.level().isClientSide()) {
            return;
        }

        handleEstateTask(player);
        handlePremiumPortalToggle(player);
        handleFunctionalEstatePortals(player);
        handleAmbientEffects(player);
        if (player.tickCount % 200 == 0
                && player.level().dimension().equals(Level.OVERWORLD)
                && player.getPersistentData().getBoolean(ESTATE_CREATED_KEY)) {
            maintainEstateLife(player.serverLevel(), estateBaseFromPlayer(player));
        }
    }

    @SubscribeEvent
    public void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.getHand() != InteractionHand.MAIN_HAND
                || event.getLevel().isClientSide()
                || !(event.getEntity() instanceof ServerPlayer player)
                || !(event.getLevel() instanceof ServerLevel level)) {
            return;
        }

        BlockPos marker = isStarterPortalMarker(level, event.getPos())
                ? event.getPos()
                : findNearestStarterPortalMarker(level, event.getPos(), 5);

        if (marker != null) {
            activatePremiumPortal(player, marker);
        }
    }

    private static void handleEstateTask(ServerPlayer player) {
        EstateTask task = TASKS.get(player.getUUID());
        if (task == null) {
            return;
        }

        if (task.ticksUntilNextStep > 0) {
            TASKS.put(player.getUUID(), new EstateTask(task.base, task.step, task.ticksUntilNextStep - 1));
            return;
        }

        if (!(player.level() instanceof ServerLevel level)) {
            TASKS.remove(player.getUUID());
            return;
        }

        switch (task.step) {
            case 0 -> {
                MagicWorldNetwork.sendInitialLoadProgress(player, 12, "Carregando casa importada...", false);
                prepareImportedEstateFoundation(level, task.base);
                buildImportedHouse(level, task.base);
                TASKS.put(player.getUUID(), new EstateTask(task.base, 1, STEP_DELAY_TICKS));
            }
            case 1 -> {
                if (MagicWorldWorldOptions.isFarmsEnabled()) {
                    MagicWorldNetwork.sendInitialLoadProgress(player, 35, "Carregando fazendas, animais e trabalhadores...", false);
                    prepareImportedEstateFoundation(level, task.base);
                    buildImportedEstateFarms(level, task.base);
                    spawnImportedStarterAnimals(level, task.base);
                    convertMainHousePerimeterTreesToCherry(level, task.base);
                }
                TASKS.put(player.getUUID(), new EstateTask(task.base, 2, STEP_DELAY_TICKS));
            }
            case 2 -> {
                MagicWorldNetwork.sendInitialLoadProgress(player, 55, "Carregando portal inicial...", false);
                buildStarterPortal(level, starterPortalCenter(task.base));
                TASKS.put(player.getUUID(), new EstateTask(task.base, 3, STEP_DELAY_TICKS));
            }
            case 3 -> {
                MagicWorldNetwork.sendInitialLoadProgress(player, 72, "Carregando praca de portais funcionais...", false);
                buildFunctionalPortalPlaza(level, task.base);
                TASKS.put(player.getUUID(), new EstateTask(task.base, 4, STEP_DELAY_TICKS));
            }
            case 4 -> {
                if (MagicWorldWorldOptions.isCastlesEnabled()) {
                    MagicWorldNetwork.sendInitialLoadProgress(player, 86, "Carregando castelo importado...", false);
                    buildImportedCastle(level, castleOrigin(task.base));
                    decorateCastleStarterLife(level, castleCenter(task.base));
                    convertCastlePerimeterTreesToCherry(level, task.base);
                }
                TASKS.put(player.getUUID(), new EstateTask(task.base, 5, STEP_DELAY_TICKS));
            }
            case 5 -> {
                MagicWorldNetwork.sendInitialLoadProgress(player, 94, "Carregando casa do fim da rua...", false);
                buildStarterRoadEndHouse(level, task.base);
                TASKS.put(player.getUUID(), new EstateTask(task.base, 6, STEP_DELAY_TICKS));
            }
            case 6 -> {
                MagicWorldNetwork.sendInitialLoadProgress(player, 97, "Carregando santuario magico do fim da rua...", false);
                buildRoadEndMagicSanctuary(level, task.base);
                TASKS.put(player.getUUID(), new EstateTask(task.base, 7, STEP_DELAY_TICKS));
            }
            case 7 -> {
                MagicWorldNetwork.sendInitialLoadProgress(player, 98, "Carregando casa das bruxas na mata...", false);
                buildWitchCovenHouse(level, task.base);
                TASKS.put(player.getUUID(), new EstateTask(task.base, 8, FINAL_DELAY_TICKS));
            }
            default -> {
                restoreStoneTreasureMineHouse(level, task.base);
                player.getPersistentData().putBoolean(ESTATE_CREATED_KEY, true);
                player.getPersistentData().putInt(ESTATE_REPAIR_VERSION_KEY, CURRENT_ESTATE_REPAIR_VERSION);
                applyMagicWorldServerSettings(player);
                teleportPlayerToEstateSpawn(player, level, task.base);
                MagicWorldNetwork.sendInitialLoadProgress(player, 100, "Magic World carregado.", true);
                player.sendSystemMessage(Component.literal("Magic World: casa, fazendas, portais e castelo carregados."));
                TASKS.remove(player.getUUID());
            }
        }
    }

    private static void applyMagicWorldServerSettings(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        applyMagicWorldGameRules(level.getGameRules(), player.server);
        player.server.setDifficulty(toMinecraftDifficulty(MagicWorldWorldOptions.startingDifficulty()), false);

        if (MagicWorldWorldOptions.startingGameMode() == MagicWorldWorldOptions.StartingGameMode.CREATIVE) {
            player.server.setDefaultGameType(GameType.CREATIVE);
            player.server.getWorldData().setGameType(GameType.CREATIVE);
            player.setGameMode(GameType.CREATIVE);
        }

        if (MagicWorldWorldOptions.isCommandsEnabled()) {
            if (!player.server.getPlayerList().isOp(player.getGameProfile())) {
                player.server.getPlayerList().op(player.getGameProfile());
            }
            player.server.getPlayerList().sendPlayerPermissionLevel(player);
        }
    }

    private static void applyMagicWorldGameRules(GameRules rules, net.minecraft.server.MinecraftServer server) {
        rules.getRule(GameRules.RULE_KEEPINVENTORY).set(true, server);
        rules.getRule(GameRules.RULE_DROWNING_DAMAGE).set(false, server);
        rules.getRule(GameRules.RULE_FALL_DAMAGE).set(false, server);
        rules.getRule(GameRules.RULE_FIRE_DAMAGE).set(false, server);
        rules.getRule(GameRules.RULE_FREEZE_DAMAGE).set(false, server);
        rules.getRule(GameRules.RULE_DO_IMMEDIATE_RESPAWN).set(true, server);
        rules.getRule(GameRules.RULE_SENDCOMMANDFEEDBACK).set(true, server);
        rules.getRule(GameRules.RULE_COMMANDBLOCKOUTPUT).set(true, server);
        rules.getRule(GameRules.RULE_LOGADMINCOMMANDS).set(true, server);
    }

    private static Difficulty toMinecraftDifficulty(MagicWorldWorldOptions.StartingDifficulty difficulty) {
        return switch (difficulty) {
            case PEACEFUL -> Difficulty.PEACEFUL;
            case EASY -> Difficulty.EASY;
            case HARD -> Difficulty.HARD;
            default -> Difficulty.NORMAL;
        };
    }

    private static BlockPos findEstateBase(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        BlockPos spawn = level.getSharedSpawnPos();
        int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, spawn.getX(), spawn.getZ());
        return new BlockPos(spawn.getX(), y, spawn.getZ());
    }

    private static void storeEstateBase(ServerPlayer player, BlockPos base) {
        CompoundTag data = player.getPersistentData();
        data.putInt(ESTATE_BASE_X_KEY, base.getX());
        data.putInt(ESTATE_BASE_Y_KEY, base.getY());
        data.putInt(ESTATE_BASE_Z_KEY, base.getZ());
    }

    private static BlockPos estateBaseFromPlayer(ServerPlayer player) {
        CompoundTag data = player.getPersistentData();
        if (!data.contains(ESTATE_BASE_X_KEY) || !data.contains(ESTATE_BASE_Y_KEY) || !data.contains(ESTATE_BASE_Z_KEY)) {
            return findEstateBase(player);
        }
        return new BlockPos(data.getInt(ESTATE_BASE_X_KEY), data.getInt(ESTATE_BASE_Y_KEY), data.getInt(ESTATE_BASE_Z_KEY));
    }

    private static BlockPos houseOrigin(BlockPos base) {
        return base.offset(HOUSE_ORIGIN_X, HOUSE_ORIGIN_Y, HOUSE_ORIGIN_Z);
    }

    private static BlockPos starterPortalCenter(BlockPos base) {
        return new BlockPos(base.getX(), base.getY(), base.getZ() + PORTAL_Z_OFFSET);
    }

    private static BlockPos castleCenter(BlockPos base) {
        return castleOrigin(base).offset(CASTLE_SIZE_X / 2, 0, CASTLE_SIZE_Z / 2);
    }

    private static BlockPos castleOrigin(BlockPos base) {
        // Igual ao NeoForge: a ancora do castelo fica no lado oeste da estrutura,
        // nao no centro. Subtrair metade do X fazia a limpeza apagar a casa.
        return base.offset(CASTLE_X_OFFSET, 0, CASTLE_Z_OFFSET - (CASTLE_SIZE_Z / 2));
    }

    private static BlockPos compactPortalPlazaCenter(BlockPos base) {
        return base.offset(-24, 0, 48);
    }

    private static ServerLevel levelFor(ServerPlayer player) {
        return player.serverLevel();
    }

    private static void repairExistingEstate(ServerLevel level, BlockPos base) {
        fillMissingStarterPortalBlocks(level, starterPortalCenter(base));
        removeLooseDroppedItemsAroundImportedHouse(level, base);
        stabilizeEstateAirGaps(level, base);
        normalizeImportedHouseFrontRoad(level, base);
        buildPlantationWorkerSettlement(level, base);
        buildAnimalCaretakerSettlement(level, base);
        buildGreenVillageSquare(level, base);
        buildEnhancedEstateLighting(level, base);
        restoreImportedHouseTemplate(level, base);
        finishImportedHouseContents(level, base);
        removeLooseDroppedItemsAroundImportedHouse(level, base);
        buildStarterRoadEndHouse(level, base);
        buildRoadEndMagicSanctuary(level, base);
        buildWitchCovenHouse(level, base);
        restoreStoneTreasureMineHouse(level, base);
        restoreAnimalPens(level, base);
        if (MagicWorldWorldOptions.isCastlesEnabled()) {
            populateCastleCouncilTable(level, castleCenter(base));
        }
    }

    private static void fillMissingStarterPortalBlocks(ServerLevel level, BlockPos center) {
        for (int x = -3; x <= 3; x++) {
            for (int z = -2; z <= 2; z++) {
                BlockPos support = center.offset(x, -1, z);
                BlockPos floor = center.offset(x, 0, z);
                if (level.getBlockState(support).isAir()) {
                    level.setBlock(support, Blocks.DIRT.defaultBlockState(), 2);
                }
                if (level.getBlockState(floor).isAir()) {
                    boolean edge = Math.abs(x) == 3 || Math.abs(z) == 2;
                    level.setBlock(floor, edge
                            ? Blocks.POLISHED_ANDESITE.defaultBlockState()
                            : Blocks.SMOOTH_STONE.defaultBlockState(), 2);
                }
            }
        }

        for (int y = 1; y <= 5; y++) {
            setBlockIfAir(level, center.offset(-2, y, 0), Blocks.STRIPPED_OAK_LOG.defaultBlockState());
            setBlockIfAir(level, center.offset(2, y, 0), Blocks.STRIPPED_OAK_LOG.defaultBlockState());
        }
        for (int x = -3; x <= 3; x++) {
            setBlockIfAir(level, center.offset(x, 5, 0), Blocks.OAK_PLANKS.defaultBlockState());
            setBlockIfAir(level, center.offset(x, 6, 0), Blocks.DARK_OAK_SLAB.defaultBlockState());
        }
        for (int y = 1; y <= 4; y++) {
            setBlockIfAir(level, center.offset(-1, y, 0), Blocks.PURPLE_STAINED_GLASS.defaultBlockState());
            setBlockIfAir(level, center.offset(1, y, 0), Blocks.MAGENTA_STAINED_GLASS.defaultBlockState());
        }
    }

    private static void setBlockIfAir(ServerLevel level, BlockPos pos, BlockState state) {
        if (level.getBlockState(pos).isAir()) {
            level.setBlock(pos, state, 2);
        }
    }

    private static void restoreImportedHouseTemplate(ServerLevel level, BlockPos base) {
        BlockPos origin = houseOrigin(base);
        level.getStructureManager().get(IMPORTED_HOUSE).ifPresent(template ->
                template.placeInWorld(
                        level,
                        origin,
                        origin,
                        new StructurePlaceSettings().setIgnoreEntities(true).setKnownShape(true),
                        RandomSource.create(level.getSeed() ^ origin.asLong()),
                        2
                )
        );
    }

    private static void finishImportedHouseContents(ServerLevel level, BlockPos base) {
        fillStarterChests(level, base);
        decorateImportedHouseAddons(level, base);
    }

    private static void maintainEstateLife(ServerLevel level, BlockPos base) {
        BlockPos castleOrigin = castleOrigin(base);
        AABB estate = new AABB(
                Math.min(base.getX() + IMPORTED_ESTATE_FENCE_MIN_X, castleOrigin.getX()) - 24,
                base.getY() - 16,
                Math.min(base.getZ() + IMPORTED_ESTATE_FENCE_MIN_Z, castleOrigin.getZ()) - 24,
                Math.max(base.getX() + IMPORTED_ESTATE_FENCE_MAX_X, castleOrigin.getX() + CASTLE_SIZE_X) + 24,
                base.getY() + 192,
                Math.max(base.getZ() + IMPORTED_ESTATE_FENCE_MAX_Z, castleOrigin.getZ() + CASTLE_SIZE_Z) + 24
        );

        for (Villager villager : level.getEntitiesOfClass(Villager.class, estate)) {
            CompoundTag data = villager.getPersistentData();
            BlockPos home = data.contains("MagicWorldHomeX")
                    ? new BlockPos(data.getInt("MagicWorldHomeX"), data.getInt("MagicWorldHomeY"), data.getInt("MagicWorldHomeZ"))
                    : villager.blockPosition();
            BlockPos work = data.contains("MagicWorldWorkX")
                    ? new BlockPos(data.getInt("MagicWorldWorkX"), data.getInt("MagicWorldWorkY"), data.getInt("MagicWorldWorkZ"))
                    : home;
            int workRadius = Math.max(GLOBAL_VILLAGER_WORK_RADIUS, data.getInt("MagicWorldWorkRadius"));
            VillagerProfession profession = professionForNamedVillager(villager);
            if (profession == VillagerProfession.NONE || profession == VillagerProfession.NITWIT) {
                profession = VillagerProfession.FARMER;
            }
            empowerMagicWorldVillager(villager, profession, home, work, workRadius);
            villager.restrictTo(work, workRadius);
        }

        spawnEstateGuardianVillagers(level, base);
        clearHostilesNearGuardianVillagers(level, estate);
        for (Monster monster : level.getEntitiesOfClass(Monster.class, estate)) {
            monster.discard();
        }
        assignAnimalCaretakersToPens(level, base);
        ensureAnimalPenPopulations(level, base);
        encourageAnimalPenBreeding(level, base);
    }

    private static VillagerProfession professionForNamedVillager(Villager villager) {
        return switch (villager.getName().getString()) {
            case "Bibliotecario Real", "Bibliotecario do Conselho" -> VillagerProfession.LIBRARIAN;
            case "Cartografo da Torre", "Cartografo do Conselho" -> VillagerProfession.CARTOGRAPHER;
            case "Armoreiro do Castelo", "Armoreiro do Conselho" -> VillagerProfession.ARMORER;
            case "Clerigo da Capela", "Clerigo do Conselho" -> VillagerProfession.CLERIC;
            case "Pedreiro Real" -> VillagerProfession.MASON;
            case "Ferreiro de Armas" -> VillagerProfession.WEAPONSMITH;
            case "Ferreiro de Ferramentas", "Ferreiro do Castelo" -> VillagerProfession.TOOLSMITH;
            case "Flecheiro da Guarda" -> VillagerProfession.FLETCHER;
            case "Cozinheiro do Salao" -> VillagerProfession.BUTCHER;
            case "Tecelao da Ponte" -> VillagerProfession.SHEPHERD;
            case "Coureeiro da Cavalaria" -> VillagerProfession.LEATHERWORKER;
            case "Guardiao Aldeao da Casa Grande", "Guardiao Aldeao da Mina",
                    "Guardiao Aldeao dos Currais", "Guardiao Aldeao da Plantacao",
                    "Guardiao Aldeao da Praca Verde" -> VillagerProfession.WEAPONSMITH;
            case "Armoreiro da Casa Grande" -> VillagerProfession.ARMORER;
            case "Ferreiro da Casa Grande" -> VillagerProfession.WEAPONSMITH;
            case "Ferramenteiro da Casa Grande" -> VillagerProfession.TOOLSMITH;
            case "Bibliotecario da Casa Grande" -> VillagerProfession.LIBRARIAN;
            case "Clerigo da Casa Grande" -> VillagerProfession.CLERIC;
            case "Pedreiro da Casa Grande" -> VillagerProfession.MASON;
            default -> villager.getVillagerData().getProfession();
        };
    }

    private static boolean isInsideImportedHouseFootprint(int x, int z) {
        return x >= HOUSE_ORIGIN_X - BREATHING_MARGIN
                && x <= IMPORTED_HOUSE_MAX_X + BREATHING_MARGIN
                && z >= HOUSE_ORIGIN_Z - BREATHING_MARGIN
                && z <= IMPORTED_HOUSE_MAX_Z + BREATHING_MARGIN;
    }

    private static boolean isInsideImportedHouseFootprint(BlockPos base, BlockPos pos) {
        return isInsideImportedHouseFootprint(pos.getX() - base.getX(), pos.getZ() - base.getZ());
    }

    private static void removeLooseDroppedItemsAroundImportedHouse(ServerLevel level, BlockPos base) {
        AABB area = new AABB(
                base.getX() + HOUSE_ORIGIN_X - 18,
                base.getY() - 10,
                base.getZ() + HOUSE_ORIGIN_Z - 18,
                base.getX() + IMPORTED_ESTATE_FENCE_MAX_X + 18,
                base.getY() + 36,
                base.getZ() + IMPORTED_ESTATE_FENCE_MAX_Z + 18
        );
        for (ItemEntity item : level.getEntitiesOfClass(ItemEntity.class, area)) {
            item.discard();
        }
    }

    private static void restoreStoneTreasureMineHouse(ServerLevel level, BlockPos base) {
        BlockPos center = base.offset(67, -1, 46);
        removeLooseDroppedItemsAroundMineHouse(level, center);
        buildStoneTreasureMineHouse(level, center);
        removeLooseDroppedItemsAroundMineHouse(level, center);
    }

    private static void removeLooseDroppedItemsAroundMineHouse(ServerLevel level, BlockPos center) {
        AABB area = new AABB(center).inflate(24.0D, 16.0D, 24.0D);
        for (ItemEntity item : level.getEntitiesOfClass(ItemEntity.class, area)) {
            item.discard();
        }
    }

    private static void prepareImportedEstateFoundation(ServerLevel level, BlockPos base) {
        for (int x = IMPORTED_ESTATE_FENCE_MIN_X; x <= IMPORTED_ESTATE_FENCE_MAX_X; x++) {
            for (int z = IMPORTED_ESTATE_FENCE_MIN_Z; z <= IMPORTED_ESTATE_FENCE_MAX_Z; z++) {
                if (isInsideImportedHouseFootprint(x, z)) {
                    continue;
                }

                BlockPos ground = base.offset(x, -1, z);
                for (int y = -5; y <= -2; y++) {
                    level.setBlock(base.offset(x, y, z), Blocks.DIRT.defaultBlockState(), 2);
                }
                level.setBlock(ground, Blocks.GRASS_BLOCK.defaultBlockState(), 2);
                for (int y = 0; y <= 24; y++) {
                    BlockPos clear = base.offset(x, y, z);
                    if (!isProtectedGeneratedBlock(level.getBlockState(clear))) {
                        level.setBlock(clear, Blocks.AIR.defaultBlockState(), 2);
                    }
                }
            }
        }
    }

    private static boolean isProtectedGeneratedBlock(BlockState state) {
        return state.is(Blocks.CHEST)
                || state.is(Blocks.ENDER_CHEST)
                || state.is(Blocks.NETHER_PORTAL)
                || state.is(Blocks.END_PORTAL)
                || state.is(Blocks.END_GATEWAY)
                || state.is(Blocks.END_PORTAL_FRAME)
                || state.is(Blocks.BEDROCK);
    }

    private static BlockPos netherPortalCenter(BlockPos center) {
        return center.offset(-7, 0, 0);
    }

    private static BlockPos endPortalCenter(BlockPos center) {
        return center;
    }

    private static BlockPos gatewayPortalCenter(BlockPos center) {
        return center.offset(7, 0, 0);
    }

    private static void buildImportedHouse(ServerLevel level, BlockPos base) {
        BlockPos origin = houseOrigin(base);
        Optional<StructureTemplate> optional = level.getStructureManager().get(IMPORTED_HOUSE);
        if (optional.isPresent()) {
            StructureTemplate template = optional.get();
            clearStructureVolume(level, origin, template.getSize(), BREATHING_MARGIN, true);
            template.placeInWorld(level, origin, origin, new StructurePlaceSettings(), RandomSource.create(level.getSeed()), 2);
            prepareBreathingSurface(level, origin, template.getSize(), BREATHING_MARGIN);
            normalizeImportedHouseFrontRoad(level, base);
            stabilizeImportedHousePerimeterTerrain(level, base);
            finishImportedHouseContents(level, base);
        } else {
            buildFallbackHouse(level, base);
            stabilizeImportedHousePerimeterTerrain(level, base);
        }
    }

    private static void stabilizeImportedHousePerimeterTerrain(ServerLevel level, BlockPos base) {
        int minX = Math.max(IMPORTED_ESTATE_FENCE_MIN_X, HOUSE_ORIGIN_X - 18);
        int maxX = Math.min(IMPORTED_ESTATE_FENCE_MAX_X, IMPORTED_HOUSE_MAX_X + 18);
        int minZ = Math.max(IMPORTED_ESTATE_FENCE_MIN_Z, HOUSE_ORIGIN_Z - 18);
        int maxZ = Math.min(IMPORTED_ESTATE_FENCE_MAX_Z, IMPORTED_HOUSE_MAX_Z + 18);

        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                if (x >= HOUSE_ORIGIN_X - 1
                        && x <= IMPORTED_HOUSE_MAX_X + 1
                        && z >= HOUSE_ORIGIN_Z - 1
                        && z <= IMPORTED_HOUSE_MAX_Z + 1) {
                    continue;
                }

                fillHousePerimeterColumn(level, base, x, z);
            }
        }
    }

    private static void fillHousePerimeterColumn(ServerLevel level, BlockPos base, int x, int z) {
        BlockPos ground = base.offset(x, -1, z);
        for (int y = -8; y <= -2; y++) {
            BlockPos support = base.offset(x, y, z);
            if (canPatchHousePerimeterGround(level.getBlockState(support))) {
                level.setBlock(support, Blocks.DIRT.defaultBlockState(), 2);
            }
        }

        BlockState groundState = level.getBlockState(ground);
        if (canPatchHousePerimeterGround(groundState)) {
            level.setBlock(ground, Blocks.GRASS_BLOCK.defaultBlockState(), 2);
        }
    }

    private static boolean canPatchHousePerimeterGround(BlockState state) {
        return state.isAir()
                || state.getFluidState().isSource()
                || state.is(Blocks.DIRT)
                || state.is(Blocks.GRASS_BLOCK)
                || state.is(Blocks.COARSE_DIRT)
                || state.is(Blocks.PODZOL)
                || state.is(Blocks.ROOTED_DIRT)
                || state.is(Blocks.STONE)
                || state.is(Blocks.DEEPSLATE)
                || state.is(Blocks.GRAVEL)
                || state.is(Blocks.SAND)
                || state.is(Blocks.CLAY);
    }

    private static BlockPos starterRoadEndHouseOrigin(BlockPos base) {
        return base.offset(-5, 1, -74);
    }

    private static void buildStarterRoadEndHouse(ServerLevel level, BlockPos base) {
        BlockPos origin = starterRoadEndHouseOrigin(base);
        Optional<StructureTemplate> optional = level.getStructureManager().get(STARTER_ROAD_END_HOUSE);
        if (optional.isEmpty()) {
            return;
        }

        StructureTemplate template = optional.get();
        Vec3i size = template.getSize();
        forceLoadStructureArea(level, origin, size.getX(), size.getZ(), 4);
        clearStructureVolume(level, origin, size, 3, true);
        prepareStarterRoadEndHouseCleanSupport(level, origin, size);
        StructurePlaceSettings settings = new StructurePlaceSettings()
                .setIgnoreEntities(true)
                .setKnownShape(true)
                .setRotation(Rotation.CLOCKWISE_180)
                .setRotationPivot(new BlockPos(size.getX() / 2, 0, size.getZ() / 2));
        template.placeInWorld(
                level,
                origin,
                origin,
                settings,
                RandomSource.create(level.getSeed() ^ origin.asLong()),
                2
        );
    }

    private static void prepareStarterRoadEndHouseCleanSupport(ServerLevel level, BlockPos origin, Vec3i size) {
        for (int x = 0; x < size.getX(); x++) {
            for (int z = 0; z < size.getZ(); z++) {
                for (int y = -7; y < 0; y++) {
                    level.setBlock(origin.offset(x, y, z), Blocks.DIRT.defaultBlockState(), 2);
                }
            }
        }
    }

    private static BlockPos roadEndMagicSanctuaryOrigin(BlockPos base) {
        return base.offset(-54, -4, -8);
    }

    private static void buildRoadEndMagicSanctuary(ServerLevel level, BlockPos base) {
        BlockPos origin = roadEndMagicSanctuaryOrigin(base);
        int width = 36;
        int depth = 17;
        int height = 10;
        int centerZ = depth / 2;

        forceLoadAreaBetween(level, base.offset(-18, 0, -12), origin.offset(width + 8, 0, depth + 6));
        buildRoadBetween(level, base, base.offset(-4, -1, 0), origin.offset(width, -1, centerZ));
        prepareRoadEndMagicSanctuaryShell(level, origin, width, depth, height, centerZ);
        clearRoadEndSanctuaryEntrance(level, origin, width, centerZ);
        buildRoadEndSanctuaryStorage(level, origin, width, depth);
        buildRoadEndSanctuaryStations(level, origin, width, depth);
        buildRoadEndSanctuaryMeetingTable(level, origin, width, depth);
        buildRoadEndSanctuaryArmorGallery(level, origin, width, depth);
        decorateRoadEndSanctuary(level, origin, width, depth, height);
        populateRoadEndSanctuary(level, origin, width, depth);
    }

    private static void prepareRoadEndMagicSanctuaryShell(
            ServerLevel level,
            BlockPos origin,
            int width,
            int depth,
            int height,
            int centerZ
    ) {
        for (int x = 0; x <= width; x++) {
            for (int z = 0; z <= depth; z++) {
                BlockPos floor = origin.offset(x, 0, z);
                boolean edge = x == 0 || x == width || z == 0 || z == depth;
                boolean entrance = (z >= centerZ - 2 && z <= centerZ + 2) && x == width;
                level.setBlock(floor.below(), Blocks.POLISHED_DEEPSLATE.defaultBlockState(), 2);
                level.setBlock(floor, sanctuaryFloorBlock(x, z), 2);
                for (int y = 1; y <= height; y++) {
                    BlockPos pos = floor.above(y);
                    if (edge && !entrance) {
                        level.setBlock(pos, sanctuaryWallBlock(x, y, z), 2);
                    } else {
                        level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
                    }
                }
            }
        }

        for (int x = 0; x <= width; x++) {
            for (int z = 0; z <= depth; z++) {
                boolean edge = x == 0 || x == width || z == 0 || z == depth;
                BlockState ceiling = edge || Math.floorMod(x + z, 5) == 0
                        ? Blocks.AMETHYST_BLOCK.defaultBlockState()
                        : Blocks.CALCITE.defaultBlockState();
                level.setBlock(origin.offset(x, height + 1, z), ceiling, 2);
                if (Math.floorMod(x * 17 + z * 11, 13) == 0) {
                    level.setBlock(origin.offset(x, height, z), Blocks.SEA_LANTERN.defaultBlockState(), 2);
                }
            }
        }

        for (int x = 0; x < width; x += 5) {
            level.setBlock(origin.offset(x, 3, centerZ), Blocks.REDSTONE_BLOCK.defaultBlockState(), 2);
            level.setBlock(origin.offset(x, 4, centerZ), Blocks.REDSTONE_LAMP.defaultBlockState(), 2);
            level.setBlock(origin.offset(x, 5, centerZ), Blocks.SEA_LANTERN.defaultBlockState(), 2);
        }
    }

    private static void clearRoadEndSanctuaryEntrance(ServerLevel level, BlockPos origin, int width, int centerZ) {
        for (int dx = 1; dx <= 6; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                BlockPos floor = origin.offset(width + dx, 0, centerZ + dz);
                level.setBlock(floor.below(), Blocks.DIRT.defaultBlockState(), 2);
                level.setBlock(floor, Math.abs(dz) <= 1
                        ? Blocks.SMOOTH_STONE.defaultBlockState()
                        : Blocks.POLISHED_ANDESITE.defaultBlockState(), 2);
                for (int y = 1; y <= 5; y++) {
                    level.setBlock(floor.above(y), Blocks.AIR.defaultBlockState(), 2);
                }
            }
        }

        for (int dz = -2; dz <= 2; dz++) {
            BlockPos mouth = origin.offset(width, 0, centerZ + dz);
            for (int y = 1; y <= 5; y++) {
                level.setBlock(mouth.above(y), Blocks.AIR.defaultBlockState(), 2);
            }
        }

        level.setBlock(origin.offset(width + 1, 1, centerZ - 3), Blocks.SEA_LANTERN.defaultBlockState(), 2);
        level.setBlock(origin.offset(width + 1, 1, centerZ + 3), Blocks.SEA_LANTERN.defaultBlockState(), 2);
    }

    private static BlockState sanctuaryFloorBlock(int x, int z) {
        if (Math.floorMod(x - z, 9) == 0) {
            return Blocks.AMETHYST_BLOCK.defaultBlockState();
        }
        if (Math.floorMod(x + z, 7) == 0) {
            return Blocks.SEA_LANTERN.defaultBlockState();
        }
        if (Math.floorMod(x * 3 + z * 5, 11) == 0) {
            return Blocks.PURPUR_BLOCK.defaultBlockState();
        }
        return Math.floorMod(x + z, 2) == 0
                ? Blocks.POLISHED_ANDESITE.defaultBlockState()
                : Blocks.SMOOTH_QUARTZ.defaultBlockState();
    }

    private static BlockState sanctuaryWallBlock(int x, int y, int z) {
        if (y == 1 && Math.floorMod(x * 13 + z * 7, 6) == 0) {
            return Blocks.CHISELED_QUARTZ_BLOCK.defaultBlockState();
        }
        if (y >= 4 && Math.floorMod(x + y + z, 5) == 0) {
            return Blocks.PURPLE_STAINED_GLASS.defaultBlockState();
        }
        if (Math.floorMod(x * 5 + y * 3 + z, 9) == 0) {
            return Blocks.GLOWSTONE.defaultBlockState();
        }
        return Blocks.DEEPSLATE_TILES.defaultBlockState();
    }

    private static void buildRoadEndSanctuaryStorage(ServerLevel level, BlockPos origin, int width, int depth) {
        List<BlockPos> itemCatalogContainers = new ArrayList<>();
        int centerZ = depth / 2;
        for (int z = 1; z < depth; z++) {
            if (z >= centerZ - 3 && z <= centerZ + 3) {
                continue;
            }
            BlockPos chest = origin.offset(width - 1, 1, z);
            placeStorageChest(level, chest, Direction.WEST, Math.floorMod(z, 2) == 0);
            itemCatalogContainers.add(chest);
            for (int y = 3; y <= 6; y++) {
                BlockPos barrel = origin.offset(width - 1, y, z);
                placeStorageBarrel(level, barrel);
                itemCatalogContainers.add(barrel);
            }
        }
        for (int x = 3; x <= width - 7; x += 4) {
            BlockPos north = origin.offset(x, 1, 1);
            BlockPos south = origin.offset(x, 1, depth - 1);
            placeStorageChest(level, north, Direction.SOUTH, false);
            placeStorageChest(level, south, Direction.NORTH, true);
            itemCatalogContainers.add(north);
            itemCatalogContainers.add(south);
        }
        fillContainersWithAllRegisteredItems(level, itemCatalogContainers);

        BlockPos wandChest = origin.offset(4, 1, depth - 3);
        placeStorageChest(level, wandChest, Direction.NORTH, false);
        fillContainerWithItem(level, wandChest, MagicWorld.VARINHA_MAGICA.get());

        BlockPos premiumChest = origin.offset(8, 1, depth - 3);
        placeStorageChest(level, premiumChest, Direction.NORTH, true);
        putItems(level, premiumChest,
                new ItemStack(Items.ELYTRA), new ItemStack(Items.DRAGON_EGG),
                new ItemStack(Items.NETHER_STAR, 32), new ItemStack(Items.BEACON, 16),
                new ItemStack(Items.ENCHANTED_GOLDEN_APPLE, 64), new ItemStack(Items.TOTEM_OF_UNDYING, 32),
                new ItemStack(Items.NETHERITE_INGOT, 64), new ItemStack(Items.DIAMOND, 64),
                new ItemStack(Items.EMERALD, 64), new ItemStack(Items.HEART_OF_THE_SEA, 32),
                new ItemStack(Items.CONDUIT, 16), new ItemStack(Items.PAINTING, 64));
    }

    private static void buildRoadEndSanctuaryStations(ServerLevel level, BlockPos origin, int width, int depth) {
        int z = 3;
        for (int x = 3; x <= 16; x++) {
            BlockPos pos = origin.offset(x, 1, z);
            BlockState state = switch (x - 3) {
                case 0 -> Blocks.CRAFTING_TABLE.defaultBlockState();
                case 1 -> Blocks.SMITHING_TABLE.defaultBlockState();
                case 2 -> Blocks.ANVIL.defaultBlockState();
                case 3 -> Blocks.GRINDSTONE.defaultBlockState();
                case 4 -> Blocks.STONECUTTER.defaultBlockState();
                case 5 -> Blocks.FURNACE.defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, Direction.SOUTH);
                case 6 -> Blocks.BLAST_FURNACE.defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, Direction.SOUTH);
                case 7 -> Blocks.SMOKER.defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, Direction.SOUTH);
                case 8 -> Blocks.BREWING_STAND.defaultBlockState();
                case 9 -> Blocks.ENCHANTING_TABLE.defaultBlockState();
                case 10 -> Blocks.LECTERN.defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, Direction.SOUTH);
                case 11 -> Blocks.CARTOGRAPHY_TABLE.defaultBlockState();
                case 12 -> Blocks.FLETCHING_TABLE.defaultBlockState();
                default -> Blocks.LOOM.defaultBlockState();
            };
            level.setBlock(pos, state, 2);
        }
        level.setBlock(origin.offset(17, 1, z), Blocks.CAULDRON.defaultBlockState(), 2);

        BlockPos toolsChest = origin.offset(3, 1, depth - 4);
        placeStorageChest(level, toolsChest, Direction.NORTH, false);
        putItems(level, toolsChest,
                new ItemStack(Items.WOODEN_PICKAXE), new ItemStack(Items.WOODEN_AXE), new ItemStack(Items.WOODEN_SHOVEL), new ItemStack(Items.WOODEN_HOE),
                new ItemStack(Items.STONE_PICKAXE), new ItemStack(Items.STONE_AXE), new ItemStack(Items.STONE_SHOVEL), new ItemStack(Items.STONE_HOE),
                new ItemStack(Items.IRON_PICKAXE), new ItemStack(Items.IRON_AXE), new ItemStack(Items.IRON_SHOVEL), new ItemStack(Items.IRON_HOE),
                new ItemStack(Items.GOLDEN_PICKAXE), new ItemStack(Items.GOLDEN_AXE), new ItemStack(Items.GOLDEN_SHOVEL), new ItemStack(Items.GOLDEN_HOE),
                new ItemStack(Items.DIAMOND_PICKAXE), new ItemStack(Items.DIAMOND_AXE), new ItemStack(Items.DIAMOND_SHOVEL), new ItemStack(Items.DIAMOND_HOE),
                new ItemStack(Items.NETHERITE_PICKAXE), new ItemStack(Items.NETHERITE_AXE), new ItemStack(Items.NETHERITE_SHOVEL), new ItemStack(Items.NETHERITE_HOE),
                new ItemStack(Items.BOW), new ItemStack(Items.CROSSBOW), new ItemStack(Items.FISHING_ROD), new ItemStack(Items.SHIELD));
    }

    private static void buildRoadEndSanctuaryMeetingTable(ServerLevel level, BlockPos origin, int width, int depth) {
        BlockPos center = origin.offset(width / 2, 1, depth / 2);
        for (int x = -5; x <= 5; x++) {
            for (int z = -2; z <= 2; z++) {
                BlockPos table = center.offset(x, 0, z);
                level.setBlock(table, Blocks.DARK_OAK_FENCE.defaultBlockState(), 2);
                level.setBlock(table.above(), Math.floorMod(x + z, 2) == 0
                        ? Blocks.PURPLE_CARPET.defaultBlockState()
                        : Blocks.LIGHT_BLUE_CARPET.defaultBlockState(), 2);
            }
        }
        for (int x = -5; x <= 5; x += 2) {
            level.setBlock(center.offset(x, 0, -3), Blocks.DARK_OAK_STAIRS.defaultBlockState()
                    .setValue(StairBlock.FACING, Direction.SOUTH), 2);
            level.setBlock(center.offset(x, 0, 3), Blocks.DARK_OAK_STAIRS.defaultBlockState()
                    .setValue(StairBlock.FACING, Direction.NORTH), 2);
        }
        for (int z = -2; z <= 2; z += 2) {
            level.setBlock(center.offset(-6, 0, z), Blocks.DARK_OAK_STAIRS.defaultBlockState()
                    .setValue(StairBlock.FACING, Direction.EAST), 2);
            level.setBlock(center.offset(6, 0, z), Blocks.DARK_OAK_STAIRS.defaultBlockState()
                    .setValue(StairBlock.FACING, Direction.WEST), 2);
        }
        level.setBlock(center.above(4), Blocks.SEA_LANTERN.defaultBlockState(), 2);
        level.setBlock(center.above(5), Blocks.BELL.defaultBlockState(), 2);
    }

    private static void buildRoadEndSanctuaryArmorGallery(ServerLevel level, BlockPos origin, int width, int depth) {
        int z = depth - 5;
        spawnArmorStand(level, origin.offset(13, 1, z), Items.LEATHER_HELMET, Items.LEATHER_CHESTPLATE, Items.LEATHER_LEGGINGS, Items.LEATHER_BOOTS, "Santuario Armadura de Couro");
        spawnArmorStand(level, origin.offset(15, 1, z), Items.CHAINMAIL_HELMET, Items.CHAINMAIL_CHESTPLATE, Items.CHAINMAIL_LEGGINGS, Items.CHAINMAIL_BOOTS, "Santuario Armadura de Malha");
        spawnArmorStand(level, origin.offset(17, 1, z), Items.IRON_HELMET, Items.IRON_CHESTPLATE, Items.IRON_LEGGINGS, Items.IRON_BOOTS, "Santuario Armadura de Ferro");
        spawnArmorStand(level, origin.offset(19, 1, z), Items.GOLDEN_HELMET, Items.GOLDEN_CHESTPLATE, Items.GOLDEN_LEGGINGS, Items.GOLDEN_BOOTS, "Santuario Armadura de Ouro");
        spawnArmorStand(level, origin.offset(21, 1, z), Items.DIAMOND_HELMET, Items.DIAMOND_CHESTPLATE, Items.DIAMOND_LEGGINGS, Items.DIAMOND_BOOTS, "Santuario Armadura de Diamante");
        spawnArmorStand(level, origin.offset(23, 1, z), Items.NETHERITE_HELMET, Items.NETHERITE_CHESTPLATE, Items.NETHERITE_LEGGINGS, Items.NETHERITE_BOOTS, "Santuario Armadura de Netherite");
        spawnArmorStand(level, origin.offset(25, 1, z),
                new ItemStack(MagicWorld.DRACONIC_AETHER_HELMET.get()),
                new ItemStack(MagicWorld.DRACONIC_AETHER_CHESTPLATE.get()),
                new ItemStack(MagicWorld.DRACONIC_AETHER_LEGGINGS.get()),
                new ItemStack(MagicWorld.DRACONIC_AETHER_BOOTS.get()),
                "Santuario Armadura Draconic Aether");
    }

    private static void decorateRoadEndSanctuary(ServerLevel level, BlockPos origin, int width, int depth, int height) {
        BlockState[] banners = {
                Blocks.WHITE_BANNER.defaultBlockState(), Blocks.ORANGE_BANNER.defaultBlockState(),
                Blocks.MAGENTA_BANNER.defaultBlockState(), Blocks.LIGHT_BLUE_BANNER.defaultBlockState(),
                Blocks.YELLOW_BANNER.defaultBlockState(), Blocks.LIME_BANNER.defaultBlockState(),
                Blocks.PINK_BANNER.defaultBlockState(), Blocks.CYAN_BANNER.defaultBlockState(),
                Blocks.PURPLE_BANNER.defaultBlockState(), Blocks.BLUE_BANNER.defaultBlockState(),
                Blocks.RED_BANNER.defaultBlockState(), Blocks.BLACK_BANNER.defaultBlockState()
        };
        for (int i = 0; i < banners.length; i++) {
            int bannerX = 2 + i * 3;
            if (bannerX < width) {
                level.setBlock(origin.offset(bannerX, 1, 2), banners[i], 2);
            }
        }

        for (int x = 3; x <= width - 3; x += 5) {
            for (int z : new int[] {2, depth - 2}) {
                BlockPos art = origin.offset(x, 3, z);
                level.setBlock(art, Blocks.BOOKSHELF.defaultBlockState(), 2);
                level.setBlock(art.above(), Blocks.LIGHT_BLUE_STAINED_GLASS.defaultBlockState(), 2);
                level.setBlock(art.above(2), Blocks.PURPLE_STAINED_GLASS.defaultBlockState(), 2);
            }
        }

        for (BlockPos pos : new BlockPos[] {
                origin.offset(2, 1, 5), origin.offset(5, 1, depth - 5),
                origin.offset(width - 7, 1, 5), origin.offset(width - 5, 1, depth - 5),
                origin.offset(width / 2 - 8, 1, 3), origin.offset(width / 2 + 8, 1, depth - 3)
        }) {
            level.setBlock(pos, Blocks.POTTED_BLUE_ORCHID.defaultBlockState(), 2);
        }
        for (BlockPos pos : new BlockPos[] {
                origin.offset(4, 1, 6), origin.offset(7, 1, depth - 6),
                origin.offset(width - 9, 1, 6), origin.offset(width - 8, 1, depth - 6)
        }) {
            level.setBlock(pos, Blocks.FLOWERING_AZALEA.defaultBlockState(), 2);
        }
        for (int x = 4; x <= width - 4; x += 6) {
            level.setBlock(origin.offset(x, height - 1, 1), Blocks.END_ROD.defaultBlockState(), 2);
            level.setBlock(origin.offset(x, height - 1, depth - 1), Blocks.END_ROD.defaultBlockState(), 2);
        }
    }

    private static void populateRoadEndSanctuary(ServerLevel level, BlockPos origin, int width, int depth) {
        spawnNamed(level, EntityType.ALLAY, origin.offset(width / 2 - 3, 2, depth / 2), "Brilho do Santuario 1");
        spawnNamed(level, EntityType.ALLAY, origin.offset(width / 2 + 3, 2, depth / 2), "Brilho do Santuario 2");
        spawnNamed(level, EntityType.PARROT, origin.offset(5, 2, 5), "Passaro Azul do Santuario");
        spawnNamed(level, EntityType.PARROT, origin.offset(width - 6, 2, depth - 5), "Passaro Magico do Santuario");
        spawnNamed(level, EntityType.RABBIT, origin.offset(8, 1, depth / 2), "Coelho do Santuario 1");
        spawnNamed(level, EntityType.RABBIT, origin.offset(width - 9, 1, depth / 2), "Coelho do Santuario 2");
    }

    private static BlockPos witchCovenAnchor(BlockPos base) {
        // Print atual: centro marcado pelo usuario em Block X 55 / Z 3.
        // Base historica do mapa de testes: X -60 / Z 30.
        return base.offset(115, 0, -27);
    }

    private static void buildWitchCovenHouse(ServerLevel level, BlockPos base) {
        BlockPos markedCenter = witchCovenAnchor(base);
        int floorY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, markedCenter.getX(), markedCenter.getZ()) - 2;
        BlockPos center = new BlockPos(markedCenter.getX(), floorY, markedCenter.getZ());
        BlockPos origin = center.offset(-6, 0, -6);
        int houseSize = 12;
        int fenceWest = -2;
        int fenceEast = 13;
        int fenceNorth = -2;
        int fenceSouth = 13;
        int doorZ = houseSize / 2;
        int height = 5;

        forceLoadAreaBetween(level, origin.offset(fenceWest - 2, 0, fenceNorth - 2), origin.offset(fenceEast + 2, 0, fenceSouth + 2));
        prepareCompactWitchCovenClearing(level, origin, fenceWest, fenceEast, fenceNorth, fenceSouth, height + 8);
        buildCompactWitchCovenFence(level, origin, fenceWest, fenceEast, fenceNorth, fenceSouth, doorZ);
        buildCompactWitchCovenShell(level, origin, houseSize, height, doorZ);
        buildCompactWitchCovenInterior(level, origin, houseSize, height);
        decorateCompactWitchCoven(level, origin, houseSize, doorZ, fenceWest, fenceEast, fenceNorth, fenceSouth);
        populateCompactWitchCoven(level, origin, houseSize);
    }

    private static void prepareCompactWitchCovenClearing(
            ServerLevel level,
            BlockPos origin,
            int fenceWest,
            int fenceEast,
            int fenceNorth,
            int fenceSouth,
            int clearHeight
    ) {
        for (int x = fenceWest; x <= fenceEast; x++) {
            for (int z = fenceNorth; z <= fenceSouth; z++) {
                BlockPos floor = origin.offset(x, 0, z);
                for (int y = -6; y < 0; y++) {
                    level.setBlock(floor.offset(0, y, 0), Blocks.DIRT.defaultBlockState(), 2);
                }
                level.setBlock(floor, witchCovenYardBlock(x, z), 2);
                for (int y = 1; y <= clearHeight; y++) {
                    BlockPos clear = floor.above(y);
                    if (!level.getBlockState(clear).isAir()) {
                        if (level.getBlockEntity(clear) instanceof Container container) {
                            container.clearContent();
                        }
                        level.setBlock(clear, Blocks.AIR.defaultBlockState(), 2);
                    }
                }
            }
        }
    }

    private static void buildCompactWitchCovenFence(
            ServerLevel level,
            BlockPos origin,
            int fenceWest,
            int fenceEast,
            int fenceNorth,
            int fenceSouth,
            int doorZ
    ) {
        for (int x = fenceWest; x <= fenceEast; x++) {
            level.setBlock(origin.offset(x, 1, fenceNorth), Blocks.DARK_OAK_FENCE.defaultBlockState(), 2);
            level.setBlock(origin.offset(x, 1, fenceSouth), Blocks.DARK_OAK_FENCE.defaultBlockState(), 2);
        }
        for (int z = fenceNorth; z <= fenceSouth; z++) {
            level.setBlock(origin.offset(fenceWest, 1, z), Blocks.DARK_OAK_FENCE.defaultBlockState(), 2);
            if (z == doorZ) {
                level.setBlock(origin.offset(fenceEast, 1, z), Blocks.DARK_OAK_FENCE_GATE.defaultBlockState()
                        .setValue(HorizontalDirectionalBlock.FACING, Direction.EAST), 2);
            } else {
                level.setBlock(origin.offset(fenceEast, 1, z), Blocks.DARK_OAK_FENCE.defaultBlockState(), 2);
            }
        }
        placeWitchWarningSign(level, origin.offset(fenceEast, 1, doorZ).north());
    }

    private static void buildCompactWitchCovenShell(ServerLevel level, BlockPos origin, int houseSize, int height, int doorZ) {
        int max = houseSize - 1;
        for (int x = 0; x < houseSize; x++) {
            for (int z = 0; z < houseSize; z++) {
                boolean edge = x == 0 || x == max || z == 0 || z == max;
                boolean door = x == max && z == doorZ;
                BlockPos floor = origin.offset(x, 0, z);
                level.setBlock(floor.below(), Blocks.DIRT.defaultBlockState(), 2);
                level.setBlock(floor, witchCovenFloorBlock(x, z), 2);
                for (int y = 1; y <= height; y++) {
                    BlockPos pos = floor.above(y);
                    if (edge && !(door && y <= 2)) {
                        level.setBlock(pos, witchCovenWallBlock(x, y, z), 2);
                    } else {
                        level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
                    }
                }
            }
        }

        placeWitchDoor(level, origin.offset(max, 1, doorZ), Direction.EAST);
        for (int x = -1; x <= houseSize; x++) {
            for (int z = -1; z <= houseSize; z++) {
                boolean roofEdge = x == -1 || x == houseSize || z == -1 || z == houseSize;
                level.setBlock(origin.offset(x, height + 1, z), roofEdge
                        ? Blocks.DARK_OAK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, roofFacingForEdge(x, z, max, max))
                        : Blocks.DEEPSLATE_TILES.defaultBlockState(), 2);
            }
        }

        for (int z : new int[] {3, 8}) {
            level.setBlock(origin.offset(0, 3, z), Blocks.PURPLE_STAINED_GLASS_PANE.defaultBlockState(), 2);
            level.setBlock(origin.offset(max, 3, z), Blocks.PURPLE_STAINED_GLASS_PANE.defaultBlockState(), 2);
        }
        for (int x : new int[] {3, 8}) {
            level.setBlock(origin.offset(x, 3, 0), Blocks.PURPLE_STAINED_GLASS_PANE.defaultBlockState(), 2);
            level.setBlock(origin.offset(x, 3, max), Blocks.PURPLE_STAINED_GLASS_PANE.defaultBlockState(), 2);
        }
        buildCompactWitchFireplace(level, origin.offset(2, 1, max - 2), height);
    }

    private static void buildCompactWitchCovenInterior(ServerLevel level, BlockPos origin, int houseSize, int height) {
        placeBed(level, origin.offset(2, 1, 2), Direction.SOUTH);
        placeBed(level, origin.offset(5, 1, 2), Direction.SOUTH);
        placeBed(level, origin.offset(8, 1, 2), Direction.SOUTH);

        BlockPos table = origin.offset(6, 1, 7);
        for (int dx = -1; dx <= 1; dx++) {
            level.setBlock(table.offset(dx, 0, 0), Blocks.DARK_OAK_FENCE.defaultBlockState(), 2);
            level.setBlock(table.offset(dx, 1, 0), dx == 0 ? Blocks.PURPLE_CARPET.defaultBlockState() : Blocks.BLACK_CARPET.defaultBlockState(), 2);
        }
        level.setBlock(table.north(), Blocks.DARK_OAK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.SOUTH), 2);
        level.setBlock(table.south(), Blocks.DARK_OAK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.NORTH), 2);

        level.setBlock(origin.offset(3, 1, 9), Blocks.CAULDRON.defaultBlockState(), 2);
        level.setBlock(origin.offset(4, 1, 9), Blocks.BREWING_STAND.defaultBlockState(), 2);
        level.setBlock(origin.offset(5, 1, 9), Blocks.ENCHANTING_TABLE.defaultBlockState(), 2);
        level.setBlock(origin.offset(6, 1, 10), Blocks.BOOKSHELF.defaultBlockState(), 2);
        level.setBlock(origin.offset(7, 1, 10), Blocks.BOOKSHELF.defaultBlockState(), 2);
        level.setBlock(origin.offset(8, 1, 10), Blocks.LECTERN.defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, Direction.NORTH), 2);

        BlockPos potions = origin.offset(10, 1, 2);
        BlockPos magic = origin.offset(10, 1, 5);
        BlockPos gear = origin.offset(10, 1, 8);
        placeChest(level, potions, Direction.WEST);
        placeChest(level, magic, Direction.WEST);
        placeChest(level, gear, Direction.WEST);
        fillCompactWitchChests(level, potions, magic, gear);

        for (BlockPos light : new BlockPos[] {
                origin.offset(3, height, 3), origin.offset(8, height, 3),
                origin.offset(3, height, 8), origin.offset(8, height, 8)
        }) {
            level.setBlock(light, Blocks.SOUL_LANTERN.defaultBlockState(), 2);
        }
        for (BlockPos web : new BlockPos[] {
                origin.offset(1, 4, 1), origin.offset(10, 4, 1),
                origin.offset(1, 4, 10), origin.offset(10, 4, 10)
        }) {
            level.setBlock(web, Blocks.COBWEB.defaultBlockState(), 2);
        }
    }

    private static void fillCompactWitchChests(ServerLevel level, BlockPos potions, BlockPos magic, BlockPos gear) {
        putItems(level, potions,
                potion(Potions.HEALING), potion(Potions.STRONG_HEALING), potion(Potions.REGENERATION),
                potion(Potions.STRONG_REGENERATION), potion(Potions.STRENGTH), potion(Potions.FIRE_RESISTANCE),
                potion(Potions.NIGHT_VISION), potion(Potions.WATER_BREATHING), potion(Potions.INVISIBILITY));
        putItems(level, magic,
                new ItemStack(MagicWorld.VARINHA_MAGICA.get()), new ItemStack(Items.TOTEM_OF_UNDYING, 8),
                new ItemStack(Items.EXPERIENCE_BOTTLE, 64), new ItemStack(Items.ENDER_PEARL, 32),
                new ItemStack(Items.BLAZE_POWDER, 64), new ItemStack(Items.GHAST_TEAR, 32),
                new ItemStack(Items.SPIDER_EYE, 32), new ItemStack(Items.FERMENTED_SPIDER_EYE, 32),
                new ItemStack(Items.AMETHYST_SHARD, 64), new ItemStack(Items.ECHO_SHARD, 16));
        putItems(level, gear,
                new ItemStack(MagicWorld.DRACONIC_AETHER_HELMET.get()),
                new ItemStack(MagicWorld.DRACONIC_AETHER_CHESTPLATE.get()),
                new ItemStack(MagicWorld.DRACONIC_AETHER_LEGGINGS.get()),
                new ItemStack(MagicWorld.DRACONIC_AETHER_BOOTS.get()),
                new ItemStack(Items.NETHERITE_SWORD), new ItemStack(Items.NETHERITE_AXE),
                new ItemStack(Items.BOW), new ItemStack(Items.CROSSBOW),
                new ItemStack(Items.SHIELD), new ItemStack(Items.ARROW, 64));
    }

    private static void buildCompactWitchFireplace(ServerLevel level, BlockPos fireplace, int height) {
        level.setBlock(fireplace, Blocks.CAMPFIRE.defaultBlockState(), 2);
        level.setBlock(fireplace.north(), Blocks.BRICKS.defaultBlockState(), 2);
        level.setBlock(fireplace.south(), Blocks.BRICKS.defaultBlockState(), 2);
        for (int y = 1; y <= height + 4; y++) {
            level.setBlock(fireplace.above(y), y % 2 == 0
                    ? Blocks.BRICKS.defaultBlockState()
                    : Blocks.CRACKED_STONE_BRICKS.defaultBlockState(), 2);
        }
        level.setBlock(fireplace.above(height + 5), Blocks.CAMPFIRE.defaultBlockState(), 2);
    }

    private static void decorateCompactWitchCoven(
            ServerLevel level,
            BlockPos origin,
            int houseSize,
            int doorZ,
            int fenceWest,
            int fenceEast,
            int fenceNorth,
            int fenceSouth
    ) {
        BlockPos door = origin.offset(houseSize - 1, 1, doorZ);
        level.setBlock(door.relative(Direction.EAST).below(), Blocks.MOSSY_COBBLESTONE.defaultBlockState(), 2);
        level.setBlock(door.relative(Direction.EAST, 2).below(), Blocks.COARSE_DIRT.defaultBlockState(), 2);
        for (BlockPos pos : new BlockPos[] {
                origin.offset(12, 1, 3), origin.offset(12, 1, 9),
                origin.offset(1, 1, 12), origin.offset(4, 1, 12)
        }) {
            level.setBlock(pos, Blocks.HAY_BLOCK.defaultBlockState(), 2);
        }
        for (BlockPos pos : new BlockPos[] {
                origin.offset(12, 1, 4), origin.offset(12, 1, 8),
                origin.offset(1, 1, -1), origin.offset(12, 1, 12)
        }) {
            level.setBlock(pos, Blocks.JACK_O_LANTERN.defaultBlockState(), 2);
        }
        for (BlockPos pos : new BlockPos[] {
                origin.offset(1, 1, -1), origin.offset(12, 1, -1),
                origin.offset(1, 1, 12), origin.offset(12, 1, 12)
        }) {
            placeWitchLanternPost(level, pos.below());
        }
        for (BlockPos pos : new BlockPos[] {
                origin.offset(2, 1, -1), origin.offset(9, 1, 12),
                origin.offset(12, 1, 2), origin.offset(12, 1, 10)
        }) {
            level.setBlock(pos, Blocks.POTTED_BROWN_MUSHROOM.defaultBlockState(), 2);
        }
        for (BlockPos pos : new BlockPos[] {
                origin.offset(fenceWest, 2, fenceNorth), origin.offset(fenceEast, 2, fenceNorth),
                origin.offset(fenceWest, 2, fenceSouth), origin.offset(fenceEast, 2, fenceSouth)
        }) {
            level.setBlock(pos, Blocks.COBWEB.defaultBlockState(), 2);
        }
    }

    private static void populateCompactWitchCoven(ServerLevel level, BlockPos origin, int houseSize) {
        spawnFriendlyWitch(level, origin.offset(4, 1, 7), "Bruxa Curandeira");
        spawnFriendlyWitch(level, origin.offset(7, 1, 7), "Bruxa Alquimista");
        spawnFriendlyWitch(level, origin.offset(9, 1, 9), "Bruxa da Mata");
        spawnNamed(level, EntityType.BAT, origin.offset(3, 4, 5), "Morcego da Chamine");
        spawnNamed(level, EntityType.BAT, origin.offset(8, 4, 8), "Morcego da Biblioteca");
    }

    private static void prepareWitchCovenClearing(
            ServerLevel level,
            BlockPos origin,
            int fenceWest,
            int fenceEast,
            int fenceNorth,
            int fenceSouth,
            int clearHeight
    ) {
        for (int x = fenceWest - 5; x <= fenceEast + 5; x++) {
            for (int z = fenceNorth - 5; z <= fenceSouth + 5; z++) {
                BlockPos floor = origin.offset(x, 0, z);
                for (int y = -8; y < 0; y++) {
                    level.setBlock(floor.offset(0, y, 0), Blocks.DIRT.defaultBlockState(), 2);
                }
                boolean insideFence = x >= fenceWest && x <= fenceEast && z >= fenceNorth && z <= fenceSouth;
                level.setBlock(floor, insideFence ? witchCovenYardBlock(x, z) : Blocks.GRASS_BLOCK.defaultBlockState(), 2);
                for (int y = 1; y <= clearHeight; y++) {
                    BlockPos clear = floor.above(y);
                    if (!level.getBlockState(clear).isAir()) {
                        if (level.getBlockEntity(clear) instanceof Container container) {
                            container.clearContent();
                        }
                        level.setBlock(clear, Blocks.AIR.defaultBlockState(), 2);
                    }
                }
            }
        }
    }

    private static BlockState witchCovenYardBlock(int x, int z) {
        if (Math.floorMod(x * 19 + z * 7, 11) == 0) {
            return Blocks.ROOTED_DIRT.defaultBlockState();
        }
        if (Math.floorMod(x + z, 5) == 0) {
            return Blocks.COARSE_DIRT.defaultBlockState();
        }
        return Blocks.PODZOL.defaultBlockState();
    }

    private static void buildWitchCovenFence(
            ServerLevel level,
            BlockPos origin,
            int fenceWest,
            int fenceEast,
            int fenceNorth,
            int fenceSouth,
            int doorZ
    ) {
        for (int x = fenceWest; x <= fenceEast; x++) {
            level.setBlock(origin.offset(x, 1, fenceNorth), Blocks.DARK_OAK_FENCE.defaultBlockState(), 2);
            level.setBlock(origin.offset(x, 1, fenceSouth), Blocks.DARK_OAK_FENCE.defaultBlockState(), 2);
        }
        for (int z = fenceNorth; z <= fenceSouth; z++) {
            level.setBlock(origin.offset(fenceWest, 1, z), Blocks.DARK_OAK_FENCE.defaultBlockState(), 2);
            if (z == doorZ) {
                level.setBlock(origin.offset(fenceEast, 1, z), Blocks.DARK_OAK_FENCE_GATE.defaultBlockState()
                        .setValue(HorizontalDirectionalBlock.FACING, Direction.EAST), 2);
            } else {
                level.setBlock(origin.offset(fenceEast, 1, z), Blocks.DARK_OAK_FENCE.defaultBlockState(), 2);
            }
        }

        BlockPos gate = origin.offset(fenceEast, 1, doorZ);
        for (int dx = 1; dx <= 7; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                BlockPos path = gate.below().offset(dx, 0, dz);
                level.setBlock(path, Math.abs(dz) <= 1 ? Blocks.COARSE_DIRT.defaultBlockState() : Blocks.PODZOL.defaultBlockState(), 2);
                for (int y = 1; y <= 5; y++) {
                    level.setBlock(path.above(y), Blocks.AIR.defaultBlockState(), 2);
                }
            }
        }
        placeWitchWarningSign(level, gate.north());
    }

    private static void buildWitchCovenShell(ServerLevel level, BlockPos origin, int width, int depth, int height, int doorZ) {
        for (int x = 0; x <= width; x++) {
            for (int z = 0; z <= depth; z++) {
                boolean edge = x == 0 || x == width || z == 0 || z == depth;
                boolean door = x == width && z == doorZ;
                BlockPos floor = origin.offset(x, 0, z);
                level.setBlock(floor.below(), Blocks.DIRT.defaultBlockState(), 2);
                level.setBlock(floor, witchCovenFloorBlock(x, z), 2);
                for (int y = 1; y <= height; y++) {
                    BlockPos pos = floor.above(y);
                    if (edge && !(door && y <= 2)) {
                        level.setBlock(pos, witchCovenWallBlock(x, y, z), 2);
                    } else {
                        level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
                    }
                }
            }
        }

        placeWitchDoor(level, origin.offset(width, 1, doorZ), Direction.EAST);
        for (int x = -1; x <= width + 1; x++) {
            for (int z = -1; z <= depth + 1; z++) {
                boolean roofEdge = x == -1 || x == width + 1 || z == -1 || z == depth + 1;
                level.setBlock(origin.offset(x, height + 1, z), roofEdge
                        ? Blocks.DARK_OAK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, roofFacingForEdge(x, z, width, depth))
                        : Blocks.DEEPSLATE_TILES.defaultBlockState(), 2);
                if (Math.floorMod(x * 13 + z * 17, 19) == 0) {
                    level.setBlock(origin.offset(x, height + 2, z), Blocks.MOSSY_COBBLESTONE.defaultBlockState(), 2);
                }
            }
        }
        for (int x = 4; x <= width - 4; x += 5) {
            level.setBlock(origin.offset(x, height + 2, depth / 2), Blocks.DARK_OAK_LOG.defaultBlockState(), 2);
            level.setBlock(origin.offset(x, height + 3, depth / 2), Blocks.DARK_OAK_FENCE.defaultBlockState(), 2);
        }
        buildWitchCovenChimney(level, origin.offset(5, 1, depth - 5), height);
        placeWitchWindows(level, origin, width, depth);
    }

    private static Direction roofFacingForEdge(int x, int z, int width, int depth) {
        if (z == -1) {
            return Direction.SOUTH;
        }
        if (z == depth + 1) {
            return Direction.NORTH;
        }
        if (x == -1) {
            return Direction.EAST;
        }
        return Direction.WEST;
    }

    private static BlockState witchCovenFloorBlock(int x, int z) {
        if (Math.floorMod(x * 3 + z * 5, 9) == 0) {
            return Blocks.MOSSY_COBBLESTONE.defaultBlockState();
        }
        if (Math.floorMod(x + z, 4) == 0) {
            return Blocks.DARK_OAK_PLANKS.defaultBlockState();
        }
        return Blocks.SPRUCE_PLANKS.defaultBlockState();
    }

    private static BlockState witchCovenWallBlock(int x, int y, int z) {
        if (y == 1 && Math.floorMod(x * 11 + z * 7, 6) == 0) {
            return Blocks.MOSSY_COBBLESTONE.defaultBlockState();
        }
        if (Math.floorMod(x + y + z, 9) == 0) {
            return Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState();
        }
        if (y >= 3 && Math.floorMod(x * 5 + z * 13, 17) == 0) {
            return Blocks.PURPLE_STAINED_GLASS.defaultBlockState();
        }
        return Blocks.DARK_OAK_PLANKS.defaultBlockState();
    }

    private static void placeWitchDoor(ServerLevel level, BlockPos lower, Direction facing) {
        level.setBlock(lower, Blocks.DARK_OAK_DOOR.defaultBlockState()
                .setValue(DoorBlock.FACING, facing)
                .setValue(DoorBlock.HALF, DoubleBlockHalf.LOWER), 2);
        level.setBlock(lower.above(), Blocks.DARK_OAK_DOOR.defaultBlockState()
                .setValue(DoorBlock.FACING, facing)
                .setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER), 2);
        level.setBlock(lower.relative(facing).below(), Blocks.MOSSY_COBBLESTONE.defaultBlockState(), 2);
    }

    private static void placeWitchWindows(ServerLevel level, BlockPos origin, int width, int depth) {
        for (int z : new int[] {4, 10, 16, 21}) {
            level.setBlock(origin.offset(0, 3, z), Blocks.PURPLE_STAINED_GLASS_PANE.defaultBlockState(), 2);
            level.setBlock(origin.offset(width, 3, z), Blocks.PURPLE_STAINED_GLASS_PANE.defaultBlockState(), 2);
        }
        for (int x : new int[] {5, 12, 19, 25}) {
            level.setBlock(origin.offset(x, 3, 0), Blocks.PURPLE_STAINED_GLASS_PANE.defaultBlockState(), 2);
            level.setBlock(origin.offset(x, 3, depth), Blocks.PURPLE_STAINED_GLASS_PANE.defaultBlockState(), 2);
        }
    }

    private static void buildWitchCovenChimney(ServerLevel level, BlockPos fireplace, int height) {
        level.setBlock(fireplace, Blocks.CAMPFIRE.defaultBlockState(), 2);
        level.setBlock(fireplace.north(), Blocks.BRICKS.defaultBlockState(), 2);
        level.setBlock(fireplace.south(), Blocks.BRICKS.defaultBlockState(), 2);
        level.setBlock(fireplace.east(), Blocks.IRON_BARS.defaultBlockState(), 2);
        level.setBlock(fireplace.west(), Blocks.IRON_BARS.defaultBlockState(), 2);
        for (int y = 1; y <= height + 5; y++) {
            BlockPos chimney = fireplace.above(y);
            level.setBlock(chimney, y % 2 == 0 ? Blocks.BRICKS.defaultBlockState() : Blocks.CRACKED_STONE_BRICKS.defaultBlockState(), 2);
            if (y <= 4) {
                level.setBlock(chimney.east(), Blocks.AIR.defaultBlockState(), 2);
                level.setBlock(chimney.west(), Blocks.AIR.defaultBlockState(), 2);
            }
        }
        level.setBlock(fireplace.above(height + 6), Blocks.CAMPFIRE.defaultBlockState(), 2);
    }

    private static void buildWitchCovenInterior(ServerLevel level, BlockPos origin, int width, int depth, int height) {
        for (int z = 1; z < depth; z++) {
            boolean bedroomDoor = z == 4 || z == 11 || z == 18;
            for (int y = 1; y <= 4; y++) {
                level.setBlock(origin.offset(10, y, z), bedroomDoor && y <= 2 ? Blocks.AIR.defaultBlockState() : Blocks.DARK_OAK_PLANKS.defaultBlockState(), 2);
            }
        }
        for (int z : new int[] {8, 15}) {
            for (int x = 1; x <= 10; x++) {
                for (int y = 1; y <= 4; y++) {
                    level.setBlock(origin.offset(x, y, z), x == 9 && y <= 2 ? Blocks.AIR.defaultBlockState() : Blocks.DARK_OAK_PLANKS.defaultBlockState(), 2);
                }
            }
        }

        buildWitchBedroom(level, origin.offset(2, 1, 3), Direction.EAST, origin.offset(7, 1, 2), "Bruxa Curandeira");
        buildWitchBedroom(level, origin.offset(2, 1, 10), Direction.EAST, origin.offset(7, 1, 9), "Bruxa Guardiã");
        buildWitchBedroom(level, origin.offset(2, 1, 17), Direction.EAST, origin.offset(7, 1, 16), "Bruxa Alquimista");

        BlockPos table = origin.offset(18, 1, depth / 2);
        for (int dx = -3; dx <= 3; dx++) {
            level.setBlock(table.offset(dx, 0, 0), Blocks.DARK_OAK_FENCE.defaultBlockState(), 2);
            level.setBlock(table.offset(dx, 1, 0), dx % 2 == 0 ? Blocks.PURPLE_CARPET.defaultBlockState() : Blocks.BLACK_CARPET.defaultBlockState(), 2);
        }
        for (int dx = -3; dx <= 3; dx += 2) {
            level.setBlock(table.offset(dx, 0, -2), Blocks.DARK_OAK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.SOUTH), 2);
            level.setBlock(table.offset(dx, 0, 2), Blocks.DARK_OAK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.NORTH), 2);
        }

        buildWitchAlchemyCorner(level, origin, width, depth);
        buildWitchStorage(level, origin, width, depth);

        for (BlockPos light : new BlockPos[] {
                origin.offset(14, height, 4), origin.offset(22, height, 6),
                origin.offset(14, height, 18), origin.offset(23, height, 20),
                origin.offset(5, height, 5), origin.offset(5, height, 12), origin.offset(5, height, 19)
        }) {
            level.setBlock(light, Blocks.SOUL_LANTERN.defaultBlockState(), 2);
        }
        for (BlockPos web : new BlockPos[] {
                origin.offset(1, 5, 1), origin.offset(width - 1, 5, 1),
                origin.offset(1, 5, depth - 1), origin.offset(width - 1, 5, depth - 1),
                origin.offset(11, 4, 8), origin.offset(11, 4, 15)
        }) {
            level.setBlock(web, Blocks.COBWEB.defaultBlockState(), 2);
        }
    }

    private static void buildWitchBedroom(ServerLevel level, BlockPos bedFoot, Direction bedFacing, BlockPos chest, String label) {
        placeBed(level, bedFoot, bedFacing);
        placeChest(level, chest, Direction.WEST);
        level.setBlock(bedFoot.offset(0, 0, 3), Blocks.BOOKSHELF.defaultBlockState(), 2);
        level.setBlock(bedFoot.offset(1, 0, 3), Blocks.POTTED_RED_MUSHROOM.defaultBlockState(), 2);
        level.setBlock(bedFoot.offset(3, 0, 3), Blocks.LANTERN.defaultBlockState(), 2);
        putItems(level, chest,
                potion(Potions.HEALING), potion(Potions.REGENERATION), potion(Potions.STRENGTH),
                new ItemStack(Items.BOOK, 16), new ItemStack(Items.PAPER, 32), new ItemStack(Items.NAME_TAG, 4),
                new ItemStack(Items.AMETHYST_SHARD, 32), new ItemStack(Items.GLOWSTONE_DUST, 32),
                new ItemStack(Items.EMERALD, 16), new ItemStack(MagicWorld.VARINHA_MAGICA.get()));
    }

    private static void buildWitchAlchemyCorner(ServerLevel level, BlockPos origin, int width, int depth) {
        BlockPos corner = origin.offset(width - 5, 1, depth - 6);
        level.setBlock(corner, Blocks.CAULDRON.defaultBlockState(), 2);
        level.setBlock(corner.north(), Blocks.BREWING_STAND.defaultBlockState(), 2);
        level.setBlock(corner.south(), Blocks.ENCHANTING_TABLE.defaultBlockState(), 2);
        for (int dx = -2; dx <= 2; dx++) {
            level.setBlock(corner.offset(dx, 0, -2), Blocks.BOOKSHELF.defaultBlockState(), 2);
            level.setBlock(corner.offset(dx, 1, -2), Blocks.PURPLE_STAINED_GLASS.defaultBlockState(), 2);
        }
        level.setBlock(corner.west(), Blocks.LECTERN.defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, Direction.EAST), 2);
        level.setBlock(corner.east(), Blocks.GRINDSTONE.defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, Direction.WEST), 2);
        level.setBlock(corner.above(3), Blocks.GLOWSTONE.defaultBlockState(), 2);
    }

    private static void buildWitchStorage(ServerLevel level, BlockPos origin, int width, int depth) {
        BlockPos potions = origin.offset(width - 4, 1, 3);
        BlockPos magic = origin.offset(width - 4, 1, 6);
        BlockPos armor = origin.offset(width - 4, 1, 9);
        BlockPos weapons = origin.offset(width - 4, 1, 12);
        placeChest(level, potions, Direction.WEST);
        placeChest(level, magic, Direction.WEST);
        placeChest(level, armor, Direction.WEST);
        placeChest(level, weapons, Direction.WEST);
        putItems(level, potions,
                potion(Potions.HEALING), potion(Potions.STRONG_HEALING), potion(Potions.REGENERATION),
                potion(Potions.STRONG_REGENERATION), potion(Potions.STRENGTH), potion(Potions.STRONG_STRENGTH),
                potion(Potions.SWIFTNESS), potion(Potions.STRONG_SWIFTNESS), potion(Potions.FIRE_RESISTANCE),
                potion(Potions.NIGHT_VISION), potion(Potions.WATER_BREATHING), potion(Potions.INVISIBILITY));
        putItems(level, magic,
                new ItemStack(MagicWorld.VARINHA_MAGICA.get()), new ItemStack(Items.TOTEM_OF_UNDYING, 8),
                new ItemStack(Items.ENCHANTED_BOOK, 16), new ItemStack(Items.EXPERIENCE_BOTTLE, 64),
                new ItemStack(Items.ENDER_PEARL, 32), new ItemStack(Items.BLAZE_ROD, 32),
                new ItemStack(Items.BLAZE_POWDER, 64), new ItemStack(Items.GHAST_TEAR, 32),
                new ItemStack(Items.SPIDER_EYE, 32), new ItemStack(Items.FERMENTED_SPIDER_EYE, 32),
                new ItemStack(Items.AMETHYST_SHARD, 64), new ItemStack(Items.ECHO_SHARD, 16));
        putItems(level, armor,
                new ItemStack(MagicWorld.DRACONIC_AETHER_HELMET.get()),
                new ItemStack(MagicWorld.DRACONIC_AETHER_CHESTPLATE.get()),
                new ItemStack(MagicWorld.DRACONIC_AETHER_LEGGINGS.get()),
                new ItemStack(MagicWorld.DRACONIC_AETHER_BOOTS.get()),
                new ItemStack(Items.NETHERITE_HELMET), new ItemStack(Items.NETHERITE_CHESTPLATE),
                new ItemStack(Items.NETHERITE_LEGGINGS), new ItemStack(Items.NETHERITE_BOOTS),
                new ItemStack(Items.DIAMOND_HELMET), new ItemStack(Items.DIAMOND_CHESTPLATE),
                new ItemStack(Items.DIAMOND_LEGGINGS), new ItemStack(Items.DIAMOND_BOOTS));
        putItems(level, weapons,
                new ItemStack(Items.NETHERITE_SWORD), new ItemStack(Items.NETHERITE_AXE),
                new ItemStack(Items.DIAMOND_SWORD), new ItemStack(Items.BOW),
                new ItemStack(Items.CROSSBOW), new ItemStack(Items.TRIDENT),
                new ItemStack(Items.SHIELD), new ItemStack(Items.ARROW, 64),
                new ItemStack(Items.SPECTRAL_ARROW, 64), new ItemStack(Items.FIREWORK_ROCKET, 64));
    }

    private static void decorateWitchCovenExterior(
            ServerLevel level,
            BlockPos origin,
            int width,
            int depth,
            int doorZ,
            int fenceWest,
            int fenceEast,
            int fenceNorth,
            int fenceSouth
    ) {
        BlockPos door = origin.offset(width, 1, doorZ);
        for (int dx = 1; dx <= 9; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                BlockPos path = door.below().offset(dx, 0, dz);
                level.setBlock(path, Math.abs(dz) == 0 ? Blocks.MOSSY_COBBLESTONE.defaultBlockState() : Blocks.COARSE_DIRT.defaultBlockState(), 2);
                for (int y = 1; y <= 5; y++) {
                    level.setBlock(path.above(y), Blocks.AIR.defaultBlockState(), 2);
                }
            }
        }

        for (BlockPos hay : new BlockPos[] {
                origin.offset(width + 2, 0, doorZ - 4), origin.offset(width + 3, 0, doorZ - 4),
                origin.offset(width + 2, 1, doorZ - 4), origin.offset(width + 3, 0, doorZ + 4),
                origin.offset(width + 4, 0, doorZ + 4)
        }) {
            level.setBlock(hay, Blocks.HAY_BLOCK.defaultBlockState(), 2);
        }
        for (BlockPos cauldron : new BlockPos[] {
                origin.offset(width + 4, 1, doorZ - 2), origin.offset(width + 4, 1, doorZ + 2),
                origin.offset(3, 1, -3)
        }) {
            level.setBlock(cauldron, Blocks.CAULDRON.defaultBlockState(), 2);
        }
        for (BlockPos pumpkin : new BlockPos[] {
                origin.offset(width + 5, 1, doorZ - 5), origin.offset(width + 5, 1, doorZ + 5),
                origin.offset(2, 1, depth + 3), origin.offset(8, 1, depth + 4)
        }) {
            level.setBlock(pumpkin, Blocks.JACK_O_LANTERN.defaultBlockState(), 2);
        }
        for (BlockPos decor : new BlockPos[] {
                origin.offset(width + 2, 1, doorZ - 6), origin.offset(width + 2, 1, doorZ + 6),
                origin.offset(4, 1, depth + 2), origin.offset(10, 1, -2)
        }) {
            level.setBlock(decor, Blocks.POTTED_BROWN_MUSHROOM.defaultBlockState(), 2);
        }

        placeWitchLanternPost(level, origin.offset(width + 3, 0, doorZ - 3));
        placeWitchLanternPost(level, origin.offset(width + 3, 0, doorZ + 3));
        placeWitchLanternPost(level, origin.offset(2, 0, 2));
        placeWitchLanternPost(level, origin.offset(2, 0, depth - 2));

        for (BlockPos web : new BlockPos[] {
                origin.offset(fenceWest, 2, fenceNorth), origin.offset(fenceWest, 2, fenceSouth),
                origin.offset(fenceEast, 2, fenceNorth), origin.offset(fenceEast, 2, fenceSouth),
                origin.offset(width + 1, 3, doorZ - 2), origin.offset(width + 1, 3, doorZ + 2)
        }) {
            level.setBlock(web, Blocks.COBWEB.defaultBlockState(), 2);
        }

        buildWitchForestRing(level, origin, fenceWest, fenceEast, fenceNorth, fenceSouth, doorZ);
    }

    private static void buildWitchForestRing(
            ServerLevel level,
            BlockPos origin,
            int fenceWest,
            int fenceEast,
            int fenceNorth,
            int fenceSouth,
            int doorZ
    ) {
        for (int x = fenceWest - 8; x <= fenceEast + 8; x += 6) {
            placeWitchCovenTree(level, origin.offset(x, 0, fenceNorth - 7), Math.floorMod(x, 3));
            placeWitchCovenTree(level, origin.offset(x + 2, 0, fenceSouth + 7), Math.floorMod(x + 1, 3));
        }
        for (int z = fenceNorth - 4; z <= fenceSouth + 4; z += 6) {
            placeWitchCovenTree(level, origin.offset(fenceWest - 7, 0, z), Math.floorMod(z, 3));
            if (Math.abs(z - doorZ) > 8) {
                placeWitchCovenTree(level, origin.offset(fenceEast + 7, 0, z), Math.floorMod(z + 2, 3));
            }
        }
    }

    private static void placeWitchCovenTree(ServerLevel level, BlockPos ground, int variant) {
        int trunkHeight = 5 + Math.floorMod(ground.getX() + ground.getZ() + variant, 3);
        BlockState log = variant == 0 ? Blocks.DARK_OAK_LOG.defaultBlockState() : Blocks.SPRUCE_LOG.defaultBlockState();
        BlockState leaves = variant == 1 ? Blocks.SPRUCE_LEAVES.defaultBlockState() : Blocks.DARK_OAK_LEAVES.defaultBlockState();
        for (int y = 1; y <= trunkHeight; y++) {
            level.setBlock(ground.above(y), log, 2);
        }
        BlockPos crown = ground.above(trunkHeight);
        for (int dx = -3; dx <= 3; dx++) {
            for (int dz = -3; dz <= 3; dz++) {
                for (int dy = -1; dy <= 2; dy++) {
                    if (Math.abs(dx) + Math.abs(dz) + Math.max(0, dy) <= 5) {
                        level.setBlock(crown.offset(dx, dy, dz), leaves, 2);
                    }
                }
            }
        }
        if (Math.floorMod(ground.getX() * 31 + ground.getZ(), 4) == 0) {
            level.setBlock(ground.above(2).east(), Blocks.COBWEB.defaultBlockState(), 2);
        }
    }

    private static void placeWitchLanternPost(ServerLevel level, BlockPos ground) {
        level.setBlock(ground, Blocks.MOSSY_COBBLESTONE.defaultBlockState(), 2);
        for (int y = 1; y <= 3; y++) {
            level.setBlock(ground.above(y), Blocks.DARK_OAK_FENCE.defaultBlockState(), 2);
        }
        level.setBlock(ground.above(4), Blocks.SOUL_LANTERN.defaultBlockState(), 2);
        level.setBlock(ground.above(5), Blocks.PURPLE_STAINED_GLASS.defaultBlockState(), 2);
    }

    private static void placeWitchWarningSign(ServerLevel level, BlockPos leftOfGate) {
        BlockPos sign = leftOfGate.above();
        level.setBlock(sign.west(), Blocks.DARK_OAK_LOG.defaultBlockState(), 2);
        level.setBlock(sign, Blocks.DARK_OAK_WALL_SIGN.defaultBlockState()
                .setValue(WallSignBlock.FACING, Direction.EAST), 2);
        if (level.getBlockEntity(sign) instanceof SignBlockEntity signEntity) {
            SignText text = signEntity.getFrontText()
                    .setMessage(0, Component.literal("fiquem longe"))
                    .setMessage(1, Component.literal("daqui"));
            signEntity.setText(text, true);
            signEntity.setChanged();
        }
    }

    private static void populateWitchCoven(ServerLevel level, BlockPos origin, int width, int depth) {
        spawnFriendlyWitch(level, origin.offset(16, 1, depth / 2), "Bruxa Curandeira");
        spawnFriendlyWitch(level, origin.offset(23, 1, depth - 7), "Bruxa Alquimista");
        spawnFriendlyWitch(level, origin.offset(width + 7, 1, depth / 2 - 8), "Bruxa da Mata");
        spawnNamed(level, EntityType.BAT, origin.offset(14, 5, 5), "Morcego da Chamine");
        spawnNamed(level, EntityType.BAT, origin.offset(21, 5, depth - 4), "Morcego da Biblioteca");
        spawnNamed(level, EntityType.BAT, origin.offset(width + 5, 5, depth / 2 + 5), "Morcego da Mata");
    }

    private static void spawnFriendlyWitch(ServerLevel level, BlockPos pos, String name) {
        AABB nearby = new AABB(pos).inflate(128.0D, 64.0D, 128.0D);
        if (!level.getEntitiesOfClass(Witch.class, nearby, witch -> name.equals(witch.getName().getString())).isEmpty()) {
            return;
        }

        Witch witch = EntityType.WITCH.spawn(level, pos, MobSpawnType.STRUCTURE);
        if (witch == null) {
            return;
        }

        witch.getPersistentData().putBoolean(FRIENDLY_WITCH_KEY, true);
        witch.setCustomName(Component.literal(name));
        witch.setCustomNameVisible(true);
        witch.setPersistenceRequired();
        witch.setNoAi(true);
        witch.setInvulnerable(true);
        witch.setTarget(null);
        witch.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 20 * 60, 1, true, false));
        witch.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 20 * 60, 4, true, false));
        witch.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 20 * 60, 0, true, false));
    }

    private static ItemStack potion(Potion potion) {
        return PotionUtils.setPotion(new ItemStack(Items.POTION), potion);
    }

    private static void buildImportedCastle(ServerLevel level, BlockPos origin) {
        Optional<StructureTemplate> optional = level.getStructureManager().get(IMPORTED_CASTLE);
        if (optional.isPresent()) {
            StructureTemplate template = optional.get();
            clearStructureVolume(level, origin, template.getSize(), BREATHING_MARGIN, true);
            template.placeInWorld(level, origin, origin, new StructurePlaceSettings(), RandomSource.create(level.getSeed() + 1L), 2);
            prepareBreathingSurface(level, origin, template.getSize(), BREATHING_MARGIN);
        } else {
            buildFallbackCastle(level, origin.offset(CASTLE_SIZE_X / 2, 0, CASTLE_SIZE_Z / 2));
        }
    }

    private static void clearStructureVolume(ServerLevel level, BlockPos origin, Vec3i size, int margin, boolean clearToSky) {
        int minY = Math.max(level.getMinBuildHeight(), origin.getY());
        int maxY = clearToSky ? level.getMaxBuildHeight() - 1 : Math.min(level.getMaxBuildHeight() - 1, origin.getY() + size.getY() + margin);
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();

        for (int x = -margin; x < size.getX() + margin; x++) {
            for (int z = -margin; z < size.getZ() + margin; z++) {
                BlockPos floor = origin.offset(x, -1, z);
                level.setBlock(floor.below(), Blocks.DIRT.defaultBlockState(), 2);
                level.setBlock(floor, Blocks.GRASS_BLOCK.defaultBlockState(), 2);

                for (int y = minY; y <= maxY; y++) {
                    mutable.set(origin.getX() + x, y, origin.getZ() + z);
                    if (!level.getBlockState(mutable).isAir()) {
                        if (level.getBlockEntity(mutable) instanceof Container container) {
                            container.clearContent();
                        }
                        level.setBlock(mutable, Blocks.AIR.defaultBlockState(), 2);
                    }
                }
            }
        }
    }

    private static void forceLoadStructureArea(ServerLevel level, BlockPos origin, int width, int depth, int margin) {
        forceLoadAreaBetween(
                level,
                origin.offset(-margin, 0, -margin),
                origin.offset(width + margin, 0, depth + margin)
        );
    }

    private static void forceLoadAreaBetween(ServerLevel level, BlockPos first, BlockPos second) {
        int minChunkX = Math.floorDiv(Math.min(first.getX(), second.getX()), 16);
        int maxChunkX = Math.floorDiv(Math.max(first.getX(), second.getX()), 16);
        int minChunkZ = Math.floorDiv(Math.min(first.getZ(), second.getZ()), 16);
        int maxChunkZ = Math.floorDiv(Math.max(first.getZ(), second.getZ()), 16);

        for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
            for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
                level.getChunk(chunkX, chunkZ);
            }
        }
    }

    private static void prepareBreathingSurface(ServerLevel level, BlockPos origin, Vec3i size, int margin) {
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        for (int x = -margin; x < size.getX() + margin; x++) {
            for (int z = -margin; z < size.getZ() + margin; z++) {
                if (x >= 0 && x < size.getX() && z >= 0 && z < size.getZ()) {
                    continue;
                }

                mutable.set(origin.getX() + x, origin.getY() - 1, origin.getZ() + z);
                if (canPatchHousePerimeterGround(level.getBlockState(mutable))) {
                    level.setBlock(mutable, Blocks.GRASS_BLOCK.defaultBlockState(), 2);
                }
                for (int y = 0; y <= 18; y++) {
                    mutable.set(origin.getX() + x, origin.getY() + y, origin.getZ() + z);
                    if (isBreathingSurfaceCleanupBlock(level.getBlockState(mutable))) {
                        level.setBlock(mutable, Blocks.AIR.defaultBlockState(), 2);
                    }
                }
            }
        }
    }

    private static boolean isBreathingSurfaceCleanupBlock(BlockState state) {
        return state.isAir()
                || state.getFluidState().isSource()
                || canPatchHousePerimeterGround(state)
                || state.is(Blocks.GRASS)
                || state.is(Blocks.TALL_GRASS)
                || state.is(Blocks.FERN)
                || state.is(Blocks.LARGE_FERN)
                || state.is(Blocks.VINE)
                || state.is(Blocks.POPPY)
                || state.is(Blocks.DANDELION)
                || state.is(Blocks.BLUE_ORCHID)
                || state.is(Blocks.ALLIUM)
                || state.is(Blocks.AZURE_BLUET)
                || state.is(Blocks.RED_TULIP)
                || state.is(Blocks.OXEYE_DAISY)
                || state.is(Blocks.CORNFLOWER);
    }

    private static void buildImportedEstateFarms(ServerLevel level, BlockPos base) {
        buildCropField(level, base.offset(-122, -1, -42), Blocks.WHEAT.defaultBlockState().setValue(CropBlock.AGE, 7));
        buildCropField(level, base.offset(-102, -1, -42), Blocks.CARROTS.defaultBlockState().setValue(CropBlock.AGE, 7));
        buildCropField(level, base.offset(-122, -1, -22), Blocks.POTATOES.defaultBlockState().setValue(CropBlock.AGE, 7));
        buildCropField(level, base.offset(-102, -1, -22), Blocks.BEETROOTS.defaultBlockState().setValue(BeetrootBlock.AGE, 3));

        buildAnimalPen(level, base.offset(46, -1, -60), 14, 12);
        buildAnimalPen(level, base.offset(62, -1, -60), 14, 12);
        buildAnimalPen(level, base.offset(78, -1, -60), 14, 12);
        buildAnimalPen(level, base.offset(46, -1, -42), 14, 12);
        buildAnimalPen(level, base.offset(62, -1, -42), 14, 12);
        buildAnimalPen(level, base.offset(78, -1, -42), 14, 12);
        furnishAnimalPens(level, base);
        buildAnimalFeedGarden(level, base.offset(78, -1, -10), 22, 12);
        buildPlantationWorkerSettlement(level, base);
        buildAnimalCaretakerSettlement(level, base);
        buildStarterEstateRoads(level, base);
        stabilizeEstateOpenGapTerrain(level, base);
        stabilizeEstateAirGaps(level, base);
        stabilizeGreenVillageDistrictTerrain(level, base);
        buildGreenVillageSquare(level, base);
        buildEstateLivingBorder(level, base, IMPORTED_ESTATE_FENCE_MIN_X, IMPORTED_ESTATE_FENCE_MAX_X,
                IMPORTED_ESTATE_FENCE_MIN_Z, IMPORTED_ESTATE_FENCE_MAX_Z);
        buildEnhancedEstateLighting(level, base);

        for (BlockPos pos : new BlockPos[] {
                base.offset(-126, 0, -72), base.offset(120, 0, -72),
                base.offset(-126, 0, 78), base.offset(120, 0, 78),
                base.offset(-110, 0, -12), base.offset(96, 0, -12),
                base.offset(-92, 0, -48), base.offset(96, 0, -48),
                base.offset(-32, 0, 72), base.offset(18, 0, 72)
        }) {
            placeLampPost(level, pos);
        }

        removeLooseDroppedItemsAroundImportedHouse(level, base);
        restoreImportedHouseTemplate(level, base);
        finishImportedHouseContents(level, base);
        removeLooseDroppedItemsAroundImportedHouse(level, base);
    }

    private static void buildCropField(ServerLevel level, BlockPos corner, BlockState cropState) {
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                BlockPos pos = corner.offset(x, 0, z);
                level.setBlock(pos.below(), Blocks.DIRT.defaultBlockState(), 2);
                for (int y = 1; y <= 5; y++) {
                    level.setBlock(pos.above(y), Blocks.AIR.defaultBlockState(), 2);
                }
                if (x == 7 || z == 7) {
                    level.setBlock(pos, Blocks.WATER.defaultBlockState(), 2);
                } else {
                    level.setBlock(pos, Blocks.FARMLAND.defaultBlockState(), 2);
                    level.setBlock(pos.above(), cropState, 2);
                }
            }
        }
        placeChest(level, corner.offset(15, 1, 15), Direction.WEST);
        putItems(level, corner.offset(15, 1, 15),
                new ItemStack(Items.WHEAT, 64),
                new ItemStack(Items.CARROT, 64),
                new ItemStack(Items.POTATO, 64),
                new ItemStack(Items.BEETROOT, 64),
                new ItemStack(Items.BONE_MEAL, 64));
    }

    private static void buildAnimalPen(ServerLevel level, BlockPos corner, int width, int depth) {
        for (int x = 0; x <= width; x++) {
            for (int z = 0; z <= depth; z++) {
                BlockPos pos = corner.offset(x, 0, z);
                boolean edge = x == 0 || x == width || z == 0 || z == depth;
                level.setBlock(pos.below(), Blocks.GRASS_BLOCK.defaultBlockState(), 2);
                level.setBlock(pos, Blocks.GRASS_BLOCK.defaultBlockState(), 2);
                for (int y = 1; y <= 8; y++) {
                    level.setBlock(pos.above(y), Blocks.AIR.defaultBlockState(), 2);
                }
                if (edge) {
                    level.setBlock(pos.above(), Blocks.OAK_FENCE.defaultBlockState(), 2);
                }
            }
        }
        placeAnimalPenGates(level, corner, width, depth);
        placeLampPost(level, corner.offset(width / 2, 0, depth / 2));
    }

    private static void placeAnimalPenGates(ServerLevel level, BlockPos corner, int width, int depth) {
        level.setBlock(corner.offset(width / 2, 1, 0), Blocks.OAK_FENCE_GATE.defaultBlockState()
                .setValue(HorizontalDirectionalBlock.FACING, Direction.NORTH), 2);
    }

    private static void restoreAnimalPens(ServerLevel level, BlockPos base) {
        BlockPos[] corners = animalPenCorners(base);
        for (BlockPos corner : corners) {
            repairAnimalPenBoundary(level, corner, 14, 12);
        }
        furnishAnimalPens(level, base);
        ensureAnimalPenPopulations(level, base);
        assignAnimalCaretakersToPens(level, base);
    }

    private static void repairAnimalPenBoundary(ServerLevel level, BlockPos corner, int width, int depth) {
        for (int x = 0; x <= width; x++) {
            for (int z = 0; z <= depth; z++) {
                if (x != 0 && x != width && z != 0 && z != depth) {
                    continue;
                }
                BlockPos fence = corner.offset(x, 1, z);
                boolean entrance = x == width / 2 && z == 0;
                level.setBlock(fence, entrance
                        ? Blocks.OAK_FENCE_GATE.defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, Direction.NORTH)
                        : Blocks.OAK_FENCE.defaultBlockState(), 2);
            }
        }
    }

    private static void furnishAnimalPens(ServerLevel level, BlockPos base) {
        Item[] foods = {
                Items.WHEAT, Items.CARROT, Items.WHEAT,
                Items.WHEAT_SEEDS, Items.GOLDEN_CARROT, Items.WHEAT
        };
        for (int i = 0; i < animalPenCorners(base).length; i++) {
            BlockPos corner = animalPenCorners(base)[i];
            level.setBlock(corner.offset(2, 1, 2), Blocks.WATER_CAULDRON.defaultBlockState(), 2);
            level.setBlock(corner.offset(3, 1, 2), Blocks.HAY_BLOCK.defaultBlockState(), 2);
            level.setBlock(corner.offset(4, 1, 2), Blocks.COMPOSTER.defaultBlockState(), 2);
            BlockPos feedChest = corner.offset(11, 1, 2);
            placeChest(level, feedChest, Direction.SOUTH);
            putItems(level, feedChest, new ItemStack(foods[i], 64), new ItemStack(foods[i], 64), new ItemStack(Items.HAY_BLOCK, 32));
        }
    }

    private static BlockPos[] animalPenCorners(BlockPos base) {
        return new BlockPos[] {
                base.offset(46, -1, -60), base.offset(62, -1, -60), base.offset(78, -1, -60),
                base.offset(46, -1, -42), base.offset(62, -1, -42), base.offset(78, -1, -42)
        };
    }

    private static void buildAnimalFeedGarden(ServerLevel level, BlockPos corner, int width, int depth) {
        for (int x = 0; x < width; x++) {
            for (int z = 0; z < depth; z++) {
                BlockPos pos = corner.offset(x, 0, z);
                level.setBlock(pos.below(), Blocks.DIRT.defaultBlockState(), 2);
                if (x == width / 2) {
                    level.setBlock(pos, Blocks.WATER.defaultBlockState(), 2);
                } else {
                    level.setBlock(pos, Blocks.FARMLAND.defaultBlockState(), 2);
                    BlockState crop = z % 3 == 0
                            ? Blocks.WHEAT.defaultBlockState().setValue(CropBlock.AGE, 7)
                            : z % 3 == 1
                            ? Blocks.CARROTS.defaultBlockState().setValue(CropBlock.AGE, 7)
                            : Blocks.POTATOES.defaultBlockState().setValue(CropBlock.AGE, 7);
                    level.setBlock(pos.above(), crop, 2);
                }
            }
        }
        level.setBlock(corner.offset(width / 2, 1, 0), Blocks.COMPOSTER.defaultBlockState(), 2);
    }

    private static void buildPlantationWorkerSettlement(ServerLevel level, BlockPos base) {
        BlockPos[] houses = {
                base.offset(-126, -1, -56),
                base.offset(-102, -1, -56)
        };
        BlockPos warehouse = base.offset(-119, -1, -76);
        BlockPos warehouseCenter = warehouse.offset(11, 1, 8);
        BlockPos[] farmCenters = {
                base.offset(-114, 0, -34),
                base.offset(-94, 0, -14)
        };

        buildPlantationWorkerWarehouse(level, warehouse);
        for (int i = 0; i < houses.length; i++) {
            buildSimplePlantationWorkerHouse(level, houses[i], Direction.SOUTH, Direction.NORTH);
            decorateWorkerHome(level, houses[i], 14, 10, "Casa da Plantacao " + (i + 1));
            buildHousePathToFarm(level, plantationWorkerHouseDoorPos(houses[i], Direction.SOUTH), Direction.SOUTH, 8);
            buildHousePathToFarm(level, plantationWorkerHouseDoorPos(houses[i], Direction.NORTH), Direction.NORTH, 10);
            int beds = countBedsInArea(level, houses[i], 14, 7, 10);
            for (int villager = 0; villager < beds; villager++) {
                spawnWorkerVillager(level, houses[i].offset(2 + (villager % 3) * 3, 1, villager % 2 == 0 ? 7 : 3),
                        "Agricultor da Plantacao Casa " + (i + 1) + "-" + (villager + 1),
                        warehouseCenter, farmCenters[i], Items.WHEAT);
            }
        }

        int warehouseBeds = countBedsInArea(level, warehouse, 22, 9, 16);
        for (int i = 0; i < warehouseBeds; i++) {
            spawnWorkerVillager(level, warehouseCenter.offset(-3 + i * 2, 0, i % 2 == 0 ? 2 : -2),
                    "Morador do Rancho da Plantacao " + (i + 1), warehouseCenter, farmCenters[i % farmCenters.length], Items.CARROT);
        }

        for (BlockPos center : farmCenters) {
            level.setBlock(center, Blocks.COMPOSTER.defaultBlockState(), 2);
            placeLampPost(level, center.offset(0, -1, -11));
        }
        spawnNamed(level, EntityType.IRON_GOLEM, warehouseCenter.offset(0, 0, 4), "Guardiao da Plantacao");
    }

    private static void buildAnimalCaretakerSettlement(ServerLevel level, BlockPos base) {
        BlockPos[] houses = {
                base.offset(106, 0, -72),
                base.offset(106, 0, -56)
        };
        BlockPos foodFarm = base.offset(92, -1, -52);
        BlockPos farmCenter = foodFarm.offset(1, 0, 16);

        for (int i = 0; i < houses.length; i++) {
            buildCaretakerHouse(level, houses[i], Direction.WEST);
            decorateWorkerHome(level, houses[i], 12, 8, "Casa dos Animais " + (i + 1));
            buildHousePathToFarm(level, houseDoorPos(houses[i], Direction.WEST), Direction.WEST, 8);
            int beds = countBedsInArea(level, houses[i], 12, 6, 8);
            for (int villager = 0; villager < beds; villager++) {
                spawnWorkerVillager(level, houses[i].offset(4, 1, 3 + villager * 2),
                        "Cuidador da Fazenda Casa " + (i + 1) + "-" + (villager + 1),
                        houses[i].offset(5, 1, 4), farmCenter, Items.WHEAT);
            }
        }

        buildAnimalFoodStripFarm(level, foodFarm, 4, 32);
        spawnNamed(level, EntityType.IRON_GOLEM, farmCenter.offset(0, 1, 0), "Guardiao dos Animais");
        assignAnimalCaretakersToPens(level, base);
        buildPremiumAnimalWorkCenter(level, base);
        spawnEstateGuardianVillagers(level, base);
    }

    private static BlockPos premiumAnimalWorkCenterCorner(BlockPos base) {
        return base.offset(106, 0, -72);
    }

    private static void buildPremiumAnimalWorkCenter(ServerLevel level, BlockPos base) {
        BlockPos corner = premiumAnimalWorkCenterCorner(base);
        int width = 18;
        int depth = 14;
        int wallHeight = 6;
        int eastDoorZ = depth / 2;

        for (int x = 0; x <= width; x++) {
            for (int z = 0; z <= depth; z++) {
                BlockPos floor = corner.offset(x, 0, z);
                boolean wall = x == 0 || x == width || z == 0 || z == depth;
                boolean post = (x == 0 || x == width) && (z == 0 || z == depth);
                level.setBlock(floor.below(), Blocks.COBBLESTONE.defaultBlockState(), 2);
                level.setBlock(floor, Math.floorMod(x + z, 4) == 0
                        ? Blocks.POLISHED_ANDESITE.defaultBlockState()
                        : Blocks.SPRUCE_PLANKS.defaultBlockState(), 2);
                for (int y = 1; y <= wallHeight; y++) {
                    BlockPos pos = floor.above(y);
                    BlockState state = wall
                            ? premiumWorkCenterWallBlock(post, x, y, z)
                            : Blocks.AIR.defaultBlockState();
                    level.setBlock(pos, state, 2);
                }
            }
        }

        for (int x = -1; x <= width + 1; x++) {
            for (int z = -1; z <= depth + 1; z++) {
                level.setBlock(corner.offset(x, wallHeight + 1, z), Blocks.DARK_OAK_PLANKS.defaultBlockState(), 2);
            }
        }
        buildDecorativeGabledRoof(level, corner, width, depth, wallHeight + 2);

        placeHouseDoor(level, corner.offset(width, 1, eastDoorZ), Direction.EAST);
        placeHouseDoor(level, corner.offset(0, 1, depth / 2 - 3), Direction.WEST);
        placeHouseDoor(level, corner.offset(0, 1, depth / 2 + 3), Direction.WEST);
        buildHousePathToFarm(level, corner.offset(0, 1, depth / 2 - 3), Direction.WEST, 8);
        buildHousePathToFarm(level, corner.offset(0, 1, depth / 2 + 3), Direction.WEST, 8);
        decorateWorkerDoors(level, corner, width, depth);
        level.setBlock(corner.offset(width, 3, eastDoorZ), Blocks.CHISELED_STONE_BRICKS.defaultBlockState(), 2);
        level.setBlock(corner.offset(width, 4, eastDoorZ), Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState(), 2);

        placePremiumWorkCenterWindows(level, corner, width, depth);
        furnishPremiumAnimalWorkCenter(level, corner, width, depth);
        lightPremiumAnimalWorkCenter(level, corner, width, depth);
        clearPremiumAnimalWorkCenterEntrances(level, corner, width, depth);
        populatePremiumAnimalWorkCenter(level, corner, base);
    }

    private static BlockState premiumWorkCenterWallBlock(boolean post, int x, int y, int z) {
        if (post) {
            return Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState();
        }
        if (y == 1 && Math.floorMod(x * 13 + z * 7, 5) == 0) {
            return Blocks.CHISELED_STONE_BRICKS.defaultBlockState();
        }
        if (y >= 4 && Math.floorMod(x + z, 3) == 0) {
            return Blocks.MOSSY_STONE_BRICKS.defaultBlockState();
        }
        return Blocks.STONE_BRICKS.defaultBlockState();
    }

    private static void placePremiumWorkCenterWindows(ServerLevel level, BlockPos corner, int width, int depth) {
        for (int x : new int[] {3, 6, 12, 15}) {
            placeWindow(level, corner.offset(x, 2, 0));
            placeWindow(level, corner.offset(x, 2, depth));
        }
        for (int z : new int[] {3, 5, 9, 11}) {
            placeWindow(level, corner.offset(0, 2, z));
            placeWindow(level, corner.offset(width, 2, z));
        }
        for (BlockPos trim : new BlockPos[] {
                corner.offset(width, 5, depth / 2 - 2),
                corner.offset(width, 5, depth / 2 + 2),
                corner.offset(0, 5, depth / 2 - 2),
                corner.offset(0, 5, depth / 2 + 2)
        }) {
            level.setBlock(trim, Blocks.LIGHT_BLUE_STAINED_GLASS.defaultBlockState(), 2);
        }
    }

    private static void furnishPremiumAnimalWorkCenter(ServerLevel level, BlockPos corner, int width, int depth) {
        placePremiumStorageWall(level, corner, width, depth);
        placePremiumWorkCenterStations(level, corner, width, depth);
        placePremiumWorkCenterTable(level, corner, width, depth);
        placePremiumWorkCenterBedsAndArmor(level, corner, width, depth);
        decoratePremiumWorkCenterExterior(level, corner, width, depth);
    }

    private static void placePremiumStorageWall(ServerLevel level, BlockPos corner, int width, int depth) {
        List<BlockPos> itemCatalogContainers = new ArrayList<>();
        for (int z = 1; z < depth; z++) {
            if (z >= depth / 2 - 1 && z <= depth / 2 + 1) {
                continue;
            }
            BlockPos chest = corner.offset(width - 1, 1, z);
            placeStorageChest(level, chest, Direction.WEST, Math.floorMod(z, 2) == 0);
            itemCatalogContainers.add(chest);
            for (int y : new int[] {3, 4, 5}) {
                BlockPos barrel = corner.offset(width - 1, y, z);
                placeStorageBarrel(level, barrel);
                itemCatalogContainers.add(barrel);
            }
        }

        for (int x = 2; x <= width - 3; x += 3) {
            BlockPos north = corner.offset(x, 1, 1);
            BlockPos south = corner.offset(x, 1, depth - 1);
            placeStorageBarrel(level, north);
            placeStorageBarrel(level, south);
            itemCatalogContainers.add(north);
            itemCatalogContainers.add(south);
        }

        fillContainersWithAllRegisteredItems(level, itemCatalogContainers);

        BlockPos wandChest = corner.offset(2, 1, depth - 2);
        placeStorageChest(level, wandChest, Direction.NORTH, false);
        fillContainerWithItem(level, wandChest, MagicWorld.VARINHA_MAGICA.get());

        BlockPos premiumChest = corner.offset(4, 1, depth - 2);
        placeStorageChest(level, premiumChest, Direction.NORTH, true);
        putItems(level, premiumChest,
                new ItemStack(Items.ELYTRA), new ItemStack(Items.DRAGON_EGG),
                new ItemStack(Items.NETHER_STAR, 16), new ItemStack(Items.BEACON, 8),
                new ItemStack(Items.ENCHANTED_GOLDEN_APPLE, 32), new ItemStack(Items.TOTEM_OF_UNDYING, 16),
                new ItemStack(Items.TRIDENT), new ItemStack(Items.HEART_OF_THE_SEA, 16),
                new ItemStack(Items.CONDUIT, 8), new ItemStack(Items.NETHERITE_INGOT, 64),
                new ItemStack(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE, 16),
                new ItemStack(MagicWorld.DRACONIC_AETHER_HELMET.get()),
                new ItemStack(MagicWorld.DRACONIC_AETHER_CHESTPLATE.get()),
                new ItemStack(MagicWorld.DRACONIC_AETHER_LEGGINGS.get()),
                new ItemStack(MagicWorld.DRACONIC_AETHER_BOOTS.get()));
    }

    private static void placePremiumWorkCenterStations(ServerLevel level, BlockPos corner, int width, int depth) {
        level.setBlock(corner.offset(2, 1, 2), Blocks.CRAFTING_TABLE.defaultBlockState(), 2);
        level.setBlock(corner.offset(3, 1, 2), Blocks.SMITHING_TABLE.defaultBlockState(), 2);
        level.setBlock(corner.offset(4, 1, 2), Blocks.ANVIL.defaultBlockState(), 2);
        level.setBlock(corner.offset(5, 1, 2), Blocks.GRINDSTONE.defaultBlockState(), 2);
        level.setBlock(corner.offset(6, 1, 2), Blocks.STONECUTTER.defaultBlockState(), 2);
        level.setBlock(corner.offset(7, 1, 2), Blocks.FURNACE.defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, Direction.SOUTH), 2);
        level.setBlock(corner.offset(8, 1, 2), Blocks.BLAST_FURNACE.defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, Direction.SOUTH), 2);
        level.setBlock(corner.offset(9, 1, 2), Blocks.SMOKER.defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, Direction.SOUTH), 2);
        level.setBlock(corner.offset(10, 1, 2), Blocks.BREWING_STAND.defaultBlockState(), 2);
        level.setBlock(corner.offset(11, 1, 2), Blocks.ENCHANTING_TABLE.defaultBlockState(), 2);
        level.setBlock(corner.offset(12, 1, 2), Blocks.LECTERN.defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, Direction.SOUTH), 2);
        level.setBlock(corner.offset(13, 1, 2), Blocks.CARTOGRAPHY_TABLE.defaultBlockState(), 2);
        level.setBlock(corner.offset(14, 1, 2), Blocks.FLETCHING_TABLE.defaultBlockState(), 2);
        level.setBlock(corner.offset(15, 1, 2), Blocks.LOOM.defaultBlockState(), 2);
        level.setBlock(corner.offset(16, 1, 2), Blocks.CAULDRON.defaultBlockState(), 2);

        for (int z = 3; z <= depth - 3; z += 2) {
            level.setBlock(corner.offset(1, 1, z), Blocks.BOOKSHELF.defaultBlockState(), 2);
            level.setBlock(corner.offset(1, 2, z), Blocks.BOOKSHELF.defaultBlockState(), 2);
            level.setBlock(corner.offset(2, 1, z), Blocks.POTTED_BLUE_ORCHID.defaultBlockState(), 2);
        }
    }

    private static void placePremiumWorkCenterTable(ServerLevel level, BlockPos corner, int width, int depth) {
        BlockPos center = corner.offset(width / 2, 1, depth / 2);
        for (int x = -3; x <= 3; x++) {
            for (int z = -1; z <= 1; z++) {
                BlockPos table = center.offset(x, 0, z);
                level.setBlock(table, Blocks.DARK_OAK_FENCE.defaultBlockState(), 2);
                level.setBlock(table.above(), Blocks.LIGHT_BLUE_CARPET.defaultBlockState(), 2);
            }
        }
        for (int x = -3; x <= 3; x += 2) {
            level.setBlock(center.offset(x, 0, -2), Blocks.DARK_OAK_STAIRS.defaultBlockState()
                    .setValue(StairBlock.FACING, Direction.SOUTH), 2);
            level.setBlock(center.offset(x, 0, 2), Blocks.DARK_OAK_STAIRS.defaultBlockState()
                    .setValue(StairBlock.FACING, Direction.NORTH), 2);
        }
        level.setBlock(center.offset(-4, 0, 0), Blocks.DARK_OAK_STAIRS.defaultBlockState()
                .setValue(StairBlock.FACING, Direction.EAST), 2);
        level.setBlock(center.offset(4, 0, 0), Blocks.DARK_OAK_STAIRS.defaultBlockState()
                .setValue(StairBlock.FACING, Direction.WEST), 2);
        level.setBlock(center.above(3), Blocks.SEA_LANTERN.defaultBlockState(), 2);
    }

    private static void placePremiumWorkCenterBedsAndArmor(ServerLevel level, BlockPos corner, int width, int depth) {
        for (int i = 0; i < 4; i++) {
            placeBed(level, corner.offset(2 + i * 3, 1, depth - 4), Direction.SOUTH);
        }
        spawnArmorStand(level, corner.offset(3, 1, 4),
                Items.NETHERITE_HELMET, Items.NETHERITE_CHESTPLATE,
                Items.NETHERITE_LEGGINGS, Items.NETHERITE_BOOTS,
                "Armadura de Netherite da Casa Grande");
        spawnArmorStand(level, corner.offset(5, 1, 4),
                new ItemStack(MagicWorld.DRACONIC_AETHER_HELMET.get()),
                new ItemStack(MagicWorld.DRACONIC_AETHER_CHESTPLATE.get()),
                new ItemStack(MagicWorld.DRACONIC_AETHER_LEGGINGS.get()),
                new ItemStack(MagicWorld.DRACONIC_AETHER_BOOTS.get()),
                "Armadura Draconic Aether da Casa Grande");
        spawnNamed(level, EntityType.PARROT, corner.offset(width / 2, 2, depth - 3), "Ave da Casa Grande Premium");
    }

    private static void decoratePremiumWorkCenterExterior(ServerLevel level, BlockPos corner, int width, int depth) {
        for (BlockPos planter : new BlockPos[] {
                corner.offset(2, 1, -1), corner.offset(6, 1, -1), corner.offset(width - 2, 1, -1),
                corner.offset(2, 1, depth + 1), corner.offset(6, 1, depth + 1), corner.offset(width - 2, 1, depth + 1),
                corner.offset(-1, 1, 2), corner.offset(-1, 1, depth - 2),
                corner.offset(width + 1, 1, 2), corner.offset(width + 1, 1, depth - 2)
        }) {
            if (level.getBlockState(planter).isAir() && level.getBlockState(planter.below()).isSolid()) {
                level.setBlock(planter, Blocks.FLOWERING_AZALEA.defaultBlockState(), 2);
            }
        }
        for (BlockPos flower : new BlockPos[] {
                corner.offset(3, 1, -2), corner.offset(5, 1, -2), corner.offset(width - 3, 1, -2),
                corner.offset(3, 1, depth + 2), corner.offset(5, 1, depth + 2), corner.offset(width - 3, 1, depth + 2)
        }) {
            if (level.getBlockState(flower).isAir() && level.getBlockState(flower.below()).isSolid()) {
                level.setBlock(flower, flowerFor(flower.getX() + flower.getZ()), 2);
            }
        }
    }

    private static void lightPremiumAnimalWorkCenter(ServerLevel level, BlockPos corner, int width, int depth) {
        for (int x = 2; x <= width - 2; x += 4) {
            for (int z = 2; z <= depth - 2; z += 4) {
                level.setBlock(corner.offset(x, 5, z), Blocks.SEA_LANTERN.defaultBlockState(), 2);
            }
        }
        for (BlockPos lamp : new BlockPos[] {
                corner.offset(-3, 0, -3), corner.offset(width + 3, 0, -3),
                corner.offset(-3, 0, depth + 3), corner.offset(width + 3, 0, depth + 3),
                corner.offset(width / 2, 0, -3), corner.offset(width / 2, 0, depth + 3)
        }) {
            placeLampPost(level, lamp);
        }
        for (int x = 1; x < width; x += 3) {
            level.setBlock(corner.offset(x, 1, -1), Blocks.SEA_LANTERN.defaultBlockState(), 2);
            level.setBlock(corner.offset(x, 1, depth + 1), Blocks.SEA_LANTERN.defaultBlockState(), 2);
        }
        for (int z = 1; z < depth; z += 3) {
            level.setBlock(corner.offset(-1, 1, z), Blocks.SEA_LANTERN.defaultBlockState(), 2);
            level.setBlock(corner.offset(width + 1, 1, z), Blocks.SEA_LANTERN.defaultBlockState(), 2);
        }
    }

    private static void clearPremiumAnimalWorkCenterEntrances(ServerLevel level, BlockPos corner, int width, int depth) {
        int eastDoorZ = depth / 2;
        BlockPos eastDoor = corner.offset(width, 1, eastDoorZ);
        BlockPos westDoorNorth = corner.offset(0, 1, depth / 2 - 3);
        BlockPos westDoorSouth = corner.offset(0, 1, depth / 2 + 3);

        clearPremiumDoorAccess(level, eastDoor, Direction.EAST, 6);
        clearPremiumDoorAccess(level, westDoorNorth, Direction.WEST, 6);
        clearPremiumDoorAccess(level, westDoorSouth, Direction.WEST, 6);

        placeHouseDoor(level, eastDoor, Direction.EAST);
        placeHouseDoor(level, westDoorNorth, Direction.WEST);
        placeHouseDoor(level, westDoorSouth, Direction.WEST);
    }

    private static void clearPremiumDoorAccess(ServerLevel level, BlockPos door, Direction facing, int outsideLength) {
        Direction side = facing.getClockWise();
        for (int step = -2; step <= outsideLength; step++) {
            if (step == 0) {
                continue;
            }
            for (int lateral = -1; lateral <= 1; lateral++) {
                BlockPos center = door.relative(facing, step).relative(side, lateral);
                BlockPos ground = center.below();
                level.setBlock(ground.below(), Blocks.DIRT.defaultBlockState(), 2);
                level.setBlock(ground, lateral == 0
                        ? Blocks.SMOOTH_STONE.defaultBlockState()
                        : Blocks.POLISHED_ANDESITE.defaultBlockState(), 2);
                for (int y = 1; y <= 4; y++) {
                    level.setBlock(ground.above(y), Blocks.AIR.defaultBlockState(), 2);
                }
            }
        }

        level.setBlock(door.relative(facing, 2).below(), Blocks.SEA_LANTERN.defaultBlockState(), 2);
        level.setBlock(door.relative(facing, 4).below(), Blocks.SEA_LANTERN.defaultBlockState(), 2);
    }

    private static void populatePremiumAnimalWorkCenter(ServerLevel level, BlockPos corner, BlockPos base) {
        BlockPos home = corner.offset(9, 1, 7);
        spawnProfessionalVillager(level, corner.offset(7, 1, 5), "Guardiao Aldeao da Casa Grande",
                VillagerProfession.WEAPONSMITH, home, home, Items.NETHERITE_SWORD, true);
        spawnProfessionalVillager(level, corner.offset(6, 1, 8), "Armoreiro da Casa Grande",
                VillagerProfession.ARMORER, home, corner.offset(4, 1, 2), Items.NETHERITE_CHESTPLATE, false);
        spawnProfessionalVillager(level, corner.offset(8, 1, 8), "Ferreiro da Casa Grande",
                VillagerProfession.WEAPONSMITH, home, corner.offset(3, 1, 2), Items.NETHERITE_SWORD, false);
        spawnProfessionalVillager(level, corner.offset(10, 1, 8), "Ferramenteiro da Casa Grande",
                VillagerProfession.TOOLSMITH, home, corner.offset(3, 1, 2), Items.NETHERITE_PICKAXE, false);
        spawnProfessionalVillager(level, corner.offset(12, 1, 8), "Bibliotecario da Casa Grande",
                VillagerProfession.LIBRARIAN, home, corner.offset(12, 1, 2), Items.BOOK, false);
        spawnProfessionalVillager(level, corner.offset(14, 1, 8), "Clerigo da Casa Grande",
                VillagerProfession.CLERIC, home, corner.offset(10, 1, 2), Items.BREWING_STAND, false);
        spawnProfessionalVillager(level, corner.offset(4, 1, 8), "Pedreiro da Casa Grande",
                VillagerProfession.MASON, home, base.offset(67, 0, 46), Items.STONE_BRICKS, false);
    }

    private static void assignAnimalCaretakersToPens(ServerLevel level, BlockPos base) {
        BlockPos[] houses = {
                base.offset(106, 0, -72),
                base.offset(106, 0, -56)
        };
        BlockPos[] pens = animalPenCorners(base);
        Item[] foods = {
                Items.WHEAT, Items.CARROT, Items.WHEAT,
                Items.WHEAT_SEEDS, Items.GOLDEN_CARROT, Items.WHEAT
        };
        AABB estate = new AABB(base).inflate(256.0D, 64.0D, 256.0D);
        for (int pen = 0; pen < pens.length; pen++) {
            int house = pen / 3;
            int worker = pen % 3 + 1;
            String name = "Cuidador da Fazenda Casa " + (house + 1) + "-" + worker;
            BlockPos home = houses[house].offset(5, 1, 4);
            BlockPos work = pens[pen].offset(7, 1, 6);
            for (Villager villager : level.getEntitiesOfClass(Villager.class, estate,
                    candidate -> name.equals(candidate.getName().getString()))) {
                villager.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(foods[pen]));
                empowerMagicWorldVillager(villager, VillagerProfession.FARMER, home, work, GLOBAL_VILLAGER_WORK_RADIUS);
                villager.getPersistentData().putInt("MagicWorldHomeX", home.getX());
                villager.getPersistentData().putInt("MagicWorldHomeY", home.getY());
                villager.getPersistentData().putInt("MagicWorldHomeZ", home.getZ());
                villager.getPersistentData().putInt("MagicWorldWorkX", work.getX());
                villager.getPersistentData().putInt("MagicWorldWorkY", work.getY());
                villager.getPersistentData().putInt("MagicWorldWorkZ", work.getZ());
                villager.getPersistentData().putInt("MagicWorldWorkRadius", GLOBAL_VILLAGER_WORK_RADIUS);
                villager.restrictTo(work, GLOBAL_VILLAGER_WORK_RADIUS);
            }
        }
    }

    private static void buildSimplePlantationWorkerHouse(ServerLevel level, BlockPos corner, Direction mainDoorFacing, Direction backDoorFacing) {
        int width = 14;
        int depth = 10;
        for (int x = 0; x <= width; x++) {
            for (int z = 0; z <= depth; z++) {
                BlockPos pos = corner.offset(x, 0, z);
                boolean wall = x == 0 || x == width || z == 0 || z == depth;
                boolean post = (x == 0 || x == width) && (z == 0 || z == depth);
                level.setBlock(pos.below(), Blocks.COBBLESTONE.defaultBlockState(), 2);
                level.setBlock(pos, Math.floorMod(x + z, 5) == 0 ? Blocks.SMOOTH_STONE.defaultBlockState() : Blocks.OAK_PLANKS.defaultBlockState(), 2);
                for (int y = 1; y <= 6; y++) {
                    BlockState wallState = post ? Blocks.OAK_LOG.defaultBlockState() : Blocks.SPRUCE_PLANKS.defaultBlockState();
                    level.setBlock(pos.above(y), wall ? wallState : Blocks.AIR.defaultBlockState(), 2);
                }
            }
        }
        for (int x = -1; x <= width + 1; x++) {
            for (int z = -1; z <= depth + 1; z++) {
                boolean edge = x == -1 || x == width + 1 || z == -1 || z == depth + 1;
                level.setBlock(corner.offset(x, 6, z), edge ? Blocks.DARK_OAK_PLANKS.defaultBlockState() : Blocks.OAK_SLAB.defaultBlockState(), 2);
            }
        }
        for (int x = -1; x <= width + 1; x++) {
            level.setBlock(corner.offset(x, 7, -1), Blocks.DARK_OAK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.NORTH), 2);
            level.setBlock(corner.offset(x, 7, depth + 1), Blocks.DARK_OAK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.SOUTH), 2);
        }
        buildDecorativeGabledRoof(level, corner, width, depth, 7);

        placeHouseDoor(level, plantationWorkerHouseDoorPos(corner, mainDoorFacing), mainDoorFacing);
        placeHouseDoor(level, plantationWorkerHouseDoorPos(corner, backDoorFacing), backDoorFacing);
        placeHouseDoor(level, plantationWorkerHouseDoorPos(corner, Direction.EAST), Direction.EAST);
        placeHouseDoor(level, plantationWorkerHouseDoorPos(corner, Direction.WEST), Direction.WEST);
        placeWindow(level, corner.offset(3, 2, 0));
        placeWindow(level, corner.offset(11, 2, 0));
        placeWindow(level, corner.offset(3, 2, depth));
        placeWindow(level, corner.offset(11, 2, depth));
        placeWindow(level, corner.offset(0, 2, 3));
        placeWindow(level, corner.offset(0, 2, 7));
        placeWindow(level, corner.offset(width, 2, 3));
        placeWindow(level, corner.offset(width, 2, 7));

        placeBed(level, corner.offset(2, 1, 2), Direction.SOUTH);
        placeBed(level, corner.offset(5, 1, 2), Direction.SOUTH);
        placeBed(level, corner.offset(8, 1, 2), Direction.SOUTH);
        placeChest(level, corner.offset(12, 1, 2), Direction.WEST);
        level.setBlock(corner.offset(12, 1, 4), Blocks.CRAFTING_TABLE.defaultBlockState(), 2);
        level.setBlock(corner.offset(12, 1, 5), Blocks.FURNACE.defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, Direction.WEST), 2);
        level.setBlock(corner.offset(12, 1, 6), Blocks.COMPOSTER.defaultBlockState(), 2);
        level.setBlock(corner.offset(7, 4, 4), Blocks.LANTERN.defaultBlockState(), 2);
        level.setBlock(corner.offset(7, 4, 8), Blocks.LANTERN.defaultBlockState(), 2);
        level.setBlock(corner.offset(3, 1, 8), Blocks.BOOKSHELF.defaultBlockState(), 2);
        putItems(level, corner.offset(12, 1, 2), new ItemStack(Items.BREAD, 32), new ItemStack(Items.WHEAT, 32),
                new ItemStack(Items.CARROT, 32), new ItemStack(Items.POTATO, 32), new ItemStack(Items.BONE_MEAL, 32));
    }

    private static void buildDecorativeGabledRoof(
            ServerLevel level,
            BlockPos corner,
            int width,
            int depth,
            int startY
    ) {
        int layers = depth / 2;
        for (int layer = 0; layer <= layers; layer++) {
            int y = startY + layer;
            int northZ = -1 + layer;
            int southZ = depth + 1 - layer;
            for (int x = -1; x <= width + 1; x++) {
                level.setBlock(corner.offset(x, y, northZ), Blocks.DARK_OAK_STAIRS.defaultBlockState()
                        .setValue(StairBlock.FACING, Direction.NORTH), 2);
                level.setBlock(corner.offset(x, y, southZ), Blocks.DARK_OAK_STAIRS.defaultBlockState()
                        .setValue(StairBlock.FACING, Direction.SOUTH), 2);
            }
        }
        int ridgeY = startY + layers + 1;
        int ridgeZ = depth / 2;
        for (int x = -1; x <= width + 1; x++) {
            level.setBlock(corner.offset(x, ridgeY, ridgeZ), Blocks.DARK_OAK_SLAB.defaultBlockState(), 2);
        }
        for (int z = 1; z < depth; z++) {
            int gableHeight = Math.min(z, depth - z);
            for (int y = startY; y < startY + gableHeight; y++) {
                level.setBlock(corner.offset(0, y, z), Blocks.SPRUCE_PLANKS.defaultBlockState(), 2);
                level.setBlock(corner.offset(width, y, z), Blocks.SPRUCE_PLANKS.defaultBlockState(), 2);
            }
        }
        placeWindow(level, corner.offset(0, startY + 1, ridgeZ));
        placeWindow(level, corner.offset(width, startY + 1, ridgeZ));
    }

    private static void buildPlantationWorkerWarehouse(ServerLevel level, BlockPos corner) {
        int width = 22;
        int depth = 16;
        for (int x = 0; x <= width; x++) {
            for (int z = 0; z <= depth; z++) {
                BlockPos pos = corner.offset(x, 0, z);
                boolean wall = x == 0 || x == width || z == 0 || z == depth;
                boolean post = (x == 0 || x == width) && (z == 0 || z == depth);
                level.setBlock(pos.below(), Blocks.COBBLESTONE.defaultBlockState(), 2);
                level.setBlock(pos, Math.floorMod(x + z, 4) == 0 ? Blocks.POLISHED_ANDESITE.defaultBlockState() : Blocks.SPRUCE_PLANKS.defaultBlockState(), 2);
                for (int y = 1; y <= 7; y++) {
                    BlockState wallState = post || (wall && y >= 5) ? Blocks.OAK_LOG.defaultBlockState() : Blocks.STRIPPED_SPRUCE_LOG.defaultBlockState();
                    level.setBlock(pos.above(y), wall ? wallState : Blocks.AIR.defaultBlockState(), 2);
                }
            }
        }
        for (int x = -2; x <= width + 2; x++) {
            for (int z = -2; z <= depth + 2; z++) {
                boolean edge = x <= -1 || x >= width + 1 || z <= -1 || z >= depth + 1;
                level.setBlock(corner.offset(x, 8, z), edge ? Blocks.DARK_OAK_PLANKS.defaultBlockState() : Blocks.DARK_OAK_SLAB.defaultBlockState(), 2);
            }
        }
        for (int x = -1; x <= width + 1; x++) {
            level.setBlock(corner.offset(x, 9, -2), Blocks.DARK_OAK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.NORTH), 2);
            level.setBlock(corner.offset(x, 9, depth + 2), Blocks.DARK_OAK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.SOUTH), 2);
        }
        buildDecorativeGabledRoof(level, corner, width, depth, 9);

        placeHouseDoor(level, corner.offset(width / 2, 1, depth), Direction.SOUTH);
        placeHouseDoor(level, corner.offset(width / 2, 1, 0), Direction.NORTH);
        placeHouseDoor(level, corner.offset(0, 1, depth / 2), Direction.WEST);
        placeHouseDoor(level, corner.offset(width, 1, depth / 2), Direction.EAST);
        buildHousePathToFarm(level, corner.offset(width / 2, 1, depth), Direction.SOUTH, 8);
        buildHousePathToFarm(level, corner.offset(width / 2, 1, 0), Direction.NORTH, 5);
        buildHousePathToFarm(level, corner.offset(0, 1, depth / 2), Direction.WEST, 5);
        buildHousePathToFarm(level, corner.offset(width, 1, depth / 2), Direction.EAST, 5);

        BlockPos center = corner.offset(width / 2, 1, depth / 2);
        level.setBlock(center, Blocks.BELL.defaultBlockState(), 2);
        for (BlockPos light : new BlockPos[] {center.above(4), corner.offset(3, 6, 3), corner.offset(width - 3, 6, 3),
                corner.offset(3, 6, depth - 3), corner.offset(width - 3, 6, depth - 3)}) {
            level.setBlock(light, Blocks.LANTERN.defaultBlockState(), 2);
        }
        for (int i = 0; i < 8; i++) {
            int x = 2 + (i % 4) * 3;
            int z = 2 + (i / 4) * (depth - 5);
            placeBed(level, corner.offset(x, 1, z), i < 4 ? Direction.SOUTH : Direction.NORTH);
        }
        for (BlockPos workstation : new BlockPos[] {
                corner.offset(width - 3, 1, 2), corner.offset(width - 3, 1, 5), corner.offset(width - 3, 1, 7),
                corner.offset(width - 5, 1, depth - 2), corner.offset(width - 7, 1, depth - 2),
                corner.offset(width - 9, 1, depth - 2), corner.offset(width - 11, 1, depth - 2)
        }) {
            level.setBlock(workstation, Blocks.CRAFTING_TABLE.defaultBlockState(), 2);
        }
        level.setBlock(corner.offset(width - 3, 1, 3), Blocks.FURNACE.defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, Direction.WEST), 2);
        level.setBlock(corner.offset(width - 3, 1, 4), Blocks.BLAST_FURNACE.defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, Direction.WEST), 2);
        placeChest(level, corner.offset(2, 1, depth - 2), Direction.NORTH);
        placeChest(level, corner.offset(4, 1, depth - 2), Direction.NORTH);
        putItems(level, corner.offset(2, 1, depth - 2), new ItemStack(Items.BREAD, 64), new ItemStack(Items.GOLDEN_CARROT, 64), new ItemStack(Items.BUCKET, 8));
        putItems(level, corner.offset(4, 1, depth - 2), new ItemStack(Items.WHEAT, 64), new ItemStack(Items.CARROT, 64), new ItemStack(Items.POTATO, 64), new ItemStack(Items.BONE_MEAL, 64));

        decoratePlantationWorkerWarehouse(level, corner, width, depth);
        placeLampPost(level, corner.offset(-3, 0, depth / 2));
        placeLampPost(level, corner.offset(width + 3, 0, depth / 2));
    }

    private static void decoratePlantationWorkerWarehouse(ServerLevel level, BlockPos corner, int width, int depth) {
        for (int x : new int[] {3, 7, width - 7, width - 3}) {
            placeWorkerWindowIfWall(level, corner.offset(x, 2, 0));
            placeWorkerWindowIfWall(level, corner.offset(x, 2, depth));
        }
        for (int z : new int[] {3, 6, depth - 6, depth - 3}) {
            placeWorkerWindowIfWall(level, corner.offset(0, 2, z));
            placeWorkerWindowIfWall(level, corner.offset(width, 2, z));
        }
        decorateWorkerDoors(level, corner, width, depth);

        for (BlockPos station : new BlockPos[] {
                corner.offset(2, 1, 7), corner.offset(2, 1, 8),
                corner.offset(width - 2, 1, 8), corner.offset(width - 2, 1, 9)
        }) {
            level.setBlock(station, Blocks.BOOKSHELF.defaultBlockState(), 2);
        }
        level.setBlock(corner.offset(3, 1, 8), Blocks.ENCHANTING_TABLE.defaultBlockState(), 2);
        level.setBlock(corner.offset(4, 1, 8), Blocks.BREWING_STAND.defaultBlockState(), 2);
        level.setBlock(corner.offset(width - 4, 1, 8), Blocks.ANVIL.defaultBlockState(), 2);
        level.setBlock(corner.offset(width - 3, 1, 8), Blocks.SMITHING_TABLE.defaultBlockState(), 2);

        for (BlockPos light : new BlockPos[] {
                corner.offset(5, 6, 5), corner.offset(width - 5, 6, 5),
                corner.offset(5, 6, depth - 5), corner.offset(width - 5, 6, depth - 5),
                corner.offset(width / 2, 6, depth / 2)
        }) {
            level.setBlock(light, Blocks.SEA_LANTERN.defaultBlockState(), 2);
        }

        BlockPos wandChest = corner.offset(7, 1, depth / 2);
        placeChest(level, wandChest, Direction.NORTH);
        fillContainerWithItem(level, wandChest, MagicWorld.VARINHA_MAGICA.get());
        BlockPos premiumChest = corner.offset(width - 7, 1, depth / 2);
        placeChest(level, premiumChest, Direction.NORTH);
        putItems(level, premiumChest,
                new ItemStack(Items.ENCHANTED_GOLDEN_APPLE, 16),
                new ItemStack(Items.TOTEM_OF_UNDYING, 8),
                new ItemStack(Items.NETHER_STAR, 8),
                new ItemStack(Items.NETHERITE_INGOT, 16),
                new ItemStack(Items.ELYTRA),
                new ItemStack(Items.TRIDENT));

        spawnArmorStand(level, corner.offset(5, 1, depth / 2),
                Items.NETHERITE_HELMET, Items.NETHERITE_CHESTPLATE,
                Items.NETHERITE_LEGGINGS, Items.NETHERITE_BOOTS,
                "Armadura Netherite do Rancho");
        spawnArmorStand(level, corner.offset(width - 5, 1, depth / 2),
                new ItemStack(MagicWorld.DRACONIC_AETHER_HELMET.get()),
                new ItemStack(MagicWorld.DRACONIC_AETHER_CHESTPLATE.get()),
                new ItemStack(MagicWorld.DRACONIC_AETHER_LEGGINGS.get()),
                new ItemStack(MagicWorld.DRACONIC_AETHER_BOOTS.get()),
                "Armadura Draconic Aether do Rancho");
        spawnNamed(level, EntityType.PARROT, corner.offset(width / 2, 2, depth - 3), "Ave do Rancho da Plantacao");

        for (BlockPos plant : new BlockPos[] {
                corner.offset(3, 1, -1), corner.offset(width - 3, 1, -1),
                corner.offset(3, 1, depth + 1), corner.offset(width - 3, 1, depth + 1)
        }) {
            if (level.getBlockState(plant).isAir() && level.getBlockState(plant.below()).isSolid()) {
                level.setBlock(plant, Blocks.FLOWERING_AZALEA.defaultBlockState(), 2);
            }
        }
    }

    private static void buildCaretakerHouse(ServerLevel level, BlockPos corner, Direction doorFacing) {
        int width = 12;
        int depth = 8;
        for (int x = 0; x <= width; x++) {
            for (int z = 0; z <= depth; z++) {
                BlockPos pos = corner.offset(x, 0, z);
                boolean wall = x == 0 || x == width || z == 0 || z == depth;
                boolean post = (x == 0 || x == width) && (z == 0 || z == depth);
                level.setBlock(pos.below(), Blocks.COBBLESTONE.defaultBlockState(), 2);
                level.setBlock(pos, Blocks.OAK_PLANKS.defaultBlockState(), 2);
                for (int y = 1; y <= 5; y++) {
                    level.setBlock(pos.above(y), wall ? (post ? Blocks.OAK_LOG.defaultBlockState() : Blocks.BIRCH_PLANKS.defaultBlockState()) : Blocks.AIR.defaultBlockState(), 2);
                }
            }
        }
        for (int x = -1; x <= width + 1; x++) {
            for (int z = -1; z <= depth + 1; z++) {
                level.setBlock(corner.offset(x, 6, z), Blocks.DARK_OAK_PLANKS.defaultBlockState(), 2);
            }
        }
        placeHouseDoor(level, houseDoorPos(corner, doorFacing), doorFacing);
        placeWindow(level, corner.offset(3, 2, 0));
        placeWindow(level, corner.offset(9, 2, 0));
        placeWindow(level, corner.offset(3, 2, depth));
        placeWindow(level, corner.offset(9, 2, depth));
        placeBed(level, corner.offset(2, 1, 2), Direction.SOUTH);
        placeBed(level, corner.offset(5, 1, 2), Direction.SOUTH);
        placeChest(level, corner.offset(10, 1, 2), Direction.WEST);
        level.setBlock(corner.offset(10, 1, 4), Blocks.COMPOSTER.defaultBlockState(), 2);
        level.setBlock(corner.offset(6, 4, 4), Blocks.LANTERN.defaultBlockState(), 2);
        putItems(level, corner.offset(10, 1, 2), new ItemStack(Items.WHEAT, 64), new ItemStack(Items.LEAD, 16), new ItemStack(Items.NAME_TAG, 8));
        placeLampPost(level, houseDoorPos(corner, doorFacing).relative(doorFacing, 4).below());
    }

    private static void decorateWorkerHome(ServerLevel level, BlockPos corner, int width, int depth, String label) {
        BlockPos table = corner.offset(width / 2, 1, depth / 2);
        level.setBlock(table, Blocks.OAK_FENCE.defaultBlockState(), 2);
        level.setBlock(table.above(), Blocks.LIGHT_BLUE_CARPET.defaultBlockState(), 2);
        level.setBlock(table.north(), Blocks.OAK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.SOUTH), 2);
        level.setBlock(table.south(), Blocks.OAK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.NORTH), 2);
        level.setBlock(table.east(), Blocks.OAK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.WEST), 2);
        level.setBlock(table.west(), Blocks.OAK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.EAST), 2);

        level.setBlock(corner.offset(width - 2, 1, 2), Blocks.SMOKER.defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, Direction.WEST), 2);
        level.setBlock(corner.offset(width - 2, 1, 3), Blocks.BLAST_FURNACE.defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, Direction.WEST), 2);
        level.setBlock(corner.offset(width - 2, 1, 4), Blocks.BARREL.defaultBlockState(), 2);
        level.setBlock(corner.offset(width - 2, 1, 5), Blocks.CRAFTING_TABLE.defaultBlockState(), 2);
        level.setBlock(corner.offset(2, 1, depth - 2), Blocks.BOOKSHELF.defaultBlockState(), 2);
        level.setBlock(corner.offset(3, 1, depth - 2), Blocks.LECTERN.defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, Direction.NORTH), 2);
        level.setBlock(corner.offset(4, 1, depth - 2), Blocks.BREWING_STAND.defaultBlockState(), 2);
        level.setBlock(corner.offset(5, 1, depth - 2), Blocks.ANVIL.defaultBlockState(), 2);
        level.setBlock(corner.offset(width - 3, 1, depth - 2), Blocks.SMITHING_TABLE.defaultBlockState(), 2);
        level.setBlock(corner.offset(width - 4, 1, depth - 2), Blocks.JUKEBOX.defaultBlockState(), 2);

        for (BlockPos carpet : new BlockPos[] {
                table.north().north(), table.south().south(),
                table.east().east(), table.west().west()
        }) {
            if (level.getBlockState(carpet).isAir()) {
                level.setBlock(carpet, Blocks.CYAN_CARPET.defaultBlockState(), 2);
            }
        }

        for (BlockPos light : new BlockPos[] {
                corner.offset(width / 2, 4, depth / 2),
                corner.offset(3, 4, 3),
                corner.offset(width - 3, 4, depth - 3)
        }) {
            level.setBlock(light, Blocks.SEA_LANTERN.defaultBlockState(), 2);
        }

        for (BlockPos pot : new BlockPos[] {
                corner.offset(1, 1, 1),
                corner.offset(width - 1, 1, 1),
                corner.offset(1, 1, depth - 1),
                corner.offset(width - 1, 1, depth - 1)
        }) {
            level.setBlock(pot, Blocks.POTTED_DANDELION.defaultBlockState(), 2);
        }

        for (BlockPos plant : new BlockPos[] {
                corner.offset(-1, 1, 2),
                corner.offset(-1, 1, depth - 2),
                corner.offset(width + 1, 1, 2),
                corner.offset(width + 1, 1, depth - 2)
        }) {
            if (level.getBlockState(plant).isAir() && level.getBlockState(plant.below()).isSolid()) {
                level.setBlock(plant, Blocks.FLOWERING_AZALEA.defaultBlockState(), 2);
            }
        }

        decorateWorkerHomeExterior(level, corner, width, depth);
        spawnNamed(level, EntityType.PARROT, corner.offset(width / 2, 2, depth - 2), "Ave " + label);
    }

    private static void decorateWorkerHomeExterior(ServerLevel level, BlockPos corner, int width, int depth) {
        for (int x = 1; x < width; x++) {
            level.setBlock(corner.offset(x, 4, 0), Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState(), 2);
            level.setBlock(corner.offset(x, 4, depth), Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState(), 2);
        }
        for (int z = 1; z < depth; z++) {
            level.setBlock(corner.offset(0, 4, z), Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState(), 2);
            level.setBlock(corner.offset(width, 4, z), Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState(), 2);
        }

        for (BlockPos post : new BlockPos[] {
                corner.offset(-1, 1, -1),
                corner.offset(width + 1, 1, -1),
                corner.offset(-1, 1, depth + 1),
                corner.offset(width + 1, 1, depth + 1)
        }) {
            for (int y = 0; y <= 4; y++) {
                BlockPos pos = post.above(y);
                if (level.getBlockState(pos).isAir() || level.getBlockState(pos).canBeReplaced()) {
                    level.setBlock(pos, Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState(), 2);
                }
            }
            level.setBlock(post.above(5), Blocks.SEA_LANTERN.defaultBlockState(), 2);
        }

        for (BlockPos window : new BlockPos[] {
                corner.offset(width / 2, 2, 0), corner.offset(width / 2 - 2, 2, 0), corner.offset(width / 2 + 2, 2, 0),
                corner.offset(width / 2, 2, depth), corner.offset(width / 2 - 2, 2, depth), corner.offset(width / 2 + 2, 2, depth),
                corner.offset(0, 2, depth / 2), corner.offset(0, 2, depth / 2 - 2), corner.offset(0, 2, depth / 2 + 2),
                corner.offset(width, 2, depth / 2), corner.offset(width, 2, depth / 2 - 2), corner.offset(width, 2, depth / 2 + 2)
        }) {
            placeWorkerWindowIfWall(level, window);
        }

        decorateWorkerDoors(level, corner, width, depth);

        for (BlockPos planter : new BlockPos[] {
                corner.offset(2, 1, -1), corner.offset(width - 2, 1, -1),
                corner.offset(2, 1, depth + 1), corner.offset(width - 2, 1, depth + 1),
                corner.offset(-1, 1, 2), corner.offset(-1, 1, depth - 2),
                corner.offset(width + 1, 1, 2), corner.offset(width + 1, 1, depth - 2)
        }) {
            if (level.getBlockState(planter).isAir() && level.getBlockState(planter.below()).isSolid()) {
                level.setBlock(planter, Blocks.FLOWERING_AZALEA.defaultBlockState(), 2);
            }
        }

        for (BlockPos light : new BlockPos[] {
                corner.offset(width / 2, 3, -1),
                corner.offset(width / 2, 3, depth + 1),
                corner.offset(-1, 3, depth / 2),
                corner.offset(width + 1, 3, depth / 2)
        }) {
            if (level.getBlockState(light).isAir() || level.getBlockState(light).canBeReplaced()) {
                level.setBlock(light, Blocks.LANTERN.defaultBlockState(), 2);
            }
        }
    }

    private static void decorateWorkerDoors(ServerLevel level, BlockPos corner, int width, int depth) {
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        for (int x = 0; x <= width; x++) {
            for (int z = 0; z <= depth; z++) {
                for (int y = 1; y <= 3; y++) {
                    mutable.set(corner.getX() + x, corner.getY() + y, corner.getZ() + z);
                    BlockState state = level.getBlockState(mutable);
                    if (!(state.getBlock() instanceof DoorBlock)
                            || state.getValue(DoorBlock.HALF) != DoubleBlockHalf.LOWER) {
                        continue;
                    }

                    Direction facing = state.getValue(DoorBlock.FACING);
                    Direction side = facing.getClockWise();
                    BlockPos lintel = mutable.above(2).immutable();
                    level.setBlock(lintel, Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState(), 2);
                    level.setBlock(lintel.relative(side), Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState(), 2);
                    level.setBlock(lintel.relative(side.getOpposite()), Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState(), 2);

                    BlockPos awning = lintel.relative(facing);
                    if (level.getBlockState(awning).isAir() || level.getBlockState(awning).canBeReplaced()) {
                        level.setBlock(awning, Blocks.DARK_OAK_STAIRS.defaultBlockState()
                                .setValue(StairBlock.FACING, facing), 2);
                    }
                    BlockPos lamp = mutable.relative(facing).above(2);
                    if (level.getBlockState(lamp).isAir() || level.getBlockState(lamp).canBeReplaced()) {
                        level.setBlock(lamp, Blocks.LANTERN.defaultBlockState(), 2);
                    }
                }
            }
        }
    }

    private static void placeWorkerWindowIfWall(ServerLevel level, BlockPos lower) {
        if (isWorkerHouseWallBlock(level.getBlockState(lower))
                && isWorkerHouseWallBlock(level.getBlockState(lower.above()))) {
            placeWindow(level, lower);
            for (Direction direction : Direction.Plane.HORIZONTAL) {
                BlockPos trim = lower.relative(direction);
                if (level.getBlockState(trim).isAir()) {
                    level.setBlock(trim, Blocks.DARK_OAK_FENCE.defaultBlockState(), 2);
                }
            }
        }
    }

    private static boolean isWorkerHouseWallBlock(BlockState state) {
        return state.is(Blocks.SPRUCE_PLANKS)
                || state.is(Blocks.BIRCH_PLANKS)
                || state.is(Blocks.OAK_PLANKS)
                || state.is(Blocks.DARK_OAK_PLANKS)
                || state.is(Blocks.OAK_LOG)
                || state.is(Blocks.STRIPPED_SPRUCE_LOG);
    }

    private static void spawnWorkersForHouseBeds(
            ServerLevel level,
            BlockPos corner,
            int width,
            int height,
            int depth,
            String namePrefix,
            BlockPos home,
            BlockPos farmCenter,
            Item heldItem
    ) {
        int beds = countBedsInArea(level, corner, width, height, depth);
        for (int villager = 0; villager < beds; villager++) {
            spawnWorkerVillager(level, corner.offset(2 + (villager % 4) * 3, 1, 3 + (villager / 4) * 3),
                    namePrefix + "-" + (villager + 1), home, farmCenter, heldItem);
        }
    }

    private static int countBedsInArea(ServerLevel level, BlockPos corner, int width, int height, int depth) {
        int beds = 0;
        for (int x = 0; x <= width; x++) {
            for (int y = 0; y <= height; y++) {
                for (int z = 0; z <= depth; z++) {
                    BlockState state = level.getBlockState(corner.offset(x, y, z));
                    if (state.getBlock() instanceof BedBlock
                            && state.getValue(BedBlock.PART) == BedPart.FOOT) {
                        beds++;
                    }
                }
            }
        }
        return beds;
    }

    private static void buildAnimalFoodStripFarm(ServerLevel level, BlockPos corner, int width, int depth) {
        for (int x = 0; x < width; x++) {
            for (int z = 0; z < depth; z++) {
                BlockPos pos = corner.offset(x, 0, z);
                level.setBlock(pos.below(), Blocks.DIRT.defaultBlockState(), 2);
                if (x == width / 2) {
                    level.setBlock(pos, Blocks.WATER.defaultBlockState(), 2);
                } else {
                    level.setBlock(pos, Blocks.FARMLAND.defaultBlockState(), 2);
                    BlockState crop = Math.floorMod(z, 3) == 0
                            ? Blocks.WHEAT.defaultBlockState().setValue(CropBlock.AGE, 7)
                            : Math.floorMod(z, 3) == 1
                            ? Blocks.CARROTS.defaultBlockState().setValue(CropBlock.AGE, 7)
                            : Blocks.POTATOES.defaultBlockState().setValue(CropBlock.AGE, 7);
                    level.setBlock(pos.above(), crop, 2);
                }
            }
        }
        placeLampPost(level, corner.offset(width / 2, 0, -2));
        placeLampPost(level, corner.offset(width / 2, 0, depth + 1));
    }

    private static void buildStarterEstateRoads(ServerLevel level, BlockPos base) {
        buildStarterRoad(level, base, Direction.SOUTH, 76);
        buildStarterRoad(level, base, Direction.NORTH, 62);
        buildStarterRoad(level, base, Direction.EAST, 76);
        buildStarterRoad(level, base, Direction.WEST, 76);
        buildRoadBetween(level, base, base.offset(-118, -1, -48), base.offset(-94, -1, -48));
        buildRoadBetween(level, base, base.offset(104, -1, -64), base.offset(104, -1, -42));
        buildRoadBetween(level, base, base.offset(-76, -1, 34), base.offset(-106, -1, 34));
        buildRoadBetween(level, base, base.offset(-106, -1, 34), base.offset(-106, -1, 60));
    }

    private static void buildStarterRoad(ServerLevel level, BlockPos base, Direction direction, int length) {
        Direction side = direction.getClockWise();
        for (int step = 0; step <= length; step++) {
            BlockPos center = filledGroundAt(level, base.relative(direction, step));
            for (int width = -2; width <= 2; width++) {
                BlockPos pos = center.relative(side, width);
                if (isInsideImportedHouseFootprint(base, pos)) {
                    continue;
                }
                level.setBlock(pos.below(), Blocks.DIRT.defaultBlockState(), 2);
                level.setBlock(pos, Math.abs(width) == 2 ? Blocks.POLISHED_ANDESITE.defaultBlockState() : Blocks.SMOOTH_STONE.defaultBlockState(), 2);
                level.setBlock(pos.above(), Blocks.AIR.defaultBlockState(), 2);
                level.setBlock(pos.above(2), Blocks.AIR.defaultBlockState(), 2);
            }
            BlockPos leftShoulder = center.relative(side, -3);
            BlockPos rightShoulder = center.relative(side, 3);
            if (!isInsideImportedHouseFootprint(base, leftShoulder)) {
                placeRoadShoulderSlab(level, leftShoulder);
            }
            if (!isInsideImportedHouseFootprint(base, rightShoulder)) {
                placeRoadShoulderSlab(level, rightShoulder);
            }
            if (step > 0 && step % 16 == 0) {
                BlockPos rightLamp = center.relative(side, 4);
                BlockPos leftLamp = center.relative(side, -4);
                if (!isInsideImportedHouseFootprint(base, rightLamp)) {
                    placeLampPost(level, rightLamp);
                }
                if (!isInsideImportedHouseFootprint(base, leftLamp)) {
                    placeLampPost(level, leftLamp);
                }
            }
        }
    }

    private static void buildRoadBetween(ServerLevel level, BlockPos base, BlockPos from, BlockPos to) {
        int dx = Integer.compare(to.getX(), from.getX());
        int dz = Integer.compare(to.getZ(), from.getZ());
        BlockPos pos = filledGroundAt(level, from);
        int guard = 0;
        while ((pos.getX() != to.getX() || pos.getZ() != to.getZ()) && guard++ < 256) {
            for (int x = -1; x <= 1; x++) {
                for (int z = -1; z <= 1; z++) {
                    BlockPos road = filledGroundAt(level, pos.offset(x, 0, z));
                    if (isInsideImportedHouseFootprint(base, road)) {
                        continue;
                    }
                    level.setBlock(road.below(), Blocks.DIRT.defaultBlockState(), 2);
                    level.setBlock(road, Blocks.SMOOTH_STONE.defaultBlockState(), 2);
                    level.setBlock(road.above(), Blocks.AIR.defaultBlockState(), 2);
                }
            }
            for (int x = -2; x <= 2; x++) {
                placeRoadShoulderSlabOutsideHouse(level, base, filledGroundAt(level, pos.offset(x, 0, -2)));
                placeRoadShoulderSlabOutsideHouse(level, base, filledGroundAt(level, pos.offset(x, 0, 2)));
            }
            for (int z = -1; z <= 1; z++) {
                placeRoadShoulderSlabOutsideHouse(level, base, filledGroundAt(level, pos.offset(-2, 0, z)));
                placeRoadShoulderSlabOutsideHouse(level, base, filledGroundAt(level, pos.offset(2, 0, z)));
            }
            if (pos.getX() != to.getX()) {
                pos = pos.offset(dx, 0, 0);
            } else if (pos.getZ() != to.getZ()) {
                pos = pos.offset(0, 0, dz);
            }
        }
    }

    private static void placeRoadShoulderSlabOutsideHouse(ServerLevel level, BlockPos base, BlockPos pos) {
        if (!isInsideImportedHouseFootprint(base, pos)) {
            placeRoadShoulderSlab(level, pos);
        }
    }

    private static void placeRoadShoulderSlab(ServerLevel level, BlockPos pos) {
        if (!canPatchHousePerimeterGround(level.getBlockState(pos)) && !level.getBlockState(pos).is(Blocks.GRASS_BLOCK)) {
            return;
        }
        level.setBlock(pos.below(), Blocks.DIRT.defaultBlockState(), 2);
        level.setBlock(pos, Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), 2);
        level.setBlock(pos.above(), Blocks.AIR.defaultBlockState(), 2);
    }

    private static void buildEnhancedEstateLighting(ServerLevel level, BlockPos base) {
        for (BlockPos pos : new BlockPos[] {
                base.offset(-58, 0, -42), base.offset(48, 0, -42),
                base.offset(-58, 0, 50), base.offset(48, 0, 50),
                base.offset(50, 0, -40), base.offset(74, 0, -24),
                base.offset(96, 0, -8), compactPortalPlazaCenter(base).offset(-14, 0, -12),
                compactPortalPlazaCenter(base).offset(14, 0, -12), compactPortalPlazaCenter(base).offset(-14, 0, 12),
                compactPortalPlazaCenter(base).offset(14, 0, 12)
        }) {
            placeLampPost(level, pos);
        }
        if (MagicWorldWorldOptions.isCastlesEnabled()) {
            BlockPos castle = castleCenter(base);
            for (BlockPos pos : new BlockPos[] {
                    castle.offset(-54, 0, -50), castle.offset(54, 0, -50),
                    castle.offset(-54, 0, 50), castle.offset(54, 0, 50)
            }) {
                placeLampPost(level, pos);
            }
        }
    }

    private static void stabilizeGreenVillageDistrictTerrain(ServerLevel level, BlockPos base) {
        for (int x = -126; x <= -84; x++) {
            for (int z = 0; z <= 70; z++) {
                BlockPos ground = base.offset(x, -1, z);
                for (int y = -8; y <= -2; y++) {
                    BlockPos support = base.offset(x, y, z);
                    if (canPatchHousePerimeterGround(level.getBlockState(support))) {
                        level.setBlock(support, Blocks.DIRT.defaultBlockState(), 2);
                    }
                }
                if (canPatchHousePerimeterGround(level.getBlockState(ground))) {
                    level.setBlock(ground, Blocks.GRASS_BLOCK.defaultBlockState(), 2);
                }
                for (int y = 0; y <= 12; y++) {
                    BlockPos clear = base.offset(x, y, z);
                    if (!isProtectedGeneratedBlock(level.getBlockState(clear))) {
                        level.setBlock(clear, Blocks.AIR.defaultBlockState(), 2);
                    }
                }
            }
        }
    }

    private static void stabilizeEstateOpenGapTerrain(ServerLevel level, BlockPos base) {
        stabilizeNaturalTerrainRect(level, base, -128, -86, -76, 80);
        stabilizeNaturalTerrainRect(level, base, -100, 44, 54, 80);
        stabilizeNaturalTerrainRect(level, base, -128, -84, -50, -5);
    }

    private static void stabilizeEstateAirGaps(ServerLevel level, BlockPos base) {
        for (int x = IMPORTED_ESTATE_FENCE_MIN_X + 1; x < IMPORTED_ESTATE_FENCE_MAX_X; x++) {
            for (int z = IMPORTED_ESTATE_FENCE_MIN_Z + 1; z < IMPORTED_ESTATE_FENCE_MAX_Z; z++) {
                if (x >= 55 && x <= 79 && z >= 34 && z <= 58) {
                    continue;
                }

                BlockPos ground = base.offset(x, -1, z);
                if (!level.getBlockState(ground).isAir()) {
                    continue;
                }

                for (int y = -8; y <= -2; y++) {
                    BlockPos support = base.offset(x, y, z);
                    if (level.getBlockState(support).isAir()) {
                        level.setBlock(support, Blocks.DIRT.defaultBlockState(), 2);
                    }
                }
                level.setBlock(ground, Blocks.GRASS_BLOCK.defaultBlockState(), 2);
            }
        }
    }

    private static void normalizeImportedHouseFrontRoad(ServerLevel level, BlockPos base) {
        for (int z = IMPORTED_HOUSE_MAX_Z + 1; z <= IMPORTED_ESTATE_FENCE_MAX_Z - 1; z++) {
            for (int x = -20; x <= 20; x++) {
                BlockState surface = Math.abs(x) <= 5
                        ? Blocks.POLISHED_ANDESITE.defaultBlockState()
                        : Math.abs(x) <= 7
                        ? Blocks.POLISHED_DIORITE.defaultBlockState()
                        : Blocks.GRASS_BLOCK.defaultBlockState();

                placeElevatedHouseRoadBlock(level, base, x, z, surface);
            }
        }

        for (int x = IMPORTED_HOUSE_MAX_X + 1; x <= IMPORTED_ESTATE_FENCE_MAX_X - 1; x++) {
            for (int z = -8; z <= 8; z++) {
                BlockState surface = Math.abs(z) <= 4
                        ? Blocks.POLISHED_ANDESITE.defaultBlockState()
                        : Math.abs(z) <= 6
                        ? Blocks.POLISHED_DIORITE.defaultBlockState()
                        : Blocks.GRASS_BLOCK.defaultBlockState();
                placeElevatedHouseRoadBlock(level, base, x, z, surface);
            }
        }

        for (int x = IMPORTED_ESTATE_FENCE_MIN_X + 1; x <= HOUSE_ORIGIN_X - 1; x++) {
            for (int z = -8; z <= 8; z++) {
                BlockState surface = Math.abs(z) <= 4
                        ? Blocks.POLISHED_ANDESITE.defaultBlockState()
                        : Math.abs(z) <= 6
                        ? Blocks.POLISHED_DIORITE.defaultBlockState()
                        : Blocks.GRASS_BLOCK.defaultBlockState();
                placeElevatedHouseRoadBlock(level, base, x, z, surface);
            }
        }
    }

    private static void placeElevatedHouseRoadBlock(ServerLevel level, BlockPos base, int x, int z, BlockState surface) {
        BlockPos road = base.offset(x, 1, z);
        for (int y = -6; y <= 0; y++) {
            BlockPos support = base.offset(x, y, z);
            if (canPatchHousePerimeterGround(level.getBlockState(support))) {
                level.setBlock(support, Blocks.DIRT.defaultBlockState(), 2);
            }
        }
        level.setBlock(road, surface, 2);
        level.setBlock(road.above(), Blocks.AIR.defaultBlockState(), 2);
        level.setBlock(road.above(2), Blocks.AIR.defaultBlockState(), 2);
    }

    private static void stabilizeNaturalTerrainRect(ServerLevel level, BlockPos base, int minX, int maxX, int minZ, int maxZ) {
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                if (x >= HOUSE_ORIGIN_X - 1
                        && x <= IMPORTED_HOUSE_MAX_X + 1
                        && z >= HOUSE_ORIGIN_Z - 1
                        && z <= IMPORTED_HOUSE_MAX_Z + 1) {
                    continue;
                }
                fillHousePerimeterColumn(level, base, x, z);
            }
        }
    }

    private static void buildGreenVillageSquare(ServerLevel level, BlockPos base) {
        BlockPos center = filledGroundAt(level, base.offset(-106, -1, 34));
        for (int x = -18; x <= 18; x++) {
            for (int z = -14; z <= 14; z++) {
                BlockPos ground = center.offset(x, 0, z);
                level.setBlock(ground.below(), Blocks.DIRT.defaultBlockState(), 2);
                boolean edge = Math.abs(x) == 18 || Math.abs(z) == 14;
                boolean path = Math.abs(x) <= 2 || Math.abs(z) <= 2;
                level.setBlock(ground, edge ? Blocks.MOSSY_COBBLESTONE.defaultBlockState() : path ? Blocks.SMOOTH_STONE.defaultBlockState() : Blocks.GRASS_BLOCK.defaultBlockState(), 2);
                level.setBlock(ground.above(), Blocks.AIR.defaultBlockState(), 2);
                if (!path && !edge && Math.floorMod(x * 13 + z * 17, 11) == 0) {
                    level.setBlock(ground.above(), flowerFor(x + z), 2);
                }
            }
        }

        reinforceGreenSquareBell(level, center);
        buildCommunityHall(level, center.offset(-8, 0, -28));
        BlockPos greenHouseA = base.offset(-124, -1, 50);
        BlockPos greenHouseB = base.offset(-101, -1, 50);
        buildSimplePlantationWorkerHouse(level, greenHouseA, Direction.SOUTH, Direction.NORTH);
        buildSimplePlantationWorkerHouse(level, greenHouseB, Direction.SOUTH, Direction.NORTH);
        decorateWorkerHome(level, greenHouseA, 14, 10, "Casa Verde 1");
        decorateWorkerHome(level, greenHouseB, 14, 10, "Casa Verde 2");

        for (BlockPos lamp : new BlockPos[] {
                center.offset(-18, 0, -14), center.offset(18, 0, -14),
                center.offset(-18, 0, 14), center.offset(18, 0, 14),
                center.offset(0, 0, -14), center.offset(0, 0, 14)
        }) {
            placeLampPost(level, lamp);
        }

        spawnAnimalGroup(level, EntityType.CAT, center.offset(-7, 1, 8), 3);
        spawnAnimalGroup(level, EntityType.PARROT, center.offset(7, 1, 8), 3);
        decorateGreenSquareGarden(level, center);
        spawnWorkersForHouseBeds(level, greenHouseA, 14, 7, 10, "Trabalhador da Casa Verde 1", greenHouseA.offset(7, 1, 5), center, Items.EMERALD);
        spawnWorkersForHouseBeds(level, greenHouseB, 14, 7, 10, "Trabalhador da Casa Verde 2", greenHouseB.offset(7, 1, 5), center, Items.BREAD);
        spawnWorkerVillager(level, center.offset(-2, 1, 3), "Morador da Praca Verde 1", center, center, Items.EMERALD);
        spawnWorkerVillager(level, center.offset(2, 1, 3), "Morador da Praca Verde 2", center, center, Items.BREAD);
        spawnNamed(level, EntityType.IRON_GOLEM, center.offset(0, 1, 7), "Guardiao da Praca Verde");
    }

    private static void reinforceGreenSquareBell(ServerLevel level, BlockPos center) {
        level.setBlock(center.below(2), Blocks.DIRT.defaultBlockState(), 2);
        level.setBlock(center.below(), Blocks.POLISHED_DEEPSLATE.defaultBlockState(), 2);
        level.setBlock(center, Blocks.BELL.defaultBlockState(), 2);
        level.setBlock(center.offset(0, 1, 0), Blocks.LANTERN.defaultBlockState(), 2);
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            level.setBlock(center.relative(direction), Blocks.POLISHED_ANDESITE.defaultBlockState(), 2);
        }
    }

    private static void decorateGreenSquareGarden(ServerLevel level, BlockPos center) {
        for (BlockPos pos : new BlockPos[] {
                center.offset(-6, 1, -6), center.offset(6, 1, -6),
                center.offset(-6, 1, 6), center.offset(6, 1, 6),
                center.offset(-10, 1, 0), center.offset(10, 1, 0),
                center.offset(0, 1, -10), center.offset(0, 1, 10)
        }) {
            if (level.getBlockState(pos).isAir() && level.getBlockState(pos.below()).isSolid()) {
                level.setBlock(pos, Blocks.FLOWERING_AZALEA.defaultBlockState(), 2);
            }
        }
        for (BlockPos pos : new BlockPos[] {
                center.offset(-8, 1, -4), center.offset(8, 1, -4),
                center.offset(-8, 1, 4), center.offset(8, 1, 4),
                center.offset(-4, 1, -8), center.offset(4, 1, -8),
                center.offset(-4, 1, 8), center.offset(4, 1, 8)
        }) {
            if (level.getBlockState(pos).isAir() && level.getBlockState(pos.below()).isSolid()) {
                level.setBlock(pos, flowerFor(pos.getX() + pos.getZ()), 2);
            }
        }
        for (BlockPos light : new BlockPos[] {
                center.offset(-5, 1, -5), center.offset(5, 1, -5),
                center.offset(-5, 1, 5), center.offset(5, 1, 5)
        }) {
            level.setBlock(light, Blocks.SEA_LANTERN.defaultBlockState(), 2);
            if (level.getBlockState(light.above()).isAir()) {
                level.setBlock(light.above(), Blocks.LIGHT_BLUE_STAINED_GLASS.defaultBlockState(), 2);
            }
        }
        spawnNamed(level, EntityType.PARROT, center.offset(-9, 1, 9), "Arara da Praca Verde");
        spawnNamed(level, EntityType.PARROT, center.offset(9, 1, 9), "Papagaio da Praca Verde");
        spawnNamed(level, EntityType.PARROT, center.offset(-9, 1, -9), "Ave Rosa da Praca Verde");
        spawnNamed(level, EntityType.CHICKEN, center.offset(9, 1, -9), "Galinha Ornamental da Praca Verde");
        spawnNamed(level, EntityType.ALLAY, center.offset(0, 2, -8), "Brilho Alegre da Praca Verde");
    }

    private static void buildCommunityHall(ServerLevel level, BlockPos corner) {
        int width = 18;
        int depth = 14;
        for (int x = 0; x <= width; x++) {
            for (int z = 0; z <= depth; z++) {
                BlockPos pos = corner.offset(x, 0, z);
                boolean wall = x == 0 || x == width || z == 0 || z == depth;
                boolean post = (x == 0 || x == width) && (z == 0 || z == depth);
                level.setBlock(pos.below(), Blocks.COBBLESTONE.defaultBlockState(), 2);
                level.setBlock(pos, Blocks.SPRUCE_PLANKS.defaultBlockState(), 2);
                for (int y = 1; y <= 7; y++) {
                    level.setBlock(pos.above(y), wall ? (post ? Blocks.DARK_OAK_LOG.defaultBlockState() : Blocks.STRIPPED_OAK_LOG.defaultBlockState()) : Blocks.AIR.defaultBlockState(), 2);
                }
            }
        }
        for (int x = -2; x <= width + 2; x++) {
            for (int z = -2; z <= depth + 2; z++) {
                level.setBlock(corner.offset(x, 8, z), Blocks.DARK_OAK_PLANKS.defaultBlockState(), 2);
            }
        }
        placeHouseDoor(level, corner.offset(width / 2, 1, depth), Direction.SOUTH);
        placeHouseDoor(level, corner.offset(width / 2, 1, 0), Direction.NORTH);
        for (int x = 4; x <= width - 4; x += 5) {
            placeWindow(level, corner.offset(x, 3, 0));
            placeWindow(level, corner.offset(x, 3, depth));
        }
        level.setBlock(corner.offset(width / 2, 5, depth / 2), Blocks.LANTERN.defaultBlockState(), 2);
        level.setBlock(corner.offset(4, 1, 4), Blocks.LECTERN.defaultBlockState(), 2);
        level.setBlock(corner.offset(width - 4, 1, 4), Blocks.CARTOGRAPHY_TABLE.defaultBlockState(), 2);
        placeChest(level, corner.offset(width / 2, 1, 3), Direction.SOUTH);
        putItems(level, corner.offset(width / 2, 1, 3), new ItemStack(Items.MAP, 8), new ItemStack(Items.BREAD, 64), new ItemStack(Items.TORCH, 64));
    }

    private static void buildStoneTreasureMineHouse(ServerLevel level, BlockPos approximateCenter) {
        BlockPos center = approximateCenter;
        prepareMineHouseGround(level, center);
        buildTreasureMine(level, center);

        for (int x = -5; x <= 4; x++) {
            for (int z = -5; z <= 4; z++) {
                boolean wall = x == -5 || x == 4 || z == -5 || z == 4;
                level.setBlock(center.offset(x, 0, z), Blocks.POLISHED_DEEPSLATE.defaultBlockState(), 2);
                for (int y = 1; y <= 5; y++) {
                    level.setBlock(center.offset(x, y, z), wall ? mineHouseWallBlock(x, y, z) : Blocks.AIR.defaultBlockState(), 2);
                }
            }
        }
        for (int x = -6; x <= 5; x++) {
            for (int z = -6; z <= 5; z++) {
                level.setBlock(center.offset(x, 6, z), Blocks.DEEPSLATE_BRICKS.defaultBlockState(), 2);
            }
        }

        placeHouseDoor(level, center.offset(0, 1, -5), Direction.NORTH);
        for (int x : new int[] {-3, 2}) {
            placeWindow(level, center.offset(x, 2, -5));
            placeWindow(level, center.offset(x, 2, 4));
        }
        for (int z : new int[] {-3, 2}) {
            placeWindow(level, center.offset(-5, 2, z));
            placeWindow(level, center.offset(4, 2, z));
        }

        decorateMineHouse(level, center);
        reinforceStoneTreasureMineHouseShell(level, center);
    }

    private static void reinforceStoneTreasureMineHouseShell(ServerLevel level, BlockPos center) {
        for (int x = -5; x <= 4; x++) {
            for (int z = -5; z <= 4; z++) {
                boolean wall = x == -5 || x == 4 || z == -5 || z == 4;
                level.setBlock(center.offset(x, -1, z), Blocks.COBBLED_DEEPSLATE.defaultBlockState(), 2);
                level.setBlock(center.offset(x, 0, z), Blocks.POLISHED_DEEPSLATE.defaultBlockState(), 2);
                if (wall) {
                    for (int y = 1; y <= 5; y++) {
                        level.setBlock(center.offset(x, y, z), mineHouseWallBlock(x, y, z), 2);
                    }
                }
            }
        }
        for (int x = -6; x <= 5; x++) {
            for (int z = -6; z <= 5; z++) {
                level.setBlock(center.offset(x, 6, z), Blocks.DEEPSLATE_BRICKS.defaultBlockState(), 2);
                if (x == -6 || x == 5 || z == -6 || z == 5) {
                    Direction facing = z < 0 ? Direction.NORTH : Direction.SOUTH;
                    level.setBlock(center.offset(x, 7, z), Blocks.DEEPSLATE_BRICK_STAIRS.defaultBlockState()
                            .setValue(StairBlock.FACING, facing), 2);
                }
            }
        }
        placeHouseDoor(level, center.offset(0, 1, -5), Direction.NORTH);
        for (int x : new int[] {-3, 2}) {
            placeWindow(level, center.offset(x, 2, -5));
            placeWindow(level, center.offset(x, 2, 4));
        }
        for (int z : new int[] {-3, 2}) {
            placeWindow(level, center.offset(-5, 2, z));
            placeWindow(level, center.offset(4, 2, z));
        }
    }

    private static BlockState mineHouseWallBlock(int x, int y, int z) {
        if (y == 1 && Math.floorMod(x * 17 + z * 11, 5) == 0) {
            return Blocks.CHISELED_STONE_BRICKS.defaultBlockState();
        }
        if (y >= 4 && Math.floorMod(x + z, 4) == 0) {
            return Blocks.MOSSY_STONE_BRICKS.defaultBlockState();
        }
        return Blocks.STONE_BRICKS.defaultBlockState();
    }

    private static void prepareMineHouseGround(ServerLevel level, BlockPos center) {
        for (int x = -10; x <= 9; x++) {
            for (int z = -10; z <= 9; z++) {
                BlockPos ground = center.offset(x, 0, z);
                level.setBlock(ground.below(), Blocks.DIRT.defaultBlockState(), 2);
                level.setBlock(ground, Blocks.GRASS_BLOCK.defaultBlockState(), 2);
                for (int y = 1; y <= 10; y++) {
                    level.setBlock(ground.above(y), Blocks.AIR.defaultBlockState(), 2);
                }
            }
        }
    }

    private static void decorateMineHouse(ServerLevel level, BlockPos center) {
        BlockPos toolsChest = center.offset(-4, 1, -2);
        BlockPos richesChest = center.offset(-4, 1, 1);
        BlockPos buildingChest = center.offset(3, 1, -2);
        BlockPos foodChest = center.offset(3, 1, 1);
        placeChest(level, toolsChest, Direction.EAST);
        placeChest(level, richesChest, Direction.EAST);
        placeChest(level, buildingChest, Direction.WEST);
        placeChest(level, foodChest, Direction.WEST);
        putItems(level, toolsChest, new ItemStack(Items.DIAMOND_PICKAXE), new ItemStack(Items.DIAMOND_AXE),
                new ItemStack(Items.DIAMOND_SHOVEL), new ItemStack(Items.BOW), new ItemStack(Items.ARROW, 64), new ItemStack(Items.TORCH, 64));
        putItems(level, richesChest, treasureMineStacks());
        putItems(level, buildingChest, new ItemStack(Items.CHEST, 32), new ItemStack(Items.CRAFTING_TABLE, 16),
                new ItemStack(Items.FURNACE, 16), new ItemStack(Items.RAIL, 64), new ItemStack(Items.LANTERN, 32));
        putItems(level, foodChest, new ItemStack(Items.GOLDEN_CARROT, 64), new ItemStack(Items.COOKED_BEEF, 64),
                new ItemStack(Items.BREAD, 64), new ItemStack(Items.GOLDEN_APPLE, 16));

        level.setBlock(center.offset(-2, 1, 3), Blocks.CRAFTING_TABLE.defaultBlockState(), 2);
        level.setBlock(center.offset(-1, 1, 3), Blocks.FURNACE.defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, Direction.NORTH), 2);
        level.setBlock(center.offset(0, 1, 3), Blocks.BLAST_FURNACE.defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, Direction.NORTH), 2);
        level.setBlock(center.offset(1, 1, 3), Blocks.SMITHING_TABLE.defaultBlockState(), 2);
        level.setBlock(center.offset(2, 1, 3), Blocks.ANVIL.defaultBlockState(), 2);
        level.setBlock(center.offset(-3, 1, 3), Blocks.STONECUTTER.defaultBlockState(), 2);
        level.setBlock(center.offset(0, 5, 0), Blocks.SEA_LANTERN.defaultBlockState(), 2);

        for (int y = center.getY(); y >= center.getY() - 22; y--) {
            BlockPos shaft = new BlockPos(center.getX(), y, center.getZ());
            level.setBlock(shaft, Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING, Direction.NORTH), 2);
            level.setBlock(shaft.relative(Direction.SOUTH), Blocks.STONE_BRICKS.defaultBlockState(), 2);
        }

        for (BlockPos pos : new BlockPos[] {
                center.offset(-7, 0, -7), center.offset(6, 0, -7),
                center.offset(-7, 0, 6), center.offset(6, 0, 6)
        }) {
            placeLampPost(level, pos);
        }
        buildHousePathToFarm(level, center.offset(0, 1, -5), Direction.NORTH, 5);
        spawnStoneTreasureHouseArmor(level, center);
        decorateStoneTreasureMineExterior(level, center);
        spawnWorkerVillager(level, center.offset(0, 1, 2), "Minerador Magic World", center, center, Items.DIAMOND_PICKAXE);
    }

    private static void spawnStoneTreasureHouseArmor(ServerLevel level, BlockPos center) {
        spawnArmorStand(level, center.offset(-3, 1, -3), Items.LEATHER_HELMET, Items.LEATHER_CHESTPLATE, Items.LEATHER_LEGGINGS, Items.LEATHER_BOOTS, "Armadura de Couro");
        spawnArmorStand(level, center.offset(-1, 1, -3), Items.CHAINMAIL_HELMET, Items.CHAINMAIL_CHESTPLATE, Items.CHAINMAIL_LEGGINGS, Items.CHAINMAIL_BOOTS, "Armadura de Malha");
        spawnArmorStand(level, center.offset(1, 1, -3), Items.IRON_HELMET, Items.IRON_CHESTPLATE, Items.IRON_LEGGINGS, Items.IRON_BOOTS, "Armadura de Ferro");
        spawnArmorStand(level, center.offset(3, 1, -3), Items.GOLDEN_HELMET, Items.GOLDEN_CHESTPLATE, Items.GOLDEN_LEGGINGS, Items.GOLDEN_BOOTS, "Armadura de Ouro");
        spawnArmorStand(level, center.offset(-3, 1, 2), Items.DIAMOND_HELMET, Items.DIAMOND_CHESTPLATE, Items.DIAMOND_LEGGINGS, Items.DIAMOND_BOOTS, "Armadura de Diamante");
        spawnArmorStand(level, center.offset(3, 1, 2), Items.NETHERITE_HELMET, Items.NETHERITE_CHESTPLATE, Items.NETHERITE_LEGGINGS, Items.NETHERITE_BOOTS, "Armadura de Netherite");
    }

    private static void decorateStoneTreasureMineExterior(ServerLevel level, BlockPos center) {
        level.setBlock(center.offset(-4, 7, -6), Blocks.BLUE_BANNER.defaultBlockState(), 2);
        level.setBlock(center.offset(4, 7, -6), Blocks.PURPLE_BANNER.defaultBlockState(), 2);
        level.setBlock(center.offset(-6, 1, -7), Blocks.ANVIL.defaultBlockState(), 2);
        level.setBlock(center.offset(5, 1, -7), Blocks.SMITHING_TABLE.defaultBlockState(), 2);
        for (int x = -7; x <= 6; x += 13) {
            for (int z = -7; z <= 6; z += 13) {
                level.setBlock(center.offset(x, 1, z), Blocks.CHAIN.defaultBlockState(), 2);
                level.setBlock(center.offset(x, 2, z), Blocks.LANTERN.defaultBlockState(), 2);
            }
        }
    }

    private static void buildTreasureMine(ServerLevel level, BlockPos center) {
        int topY = center.getY() - 2;
        BlockState[] layers = new BlockState[] {
                Blocks.STONE.defaultBlockState(),
                Blocks.COAL_ORE.defaultBlockState(),
                Blocks.IRON_ORE.defaultBlockState(),
                Blocks.COPPER_ORE.defaultBlockState(),
                Blocks.REDSTONE_ORE.defaultBlockState(),
                Blocks.LAPIS_ORE.defaultBlockState(),
                Blocks.EMERALD_ORE.defaultBlockState(),
                Blocks.DIAMOND_ORE.defaultBlockState(),
                Blocks.DEEPSLATE_DIAMOND_ORE.defaultBlockState()
        };

        for (int layer = 0; layer < layers.length; layer++) {
            for (int depth = 0; depth < 2; depth++) {
                int y = topY - layer * 2 - depth;
                for (int x = -20; x <= 19; x++) {
                    for (int z = -20; z <= 19; z++) {
                        level.setBlock(new BlockPos(center.getX() + x, y, center.getZ() + z), layers[layer], 2);
                    }
                }
            }
        }

        for (int galleryY : new int[] {topY - 3, topY - 9, topY - 15}) {
            carveTreasureMineGallery(level, center, galleryY);
        }
    }

    private static void carveTreasureMineGallery(ServerLevel level, BlockPos center, int galleryY) {
        for (int x = -18; x <= 18; x++) {
            for (int z = -1; z <= 1; z++) {
                clearMineWalkway(level, center.offset(x, galleryY - center.getY(), z));
            }
            level.setBlock(center.offset(x, galleryY - center.getY(), 0), Blocks.RAIL.defaultBlockState(), 2);
        }
        for (int z = -18; z <= 18; z++) {
            for (int x = -1; x <= 1; x++) {
                clearMineWalkway(level, center.offset(x, galleryY - center.getY(), z));
            }
            if (Math.abs(z) > 2) {
                level.setBlock(center.offset(0, galleryY - center.getY(), z), Blocks.RAIL.defaultBlockState(), 2);
            }
        }
        for (int step = -16; step <= 16; step += 8) {
            placeMineSupport(level, center.offset(step, galleryY - center.getY(), -2));
            placeMineSupport(level, center.offset(step, galleryY - center.getY(), 2));
            placeMineSupport(level, center.offset(-2, galleryY - center.getY(), step));
            placeMineSupport(level, center.offset(2, galleryY - center.getY(), step));
            level.setBlock(center.offset(step, galleryY - center.getY() + 3, 0), Blocks.SEA_LANTERN.defaultBlockState(), 2);
            level.setBlock(center.offset(0, galleryY - center.getY() + 3, step), Blocks.SEA_LANTERN.defaultBlockState(), 2);
        }
        for (BlockPos pos : new BlockPos[] {
                center.offset(-15, galleryY - center.getY(), -15),
                center.offset(15, galleryY - center.getY(), -15),
                center.offset(-15, galleryY - center.getY(), 15),
                center.offset(15, galleryY - center.getY(), 15)
        }) {
            clearMineWalkway(level, pos);
            placeChest(level, pos, Direction.SOUTH);
            putItems(level, pos, treasureMineStacks());
        }
    }

    private static void clearMineWalkway(ServerLevel level, BlockPos foot) {
        level.setBlock(foot, Blocks.AIR.defaultBlockState(), 2);
        level.setBlock(foot.above(), Blocks.AIR.defaultBlockState(), 2);
        level.setBlock(foot.above(2), Blocks.AIR.defaultBlockState(), 2);
        level.setBlock(foot.below(), Blocks.POLISHED_DEEPSLATE.defaultBlockState(), 2);
    }

    private static void placeMineSupport(ServerLevel level, BlockPos foot) {
        for (int y = 0; y <= 2; y++) {
            level.setBlock(foot.above(y), Blocks.OAK_LOG.defaultBlockState(), 2);
        }
        level.setBlock(foot.above(3), Blocks.OAK_PLANKS.defaultBlockState(), 2);
    }

    private static ItemStack[] treasureMineStacks() {
        return new ItemStack[] {
                new ItemStack(Items.COAL, 64),
                new ItemStack(Items.RAW_IRON, 64),
                new ItemStack(Items.RAW_GOLD, 64),
                new ItemStack(Items.RAW_COPPER, 64),
                new ItemStack(Items.IRON_INGOT, 64),
                new ItemStack(Items.GOLD_INGOT, 64),
                new ItemStack(Items.DIAMOND, 32),
                new ItemStack(Items.EMERALD, 32),
                new ItemStack(Items.REDSTONE, 64),
                new ItemStack(Items.LAPIS_LAZULI, 64),
                new ItemStack(Items.TORCH, 64)
        };
    }

    private static void buildWorkerSettlement(ServerLevel level, BlockPos base) {
        BlockPos[] houses = {
                base.offset(-126, -1, -56),
                base.offset(-102, -1, -56),
                base.offset(106, 0, -72),
                base.offset(106, 0, -56)
        };
        for (int i = 0; i < houses.length; i++) {
            buildWorkerHouse(level, houses[i], i < 2 ? Direction.SOUTH : Direction.WEST);
            spawnNamed(level, EntityType.VILLAGER, houses[i].offset(4, 1, 4), "Trabalhador Magic World " + (i + 1));
            spawnNamed(level, EntityType.VILLAGER, houses[i].offset(6, 1, 4), "Cuidador Magic World " + (i + 1));
        }
    }

    private static void buildWorkerHouse(ServerLevel level, BlockPos corner, Direction doorFacing) {
        int width = 10;
        int depth = 8;
        for (int x = 0; x <= width; x++) {
            for (int z = 0; z <= depth; z++) {
                BlockPos pos = corner.offset(x, 0, z);
                boolean wall = x == 0 || x == width || z == 0 || z == depth;
                boolean post = (x == 0 || x == width) && (z == 0 || z == depth);
                level.setBlock(pos.below(), Blocks.COBBLESTONE.defaultBlockState(), 2);
                level.setBlock(pos, Blocks.OAK_PLANKS.defaultBlockState(), 2);
                for (int y = 1; y <= 4; y++) {
                    level.setBlock(pos.above(y), wall
                            ? (post ? Blocks.OAK_LOG.defaultBlockState() : Blocks.SPRUCE_PLANKS.defaultBlockState())
                            : Blocks.AIR.defaultBlockState(), 2);
                }
            }
        }
        for (int x = -1; x <= width + 1; x++) {
            for (int z = -1; z <= depth + 1; z++) {
                level.setBlock(corner.offset(x, 5, z), Blocks.DARK_OAK_PLANKS.defaultBlockState(), 2);
            }
        }
        BlockPos door = switch (doorFacing) {
            case NORTH -> corner.offset(width / 2, 1, 0);
            case SOUTH -> corner.offset(width / 2, 1, depth);
            case WEST -> corner.offset(0, 1, depth / 2);
            default -> corner.offset(width, 1, depth / 2);
        };
        level.setBlock(door, Blocks.AIR.defaultBlockState(), 2);
        level.setBlock(door.above(), Blocks.AIR.defaultBlockState(), 2);
        placeChest(level, corner.offset(2, 1, 2), Direction.SOUTH);
        putItems(level, corner.offset(2, 1, 2),
                new ItemStack(Items.BREAD, 32),
                new ItemStack(Items.WHEAT_SEEDS, 32),
                new ItemStack(Items.BONE_MEAL, 32),
                new ItemStack(Items.TORCH, 32));
        level.setBlock(corner.offset(8, 1, 2), Blocks.CRAFTING_TABLE.defaultBlockState(), 2);
        level.setBlock(corner.offset(8, 1, 4), Blocks.COMPOSTER.defaultBlockState(), 2);
        level.setBlock(corner.offset(5, 4, 4), Blocks.LANTERN.defaultBlockState(), 2);
    }

    private static void buildEstateLivingBorder(ServerLevel level, BlockPos base, int minX, int maxX, int minZ, int maxZ) {
        for (int x = minX; x <= maxX; x++) {
            placeEstateLivingBorderBlock(level, base.offset(x, 0, minZ), x, minZ, x == 0);
            placeEstateLivingBorderBlock(level, base.offset(x, 0, maxZ), x, maxZ, x == 0);
        }
        for (int z = minZ; z <= maxZ; z++) {
            placeEstateLivingBorderBlock(level, base.offset(minX, 0, z), minX, z, z == 0);
            placeEstateLivingBorderBlock(level, base.offset(maxX, 0, z), maxX, z, z == 0);
        }
    }

    private static void placeEstateLivingBorderBlock(ServerLevel level, BlockPos ground, int xRel, int zRel, boolean mainOpening) {
        level.setBlock(ground.below(), Blocks.DIRT.defaultBlockState(), 2);
        level.setBlock(ground, Blocks.GRASS_BLOCK.defaultBlockState(), 2);
        for (int y = 1; y <= 5; y++) {
            level.setBlock(ground.above(y), Blocks.AIR.defaultBlockState(), 2);
        }

        if (mainOpening || isEstateLivingBorderGap(xRel, zRel)) {
            return;
        }

        int variant = Math.floorMod(xRel * 31 + zRel * 17 + xRel * zRel, 12);
        if (variant <= 2) {
            return;
        }
        if (variant <= 5) {
            level.setBlock(ground.above(), flowerFor(xRel + zRel), 2);
            return;
        }
        if (variant <= 8) {
            placeEstateShrub(level, ground, xRel, zRel);
            return;
        }
        if (variant == 9) {
            level.setBlock(ground.above(), flowerFor(xRel - zRel), 2);
            placeEstateShrub(level, ground.relative(borderAccentDirection(xRel, zRel)), xRel + 3, zRel - 3);
            return;
        }
        placeEstateBorderTree(level, ground, xRel, zRel);
    }

    private static boolean isEstateLivingBorderGap(int xRel, int zRel) {
        int pattern = Math.floorMod(xRel * 7 + zRel * 11, 23);
        return pattern <= 2 || Math.floorMod(xRel + zRel, 37) == 0;
    }

    private static Direction borderAccentDirection(int xRel, int zRel) {
        if (Math.abs(xRel) > Math.abs(zRel)) {
            return xRel < 0 ? Direction.EAST : Direction.WEST;
        }
        return zRel < 0 ? Direction.SOUTH : Direction.NORTH;
    }

    private static void placeEstateShrub(ServerLevel level, BlockPos ground, int xRel, int zRel) {
        BlockState shrub = Blocks.CHERRY_LEAVES.defaultBlockState();
        level.setBlock(ground.above(), shrub, 2);
        if (Math.floorMod(xRel - zRel, 5) == 0) {
            level.setBlock(ground.above(2), shrub, 2);
        }
    }

    private static void placeEstateBorderTree(ServerLevel level, BlockPos ground, int xRel, int zRel) {
        int height = 3 + Math.floorMod(xRel + zRel, 2);
        for (int y = 1; y <= height; y++) {
            level.setBlock(ground.above(y), Blocks.CHERRY_LOG.defaultBlockState(), 2);
        }
        BlockState leaves = Blocks.CHERRY_LEAVES.defaultBlockState();
        BlockPos crown = ground.above(height);
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            level.setBlock(crown.relative(direction), leaves, 2);
        }
        level.setBlock(crown.above(), leaves, 2);
    }

    private static void spawnImportedStarterAnimals(ServerLevel level, BlockPos base) {
        ensureAnimalPenPopulations(level, base);
    }

    private static void ensureAnimalPenPopulations(ServerLevel level, BlockPos base) {
        EntityType<?>[] species = {
                EntityType.COW, EntityType.PIG, EntityType.SHEEP,
                EntityType.CHICKEN, EntityType.HORSE, EntityType.GOAT
        };
        String[] speciesNames = {"Vaca", "Porco", "Ovelha", "Galinha", "Cavalo", "Cabra"};
        BlockPos[] corners = animalPenCorners(base);
        for (int pen = 0; pen < corners.length; pen++) {
            BlockPos center = corners[pen].offset(7, 0, 6);
            for (int adult = 1; adult <= 3; adult++) {
                spawnBreedingAnimal(level, species[pen], center.offset(adult - 2, 1, -1),
                        speciesNames[pen] + " Curral " + (pen + 1) + " Adulto " + adult, false);
            }
            for (int baby = 1; baby <= 2; baby++) {
                spawnBreedingAnimal(level, species[pen], center.offset(baby - 1, 1, 1),
                        speciesNames[pen] + " Curral " + (pen + 1) + " Filhote " + baby, true);
            }
        }
    }

    private static void spawnBreedingAnimal(ServerLevel level, EntityType<?> type, BlockPos pos, String name, boolean baby) {
        AABB nearby = new AABB(pos).inflate(256.0D, 64.0D, 256.0D);
        if (!level.getEntitiesOfClass(Entity.class, nearby, entity -> name.equals(entity.getName().getString())).isEmpty()) {
            return;
        }
        Entity entity = type.spawn(level, pos, MobSpawnType.STRUCTURE);
        if (entity == null) {
            return;
        }
        entity.setCustomName(Component.literal(name));
        entity.setCustomNameVisible(false);
        entity.setInvulnerable(true);
        if (entity instanceof Mob mob) {
            mob.setPersistenceRequired();
        }
        if (entity instanceof AgeableMob ageable) {
            ageable.setAge(baby ? -24000 : 0);
        }
    }

    private static void encourageAnimalPenBreeding(ServerLevel level, BlockPos base) {
        if (Math.floorMod(level.getGameTime(), 2400L) >= 200L) {
            return;
        }
        for (BlockPos corner : animalPenCorners(base)) {
            AABB pen = new AABB(corner.offset(1, 0, 1), corner.offset(13, 4, 11));
            List<Animal> animals = level.getEntitiesOfClass(Animal.class, pen);
            if (animals.size() >= 9) {
                continue;
            }
            List<Animal> adults = animals.stream().filter(animal -> animal.getAge() == 0).toList();
            for (int i = 0; i < Math.min(2, adults.size()); i++) {
                adults.get(i).setInLove(null);
            }
        }
    }

    private static void buildStarterPortal(ServerLevel level, BlockPos center) {
        clearFlatArea(level, center.offset(-16, 0, -16), 33, 33, 12);

        for (int x = -3; x <= 3; x++) {
            for (int z = -2; z <= 2; z++) {
                boolean edge = Math.abs(x) == 3 || Math.abs(z) == 2;
                level.setBlock(center.offset(x, -1, z), Blocks.DIRT.defaultBlockState(), 2);
                level.setBlock(center.offset(x, 0, z), edge ? Blocks.POLISHED_ANDESITE.defaultBlockState() : Blocks.SMOOTH_STONE.defaultBlockState(), 2);
            }
        }

        for (int y = 1; y <= 5; y++) {
            level.setBlock(center.offset(-2, y, 0), Blocks.STRIPPED_OAK_LOG.defaultBlockState(), 2);
            level.setBlock(center.offset(2, y, 0), Blocks.STRIPPED_OAK_LOG.defaultBlockState(), 2);
        }
        for (int x = -3; x <= 3; x++) {
            level.setBlock(center.offset(x, 5, 0), Blocks.OAK_PLANKS.defaultBlockState(), 2);
            level.setBlock(center.offset(x, 6, 0), Blocks.DARK_OAK_SLAB.defaultBlockState(), 2);
        }
        for (int y = 1; y <= 4; y++) {
            level.setBlock(center.offset(-1, y, 0), Blocks.PURPLE_STAINED_GLASS.defaultBlockState(), 2);
            level.setBlock(center.offset(1, y, 0), Blocks.MAGENTA_STAINED_GLASS.defaultBlockState(), 2);
            level.setBlock(center.offset(0, y, 0), Blocks.AIR.defaultBlockState(), 2);
        }
        level.setBlock(center.offset(-1, 0, 0), Blocks.AMETHYST_BLOCK.defaultBlockState(), 2);
        level.setBlock(center.offset(0, 0, 0), Blocks.END_PORTAL_FRAME.defaultBlockState(), 2);
        level.setBlock(center.offset(1, 0, 0), Blocks.AMETHYST_BLOCK.defaultBlockState(), 2);
        decoratePortalGarden(level, center);
        clearPortalGrassRing(level, center);
        placeLampPost(level, center.offset(-10, 1, -10));
        placeLampPost(level, center.offset(10, 1, -10));
        placeMagicWorldGearChest(level, center.offset(-10, 0, 6));
        placeMagicWorldGearChest(level, center.offset(10, 0, 6));
    }

    private static void clearPortalGrassRing(ServerLevel level, BlockPos center) {
        for (int x = -8; x <= 8; x++) {
            for (int z = -8; z <= 8; z++) {
                if (Math.abs(x) <= 3 && Math.abs(z) <= 2) {
                    continue;
                }
                BlockPos ground = center.offset(x, 0, z);
                level.setBlock(ground, Blocks.GRASS_BLOCK.defaultBlockState(), 2);
                for (int y = 1; y <= 10; y++) {
                    if (!isProtectedGeneratedBlock(level.getBlockState(ground.above(y)))) {
                        level.setBlock(ground.above(y), Blocks.AIR.defaultBlockState(), 2);
                    }
                }
            }
        }
    }

    private static void decoratePortalGarden(ServerLevel level, BlockPos center) {
        for (int x = -20; x <= 20; x++) {
            for (int z = -16; z <= 16; z++) {
                if (Math.abs(x) <= 6 && Math.abs(z) <= 4) {
                    continue;
                }
                BlockPos ground = center.offset(x, 0, z);
                if ((x * x + z * z) % 5 == 0) {
                    level.setBlock(ground, Blocks.GRASS_BLOCK.defaultBlockState(), 2);
                    level.setBlock(ground.above(), flowerFor(x + z), 2);
                } else if ((x + z) % 13 == 0) {
                    level.setBlock(ground, Blocks.MOSS_BLOCK.defaultBlockState(), 2);
                    level.setBlock(ground.above(), Blocks.FERN.defaultBlockState(), 2);
                }
            }
        }

        for (BlockPos pos : new BlockPos[] {
                center.offset(-18, 0, -14), center.offset(18, 0, -14),
                center.offset(-18, 0, 14), center.offset(18, 0, 14),
                center.offset(-12, 0, 0), center.offset(12, 0, 0),
                center.offset(0, 0, -14), center.offset(0, 0, 14)
        }) {
            placeLampPost(level, pos);
            level.setBlock(pos.above(2).relative(Direction.NORTH), Blocks.VINE.defaultBlockState(), 2);
        }

        for (int x = -12; x <= 12; x += 6) {
            level.setBlock(center.offset(x, 0, 12), Blocks.MOSSY_COBBLESTONE.defaultBlockState(), 2);
            level.setBlock(center.offset(x, 1, 12), Blocks.FLOWERING_AZALEA.defaultBlockState(), 2);
            level.setBlock(center.offset(x, 0, -12), Blocks.MOSSY_COBBLESTONE.defaultBlockState(), 2);
            level.setBlock(center.offset(x, 1, -12), Blocks.AZALEA.defaultBlockState(), 2);
        }

        for (BlockPos pos : new BlockPos[] {
                center.offset(-12, 1, 8), center.offset(12, 1, 8),
                center.offset(-14, 1, -6), center.offset(14, 1, -6),
                center.offset(0, 1, 10)
        }) {
            level.setBlock(pos, Blocks.CAMPFIRE.defaultBlockState(), 2);
            level.setBlock(pos.above(), Blocks.LIGHT_BLUE_STAINED_GLASS.defaultBlockState(), 2);
        }

        spawnNamed(level, EntityType.PARROT, center.offset(-9, 1, 7), "Passaro Azul do Portal");
        spawnNamed(level, EntityType.PARROT, center.offset(9, 1, 7), "Passaro Verde do Portal");
        spawnNamed(level, EntityType.PARROT, center.offset(-11, 1, -7), "Passaro Encantado");
        spawnNamed(level, EntityType.RABBIT, center.offset(6, 1, 10), "Coelho do Jardim");
        spawnNamed(level, EntityType.RABBIT, center.offset(-6, 1, 10), "Coelho Brilhante");
        spawnNamed(level, EntityType.ALLAY, center.offset(0, 2, 8), "Brilho do Portal");
    }

    private static void buildFunctionalPortalPlaza(ServerLevel level, BlockPos base) {
        BlockPos center = findHighestFeatureSurface(level, compactPortalPlazaCenter(base), 80);
        clearFlatArea(level, center.offset(-14, 0, -8), 29, 17, 8);

        for (int x = -14; x <= 14; x++) {
            for (int z = -8; z <= 8; z++) {
                BlockPos ground = center.offset(x, -1, z);
                boolean plazaFloor = Math.abs(x) <= 12 && Math.abs(z) <= 6;
                boolean plazaEdge = Math.abs(x) == 12 || Math.abs(z) == 6;
                level.setBlock(ground.below(), Blocks.DIRT.defaultBlockState(), 2);
                level.setBlock(ground, plazaFloor
                        ? (plazaEdge ? Blocks.PURPUR_BLOCK.defaultBlockState() : Blocks.AMETHYST_BLOCK.defaultBlockState())
                        : Blocks.GRASS_BLOCK.defaultBlockState(), 2);
                if (!plazaFloor && Math.floorMod(x * 17 + z * 11, 13) == 0) {
                    level.setBlock(ground.above(), flowerFor(x + z), 2);
                }
            }
        }

        buildNetherPortalInMagicArea(level, netherPortalCenter(center));
        buildEndPortalInMagicArea(level, endPortalCenter(center));
        buildEndGatewayInMagicArea(level, gatewayPortalCenter(center));
        buildPortalMagicBeacons(level, center);
        for (BlockPos pos : new BlockPos[] {
                center.offset(-13, 0, -7), center.offset(13, 0, -7),
                center.offset(-13, 0, 7), center.offset(13, 0, 7)
        }) {
            placeLampPost(level, pos);
        }
        for (BlockPos pos : new BlockPos[] {
                center.offset(-9, 1, 6), center.offset(9, 1, 6),
                center.offset(-9, 1, -6), center.offset(9, 1, -6)
        }) {
            level.setBlock(pos.below(), Blocks.AMETHYST_BLOCK.defaultBlockState(), 2);
            level.setBlock(pos, Blocks.CAMPFIRE.defaultBlockState(), 2);
            level.setBlock(pos.above(), Blocks.PURPLE_STAINED_GLASS.defaultBlockState(), 2);
        }
    }

    private static void buildNetherPortalInMagicArea(ServerLevel level, BlockPos center) {
        BlockState portal = Blocks.NETHER_PORTAL.defaultBlockState().setValue(NetherPortalBlock.AXIS, Direction.Axis.X);
        for (int x = -2; x <= 2; x++) {
            for (int y = 0; y <= 5; y++) {
                boolean frame = x == -2 || x == 2 || y == 0 || y == 5;
                level.setBlock(center.offset(x, y, 0), frame ? Blocks.OBSIDIAN.defaultBlockState() : portal, 2);
            }
        }
        level.setBlock(center.offset(0, -1, 0), Blocks.CRYING_OBSIDIAN.defaultBlockState(), 2);
    }

    private static void buildEndPortalInMagicArea(ServerLevel level, BlockPos center) {
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                BlockPos pos = center.offset(x, 0, z);
                level.setBlock(pos.below(), Blocks.END_STONE.defaultBlockState(), 2);
                if (Math.abs(x) <= 1 && Math.abs(z) <= 1) {
                    level.setBlock(pos, Blocks.END_PORTAL.defaultBlockState(), 2);
                } else if (isEndPortalFrameSlot(x, z)) {
                    level.setBlock(pos, endPortalFrameState(x, z), 2);
                } else {
                    level.setBlock(pos, Blocks.END_STONE.defaultBlockState(), 2);
                }
                level.setBlock(pos.above(), Blocks.AIR.defaultBlockState(), 2);
            }
        }
    }

    private static void buildEndGatewayInMagicArea(ServerLevel level, BlockPos center) {
        for (int x = -2; x <= 2; x++) {
            for (int y = -2; y <= 2; y++) {
                for (int z = -2; z <= 2; z++) {
                    BlockPos pos = center.offset(x, y, z);
                    boolean gateway = x == 0 && y == 0 && z == 0;
                    boolean bedrock = (x == 0 && z == 0 && Math.abs(y) == 2)
                            || (y == 0 && Math.abs(x) + Math.abs(z) == 2);
                    if (gateway) {
                        level.setBlock(pos, Blocks.END_GATEWAY.defaultBlockState(), 2);
                    } else if (bedrock) {
                        level.setBlock(pos, Blocks.BEDROCK.defaultBlockState(), 2);
                    } else if (y == -2) {
                        level.setBlock(pos, Blocks.END_STONE.defaultBlockState(), 2);
                    } else {
                        level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
                    }
                }
            }
        }
    }

    private static boolean isEndPortalFrameSlot(int x, int z) {
        return (Math.abs(x) == 2 && Math.abs(z) <= 1)
                || (Math.abs(z) == 2 && Math.abs(x) <= 1);
    }

    private static BlockState endPortalFrameState(int x, int z) {
        Direction facing;
        if (z == -2) {
            facing = Direction.SOUTH;
        } else if (z == 2) {
            facing = Direction.NORTH;
        } else if (x == -2) {
            facing = Direction.EAST;
        } else {
            facing = Direction.WEST;
        }
        return Blocks.END_PORTAL_FRAME.defaultBlockState()
                .setValue(EndPortalFrameBlock.FACING, facing)
                .setValue(EndPortalFrameBlock.HAS_EYE, true);
    }

    private static void handleFunctionalEstatePortals(ServerPlayer player) {
        if (!(player.level() instanceof ServerLevel level)
                || !MagicWorldWorldOptions.isStarterEstateEnabled()) {
            return;
        }

        BlockPos estateBase = estateBaseFromPlayer(player);
        if (level.dimension().equals(Level.OVERWORLD)) {
            BlockPos plaza = findHighestFeatureSurface(level, compactPortalPlazaCenter(estateBase), 80);
            if (player.tickCount % 100 == 0) {
                ensureFunctionalPortalVisual(level, netherPortalCenter(plaza), FunctionalPortalKind.NETHER);
                ensureFunctionalPortalVisual(level, endPortalCenter(plaza), FunctionalPortalKind.END_PORTAL);
                ensureFunctionalPortalVisual(level, gatewayPortalCenter(plaza), FunctionalPortalKind.END_GATEWAY);
            }
        }

        long cooldownUntil = player.getPersistentData().getLong(PORTAL_COOLDOWN_KEY);
        if (level.getGameTime() < cooldownUntil) {
            return;
        }

        if (level.dimension().equals(Level.OVERWORLD)) {
            handleOverworldFunctionalPortal(player, estateBase);
        } else if (level.dimension().equals(Level.NETHER)) {
            handleReturnPortal(player, estateBase, FunctionalPortalKind.NETHER);
        } else if (level.dimension().equals(Level.END)) {
            handleReturnPortal(player, estateBase, FunctionalPortalKind.END_PORTAL);
            handleReturnPortal(player, estateBase, FunctionalPortalKind.END_GATEWAY);
        }
    }

    private static void handleOverworldFunctionalPortal(ServerPlayer player, BlockPos estateBase) {
        ServerLevel level = player.serverLevel();
        BlockPos plaza = findHighestFeatureSurface(level, compactPortalPlazaCenter(estateBase), 80);
        if (isPlayerInsideVerticalPortal(player, netherPortalCenter(plaza), 4.0D)) {
            teleportToFunctionalPortalDestination(player, Level.NETHER, estateBase.offset(0, 0, 128), FunctionalPortalKind.NETHER);
        } else if (isPlayerNearFlatPortal(player, endPortalCenter(plaza), 3.5D)) {
            teleportToFunctionalPortalDestination(player, Level.END, estateBase.offset(96, 0, 96), FunctionalPortalKind.END_PORTAL);
        } else if (isPlayerNearFlatPortal(player, gatewayPortalCenter(plaza), 3.5D)) {
            teleportToFunctionalPortalDestination(player, Level.END, estateBase.offset(-96, 0, 96), FunctionalPortalKind.END_GATEWAY);
        }
    }

    private static void handleReturnPortal(ServerPlayer player, BlockPos estateBase, FunctionalPortalKind kind) {
        BlockPos returnPortal = savedReturnPortal(player, kind);
        if (returnPortal == null) {
            return;
        }
        ensureFunctionalPortalVisual(player.serverLevel(), returnPortal, kind);
        if (isPlayerNearPortal(player, returnPortal, 4.5D) || isPlayerTouchingFunctionalPortalBlock(player, kind, 4)) {
            teleportBackToEstate(player, estateBase);
        }
    }

    private static void teleportToFunctionalPortalDestination(
            ServerPlayer player,
            ResourceKey<Level> dimension,
            BlockPos requested,
            FunctionalPortalKind kind
    ) {
        ServerLevel targetLevel = player.server.getLevel(dimension);
        if (targetLevel == null) {
            return;
        }

        BlockPos returnPortal = savedReturnPortal(player, kind);
        if (returnPortal == null) {
            returnPortal = resolveFunctionalSpawn(targetLevel, requested).north(4);
            saveReturnPortal(player, kind, returnPortal);
        }

        BlockPos spawn = returnPortal.south(4);
        buildReturnPortalPlatform(targetLevel, returnPortal, kind);
        setPortalCooldown(player);
        player.teleportTo(
                targetLevel,
                spawn.getX() + 0.5D,
                spawn.getY(),
                spawn.getZ() + 0.5D,
                Set.of(),
                player.getYRot(),
                player.getXRot()
        );
    }

    private static void teleportBackToEstate(ServerPlayer player, BlockPos estateBase) {
        ServerLevel overworld = player.server.getLevel(Level.OVERWORLD);
        if (overworld == null) {
            return;
        }

        BlockPos plaza = findHighestFeatureSurface(overworld, compactPortalPlazaCenter(estateBase), 80);
        setPortalCooldown(player);
        player.teleportTo(
                overworld,
                plaza.getX() + 0.5D,
                plaza.getY(),
                plaza.getZ() + 7.5D,
                Set.of(),
                player.getYRot(),
                player.getXRot()
        );
    }

    private static void teleportPlayerToEstateSpawn(ServerPlayer player, ServerLevel level, BlockPos base) {
        BlockPos spawn = findEstateSpawn(level, base);
        player.setRespawnPosition(Level.OVERWORLD, spawn, player.getYRot(), true, false);
        player.teleportTo(
                level,
                spawn.getX() + 0.5D,
                spawn.getY(),
                spawn.getZ() + 0.5D,
                Set.of(),
                player.getYRot(),
                player.getXRot()
        );
    }

    private static BlockPos findEstateSpawn(ServerLevel level, BlockPos base) {
        BlockPos preferred = base.offset(0, 1, 0);
        BlockPos safe = findSafeInteriorFloor(level, preferred, 18, 10);
        if (safe != null) {
            return safe;
        }

        int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, base.getX(), base.getZ());
        return new BlockPos(base.getX(), y, base.getZ());
    }

    private static void buildReturnPortalPlatform(ServerLevel level, BlockPos center, FunctionalPortalKind kind) {
        clearFlatArea(level, center.offset(-6, -1, -6), 13, 13, 8);
        BlockState floor = kind == FunctionalPortalKind.NETHER
                ? Blocks.NETHERRACK.defaultBlockState()
                : Blocks.END_STONE.defaultBlockState();
        BlockState border = kind == FunctionalPortalKind.NETHER
                ? Blocks.BLACKSTONE.defaultBlockState()
                : Blocks.PURPUR_BLOCK.defaultBlockState();
        for (int x = -5; x <= 5; x++) {
            for (int z = -5; z <= 5; z++) {
                boolean edge = Math.abs(x) == 5 || Math.abs(z) == 5;
                level.setBlock(center.offset(x, -1, z), edge ? border : floor, 2);
                level.setBlock(center.offset(x, 0, z), Blocks.AIR.defaultBlockState(), 2);
                level.setBlock(center.offset(x, 1, z), Blocks.AIR.defaultBlockState(), 2);
            }
        }
        ensureFunctionalPortalVisual(level, center, kind);
    }

    private static void ensureFunctionalPortalVisual(ServerLevel level, BlockPos center, FunctionalPortalKind kind) {
        if (isFunctionalPortalVisualComplete(level, center, kind)) {
            return;
        }
        if (kind == FunctionalPortalKind.NETHER) {
            buildNetherPortalInMagicArea(level, center);
        } else if (kind == FunctionalPortalKind.END_PORTAL) {
            buildEndPortalInMagicArea(level, center);
        } else {
            buildEndGatewayInMagicArea(level, center);
        }
    }

    private static boolean isFunctionalPortalVisualComplete(ServerLevel level, BlockPos center, FunctionalPortalKind kind) {
        return switch (kind) {
            case NETHER -> level.getBlockState(center.above()).is(Blocks.NETHER_PORTAL)
                    && level.getBlockState(center.offset(-2, 2, 0)).is(Blocks.OBSIDIAN)
                    && level.getBlockState(center.offset(2, 2, 0)).is(Blocks.OBSIDIAN);
            case END_PORTAL -> level.getBlockState(center).is(Blocks.END_PORTAL)
                    && level.getBlockState(center.north()).is(Blocks.END_PORTAL)
                    && level.getBlockState(center.south()).is(Blocks.END_PORTAL)
                    && level.getBlockState(center.east()).is(Blocks.END_PORTAL)
                    && level.getBlockState(center.west()).is(Blocks.END_PORTAL);
            case END_GATEWAY -> level.getBlockState(center).is(Blocks.END_GATEWAY)
                    && level.getBlockState(center.above(2)).is(Blocks.BEDROCK)
                    && level.getBlockState(center.below(2)).is(Blocks.BEDROCK);
        };
    }

    private static boolean isPlayerNearPortal(ServerPlayer player, BlockPos center, double radius) {
        return player.position().distanceToSqr(center.getX() + 0.5D, center.getY() + 0.5D, center.getZ() + 0.5D) <= radius * radius;
    }

    private static boolean isPlayerInsideVerticalPortal(ServerPlayer player, BlockPos center, double radius) {
        return Math.abs(player.getX() - (center.getX() + 0.5D)) <= radius
                && Math.abs(player.getZ() - (center.getZ() + 0.5D)) <= radius
                && player.getY() >= center.getY()
                && player.getY() <= center.getY() + 6.5D;
    }

    private static boolean isPlayerNearFlatPortal(ServerPlayer player, BlockPos center, double radius) {
        return Math.abs(player.getX() - (center.getX() + 0.5D)) <= radius
                && Math.abs(player.getZ() - (center.getZ() + 0.5D)) <= radius
                && Math.abs(player.getY() - center.getY()) <= 3.0D;
    }

    private static boolean isPlayerTouchingFunctionalPortalBlock(ServerPlayer player, FunctionalPortalKind kind, int radius) {
        ServerLevel level = player.serverLevel();
        BlockPos center = player.blockPosition();
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-radius, -2, -radius), center.offset(radius, 3, radius))) {
            if (isFunctionalPortalBlock(level.getBlockState(pos), kind)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isFunctionalPortalBlock(BlockState state, FunctionalPortalKind kind) {
        return switch (kind) {
            case NETHER -> state.is(Blocks.NETHER_PORTAL);
            case END_PORTAL -> state.is(Blocks.END_PORTAL) || state.is(Blocks.END_PORTAL_FRAME);
            case END_GATEWAY -> state.is(Blocks.END_GATEWAY);
        };
    }

    private static BlockPos resolveFunctionalSpawn(ServerLevel level, BlockPos requested) {
        BlockPos surface = findHighestFeatureSurface(level, requested, 192);
        return surface == null ? new BlockPos(requested.getX(), Math.max(level.getMinBuildHeight() + 8, 80), requested.getZ()) : surface;
    }

    private static BlockPos savedReturnPortal(ServerPlayer player, FunctionalPortalKind kind) {
        CompoundTag data = player.getPersistentData();
        String xKey = returnPortalKey(kind, "X");
        String yKey = returnPortalKey(kind, "Y");
        String zKey = returnPortalKey(kind, "Z");
        if (!data.contains(xKey) || !data.contains(yKey) || !data.contains(zKey)) {
            return null;
        }
        return new BlockPos(data.getInt(xKey), data.getInt(yKey), data.getInt(zKey));
    }

    private static void saveReturnPortal(ServerPlayer player, FunctionalPortalKind kind, BlockPos pos) {
        CompoundTag data = player.getPersistentData();
        data.putInt(returnPortalKey(kind, "X"), pos.getX());
        data.putInt(returnPortalKey(kind, "Y"), pos.getY());
        data.putInt(returnPortalKey(kind, "Z"), pos.getZ());
    }

    private static String returnPortalKey(FunctionalPortalKind kind, String axis) {
        return RETURN_PORTAL_PREFIX + kind.name() + axis;
    }

    private static void setPortalCooldown(ServerPlayer player) {
        player.getPersistentData().putLong(PORTAL_COOLDOWN_KEY, player.level().getGameTime() + 80L);
    }

    private static void handlePremiumPortalToggle(ServerPlayer player) {
        if (player.tickCount % 10 != 0 || !(player.level() instanceof ServerLevel level)) {
            return;
        }
        BlockPos marker = findNearestStarterPortalMarker(level, player.blockPosition(), 3);
        if (marker == null) {
            PLAYERS_TOUCHING_STARTER_PORTAL.remove(player.getUUID());
            return;
        }

        if (PLAYERS_TOUCHING_STARTER_PORTAL.add(player.getUUID())) {
            activatePremiumPortal(player, marker);
        }
    }

    private static void activatePremiumPortal(ServerPlayer player, BlockPos marker) {
        long cooldownUntil = player.getPersistentData().getLong(PORTAL_COOLDOWN_KEY);
        if (player.level().getGameTime() < cooldownUntil) {
            return;
        }
        setPortalCooldown(player);
        boolean wasActive = player.getPersistentData().getBoolean(PREMIUM_UNLOCKED_KEY);
        player.getPersistentData().putBoolean(PREMIUM_UNLOCKED_KEY, true);

        if (!wasActive) {
            player.sendSystemMessage(Component.literal("Magic World: experiencia premium ativada."));
        }
        player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 20 * 60, 0));
        player.addEffect(new MobEffectInstance(MobEffects.LUCK, 20 * 60, 0));

        if (player.level() instanceof ServerLevel level) {
            MagicWorld.effects(level, marker);
        }
    }

    private static void handleAmbientEffects(ServerPlayer player) {
        if (player.tickCount % 80 != 0 || !(player.level() instanceof ServerLevel level)) {
            return;
        }
        BlockPos portal = findNearestStarterPortalMarker(level, player.blockPosition(), 64);
        if (portal != null) {
            MagicWorld.effects(level, portal);
        }
        handleWitchCovenSupport(player, level);
    }

    private static void handleWitchCovenSupport(ServerPlayer player, ServerLevel level) {
        if (!level.dimension().equals(Level.OVERWORLD)
                || !player.getPersistentData().getBoolean(ESTATE_CREATED_KEY)) {
            return;
        }

        BlockPos anchor = witchCovenAnchor(estateBaseFromPlayer(player));
        if (player.blockPosition().distSqr(anchor) > 72.0D * 72.0D) {
            return;
        }

        player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 20 * 12, 0, true, true));
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 20 * 12, 0, true, true));
        player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 20 * 16, 0, true, true));
        player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 20 * 24, 0, true, true));
        player.addEffect(new MobEffectInstance(MobEffects.LUCK, 20 * 24, 1, true, true));

        AABB covenArea = new AABB(anchor).inflate(56.0D, 32.0D, 56.0D);
        for (Monster monster : level.getEntitiesOfClass(Monster.class, covenArea,
                monster -> !monster.getPersistentData().getBoolean(FRIENDLY_WITCH_KEY))) {
            monster.discard();
        }
        for (Witch witch : level.getEntitiesOfClass(Witch.class, covenArea,
                witch -> witch.getPersistentData().getBoolean(FRIENDLY_WITCH_KEY))) {
            witch.setTarget(null);
            witch.setNoAi(true);
            witch.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 20 * 20, 1, true, false));
            witch.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 20 * 20, 4, true, false));
            witch.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 20 * 20, 0, true, false));
        }
    }

    private static void fillStarterChests(ServerLevel level, BlockPos base) {
        BlockPos[] chests = {
                placeSafeChest(level, base.offset(-8, 1, -6), Direction.SOUTH),
                placeSafeChest(level, base.offset(4, 1, -6), Direction.SOUTH),
                placeSafeChest(level, base.offset(-8, 1, 6), Direction.NORTH),
                placeSafeChest(level, base.offset(4, 1, 6), Direction.NORTH)
        };
        putItems(level, chests[0],
                new ItemStack(Items.IRON_SWORD),
                new ItemStack(Items.IRON_PICKAXE),
                new ItemStack(Items.IRON_AXE),
                new ItemStack(Items.SHIELD),
                new ItemStack(Items.BOW),
                new ItemStack(Items.ARROW, 64),
                new ItemStack(MagicWorld.VARINHA_MAGICA.get()),
                new ItemStack(MagicWorld.DRACONIC_AETHER_HELMET.get()),
                new ItemStack(MagicWorld.DRACONIC_AETHER_CHESTPLATE.get()),
                new ItemStack(MagicWorld.DRACONIC_AETHER_LEGGINGS.get()),
                new ItemStack(MagicWorld.DRACONIC_AETHER_BOOTS.get()));
        putItems(level, chests[1],
                new ItemStack(Items.COOKED_BEEF, 64),
                new ItemStack(Items.BREAD, 64),
                new ItemStack(Items.GOLDEN_CARROT, 64),
                new ItemStack(Items.GOLDEN_APPLE, 16),
                new ItemStack(Items.TOTEM_OF_UNDYING, 2));
        putItems(level, chests[2],
                new ItemStack(Items.OAK_LOG, 64),
                new ItemStack(Items.STONE_BRICKS, 64),
                new ItemStack(Items.GLASS, 64),
                new ItemStack(Items.IRON_INGOT, 64),
                new ItemStack(Items.DIAMOND, 16));
        putItems(level, chests[3],
                new ItemStack(Items.WHEAT_SEEDS, 64),
                new ItemStack(Items.CARROT, 64),
                new ItemStack(Items.POTATO, 64),
                new ItemStack(Items.BEETROOT_SEEDS, 64),
                new ItemStack(Items.BONE_MEAL, 64),
                new ItemStack(Items.SADDLE, 4),
                new ItemStack(Items.NAME_TAG, 8));
    }

    private static void decorateImportedHouseAddons(ServerLevel level, BlockPos base) {
        for (BlockPos pos : new BlockPos[] {
                base.offset(-76, 1, -54), base.offset(28, 1, -54),
                base.offset(-76, 1, 64), base.offset(28, 1, 64),
                base.offset(-46, 1, 72), base.offset(8, 1, 72),
                base.offset(-90, 1, 0), base.offset(42, 1, 0)
        }) {
            placeLampPost(level, pos);
        }

        decorateImportedHouseDoors(level, base);
        expandImportedHouseWindows(level, base);
        decorateImportedHouseFrontFacade(level, base);
        convertMainHousePerimeterTreesToCherry(level, base);

        for (BlockPos preferred : new BlockPos[] {
                base.offset(0, 1, 0), base.offset(10, 1, 0), base.offset(-10, 1, 0),
                base.offset(0, 1, 10), base.offset(0, 1, -10),
                base.offset(16, 1, 12), base.offset(-16, 1, 12),
                base.offset(16, 1, -12), base.offset(-16, 1, -12)
        }) {
            decorateSafeInteriorSpot(level, preferred);
        }

        for (BlockPos preferred : new BlockPos[] {
                base.offset(6, 1, 6), base.offset(-6, 1, 6),
                base.offset(6, 1, -6), base.offset(-6, 1, -6),
                base.offset(18, 1, 0), base.offset(-18, 1, 0)
        }) {
            placeSafeInteriorLight(level, preferred);
        }

        fillImportedHousePremiumChests(level, base);
        spawnArmorStand(level, base.offset(-12, 1, 3),
                Items.NETHERITE_HELMET, Items.NETHERITE_CHESTPLATE,
                Items.NETHERITE_LEGGINGS, Items.NETHERITE_BOOTS,
                "Armadura de Netherite");
        spawnNamedMagicWorldArmorStand(level, base.offset(12, 1, 3));
    }

    private static void decorateImportedHouseFrontFacade(ServerLevel level, BlockPos base) {
        int frontZ = IMPORTED_HOUSE_MAX_Z;
        for (int y = 1; y <= 5; y++) {
            level.setBlock(base.offset(-5, y, frontZ), Blocks.STONE_BRICKS.defaultBlockState(), 2);
            level.setBlock(base.offset(5, y, frontZ), Blocks.STONE_BRICKS.defaultBlockState(), 2);
        }
        for (int x = -5; x <= 5; x++) {
            if (x >= -2 && x <= 2) {
                level.setBlock(base.offset(x, 4, frontZ), Blocks.CHISELED_STONE_BRICKS.defaultBlockState(), 2);
            } else {
                level.setBlock(base.offset(x, 4, frontZ), Blocks.STONE_BRICKS.defaultBlockState(), 2);
            }
            level.setBlock(base.offset(x, 5, frontZ), Blocks.DARK_OAK_STAIRS.defaultBlockState()
                    .setValue(StairBlock.FACING, Direction.SOUTH), 2);
        }
        level.setBlock(base.offset(0, 6, frontZ), Blocks.SEA_LANTERN.defaultBlockState(), 2);
        level.setBlock(base.offset(0, 7, frontZ), Blocks.LIGHT_BLUE_STAINED_GLASS.defaultBlockState(), 2);

        for (int x : new int[] {-24, -23, -22, -21, -20, -19, -18, -17, -16, -15, -14, -13, -12,
                12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24}) {
            level.setBlock(base.offset(x, 1, frontZ), Blocks.STONE_BRICK_WALL.defaultBlockState(), 2);
        }

        for (int x : new int[] {-18, -17, -16, -14, -13, -12, 12, 13, 14, 16, 17, 18}) {
            level.setBlock(base.offset(x, 2, frontZ), Blocks.LIGHT_BLUE_STAINED_GLASS_PANE.defaultBlockState(), 2);
            level.setBlock(base.offset(x, 3, frontZ), Blocks.LIGHT_BLUE_STAINED_GLASS_PANE.defaultBlockState(), 2);
        }

        for (BlockPos lamp : new BlockPos[] {
                base.offset(-7, 3, frontZ), base.offset(7, 3, frontZ),
                base.offset(-26, 1, frontZ + 1), base.offset(26, 1, frontZ + 1)
        }) {
            level.setBlock(lamp, Blocks.SEA_LANTERN.defaultBlockState(), 2);
        }

        for (BlockPos plant : new BlockPos[] {
                base.offset(-28, 1, frontZ + 1), base.offset(-27, 1, frontZ + 1),
                base.offset(27, 1, frontZ + 1), base.offset(28, 1, frontZ + 1)
        }) {
            if (level.getBlockState(plant).isAir() && level.getBlockState(plant.below()).isSolid()) {
                level.setBlock(plant, Blocks.FLOWERING_AZALEA.defaultBlockState(), 2);
            }
        }
    }

    private static void convertMainHousePerimeterTreesToCherry(ServerLevel level, BlockPos base) {
        int minX = base.getX() + IMPORTED_ESTATE_FENCE_MIN_X;
        int maxX = base.getX() + IMPORTED_ESTATE_FENCE_MAX_X;
        int minZ = base.getZ() + IMPORTED_ESTATE_FENCE_MIN_Z;
        int maxZ = base.getZ() + IMPORTED_ESTATE_FENCE_MAX_Z;

        convertNaturalTreesToCherryInRing(level, minX, maxX, minZ, maxZ,
                1, 0, 1, 0);
    }

    private static void convertCastlePerimeterTreesToCherry(ServerLevel level, BlockPos base) {
        int margin = 24;
        BlockPos origin = castleOrigin(base);
        int minX = origin.getX() - margin;
        int maxX = origin.getX() + CASTLE_SIZE_X + margin;
        int minZ = origin.getZ() - margin;
        int maxZ = origin.getZ() + CASTLE_SIZE_Z + margin;
        int excludedMinX = origin.getX() - BREATHING_MARGIN;
        int excludedMaxX = origin.getX() + CASTLE_SIZE_X + BREATHING_MARGIN;
        int excludedMinZ = origin.getZ() - BREATHING_MARGIN;
        int excludedMaxZ = origin.getZ() + CASTLE_SIZE_Z + BREATHING_MARGIN;

        convertNaturalTreesToCherryInRing(level, minX, maxX, minZ, maxZ,
                excludedMinX, excludedMaxX, excludedMinZ, excludedMaxZ);
    }

    private static void convertNaturalTreesToCherryInRing(ServerLevel level,
                                                          int minX,
                                                          int maxX,
                                                          int minZ,
                                                          int maxZ,
                                                          int excludedMinX,
                                                          int excludedMaxX,
                                                          int excludedMinZ,
                                                          int excludedMaxZ) {
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        int minBuildY = level.getMinBuildHeight();
        int maxBuildY = level.getMaxBuildHeight() - 1;

        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                if (x >= excludedMinX && x <= excludedMaxX && z >= excludedMinZ && z <= excludedMaxZ) {
                    continue;
                }
                int surfaceY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
                int scanMinY = Math.max(minBuildY, surfaceY - 10);
                int scanMaxY = Math.min(maxBuildY, surfaceY + 34);
                boolean convertedAny = false;
                for (int y = scanMinY; y <= scanMaxY; y++) {
                    mutable.set(x, y, z);
                    BlockState state = level.getBlockState(mutable);
                    if (isNaturalTreeLog(state) && hasNaturalLeavesNearby(level, mutable.immutable(), 4)) {
                        level.setBlock(mutable, Blocks.CHERRY_LOG.defaultBlockState(), 2);
                        convertedAny = true;
                    } else if (isNaturalTreeLeaf(state)) {
                        level.setBlock(mutable, Blocks.CHERRY_LEAVES.defaultBlockState(), 2);
                        convertedAny = true;
                    }
                }
                if (convertedAny) {
                    placeCherryPetalsInAllowedTreeColumn(level, x, z, surfaceY,
                            minX, maxX, minZ, maxZ, excludedMinX, excludedMaxX, excludedMinZ, excludedMaxZ);
                }
            }
        }
    }

    private static boolean isNaturalTreeLog(BlockState state) {
        return state.is(Blocks.OAK_LOG)
                || state.is(Blocks.SPRUCE_LOG)
                || state.is(Blocks.BIRCH_LOG)
                || state.is(Blocks.DARK_OAK_LOG)
                || state.is(Blocks.ACACIA_LOG)
                || state.is(Blocks.JUNGLE_LOG)
                || state.is(Blocks.MANGROVE_LOG);
    }

    private static boolean isNaturalTreeLeaf(BlockState state) {
        return state.is(Blocks.OAK_LEAVES)
                || state.is(Blocks.SPRUCE_LEAVES)
                || state.is(Blocks.BIRCH_LEAVES)
                || state.is(Blocks.DARK_OAK_LEAVES)
                || state.is(Blocks.ACACIA_LEAVES)
                || state.is(Blocks.JUNGLE_LEAVES)
                || state.is(Blocks.MANGROVE_LEAVES)
                || state.is(Blocks.FLOWERING_AZALEA_LEAVES)
                || state.is(Blocks.AZALEA_LEAVES);
    }

    private static boolean hasNaturalLeavesNearby(ServerLevel level, BlockPos pos, int radius) {
        for (int x = -radius; x <= radius; x++) {
            for (int y = 0; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    if (isNaturalTreeLeaf(level.getBlockState(pos.offset(x, y, z)))) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static void placeCherryPetalsInAllowedTreeColumn(ServerLevel level,
                                                             int x,
                                                             int z,
                                                             int surfaceY,
                                                             int minX,
                                                             int maxX,
                                                             int minZ,
                                                             int maxZ,
                                                             int excludedMinX,
                                                             int excludedMaxX,
                                                             int excludedMinZ,
                                                             int excludedMaxZ) {
        if (x < minX || x > maxX || z < minZ || z > maxZ
                || (x >= excludedMinX && x <= excludedMaxX && z >= excludedMinZ && z <= excludedMaxZ)
                || surfaceY <= level.getMinBuildHeight()) {
            return;
        }
        BlockPos ground = new BlockPos(x, surfaceY - 1, z);
        if (!level.getBlockState(ground).is(Blocks.GRASS_BLOCK)
                || !level.getBlockState(ground.above()).isAir()) {
            return;
        }
        if (Math.floorMod(ground.getX() * 31 + ground.getZ() * 17, 9) <= 2) {
            level.setBlock(ground.above(), Blocks.PINK_PETALS.defaultBlockState(), 2);
        }
    }

    private static void decorateImportedHouseDoors(ServerLevel level, BlockPos base) {
        BlockPos origin = houseOrigin(base);
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        for (int x = 0; x < IMPORTED_HOUSE_SIZE_X; x++) {
            for (int z = 0; z < IMPORTED_HOUSE_SIZE_Z; z++) {
                for (int y = 0; y <= 18; y++) {
                    mutable.set(origin.getX() + x, origin.getY() + y, origin.getZ() + z);
                    BlockState state = level.getBlockState(mutable);
                    if (!(state.getBlock() instanceof DoorBlock)
                            || state.getValue(DoorBlock.HALF) != DoubleBlockHalf.UPPER) {
                        continue;
                    }

                    BlockPos lintel = mutable.above().immutable();
                    if (level.getBlockState(lintel).isAir()) {
                        level.setBlock(lintel, Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState(), 2);
                    }
                    Direction facing = state.getValue(DoorBlock.FACING);
                    Direction side = facing.getClockWise();
                    for (Direction direction : new Direction[] {side, side.getOpposite()}) {
                        BlockPos trim = lintel.relative(direction);
                        if (level.getBlockState(trim).isAir()) {
                            level.setBlock(trim, Blocks.DARK_OAK_STAIRS.defaultBlockState()
                                    .setValue(StairBlock.FACING, direction.getOpposite()), 2);
                        }
                    }
                    BlockPos lamp = lintel.relative(facing.getOpposite());
                    if (level.getBlockState(lamp).isAir()) {
                        level.setBlock(lamp, Blocks.LANTERN.defaultBlockState(), 2);
                    }
                }
            }
        }
    }

    private static void expandImportedHouseWindows(ServerLevel level, BlockPos base) {
        BlockPos origin = houseOrigin(base);
        List<BlockPos> windows = new ArrayList<>();
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        for (int x = 0; x < IMPORTED_HOUSE_SIZE_X; x++) {
            for (int z = 0; z < IMPORTED_HOUSE_SIZE_Z; z++) {
                for (int y = 1; y <= 18; y++) {
                    mutable.set(origin.getX() + x, origin.getY() + y, origin.getZ() + z);
                    BlockState state = level.getBlockState(mutable);
                    if (state.is(Blocks.GLASS_PANE)
                            || state.is(Blocks.GLASS)
                            || state.is(Blocks.LIGHT_BLUE_STAINED_GLASS_PANE)
                            || state.is(Blocks.LIGHT_BLUE_STAINED_GLASS)) {
                        windows.add(mutable.immutable());
                    }
                }
            }
        }

        for (BlockPos window : windows) {
            boolean northSouthAir = level.getBlockState(window.north()).isAir()
                    && level.getBlockState(window.south()).isAir();
            boolean eastWestAir = level.getBlockState(window.east()).isAir()
                    && level.getBlockState(window.west()).isAir();
            if (northSouthAir) {
                widenWindow(level, window.east(), Direction.NORTH, Direction.SOUTH);
                widenWindow(level, window.west(), Direction.NORTH, Direction.SOUTH);
            } else if (eastWestAir) {
                widenWindow(level, window.north(), Direction.EAST, Direction.WEST);
                widenWindow(level, window.south(), Direction.EAST, Direction.WEST);
            }
        }
    }

    private static void widenWindow(ServerLevel level, BlockPos pos, Direction firstSide, Direction secondSide) {
        BlockState state = level.getBlockState(pos);
        if (!isImportedHouseWindowWall(state)
                || !level.getBlockState(pos.relative(firstSide)).isAir()
                || !level.getBlockState(pos.relative(secondSide)).isAir()) {
            return;
        }
        level.setBlock(pos, Blocks.LIGHT_BLUE_STAINED_GLASS_PANE.defaultBlockState(), 2);
    }

    private static boolean isImportedHouseWindowWall(BlockState state) {
        return state.is(Blocks.STONE_BRICKS)
                || state.is(Blocks.MOSSY_STONE_BRICKS)
                || state.is(Blocks.DEEPSLATE_BRICKS)
                || state.is(Blocks.POLISHED_DEEPSLATE)
                || state.is(Blocks.DARK_OAK_PLANKS)
                || state.is(Blocks.SPRUCE_PLANKS)
                || state.is(Blocks.OAK_PLANKS)
                || state.is(Blocks.BIRCH_PLANKS)
                || state.is(Blocks.BRICKS);
    }

    private static void decorateSafeInteriorSpot(ServerLevel level, BlockPos preferred) {
        BlockPos safe = findSafeInteriorFloor(level, preferred, 8, 5);
        if (safe == null) {
            return;
        }

        int style = Math.floorMod(safe.getX() + safe.getZ(), 4);
        switch (style) {
            case 0 -> {
                level.setBlock(safe, Blocks.BOOKSHELF.defaultBlockState(), 2);
                if (level.getBlockState(safe.above()).isAir()) {
                    level.setBlock(safe.above(), Blocks.POTTED_DANDELION.defaultBlockState(), 2);
                }
            }
            case 1 -> {
                level.setBlock(safe, Blocks.CRAFTING_TABLE.defaultBlockState(), 2);
                if (level.getBlockState(safe.relative(Direction.EAST)).isAir()) {
                    level.setBlock(safe.relative(Direction.EAST), Blocks.ANVIL.defaultBlockState(), 2);
                }
            }
            case 2 -> {
                level.setBlock(safe, Blocks.BARREL.defaultBlockState(), 2);
                if (level.getBlockState(safe.above()).isAir()) {
                    level.setBlock(safe.above(), Blocks.LANTERN.defaultBlockState(), 2);
                }
            }
            default -> {
                level.setBlock(safe, Blocks.FLOWERING_AZALEA.defaultBlockState(), 2);
                if (level.getBlockState(safe.below()).isSolid()) {
                    for (Direction direction : Direction.Plane.HORIZONTAL) {
                        BlockPos flower = safe.relative(direction);
                        if (level.getBlockState(flower).isAir()
                                && level.getBlockState(flower.below()).isSolid()) {
                            level.setBlock(flower, flowerFor(flower.getX() + flower.getZ()), 2);
                        }
                    }
                }
            }
        }
    }

    private static void placeSafeInteriorLight(ServerLevel level, BlockPos preferred) {
        BlockPos safe = findSafeInteriorFloor(level, preferred, 8, 5);
        if (safe == null) {
            return;
        }
        BlockPos light = safe.above(2);
        if (level.getBlockState(light).isAir()) {
            level.setBlock(light, Blocks.SEA_LANTERN.defaultBlockState(), 2);
        }
    }

    private static void fillImportedHousePremiumChests(ServerLevel level, BlockPos base) {
        BlockPos wands = placeSafeChest(level, base.offset(-14, 1, -8), Direction.SOUTH);
        fillContainerWithItem(level, wands, MagicWorld.VARINHA_MAGICA.get());

        BlockPos rareItems = placeSafeChest(level, base.offset(14, 1, -8), Direction.SOUTH);
        putItems(level, rareItems,
                new ItemStack(Items.ELYTRA), new ItemStack(Items.DRAGON_EGG),
                new ItemStack(Items.NETHER_STAR, 8), new ItemStack(Items.BEACON, 4),
                new ItemStack(Items.ENCHANTED_GOLDEN_APPLE, 16), new ItemStack(Items.TOTEM_OF_UNDYING, 8),
                new ItemStack(Items.TRIDENT), new ItemStack(Items.HEART_OF_THE_SEA, 8),
                new ItemStack(Items.CONDUIT, 4), new ItemStack(Items.NETHERITE_INGOT, 16),
                new ItemStack(MagicWorld.DRACONIC_AETHER_HELMET.get()),
                new ItemStack(MagicWorld.DRACONIC_AETHER_CHESTPLATE.get()),
                new ItemStack(MagicWorld.DRACONIC_AETHER_LEGGINGS.get()),
                new ItemStack(MagicWorld.DRACONIC_AETHER_BOOTS.get()));

        BlockPos usefulItems = placeSafeChest(level, base.offset(14, 1, 8), Direction.NORTH);
        putItems(level, usefulItems,
                new ItemStack(Items.NETHERITE_PICKAXE), new ItemStack(Items.NETHERITE_AXE),
                new ItemStack(Items.NETHERITE_SHOVEL), new ItemStack(Items.NETHERITE_HOE),
                new ItemStack(Items.NETHERITE_SWORD), new ItemStack(Items.BOW),
                new ItemStack(Items.CROSSBOW), new ItemStack(Items.SHIELD),
                new ItemStack(Items.FIREWORK_ROCKET, 64), new ItemStack(Items.EXPERIENCE_BOTTLE, 64));
    }

    private static void spawnNamedMagicWorldArmorStand(ServerLevel level, BlockPos pos) {
        spawnArmorStand(level, pos,
                new ItemStack(MagicWorld.DRACONIC_AETHER_HELMET.get()),
                new ItemStack(MagicWorld.DRACONIC_AETHER_CHESTPLATE.get()),
                new ItemStack(MagicWorld.DRACONIC_AETHER_LEGGINGS.get()),
                new ItemStack(MagicWorld.DRACONIC_AETHER_BOOTS.get()),
                "Armadura Draconic Aether");
    }

    private static void decorateCastleStarterLife(ServerLevel level, BlockPos center) {
        placeMagicWorldGearChest(level, center.offset(0, 1, -18));
        spawnNamed(level, EntityType.IRON_GOLEM, center.offset(0, 1, -10), "Guardiao do Castelo");
        spawnNamed(level, EntityType.VILLAGER, center.offset(4, 1, -8), "Mordomo do Castelo");
        spawnNamed(level, EntityType.VILLAGER, center.offset(-4, 1, -8), "Ferreiro do Castelo");
        populateCastleResidents(level, center);
        populateCastleCouncilTable(level, center);
        ArmorStand stand = EntityType.ARMOR_STAND.create(level);
        if (stand != null) {
            stand.setCustomName(Component.literal("Dragao Magic World"));
            stand.setCustomNameVisible(true);
            stand.moveTo(center.getX() + 0.5D, center.getY() + 18.0D, center.getZ() + 0.5D, 0.0F, 0.0F);
            stand.setNoGravity(true);
            level.addFreshEntity(stand);
        }
    }

    private static void placeMagicWorldGearChest(ServerLevel level, BlockPos center) {
        BlockPos chestPos = center.offset(0, 1, 0);
        placeChest(level, chestPos, Direction.NORTH);
        putItems(level, chestPos,
                new ItemStack(MagicWorld.VARINHA_MAGICA.get()),
                new ItemStack(Items.DIAMOND_SWORD),
                new ItemStack(Items.DIAMOND_PICKAXE),
                new ItemStack(Items.DIAMOND_AXE),
                new ItemStack(Items.DIAMOND_CHESTPLATE),
                new ItemStack(Items.DIAMOND_LEGGINGS),
                new ItemStack(Items.DIAMOND_BOOTS),
                new ItemStack(Items.ENCHANTED_GOLDEN_APPLE, 4),
                new ItemStack(Items.TOTEM_OF_UNDYING, 2));
    }

    private static void buildFallbackHouse(ServerLevel level, BlockPos base) {
        clearFlatArea(level, base.offset(-8, 0, -8), 17, 17, 12);
        for (int x = -5; x <= 5; x++) {
            for (int z = -5; z <= 5; z++) {
                level.setBlock(base.offset(x, 0, z), Blocks.OAK_PLANKS.defaultBlockState(), 2);
                if (Math.abs(x) == 5 || Math.abs(z) == 5) {
                    for (int y = 1; y <= 4; y++) {
                        level.setBlock(base.offset(x, y, z), Blocks.OAK_LOG.defaultBlockState(), 2);
                    }
                }
            }
        }
        for (int x = -5; x <= 5; x++) {
            for (int z = -5; z <= 5; z++) {
                level.setBlock(base.offset(x, 5, z), Blocks.SPRUCE_PLANKS.defaultBlockState(), 2);
            }
        }
        fillStarterChests(level, base);
    }

    private static void buildFallbackCastle(ServerLevel level, BlockPos center) {
        clearFlatArea(level, center.offset(-32, 0, -32), 65, 65, 28);
        for (int x = -28; x <= 28; x++) {
            for (int z = -28; z <= 28; z++) {
                level.setBlock(center.offset(x, 0, z), Blocks.STONE_BRICKS.defaultBlockState(), 2);
                if (Math.abs(x) == 28 || Math.abs(z) == 28) {
                    for (int y = 1; y <= 10; y++) {
                        level.setBlock(center.offset(x, y, z), Blocks.DEEPSLATE_BRICKS.defaultBlockState(), 2);
                    }
                }
            }
        }
    }

    private static void clearFlatArea(ServerLevel level, BlockPos corner, int width, int depth, int clearHeight) {
        for (int x = 0; x < width; x++) {
            for (int z = 0; z < depth; z++) {
                BlockPos ground = corner.offset(x, -1, z);
                level.setBlock(ground.below(), Blocks.DIRT.defaultBlockState(), 2);
                level.setBlock(ground, Blocks.GRASS_BLOCK.defaultBlockState(), 2);
                for (int y = 1; y <= clearHeight; y++) {
                    level.setBlock(ground.above(y), Blocks.AIR.defaultBlockState(), 2);
                }
            }
        }
    }

    private static BlockPos findHighestFeatureSurface(ServerLevel level, BlockPos approximate, int horizontalRadius) {
        BlockPos best = null;
        int bestScore = Integer.MAX_VALUE;
        int maxRadius = Math.max(0, horizontalRadius);
        for (int radius = 0; radius <= maxRadius; radius += 8) {
            for (int dx = -radius; dx <= radius; dx += Math.max(1, radius == 0 ? 1 : 8)) {
                for (int dz = -radius; dz <= radius; dz += Math.max(1, radius == 0 ? 1 : 8)) {
                    if (radius != 0 && Math.abs(dx) != radius && Math.abs(dz) != radius) {
                        continue;
                    }
                    int x = approximate.getX() + dx;
                    int z = approximate.getZ() + dz;
                    int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
                    BlockPos candidate = new BlockPos(x, y, z);
                    int score = Math.abs(dx) + Math.abs(dz) + Math.abs(y - approximate.getY()) * 2;
                    if (best == null || score < bestScore) {
                        best = candidate;
                        bestScore = score;
                    }
                }
            }
        }
        return best == null ? approximate : best;
    }

    private static BlockPos placeSafeChest(ServerLevel level, BlockPos preferred, Direction facing) {
        BlockPos safe = findSafeInteriorFloor(level, preferred, 28, 8);
        if (safe != null) {
            placeChest(level, safe, facing);
            return safe;
        }
        placeChest(level, preferred, facing);
        return preferred;
    }

    private static BlockPos findSafeInteriorFloor(ServerLevel level, BlockPos preferred, int horizontalRadius, int verticalRadius) {
        BlockPos best = null;
        int bestDistance = Integer.MAX_VALUE;
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();

        for (int y = preferred.getY() - verticalRadius; y <= preferred.getY() + verticalRadius; y++) {
            for (int x = preferred.getX() - horizontalRadius; x <= preferred.getX() + horizontalRadius; x++) {
                for (int z = preferred.getZ() - horizontalRadius; z <= preferred.getZ() + horizontalRadius; z++) {
                    mutable.set(x, y, z);
                    if (!level.isInWorldBounds(mutable)
                            || !level.getBlockState(mutable).isAir()
                            || !level.getBlockState(mutable.above()).isAir()
                            || !level.getBlockState(mutable.below()).isSolid()
                            || !hasSolidCeilingAbove(level, mutable, 14)) {
                        continue;
                    }

                    int distance = Math.abs(x - preferred.getX()) + Math.abs(y - preferred.getY()) + Math.abs(z - preferred.getZ());
                    if (distance < bestDistance) {
                        bestDistance = distance;
                        best = mutable.immutable();
                    }
                }
            }
        }
        return best;
    }

    private static BlockPos findNearestStarterPortalMarker(ServerLevel level, BlockPos center, int radius) {
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-radius, -4, -radius), center.offset(radius, 12, radius))) {
            if (isStarterPortalMarker(level, pos)) {
                return pos.immutable();
            }
        }
        return null;
    }

    private static boolean isStarterPortalMarker(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return state.is(Blocks.CRYING_OBSIDIAN)
                || state.is(Blocks.END_PORTAL_FRAME)
                || state.is(Blocks.PURPLE_STAINED_GLASS)
                || state.is(Blocks.MAGENTA_STAINED_GLASS)
                || state.is(Blocks.AMETHYST_BLOCK)
                || state.is(Blocks.END_GATEWAY);
    }

    private static void buildPortalMagicBeacons(ServerLevel level, BlockPos center) {
        for (BlockPos pos : new BlockPos[] {
                center.offset(-7, 1, -7), center.offset(7, 1, -7),
                center.offset(-7, 1, 7), center.offset(7, 1, 7),
                center.offset(0, 1, -7), center.offset(0, 1, 7)
        }) {
            level.setBlock(pos.below(), Blocks.AMETHYST_BLOCK.defaultBlockState(), 2);
            level.setBlock(pos, Blocks.END_ROD.defaultBlockState(), 2);
        }
    }

    private static void populateCastleResidents(ServerLevel level, BlockPos center) {
        spawnCastleResident(level, center.offset(-94, 25, -64), "Bibliotecario Real", Items.BOOK,
                Blocks.LECTERN.defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, Direction.SOUTH), Direction.SOUTH,
                new ItemStack(Items.BOOK, 32), new ItemStack(Items.PAPER, 64), new ItemStack(Items.EMERALD, 16));
        spawnCastleResident(level, center.offset(-4, 52, -81), "Cartografo da Torre", Items.MAP,
                Blocks.CARTOGRAPHY_TABLE.defaultBlockState(), Direction.SOUTH,
                new ItemStack(Items.MAP, 16), new ItemStack(Items.COMPASS, 8), new ItemStack(Items.PAPER, 64));
        spawnCastleResident(level, center.offset(24, 32, 14), "Armoreiro do Castelo", Items.IRON_CHESTPLATE,
                Blocks.BLAST_FURNACE.defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, Direction.WEST), Direction.WEST,
                new ItemStack(Items.IRON_INGOT, 64), new ItemStack(Items.COAL, 64), new ItemStack(Items.SHIELD, 8));
        spawnCastleResident(level, center.offset(-5, 46, -40), "Clerigo da Capela", Items.BLAZE_ROD,
                Blocks.BREWING_STAND.defaultBlockState(), Direction.NORTH,
                new ItemStack(Items.GLASS_BOTTLE, 64), new ItemStack(Items.BLAZE_POWDER, 32), new ItemStack(Items.GOLDEN_APPLE, 8));
        spawnCastleResident(level, center.offset(-78, 26, -58), "Pedreiro Real", Items.STONE_BRICKS,
                Blocks.STONECUTTER.defaultBlockState(), Direction.EAST,
                new ItemStack(Items.STONE_BRICKS, 64), new ItemStack(Items.POLISHED_ANDESITE, 64), new ItemStack(Items.BRICKS, 64));
        spawnCastleResident(level, center.offset(17, 52, -61), "Ferreiro de Armas", Items.IRON_SWORD,
                Blocks.GRINDSTONE.defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, Direction.SOUTH), Direction.SOUTH,
                new ItemStack(Items.IRON_SWORD, 8), new ItemStack(Items.IRON_AXE, 8), new ItemStack(Items.COAL, 64));
        spawnCastleResident(level, findHighestFeatureSurface(level, center.offset(38, 0, -43), 32), "Ferreiro de Ferramentas", Items.IRON_PICKAXE,
                Blocks.SMITHING_TABLE.defaultBlockState(), Direction.WEST,
                new ItemStack(Items.IRON_PICKAXE, 8), new ItemStack(Items.IRON_SHOVEL, 8), new ItemStack(Items.IRON_INGOT, 64));
        spawnCastleResident(level, center.offset(-49, 46, -59), "Flecheiro da Guarda", Items.BOW,
                Blocks.FLETCHING_TABLE.defaultBlockState(), Direction.EAST,
                new ItemStack(Items.BOW, 8), new ItemStack(Items.ARROW, 64), new ItemStack(Items.FLINT, 64));
        spawnCastleResident(level, center.offset(-4, 47, -45), "Jardineiro do Castelo", Items.WHEAT,
                Blocks.COMPOSTER.defaultBlockState(), Direction.NORTH,
                new ItemStack(Items.BREAD, 64), new ItemStack(Items.WHEAT, 64), new ItemStack(Items.BONE_MEAL, 64));
        spawnCastleResident(level, findHighestFeatureSurface(level, center.offset(47, 0, -39), 32), "Cozinheiro do Salao", Items.COOKED_BEEF,
                Blocks.SMOKER.defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, Direction.WEST), Direction.WEST,
                new ItemStack(Items.COOKED_BEEF, 64), new ItemStack(Items.BREAD, 64), new ItemStack(Items.COAL, 64));
        spawnCastleResident(level, center.offset(-8, 49, -115), "Tecelao da Ponte", Items.WHITE_WOOL,
                Blocks.LOOM.defaultBlockState(), Direction.SOUTH,
                new ItemStack(Items.WHITE_WOOL, 64), new ItemStack(Items.BLUE_DYE, 32), new ItemStack(Items.LEATHER, 32));
        spawnCastleResident(level, center.offset(62, 38, 14), "Coureeiro da Cavalaria", Items.LEATHER,
                Blocks.CAULDRON.defaultBlockState(), Direction.NORTH,
                new ItemStack(Items.LEATHER, 64), new ItemStack(Items.SADDLE, 4), new ItemStack(Items.LEAD, 16));
    }

    private static void populateCastleCouncilTable(ServerLevel level, BlockPos center) {
        spawnCastleCouncilResident(level, center.offset(2, 0, 5), "Bibliotecario do Conselho",
                VillagerProfession.LIBRARIAN, Items.BOOK, Blocks.LECTERN.defaultBlockState(), Direction.SOUTH);
        spawnCastleCouncilResident(level, center.offset(2, 0, -8), "Cartografo do Conselho",
                VillagerProfession.CARTOGRAPHER, Items.MAP, Blocks.CARTOGRAPHY_TABLE.defaultBlockState(), Direction.NORTH);
        spawnCastleCouncilResident(level, center.offset(-8, 0, -1), "Clerigo do Conselho",
                VillagerProfession.CLERIC, Items.BLAZE_ROD, Blocks.BREWING_STAND.defaultBlockState(), Direction.WEST);
        spawnCastleCouncilResident(level, center.offset(8, 0, 0), "Armoreiro do Conselho",
                VillagerProfession.ARMORER, Items.IRON_CHESTPLATE,
                Blocks.BLAST_FURNACE.defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, Direction.WEST), Direction.EAST);
    }

    private static void spawnCastleCouncilResident(
            ServerLevel level,
            BlockPos seat,
            String name,
            VillagerProfession profession,
            Item heldItem,
            BlockState jobBlock,
            Direction outward
    ) {
        BlockPos spawn = level.getBlockState(seat).isAir() && level.getBlockState(seat.below()).isSolid()
                ? seat
                : seat.above();
        AABB castleArea = new AABB(spawn).inflate(192.0D, 96.0D, 192.0D);
        List<Villager> existing = level.getEntitiesOfClass(Villager.class, castleArea,
                villager -> name.equals(villager.getName().getString()));
        Villager villager = existing.isEmpty() ? null : existing.get(0);
        if (villager == null) {
            Entity entity = EntityType.VILLAGER.spawn(level, spawn, MobSpawnType.STRUCTURE);
            if (entity instanceof Villager spawned) {
                villager = spawned;
            }
        }
        if (villager == null) {
            return;
        }

        villager.moveTo(spawn.getX() + 0.5D, spawn.getY(), spawn.getZ() + 0.5D, outward.toYRot(), 0.0F);
        villager.setCustomName(Component.literal(name));
        villager.setCustomNameVisible(true);
        villager.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(heldItem));
        empowerMagicWorldVillager(villager, profession, spawn, seat, GLOBAL_VILLAGER_WORK_RADIUS);

        BlockPos station = seat.relative(outward, 2);
        if (level.getBlockState(station).isAir() || level.getBlockState(station).canBeReplaced()) {
            level.setBlock(station, jobBlock, 2);
        }
    }

    private static void spawnCastleResident(
            ServerLevel level,
            BlockPos requested,
            String name,
            net.minecraft.world.item.Item heldItem,
            BlockState jobBlock,
            Direction facing,
            ItemStack... supplies
    ) {
        BlockPos spot = findSafeInteriorFloor(level, requested, 8, 18);
        BlockPos groundSpot = findWalkableGroundNear(level, requested, 80, 160);
        if (groundSpot != null && requested.getY() > groundSpot.getY() + 8) {
            spot = groundSpot;
        }
        if (spot == null) {
            spot = castleGroundResidentSpot(level, requested);
        } else if (!hasSolidCeilingAbove(level, spot, 8)) {
            BlockPos lowerGroundSpot = findWalkableGroundNear(level, spot, 64, 96);
            if (lowerGroundSpot != null && spot.getY() > lowerGroundSpot.getY() + 8) {
                spot = lowerGroundSpot;
            }
        }
        AABB nearby = new AABB(spot).inflate(256.0D, 96.0D, 256.0D);
        List<Villager> residents = level.getEntitiesOfClass(Villager.class, nearby,
                villager -> name.equals(villager.getName().getString()));
        Villager resident = residents.isEmpty() ? null : residents.get(0);
        if (resident == null) {
            Entity entity = EntityType.VILLAGER.spawn(level, spot, MobSpawnType.STRUCTURE);
            if (entity instanceof Villager spawned) {
                resident = spawned;
            }
        }
        if (resident != null) {
            resident.setCustomName(Component.literal(name));
            resident.setCustomNameVisible(true);
            resident.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(heldItem));
            empowerMagicWorldVillager(resident, professionForJobBlock(jobBlock), spot, requested, GLOBAL_VILLAGER_WORK_RADIUS);
        }
        decorateCastleResidentStation(level, spot, facing, jobBlock, supplies);
    }

    private static VillagerProfession professionForJobBlock(BlockState jobBlock) {
        if (jobBlock.is(Blocks.LECTERN)) return VillagerProfession.LIBRARIAN;
        if (jobBlock.is(Blocks.CARTOGRAPHY_TABLE)) return VillagerProfession.CARTOGRAPHER;
        if (jobBlock.is(Blocks.BLAST_FURNACE)) return VillagerProfession.ARMORER;
        if (jobBlock.is(Blocks.BREWING_STAND)) return VillagerProfession.CLERIC;
        if (jobBlock.is(Blocks.STONECUTTER)) return VillagerProfession.MASON;
        if (jobBlock.is(Blocks.GRINDSTONE)) return VillagerProfession.WEAPONSMITH;
        if (jobBlock.is(Blocks.SMITHING_TABLE)) return VillagerProfession.TOOLSMITH;
        if (jobBlock.is(Blocks.FLETCHING_TABLE)) return VillagerProfession.FLETCHER;
        if (jobBlock.is(Blocks.COMPOSTER)) return VillagerProfession.FARMER;
        if (jobBlock.is(Blocks.SMOKER)) return VillagerProfession.BUTCHER;
        if (jobBlock.is(Blocks.LOOM)) return VillagerProfession.SHEPHERD;
        if (jobBlock.is(Blocks.CAULDRON)) return VillagerProfession.LEATHERWORKER;
        return VillagerProfession.FARMER;
    }

    private static BlockPos castleGroundResidentSpot(ServerLevel level, BlockPos requested) {
        BlockPos walkable = findWalkableGroundNear(level, requested, 80, 160);
        if (walkable != null) {
            return walkable;
        }
        return findHighestFeatureSurface(level, requested, 96);
    }

    private static BlockPos findWalkableGroundNear(ServerLevel level, BlockPos preferred, int horizontalRadius, int verticalTolerance) {
        BlockPos best = null;
        int bestScore = Integer.MAX_VALUE;
        for (int radius = 0; radius <= horizontalRadius; radius += 4) {
            for (int dx = -radius; dx <= radius; dx += Math.max(1, radius == 0 ? 1 : 4)) {
                for (int dz = -radius; dz <= radius; dz += Math.max(1, radius == 0 ? 1 : 4)) {
                    if (radius != 0 && Math.abs(dx) != radius && Math.abs(dz) != radius) {
                        continue;
                    }
                    int x = preferred.getX() + dx;
                    int z = preferred.getZ() + dz;
                    int surfaceY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
                    for (int y = surfaceY; y >= Math.max(level.getMinBuildHeight() + 2, preferred.getY() - verticalTolerance); y--) {
                        BlockPos candidate = new BlockPos(x, y, z);
                        if (!level.isInWorldBounds(candidate)
                                || !level.getBlockState(candidate).isAir()
                                || !level.getBlockState(candidate.above()).isAir()
                                || !level.getBlockState(candidate.below()).isSolid()) {
                            continue;
                        }
                        int score = Math.abs(dx) + Math.abs(dz) + Math.max(0, y - preferred.getY()) * 6 + Math.abs(y - preferred.getY());
                        if (best == null || score < bestScore) {
                            best = candidate;
                            bestScore = score;
                        }
                        break;
                    }
                }
            }
            if (best != null && best.getY() <= preferred.getY() + 8) {
                return best;
            }
        }
        return best;
    }

    private static void decorateCastleResidentStation(ServerLevel level, BlockPos spot, Direction facing, BlockState jobBlock, ItemStack... supplies) {
        BlockPos job = spot.relative(facing, 2);
        BlockPos bed = spot.relative(facing.getOpposite(), 2);
        BlockPos chest = spot.relative(facing.getClockWise(), 2);
        BlockPos table = spot.relative(facing.getCounterClockWise(), 2);

        if (!hasSolidCeilingAbove(level, spot, 8)) {
            buildSmallResidentShelter(level, spot);
        }
        prepareResidentAir(level, spot);
        prepareResidentAir(level, job);
        prepareResidentAir(level, chest);
        prepareResidentAir(level, table);
        prepareResidentAir(level, bed);
        prepareResidentAir(level, bed.relative(facing));

        level.setBlock(job, jobBlock, 2);
        placeBed(level, bed, facing);
        placeChest(level, chest, facing.getOpposite());
        level.setBlock(table, Blocks.CRAFTING_TABLE.defaultBlockState(), 2);
        level.setBlock(spot.above(2), Blocks.LANTERN.defaultBlockState(), 2);
        level.setBlock(spot.relative(facing.getCounterClockWise()), Blocks.BLUE_CARPET.defaultBlockState(), 2);
        level.setBlock(table.above(), Blocks.POTTED_DANDELION.defaultBlockState(), 2);
        putItems(level, chest, supplies);
    }

    private static void buildSmallResidentShelter(ServerLevel level, BlockPos center) {
        for (int x = -4; x <= 4; x++) {
            for (int z = -4; z <= 4; z++) {
                BlockPos floor = center.offset(x, -1, z);
                boolean pillar = Math.abs(x) == 4 && Math.abs(z) == 4;
                boolean edge = Math.abs(x) == 4 || Math.abs(z) == 4;
                level.setBlock(floor, edge ? Blocks.MOSSY_COBBLESTONE.defaultBlockState() : Blocks.STONE_BRICKS.defaultBlockState(), 2);
                for (int y = 0; y <= 3; y++) {
                    level.setBlock(center.offset(x, y, z), pillar ? Blocks.COBBLESTONE.defaultBlockState() : Blocks.AIR.defaultBlockState(), 2);
                }
                level.setBlock(center.offset(x, 4, z), edge ? Blocks.MOSSY_STONE_BRICKS.defaultBlockState() : Blocks.STONE_BRICKS.defaultBlockState(), 2);
            }
        }
        for (BlockPos lamp : new BlockPos[] {
                center.offset(-3, 0, -3), center.offset(3, 0, -3),
                center.offset(-3, 0, 3), center.offset(3, 0, 3)
        }) {
            level.setBlock(lamp, Blocks.LANTERN.defaultBlockState(), 2);
        }
    }

    private static boolean hasSolidCeilingAbove(ServerLevel level, BlockPos center, int height) {
        for (int y = 2; y <= height; y++) {
            if (level.getBlockState(center.above(y)).isSolid()) {
                return true;
            }
        }
        return false;
    }

    private static void prepareResidentAir(ServerLevel level, BlockPos pos) {
        if (!level.isInWorldBounds(pos)) {
            return;
        }
        if (level.getBlockState(pos.below()).isAir()) {
            level.setBlock(pos.below(), Blocks.POLISHED_DEEPSLATE.defaultBlockState(), 2);
        }
        level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
        level.setBlock(pos.above(), Blocks.AIR.defaultBlockState(), 2);
    }

    private static void spawnArmorStand(
            ServerLevel level,
            BlockPos pos,
            net.minecraft.world.item.Item helmet,
            net.minecraft.world.item.Item chestplate,
            net.minecraft.world.item.Item leggings,
            net.minecraft.world.item.Item boots,
            String name
    ) {
        BlockPos safePos = findSafeInteriorFloor(level, pos, 5, 4);
        if (safePos == null) {
            return;
        }

        AABB nearby = new AABB(safePos).inflate(16.0D, 8.0D, 16.0D);
        if (!level.getEntitiesOfClass(ArmorStand.class, nearby, stand -> name.equals(stand.getName().getString())).isEmpty()) {
            return;
        }

        Entity entity = EntityType.ARMOR_STAND.spawn(level, safePos, MobSpawnType.STRUCTURE);
        if (!(entity instanceof ArmorStand stand)) {
            return;
        }

        stand.setItemSlot(EquipmentSlot.HEAD, new ItemStack(helmet));
        stand.setItemSlot(EquipmentSlot.CHEST, new ItemStack(chestplate));
        stand.setItemSlot(EquipmentSlot.LEGS, new ItemStack(leggings));
        stand.setItemSlot(EquipmentSlot.FEET, new ItemStack(boots));
        stand.setCustomName(Component.literal(name));
        stand.setCustomNameVisible(true);
        stand.setNoGravity(true);
    }

    private static void spawnArmorStand(
            ServerLevel level,
            BlockPos pos,
            ItemStack helmet,
            ItemStack chestplate,
            ItemStack leggings,
            ItemStack boots,
            String name
    ) {
        BlockPos safePos = findSafeInteriorFloor(level, pos, 5, 4);
        if (safePos == null) {
            return;
        }

        AABB nearby = new AABB(safePos).inflate(16.0D, 8.0D, 16.0D);
        if (!level.getEntitiesOfClass(ArmorStand.class, nearby, stand -> name.equals(stand.getName().getString())).isEmpty()) {
            return;
        }

        Entity entity = EntityType.ARMOR_STAND.spawn(level, safePos, MobSpawnType.STRUCTURE);
        if (!(entity instanceof ArmorStand stand)) {
            return;
        }

        stand.setItemSlot(EquipmentSlot.HEAD, helmet);
        stand.setItemSlot(EquipmentSlot.CHEST, chestplate);
        stand.setItemSlot(EquipmentSlot.LEGS, leggings);
        stand.setItemSlot(EquipmentSlot.FEET, boots);
        stand.setCustomName(Component.literal(name));
        stand.setCustomNameVisible(true);
        stand.setNoGravity(true);
    }

    private static void spawnWorkerVillager(ServerLevel level, BlockPos pos, String name, BlockPos home, BlockPos farmCenter, net.minecraft.world.item.Item heldItem) {
        AABB nearby = new AABB(pos).inflate(256.0D, 64.0D, 256.0D);
        if (!level.getEntitiesOfClass(Villager.class, nearby, villager -> name.equals(villager.getName().getString())).isEmpty()) {
            return;
        }
        Entity entity = EntityType.VILLAGER.spawn(level, pos, MobSpawnType.STRUCTURE);
        if (entity instanceof Villager villager) {
            villager.setCustomName(Component.literal(name));
            villager.setCustomNameVisible(true);
            villager.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(heldItem));
            empowerMagicWorldVillager(villager, VillagerProfession.FARMER, home, farmCenter, 128);
            villager.getPersistentData().putInt("MagicWorldHomeX", home.getX());
            villager.getPersistentData().putInt("MagicWorldHomeY", home.getY());
            villager.getPersistentData().putInt("MagicWorldHomeZ", home.getZ());
            villager.getPersistentData().putInt("MagicWorldFarmX", farmCenter.getX());
            villager.getPersistentData().putInt("MagicWorldFarmY", farmCenter.getY());
            villager.getPersistentData().putInt("MagicWorldFarmZ", farmCenter.getZ());
        }
    }

    private static void spawnProfessionalVillager(
            ServerLevel level,
            BlockPos pos,
            String name,
            VillagerProfession profession,
            BlockPos home,
            BlockPos work,
            Item heldItem,
            boolean guardian
    ) {
        AABB nearby = new AABB(pos).inflate(256.0D, 64.0D, 256.0D);
        if (!level.getEntitiesOfClass(Villager.class, nearby, villager -> name.equals(villager.getName().getString())).isEmpty()) {
            return;
        }
        Entity entity = EntityType.VILLAGER.spawn(level, pos, MobSpawnType.STRUCTURE);
        if (!(entity instanceof Villager villager)) {
            return;
        }

        villager.setCustomName(Component.literal(name));
        villager.setCustomNameVisible(true);
        villager.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(heldItem));
        if (guardian) {
            villager.setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(Items.TOTEM_OF_UNDYING));
            villager.getPersistentData().putBoolean("MagicWorldGuardianVillager", true);
        }
        empowerMagicWorldVillager(villager, profession, home, work, GLOBAL_VILLAGER_WORK_RADIUS);
    }

    private static void spawnEstateGuardianVillagers(ServerLevel level, BlockPos base) {
        BlockPos workCenter = premiumAnimalWorkCenterCorner(base).offset(9, 1, 7);
        spawnProfessionalVillager(level, workCenter, "Guardiao Aldeao da Casa Grande",
                VillagerProfession.WEAPONSMITH, workCenter, workCenter, Items.NETHERITE_SWORD, true);

        BlockPos mine = base.offset(67, 0, 46);
        spawnProfessionalVillager(level, mine.offset(2, 1, 0), "Guardiao Aldeao da Mina",
                VillagerProfession.WEAPONSMITH, mine, mine, Items.NETHERITE_AXE, true);

        BlockPos animalFarm = base.offset(92, 0, -36);
        spawnProfessionalVillager(level, animalFarm, "Guardiao Aldeao dos Currais",
                VillagerProfession.WEAPONSMITH, animalFarm, animalFarm, Items.NETHERITE_SWORD, true);

        BlockPos plantation = base.offset(-108, 0, -68);
        spawnProfessionalVillager(level, plantation, "Guardiao Aldeao da Plantacao",
                VillagerProfession.WEAPONSMITH, plantation, plantation, Items.NETHERITE_SWORD, true);

        BlockPos greenSquare = base.offset(-74, 0, 62);
        spawnProfessionalVillager(level, greenSquare, "Guardiao Aldeao da Praca Verde",
                VillagerProfession.WEAPONSMITH, greenSquare, greenSquare, Items.NETHERITE_SWORD, true);
    }

    private static void clearHostilesNearGuardianVillagers(ServerLevel level, AABB estate) {
        for (Villager guardian : level.getEntitiesOfClass(Villager.class, estate,
                villager -> villager.getPersistentData().getBoolean("MagicWorldGuardianVillager"))) {
            AABB defendedArea = new AABB(guardian.blockPosition()).inflate(GLOBAL_VILLAGER_WORK_RADIUS, 80.0D, GLOBAL_VILLAGER_WORK_RADIUS);
            for (Monster monster : level.getEntitiesOfClass(Monster.class, defendedArea)) {
                monster.discard();
            }
        }
    }

    private static void empowerMagicWorldVillager(
            Villager villager,
            VillagerProfession profession,
            BlockPos home,
            BlockPos work,
            int workRadius
    ) {
        int effectiveWorkRadius = Math.max(GLOBAL_VILLAGER_WORK_RADIUS, workRadius);
        villager.setPersistenceRequired();
        villager.setInvulnerable(true);
        villager.setCanPickUpLoot(true);
        villager.setVillagerData(villager.getVillagerData()
                .setProfession(profession)
                .setLevel(5));
        if (villager.getAttribute(Attributes.MAX_HEALTH) != null) {
            villager.getAttribute(Attributes.MAX_HEALTH).setBaseValue(80.0D);
        }
        if (villager.getAttribute(Attributes.MOVEMENT_SPEED) != null) {
            villager.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.42D);
        }
        if (villager.getAttribute(Attributes.FOLLOW_RANGE) != null) {
            villager.getAttribute(Attributes.FOLLOW_RANGE).setBaseValue(effectiveWorkRadius);
        }
        villager.setHealth(villager.getMaxHealth());
        int longDuration = 20 * 60 * 60 * 8;
        villager.addEffect(new MobEffectInstance(MobEffects.REGENERATION, longDuration, 4, true, false));
        villager.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, longDuration, 4, true, false));
        villager.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, longDuration, 1, true, false));
        villager.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, longDuration, 1, true, false));
        villager.addEffect(new MobEffectInstance(MobEffects.HERO_OF_THE_VILLAGE, longDuration, 4, true, false));
        villager.restrictTo(work, effectiveWorkRadius);
        villager.getPersistentData().putInt("MagicWorldWorkRadius", effectiveWorkRadius);
        villager.getPersistentData().putInt("MagicWorldHomeX", home.getX());
        villager.getPersistentData().putInt("MagicWorldHomeY", home.getY());
        villager.getPersistentData().putInt("MagicWorldHomeZ", home.getZ());
        villager.getPersistentData().putInt("MagicWorldWorkX", work.getX());
        villager.getPersistentData().putInt("MagicWorldWorkY", work.getY());
        villager.getPersistentData().putInt("MagicWorldWorkZ", work.getZ());
    }

    private static BlockPos plantationWorkerHouseDoorPos(BlockPos corner, Direction doorFacing) {
        return switch (doorFacing) {
            case NORTH -> corner.offset(7, 1, 0);
            case SOUTH -> corner.offset(7, 1, 10);
            case WEST -> corner.offset(0, 1, 5);
            default -> corner.offset(14, 1, 5);
        };
    }

    private static BlockPos houseDoorPos(BlockPos corner, Direction doorFacing) {
        return switch (doorFacing) {
            case NORTH -> corner.offset(6, 1, 0);
            case SOUTH -> corner.offset(6, 1, 8);
            case WEST -> corner.offset(0, 1, 4);
            default -> corner.offset(12, 1, 4);
        };
    }

    private static void placeHouseDoor(ServerLevel level, BlockPos lower, Direction facing) {
        level.setBlock(lower, Blocks.OAK_DOOR.defaultBlockState()
                .setValue(DoorBlock.FACING, facing)
                .setValue(DoorBlock.HALF, DoubleBlockHalf.LOWER), 2);
        level.setBlock(lower.above(), Blocks.OAK_DOOR.defaultBlockState()
                .setValue(DoorBlock.FACING, facing)
                .setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER), 2);
        BlockPos step = filledGroundAt(level, lower.relative(facing));
        level.setBlock(step, Blocks.SMOOTH_STONE.defaultBlockState(), 2);
    }

    private static void placeWindow(ServerLevel level, BlockPos pos) {
        level.setBlock(pos, Blocks.GLASS_PANE.defaultBlockState(), 2);
        level.setBlock(pos.above(), Blocks.GLASS_PANE.defaultBlockState(), 2);
    }

    private static void placeBed(ServerLevel level, BlockPos foot, Direction facing) {
        level.setBlock(foot, Blocks.BLUE_BED.defaultBlockState()
                .setValue(BedBlock.FACING, facing)
                .setValue(BedBlock.PART, BedPart.FOOT), 2);
        level.setBlock(foot.relative(facing), Blocks.BLUE_BED.defaultBlockState()
                .setValue(BedBlock.FACING, facing)
                .setValue(BedBlock.PART, BedPart.HEAD), 2);
    }

    private static void buildHousePathToFarm(ServerLevel level, BlockPos door, Direction direction, int length) {
        Direction side = direction.getClockWise();
        for (int step = 1; step <= length; step++) {
            BlockPos center = filledGroundAt(level, door.relative(direction, step));
            for (int width = -1; width <= 1; width++) {
                BlockPos path = center.relative(side, width);
                level.setBlock(path.below(), Blocks.DIRT.defaultBlockState(), 2);
                level.setBlock(path, Blocks.SMOOTH_STONE.defaultBlockState(), 2);
                level.setBlock(path.above(), Blocks.AIR.defaultBlockState(), 2);
            }
        }
        placeLampPost(level, door.relative(direction, Math.max(3, length / 2)).below().relative(side, 3));
    }

    private static BlockPos filledGroundAt(ServerLevel level, BlockPos approximate) {
        BlockPos surface = findHighestFeatureSurface(level, approximate, 24).below();
        if (surface.getY() < approximate.getY() - 1) {
            surface = new BlockPos(approximate.getX(), approximate.getY() - 1, approximate.getZ());
        }
        for (int y = surface.getY() - 3; y < surface.getY(); y++) {
            level.setBlock(new BlockPos(surface.getX(), y, surface.getZ()), Blocks.DIRT.defaultBlockState(), 2);
        }
        return surface;
    }

    private static void spawnAnimalGroup(ServerLevel level, EntityType<?> type, BlockPos center, int count) {
        for (int i = 0; i < count; i++) {
            spawnNamed(level, type, center.offset(i % 3, 1, i / 3), "");
        }
    }

    private static void spawnNamed(ServerLevel level, EntityType<?> type, BlockPos pos, String name) {
        AABB nearby = new AABB(pos).inflate(256.0D, 64.0D, 256.0D);
        if (!name.isBlank() && !level.getEntitiesOfClass(Entity.class, nearby, entity -> name.equals(entity.getName().getString())).isEmpty()) {
            return;
        }
        Entity entity = type.spawn(level, pos, MobSpawnType.STRUCTURE);
        if (entity == null) {
            return;
        }

        if (!name.isBlank()) {
            entity.setCustomName(Component.literal(name));
            entity.setCustomNameVisible(true);
        }
        if (entity instanceof Mob mob) {
            mob.setPersistenceRequired();
        }
        if (entity instanceof Villager villager) {
            empowerMagicWorldVillager(villager, VillagerProfession.FARMER, pos, pos, 96);
        }
    }

    private static void placeLampPost(ServerLevel level, BlockPos pos) {
        BlockPos base = findHighestFeatureSurface(level, pos, 16);
        level.setBlock(base, Blocks.POLISHED_DEEPSLATE.defaultBlockState(), 2);
        for (int y = 1; y <= 4; y++) {
            level.setBlock(base.above(y), Blocks.DARK_OAK_FENCE.defaultBlockState(), 2);
        }
        level.setBlock(base.above(5), Blocks.SEA_LANTERN.defaultBlockState(), 2);
        level.setBlock(base.above(6), Blocks.LIGHT_BLUE_STAINED_GLASS.defaultBlockState(), 2);
    }

    private static void placeChest(ServerLevel level, BlockPos pos, Direction facing) {
        if (level.getBlockEntity(pos) instanceof Container container) {
            container.clearContent();
            container.setChanged();
        }
        level.setBlock(pos, Blocks.CHEST.defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, facing), 2);
    }

    private static void placeStorageChest(ServerLevel level, BlockPos pos, Direction facing, boolean trapped) {
        if (level.getBlockEntity(pos) instanceof Container container) {
            container.clearContent();
            container.setChanged();
        }
        level.setBlock(pos, (trapped ? Blocks.TRAPPED_CHEST : Blocks.CHEST).defaultBlockState()
                .setValue(HorizontalDirectionalBlock.FACING, facing), 2);
    }

    private static void placeStorageBarrel(ServerLevel level, BlockPos pos) {
        if (level.getBlockEntity(pos) instanceof Container container) {
            container.clearContent();
            container.setChanged();
        }
        level.setBlock(pos, Blocks.BARREL.defaultBlockState(), 2);
    }

    private static void fillContainersWithAllRegisteredItems(ServerLevel level, List<BlockPos> containers) {
        List<ItemStack> registeredItems = allRegisteredItemStacks();
        if (registeredItems.isEmpty()) {
            return;
        }

        int itemIndex = 0;
        for (BlockPos pos : containers) {
            if (!(level.getBlockEntity(pos) instanceof Container container)) {
                continue;
            }
            for (int slot = 0; slot < container.getContainerSize(); slot++) {
                ItemStack stack = registeredItems.get(itemIndex % registeredItems.size()).copy();
                container.setItem(slot, stack);
                itemIndex++;
            }
            container.setChanged();
        }
    }

    private static List<ItemStack> allRegisteredItemStacks() {
        List<ItemStack> stacks = new ArrayList<>();
        for (Item item : BuiltInRegistries.ITEM) {
            ItemStack stack = new ItemStack(item);
            if (stack.isEmpty()) {
                continue;
            }
            if (stack.getMaxStackSize() > 1) {
                stack.setCount(Math.min(64, stack.getMaxStackSize()));
            }
            stacks.add(stack);
        }
        return stacks;
    }

    private static void putItems(ServerLevel level, BlockPos pos, ItemStack... items) {
        if (pos == null || !(level.getBlockEntity(pos) instanceof Container container) || items.length == 0) {
            return;
        }

        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack stack = items[i % items.length].copy();
            if (stack.getMaxStackSize() > 1) {
                stack.setCount(Math.min(stack.getMaxStackSize(), stack.getCount()));
            }
            container.setItem(i, stack);
        }
        container.setChanged();
    }

    private static void fillContainerWithItem(ServerLevel level, BlockPos pos, Item item) {
        if (pos == null || !(level.getBlockEntity(pos) instanceof Container container)) {
            return;
        }
        for (int slot = 0; slot < container.getContainerSize(); slot++) {
            container.setItem(slot, new ItemStack(item));
        }
        container.setChanged();
    }

    private static BlockState flowerFor(int seed) {
        return switch (Math.floorMod(seed, 8)) {
            case 0 -> Blocks.POPPY.defaultBlockState();
            case 1 -> Blocks.DANDELION.defaultBlockState();
            case 2 -> Blocks.BLUE_ORCHID.defaultBlockState();
            case 3 -> Blocks.ALLIUM.defaultBlockState();
            case 4 -> Blocks.AZURE_BLUET.defaultBlockState();
            case 5 -> Blocks.RED_TULIP.defaultBlockState();
            case 6 -> Blocks.OXEYE_DAISY.defaultBlockState();
            default -> Blocks.CORNFLOWER.defaultBlockState();
        };
    }
}
