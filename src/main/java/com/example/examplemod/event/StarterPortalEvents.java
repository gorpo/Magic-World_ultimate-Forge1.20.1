package com.example.examplemod.event;

import com.example.examplemod.ExampleMod;
import com.example.examplemod.MagicWorldWorldOptions;
import com.example.examplemod.network.MagicWorldNetwork;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class StarterPortalEvents {
    private static final String ESTATE_CREATED_KEY = "MagicWorldForgeStarterEstateCreated";
    private static final String PREMIUM_UNLOCKED_KEY = "MagicWorldForgePremiumUnlocked";
    private static final ResourceLocation IMPORTED_HOUSE = new ResourceLocation(ExampleMod.MODID, "imported_house");
    private static final ResourceLocation IMPORTED_CASTLE = new ResourceLocation(ExampleMod.MODID, "imported_castle");
    private static final int START_DELAY_TICKS = 40;
    private static final int STEP_DELAY_TICKS = 120;
    private static final int FINAL_DELAY_TICKS = 100;
    private static final int BREATHING_MARGIN = 8;
    private static final int PORTAL_Z_OFFSET = 70;
    private static final int CASTLE_Z_OFFSET = 90;
    private static final int CASTLE_X_OFFSET = 40;
    private static final int HOUSE_ORIGIN_X = -83;
    private static final int HOUSE_ORIGIN_Y = -4;
    private static final int HOUSE_ORIGIN_Z = -61;
    private static final int HOUSE_SIZE_X = 119;
    private static final int HOUSE_SIZE_Z = 131;
    private static final int CASTLE_SIZE_X = 265;
    private static final int CASTLE_SIZE_Z = 221;
    private static final Map<UUID, EstateTask> TASKS = new HashMap<>();
    private static final Map<UUID, Integer> PORTAL_COOLDOWNS = new HashMap<>();

    private record EstateTask(BlockPos base, int step, int ticksUntilNextStep) {
    }

    @SubscribeEvent
    public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)
                || player.level().isClientSide()
                || !player.level().dimension().equals(Level.OVERWORLD)) {
            return;
        }

        CompoundTag data = player.getPersistentData();
        if (!MagicWorldWorldOptions.isStarterEstateEnabled()
                || data.getBoolean(ESTATE_CREATED_KEY)
                || TASKS.containsKey(player.getUUID())) {
            return;
        }

        TASKS.put(player.getUUID(), new EstateTask(findEstateBase(player), 0, START_DELAY_TICKS));
        MagicWorldNetwork.openInitialLoadNotice(player);
        MagicWorldNetwork.sendInitialLoadProgress(player, 3, "Preparando o carregamento inicial...", false);
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END
                || !(event.player instanceof ServerPlayer player)
                || player.level().isClientSide()) {
            return;
        }

        handleEstateTask(player);
        handlePortalEntry(player);
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
            activatePortal(player, marker);
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
                MagicWorldNetwork.sendInitialLoadProgress(player, 12, "Carregando casa...", false);
                buildImportedHouse(level, task.base);
                TASKS.put(player.getUUID(), new EstateTask(task.base, 1, STEP_DELAY_TICKS));
            }
            case 1 -> {
                MagicWorldNetwork.sendInitialLoadProgress(player, 35, "Carregando fazendas e animais...", false);
                buildStarterLife(level, task.base);
                TASKS.put(player.getUUID(), new EstateTask(task.base, 2, STEP_DELAY_TICKS));
            }
            case 2 -> {
                MagicWorldNetwork.sendInitialLoadProgress(player, 55, "Carregando portal...", false);
                buildStarterPortal(level, portalBase(task.base));
                TASKS.put(player.getUUID(), new EstateTask(task.base, 3, STEP_DELAY_TICKS));
            }
            case 3 -> {
                MagicWorldNetwork.sendInitialLoadProgress(player, 73, "Carregando castelo...", false);
                buildImportedCastle(level, castleOrigin(task.base));
                TASKS.put(player.getUUID(), new EstateTask(task.base, 4, STEP_DELAY_TICKS));
            }
            case 4 -> {
                MagicWorldNetwork.sendInitialLoadProgress(player, 88, "Carregando dragao...", false);
                spawnPeacefulDecorativeDragon(level, castleCenter(task.base));
                TASKS.put(player.getUUID(), new EstateTask(task.base, 5, FINAL_DELAY_TICKS));
            }
            case 5 -> {
                MagicWorldNetwork.sendInitialLoadProgress(player, 98, "Finalizando estruturas do Magic World", false);
                TASKS.put(player.getUUID(), new EstateTask(task.base, 6, FINAL_DELAY_TICKS));
            }
            default -> {
                player.getPersistentData().putBoolean(ESTATE_CREATED_KEY, true);
                MagicWorldNetwork.sendInitialLoadProgress(player, 100, "Tudo carregado.", true);
                player.sendSystemMessage(Component.literal("Magic World: casa, portal e castelo carregados."));
                TASKS.remove(player.getUUID());
            }
        }
    }

    private static BlockPos findEstateBase(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        BlockPos spawn = level.getSharedSpawnPos();
        int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, spawn.getX(), spawn.getZ());
        return new BlockPos(spawn.getX(), y, spawn.getZ());
    }

    private static BlockPos houseOrigin(BlockPos base) {
        return base.offset(HOUSE_ORIGIN_X, HOUSE_ORIGIN_Y, HOUSE_ORIGIN_Z);
    }

    private static BlockPos portalBase(BlockPos base) {
        int y = base.getY();
        return new BlockPos(base.getX(), y, base.getZ() + PORTAL_Z_OFFSET);
    }

    private static BlockPos castleCenter(BlockPos base) {
        return new BlockPos(base.getX() + CASTLE_X_OFFSET, base.getY(), base.getZ() + CASTLE_Z_OFFSET);
    }

    private static BlockPos castleOrigin(BlockPos base) {
        BlockPos center = castleCenter(base);
        return center.offset(-(CASTLE_SIZE_X / 2), 0, -(CASTLE_SIZE_Z / 2));
    }

    private static void buildImportedHouse(ServerLevel level, BlockPos base) {
        BlockPos origin = houseOrigin(base);
        Optional<StructureTemplate> optional = level.getStructureManager().get(IMPORTED_HOUSE);
        if (optional.isPresent()) {
            StructureTemplate template = optional.get();
            clearStructureVolume(level, origin, template.getSize(), BREATHING_MARGIN, true);
            template.placeInWorld(level, origin, origin, new StructurePlaceSettings(), RandomSource.create(level.getSeed()), 2);
            prepareBreathingSurface(level, origin, template.getSize(), BREATHING_MARGIN);
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
                level.setBlock(floor, Blocks.GRASS_BLOCK.defaultBlockState(), 3);

                for (int y = minY; y <= maxY; y++) {
                    mutable.set(origin.getX() + x, y, origin.getZ() + z);
                    if (!level.getBlockState(mutable).isAir()) {
                        level.setBlock(mutable, Blocks.AIR.defaultBlockState(), 3);
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
                level.setBlock(mutable, Blocks.GRASS_BLOCK.defaultBlockState(), 3);
                for (int y = 0; y <= 18; y++) {
                    mutable.set(origin.getX() + x, origin.getY() + y, origin.getZ() + z);
                    level.setBlock(mutable, Blocks.AIR.defaultBlockState(), 3);
                }
            }
        }
    }

    private static void buildStarterPortal(ServerLevel level, BlockPos center) {
        Vec3i size = new Vec3i(33, 24, 33);
        clearStructureVolume(level, center.offset(-16, 0, -16), size, 0, true);

        for (int x = -6; x <= 6; x++) {
            for (int z = -6; z <= 6; z++) {
                level.setBlock(center.offset(x, -1, z), Blocks.GRASS_BLOCK.defaultBlockState(), 3);
            }
        }

        for (int y = 0; y <= 5; y++) {
            level.setBlock(center.offset(-2, y, 0), Blocks.CRYING_OBSIDIAN.defaultBlockState(), 3);
            level.setBlock(center.offset(2, y, 0), Blocks.CRYING_OBSIDIAN.defaultBlockState(), 3);
        }
        for (int x = -2; x <= 2; x++) {
            level.setBlock(center.offset(x, 0, 0), Blocks.END_PORTAL_FRAME.defaultBlockState(), 3);
            level.setBlock(center.offset(x, 5, 0), Blocks.CRYING_OBSIDIAN.defaultBlockState(), 3);
        }
        for (int y = 1; y <= 4; y++) {
            level.setBlock(center.offset(-1, y, 0), Blocks.PURPLE_STAINED_GLASS.defaultBlockState(), 3);
            level.setBlock(center.offset(0, y, 0), Blocks.AIR.defaultBlockState(), 3);
            level.setBlock(center.offset(1, y, 0), Blocks.MAGENTA_STAINED_GLASS.defaultBlockState(), 3);
        }
        level.setBlock(center.offset(0, 1, -1), Blocks.AMETHYST_BLOCK.defaultBlockState(), 3);
        level.setBlock(center.offset(0, 1, 1), Blocks.AMETHYST_BLOCK.defaultBlockState(), 3);
        placeSign(level, center.offset(0, 0, -3));
    }

    private static void placeSign(ServerLevel level, BlockPos pos) {
        level.setBlock(pos, Blocks.OAK_SIGN.defaultBlockState(), 3);
    }

    private static void buildStarterLife(ServerLevel level, BlockPos base) {
        spawnNamed(level, EntityType.GOAT, base.offset(4, 1, 4), "Guardiao da Casa");
        spawnNamed(level, EntityType.VILLAGER, base.offset(7, 1, 4), "Morador Magic World");
        spawnNamed(level, EntityType.COW, base.offset(-9, 1, 8), "Vaca Premium");
        spawnNamed(level, EntityType.SHEEP, base.offset(-11, 1, 9), "Ovelha Premium");
        spawnNamed(level, EntityType.HORSE, base.offset(-13, 1, 10), "Montaria Inicial");

        placeChest(level, base.offset(2, 1, 2), Direction.SOUTH);
        if (level.getBlockEntity(base.offset(2, 1, 2)) instanceof net.minecraft.world.Container container) {
            container.setItem(0, new ItemStack(Items.BREAD, 32));
            container.setItem(1, new ItemStack(Items.TORCH, 64));
            container.setItem(2, new ItemStack(Items.OAK_LOG, 64));
            container.setItem(3, new ItemStack(Items.IRON_PICKAXE));
            container.setChanged();
        }
    }

    private static void buildFallbackHouse(ServerLevel level, BlockPos base) {
        clearStructureVolume(level, base.offset(-8, 0, -8), new Vec3i(17, 12, 17), BREATHING_MARGIN, true);
        for (int x = -5; x <= 5; x++) {
            for (int z = -5; z <= 5; z++) {
                level.setBlock(base.offset(x, 0, z), Blocks.OAK_PLANKS.defaultBlockState(), 3);
                if (Math.abs(x) == 5 || Math.abs(z) == 5) {
                    for (int y = 1; y <= 4; y++) {
                        level.setBlock(base.offset(x, y, z), Blocks.OAK_LOG.defaultBlockState(), 3);
                    }
                }
            }
        }
        for (int x = -5; x <= 5; x++) {
            for (int z = -5; z <= 5; z++) {
                level.setBlock(base.offset(x, 5, z), Blocks.SPRUCE_PLANKS.defaultBlockState(), 3);
            }
        }
    }

    private static void buildFallbackCastle(ServerLevel level, BlockPos center) {
        clearStructureVolume(level, center.offset(-32, 0, -32), new Vec3i(65, 28, 65), BREATHING_MARGIN, true);
        for (int x = -28; x <= 28; x++) {
            for (int z = -28; z <= 28; z++) {
                level.setBlock(center.offset(x, 0, z), Blocks.STONE_BRICKS.defaultBlockState(), 3);
                if (Math.abs(x) == 28 || Math.abs(z) == 28) {
                    for (int y = 1; y <= 10; y++) {
                        level.setBlock(center.offset(x, y, z), Blocks.DEEPSLATE_BRICKS.defaultBlockState(), 3);
                    }
                }
            }
        }
    }

    private static void spawnPeacefulDecorativeDragon(ServerLevel level, BlockPos center) {
        EnderDragon dragon = EntityType.ENDER_DRAGON.create(level);
        if (dragon == null) {
            ArmorStand stand = EntityType.ARMOR_STAND.create(level);
            if (stand != null) {
                stand.setCustomName(Component.literal("Dragao Magic World"));
                stand.setCustomNameVisible(true);
                stand.moveTo(center.getX() + 0.5D, center.getY() + 32.0D, center.getZ() + 0.5D, 0.0F, 0.0F);
                level.addFreshEntity(stand);
            }
            return;
        }

        dragon.setCustomName(Component.literal("Dragao Pacifico Magic World"));
        dragon.setCustomNameVisible(true);
        dragon.setInvulnerable(true);
        dragon.setNoAi(true);
        dragon.noPhysics = true;
        dragon.moveTo(center.getX() + 0.5D, center.getY() + 38.0D, center.getZ() + 0.5D, 0.0F, 0.0F);
        level.addFreshEntity(dragon);
    }

    private static void spawnNamed(ServerLevel level, EntityType<?> type, BlockPos pos, String name) {
        Entity entity = type.spawn(level, pos, MobSpawnType.STRUCTURE);
        if (entity == null) {
            return;
        }

        entity.setCustomName(Component.literal(name));
        entity.setCustomNameVisible(true);
        if (entity instanceof Villager villager) {
            villager.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 20 * 60, 0));
        }
    }

    private static void placeChest(ServerLevel level, BlockPos pos, Direction facing) {
        BlockState state = Blocks.CHEST.defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, facing);
        level.setBlock(pos, state, 3);
    }

    private static void handlePortalEntry(ServerPlayer player) {
        Integer cooldown = PORTAL_COOLDOWNS.get(player.getUUID());
        if (cooldown != null && cooldown > 0) {
            PORTAL_COOLDOWNS.put(player.getUUID(), cooldown - 1);
            return;
        }

        if (!(player.level() instanceof ServerLevel level)) {
            return;
        }

        BlockPos marker = findNearestStarterPortalMarker(level, player.blockPosition(), 4);
        if (marker == null) {
            return;
        }

        activatePortal(player, marker);
    }

    private static void activatePortal(ServerPlayer player, BlockPos marker) {
        PORTAL_COOLDOWNS.put(player.getUUID(), 60);
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
            ExampleMod.effects(level, marker);
        }
    }

    private static BlockPos findNearestStarterPortalMarker(ServerLevel level, BlockPos center, int radius) {
        BlockPos min = center.offset(-radius, -4, -radius);
        BlockPos max = center.offset(radius, 12, radius);
        for (BlockPos pos : BlockPos.betweenClosed(min, max)) {
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
}
