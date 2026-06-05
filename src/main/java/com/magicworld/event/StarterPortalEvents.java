package com.magicworld.event;

import com.magicworld.MagicWorld;
import com.magicworld.MagicWorldWorldOptions;
import com.magicworld.network.MagicWorldNetwork;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
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
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.RelativeMovement;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
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
import net.minecraft.world.level.block.StairBlock;
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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class StarterPortalEvents {
    private static final String ESTATE_CREATED_KEY = "MagicWorldForgeStarterEstateCreated";
    private static final String ESTATE_BASE_X_KEY = "MagicWorldForgeStarterEstateBaseX";
    private static final String ESTATE_BASE_Y_KEY = "MagicWorldForgeStarterEstateBaseY";
    private static final String ESTATE_BASE_Z_KEY = "MagicWorldForgeStarterEstateBaseZ";
    private static final String PREMIUM_UNLOCKED_KEY = "MagicWorldForgePremiumUnlocked";
    private static final String PORTAL_COOLDOWN_KEY = "MagicWorldForgePortalCooldown";
    private static final String RETURN_PORTAL_PREFIX = "MagicWorldForgeReturnPortal";

    private static final ResourceLocation IMPORTED_HOUSE = new ResourceLocation(MagicWorld.MODID, "imported_house");
    private static final ResourceLocation IMPORTED_CASTLE = new ResourceLocation(MagicWorld.MODID, "imported_castle");

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
    private static final int CASTLE_SIZE_X = 265;
    private static final int CASTLE_SIZE_Z = 221;

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
                buildImportedHouse(level, task.base);
                TASKS.put(player.getUUID(), new EstateTask(task.base, 1, STEP_DELAY_TICKS));
            }
            case 1 -> {
                if (MagicWorldWorldOptions.isFarmsEnabled()) {
                    MagicWorldNetwork.sendInitialLoadProgress(player, 35, "Carregando fazendas, animais e trabalhadores...", false);
                    buildImportedEstateFarms(level, task.base);
                    spawnImportedStarterAnimals(level, task.base);
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
                }
                TASKS.put(player.getUUID(), new EstateTask(task.base, 5, FINAL_DELAY_TICKS));
            }
            default -> {
                player.getPersistentData().putBoolean(ESTATE_CREATED_KEY, true);
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
            fillStarterChests(level, base);
            decorateImportedHouseAddons(level, base);
        } else {
            buildFallbackHouse(level, base);
        }
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
                        level.setBlock(mutable, Blocks.AIR.defaultBlockState(), 2);
                    }
                }
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
                level.setBlock(mutable, Blocks.GRASS_BLOCK.defaultBlockState(), 2);
                for (int y = 0; y <= 18; y++) {
                    mutable.set(origin.getX() + x, origin.getY() + y, origin.getZ() + z);
                    level.setBlock(mutable, Blocks.AIR.defaultBlockState(), 2);
                }
            }
        }
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
        buildAnimalFeedGarden(level, base.offset(78, -1, -10), 22, 12);
        buildPlantationWorkerSettlement(level, base);
        buildAnimalCaretakerSettlement(level, base);
        buildStarterEstateRoads(level, base);
        buildGreenVillageSquare(level, base);
        buildStoneTreasureMineHouse(level, base.offset(67, -1, 46));
        buildEstateLivingBorder(level, base, -128, 122, -76, 80);
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
        level.setBlock(corner.offset(width / 2, 1, depth), Blocks.OAK_FENCE_GATE.defaultBlockState()
                .setValue(HorizontalDirectionalBlock.FACING, Direction.SOUTH), 2);
        level.setBlock(corner.offset(0, 1, depth / 2), Blocks.OAK_FENCE_GATE.defaultBlockState()
                .setValue(HorizontalDirectionalBlock.FACING, Direction.WEST), 2);
        level.setBlock(corner.offset(width, 1, depth / 2), Blocks.OAK_FENCE_GATE.defaultBlockState()
                .setValue(HorizontalDirectionalBlock.FACING, Direction.EAST), 2);
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
            buildHousePathToFarm(level, plantationWorkerHouseDoorPos(houses[i], Direction.SOUTH), Direction.SOUTH, 8);
            buildHousePathToFarm(level, plantationWorkerHouseDoorPos(houses[i], Direction.NORTH), Direction.NORTH, 10);
            for (int villager = 0; villager < 2; villager++) {
                spawnWorkerVillager(level, houses[i].offset(2 + villager * 4, 1, villager % 2 == 0 ? 7 : 3),
                        "Agricultor da Plantacao Casa " + (i + 1) + "-" + (villager + 1),
                        warehouseCenter, farmCenters[i], Items.WHEAT);
            }
        }

        for (int i = 0; i < 4; i++) {
            spawnWorkerVillager(level, warehouseCenter.offset(-3 + i * 2, 0, i % 2 == 0 ? 2 : -2),
                    "Morador do Rancho da Plantacao " + (i + 1), warehouseCenter, farmCenters[i % farmCenters.length], Items.CARROT);
        }

        for (BlockPos center : farmCenters) {
            level.setBlock(center, Blocks.COMPOSTER.defaultBlockState(), 2);
            placeLampPost(level, center.offset(0, -1, -11));
        }
    }

    private static void buildAnimalCaretakerSettlement(ServerLevel level, BlockPos base) {
        BlockPos[] houses = {
                base.offset(106, -1, -72),
                base.offset(106, -1, -56)
        };
        BlockPos foodFarm = base.offset(92, -1, -52);
        BlockPos farmCenter = foodFarm.offset(1, 0, 16);

        for (int i = 0; i < houses.length; i++) {
            buildCaretakerHouse(level, houses[i], Direction.WEST);
            buildHousePathToFarm(level, houseDoorPos(houses[i], Direction.WEST), Direction.WEST, 8);
            for (int villager = 0; villager < 2; villager++) {
                spawnWorkerVillager(level, houses[i].offset(4, 1, 3 + villager * 2),
                        "Cuidador da Fazenda Casa " + (i + 1) + "-" + (villager + 1),
                        houses[i].offset(5, 1, 4), farmCenter, Items.WHEAT);
            }
        }

        buildAnimalFoodStripFarm(level, foodFarm, 4, 32);
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

        placeLampPost(level, corner.offset(-3, 0, depth / 2));
        placeLampPost(level, corner.offset(width + 3, 0, depth / 2));
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
        buildRoadBetween(level, base.offset(-118, -1, -48), base.offset(-94, -1, -48));
        buildRoadBetween(level, base.offset(104, -1, -64), base.offset(104, -1, -42));
    }

    private static void buildStarterRoad(ServerLevel level, BlockPos base, Direction direction, int length) {
        Direction side = direction.getClockWise();
        for (int step = 0; step <= length; step++) {
            BlockPos center = filledGroundAt(level, base.relative(direction, step));
            for (int width = -2; width <= 2; width++) {
                BlockPos pos = center.relative(side, width);
                level.setBlock(pos.below(), Blocks.DIRT.defaultBlockState(), 2);
                level.setBlock(pos, Math.abs(width) == 2 ? Blocks.POLISHED_ANDESITE.defaultBlockState() : Blocks.SMOOTH_STONE.defaultBlockState(), 2);
                level.setBlock(pos.above(), Blocks.AIR.defaultBlockState(), 2);
                level.setBlock(pos.above(2), Blocks.AIR.defaultBlockState(), 2);
            }
            if (step > 0 && step % 16 == 0) {
                placeLampPost(level, center.relative(side, 4));
                placeLampPost(level, center.relative(side, -4));
            }
        }
    }

    private static void buildRoadBetween(ServerLevel level, BlockPos from, BlockPos to) {
        int dx = Integer.compare(to.getX(), from.getX());
        int dz = Integer.compare(to.getZ(), from.getZ());
        BlockPos pos = filledGroundAt(level, from);
        int guard = 0;
        while ((pos.getX() != to.getX() || pos.getZ() != to.getZ()) && guard++ < 256) {
            for (int x = -1; x <= 1; x++) {
                for (int z = -1; z <= 1; z++) {
                    BlockPos road = filledGroundAt(level, pos.offset(x, 0, z));
                    level.setBlock(road.below(), Blocks.DIRT.defaultBlockState(), 2);
                    level.setBlock(road, Blocks.SMOOTH_STONE.defaultBlockState(), 2);
                    level.setBlock(road.above(), Blocks.AIR.defaultBlockState(), 2);
                }
            }
            if (pos.getX() != to.getX()) {
                pos = pos.offset(dx, 0, 0);
            } else if (pos.getZ() != to.getZ()) {
                pos = pos.offset(0, 0, dz);
            }
        }
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

    private static void buildGreenVillageSquare(ServerLevel level, BlockPos base) {
        BlockPos center = filledGroundAt(level, base.offset(-54, 0, 96));
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

        level.setBlock(center, Blocks.BELL.defaultBlockState(), 2);
        level.setBlock(center.offset(0, 1, 0), Blocks.LANTERN.defaultBlockState(), 2);
        buildCommunityHall(level, center.offset(-8, 0, -28));
        buildSimplePlantationWorkerHouse(level, center.offset(-23, 0, 8), Direction.SOUTH, Direction.NORTH);
        buildSimplePlantationWorkerHouse(level, center.offset(9, 0, 8), Direction.SOUTH, Direction.NORTH);

        for (BlockPos lamp : new BlockPos[] {
                center.offset(-18, 0, -14), center.offset(18, 0, -14),
                center.offset(-18, 0, 14), center.offset(18, 0, 14),
                center.offset(0, 0, -14), center.offset(0, 0, 14)
        }) {
            placeLampPost(level, lamp);
        }

        spawnAnimalGroup(level, EntityType.CAT, center.offset(-7, 1, 8), 3);
        spawnAnimalGroup(level, EntityType.PARROT, center.offset(7, 1, 8), 3);
        spawnWorkerVillager(level, center.offset(-2, 1, 3), "Morador da Praca Verde 1", center, center, Items.EMERALD);
        spawnWorkerVillager(level, center.offset(2, 1, 3), "Morador da Praca Verde 2", center, center, Items.BREAD);
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
        BlockPos center = filledGroundAt(level, approximateCenter);
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
        spawnWorkerVillager(level, center.offset(0, 1, 2), "Minerador Magic World", center, center, Items.DIAMOND_PICKAXE);
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
                base.offset(106, -1, -72),
                base.offset(106, -1, -56)
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
            placeBorderAccent(level, base.offset(x, 0, minZ), x, minZ);
            placeBorderAccent(level, base.offset(x, 0, maxZ), x, maxZ);
        }
        for (int z = minZ; z <= maxZ; z++) {
            placeBorderAccent(level, base.offset(minX, 0, z), minX, z);
            placeBorderAccent(level, base.offset(maxX, 0, z), maxX, z);
        }
    }

    private static void placeBorderAccent(ServerLevel level, BlockPos ground, int xRel, int zRel) {
        level.setBlock(ground, Blocks.GRASS_BLOCK.defaultBlockState(), 2);
        if (Math.floorMod(xRel * 7 + zRel * 11, 9) == 0) {
            level.setBlock(ground.above(), Blocks.FLOWERING_AZALEA.defaultBlockState(), 2);
        } else if (Math.floorMod(xRel + zRel, 17) == 0) {
            level.setBlock(ground.above(), flowerFor(xRel + zRel), 2);
        }
    }

    private static void spawnImportedStarterAnimals(ServerLevel level, BlockPos base) {
        spawnAnimalGroup(level, EntityType.COW, base.offset(51, 0, -56), 4);
        spawnAnimalGroup(level, EntityType.PIG, base.offset(67, 0, -56), 4);
        spawnAnimalGroup(level, EntityType.SHEEP, base.offset(83, 0, -56), 4);
        spawnAnimalGroup(level, EntityType.CHICKEN, base.offset(51, 0, -38), 6);
        spawnAnimalGroup(level, EntityType.GOAT, base.offset(67, 0, -38), 4);
        spawnAnimalGroup(level, EntityType.RABBIT, base.offset(83, 0, -38), 6);
        spawnAnimalGroup(level, EntityType.LLAMA, base.offset(51, 0, -22), 4);
        spawnAnimalGroup(level, EntityType.HORSE, base.offset(67, 0, -22), 3);
        spawnAnimalGroup(level, EntityType.DONKEY, base.offset(83, 0, -22), 3);
        spawnAnimalGroup(level, EntityType.CAMEL, base.offset(67, 0, -6), 3);
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
        placeLampPost(level, center.offset(-10, 1, -10));
        placeLampPost(level, center.offset(10, 1, -10));
        placeMagicWorldGearChest(level, center.offset(-10, 0, 6));
        placeMagicWorldGearChest(level, center.offset(10, 0, 6));
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
        boolean active = !player.getPersistentData().getBoolean(PREMIUM_UNLOCKED_KEY);
        player.getPersistentData().putBoolean(PREMIUM_UNLOCKED_KEY, active);

        if (active) {
            player.sendSystemMessage(Component.literal("Magic World: experiencia premium ativada."));
            player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 20 * 60, 0));
            player.addEffect(new MobEffectInstance(MobEffects.LUCK, 20 * 60, 0));
        } else {
            player.sendSystemMessage(Component.literal("Magic World: experiencia premium desativada."));
        }

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
                new ItemStack(MagicWorld.VARINHA_MAGICA.get()));
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
    }

    private static void decorateCastleStarterLife(ServerLevel level, BlockPos center) {
        placeMagicWorldGearChest(level, center.offset(0, 1, -18));
        spawnNamed(level, EntityType.IRON_GOLEM, center.offset(0, 1, -10), "Guardiao do Castelo");
        spawnNamed(level, EntityType.VILLAGER, center.offset(4, 1, -8), "Mordomo do Castelo");
        spawnNamed(level, EntityType.VILLAGER, center.offset(-4, 1, -8), "Ferreiro do Castelo");
        populateCastleResidents(level, center);
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
                            || !level.getBlockState(mutable.below()).isSolid()) {
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
        if (spot == null) {
            spot = castleGroundResidentSpot(level, requested);
        }
        AABB nearby = new AABB(spot).inflate(28.0D, 20.0D, 28.0D);
        if (level.getEntitiesOfClass(Villager.class, nearby, villager -> name.equals(villager.getName().getString())).isEmpty()) {
            Entity entity = EntityType.VILLAGER.spawn(level, spot, MobSpawnType.STRUCTURE);
            if (entity instanceof Villager villager) {
                villager.setCustomName(Component.literal(name));
                villager.setCustomNameVisible(true);
                villager.setPersistenceRequired();
                villager.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(heldItem));
                villager.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 20 * 60, 0));
            }
        }
        decorateCastleResidentStation(level, spot, facing, jobBlock, supplies);
    }

    private static BlockPos castleGroundResidentSpot(ServerLevel level, BlockPos requested) {
        BlockPos surface = findHighestFeatureSurface(level, requested, 96);
        if (surface.getY() > requested.getY() + 8) {
            return requested;
        }
        return surface;
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

    private static void spawnWorkerVillager(ServerLevel level, BlockPos pos, String name, BlockPos home, BlockPos farmCenter, net.minecraft.world.item.Item heldItem) {
        AABB nearby = new AABB(pos).inflate(20.0D, 8.0D, 20.0D);
        if (!level.getEntitiesOfClass(Villager.class, nearby, villager -> name.equals(villager.getName().getString())).isEmpty()) {
            return;
        }
        Entity entity = EntityType.VILLAGER.spawn(level, pos, MobSpawnType.STRUCTURE);
        if (entity instanceof Villager villager) {
            villager.setCustomName(Component.literal(name));
            villager.setCustomNameVisible(true);
            villager.setPersistenceRequired();
            villager.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(heldItem));
            villager.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 20 * 60, 0));
            villager.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 20 * 60, 0));
            villager.getPersistentData().putInt("MagicWorldHomeX", home.getX());
            villager.getPersistentData().putInt("MagicWorldHomeY", home.getY());
            villager.getPersistentData().putInt("MagicWorldHomeZ", home.getZ());
            villager.getPersistentData().putInt("MagicWorldFarmX", farmCenter.getX());
            villager.getPersistentData().putInt("MagicWorldFarmY", farmCenter.getY());
            villager.getPersistentData().putInt("MagicWorldFarmZ", farmCenter.getZ());
        }
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
        AABB nearby = new AABB(pos).inflate(24.0D, 12.0D, 24.0D);
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
            villager.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 20 * 60, 0));
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
        level.setBlock(pos, Blocks.CHEST.defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, facing), 2);
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
