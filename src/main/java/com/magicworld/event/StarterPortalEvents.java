package com.magicworld.event;

import com.magicworld.Config;
import com.magicworld.MagicWorld;
import com.magicworld.MagicWorldWorldOptions;
import com.magicworld.entity.PeacefulDragon;
import com.magicworld.network.MagicWorldNetwork;
import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.Container;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RailBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.StandingSignBlock;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SignText;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public final class StarterPortalEvents {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String PLAYER_UNLOCKED_KEY = "MagicWorldVisualExperienceUnlocked";
    private static final String PLAYER_ESTATE_CREATED_KEY = "MagicWorldStarterEstateCreated";
    private static final String PLAYER_PREMIUM_RESOURCE_PACK_KEY = "MagicWorldPremiumResourcePack";
    private static final String PLAYER_PREMIUM_SHADER_PACK_KEY = "MagicWorldPremiumShaderPack";
    private static final String PLAYER_PREMIUM_COMPLETE_PACK_KEY = "MagicWorldPremiumCompletePack";
    private static final String PLAYER_INITIAL_LOAD_NOTICE_KEY = "MagicWorldInitialLoadNoticeShown";
    private static final Identifier IMPORTED_HOUSE_STRUCTURE = Identifier.fromNamespaceAndPath(MagicWorld.MODID, "imported_house");
    private static final Identifier IMPORTED_CASTLE_STRUCTURE = Identifier.fromNamespaceAndPath(MagicWorld.MODID, "imported_castle");
    private static final Set<UUID> PLAYERS_CHOOSING_PREMIUM_OPTIONS = new HashSet<>();
    private static final int STARTER_PORTAL_SPAWN_DELAY_TICKS = 20;
    private static final int STARTER_PORTAL_CHEST_SEARCH_RADIUS = 24;
    private static final int HOUSE_TERRAIN_RADIUS = 128;
    private static final int STRUCTURE_BREATHING_MARGIN = 8;
    private static final int PORTAL_Z_OFFSET = 70;
    private static final int CASTLE_Z_OFFSET_FROM_BASE = 90;
    private static final int CASTLE_X_OFFSET_FROM_PORTAL = 40;
    private static final int CASTLE_TERRAIN_RADIUS = 28;
    private static final int IMPORTED_HOUSE_ORIGIN_X_FROM_BASE = -83;
    private static final int IMPORTED_HOUSE_ORIGIN_Y_FROM_BASE = -4;
    private static final int IMPORTED_HOUSE_ORIGIN_Z_FROM_BASE = -61;
    private static final int IMPORTED_HOUSE_SIZE_X = 119;
    private static final int IMPORTED_HOUSE_SIZE_Z = 131;
    private static final int IMPORTED_HOUSE_MAX_X_FROM_BASE = IMPORTED_HOUSE_ORIGIN_X_FROM_BASE + IMPORTED_HOUSE_SIZE_X - 1;
    private static final int IMPORTED_HOUSE_MAX_Z_FROM_BASE = IMPORTED_HOUSE_ORIGIN_Z_FROM_BASE + IMPORTED_HOUSE_SIZE_Z - 1;
    private static final int IMPORTED_CASTLE_SIZE_X = 265;
    private static final int IMPORTED_CASTLE_SIZE_Z = 221;
    private static final int IMPORTED_CASTLE_CENTER_X_REL = IMPORTED_CASTLE_SIZE_X / 2;
    private static final int IMPORTED_CASTLE_CENTER_Z_REL = IMPORTED_CASTLE_SIZE_Z / 2;
    private static final int ESTATE_STEP_DELAY_TICKS = 180;
    private static final int ESTATE_PORTAL_DELAY_TICKS = 360;
    private static final int ESTATE_POST_DRAGON_DELAY_TICKS = 100;
    private static final int ESTATE_FINALIZING_DELAY_TICKS = 100;
    private static final Map<UUID, Integer> PENDING_STARTER_PORTAL_SPAWNS = new HashMap<>();
    private static final Map<UUID, EstateExpansionTask> PENDING_ESTATE_EXPANSIONS = new HashMap<>();
    private static final Map<UUID, UUID> FOLLOWING_ALLIES = new HashMap<>();
    private static final Map<UUID, BlockPos> CASTLE_PATROL_GUARDS = new HashMap<>();
    private static final Set<BlockPos> PROTECTED_RAIL_TUNNELS = new HashSet<>();
    private static final Set<UUID> ALLY_ENTITY_IDS = new HashSet<>();
    private static final Set<UUID> PLAYERS_TOUCHING_STARTER_PORTAL = new HashSet<>();

    private StarterPortalEvents() {
    }

    private record EstateExpansionTask(BlockPos base, int step, int ticksUntilNextStep, boolean importedHouse) {
    }

    public static void registerListeners() {
        NeoForge.EVENT_BUS.addListener(StarterPortalEvents::onPlayerLoggedIn);
        NeoForge.EVENT_BUS.addListener(StarterPortalEvents::onRightClickBlock);
        NeoForge.EVENT_BUS.addListener(StarterPortalEvents::onRightClickEntity);
        NeoForge.EVENT_BUS.addListener(StarterPortalEvents::onPlayerTick);
    }

    public static boolean isVisualExperienceUnlocked(Player player) {
        if (Config.visualExperienceStartMode.equals("portal_active")
                || Config.visualExperienceStartMode.equals("no_portal")) {
            return true;
        }

        return player.getPersistentData().getBoolean(PLAYER_UNLOCKED_KEY).orElse(false);
    }

    private static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        if (Config.starterPortalEnabled
                && Config.visualExperienceStartMode.equals("locked_until_portal")) {
            player.getPersistentData().putBoolean(PLAYER_UNLOCKED_KEY, false);
            PLAYERS_TOUCHING_STARTER_PORTAL.remove(player.getUUID());
        }

        applyStartingGameMode(player);
        if (shouldUseStarterPortal(player)) {
            showInitialLoadNotice(player);
        }
        scheduleStarterEstateIfNeeded(player, STARTER_PORTAL_SPAWN_DELAY_TICKS);
    }

    private static void applyStartingGameMode(ServerPlayer player) {
        if (MagicWorldWorldOptions.startingGameMode() == MagicWorldWorldOptions.StartingGameMode.CREATIVE) {
            player.setGameMode(GameType.CREATIVE);
        }
    }

    private static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.getHand() != InteractionHand.MAIN_HAND
                || event.getLevel().isClientSide()
                || !(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }

        BlockPos pos = event.getPos();
        BlockPos portalPos = isStarterPortalCenter(level, pos)
                ? pos
                : null;
        if (portalPos == null) {
            return;
        }

        toggleVisualExperience(player, portalPos, "interacao");
    }

    private static void onRightClickEntity(PlayerInteractEvent.EntityInteract event) {
        if (event.getHand() != InteractionHand.MAIN_HAND
                || event.getLevel().isClientSide()
                || !(event.getEntity() instanceof ServerPlayer player)
                || !(event.getTarget() instanceof Mob mob)
                || !isMagicWorldAlly(mob)) {
            return;
        }

        event.setCanceled(true);
        if (player.isShiftKeyDown()) {
            giveAllyHelp(player, mob);
            return;
        }

        if (player.getUUID().equals(FOLLOWING_ALLIES.get(mob.getUUID()))) {
            FOLLOWING_ALLIES.remove(mob.getUUID());
            player.sendSystemMessage(Component.literal(mob.getName().getString() + ": vou guardar este lugar."));
        } else {
            FOLLOWING_ALLIES.put(mob.getUUID(), player.getUUID());
            player.sendSystemMessage(Component.literal(mob.getName().getString() + ": seguindo voce."));
        }
    }

    private static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || player.level().isClientSide()) {
            return;
        }

        handlePendingStarterPortalSpawn(player);
        handlePendingEstateExpansion(player);
        handleFollowingAllies(player);
        handleCastlePatrols(player);
        handleProtectedRailTunnel(player);
        handlePortalAmbientEffects(player);
        handleMagicEstateAmbience(player);

        if (player.tickCount % 20 == 0) {
            scheduleStarterEstateIfNeeded(player, 1);
        }

        if (player.tickCount % 20 != 0) {
            return;
        }

        BlockPos pos = player.blockPosition();
        BlockPos portalPos = findStarterPortalCenterAtPlayer((ServerLevel) player.level(), pos);

        if (portalPos == null) {
            PLAYERS_TOUCHING_STARTER_PORTAL.remove(player.getUUID());
            return;
        }

        if (PLAYERS_TOUCHING_STARTER_PORTAL.add(player.getUUID())) {
            toggleVisualExperience(player, portalPos, "entrada");
        }
    }

    private static void handleFollowingAllies(ServerPlayer player) {
        if (player.tickCount % 10 != 0 || !(player.level() instanceof ServerLevel level)) {
            return;
        }

        AABB area = player.getBoundingBox().inflate(72.0D);
        for (Mob mob : level.getEntitiesOfClass(Mob.class, area, mob -> player.getUUID().equals(FOLLOWING_ALLIES.get(mob.getUUID())))) {
            double distance = mob.distanceToSqr(player);
            if (distance > 900.0D) {
                mob.teleportTo(player.getX() + 2.0D, player.getY(), player.getZ() + 2.0D);
            } else if (distance > 16.0D) {
                mob.getNavigation().moveTo(player, 1.25D);
            }
        }
    }

    private static void handleCastlePatrols(ServerPlayer player) {
        if (player.tickCount % 20 != 0 || !(player.level() instanceof ServerLevel level)) {
            return;
        }

        CASTLE_PATROL_GUARDS.entrySet().removeIf(entry -> level.getEntity(entry.getKey()) == null);
        AABB nearby = player.getBoundingBox().inflate(180.0D);
        for (Mob guard : level.getEntitiesOfClass(Mob.class, nearby, mob -> CASTLE_PATROL_GUARDS.containsKey(mob.getUUID()))) {
            BlockPos patrolCenter = CASTLE_PATROL_GUARDS.get(guard.getUUID());
            if (patrolCenter == null) {
                continue;
            }

            double distance = guard.distanceToSqr(patrolCenter.getX() + 0.5D, patrolCenter.getY(), patrolCenter.getZ() + 0.5D);
            if (distance > 196.0D) {
                guard.getNavigation().moveTo(patrolCenter.getX() + 0.5D, patrolCenter.getY(), patrolCenter.getZ() + 0.5D, 1.15D);
                continue;
            }

            int phase = Math.floorMod(guard.tickCount / 80 + guard.getId(), 4);
            BlockPos target = switch (phase) {
                case 0 -> patrolCenter.offset(-8, 0, 0);
                case 1 -> patrolCenter.offset(0, 0, 5);
                case 2 -> patrolCenter.offset(8, 0, 0);
                default -> patrolCenter.offset(0, 0, -5);
            };
            guard.getNavigation().moveTo(target.getX() + 0.5D, target.getY(), target.getZ() + 0.5D, 1.0D);
        }
    }

    private static void handleProtectedRailTunnel(ServerPlayer player) {
        if (player.tickCount % 40 != 0 || !(player.level() instanceof ServerLevel level)) {
            return;
        }

        if (PROTECTED_RAIL_TUNNELS.isEmpty()) {
            return;
        }

        for (BlockPos center : PROTECTED_RAIL_TUNNELS) {
            if (player.distanceToSqr(center.getX() + 0.5D, center.getY() + 0.5D, center.getZ() + 0.5D) > 30000.0D) {
                continue;
            }

            AABB tunnelArea = new AABB(center).inflate(120.0D, 18.0D, 120.0D);
            for (Monster monster : level.getEntitiesOfClass(Monster.class, tunnelArea)) {
                monster.discard();
            }
        }
    }

    private static void giveAllyHelp(ServerPlayer player, Mob ally) {
        String name = ally.getName().getString();
        player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 20 * 24, 1));
        player.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 20 * 36, 0));
        player.addEffect(new MobEffectInstance(MobEffects.SPEED, 20 * 36, 0));

        if (name.toLowerCase().contains("ferreiro") || name.toLowerCase().contains("oficina")) {
            player.addItem(new ItemStack(Items.IRON_INGOT, 32));
            player.addItem(new ItemStack(Items.DIAMOND, 8));
            player.addItem(new ItemStack(Items.EXPERIENCE_BOTTLE, 32));
            player.sendSystemMessage(Component.literal(name + ": reforcei seus equipamentos e deixei recursos prontos."));
        } else if (name.toLowerCase().contains("cozinheiro") || name.toLowerCase().contains("chef")) {
            player.addItem(new ItemStack(Items.COOKED_BEEF, 32));
            player.addItem(new ItemStack(Items.GOLDEN_CARROT, 32));
            player.addItem(new ItemStack(Items.GOLDEN_APPLE, 4));
            player.sendSystemMessage(Component.literal(name + ": comida forte para voce continuar explorando."));
        } else if (name.toLowerCase().contains("bibliotec")) {
            player.addItem(new ItemStack(Items.EXPERIENCE_BOTTLE, 64));
            player.addItem(new ItemStack(Items.BOOK, 16));
            player.addItem(new ItemStack(Items.LAPIS_LAZULI, 64));
            player.sendSystemMessage(Component.literal(name + ": conhecimento arcano e experiencia para seus encantamentos."));
        } else if (name.toLowerCase().contains("agricultor") || name.toLowerCase().contains("tratador")) {
            player.addItem(new ItemStack(Items.BONE_MEAL, 64));
            player.addItem(new ItemStack(Items.WHEAT, 64));
            player.addItem(new ItemStack(Items.LEAD, 8));
            player.sendSystemMessage(Component.literal(name + ": mantimentos e cuidado para a comunidade crescer."));
        } else {
            player.addItem(new ItemStack(Items.BREAD, 16));
            player.addItem(new ItemStack(Items.TORCH, 32));
            player.addItem(new ItemStack(Items.OAK_LOG, 16));
            player.sendSystemMessage(Component.literal(name + ": suprimentos e protecao entregues."));
        }

        FOLLOWING_ALLIES.put(ally.getUUID(), player.getUUID());
        if (player.level() instanceof ServerLevel level) {
            MagicWorld.effects(level, ally.blockPosition());
        }
    }

    private static void handlePortalAmbientEffects(ServerPlayer player) {
        if (player.tickCount % 40 != 0 || !(player.level() instanceof ServerLevel level)) {
            return;
        }

        BlockPos center = findNearestStarterPortalMarker(level, player.blockPosition(), 28);
        if (center != null) {
            MagicWorld.effects(level, center.above());
        }
    }

    private static void handleMagicEstateAmbience(ServerPlayer player) {
        if (player.tickCount % 60 != 0 || !(player.level() instanceof ServerLevel level)) {
            return;
        }

        BlockPos portal = findNearestStarterPortalCenter(level, player.blockPosition(), 180);
        if (portal == null) {
            return;
        }

        BlockPos base = portal.offset(0, 0, -PORTAL_Z_OFFSET);
        sendAmbientMagic(level, base.offset(0, 4, 0));
        sendAmbientMagic(level, portal.above(3));
        if (MagicWorldWorldOptions.isCastlesEnabled()) {
            sendAmbientMagic(level, castleLifeCenter(base).above(5));
        }
    }

    private static void sendAmbientMagic(ServerLevel level, BlockPos pos) {
        level.sendParticles(ParticleTypes.ENCHANT, pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D, 22, 6.0D, 3.0D, 6.0D, 0.05D);
        level.sendParticles(ParticleTypes.END_ROD, pos.getX() + 0.5D, pos.getY() + 1.0D, pos.getZ() + 0.5D, 10, 4.0D, 2.0D, 4.0D, 0.02D);
        level.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, pos.getX() + 0.5D, pos.getY() - 1.0D, pos.getZ() + 0.5D, 4, 2.0D, 0.4D, 2.0D, 0.01D);
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

    private static BlockPos findNearestStarterPortalCenter(ServerLevel level, BlockPos center, int radius) {
        BlockPos min = center.offset(-radius, -4, -radius);
        BlockPos max = center.offset(radius, 8, radius);
        for (BlockPos pos : BlockPos.betweenClosed(min, max)) {
            if (isStarterPortalCenter(level, pos)) {
                return pos.immutable();
            }
        }

        return null;
    }

    private static BlockPos findStarterPortalCenterAtPlayer(ServerLevel level, BlockPos playerPos) {
        for (int yOffset = -4; yOffset <= 0; yOffset++) {
            BlockPos center = new BlockPos(playerPos.getX(), playerPos.getY() + yOffset, playerPos.getZ());
            if (isStarterPortalCenter(level, center)) {
                return center.immutable();
            }
        }

        return null;
    }

    private static boolean isStarterPortalCenter(ServerLevel level, BlockPos center) {
        return level.getBlockState(center).is(Blocks.END_PORTAL_FRAME)
                && level.getBlockState(center.west()).is(Blocks.AMETHYST_BLOCK)
                && level.getBlockState(center.east()).is(Blocks.AMETHYST_BLOCK)
                && level.getBlockState(center.above()).isAir()
                && level.getBlockState(center.above(2)).isAir()
                && level.getBlockState(center.above().west()).is(Blocks.PURPLE_STAINED_GLASS)
                && level.getBlockState(center.above().east()).is(Blocks.MAGENTA_STAINED_GLASS);
    }

    private static boolean isMagicWorldAlly(Mob mob) {
        return ALLY_ENTITY_IDS.contains(mob.getUUID())
                || (mob.getCustomName() != null && mob.isCustomNameVisible());
    }

    private static void toggleVisualExperience(ServerPlayer player, BlockPos pos, String source) {
        if (!Config.starterPortalEnabled
                || !Config.visualExperienceStartMode.equals("locked_until_portal")) {
            return;
        }

        boolean active = !isVisualExperienceUnlocked(player);
        if (active) {
            openPremiumOptionsMenu(player, pos, source);
            return;
        }

        player.getPersistentData().putBoolean(PLAYER_UNLOCKED_KEY, active);
        player.sendSystemMessage(Component.literal(
                "Magic World: experiencia visual especial desativada. Voltando para Vanilla."
        ));

        if (player.level() instanceof ServerLevel serverLevel) {
            MagicWorld.effects(serverLevel, pos);
        }
        PacketDistributor.sendToPlayer(
                player,
                new MagicWorldNetwork.ApplyPremiumPortalVisualPayload(false, false, false)
        );

        LOGGER.info(
                "Magic World starter portal toggled visual experience for {} to {} by {} at {}.",
                player.getScoreboardName(),
                false,
                source,
                pos
        );
    }

    private static void openPremiumOptionsMenu(ServerPlayer player, BlockPos pos, String source) {
        if (!PLAYERS_CHOOSING_PREMIUM_OPTIONS.add(player.getUUID())) {
            return;
        }

        if (player.level() instanceof ServerLevel serverLevel) {
            MagicWorld.effects(serverLevel, pos);
        }

        PacketDistributor.sendToPlayer(player, new MagicWorldNetwork.OpenPremiumPortalOptionsPayload());
        player.sendSystemMessage(Component.literal(
                "Magic World: escolha ResourcePack, ShaderPack ou Pacote completo para ativar o Premium."
        ));
        LOGGER.info(
                "Magic World starter portal opened premium option menu for {} by {} at {}.",
                player.getScoreboardName(),
                source,
                pos
        );
    }

    public static void confirmPremiumPortalOptions(
            ServerPlayer player,
            boolean resourcePack,
            boolean shaderPack,
            boolean completePack
    ) {
        boolean resolvedResourcePack = resourcePack || completePack;
        boolean resolvedShaderPack = shaderPack || completePack;

        PLAYERS_CHOOSING_PREMIUM_OPTIONS.remove(player.getUUID());
        player.getPersistentData().putBoolean(PLAYER_UNLOCKED_KEY, true);
        player.getPersistentData().putBoolean(PLAYER_PREMIUM_RESOURCE_PACK_KEY, resolvedResourcePack);
        player.getPersistentData().putBoolean(PLAYER_PREMIUM_SHADER_PACK_KEY, resolvedShaderPack);
        player.getPersistentData().putBoolean(PLAYER_PREMIUM_COMPLETE_PACK_KEY, completePack);

        player.sendSystemMessage(Component.literal(
                "Magic World: Premium ativado. ResourcePack: "
                        + optionStatus(resolvedResourcePack)
                        + " | ShaderPack: "
                        + optionStatus(resolvedShaderPack)
                        + " | Pacote completo: "
                        + optionStatus(completePack)
                        + "."
        ));

        if (player.level() instanceof ServerLevel serverLevel) {
            MagicWorld.effects(serverLevel, player.blockPosition());
        }
        PacketDistributor.sendToPlayer(
                player,
                new MagicWorldNetwork.ApplyPremiumPortalVisualPayload(true, resolvedResourcePack, resolvedShaderPack)
        );

        LOGGER.info(
                "Magic World premium enabled for {} with resourcePack={}, shaderPack={}, completePack={}.",
                player.getScoreboardName(),
                resolvedResourcePack,
                resolvedShaderPack,
                completePack
        );
    }

    private static String optionStatus(boolean active) {
        return active ? "ON" : "OFF";
    }

    private static void handlePendingStarterPortalSpawn(ServerPlayer player) {
        Integer ticksLeft = PENDING_STARTER_PORTAL_SPAWNS.get(player.getUUID());
        if (ticksLeft == null) {
            return;
        }

        if (ticksLeft > 0) {
            PENDING_STARTER_PORTAL_SPAWNS.put(player.getUUID(), ticksLeft - 1);
            return;
        }

        PENDING_STARTER_PORTAL_SPAWNS.remove(player.getUUID());
        tryCreateStarterPortal(player);
    }

    private static void scheduleStarterEstateIfNeeded(ServerPlayer player, int delayTicks) {
        if (!shouldUseStarterPortal(player)
                || PENDING_STARTER_PORTAL_SPAWNS.containsKey(player.getUUID())) {
            return;
        }

        PENDING_STARTER_PORTAL_SPAWNS.put(player.getUUID(), Math.max(0, delayTicks));
    }

    private static void tryCreateStarterPortal(ServerPlayer player) {
        if (!(player.level() instanceof ServerLevel level) || !shouldUseStarterPortal(player)) {
            return;
        }

        if (!level.dimension().equals(Level.OVERWORLD)) {
            return;
        }

        BlockPos playerPos = player.blockPosition();
        BlockPos spawnPos = level.getLevelData().getRespawnData().pos();
        BlockPos chestPos = findNearestChest(level, playerPos, STARTER_PORTAL_CHEST_SEARCH_RADIUS);
        if (chestPos == null) {
            chestPos = findNearestChest(level, spawnPos, STARTER_PORTAL_CHEST_SEARCH_RADIUS);
        }

        BlockPos anchorPos = chestPos != null ? chestPos : spawnPos;
        BlockPos portalBase = findStarterPortalBase(level, anchorPos);
        if (portalBase == null && chestPos != null) {
            portalBase = findStarterPortalBase(level, playerPos);
        }

        if (portalBase == null) {
            LOGGER.warn("Magic World could not find a safe place for the starter portal near {}.", anchorPos);
            return;
        }

        buildStarterEstate(level, portalBase, player);
        player.getPersistentData().putBoolean(PLAYER_ESTATE_CREATED_KEY, true);
        player.sendSystemMessage(Component.literal("Magic World: casa inicial criada perto do bau bonus. O portal vai carregar depois."));
        LOGGER.info(
                "Magic World starter estate created for {} at {} near {}.",
                player.getScoreboardName(),
                portalBase,
                anchorPos
        );
    }

    private static BlockPos findNearestChest(ServerLevel level, BlockPos center, int radius) {
        BlockPos min = center.offset(-radius, -6, -radius);
        BlockPos max = center.offset(radius, 6, radius);
        for (BlockPos pos : BlockPos.betweenClosed(min, max)) {
            if (level.getBlockState(pos).is(Blocks.CHEST)) {
                return pos.immutable();
            }
        }

        return null;
    }

    private static boolean hasStarterPortalNear(ServerLevel level, BlockPos center, int radius) {
        BlockPos min = center.offset(-radius, -8, -radius);
        BlockPos max = center.offset(radius, 8, radius);
        for (BlockPos pos : BlockPos.betweenClosed(min, max)) {
            if (isStarterPortalMarker(level, pos)) {
                return true;
            }
        }

        return false;
    }

    private static BlockPos findStarterPortalBase(ServerLevel level, BlockPos anchorPos) {
        Direction[] directions = {Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST};
        for (int offset = 3; offset <= 8; offset++) {
            for (Direction direction : directions) {
                BlockPos base = findGroundBase(level, anchorPos.relative(direction, offset));
                if (base != null && canBuildStarterPortalAt(level, base)) {
                    return base;
                }
            }
        }

        for (int radius = 2; radius <= 12; radius++) {
            for (int x = -radius; x <= radius; x++) {
                for (int z = -radius; z <= radius; z++) {
                    if (Math.abs(x) != radius && Math.abs(z) != radius) {
                        continue;
                    }

                    BlockPos base = findGroundBase(level, anchorPos.offset(x, 0, z));
                    if (base != null && canBuildStarterPortalAt(level, base)) {
                        return base;
                    }
                }
            }
        }

        return null;
    }

    private static BlockPos findGroundBase(ServerLevel level, BlockPos start) {
        int topY = Math.min(level.getMaxY() - 6, start.getY() + 8);
        int bottomY = Math.max(level.getMinY() + 2, start.getY() - 10);
        for (int y = topY; y >= bottomY; y--) {
            BlockPos base = new BlockPos(start.getX(), y, start.getZ());
            if (!level.getBlockState(base.below()).isAir() && canBuildStarterPortalAt(level, base)) {
                return base;
            }
        }

        return null;
    }

    private static boolean canBuildStarterPortalAt(ServerLevel level, BlockPos base) {
        if (level.getBlockState(base.below()).isAir()) {
            return false;
        }

        return true;
    }

    private static boolean canReplaceForStarterPortal(BlockState state) {
        return state.isAir() || state.canBeReplaced();
    }

    private static void buildStarterEstate(ServerLevel level, BlockPos base, ServerPlayer player) {
        boolean importedHouse = buildImportedHouse(level, base);
        if (!importedHouse) {
            flattenAndClearHouseOnly(level, base);
            buildPremiumHouse(level, base);
            fillStarterChests(level, base);
        } else {
            fillImportedHouseChests(level, base);
        }
        placeWaitingPortalSign(level, base);
        spawnStarterAllies(level, base);
        cleanupEstateGenerationDebris(level, base);

        BlockPos spawnPos = importedHouse ? importedHouseSpawn(base) : base.offset(-18, 1, -23);
        showInitialLoadNotice(player);
        player.teleportTo(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5);
        player.setRespawnPosition(
                new ServerPlayer.RespawnConfig(
                        LevelData.RespawnData.of(level.dimension(), spawnPos, 180.0F, 0.0F),
                        true
                ),
                false
        );

        PENDING_ESTATE_EXPANSIONS.put(player.getUUID(), new EstateExpansionTask(base.immutable(), 0, ESTATE_STEP_DELAY_TICKS, importedHouse));
        player.sendSystemMessage(Component.literal(
                "Magic World: carregamento inicial iniciado. Casa primeiro; fazendas, portal, castelo e dragao virao em etapas."
        ));
    }

    private static void showInitialLoadNotice(ServerPlayer player) {
        if (player.getPersistentData().getBoolean(PLAYER_INITIAL_LOAD_NOTICE_KEY).orElse(false)) {
            return;
        }

        player.getPersistentData().putBoolean(PLAYER_INITIAL_LOAD_NOTICE_KEY, true);
        PacketDistributor.sendToPlayer(player, new MagicWorldNetwork.OpenInitialLoadNoticePayload());
        sendInitialLoadProgress(player, 0, "Carregando casa...", false);
    }

    private static void handlePendingEstateExpansion(ServerPlayer player) {
        EstateExpansionTask task = PENDING_ESTATE_EXPANSIONS.get(player.getUUID());
        if (task == null || !(player.level() instanceof ServerLevel level)) {
            return;
        }

        if (task.ticksUntilNextStep() > 0) {
            PENDING_ESTATE_EXPANSIONS.put(
                    player.getUUID(),
                    new EstateExpansionTask(task.base(), task.step(), task.ticksUntilNextStep() - 1, task.importedHouse())
            );
            return;
        }

        boolean hasMoreSteps = buildEstateExpansionStep(player, level, task.base(), task.step(), task.importedHouse());
        if (!hasMoreSteps) {
            PENDING_ESTATE_EXPANSIONS.remove(player.getUUID());
            cleanupEstateGenerationDebris(level, task.base());
            cleanupFloatingDebrisBlocks(level, task.base());
            sendInitialLoadProgress(player, 100, "Tudo carregado. Pode confirmar e jogar.", true);
            player.sendSystemMessage(Component.literal(
                    "Magic World: castelos, fazendas, muralhas e dragao finalizados."
            ));
            return;
        }

        PENDING_ESTATE_EXPANSIONS.put(
                player.getUUID(),
                new EstateExpansionTask(task.base(), task.step() + 1, delayAfterEstateStep(task.step()), task.importedHouse())
        );
        cleanupEstateGenerationDebris(level, task.base());
    }

    private static boolean buildEstateExpansionStep(ServerPlayer player, ServerLevel level, BlockPos base, int step, boolean importedHouse) {
        switch (step) {
            case 0 -> {
                sendInitialLoadProgress(player, 8, "Carregando casa...", false);
                if (importedHouse) {
                    decorateImportedHouseAddons(level, base);
                } else {
                    decorateMainHouseExterior(level, base);
                }
                sendInitialLoadProgress(player, 18, "Casa e decoracao principal carregadas.", false);
                return true;
            }
            case 1 -> {
                sendInitialLoadProgress(player, 24, "Carregando fazendas e animais...", false);
                prepareGardenTerrain(level, base);
                if (importedHouse) {
                    buildImportedGardenFarmAndLake(level, base);
                    spawnImportedStarterAnimals(level, base);
                } else {
                    buildGardenFarmAndLake(level, base);
                    spawnStarterAnimals(level, base);
                }
                sendInitialLoadProgress(player, 40, "Fazendas, animais e plantacoes carregados.", false);
                return true;
            }
            case 2 -> {
                sendInitialLoadProgress(player, 50, "Carregando portal...", false);
                buildStarterPortal(level, portalCenter(base));
                MagicWorld.effects(level, portalCenter(base).above());
                sendInitialLoadProgress(player, 62, "Portal premium e baus do portal carregados.", false);
                return true;
            }
            case 3 -> {
                if (!MagicWorldWorldOptions.isCastlesEnabled()) {
                    sendInitialLoadProgress(player, 76, "Carregando dragao...", false);
                    spawnPeacefulDragon(level, base);
                    sendInitialLoadProgress(player, 86, "Dragao carregado. Finalizando ajudantes.", false);
                    return true;
                }

                sendInitialLoadProgress(player, 70, "Carregando castelo...", false);
                buildPersonalCastle(level, castleAnchor(base));
                sendInitialLoadProgress(player, 80, "Castelo premium carregado.", false);
                return true;
            }
            case 4 -> {
                if (MagicWorldWorldOptions.isCastlesEnabled()) {
                    sendInitialLoadProgress(player, 88, "Carregando dragao...", false);
                    spawnPeacefulDragon(level, base);
                    sendInitialLoadProgress(player, 92, "Dragao carregado. Finalizando ajudantes.", false);
                } else {
                    sendInitialLoadProgress(player, 92, "Dragao carregado. Finalizando ajudantes.", false);
                }
                return true;
            }
            case 5 -> {
                spawnPostDragonLife(level, base);
                buildMagicalEstateAmbience(level, base);
                buildDirectionalSigns(level, base);
                buildUndergroundRailSystem(level, base);
                buildEnhancedEstateLighting(level, base);
                buildCastleBatChamber(level, castleLifeCenter(base));
                buildHiddenMagicWorldSurprise(level, base);
                sendInitialLoadProgress(player, 98, "Finalizando estruturas do Magic World", false);
                return true;
            }
            case 6 -> {
                sendInitialLoadProgress(player, 100, "Tudo carregado. Pode confirmar e jogar.", true);
                return false;
            }
            default -> {
                return false;
            }
        }
    }

    private static void sendInitialLoadProgress(ServerPlayer player, int progress, String message, boolean complete) {
        PacketDistributor.sendToPlayer(
                player,
                new MagicWorldNetwork.InitialLoadProgressPayload(progress, message, complete)
        );
    }

    private static BlockPos portalCenter(BlockPos base) {
        return base.offset(0, 0, PORTAL_Z_OFFSET);
    }

    private static BlockPos castleAnchor(BlockPos base) {
        return base.offset(CASTLE_X_OFFSET_FROM_PORTAL, 0, CASTLE_Z_OFFSET_FROM_BASE);
    }

    private static BlockPos importedCastleOrigin(BlockPos castleAnchor) {
        return castleAnchor.offset(0, 0, -IMPORTED_CASTLE_CENTER_Z_REL);
    }

    private static BlockPos importedCastleCenter(BlockPos castleAnchor) {
        return importedCastleOrigin(castleAnchor).offset(IMPORTED_CASTLE_CENTER_X_REL, 0, IMPORTED_CASTLE_CENTER_Z_REL);
    }

    private static BlockPos castleLifeCenter(BlockPos base) {
        return importedCastleCenter(castleAnchor(base));
    }

    private static BlockPos importedHouseOrigin(BlockPos base) {
        return base.offset(IMPORTED_HOUSE_ORIGIN_X_FROM_BASE, IMPORTED_HOUSE_ORIGIN_Y_FROM_BASE, IMPORTED_HOUSE_ORIGIN_Z_FROM_BASE);
    }

    private static BlockPos importedHouseSpawn(BlockPos base) {
        return base.offset(0, 1, 0);
    }

    private static boolean isInsideImportedHouseFootprint(int x, int z) {
        return x >= IMPORTED_HOUSE_ORIGIN_X_FROM_BASE - STRUCTURE_BREATHING_MARGIN
                && x <= IMPORTED_HOUSE_MAX_X_FROM_BASE + STRUCTURE_BREATHING_MARGIN
                && z >= IMPORTED_HOUSE_ORIGIN_Z_FROM_BASE - STRUCTURE_BREATHING_MARGIN
                && z <= IMPORTED_HOUSE_MAX_Z_FROM_BASE + STRUCTURE_BREATHING_MARGIN;
    }

    private static boolean isInsideImportedCastleFootprint(int x, int z) {
        int minX = CASTLE_X_OFFSET_FROM_PORTAL - STRUCTURE_BREATHING_MARGIN;
        int maxX = CASTLE_X_OFFSET_FROM_PORTAL + IMPORTED_CASTLE_SIZE_X + STRUCTURE_BREATHING_MARGIN - 1;
        int minZ = CASTLE_Z_OFFSET_FROM_BASE - IMPORTED_CASTLE_CENTER_Z_REL - STRUCTURE_BREATHING_MARGIN;
        int maxZ = CASTLE_Z_OFFSET_FROM_BASE - IMPORTED_CASTLE_CENTER_Z_REL + IMPORTED_CASTLE_SIZE_Z + STRUCTURE_BREATHING_MARGIN - 1;
        return x >= minX && x <= maxX && z >= minZ && z <= maxZ;
    }

    private static int delayAfterEstateStep(int completedStep) {
        if (completedStep == 1) {
            return ESTATE_PORTAL_DELAY_TICKS;
        }
        if (completedStep == 4) {
            return ESTATE_POST_DRAGON_DELAY_TICKS;
        }
        if (completedStep == 5) {
            return ESTATE_FINALIZING_DELAY_TICKS;
        }
        return ESTATE_STEP_DELAY_TICKS;
    }

    private static void flattenAndClearHouseOnly(ServerLevel level, BlockPos base) {
        prepareFlatArea(level, base, -42, 42, -50, 36, overheadClearHeight(level, base));
    }

    private static boolean buildImportedHouse(ServerLevel level, BlockPos base) {
        boolean placed = placeImportedStructure(level, IMPORTED_HOUSE_STRUCTURE, importedHouseOrigin(base), true);
        if (placed) {
            prepareImportedHouseBreathingSurface(level, base);
        }
        return placed;
    }

    private static boolean placeImportedStructure(ServerLevel level, Identifier id, BlockPos origin, boolean clearBeforePlace) {
        Optional<StructureTemplate> template = level.getStructureManager().get(id);
        if (template.isEmpty()) {
            LOGGER.warn("Magic World could not load imported structure {}.", id);
            return false;
        }

        StructureTemplate structure = template.get();
        Vec3i size = structure.getSize();
        if (clearBeforePlace) {
            clearStructureVolume(level, origin, size);
        }

        StructurePlaceSettings settings = new StructurePlaceSettings()
                .setIgnoreEntities(false)
                .setKnownShape(true)
                .setFinalizeEntities(true);
        return structure.placeInWorld(
                level,
                origin,
                origin,
                settings,
                RandomSource.create(level.getSeed() ^ origin.asLong()),
                3
        );
    }

    private static void clearStructureVolume(ServerLevel level, BlockPos origin, Vec3i size) {
        clearBreathingVolume(level, origin, size, STRUCTURE_BREATHING_MARGIN, true);
    }

    private static void cleanupEstateGenerationDebris(ServerLevel level, BlockPos base) {
        cleanupDroppedItems(level, base);
    }

    private static void cleanupDroppedItems(ServerLevel level, BlockPos base) {
        AABB area = new AABB(
                base.getX() - 180, level.getMinY(), base.getZ() - 180,
                base.getX() + 360, level.getMaxY(), base.getZ() + 260
        );
        for (ItemEntity item : level.getEntitiesOfClass(ItemEntity.class, area)) {
            item.discard();
        }
    }

    private static void cleanupFloatingDebrisBlocks(ServerLevel level, BlockPos base) {
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        for (int x = -180; x <= 360; x++) {
            for (int z = -180; z <= 260; z++) {
                if (isInsideImportedHouseFootprint(x, z) || isInsideImportedCastleFootprint(x, z)) {
                    continue;
                }

                int worldX = base.getX() + x;
                int worldZ = base.getZ() + z;
                for (int y = base.getY() + 1; y <= Math.min(level.getMaxY() - 1, base.getY() + 96); y++) {
                    mutable.set(worldX, y, worldZ);
                    if (!level.isInWorldBounds(mutable)) {
                        continue;
                    }

                    BlockState state = level.getBlockState(mutable);
                    if (state.isAir() || !isFloatingDebrisBlock(state)) {
                        continue;
                    }

                    if (level.getBlockState(mutable.below()).isAir()) {
                        level.setBlock(mutable, Blocks.AIR.defaultBlockState(), 3);
                    }
                }
            }
        }
    }

    private static boolean isFloatingDebrisBlock(BlockState state) {
        return state.is(Blocks.PRISMARINE)
                || state.is(Blocks.PRISMARINE_BRICKS)
                || state.is(Blocks.DARK_PRISMARINE)
                || state.is(Blocks.GRASS_BLOCK)
                || state.is(Blocks.DIRT)
                || state.is(Blocks.COARSE_DIRT)
                || state.is(Blocks.ROOTED_DIRT)
                || state.is(Blocks.STONE)
                || state.is(Blocks.COBBLESTONE)
                || state.is(Blocks.GRAVEL)
                || state.is(Blocks.SAND)
                || state.is(Blocks.SANDSTONE)
                || state.is(Blocks.OAK_LOG)
                || state.is(Blocks.SPRUCE_LOG)
                || state.is(Blocks.OAK_LEAVES)
                || state.is(Blocks.FLOWERING_AZALEA_LEAVES);
    }

    private static int overheadClearHeight(ServerLevel level, BlockPos base) {
        return Math.max(32, level.getMaxY() - base.getY() - 1);
    }

    private static void clearBreathingVolume(ServerLevel level, BlockPos origin, Vec3i size, int margin, boolean grassFloor) {
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        int minX = -margin;
        int maxX = size.getX() + margin - 1;
        int minZ = -margin;
        int maxZ = size.getZ() + margin - 1;
        int topY = level.getMaxY() - 1;

        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                if (grassFloor) {
                    mutable.set(origin.getX() + x, origin.getY() - 1, origin.getZ() + z);
                    if (level.isInWorldBounds(mutable)) {
                        level.setBlock(mutable, Blocks.GRASS_BLOCK.defaultBlockState(), 3);
                    }
                }

                for (int y = origin.getY(); y <= topY; y++) {
                    mutable.set(origin.getX() + x, y, origin.getZ() + z);
                    if (level.isInWorldBounds(mutable) && !level.getBlockState(mutable).isAir()) {
                        level.setBlock(mutable, Blocks.AIR.defaultBlockState(), 3);
                    }
                }
            }
        }
    }

    private static void prepareImportedHouseBreathingSurface(ServerLevel level, BlockPos base) {
        int minX = IMPORTED_HOUSE_ORIGIN_X_FROM_BASE - STRUCTURE_BREATHING_MARGIN;
        int maxX = IMPORTED_HOUSE_MAX_X_FROM_BASE + STRUCTURE_BREATHING_MARGIN;
        int minZ = IMPORTED_HOUSE_ORIGIN_Z_FROM_BASE - STRUCTURE_BREATHING_MARGIN;
        int maxZ = IMPORTED_HOUSE_MAX_Z_FROM_BASE + STRUCTURE_BREATHING_MARGIN;

        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                boolean insideHouse = x >= IMPORTED_HOUSE_ORIGIN_X_FROM_BASE
                        && x <= IMPORTED_HOUSE_MAX_X_FROM_BASE
                        && z >= IMPORTED_HOUSE_ORIGIN_Z_FROM_BASE
                        && z <= IMPORTED_HOUSE_MAX_Z_FROM_BASE;
                if (insideHouse) {
                    continue;
                }

                level.setBlock(base.offset(x, -1, z), Blocks.GRASS_BLOCK.defaultBlockState(), 3);
                for (int y = 0; y <= 18; y++) {
                    level.setBlock(base.offset(x, y, z), Blocks.AIR.defaultBlockState(), 3);
                }
            }
        }
    }

    private static void prepareGardenTerrain(ServerLevel level, BlockPos base) {
        for (int x = -HOUSE_TERRAIN_RADIUS; x <= HOUSE_TERRAIN_RADIUS; x++) {
            for (int z = -HOUSE_TERRAIN_RADIUS; z <= HOUSE_TERRAIN_RADIUS + 24; z++) {
                if (isInsideImportedHouseFootprint(x, z) || (x >= -34 && x <= 34 && z >= -42 && z <= 28)) {
                    continue;
                }

                BlockPos ground = base.offset(x, -1, z);
                level.setBlock(ground, Blocks.GRASS_BLOCK.defaultBlockState(), 3);
                for (int y = 0; y <= 18; y++) {
                    level.setBlock(base.offset(x, y, z), Blocks.AIR.defaultBlockState(), 3);
                }
            }
        }
    }

    private static void prepareFlatArea(ServerLevel level, BlockPos base, int minX, int maxX, int minZ, int maxZ, int clearHeight) {
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                BlockPos ground = base.offset(x, -1, z);
                level.setBlock(ground, Blocks.GRASS_BLOCK.defaultBlockState(), 3);
                for (int y = 0; y <= clearHeight; y++) {
                    level.setBlock(base.offset(x, y, z), Blocks.AIR.defaultBlockState(), 3);
                }
            }
        }
    }

    private static void flattenAndClear(ServerLevel level, BlockPos base) {
        for (int x = -HOUSE_TERRAIN_RADIUS; x <= HOUSE_TERRAIN_RADIUS; x++) {
            for (int z = -HOUSE_TERRAIN_RADIUS; z <= HOUSE_TERRAIN_RADIUS + 24; z++) {
                BlockPos ground = base.offset(x, -1, z);
                level.setBlock(ground, Blocks.GRASS_BLOCK.defaultBlockState(), 3);
                for (int y = 0; y <= 22; y++) {
                    level.setBlock(base.offset(x, y, z), Blocks.AIR.defaultBlockState(), 3);
                }
            }
        }

    }

    private static void placeWaitingPortalSign(ServerLevel level, BlockPos base) {
        placePortalSign(level, base.offset(0, 1, PORTAL_Z_OFFSET - 8), 0,
                "se eu nao",
                "estiver aqui",
                "espere!",
                "");
    }

    private static void flattenPath(ServerLevel level, BlockPos base, Direction direction, int length, int halfWidth) {
        for (int step = HOUSE_TERRAIN_RADIUS; step <= length - CASTLE_TERRAIN_RADIUS; step++) {
            for (int side = -halfWidth; side <= halfWidth; side++) {
                BlockPos pos = direction.getAxis() == Direction.Axis.Z
                        ? base.relative(direction, step).offset(side, 0, 0)
                        : base.relative(direction, step).offset(0, 0, side);
                level.setBlock(pos.below(), Blocks.GRASS_BLOCK.defaultBlockState(), 3);
                for (int y = 0; y <= 12; y++) {
                    level.setBlock(pos.offset(0, y, 0), Blocks.AIR.defaultBlockState(), 3);
                }
                if (side == 0 || (Math.abs(side) == 1 && step % 3 != 0)) {
                    level.setBlock(pos, Blocks.DIRT_PATH.defaultBlockState(), 3);
                } else if (Math.abs(side) == 2 && step % 21 == 0) {
                    placeLampPost(level, pos);
                }
            }
        }
    }

    private static void buildIronDisplayHouse(ServerLevel level, BlockPos base) {
        for (int x = -9; x <= 9; x++) {
            for (int z = -7; z <= 7; z++) {
                level.setBlock(base.offset(x, -1, z), Blocks.IRON_BLOCK.defaultBlockState(), 3);
                level.setBlock(base.offset(x, 0, z), Blocks.SMOOTH_STONE.defaultBlockState(), 3);
                for (int y = 1; y <= 6; y++) {
                    boolean wall = Math.abs(x) == 9 || Math.abs(z) == 7;
                    level.setBlock(base.offset(x, y, z), wall ? Blocks.IRON_BLOCK.defaultBlockState() : Blocks.AIR.defaultBlockState(), 3);
                }
            }
        }

        for (int x = -10; x <= 10; x++) {
            for (int z = -8; z <= 8; z++) {
                level.setBlock(base.offset(x, 7, z), Blocks.IRON_BLOCK.defaultBlockState(), 3);
            }
        }

        placeIronDoor(level, base.offset(0, 1, 7), Direction.SOUTH);
        level.setBlock(base.offset(2, 1, 7), Blocks.IRON_TRAPDOOR.defaultBlockState(), 3);
        level.setBlock(base.offset(-2, 0, 5), Blocks.IRON_TRAPDOOR.defaultBlockState(), 3);

        for (int x : new int[] {-6, -3, 3, 6}) {
            level.setBlock(base.offset(x, 2, 7), Blocks.IRON_BARS.defaultBlockState(), 3);
            level.setBlock(base.offset(x, 3, 7), Blocks.GLASS_PANE.defaultBlockState(), 3);
            level.setBlock(base.offset(x, 2, -7), Blocks.IRON_BARS.defaultBlockState(), 3);
            level.setBlock(base.offset(x, 3, -7), Blocks.GLASS_PANE.defaultBlockState(), 3);
        }
        for (int z : new int[] {-4, 0, 4}) {
            level.setBlock(base.offset(-9, 2, z), Blocks.IRON_BARS.defaultBlockState(), 3);
            level.setBlock(base.offset(-9, 3, z), Blocks.GLASS_PANE.defaultBlockState(), 3);
            level.setBlock(base.offset(9, 2, z), Blocks.IRON_BARS.defaultBlockState(), 3);
            level.setBlock(base.offset(9, 3, z), Blocks.GLASS_PANE.defaultBlockState(), 3);
        }

        for (BlockPos pos : new BlockPos[] {
                base.offset(-7, 1, -5), base.offset(-4, 1, -5), base.offset(-1, 1, -5),
                base.offset(2, 1, -5), base.offset(5, 1, -5), base.offset(7, 1, -2),
                base.offset(7, 1, 1)
        }) {
            level.setBlock(pos.below(), Blocks.POLISHED_DEEPSLATE.defaultBlockState(), 3);
        }

        for (int y = 1; y <= 4; y++) {
            level.setBlock(base.offset(-8, y, 2), Blocks.BOOKSHELF.defaultBlockState(), 3);
            level.setBlock(base.offset(-8, y, 3), Blocks.BOOKSHELF.defaultBlockState(), 3);
            level.setBlock(base.offset(-8, y, 4), Blocks.BOOKSHELF.defaultBlockState(), 3);
        }
        level.setBlock(base.offset(-6, 1, 3), Blocks.LECTERN.defaultBlockState(), 3);
        level.setBlock(base.offset(-6, 1, 5), Blocks.ENCHANTING_TABLE.defaultBlockState(), 3);

        level.setBlock(base.offset(2, 1, 4), Blocks.CRAFTING_TABLE.defaultBlockState(), 3);
        level.setBlock(base.offset(4, 1, 4), Blocks.FURNACE.defaultBlockState(), 3);
        level.setBlock(base.offset(6, 1, 4), Blocks.BLAST_FURNACE.defaultBlockState(), 3);
        level.setBlock(base.offset(2, 1, 2), Blocks.SMITHING_TABLE.defaultBlockState(), 3);
        level.setBlock(base.offset(4, 1, 2), Blocks.ANVIL.defaultBlockState(), 3);
        level.setBlock(base.offset(6, 1, 2), Blocks.GRINDSTONE.defaultBlockState(), 3);

        placeChest(level, base.offset(0, 1, 5), Direction.NORTH);
        putItems(level, base.offset(0, 1, 5),
                new ItemStack(MagicWorld.DRACONIC_AETHER_HELMET.get()),
                new ItemStack(MagicWorld.DRACONIC_AETHER_CHESTPLATE.get()),
                new ItemStack(MagicWorld.DRACONIC_AETHER_LEGGINGS.get()),
                new ItemStack(MagicWorld.DRACONIC_AETHER_BOOTS.get()),
                new ItemStack(MagicWorld.VARINHA_MAGICA.get()),
                new ItemStack(Items.IRON_INGOT, 64),
                new ItemStack(Items.DIAMOND, 32),
                new ItemStack(Items.EMERALD, 32));

        for (BlockPos pos : new BlockPos[] {
                base.offset(-7, 1, 6), base.offset(7, 1, 6),
                base.offset(-7, 1, -6), base.offset(7, 1, -6),
                base.offset(0, 6, 0), base.offset(-4, 6, 0), base.offset(4, 6, 0)
        }) {
            level.setBlock(pos, Blocks.SEA_LANTERN.defaultBlockState(), 3);
            level.setBlock(pos.above(), Blocks.LANTERN.defaultBlockState(), 3);
        }

        for (int x = -12; x <= 12; x++) {
            for (int z = -10; z <= 10; z++) {
                if (Math.abs(x) == 12 || Math.abs(z) == 10) {
                    level.setBlock(base.offset(x, 0, z), Blocks.IRON_BARS.defaultBlockState(), 3);
                } else if ((x * x + z * z) % 9 == 0) {
                    level.setBlock(base.offset(x, 0, z), Blocks.MOSS_BLOCK.defaultBlockState(), 3);
                    level.setBlock(base.offset(x, 1, z), flowerFor(x + z), 3);
                }
            }
        }
        placeLampPost(level, base.offset(-12, 0, -10));
        placeLampPost(level, base.offset(12, 0, -10));
        placeLampPost(level, base.offset(-12, 0, 10));
        placeLampPost(level, base.offset(12, 0, 10));
    }

    private static void placeIronDoor(ServerLevel level, BlockPos foot, Direction facing) {
        level.setBlock(foot, Blocks.IRON_DOOR.defaultBlockState()
                .setValue(DoorBlock.FACING, facing)
                .setValue(DoorBlock.HALF, DoubleBlockHalf.LOWER), 3);
        level.setBlock(foot.above(), Blocks.IRON_DOOR.defaultBlockState()
                .setValue(DoorBlock.FACING, facing)
                .setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER), 3);
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

        var stand = EntityType.ARMOR_STAND.spawn(level, safePos, EntitySpawnReason.STRUCTURE);
        if (stand == null) {
            return;
        }

        stand.setItemSlot(EquipmentSlot.HEAD, new ItemStack(helmet));
        stand.setItemSlot(EquipmentSlot.CHEST, new ItemStack(chestplate));
        stand.setItemSlot(EquipmentSlot.LEGS, new ItemStack(leggings));
        stand.setItemSlot(EquipmentSlot.FEET, new ItemStack(boots));
        stand.setCustomName(Component.literal(name));
        stand.setCustomNameVisible(true);
    }

    private static void buildPremiumHouse(ServerLevel level, BlockPos base) {
        for (int x = -23; x <= 23; x++) {
            for (int z = -30; z <= 14; z++) {
                level.setBlock(base.offset(x, 0, z), Blocks.SPRUCE_PLANKS.defaultBlockState(), 3);
                level.setBlock(base.offset(x, 6, z), Blocks.DARK_OAK_PLANKS.defaultBlockState(), 3);
                level.setBlock(base.offset(x, 13, z), Blocks.POLISHED_DEEPSLATE.defaultBlockState(), 3);
            }
        }

        for (int y = 1; y <= 12; y++) {
            for (int x = -23; x <= 23; x++) {
                placeHouseWall(level, base.offset(x, y, -30), x == -23 || x == 23);
                if (!(x >= -3 && x <= 3 && y <= 4)) {
                    placeHouseWall(level, base.offset(x, y, 14), x == -23 || x == 23);
                }
            }
            for (int z = -30; z <= 14; z++) {
                placeHouseWall(level, base.offset(-23, y, z), z == -30 || z == 14);
                placeHouseWall(level, base.offset(23, y, z), z == -30 || z == 14);
            }
        }

        buildGroundFloorRooms(level, base);
        buildArcaneHall(level, base);
        buildRoofTerrace(level, base);
        buildBasementRail(level, base);

        placeHouseDoor(level, base.offset(-1, 1, 14), Direction.SOUTH);
        placeHouseDoor(level, base.offset(0, 1, 14), Direction.SOUTH);
        placeHouseDoor(level, base.offset(1, 1, 14), Direction.SOUTH);
        placeHouseDoor(level, base.offset(-1, 1, -30), Direction.NORTH);
        placeHouseDoor(level, base.offset(0, 1, -30), Direction.NORTH);
        placeHouseDoor(level, base.offset(1, 1, -30), Direction.NORTH);
        for (int z = 15; z <= 21; z++) {
            level.setBlock(base.offset(-1, 0, z), stair(Direction.SOUTH), 3);
            level.setBlock(base.offset(0, 0, z), stair(Direction.SOUTH), 3);
            level.setBlock(base.offset(1, 0, z), stair(Direction.SOUTH), 3);
        }
        for (int z = -36; z <= -31; z++) {
            level.setBlock(base.offset(-1, 0, z), stair(Direction.NORTH), 3);
            level.setBlock(base.offset(0, 0, z), stair(Direction.NORTH), 3);
            level.setBlock(base.offset(1, 0, z), stair(Direction.NORTH), 3);
        }

        for (int x : new int[] {-18, -12, -6, 6, 12, 18}) {
            placeWindow(level, base.offset(x, 2, 14));
            placeWindow(level, base.offset(x, 8, 14));
            placeWindow(level, base.offset(x, 2, -30));
            placeWindow(level, base.offset(x, 8, -30));
        }
        for (int z : new int[] {-24, -18, -12, -6, 0, 6}) {
            placeWindow(level, base.offset(-23, 2, z));
            placeWindow(level, base.offset(23, 2, z));
            placeWindow(level, base.offset(-23, 8, z));
            placeWindow(level, base.offset(23, 8, z));
        }
        decorateHaciendaHouseExterior(level, base);

        for (int step = 0; step <= 6; step++) {
            level.setBlock(base.offset(0, step, -25 + step), Blocks.OAK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.SOUTH), 3);
            level.setBlock(base.offset(1, step, -25 + step), Blocks.OAK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.SOUTH), 3);
        }

        for (int z = -32; z <= 16; z++) {
            int roofY = 14 + Math.max(0, 20 - Math.abs(z + 8)) / 5;
            for (int x = -25; x <= 25; x++) {
                BlockState roof = Math.abs(x) >= 21
                        ? Blocks.DARK_OAK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, x < 0 ? Direction.WEST : Direction.EAST)
                        : Blocks.DARK_OAK_PLANKS.defaultBlockState();
                level.setBlock(base.offset(x, roofY, z), roof, 3);
            }
        }

        placeChest(level, base.offset(-18, 1, -28), Direction.SOUTH);
        placeChest(level, base.offset(-8, 1, -28), Direction.SOUTH);
        placeChest(level, base.offset(8, 1, -28), Direction.SOUTH);
        placeChest(level, base.offset(18, 1, -28), Direction.SOUTH);
        placeChest(level, base.offset(-12, 8, -27), Direction.SOUTH);
        placeChest(level, base.offset(0, 8, -27), Direction.SOUTH);
        placeChest(level, base.offset(12, 8, -27), Direction.SOUTH);
    }

    private static void decorateHaciendaHouseExterior(ServerLevel level, BlockPos base) {
        for (int x = -22; x <= 22; x += 4) {
            for (int y = 1; y <= 12; y++) {
                level.setBlock(base.offset(x, y, 15), Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState(), 3);
                level.setBlock(base.offset(x, y, -31), Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState(), 3);
            }
        }
        for (int z = -28; z <= 12; z += 4) {
            for (int y = 1; y <= 12; y++) {
                level.setBlock(base.offset(-24, y, z), Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState(), 3);
                level.setBlock(base.offset(24, y, z), Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState(), 3);
            }
        }
        for (int y : new int[] {4, 10}) {
            for (int x = -22; x <= 22; x++) {
                level.setBlock(base.offset(x, y, 15), Blocks.DARK_OAK_SLAB.defaultBlockState(), 3);
                level.setBlock(base.offset(x, y, -31), Blocks.DARK_OAK_SLAB.defaultBlockState(), 3);
            }
            for (int z = -30; z <= 14; z++) {
                level.setBlock(base.offset(-24, y, z), Blocks.DARK_OAK_SLAB.defaultBlockState(), 3);
                level.setBlock(base.offset(24, y, z), Blocks.DARK_OAK_SLAB.defaultBlockState(), 3);
            }
        }
        for (int x : new int[] {-18, -12, -6, 6, 12, 18}) {
            level.setBlock(base.offset(x, 1, 16), Blocks.FLOWERING_AZALEA.defaultBlockState(), 3);
            level.setBlock(base.offset(x, 5, 16), Blocks.OAK_LEAVES.defaultBlockState(), 3);
            level.setBlock(base.offset(x, 9, 16), Blocks.OAK_LEAVES.defaultBlockState(), 3);
        }
        for (int x = -6; x <= 6; x++) {
            level.setBlock(base.offset(x, 13, 16), Blocks.DARK_OAK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.SOUTH), 3);
            level.setBlock(base.offset(x, 14, 17), Blocks.DARK_OAK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.SOUTH), 3);
        }
        for (BlockPos pos : new BlockPos[] {
                base.offset(-18, 1, 16), base.offset(18, 1, 16),
                base.offset(-18, 7, 16), base.offset(18, 7, 16),
                base.offset(-24, 1, -8), base.offset(24, 1, -8)
        }) {
            placeLampPost(level, pos);
        }
    }

    private static void buildMainHouseArmorDisplay(ServerLevel level, BlockPos base) {
        BlockPos[] displayPositions = {
                base.offset(-9, 1, 9),
                base.offset(-6, 1, 9),
                base.offset(-3, 1, 9),
                base.offset(3, 1, 9),
                base.offset(6, 1, 9),
                base.offset(9, 1, 9)
        };
        BlockState[] displayBases = {
                Blocks.IRON_BLOCK.defaultBlockState(),
                Blocks.GOLD_BLOCK.defaultBlockState(),
                Blocks.DIAMOND_BLOCK.defaultBlockState(),
                Blocks.EMERALD_BLOCK.defaultBlockState(),
                Blocks.NETHERITE_BLOCK.defaultBlockState(),
                Blocks.AMETHYST_BLOCK.defaultBlockState()
        };

        for (int i = 0; i < displayPositions.length; i++) {
            level.setBlock(displayPositions[i].below(), displayBases[i], 3);
            level.setBlock(displayPositions[i].above(3), Blocks.SEA_LANTERN.defaultBlockState(), 3);
        }

        spawnArmorStand(level, displayPositions[0], Items.IRON_HELMET, Items.IRON_CHESTPLATE, Items.IRON_LEGGINGS, Items.IRON_BOOTS, "Ferro");
        spawnArmorStand(level, displayPositions[1], Items.GOLDEN_HELMET, Items.GOLDEN_CHESTPLATE, Items.GOLDEN_LEGGINGS, Items.GOLDEN_BOOTS, "Ouro");
        spawnArmorStand(level, displayPositions[2], Items.DIAMOND_HELMET, Items.DIAMOND_CHESTPLATE, Items.DIAMOND_LEGGINGS, Items.DIAMOND_BOOTS, "Diamante");
        spawnArmorStand(level, displayPositions[3], Items.NETHERITE_HELMET, Items.NETHERITE_CHESTPLATE, Items.NETHERITE_LEGGINGS, Items.NETHERITE_BOOTS, "Netherite");
        spawnArmorStand(level, displayPositions[4],
                MagicWorld.DRACONIC_AETHER_HELMET.get(),
                MagicWorld.DRACONIC_AETHER_CHESTPLATE.get(),
                MagicWorld.DRACONIC_AETHER_LEGGINGS.get(),
                MagicWorld.DRACONIC_AETHER_BOOTS.get(),
                "Magic World");
    }

    private static void decorateMainHouseExterior(ServerLevel level, BlockPos base) {
        for (int x = -28; x <= 28; x++) {
            for (int z = -36; z <= 20; z++) {
                boolean nearWall = Math.abs(x) == 25 || z == -33 || z == 17;
                if (!nearWall || (Math.abs(x) <= 3 && z == 17)) {
                    continue;
                }
                if ((x + z) % 5 == 0) {
                    level.setBlock(base.offset(x, 0, z), Blocks.MOSS_BLOCK.defaultBlockState(), 3);
                    level.setBlock(base.offset(x, 1, z), flowerFor(x * 31 + z), 3);
                } else if ((x + z) % 9 == 0) {
                    level.setBlock(base.offset(x, 0, z), Blocks.PODZOL.defaultBlockState(), 3);
                    level.setBlock(base.offset(x, 1, z), Blocks.FLOWERING_AZALEA.defaultBlockState(), 3);
                }
            }
        }

        for (BlockPos pos : new BlockPos[] {
                base.offset(-28, 0, -34), base.offset(28, 0, -34),
                base.offset(-28, 0, 18), base.offset(28, 0, 18),
                base.offset(-10, 0, 20), base.offset(10, 0, 20),
                base.offset(-25, 0, -8), base.offset(25, 0, -8)
        }) {
            placeLampPost(level, pos);
        }

        for (int z = -28; z <= 12; z += 8) {
            level.setBlock(base.offset(-24, 1, z), Blocks.OAK_LEAVES.defaultBlockState(), 3);
            level.setBlock(base.offset(-24, 2, z), Blocks.FLOWERING_AZALEA_LEAVES.defaultBlockState(), 3);
            level.setBlock(base.offset(24, 1, z), Blocks.OAK_LEAVES.defaultBlockState(), 3);
            level.setBlock(base.offset(24, 2, z), Blocks.FLOWERING_AZALEA_LEAVES.defaultBlockState(), 3);
        }

        for (int x = -18; x <= 18; x += 6) {
            level.setBlock(base.offset(x, 1, 18), Blocks.BARREL.defaultBlockState(), 3);
            level.setBlock(base.offset(x, 2, 18), Blocks.LANTERN.defaultBlockState(), 3);
        }
    }

    private static void buildGroundFloorRooms(ServerLevel level, BlockPos base) {
        for (int y = 1; y <= 5; y++) {
            for (int x = -22; x <= 22; x++) {
                boolean door = (x >= -2 && x <= 2) || (x >= -18 && x <= -15) || (x >= 15 && x <= 18);
                if (door && y <= 3) {
                    level.setBlock(base.offset(x, y, -8), Blocks.AIR.defaultBlockState(), 3);
                } else {
                    placeInteriorWall(level, base.offset(x, y, -8));
                }
            }
            for (int z = -29; z <= 13; z++) {
                boolean leftDoor = (z >= -20 && z <= -17) || (z >= 4 && z <= 7);
                boolean rightDoor = (z >= -20 && z <= -17) || (z >= 4 && z <= 7);
                if (leftDoor && y <= 3) {
                    level.setBlock(base.offset(-8, y, z), Blocks.AIR.defaultBlockState(), 3);
                } else {
                    placeInteriorWall(level, base.offset(-8, y, z));
                }
                if (rightDoor && y <= 3) {
                    level.setBlock(base.offset(8, y, z), Blocks.AIR.defaultBlockState(), 3);
                } else {
                    placeInteriorWall(level, base.offset(8, y, z));
                }
            }
        }

        placeBed(level, base.offset(-19, 1, -25), Direction.SOUTH);
        placeBed(level, base.offset(-17, 1, -25), Direction.SOUTH);
        placeDraconicArmorChest(level, base.offset(-15, 0, -25));
        placeChest(level, base.offset(-21, 1, -25), Direction.SOUTH);
        placeChest(level, base.offset(-21, 1, -23), Direction.SOUTH);
        putItems(level, base.offset(-21, 1, -25),
                new ItemStack(Items.WOODEN_SWORD), new ItemStack(Items.STONE_SWORD), new ItemStack(Items.IRON_SWORD),
                new ItemStack(Items.GOLDEN_SWORD), new ItemStack(Items.DIAMOND_SWORD), new ItemStack(Items.NETHERITE_SWORD),
                new ItemStack(Items.WOODEN_PICKAXE), new ItemStack(Items.STONE_PICKAXE), new ItemStack(Items.IRON_PICKAXE),
                new ItemStack(Items.GOLDEN_PICKAXE), new ItemStack(Items.DIAMOND_PICKAXE), new ItemStack(Items.NETHERITE_PICKAXE));
        putItems(level, base.offset(-21, 1, -23),
                new ItemStack(Items.LEATHER_HELMET), new ItemStack(Items.LEATHER_CHESTPLATE), new ItemStack(Items.LEATHER_LEGGINGS), new ItemStack(Items.LEATHER_BOOTS),
                new ItemStack(Items.CHAINMAIL_HELMET), new ItemStack(Items.CHAINMAIL_CHESTPLATE), new ItemStack(Items.CHAINMAIL_LEGGINGS), new ItemStack(Items.CHAINMAIL_BOOTS),
                new ItemStack(Items.IRON_HELMET), new ItemStack(Items.IRON_CHESTPLATE), new ItemStack(Items.IRON_LEGGINGS), new ItemStack(Items.IRON_BOOTS));
        level.setBlock(base.offset(-21, 1, -19), Blocks.WHITE_CARPET.defaultBlockState(), 3);
        level.setBlock(base.offset(-20, 1, -19), Blocks.WHITE_CARPET.defaultBlockState(), 3);
        level.setBlock(base.offset(-19, 1, -19), Blocks.WHITE_CARPET.defaultBlockState(), 3);

        level.setBlock(base.offset(12, 1, -25), Blocks.SMOKER.defaultBlockState(), 3);
        level.setBlock(base.offset(14, 1, -25), Blocks.FURNACE.defaultBlockState(), 3);
        level.setBlock(base.offset(16, 1, -25), Blocks.CAULDRON.defaultBlockState(), 3);
        level.setBlock(base.offset(18, 1, -25), Blocks.BARREL.defaultBlockState(), 3);
        level.setBlock(base.offset(20, 1, -25), Blocks.CRAFTING_TABLE.defaultBlockState(), 3);

        level.setBlock(base.offset(-20, 1, -2), Blocks.SMITHING_TABLE.defaultBlockState(), 3);
        level.setBlock(base.offset(-18, 1, -2), Blocks.ANVIL.defaultBlockState(), 3);
        level.setBlock(base.offset(-16, 1, -2), Blocks.GRINDSTONE.defaultBlockState(), 3);
        level.setBlock(base.offset(-14, 1, -2), Blocks.STONECUTTER.defaultBlockState(), 3);
        level.setBlock(base.offset(-12, 1, -2), Blocks.BLAST_FURNACE.defaultBlockState(), 3);

        for (int x = -4; x <= 4; x++) {
            level.setBlock(base.offset(x, 1, 2), Blocks.RED_CARPET.defaultBlockState(), 3);
            level.setBlock(base.offset(x, 1, 8), Blocks.RED_CARPET.defaultBlockState(), 3);
        }
        level.setBlock(base.offset(0, 1, 5), Blocks.JUKEBOX.defaultBlockState(), 3);
        level.setBlock(base.offset(3, 1, 5), Blocks.BELL.defaultBlockState(), 3);

        for (int z = -3; z <= 10; z++) {
            for (int y = 1; y <= 4; y++) {
                level.setBlock(base.offset(12, y, z), Blocks.BOOKSHELF.defaultBlockState(), 3);
                level.setBlock(base.offset(21, y, z), Blocks.BOOKSHELF.defaultBlockState(), 3);
            }
        }
        level.setBlock(base.offset(16, 1, 4), Blocks.LECTERN.defaultBlockState(), 3);
        level.setBlock(base.offset(18, 1, 4), Blocks.CARTOGRAPHY_TABLE.defaultBlockState(), 3);

        for (BlockPos pos : new BlockPos[] {
                base.offset(-20, 4, -25), base.offset(20, 4, -25),
                base.offset(-20, 4, 10), base.offset(20, 4, 10),
                base.offset(0, 4, 5), base.offset(0, 4, -20),
                base.offset(-14, 4, -14), base.offset(14, 4, -14),
                base.offset(-14, 4, 6), base.offset(14, 4, 6)
        }) {
            level.setBlock(pos, Blocks.LANTERN.defaultBlockState(), 3);
        }
        for (BlockPos pos : new BlockPos[] {
                base.offset(-18, 1, -12), base.offset(18, 1, -12),
                base.offset(-18, 1, 12), base.offset(18, 1, 12),
                base.offset(0, 1, -12), base.offset(0, 1, 12)
        }) {
            level.setBlock(pos, Blocks.SEA_LANTERN.defaultBlockState(), 3);
        }
    }

    private static void buildArcaneHall(ServerLevel level, BlockPos base) {
        for (int x = -18; x <= 18; x++) {
            for (int z = -25; z <= 10; z++) {
                level.setBlock(base.offset(x, 7, z), Blocks.BLUE_CARPET.defaultBlockState(), 3);
            }
        }
        level.setBlock(base.offset(0, 7, -8), Blocks.ENCHANTING_TABLE.defaultBlockState(), 3);
        level.setBlock(base.offset(0, 8, -8), Blocks.BEACON.defaultBlockState(), 3);
        level.setBlock(base.offset(-3, 7, -8), Blocks.AMETHYST_BLOCK.defaultBlockState(), 3);
        level.setBlock(base.offset(3, 7, -8), Blocks.LAPIS_BLOCK.defaultBlockState(), 3);
        level.setBlock(base.offset(0, 7, -12), Blocks.BREWING_STAND.defaultBlockState(), 3);
        level.setBlock(base.offset(-6, 7, -12), Blocks.SCULK.defaultBlockState(), 3);
        level.setBlock(base.offset(6, 7, -12), Blocks.CRYING_OBSIDIAN.defaultBlockState(), 3);

        for (int x = -10; x <= 10; x += 5) {
            for (int z = -22; z <= 6; z += 7) {
                level.setBlock(base.offset(x, 8, z), Blocks.END_ROD.defaultBlockState(), 3);
                level.setBlock(base.offset(x, 9, z), Blocks.BLUE_STAINED_GLASS.defaultBlockState(), 3);
            }
        }
        for (int x = -16; x <= 16; x += 8) {
            level.setBlock(base.offset(x, 7, 9), Blocks.LOOM.defaultBlockState(), 3);
            level.setBlock(base.offset(x, 8, 9), Blocks.PURPLE_BANNER.defaultBlockState(), 3);
        }
    }

    private static void buildRoofTerrace(ServerLevel level, BlockPos base) {
        for (int x = -20; x <= 20; x++) {
            for (int z = -26; z <= 10; z++) {
                level.setBlock(base.offset(x, 15, z), Blocks.SMOOTH_STONE.defaultBlockState(), 3);
            }
        }
        for (int x = -21; x <= 21; x++) {
            level.setBlock(base.offset(x, 16, -27), Blocks.SPRUCE_FENCE.defaultBlockState(), 3);
            level.setBlock(base.offset(x, 16, 11), Blocks.SPRUCE_FENCE.defaultBlockState(), 3);
        }
        for (int z = -27; z <= 11; z++) {
            level.setBlock(base.offset(-21, 16, z), Blocks.SPRUCE_FENCE.defaultBlockState(), 3);
            level.setBlock(base.offset(21, 16, z), Blocks.SPRUCE_FENCE.defaultBlockState(), 3);
        }
        for (BlockPos pos : new BlockPos[] {
                base.offset(-14, 16, -20), base.offset(14, 16, -20),
                base.offset(-14, 16, 4), base.offset(14, 16, 4),
                base.offset(0, 16, -8)
        }) {
            level.setBlock(pos, Blocks.SEA_LANTERN.defaultBlockState(), 3);
        }
        level.setBlock(base.offset(0, 16, -8), Blocks.BEACON.defaultBlockState(), 3);
        level.setBlock(base.offset(-4, 16, -8), Blocks.RESPAWN_ANCHOR.defaultBlockState(), 3);
        level.setBlock(base.offset(4, 16, -8), Blocks.DRAGON_EGG.defaultBlockState(), 3);
        placeChest(level, base.offset(-10, 16, -22), Direction.SOUTH);
        placeChest(level, base.offset(10, 16, -22), Direction.SOUTH);
    }

    private static void buildBasementRail(ServerLevel level, BlockPos base) {
        for (int x = -18; x <= 18; x++) {
            for (int z = -24; z <= 8; z++) {
                level.setBlock(base.offset(x, -6, z), Blocks.DEEPSLATE_TILES.defaultBlockState(), 3);
                level.setBlock(base.offset(x, -1, z), Blocks.SPRUCE_PLANKS.defaultBlockState(), 3);
                for (int y = -5; y <= -2; y++) {
                    boolean wall = x == -18 || x == 18 || z == -24 || z == 8;
                    level.setBlock(base.offset(x, y, z), wall ? Blocks.DEEPSLATE_BRICKS.defaultBlockState() : Blocks.AIR.defaultBlockState(), 3);
                }
            }
        }
        for (int x = -12; x <= 12; x++) {
            placeBasementRail(level, base.offset(x, -5, -20), x);
            placeBasementRail(level, base.offset(x, -5, 4), x);
        }
        for (int z = -20; z <= 4; z++) {
            placeBasementRail(level, base.offset(-12, -5, z), z);
            placeBasementRail(level, base.offset(12, -5, z), z);
        }
        level.setBlock(base.offset(0, -4, -22), Blocks.CHEST.defaultBlockState(), 3);
        putItems(level, base.offset(0, -4, -22),
                new ItemStack(Items.MINECART, 4),
                new ItemStack(Items.RAIL, 64),
                new ItemStack(Items.POWERED_RAIL, 32),
                new ItemStack(Items.REDSTONE_TORCH, 32));
        for (int step = 0; step <= 5; step++) {
            level.setBlock(base.offset(-3, -5 + step, 6 - step), Blocks.STONE_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.NORTH), 3);
            level.setBlock(base.offset(-2, -5 + step, 6 - step), Blocks.STONE_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.NORTH), 3);
        }
        level.setBlock(base.offset(0, -3, -18), Blocks.REDSTONE_LAMP.defaultBlockState(), 3);
        level.setBlock(base.offset(0, -4, -18), Blocks.REDSTONE_BLOCK.defaultBlockState(), 3);
        level.setBlock(base.offset(2, -4, -18), Blocks.LEVER.defaultBlockState(), 3);
        EntityType.MINECART.spawn(level, base.offset(0, -5, -20), EntitySpawnReason.STRUCTURE);
    }

    private static void placeBasementRail(ServerLevel level, BlockPos railPos, int index) {
        boolean powered = Math.floorMod(index, 6) == 0;
        level.setBlock(railPos.below(), powered ? Blocks.REDSTONE_BLOCK.defaultBlockState() : Blocks.DEEPSLATE_TILES.defaultBlockState(), 3);
        level.setBlock(railPos, powered ? Blocks.POWERED_RAIL.defaultBlockState() : Blocks.RAIL.defaultBlockState(), 3);
    }

    private static void placeHouseWall(ServerLevel level, BlockPos pos, boolean pillar) {
        level.setBlock(pos, (pillar ? Blocks.STRIPPED_DARK_OAK_LOG : Blocks.SMOOTH_SANDSTONE).defaultBlockState(), 3);
    }

    private static void placeInteriorWall(ServerLevel level, BlockPos pos) {
        level.setBlock(pos, Blocks.SPRUCE_PLANKS.defaultBlockState(), 3);
    }

    private static void placeWindow(ServerLevel level, BlockPos pos) {
        level.setBlock(pos, Blocks.GLASS_PANE.defaultBlockState(), 3);
        level.setBlock(pos.above(), Blocks.GLASS_PANE.defaultBlockState(), 3);
    }

    private static void placeHouseDoor(ServerLevel level, BlockPos foot, Direction facing) {
        BlockState lower = Blocks.DARK_OAK_DOOR.defaultBlockState()
                .setValue(DoorBlock.FACING, facing)
                .setValue(DoorBlock.HALF, DoubleBlockHalf.LOWER);
        BlockState upper = Blocks.DARK_OAK_DOOR.defaultBlockState()
                .setValue(DoorBlock.FACING, facing)
                .setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER);
        level.setBlock(foot, lower, 3);
        level.setBlock(foot.above(), upper, 3);
    }

    private static void buildStarterPortal(ServerLevel level, BlockPos center) {
        for (int x = -16; x <= 16; x++) {
            for (int z = -16; z <= 16; z++) {
                level.setBlock(center.offset(x, -1, z), Blocks.GRASS_BLOCK.defaultBlockState(), 3);
                level.setBlock(center.offset(x, 0, z), Blocks.GRASS_BLOCK.defaultBlockState(), 3);
                for (int y = 1; y <= overheadClearHeight(level, center); y++) {
                    level.setBlock(center.offset(x, y, z), Blocks.AIR.defaultBlockState(), 3);
                }
            }
        }
        for (int x = -3; x <= 3; x++) {
            for (int z = -2; z <= 2; z++) {
                boolean edge = Math.abs(x) == 3 || Math.abs(z) == 2;
                level.setBlock(center.offset(x, 0, z), edge ? Blocks.OAK_SLAB.defaultBlockState() : Blocks.DIRT_PATH.defaultBlockState(), 3);
            }
        }

        for (int y = 1; y <= 5; y++) {
            level.setBlock(center.offset(-2, y, 0), Blocks.STRIPPED_OAK_LOG.defaultBlockState(), 3);
            level.setBlock(center.offset(2, y, 0), Blocks.STRIPPED_OAK_LOG.defaultBlockState(), 3);
        }
        for (int x = -3; x <= 3; x++) {
            level.setBlock(center.offset(x, 5, 0), Blocks.OAK_PLANKS.defaultBlockState(), 3);
            level.setBlock(center.offset(x, 6, 0), Blocks.DARK_OAK_SLAB.defaultBlockState(), 3);
            level.setBlock(center.offset(x, 7, 0), Blocks.OAK_LEAVES.defaultBlockState(), 3);
        }
        for (int y = 2; y <= 4; y++) {
            level.setBlock(center.offset(-1, y, 0), Blocks.PURPLE_STAINED_GLASS.defaultBlockState(), 3);
            level.setBlock(center.offset(1, y, 0), Blocks.MAGENTA_STAINED_GLASS.defaultBlockState(), 3);
            level.setBlock(center.offset(0, y, 0), Blocks.AIR.defaultBlockState(), 3);
        }
        level.setBlock(center.offset(-1, 1, 0), Blocks.PURPLE_STAINED_GLASS.defaultBlockState(), 3);
        level.setBlock(center.offset(1, 1, 0), Blocks.MAGENTA_STAINED_GLASS.defaultBlockState(), 3);
        level.setBlock(center.offset(0, 1, 0), Blocks.AIR.defaultBlockState(), 3);
        level.setBlock(center.offset(-1, 0, 0), Blocks.AMETHYST_BLOCK.defaultBlockState(), 3);
        level.setBlock(center.offset(0, 0, 0), Blocks.END_PORTAL_FRAME.defaultBlockState(), 3);
        level.setBlock(center.offset(1, 0, 0), Blocks.AMETHYST_BLOCK.defaultBlockState(), 3);
        level.setBlock(center.offset(-3, 1, 0), Blocks.OAK_FENCE.defaultBlockState(), 3);
        level.setBlock(center.offset(3, 1, 0), Blocks.OAK_FENCE.defaultBlockState(), 3);
        level.setBlock(center.offset(-3, 2, 0), Blocks.LANTERN.defaultBlockState(), 3);
        level.setBlock(center.offset(3, 2, 0), Blocks.LANTERN.defaultBlockState(), 3);
        level.setBlock(center.offset(-2, 6, 0), Blocks.GLOWSTONE.defaultBlockState(), 3);
        level.setBlock(center.offset(2, 6, 0), Blocks.GLOWSTONE.defaultBlockState(), 3);
        level.setBlock(center.offset(-3, 3, 0), Blocks.VINE.defaultBlockState(), 3);
        level.setBlock(center.offset(3, 3, 0), Blocks.VINE.defaultBlockState(), 3);

        decoratePortalGarden(level, center);
        clearPortalGrassRing(level, center);

        placePortalSign(level, center.offset(-10, 1, -4), 4,
                "Portal", "Premium", "Entre no", "centro");
        placePortalSign(level, center.offset(10, 1, -4), 12,
                "Volte aqui", "para alternar", "Premium", "Simples");
        placeMagicWorldGearChest(level, center.offset(-10, 0, 6));
        placeMagicWorldGearChest(level, center.offset(10, 0, 6));

        for (BlockPos pos : new BlockPos[] {
                center.offset(-10, 1, -10), center.offset(10, 1, -10),
                center.offset(-10, 1, 10), center.offset(10, 1, 10)
        }) {
            placeLampPost(level, pos);
        }
    }

    private static void clearPortalGrassRing(ServerLevel level, BlockPos center) {
        for (int x = -8; x <= 8; x++) {
            for (int z = -8; z <= 8; z++) {
                if (Math.abs(x) <= 3 && Math.abs(z) <= 2) {
                    continue;
                }
                level.setBlock(center.offset(x, 0, z), Blocks.GRASS_BLOCK.defaultBlockState(), 3);
                for (int y = 1; y <= 10; y++) {
                    level.setBlock(center.offset(x, y, z), Blocks.AIR.defaultBlockState(), 3);
                }
            }
        }
    }

    private static BlockState accentLight(int index) {
        return switch (index % 4) {
            case 0 -> Blocks.BLUE_STAINED_GLASS.defaultBlockState();
            case 1 -> Blocks.CYAN_STAINED_GLASS.defaultBlockState();
            case 2 -> Blocks.PURPLE_STAINED_GLASS.defaultBlockState();
            default -> Blocks.MAGENTA_STAINED_GLASS.defaultBlockState();
        };
    }

    private static void decoratePortalGarden(ServerLevel level, BlockPos center) {
        for (int x = -20; x <= 20; x++) {
            for (int z = -16; z <= 16; z++) {
                if (Math.abs(x) <= 6 && Math.abs(z) <= 4) {
                    continue;
                }
                if ((x * x + z * z) % 5 == 0) {
                    level.setBlock(center.offset(x, 0, z), Blocks.GRASS_BLOCK.defaultBlockState(), 3);
                    level.setBlock(center.offset(x, 1, z), flowerFor(x + z), 3);
                } else if ((x + z) % 13 == 0) {
                    level.setBlock(center.offset(x, 0, z), Blocks.MOSS_BLOCK.defaultBlockState(), 3);
                    level.setBlock(center.offset(x, 1, z), Blocks.FERN.defaultBlockState(), 3);
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
            level.setBlock(pos.above(2).relative(Direction.NORTH), Blocks.VINE.defaultBlockState(), 3);
        }
        for (int x = -12; x <= 12; x += 6) {
            level.setBlock(center.offset(x, 0, 12), Blocks.MOSSY_COBBLESTONE.defaultBlockState(), 3);
            level.setBlock(center.offset(x, 1, 12), Blocks.FLOWERING_AZALEA.defaultBlockState(), 3);
            level.setBlock(center.offset(x, 0, -12), Blocks.MOSSY_COBBLESTONE.defaultBlockState(), 3);
            level.setBlock(center.offset(x, 1, -12), Blocks.AZALEA.defaultBlockState(), 3);
        }
        for (BlockPos pos : new BlockPos[] {
                center.offset(-12, 1, 8), center.offset(12, 1, 8),
                center.offset(-14, 1, -6), center.offset(14, 1, -6),
                center.offset(0, 1, 10)
        }) {
            level.setBlock(pos, Blocks.CAMPFIRE.defaultBlockState(), 3);
            level.setBlock(pos.above(), Blocks.LIGHT_BLUE_STAINED_GLASS.defaultBlockState(), 3);
        }
        spawnNamedCharacter(level, EntityType.PARROT, center.offset(-9, 1, 7), "Passaro Azul do Portal");
        spawnNamedCharacter(level, EntityType.PARROT, center.offset(9, 1, 7), "Passaro Verde do Portal");
        spawnNamedCharacter(level, EntityType.PARROT, center.offset(-11, 1, -7), "Passaro Encantado");
        spawnNamedCharacter(level, EntityType.RABBIT, center.offset(6, 1, 10), "Coelho do Jardim");
        spawnNamedCharacter(level, EntityType.RABBIT, center.offset(-6, 1, 10), "Coelho Brilhante");
        spawnNamedCharacter(level, EntityType.ALLAY, center.offset(0, 2, 8), "Brilho do Portal");
    }

    private static void placeMagicWorldGearChest(ServerLevel level, BlockPos center) {
        BlockPos chestPos = center.offset(0, 1, 0);
        placeChest(level, chestPos, Direction.NORTH);
        putItems(level, chestPos,
                new ItemStack(MagicWorld.DRACONIC_AETHER_HELMET.get()),
                new ItemStack(MagicWorld.DRACONIC_AETHER_CHESTPLATE.get()),
                new ItemStack(MagicWorld.DRACONIC_AETHER_LEGGINGS.get()),
                new ItemStack(MagicWorld.DRACONIC_AETHER_BOOTS.get()),
                new ItemStack(MagicWorld.VARINHA_MAGICA.get()),
                new ItemStack(MagicWorld.VARINHA_MAGICA.get()),
                new ItemStack(Items.ENCHANTED_GOLDEN_APPLE, 8),
                new ItemStack(Items.TOTEM_OF_UNDYING, 4));
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

    private static void placeDraconicArmorChest(ServerLevel level, BlockPos center) {
        BlockPos chestPos = center.offset(0, 1, 4);
        placeChest(level, chestPos, Direction.NORTH);
        putItems(level, chestPos,
                new ItemStack(MagicWorld.DRACONIC_AETHER_HELMET.get()),
                new ItemStack(MagicWorld.DRACONIC_AETHER_CHESTPLATE.get()),
                new ItemStack(MagicWorld.DRACONIC_AETHER_LEGGINGS.get()),
                new ItemStack(MagicWorld.DRACONIC_AETHER_BOOTS.get()),
                new ItemStack(MagicWorld.VARINHA_MAGICA.get()),
                new ItemStack(MagicWorld.VARINHA_MAGICA.get()),
                new ItemStack(Items.ENCHANTED_GOLDEN_APPLE, 8),
                new ItemStack(Items.TOTEM_OF_UNDYING, 4),
                new ItemStack(Items.NETHERITE_SWORD),
                new ItemStack(Items.NETHERITE_PICKAXE),
                new ItemStack(Items.EXPERIENCE_BOTTLE, 64),
                new ItemStack(Items.DIAMOND, 64),
                new ItemStack(Items.EMERALD, 64));
        placePortalSign(level, center.offset(0, 1, 5), 0,
                "Armadura", "Draconica", "do Eter", "Pegue aqui");
    }

    private static void buildGardenFarmAndLake(ServerLevel level, BlockPos base) {
        buildStarterRoad(level, base, Direction.SOUTH, 76);
        buildStarterRoad(level, base, Direction.NORTH, 62);
        buildStarterRoad(level, base, Direction.EAST, 76);
        buildStarterRoad(level, base, Direction.WEST, 76);

        buildCropField(level, base.offset(-42, 0, 16), Blocks.WHEAT.defaultBlockState().setValue(CropBlock.AGE, 7));
        buildCropField(level, base.offset(-22, 0, 16), Blocks.CARROTS.defaultBlockState().setValue(CropBlock.AGE, 7));
        buildCropField(level, base.offset(-42, 0, 36), Blocks.POTATOES.defaultBlockState().setValue(CropBlock.AGE, 7));
        buildCropField(level, base.offset(-22, 0, 36), Blocks.BEETROOTS.defaultBlockState());

        buildAnimalPen(level, base.offset(24, 0, 16), 14, 12, true);
        buildAnimalPen(level, base.offset(40, 0, 16), 14, 12, true);
        buildAnimalPen(level, base.offset(24, 0, 32), 14, 12, true);
        buildAnimalPen(level, base.offset(40, 0, 32), 14, 12, true);
        buildAnimalPen(level, base.offset(32, 0, 48), 18, 12, true);
        buildAnimalPen(level, base.offset(56, 0, 16), 14, 12, true);
        buildAnimalPen(level, base.offset(56, 0, 32), 14, 12, true);
        buildAnimalPen(level, base.offset(56, 0, 48), 14, 12, true);
        buildAnimalPen(level, base.offset(24, 0, 64), 22, 12, true);

        buildWaterFountain(level, base.offset(0, 0, -42));
        buildCareStation(level, base.offset(58, 0, 12));

        for (BlockPos pos : new BlockPos[] {
                base.offset(-24, 0, 18), base.offset(24, 0, 18),
                base.offset(-24, 0, -32), base.offset(24, 0, -32),
                base.offset(-44, 0, 36), base.offset(44, 0, 36),
                base.offset(-12, 0, 54), base.offset(12, 0, 54)
        }) {
            placeLampPost(level, pos);
        }
    }

    private static void buildImportedGardenFarmAndLake(ServerLevel level, BlockPos base) {
        buildCropField(level, base.offset(-122, 0, -42), Blocks.WHEAT.defaultBlockState().setValue(CropBlock.AGE, 7));
        buildCropField(level, base.offset(-102, 0, -42), Blocks.CARROTS.defaultBlockState().setValue(CropBlock.AGE, 7));
        buildCropField(level, base.offset(-122, 0, -22), Blocks.POTATOES.defaultBlockState().setValue(CropBlock.AGE, 7));
        buildCropField(level, base.offset(-102, 0, -22), Blocks.BEETROOTS.defaultBlockState());

        buildAnimalPen(level, base.offset(46, 0, -42), 14, 12, true);
        buildAnimalPen(level, base.offset(62, 0, -42), 14, 12, true);
        buildAnimalPen(level, base.offset(78, 0, -42), 14, 12, true);
        buildAnimalPen(level, base.offset(46, 0, -26), 14, 12, true);
        buildAnimalPen(level, base.offset(62, 0, -26), 14, 12, true);
        buildAnimalPen(level, base.offset(78, 0, -26), 14, 12, true);
        buildAnimalPen(level, base.offset(46, 0, -10), 14, 12, true);
        buildAnimalPen(level, base.offset(62, 0, -10), 14, 12, true);
        buildAnimalPen(level, base.offset(78, 0, -10), 22, 12, true);

        buildWaterFountain(level, base.offset(-32, 0, 76));
        buildCareStation(level, base.offset(104, 0, -26));
        buildEstateFence(level, base, -128, 122, -76, 80);

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

    private static void buildEstateFence(ServerLevel level, BlockPos base, int minX, int maxX, int minZ, int maxZ) {
        for (int x = minX; x <= maxX; x++) {
            placeEstateFenceBlock(level, base.offset(x, 1, minZ), x == 0);
            placeEstateFenceBlock(level, base.offset(x, 1, maxZ), x == 0);
            level.setBlock(base.offset(x, 0, minZ), Blocks.GRASS_BLOCK.defaultBlockState(), 3);
            level.setBlock(base.offset(x, 0, maxZ), Blocks.GRASS_BLOCK.defaultBlockState(), 3);
        }
        for (int z = minZ; z <= maxZ; z++) {
            placeEstateFenceBlock(level, base.offset(minX, 1, z), z == 0);
            placeEstateFenceBlock(level, base.offset(maxX, 1, z), z == 0);
            level.setBlock(base.offset(minX, 0, z), Blocks.GRASS_BLOCK.defaultBlockState(), 3);
            level.setBlock(base.offset(maxX, 0, z), Blocks.GRASS_BLOCK.defaultBlockState(), 3);
        }

        level.setBlock(base.offset(0, 1, minZ), Blocks.OAK_FENCE_GATE.defaultBlockState()
                .setValue(FenceGateBlock.FACING, Direction.NORTH), 3);
        level.setBlock(base.offset(0, 1, maxZ), Blocks.OAK_FENCE_GATE.defaultBlockState()
                .setValue(FenceGateBlock.FACING, Direction.SOUTH), 3);
        level.setBlock(base.offset(minX, 1, 0), Blocks.OAK_FENCE_GATE.defaultBlockState()
                .setValue(FenceGateBlock.FACING, Direction.WEST), 3);
        level.setBlock(base.offset(maxX, 1, 0), Blocks.OAK_FENCE_GATE.defaultBlockState()
                .setValue(FenceGateBlock.FACING, Direction.EAST), 3);
    }

    private static void placeEstateFenceBlock(ServerLevel level, BlockPos pos, boolean gateOpening) {
        if (!gateOpening) {
            level.setBlock(pos, Blocks.OAK_FENCE.defaultBlockState(), 3);
        }
    }

    private static void buildStarterRoad(ServerLevel level, BlockPos base, Direction direction, int length) {
        for (int step = 0; step <= length; step++) {
            for (int side = -3; side <= 3; side++) {
                BlockPos pos = direction.getAxis() == Direction.Axis.Z
                        ? base.relative(direction, step).offset(side, 0, 0)
                        : base.relative(direction, step).offset(0, 0, side);
                level.setBlock(pos, Math.abs(side) <= 1 ? Blocks.DIRT_PATH.defaultBlockState() : Blocks.GRASS_BLOCK.defaultBlockState(), 3);
                if (Math.abs(side) == 3 && step % 12 == 0) {
                    placeLampPost(level, pos);
                }
            }
        }
    }

    private static void buildCropField(ServerLevel level, BlockPos corner, BlockState cropState) {
        for (int x = 0; x <= 17; x++) {
            for (int z = 0; z <= 17; z++) {
                boolean edge = x == 0 || x == 17 || z == 0 || z == 17;
                boolean water = x == 8 || z == 8;
                BlockPos pos = corner.offset(x, 0, z);
                level.setBlock(pos.below(), Blocks.GRASS_BLOCK.defaultBlockState(), 3);
                for (int y = 1; y <= 8; y++) {
                    level.setBlock(pos.above(y), Blocks.AIR.defaultBlockState(), 3);
                }
                if (edge) {
                    level.setBlock(pos, Blocks.SPRUCE_FENCE.defaultBlockState(), 3);
                } else if (water) {
                    level.setBlock(pos, Blocks.WATER.defaultBlockState(), 3);
                    if ((x + z) % 6 == 0) {
                        level.setBlock(pos.above(), Blocks.LILY_PAD.defaultBlockState(), 3);
                    }
                } else {
                    level.setBlock(pos, Blocks.FARMLAND.defaultBlockState(), 3);
                    level.setBlock(pos.above(), cropState, 3);
                }
            }
        }
        level.setBlock(corner.offset(8, 1, 8), Blocks.COMPOSTER.defaultBlockState(), 3);
        placeChest(level, corner.offset(15, 1, 15), Direction.NORTH);
        putItems(level, corner.offset(15, 1, 15),
                new ItemStack(Items.BONE_MEAL, 64),
                new ItemStack(Items.WHEAT_SEEDS, 64),
                new ItemStack(Items.CARROT, 64),
                new ItemStack(Items.POTATO, 64));
    }

    private static void buildAnimalPen(ServerLevel level, BlockPos corner, int width, int depth, boolean roof) {
        for (int x = 0; x <= width; x++) {
            for (int z = 0; z <= depth; z++) {
                BlockPos pos = corner.offset(x, 0, z);
                boolean edge = x == 0 || x == width || z == 0 || z == depth;
                level.setBlock(pos.below(), Blocks.GRASS_BLOCK.defaultBlockState(), 3);
                level.setBlock(pos, Blocks.GRASS_BLOCK.defaultBlockState(), 3);
                for (int y = 1; y <= 8; y++) {
                    level.setBlock(pos.above(y), Blocks.AIR.defaultBlockState(), 3);
                }
                if (edge) {
                    level.setBlock(pos.above(), Blocks.OAK_FENCE.defaultBlockState(), 3);
                }
                if (roof && x >= 2 && x <= width - 2 && z >= 1 && z <= 4) {
                    level.setBlock(pos.above(4), Blocks.OAK_SLAB.defaultBlockState(), 3);
                }
            }
        }
        level.setBlock(corner.offset(width / 2, 1, 0), Blocks.OAK_FENCE_GATE.defaultBlockState()
                .setValue(FenceGateBlock.FACING, Direction.NORTH), 3);
        for (BlockPos pos : new BlockPos[] {
                corner.offset(2, 0, -2), corner.offset(4, 0, -2),
                corner.offset(6, 0, -2), corner.offset(width - 2, 0, -2)
        }) {
            level.setBlock(pos, Blocks.GRASS_BLOCK.defaultBlockState(), 3);
            level.setBlock(pos.above(), Blocks.AIR.defaultBlockState(), 3);
            level.setBlock(pos.above(2), Blocks.AIR.defaultBlockState(), 3);
        }
        level.setBlock(corner.offset(2, 1, -2), Blocks.WATER_CAULDRON.defaultBlockState(), 3);
        level.setBlock(corner.offset(4, 1, -2), Blocks.HAY_BLOCK.defaultBlockState(), 3);
        level.setBlock(corner.offset(6, 1, -2), Blocks.COMPOSTER.defaultBlockState(), 3);
        placeChest(level, corner.offset(width - 2, 1, -2), Direction.SOUTH);
        putItems(level, corner.offset(width - 2, 1, -2),
                new ItemStack(Items.WHEAT, 64),
                new ItemStack(Items.CARROT, 64),
                new ItemStack(Items.WHEAT_SEEDS, 64),
                new ItemStack(Items.LEAD, 8));
    }

    private static BlockState stableFloorFor(int x, int z) {
        if ((x + z) % 7 == 0) {
            return Blocks.HAY_BLOCK.defaultBlockState();
        }
        if ((x + z) % 3 == 0) {
            return Blocks.SPRUCE_PLANKS.defaultBlockState();
        }
        return Blocks.OAK_PLANKS.defaultBlockState();
    }

    private static void buildWaterFountain(ServerLevel level, BlockPos center) {
        for (int x = -6; x <= 6; x++) {
            for (int z = -6; z <= 6; z++) {
                boolean edge = Math.abs(x) == 6 || Math.abs(z) == 6;
                level.setBlock(center.offset(x, 0, z), edge ? Blocks.STONE_BRICKS.defaultBlockState() : Blocks.WATER.defaultBlockState(), 3);
            }
        }
        level.setBlock(center, Blocks.SEA_LANTERN.defaultBlockState(), 3);
        level.setBlock(center.above(), Blocks.WATER.defaultBlockState(), 3);
    }

    private static void buildCareStation(ServerLevel level, BlockPos pos) {
        level.setBlock(pos, Blocks.HAY_BLOCK.defaultBlockState(), 3);
        level.setBlock(pos.offset(1, 0, 0), Blocks.COMPOSTER.defaultBlockState(), 3);
        level.setBlock(pos.offset(2, 0, 0), Blocks.WATER_CAULDRON.defaultBlockState(), 3);
        level.setBlock(pos.offset(3, 0, 0), Blocks.BARREL.defaultBlockState(), 3);
        putItems(level, pos.offset(3, 0, 0),
                new ItemStack(Items.WHEAT, 64),
                new ItemStack(Items.CARROT, 64),
                new ItemStack(Items.BEETROOT, 64),
                new ItemStack(Items.WHEAT_SEEDS, 64));
    }

    private static void placeLampPost(ServerLevel level, BlockPos pos) {
        level.setBlock(pos, Blocks.GLOWSTONE.defaultBlockState(), 3);
        for (int y = 1; y <= 3; y++) {
            level.setBlock(pos.above(y), Blocks.OAK_FENCE.defaultBlockState(), 3);
        }
        level.setBlock(pos.above(4), Blocks.LANTERN.defaultBlockState(), 3);
    }

    private static void decorateImportedHouseAddons(ServerLevel level, BlockPos base) {
        cleanupOutdoorImportedHouseFurniture(level, base);
        for (BlockPos pos : new BlockPos[] {
                base.offset(-76, 1, -54), base.offset(28, 1, -54),
                base.offset(-76, 1, 64), base.offset(28, 1, 64),
                base.offset(-46, 1, 72), base.offset(8, 1, 72),
                base.offset(-90, 1, 0), base.offset(42, 1, 0)
        }) {
            placeLampPost(level, pos);
        }

        for (BlockPos pos : new BlockPos[] {
                base.offset(0, 3, 0), base.offset(4, 3, 0),
                base.offset(-4, 3, 0), base.offset(0, 4, 4),
                base.offset(0, 4, -4), base.offset(8, 4, 8),
                base.offset(-8, 4, 8), base.offset(8, 4, -8),
                base.offset(-8, 4, -8)
        }) {
            level.setBlock(pos, Blocks.SEA_LANTERN.defaultBlockState(), 3);
        }

        placeSafeBed(level, base.offset(-2, 1, 0), Direction.EAST);
        BlockPos carpet = findSafeInteriorFloor(level, base.offset(0, 1, 0), 24, 8);
        if (carpet != null) {
            level.setBlock(carpet, Blocks.WHITE_CARPET.defaultBlockState(), 3);
        }
    }

    private static void fillImportedHouseChests(ServerLevel level, BlockPos base) {
        cleanupOutdoorImportedHouseFurniture(level, base);
        BlockPos[] chests = new BlockPos[] {
                placeSafeChest(level, base.offset(-8, 1, -6), Direction.SOUTH),
                placeSafeChest(level, base.offset(-4, 1, -6), Direction.SOUTH),
                placeSafeChest(level, base.offset(4, 1, -6), Direction.SOUTH),
                placeSafeChest(level, base.offset(8, 1, -6), Direction.SOUTH),
                placeSafeChest(level, base.offset(-8, 1, 6), Direction.NORTH),
                placeSafeChest(level, base.offset(-4, 1, 6), Direction.NORTH),
                placeSafeChest(level, base.offset(4, 1, 6), Direction.NORTH),
                placeSafeChest(level, base.offset(8, 1, 6), Direction.NORTH)
        };

        putItems(level, chests[0],
                new ItemStack(Items.NETHERITE_HELMET), new ItemStack(Items.NETHERITE_CHESTPLATE),
                new ItemStack(Items.NETHERITE_LEGGINGS), new ItemStack(Items.NETHERITE_BOOTS),
                new ItemStack(Items.DIAMOND_HELMET), new ItemStack(Items.DIAMOND_CHESTPLATE),
                new ItemStack(Items.DIAMOND_LEGGINGS), new ItemStack(Items.DIAMOND_BOOTS),
                new ItemStack(MagicWorld.DRACONIC_AETHER_HELMET.get()), new ItemStack(MagicWorld.DRACONIC_AETHER_CHESTPLATE.get()),
                new ItemStack(MagicWorld.DRACONIC_AETHER_LEGGINGS.get()), new ItemStack(MagicWorld.DRACONIC_AETHER_BOOTS.get()),
                new ItemStack(MagicWorld.VARINHA_MAGICA.get()));
        putItems(level, chests[1],
                new ItemStack(Items.WOODEN_SWORD), new ItemStack(Items.STONE_SWORD), new ItemStack(Items.IRON_SWORD),
                new ItemStack(Items.GOLDEN_SWORD), new ItemStack(Items.DIAMOND_SWORD), new ItemStack(Items.NETHERITE_SWORD),
                new ItemStack(Items.BOW), new ItemStack(Items.CROSSBOW), new ItemStack(Items.TRIDENT),
                new ItemStack(Items.ARROW, 64), new ItemStack(Items.SHIELD), new ItemStack(MagicWorld.VARINHA_MAGICA.get()));
        putItems(level, chests[2],
                new ItemStack(Items.WOODEN_PICKAXE), new ItemStack(Items.STONE_PICKAXE), new ItemStack(Items.IRON_PICKAXE),
                new ItemStack(Items.GOLDEN_PICKAXE), new ItemStack(Items.DIAMOND_PICKAXE), new ItemStack(Items.NETHERITE_PICKAXE),
                new ItemStack(Items.WOODEN_AXE), new ItemStack(Items.IRON_AXE), new ItemStack(Items.DIAMOND_AXE),
                new ItemStack(Items.NETHERITE_AXE), new ItemStack(Items.NETHERITE_SHOVEL), new ItemStack(Items.NETHERITE_HOE));
        putItems(level, chests[3],
                new ItemStack(Items.COOKED_BEEF, 64), new ItemStack(Items.COOKED_PORKCHOP, 64),
                new ItemStack(Items.COOKED_CHICKEN, 64), new ItemStack(Items.BREAD, 64),
                new ItemStack(Items.GOLDEN_CARROT, 64), new ItemStack(Items.GOLDEN_APPLE, 32),
                new ItemStack(Items.ENCHANTED_GOLDEN_APPLE, 16), new ItemStack(Items.HONEY_BOTTLE, 16),
                new ItemStack(Items.CAKE, 8), new ItemStack(Items.TOTEM_OF_UNDYING, 8));
        putItems(level, chests[4],
                new ItemStack(Items.OAK_LOG, 64), new ItemStack(Items.SPRUCE_LOG, 64),
                new ItemStack(Items.DARK_OAK_LOG, 64), new ItemStack(Items.STONE_BRICKS, 64),
                new ItemStack(Items.BRICKS, 64), new ItemStack(Items.GLASS, 64),
                new ItemStack(Items.IRON_BLOCK, 32), new ItemStack(Items.GOLD_BLOCK, 16),
                new ItemStack(Items.DIAMOND_BLOCK, 8), new ItemStack(Items.EMERALD_BLOCK, 8));
        putItems(level, chests[5],
                new ItemStack(Items.CRAFTING_TABLE, 16), new ItemStack(Items.FURNACE, 16),
                new ItemStack(Items.BLAST_FURNACE, 8), new ItemStack(Items.SMOKER, 8),
                new ItemStack(Items.ANVIL, 4), new ItemStack(Items.SMITHING_TABLE, 4),
                new ItemStack(Items.ENCHANTING_TABLE, 4), new ItemStack(Items.BREWING_STAND, 8),
                new ItemStack(Items.CHEST, 32), new ItemStack(Items.BARREL, 32));
        putItems(level, chests[6],
                new ItemStack(Items.WHEAT_SEEDS, 64), new ItemStack(Items.CARROT, 64),
                new ItemStack(Items.POTATO, 64), new ItemStack(Items.BEETROOT_SEEDS, 64),
                new ItemStack(Items.MELON_SEEDS, 64), new ItemStack(Items.PUMPKIN_SEEDS, 64),
                new ItemStack(Items.BONE_MEAL, 64), new ItemStack(Items.LEAD, 32),
                new ItemStack(Items.SADDLE, 8), new ItemStack(Items.NAME_TAG, 16));
        putItems(level, chests[7],
                new ItemStack(Items.DIAMOND, 64), new ItemStack(Items.EMERALD, 64),
                new ItemStack(Items.GOLD_INGOT, 64), new ItemStack(Items.IRON_INGOT, 64),
                new ItemStack(Items.NETHERITE_INGOT, 16), new ItemStack(Items.EXPERIENCE_BOTTLE, 64),
                new ItemStack(Items.BEACON, 4), new ItemStack(Items.ELYTRA),
                new ItemStack(Items.FIREWORK_ROCKET, 64), new ItemStack(Items.LANTERN, 64));
    }

    private static void fillStarterChests(ServerLevel level, BlockPos base) {
        putItems(level, base.offset(-18, 1, -28),
                new ItemStack(Items.NETHERITE_SWORD),
                new ItemStack(Items.NETHERITE_AXE),
                new ItemStack(Items.NETHERITE_PICKAXE),
                new ItemStack(Items.NETHERITE_SHOVEL),
                new ItemStack(Items.NETHERITE_HOE),
                new ItemStack(Items.BOW),
                new ItemStack(Items.ARROW, 64),
                new ItemStack(Items.SHIELD),
                new ItemStack(Items.CROSSBOW),
                new ItemStack(Items.TRIDENT));

        putItems(level, base.offset(-8, 1, -28),
                new ItemStack(Items.NETHERITE_HELMET),
                new ItemStack(Items.NETHERITE_CHESTPLATE),
                new ItemStack(Items.NETHERITE_LEGGINGS),
                new ItemStack(Items.NETHERITE_BOOTS),
                new ItemStack(Items.GOLDEN_APPLE, 8),
                new ItemStack(Items.COOKED_BEEF, 32),
                new ItemStack(Items.TORCH, 64),
                new ItemStack(Items.BREAD, 32),
                new ItemStack(Items.POTION, 8),
                new ItemStack(Items.EXPERIENCE_BOTTLE, 64));

        putItems(level, base.offset(8, 1, -28),
                new ItemStack(Items.OAK_LOG, 64),
                new ItemStack(Items.SPRUCE_LOG, 64),
                new ItemStack(Items.DARK_OAK_LOG, 64),
                new ItemStack(Items.STONE_BRICKS, 64),
                new ItemStack(Items.IRON_INGOT, 32),
                new ItemStack(Items.DIAMOND, 12),
                new ItemStack(Items.EMERALD, 16),
                new ItemStack(Items.WHEAT_SEEDS, 32),
                new ItemStack(Items.BUCKET, 2),
                new ItemStack(Items.WATER_BUCKET));

        putItems(level, base.offset(18, 1, -28),
                new ItemStack(Items.COAL, 64),
                new ItemStack(Items.IRON_INGOT, 64),
                new ItemStack(Items.COPPER_INGOT, 64),
                new ItemStack(Items.REDSTONE, 64),
                new ItemStack(Items.REPEATER, 16),
                new ItemStack(Items.COMPARATOR, 16),
                new ItemStack(Items.REDSTONE_TORCH, 32),
                new ItemStack(Items.LEVER, 16));

        putItems(level, base.offset(-12, 8, -27),
                new ItemStack(Items.ENCHANTED_BOOK, 12),
                new ItemStack(Items.BOOK, 32),
                new ItemStack(Items.PAPER, 64),
                new ItemStack(Items.MAP, 8),
                new ItemStack(Items.COMPASS),
                new ItemStack(Items.CLOCK),
                new ItemStack(Items.ENDER_EYE, 16),
                new ItemStack(Items.ENDER_PEARL, 16));

        putItems(level, base.offset(0, 8, -27),
                new ItemStack(Items.AMETHYST_SHARD, 64),
                new ItemStack(Items.BLAZE_ROD, 32),
                new ItemStack(Items.NETHER_STAR),
                new ItemStack(Items.DRAGON_BREATH, 16),
                new ItemStack(Items.LAPIS_LAZULI, 64),
                new ItemStack(Items.REDSTONE, 64),
                new ItemStack(Items.GLOWSTONE_DUST, 64),
                new ItemStack(Items.QUARTZ, 64));

        putItems(level, base.offset(12, 8, -27),
                new ItemStack(Items.WHEAT_SEEDS, 64),
                new ItemStack(Items.CARROT, 64),
                new ItemStack(Items.POTATO, 64),
                new ItemStack(Items.BEETROOT_SEEDS, 64),
                new ItemStack(Items.MELON_SEEDS, 64),
                new ItemStack(Items.PUMPKIN_SEEDS, 64),
                new ItemStack(Items.BONE_MEAL, 64),
                new ItemStack(Items.LEAD, 16));

        putItems(level, base.offset(-10, 16, -22),
                new ItemStack(Items.ELYTRA),
                new ItemStack(Items.FIREWORK_ROCKET, 64),
                new ItemStack(Items.TOTEM_OF_UNDYING, 8),
                new ItemStack(Items.ENCHANTED_GOLDEN_APPLE, 16),
                new ItemStack(Items.NETHER_STAR, 4),
                new ItemStack(Items.BEACON, 4));

        putItems(level, base.offset(10, 16, -22),
                new ItemStack(Items.DRAGON_BREATH, 32),
                new ItemStack(Items.END_CRYSTAL, 8),
                new ItemStack(Items.EXPERIENCE_BOTTLE, 64),
                new ItemStack(Items.LAPIS_LAZULI, 64),
                new ItemStack(Items.AMETHYST_SHARD, 64),
                new ItemStack(Items.GLOWSTONE, 64));
    }

    private static void spawnStarterAnimals(ServerLevel level, BlockPos base) {
        spawnAnimalGroup(level, EntityType.COW, base.offset(29, 1, 20), 3);
        spawnAnimalGroup(level, EntityType.PIG, base.offset(45, 1, 20), 3);
        spawnAnimalGroup(level, EntityType.CHICKEN, base.offset(29, 1, 36), 5);
        spawnAnimalGroup(level, EntityType.SHEEP, base.offset(45, 1, 36), 3);
        spawnAnimalGroup(level, EntityType.HORSE, base.offset(39, 1, 52), 2);
        spawnAnimalGroup(level, EntityType.RABBIT, base.offset(61, 1, 20), 4);
        spawnAnimalGroup(level, EntityType.GOAT, base.offset(61, 1, 36), 2);
        spawnAnimalGroup(level, EntityType.LLAMA, base.offset(61, 1, 52), 2);
        spawnAnimalGroup(level, EntityType.DONKEY, base.offset(29, 1, 68), 2);
        spawnAnimalGroup(level, EntityType.MULE, base.offset(37, 1, 68), 1);
    }

    private static void spawnImportedStarterAnimals(ServerLevel level, BlockPos base) {
        spawnAnimalGroup(level, EntityType.COW, base.offset(51, 1, -38), 3);
        spawnAnimalGroup(level, EntityType.PIG, base.offset(67, 1, -38), 3);
        spawnAnimalGroup(level, EntityType.CHICKEN, base.offset(83, 1, -38), 5);
        spawnAnimalGroup(level, EntityType.SHEEP, base.offset(51, 1, -22), 3);
        spawnAnimalGroup(level, EntityType.GOAT, base.offset(67, 1, -22), 2);
        spawnAnimalGroup(level, EntityType.RABBIT, base.offset(83, 1, -22), 4);
        spawnAnimalGroup(level, EntityType.LLAMA, base.offset(51, 1, -6), 2);
        spawnAnimalGroup(level, EntityType.HORSE, base.offset(67, 1, -6), 3);
        spawnAnimalGroup(level, EntityType.DONKEY, base.offset(83, 1, -6), 2);
        spawnAnimalGroup(level, EntityType.MULE, base.offset(91, 1, -6), 1);
    }

    private static void spawnStarterAllies(ServerLevel level, BlockPos base) {
        spawnNamedCharacter(level, EntityType.ALLAY, base.offset(16, 2, -24), "Chef da Casa");
        spawnNamedCharacter(level, EntityType.ALLAY, base.offset(-18, 2, -2), "Mestre da Oficina");
        spawnNamedCharacter(level, EntityType.ALLAY, base.offset(16, 2, 5), "Bibliotecaria da Casa");
        spawnNamedCharacter(level, EntityType.IRON_GOLEM, base.offset(0, 1, 20), "Guardiao da Casa");
    }

    private static void spawnPostDragonLife(ServerLevel level, BlockPos base) {
        if (!MagicWorldWorldOptions.isCastlesEnabled()) {
            buildHouseMagicalAmbience(level, base);
            return;
        }

        spawnCastleLife(level, castleLifeCenter(base));
    }

    private static void buildMagicalEstateAmbience(ServerLevel level, BlockPos base) {
        buildHouseMagicalAmbience(level, base);
        buildPortalMagicBeacons(level, portalCenter(base));
        if (MagicWorldWorldOptions.isCastlesEnabled()) {
            buildCastleMagicalAmbience(level, castleLifeCenter(base));
        }
        level.playSound(null, base, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 1.8F, 0.9F);
    }

    private static void buildHouseMagicalAmbience(ServerLevel level, BlockPos base) {
        for (BlockPos pos : new BlockPos[] {
                base.offset(-42, 1, -34), base.offset(36, 1, -34),
                base.offset(-42, 1, 42), base.offset(36, 1, 42),
                base.offset(0, 1, -48), base.offset(0, 1, 50)
        }) {
            placeMagicSmokeBowl(level, pos);
        }

        spawnFlyingFlock(level, base.offset(-34, 9, -24), "Ave Arcana da Casa", 4);
        spawnNamedCharacter(level, EntityType.ALLAY, base.offset(12, 4, 12), "Luz Guardia da Casa");
        spawnNamedCharacter(level, EntityType.ALLAY, base.offset(-12, 4, 12), "Brilho Curador da Casa");
    }

    private static void buildCastleMagicalAmbience(ServerLevel level, BlockPos center) {
        for (BlockPos pos : new BlockPos[] {
                center.offset(-44, 1, -36), center.offset(44, 1, -36),
                center.offset(-44, 1, 36), center.offset(44, 1, 36),
                center.offset(0, 1, -48), center.offset(0, 1, 48)
        }) {
            placeMagicSmokeBowl(level, pos);
        }

        spawnFlyingFlock(level, center.offset(-28, 14, -26), "Ave Real Encantada", 5);
        spawnFlyingFlock(level, center.offset(28, 14, 26), "Sentinela Alada do Castelo", 5);
        spawnNamedCharacter(level, EntityType.ALLAY, center.offset(-18, 5, 0), "Mordomo Magico");
        spawnNamedCharacter(level, EntityType.ALLAY, center.offset(18, 5, 0), "Guia do Castelo");
        buildCastleFrontBanners(level, center);
        spawnMountedCastlePatrol(level, center);
    }

    private static void buildCastleFrontBanners(ServerLevel level, BlockPos center) {
        for (int x : new int[] {-24, -18, -12, 12, 18, 24}) {
            BlockPos pole = center.offset(x, 1, -42);
            for (int y = 0; y <= 5; y++) {
                level.setBlock(pole.above(y), Blocks.DARK_OAK_FENCE.defaultBlockState(), 3);
            }
            level.setBlock(pole.above(6), Blocks.BLUE_BANNER.defaultBlockState(), 3);
            level.setBlock(pole.offset(0, 0, -1), Blocks.AMETHYST_BLOCK.defaultBlockState(), 3);
            level.setBlock(pole.offset(0, 1, -1), Blocks.END_ROD.defaultBlockState(), 3);
        }

        for (int x = -30; x <= 30; x += 6) {
            level.setBlock(center.offset(x, 0, -40), Blocks.POLISHED_DEEPSLATE.defaultBlockState(), 3);
            level.setBlock(center.offset(x, 1, -40), Blocks.PURPLE_CARPET.defaultBlockState(), 3);
        }
    }

    private static void spawnMountedCastlePatrol(ServerLevel level, BlockPos center) {
        for (int i = 0; i < 3; i++) {
            BlockPos patrolCenter = center.offset(-8 + i * 8, 1, -38);
            var horse = EntityType.HORSE.spawn(level, patrolCenter, EntitySpawnReason.STRUCTURE);
            var guard = EntityType.SNOW_GOLEM.spawn(level, patrolCenter.above(), EntitySpawnReason.STRUCTURE);
            if (horse != null) {
                horse.setCustomName(Component.literal("Cavalo da Guarda " + (i + 1)));
                horse.setCustomNameVisible(true);
                horse.setPersistenceRequired();
            }
            if (guard != null) {
                guard.setCustomName(Component.literal("Soldado Montado " + (i + 1)));
                guard.setCustomNameVisible(true);
                guard.setPersistenceRequired();
                ALLY_ENTITY_IDS.add(guard.getUUID());
                CASTLE_PATROL_GUARDS.put(guard.getUUID(), patrolCenter.immutable());
                if (horse != null) {
                    guard.startRiding(horse);
                    CASTLE_PATROL_GUARDS.put(horse.getUUID(), patrolCenter.immutable());
                }
            }
        }
    }

    private static void buildPortalMagicBeacons(ServerLevel level, BlockPos center) {
        for (BlockPos pos : new BlockPos[] {
                center.offset(-7, 1, -7), center.offset(7, 1, -7),
                center.offset(-7, 1, 7), center.offset(7, 1, 7)
        }) {
            level.setBlock(pos.below(), Blocks.AMETHYST_BLOCK.defaultBlockState(), 3);
            level.setBlock(pos, Blocks.END_ROD.defaultBlockState(), 3);
        }
    }

    private static void buildDirectionalSigns(ServerLevel level, BlockPos base) {
        placePortalSign(level, base.offset(-6, 1, 30), 0,
                "Portal", "Premium", "siga em", "frente");
        placePortalSign(level, base.offset(6, 1, 30), 0,
                "Castelo", "Magic World", "siga alem", "do portal");
        placePortalSign(level, portalCenter(base).offset(8, 1, 10), 4,
                "Casa", "volte por", "este caminho", "");
        placePortalSign(level, portalCenter(base).offset(-8, 1, 10), 12,
                "Castelo", "a frente", "guardas", "montados");
    }

    private static void buildUndergroundRailSystem(ServerLevel level, BlockPos base) {
        if (!MagicWorldWorldOptions.isCastlesEnabled()) {
            return;
        }

        BlockPos houseStation = base.offset(10, -6, 18);
        BlockPos castleStation = castleLifeCenter(base).offset(0, -6, -42);
        PROTECTED_RAIL_TUNNELS.add(midpoint(houseStation, castleStation));
        buildRailStation(level, houseStation, Direction.NORTH, "Metro da Casa", "para o castelo");
        buildRailStation(level, castleStation, Direction.SOUTH, "Metro do Castelo", "para a casa");
        buildStairAccess(level, base.offset(10, 1, 11), houseStation, Direction.SOUTH);
        buildStairAccess(level, castleLifeCenter(base).offset(0, 1, -35), castleStation, Direction.NORTH);
        buildPoweredRailPath(level, houseStation, castleStation);
        placeRailSupplyChests(level, houseStation, Direction.EAST);
        placeRailSupplyChests(level, castleStation, Direction.WEST);
        buildRailTunnelBonus(level, midpoint(houseStation, castleStation));
    }

    private static void buildRailStation(ServerLevel level, BlockPos center, Direction signFacing, String line1, String line2) {
        for (int x = -5; x <= 5; x++) {
            for (int z = -5; z <= 5; z++) {
                level.setBlock(center.offset(x, -1, z), Blocks.POLISHED_DEEPSLATE.defaultBlockState(), 3);
                level.setBlock(center.offset(x, 4, z), Blocks.DEEPSLATE_TILES.defaultBlockState(), 3);
                for (int y = 0; y <= 3; y++) {
                    boolean wall = Math.abs(x) == 5 || Math.abs(z) == 5;
                    level.setBlock(center.offset(x, y, z), wall ? Blocks.DEEPSLATE_BRICKS.defaultBlockState() : Blocks.AIR.defaultBlockState(), 3);
                }
            }
        }

        for (BlockPos lamp : new BlockPos[] {
                center.offset(-4, 2, -4), center.offset(4, 2, -4),
                center.offset(-4, 2, 4), center.offset(4, 2, 4)
        }) {
            level.setBlock(lamp, Blocks.SEA_LANTERN.defaultBlockState(), 3);
            level.setBlock(lamp.below(), Blocks.REDSTONE_LAMP.defaultBlockState().setValue(net.minecraft.world.level.block.RedstoneLampBlock.LIT, true), 3);
        }
        level.setBlock(center.offset(0, 3, -4), Blocks.GLOWSTONE.defaultBlockState(), 3);
        level.setBlock(center.offset(0, 3, 4), Blocks.GLOWSTONE.defaultBlockState(), 3);
        level.setBlock(center.offset(-4, 3, 0), Blocks.GLOWSTONE.defaultBlockState(), 3);
        level.setBlock(center.offset(4, 3, 0), Blocks.GLOWSTONE.defaultBlockState(), 3);

        placePortalSign(level, center.relative(signFacing, 4), 0, line1, line2, "trilhos", "energizados");
    }

    private static void buildStairAccess(ServerLevel level, BlockPos top, BlockPos bottom, Direction direction) {
        int steps = Math.max(1, top.getY() - bottom.getY());
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        for (int step = 0; step <= steps; step++) {
            int y = top.getY() - step;
            BlockPos center = top.relative(direction, step);
            mutable.set(center.getX(), y, center.getZ());
            for (int dx = -1; dx <= 1; dx++) {
                for (int head = 0; head <= 4; head++) {
                    level.setBlock(mutable.offset(dx, head, 0), Blocks.AIR.defaultBlockState(), 3);
                }
            }
            level.setBlock(mutable.offset(-2, 0, 0), Blocks.DEEPSLATE_BRICKS.defaultBlockState(), 3);
            level.setBlock(mutable.offset(2, 0, 0), Blocks.DEEPSLATE_BRICKS.defaultBlockState(), 3);
            level.setBlock(mutable.offset(-2, 1, 0), Blocks.DEEPSLATE_BRICKS.defaultBlockState(), 3);
            level.setBlock(mutable.offset(2, 1, 0), Blocks.DEEPSLATE_BRICKS.defaultBlockState(), 3);
            level.setBlock(mutable.above(4), Blocks.DEEPSLATE_TILES.defaultBlockState(), 3);
            if (step % 2 == 0) {
                level.setBlock(mutable.above(3), Blocks.SEA_LANTERN.defaultBlockState(), 3);
            }
            level.setBlock(mutable.below(), Blocks.STONE_BRICK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, direction.getOpposite()), 3);
        }
    }

    private static void buildPoweredRailPath(ServerLevel level, BlockPos start, BlockPos end) {
        BlockPos cursor = start;
        int poweredIndex = 0;
        BlockPos corner = new BlockPos(start.getX(), start.getY(), end.getZ());
        while (cursor.getZ() != corner.getZ()) {
            Direction direction = cursor.getZ() < end.getZ() ? Direction.SOUTH : Direction.NORTH;
            placePoweredRailSegment(level, cursor, direction, poweredIndex++);
            cursor = cursor.relative(direction);
        }
        placeRailCorner(level, corner, start.getZ() < end.getZ() ? Direction.SOUTH : Direction.NORTH, start.getX() < end.getX() ? Direction.EAST : Direction.WEST, poweredIndex++);
        cursor = corner.relative(start.getX() < end.getX() ? Direction.EAST : Direction.WEST);
        while (cursor.getX() != end.getX()) {
            Direction direction = cursor.getX() < end.getX() ? Direction.EAST : Direction.WEST;
            placePoweredRailSegment(level, cursor, direction, poweredIndex++);
            cursor = cursor.relative(direction);
        }
        placePoweredRailSegment(level, end, start.getX() < end.getX() ? Direction.EAST : Direction.WEST, poweredIndex);
    }

    private static void placePoweredRailSegment(ServerLevel level, BlockPos pos, Direction direction, int index) {
        carveRailTunnelSection(level, pos);
        level.setBlock(pos.below(), index % 4 == 0 ? Blocks.REDSTONE_BLOCK.defaultBlockState() : Blocks.POLISHED_DEEPSLATE.defaultBlockState(), 3);
        level.setBlock(
                pos,
                Blocks.POWERED_RAIL.defaultBlockState()
                        .setValue(net.minecraft.world.level.block.PoweredRailBlock.SHAPE,
                                direction.getAxis() == Direction.Axis.X
                                        ? RailShape.EAST_WEST
                                        : RailShape.NORTH_SOUTH)
                        .setValue(net.minecraft.world.level.block.PoweredRailBlock.POWERED, true),
                3
        );
        if (index % 3 == 0) {
            level.setBlock(pos.above(3), Blocks.SEA_LANTERN.defaultBlockState(), 3);
            level.setBlock(pos.offset(direction.getAxis() == Direction.Axis.X ? 0 : 1, 1, direction.getAxis() == Direction.Axis.Z ? 0 : 1), Blocks.END_ROD.defaultBlockState(), 3);
        }
    }

    private static void placeRailCorner(ServerLevel level, BlockPos pos, Direction zDirection, Direction xDirection, int index) {
        carveRailTunnelSection(level, pos);
        level.setBlock(pos.below(), Blocks.REDSTONE_BLOCK.defaultBlockState(), 3);
        RailShape shape = switch (zDirection) {
            case SOUTH -> xDirection == Direction.EAST ? RailShape.SOUTH_EAST : RailShape.SOUTH_WEST;
            case NORTH -> xDirection == Direction.EAST ? RailShape.NORTH_EAST : RailShape.NORTH_WEST;
            default -> RailShape.SOUTH_EAST;
        };
        level.setBlock(pos, Blocks.RAIL.defaultBlockState().setValue(RailBlock.SHAPE, shape), 3);
        level.setBlock(pos.above(3), Blocks.SEA_LANTERN.defaultBlockState(), 3);
        level.setBlock(pos.above(2), Blocks.END_ROD.defaultBlockState(), 3);
    }

    private static void carveRailTunnelSection(ServerLevel level, BlockPos pos) {
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                level.setBlock(pos.offset(x, -1, z), Blocks.DEEPSLATE_TILES.defaultBlockState(), 3);
                level.setBlock(pos.offset(x, 4, z), Blocks.DEEPSLATE_BRICKS.defaultBlockState(), 3);
            }
        }
        for (int x = -2; x <= 2; x++) {
            for (int y = 0; y <= 3; y++) {
                for (int z = -2; z <= 2; z++) {
                    boolean wall = Math.abs(x) == 2 || Math.abs(z) == 2;
                    level.setBlock(pos.offset(x, y, z), wall ? Blocks.DEEPSLATE_BRICKS.defaultBlockState() : Blocks.AIR.defaultBlockState(), 3);
                }
            }
        }
        level.setBlock(pos.offset(-2, 2, 0), Blocks.SEA_LANTERN.defaultBlockState(), 3);
        level.setBlock(pos.offset(2, 2, 0), Blocks.SEA_LANTERN.defaultBlockState(), 3);
    }

    private static void placeRailSupplyChests(ServerLevel level, BlockPos station, Direction side) {
        for (int i = 0; i < 3; i++) {
            BlockPos chest = station.relative(side, 3).offset(0, 0, -2 + i * 2);
            placeChest(level, chest, side.getOpposite());
            putItems(level, chest,
                    new ItemStack(Items.MINECART, 16),
                    new ItemStack(Items.CHEST_MINECART, 8),
                    new ItemStack(Items.HOPPER_MINECART, 8),
                    new ItemStack(Items.FURNACE_MINECART, 8),
                    new ItemStack(Items.TNT_MINECART, 8),
                    new ItemStack(Items.RAIL, 64),
                    new ItemStack(Items.POWERED_RAIL, 64),
                    new ItemStack(Items.DETECTOR_RAIL, 32),
                    new ItemStack(Items.ACTIVATOR_RAIL, 32),
                    new ItemStack(Items.REDSTONE_BLOCK, 32),
                    new ItemStack(Items.REDSTONE_TORCH, 64),
                    new ItemStack(Items.LEVER, 32));
        }
    }

    private static BlockPos midpoint(BlockPos first, BlockPos second) {
        return new BlockPos((first.getX() + second.getX()) / 2, (first.getY() + second.getY()) / 2, (first.getZ() + second.getZ()) / 2);
    }

    private static void buildRailTunnelBonus(ServerLevel level, BlockPos center) {
        for (int x = -4; x <= 4; x++) {
            for (int z = -4; z <= 4; z++) {
                level.setBlock(center.offset(x, -1, z), Blocks.POLISHED_DEEPSLATE.defaultBlockState(), 3);
                level.setBlock(center.offset(x, 4, z), Blocks.DEEPSLATE_TILES.defaultBlockState(), 3);
                for (int y = 0; y <= 3; y++) {
                    boolean wall = Math.abs(x) == 4 || Math.abs(z) == 4;
                    level.setBlock(center.offset(x, y, z), wall ? Blocks.DEEPSLATE_BRICKS.defaultBlockState() : Blocks.AIR.defaultBlockState(), 3);
                }
            }
        }
        level.setBlock(center.offset(0, 0, 0), Blocks.AMETHYST_BLOCK.defaultBlockState(), 3);
        level.setBlock(center.offset(0, 1, 0), Blocks.ENCHANTING_TABLE.defaultBlockState(), 3);
        level.setBlock(center.offset(-2, 1, 0), Blocks.BOOKSHELF.defaultBlockState(), 3);
        level.setBlock(center.offset(2, 1, 0), Blocks.BOOKSHELF.defaultBlockState(), 3);
        level.setBlock(center.offset(0, 3, -3), Blocks.SEA_LANTERN.defaultBlockState(), 3);
        level.setBlock(center.offset(0, 3, 3), Blocks.SEA_LANTERN.defaultBlockState(), 3);
        level.setBlock(center.offset(-3, 3, 0), Blocks.SEA_LANTERN.defaultBlockState(), 3);
        level.setBlock(center.offset(3, 3, 0), Blocks.SEA_LANTERN.defaultBlockState(), 3);
        placePortalSign(level, center.offset(0, 1, -3), 0,
                "Galeria", "Arcana", "parada", "secreta");
        spawnNamedCharacter(level, EntityType.ALLAY, center.offset(0, 2, 2), "Guardiao da Galeria Arcana");
    }

    private static void buildEnhancedEstateLighting(ServerLevel level, BlockPos base) {
        for (BlockPos pos : new BlockPos[] {
                base.offset(-58, 0, -42), base.offset(48, 0, -42),
                base.offset(-58, 0, 50), base.offset(48, 0, 50),
                base.offset(50, 0, -40), base.offset(74, 0, -24),
                base.offset(96, 0, -8), portalCenter(base).offset(-14, 0, -12),
                portalCenter(base).offset(14, 0, -12), portalCenter(base).offset(-14, 0, 12),
                portalCenter(base).offset(14, 0, 12), castleLifeCenter(base).offset(-54, 0, -50),
                castleLifeCenter(base).offset(54, 0, -50), castleLifeCenter(base).offset(-54, 0, 50),
                castleLifeCenter(base).offset(54, 0, 50)
        }) {
            placeMagicLampPost(level, pos);
        }

        for (BlockPos pos : new BlockPos[] {
                base.offset(-8, 3, 0), base.offset(8, 3, 0),
                base.offset(0, 4, 8), castleLifeCenter(base).offset(-20, 4, -20),
                castleLifeCenter(base).offset(20, 4, -20), castleLifeCenter(base).offset(0, 5, 0),
                castleLifeCenter(base).offset(-20, 4, 20), castleLifeCenter(base).offset(20, 4, 20)
        }) {
            level.setBlock(pos, Blocks.LANTERN.defaultBlockState(), 3);
            level.setBlock(pos.above(), Blocks.TORCH.defaultBlockState(), 3);
        }
    }

    private static void placeMagicLampPost(ServerLevel level, BlockPos pos) {
        level.setBlock(pos, Blocks.AMETHYST_BLOCK.defaultBlockState(), 3);
        for (int y = 1; y <= 4; y++) {
            level.setBlock(pos.above(y), Blocks.DARK_OAK_FENCE.defaultBlockState(), 3);
        }
        level.setBlock(pos.above(5), Blocks.REDSTONE_LAMP.defaultBlockState().setValue(net.minecraft.world.level.block.RedstoneLampBlock.LIT, true), 3);
        level.setBlock(pos.above(6), Blocks.LIGHT_BLUE_STAINED_GLASS.defaultBlockState(), 3);
        level.setBlock(pos.above(7), Blocks.END_ROD.defaultBlockState(), 3);
        level.sendParticles(ParticleTypes.ENCHANT, pos.getX() + 0.5D, pos.getY() + 6.0D, pos.getZ() + 0.5D, 12, 0.3D, 0.8D, 0.3D, 0.15D);
    }

    private static void buildCastleBatChamber(ServerLevel level, BlockPos center) {
        BlockPos chamber = center.offset(28, -5, 0);
        for (int x = -6; x <= 6; x++) {
            for (int z = -6; z <= 6; z++) {
                level.setBlock(chamber.offset(x, -1, z), Blocks.DEEPSLATE_TILES.defaultBlockState(), 3);
                level.setBlock(chamber.offset(x, 5, z), Blocks.DEEPSLATE_TILES.defaultBlockState(), 3);
                for (int y = 0; y <= 4; y++) {
                    boolean wall = Math.abs(x) == 6 || Math.abs(z) == 6;
                    level.setBlock(chamber.offset(x, y, z), wall ? Blocks.TINTED_GLASS.defaultBlockState() : Blocks.AIR.defaultBlockState(), 3);
                }
            }
        }
        placePortalSign(level, chamber.offset(0, 0, -5), 0, "Camara", "dos Morcegos", "magicos", "selada");
        for (int i = 0; i < 8; i++) {
            spawnNamedCharacter(level, EntityType.BAT, chamber.offset(-3 + i % 4 * 2, 2 + i % 2, -2 + i / 4 * 3), "Morcego Arcano");
        }
    }

    private static void buildHiddenMagicWorldSurprise(ServerLevel level, BlockPos base) {
        BlockPos shrine = portalCenter(base).offset(0, -4, 18);
        for (int x = -4; x <= 4; x++) {
            for (int z = -4; z <= 4; z++) {
                level.setBlock(shrine.offset(x, -1, z), Blocks.AMETHYST_BLOCK.defaultBlockState(), 3);
                for (int y = 0; y <= 4; y++) {
                    level.setBlock(shrine.offset(x, y, z), Blocks.AIR.defaultBlockState(), 3);
                }
            }
        }
        level.setBlock(shrine, Blocks.ENCHANTING_TABLE.defaultBlockState(), 3);
        level.setBlock(shrine.above(), Blocks.BEACON.defaultBlockState(), 3);
        placeMagicWorldGearChest(level, shrine.offset(0, 0, 2));
        placePortalSign(level, shrine.offset(0, 0, -3), 0, "Santuario", "secreto", "Magic World", "boa sorte");
        MagicWorld.effects(level, shrine);
    }

    private static void placeMagicSmokeBowl(ServerLevel level, BlockPos pos) {
        level.setBlock(pos.below(), Blocks.AMETHYST_BLOCK.defaultBlockState(), 3);
        level.setBlock(pos, Blocks.CAMPFIRE.defaultBlockState(), 3);
        level.setBlock(pos.above(), Blocks.LIGHT_BLUE_STAINED_GLASS.defaultBlockState(), 3);
        level.setBlock(pos.above(2), Blocks.END_ROD.defaultBlockState(), 3);
        MagicWorld.effects(level, pos);
    }

    private static void spawnFlyingFlock(ServerLevel level, BlockPos center, String name, int count) {
        for (int i = 0; i < count; i++) {
            BlockPos pos = center.offset((i % 3) * 5, i % 2, (i / 3) * 5);
            spawnNamedCharacter(level, EntityType.PARROT, pos, name + " " + (i + 1));
        }
    }

    private static void spawnAnimalGroup(ServerLevel level, EntityType<?> type, BlockPos center, int count) {
        for (int i = 0; i < count; i++) {
            type.spawn(level, center.offset((i % 3) * 2, 0, (i / 3) * 2), EntitySpawnReason.STRUCTURE);
        }
    }

    private static void spawnPeacefulDragon(ServerLevel level, BlockPos base) {
        PeacefulDragon dragon = MagicWorld.PEACEFUL_DRAGON.get().create(level, EntitySpawnReason.STRUCTURE);
        if (dragon == null) {
            return;
        }

        dragon.setEstateCenter(base);
        dragon.setCustomName(Component.literal("Guardiao Draconico do Eter"));
        dragon.setCustomNameVisible(true);
        dragon.setPos(base.getX() + 0.5D, base.getY() + 20.0D, base.getZ() + 0.5D);
        dragon.setPersistenceRequired();
        level.addFreshEntity(dragon);
    }

    private static void buildPersonalCastle(ServerLevel level, BlockPos anchor) {
        if (!placeImportedStructure(level, IMPORTED_CASTLE_STRUCTURE, importedCastleOrigin(anchor), true)) {
            LOGGER.warn("Magic World imported castle was not placed; skipping old procedural castle.");
            return;
        }

        BlockPos center = importedCastleCenter(anchor);
        decorateImportedCastleSupport(level, center);
        buildCastleArmory(level, center);
        fillPersonalCastleChests(level, center);
        spawnNamedCharacter(level, EntityType.ALLAY, center.offset(-10, 2, -10), "Guarda do Castelo");
        spawnNamedCharacter(level, EntityType.ALLAY, center.offset(10, 2, -10), "Mestre dos Baus");
        spawnNamedCharacter(level, EntityType.IRON_GOLEM, center.offset(0, 1, -18), "Protetor do Castelo");
    }

    private static void decorateImportedCastleSupport(ServerLevel level, BlockPos center) {
        for (BlockPos pos : new BlockPos[] {
                center.offset(-34, 1, -28), center.offset(34, 1, -28),
                center.offset(-34, 1, 28), center.offset(34, 1, 28),
                center.offset(0, 1, 0), center.offset(0, 7, 0),
                center.offset(-18, 1, 18), center.offset(18, 1, 18)
        }) {
            level.setBlock(pos, Blocks.SEA_LANTERN.defaultBlockState(), 3);
            level.setBlock(pos.above(), Blocks.LANTERN.defaultBlockState(), 3);
        }
    }

    private static void decoratePersonalCastleExterior(ServerLevel level, BlockPos center) {
        for (int x = -18; x <= 18; x++) {
            if (Math.floorMod(x, 4) == 0) {
                level.setBlock(center.offset(x, 11, -18), Blocks.COBBLED_DEEPSLATE_WALL.defaultBlockState(), 3);
                level.setBlock(center.offset(x, 11, 18), Blocks.COBBLED_DEEPSLATE_WALL.defaultBlockState(), 3);
            }
        }
        for (int z = -18; z <= 18; z++) {
            if (Math.floorMod(z, 4) == 0) {
                level.setBlock(center.offset(-18, 11, z), Blocks.COBBLED_DEEPSLATE_WALL.defaultBlockState(), 3);
                level.setBlock(center.offset(18, 11, z), Blocks.COBBLED_DEEPSLATE_WALL.defaultBlockState(), 3);
            }
        }
        for (int x : new int[] {-12, -6, 6, 12}) {
            for (int y : new int[] {3, 7}) {
                level.setBlock(center.offset(x, y, -19), Blocks.IRON_BARS.defaultBlockState(), 3);
                level.setBlock(center.offset(x, y + 1, -19), Blocks.IRON_BARS.defaultBlockState(), 3);
                level.setBlock(center.offset(x, y, 19), Blocks.IRON_BARS.defaultBlockState(), 3);
                level.setBlock(center.offset(x, y + 1, 19), Blocks.IRON_BARS.defaultBlockState(), 3);
            }
        }
        for (int z : new int[] {-12, -6, 6, 12}) {
            for (int y : new int[] {3, 7}) {
                level.setBlock(center.offset(-19, y, z), Blocks.IRON_BARS.defaultBlockState(), 3);
                level.setBlock(center.offset(-19, y + 1, z), Blocks.IRON_BARS.defaultBlockState(), 3);
                level.setBlock(center.offset(19, y, z), Blocks.IRON_BARS.defaultBlockState(), 3);
                level.setBlock(center.offset(19, y + 1, z), Blocks.IRON_BARS.defaultBlockState(), 3);
            }
        }
        for (int[] tower : new int[][] {{-18, -18}, {18, -18}, {-18, 18}, {18, 18}}) {
            for (int y = 16; y <= 20; y++) {
                level.setBlock(center.offset(tower[0], y, tower[1]), Blocks.DARK_OAK_LOG.defaultBlockState(), 3);
            }
            level.setBlock(center.offset(tower[0], 21, tower[1]), Blocks.BLUE_BANNER.defaultBlockState(), 3);
        }
        for (BlockPos pos : new BlockPos[] {
                center.offset(-10, 12, -19), center.offset(10, 12, -19),
                center.offset(-10, 12, 19), center.offset(10, 12, 19),
                center.offset(-19, 12, -10), center.offset(-19, 12, 10),
                center.offset(19, 12, -10), center.offset(19, 12, 10)
        }) {
            level.setBlock(pos, Blocks.LANTERN.defaultBlockState(), 3);
        }
        for (int x = -4; x <= 4; x++) {
            level.setBlock(center.offset(x, 0, -24), Blocks.DIRT_PATH.defaultBlockState(), 3);
            level.setBlock(center.offset(x, 0, -23), Blocks.DIRT_PATH.defaultBlockState(), 3);
        }
    }

    private static void buildCastleArmory(ServerLevel level, BlockPos center) {
        BlockPos[] stands = {
                center.offset(-10, 1, 8), center.offset(-6, 1, 8), center.offset(-2, 1, 8),
                center.offset(2, 1, 8), center.offset(6, 1, 8), center.offset(10, 1, 8)
        };
        spawnArmorStand(level, stands[0], Items.LEATHER_HELMET, Items.LEATHER_CHESTPLATE, Items.LEATHER_LEGGINGS, Items.LEATHER_BOOTS, "Couro");
        spawnArmorStand(level, stands[1], Items.CHAINMAIL_HELMET, Items.CHAINMAIL_CHESTPLATE, Items.CHAINMAIL_LEGGINGS, Items.CHAINMAIL_BOOTS, "Malha");
        spawnArmorStand(level, stands[2], Items.IRON_HELMET, Items.IRON_CHESTPLATE, Items.IRON_LEGGINGS, Items.IRON_BOOTS, "Ferro");
        spawnArmorStand(level, stands[3], Items.GOLDEN_HELMET, Items.GOLDEN_CHESTPLATE, Items.GOLDEN_LEGGINGS, Items.GOLDEN_BOOTS, "Ouro");
        spawnArmorStand(level, stands[4], Items.DIAMOND_HELMET, Items.DIAMOND_CHESTPLATE, Items.DIAMOND_LEGGINGS, Items.DIAMOND_BOOTS, "Diamante");
        spawnArmorStand(level, stands[5], Items.NETHERITE_HELMET, Items.NETHERITE_CHESTPLATE, Items.NETHERITE_LEGGINGS, Items.NETHERITE_BOOTS, "Netherite");
    }

    private static void fillPersonalCastleChests(ServerLevel level, BlockPos center) {
        for (BlockPos pos : new BlockPos[] {
                center.offset(-12, 1, 14), center.offset(-8, 1, 14), center.offset(-4, 1, 14),
                center.offset(0, 1, 14), center.offset(4, 1, 14), center.offset(8, 1, 14),
                center.offset(12, 1, 14), center.offset(-12, 6, 14), center.offset(12, 6, 14)
        }) {
            placeChest(level, pos, Direction.NORTH);
        }
        putItems(level, center.offset(-12, 1, 14),
                new ItemStack(MagicWorld.DRACONIC_AETHER_HELMET.get()), new ItemStack(MagicWorld.DRACONIC_AETHER_CHESTPLATE.get()),
                new ItemStack(MagicWorld.DRACONIC_AETHER_LEGGINGS.get()), new ItemStack(MagicWorld.DRACONIC_AETHER_BOOTS.get()),
                new ItemStack(MagicWorld.VARINHA_MAGICA.get()),
                new ItemStack(Items.WOODEN_SWORD), new ItemStack(Items.STONE_SWORD), new ItemStack(Items.IRON_SWORD),
                new ItemStack(Items.GOLDEN_SWORD), new ItemStack(Items.DIAMOND_SWORD), new ItemStack(Items.NETHERITE_SWORD),
                new ItemStack(Items.BOW), new ItemStack(Items.CROSSBOW), new ItemStack(Items.ARROW, 64));
        putItems(level, center.offset(-8, 1, 14),
                new ItemStack(MagicWorld.DRACONIC_AETHER_HELMET.get()), new ItemStack(MagicWorld.DRACONIC_AETHER_CHESTPLATE.get()),
                new ItemStack(MagicWorld.DRACONIC_AETHER_LEGGINGS.get()), new ItemStack(MagicWorld.DRACONIC_AETHER_BOOTS.get()),
                new ItemStack(MagicWorld.VARINHA_MAGICA.get()),
                new ItemStack(Items.WOODEN_PICKAXE), new ItemStack(Items.STONE_PICKAXE), new ItemStack(Items.IRON_PICKAXE),
                new ItemStack(Items.GOLDEN_PICKAXE), new ItemStack(Items.DIAMOND_PICKAXE), new ItemStack(Items.NETHERITE_PICKAXE),
                new ItemStack(Items.WOODEN_AXE), new ItemStack(Items.DIAMOND_AXE), new ItemStack(Items.NETHERITE_AXE));
        putItems(level, center.offset(-4, 1, 14),
                new ItemStack(MagicWorld.DRACONIC_AETHER_HELMET.get()), new ItemStack(MagicWorld.DRACONIC_AETHER_CHESTPLATE.get()),
                new ItemStack(MagicWorld.DRACONIC_AETHER_LEGGINGS.get()), new ItemStack(MagicWorld.DRACONIC_AETHER_BOOTS.get()),
                new ItemStack(MagicWorld.VARINHA_MAGICA.get()),
                new ItemStack(Items.LEATHER_HELMET), new ItemStack(Items.LEATHER_CHESTPLATE), new ItemStack(Items.LEATHER_LEGGINGS), new ItemStack(Items.LEATHER_BOOTS),
                new ItemStack(Items.CHAINMAIL_HELMET), new ItemStack(Items.CHAINMAIL_CHESTPLATE), new ItemStack(Items.CHAINMAIL_LEGGINGS), new ItemStack(Items.CHAINMAIL_BOOTS));
        putItems(level, center.offset(0, 1, 14),
                new ItemStack(MagicWorld.DRACONIC_AETHER_HELMET.get()), new ItemStack(MagicWorld.DRACONIC_AETHER_CHESTPLATE.get()),
                new ItemStack(MagicWorld.DRACONIC_AETHER_LEGGINGS.get()), new ItemStack(MagicWorld.DRACONIC_AETHER_BOOTS.get()),
                new ItemStack(MagicWorld.VARINHA_MAGICA.get()),
                new ItemStack(Items.IRON_HELMET), new ItemStack(Items.IRON_CHESTPLATE), new ItemStack(Items.IRON_LEGGINGS), new ItemStack(Items.IRON_BOOTS),
                new ItemStack(Items.GOLDEN_HELMET), new ItemStack(Items.GOLDEN_CHESTPLATE), new ItemStack(Items.GOLDEN_LEGGINGS), new ItemStack(Items.GOLDEN_BOOTS));
        putItems(level, center.offset(4, 1, 14),
                new ItemStack(MagicWorld.DRACONIC_AETHER_HELMET.get()), new ItemStack(MagicWorld.DRACONIC_AETHER_CHESTPLATE.get()),
                new ItemStack(MagicWorld.DRACONIC_AETHER_LEGGINGS.get()), new ItemStack(MagicWorld.DRACONIC_AETHER_BOOTS.get()),
                new ItemStack(MagicWorld.VARINHA_MAGICA.get()),
                new ItemStack(Items.DIAMOND_HELMET), new ItemStack(Items.DIAMOND_CHESTPLATE), new ItemStack(Items.DIAMOND_LEGGINGS), new ItemStack(Items.DIAMOND_BOOTS),
                new ItemStack(Items.NETHERITE_HELMET), new ItemStack(Items.NETHERITE_CHESTPLATE), new ItemStack(Items.NETHERITE_LEGGINGS), new ItemStack(Items.NETHERITE_BOOTS));
        putItems(level, center.offset(8, 1, 14),
                new ItemStack(MagicWorld.DRACONIC_AETHER_HELMET.get()), new ItemStack(MagicWorld.DRACONIC_AETHER_CHESTPLATE.get()),
                new ItemStack(MagicWorld.DRACONIC_AETHER_LEGGINGS.get()), new ItemStack(MagicWorld.DRACONIC_AETHER_BOOTS.get()),
                new ItemStack(MagicWorld.VARINHA_MAGICA.get()),
                new ItemStack(Items.OAK_LOG, 64), new ItemStack(Items.SPRUCE_LOG, 64), new ItemStack(Items.STONE_BRICKS, 64),
                new ItemStack(Items.IRON_BLOCK, 32), new ItemStack(Items.GOLD_BLOCK, 16), new ItemStack(Items.DIAMOND_BLOCK, 8),
                new ItemStack(Items.CRAFTING_TABLE, 16), new ItemStack(Items.FURNACE, 16), new ItemStack(Items.CHEST, 32));
        putItems(level, center.offset(12, 1, 14),
                new ItemStack(Items.COOKED_BEEF, 64), new ItemStack(Items.COOKED_PORKCHOP, 64), new ItemStack(Items.COOKED_CHICKEN, 64),
                new ItemStack(Items.BREAD, 64), new ItemStack(Items.GOLDEN_CARROT, 64), new ItemStack(Items.GOLDEN_APPLE, 32),
                new ItemStack(Items.ENCHANTED_GOLDEN_APPLE, 8), new ItemStack(Items.CAKE, 16), new ItemStack(Items.HONEY_BOTTLE, 16));
        putItems(level, center.offset(-12, 6, 14),
                new ItemStack(MagicWorld.DRACONIC_AETHER_HELMET.get()), new ItemStack(MagicWorld.DRACONIC_AETHER_CHESTPLATE.get()),
                new ItemStack(MagicWorld.DRACONIC_AETHER_LEGGINGS.get()), new ItemStack(MagicWorld.DRACONIC_AETHER_BOOTS.get()),
                new ItemStack(MagicWorld.VARINHA_MAGICA.get()), new ItemStack(Items.TOTEM_OF_UNDYING, 16),
                new ItemStack(Items.EXPERIENCE_BOTTLE, 64), new ItemStack(Items.BEACON, 4));
        putItems(level, center.offset(12, 6, 14),
                new ItemStack(Items.WHEAT, 64), new ItemStack(Items.CARROT, 64), new ItemStack(Items.POTATO, 64),
                new ItemStack(Items.BEETROOT, 64), new ItemStack(Items.BONE_MEAL, 64), new ItemStack(Items.LEAD, 32),
                new ItemStack(Items.SADDLE, 8), new ItemStack(Items.NAME_TAG, 16));
    }

    private static void buildStarterCastle(
            ServerLevel level,
            BlockPos center,
            BlockState wallBlock,
            BlockState floorBlock,
            BlockState accent,
            BlockState banner,
            BlockState cropState,
            String name,
            String propertyName
    ) {
        for (int x = -CASTLE_TERRAIN_RADIUS; x <= CASTLE_TERRAIN_RADIUS; x++) {
            for (int z = -CASTLE_TERRAIN_RADIUS; z <= CASTLE_TERRAIN_RADIUS; z++) {
                level.setBlock(center.offset(x, -1, z), Blocks.GRASS_BLOCK.defaultBlockState(), 3);
                level.setBlock(center.offset(x, 0, z), Math.abs(x) <= 24 && Math.abs(z) <= 24
                        ? floorBlock
                        : Blocks.GRASS_BLOCK.defaultBlockState(), 3);
                for (int y = 1; y <= 34; y++) {
                    level.setBlock(center.offset(x, y, z), Blocks.AIR.defaultBlockState(), 3);
                }
            }
        }

        for (int x = -CASTLE_TERRAIN_RADIUS; x <= CASTLE_TERRAIN_RADIUS; x++) {
            for (int z = -CASTLE_TERRAIN_RADIUS; z <= CASTLE_TERRAIN_RADIUS; z++) {
                boolean outerWall = Math.abs(x) == CASTLE_TERRAIN_RADIUS || Math.abs(z) == CASTLE_TERRAIN_RADIUS;
                if (outerWall) {
                    level.setBlock(center.offset(x, 0, z), Blocks.COBBLED_DEEPSLATE_WALL.defaultBlockState(), 3);
                    if ((x + z) % 7 == 0) {
                        level.setBlock(center.offset(x, 1, z), Blocks.LANTERN.defaultBlockState(), 3);
                    }
                }
            }
        }
        level.setBlock(center.offset(0, 0, -CASTLE_TERRAIN_RADIUS), Blocks.OAK_FENCE_GATE.defaultBlockState()
                .setValue(FenceGateBlock.FACING, Direction.NORTH), 3);

        for (int x = -30; x <= 30; x++) {
            for (int z = -30; z <= 30; z++) {
                boolean wall = Math.abs(x) == 30 || Math.abs(z) == 30;
                if (wall) {
                    for (int y = 1; y <= 22; y++) {
                        level.setBlock(center.offset(x, y, z), wallBlock, 3);
                    }
                    if ((x + z) % 2 == 0) {
                        level.setBlock(center.offset(x, 23, z), accent, 3);
                    }
                }
            }
        }

        for (int floorY : new int[] {4, 8, 12, 16, 20}) {
            for (int x = -28; x <= 28; x++) {
                for (int z = -28; z <= 28; z++) {
                    if (Math.abs(x) < 26 && Math.abs(z) < 26) {
                        level.setBlock(center.offset(x, floorY, z), floorBlock, 3);
                    }
                }
            }
            for (int x = -4; x <= 4; x++) {
                for (int z = -30; z <= -26; z++) {
                    level.setBlock(center.offset(x, floorY, z), Blocks.AIR.defaultBlockState(), 3);
                }
            }
        }

        int[][] towers = {{-30, -30}, {30, -30}, {-30, 30}, {30, 30}};
        for (int[] tower : towers) {
            for (int x = tower[0] - 4; x <= tower[0] + 4; x++) {
                for (int z = tower[1] - 4; z <= tower[1] + 4; z++) {
                    for (int y = 1; y <= 30; y++) {
                        level.setBlock(center.offset(x, y, z), y >= 27 ? accent : wallBlock, 3);
                    }
                }
            }
            level.setBlock(center.offset(tower[0], 31, tower[1]), banner, 3);
            level.setBlock(center.offset(tower[0], 26, tower[1]), Blocks.SEA_LANTERN.defaultBlockState(), 3);
        }

        for (int z = -30; z <= -25; z++) {
            for (int y = 1; y <= 8; y++) {
                for (int x = -5; x <= 5; x++) {
                    level.setBlock(center.offset(x, y, z), Blocks.AIR.defaultBlockState(), 3);
                }
            }
        }

        for (int x = -14; x <= 14; x++) {
            for (int z = -14; z <= 14; z++) {
                level.setBlock(center.offset(x, 0, z), floorBlock, 3);
            }
        }
        level.setBlock(center.offset(0, 1, 0), Blocks.ENCHANTING_TABLE.defaultBlockState(), 3);
        level.setBlock(center.offset(-2, 1, 0), Blocks.BEACON.defaultBlockState(), 3);
        level.setBlock(center.offset(2, 1, 0), Blocks.END_PORTAL_FRAME.defaultBlockState(), 3);

        for (int x = -52; x <= -34; x++) {
            for (int z = 18; z <= 46; z++) {
                boolean water = x == -43 || z == 32;
                level.setBlock(center.offset(x, 0, z), water ? Blocks.WATER.defaultBlockState() : Blocks.FARMLAND.defaultBlockState(), 3);
                if (!water) {
                    level.setBlock(center.offset(x, 1, z), cropState, 3);
                }
            }
        }

        for (int x = 36; x <= 54; x++) {
            for (int z = 16; z <= 40; z++) {
                boolean edge = x == 36 || x == 54 || z == 16 || z == 40;
                level.setBlock(center.offset(x, 0, z), edge ? Blocks.OAK_FENCE.defaultBlockState() : Blocks.GRASS_BLOCK.defaultBlockState(), 3);
            }
        }

        for (int x = -10; x <= 10; x++) {
            for (int z = -7; z <= 7; z++) {
                boolean edge = Math.abs(x) == 10 || Math.abs(z) == 7;
                level.setBlock(center.offset(x, 0, z + 44), edge ? wallBlock : Blocks.WATER.defaultBlockState(), 3);
            }
        }

        placeChest(level, center.offset(-6, 1, 8), Direction.SOUTH);
        placeChest(level, center.offset(0, 1, 4), Direction.SOUTH);
        placeChest(level, center.offset(6, 1, 8), Direction.SOUTH);
        fillCastleChests(level, center);

        for (BlockPos pos : new BlockPos[] {
                center.offset(-22, 1, -22), center.offset(22, 1, -22),
                center.offset(-22, 1, 22), center.offset(22, 1, 22),
                center.offset(0, 1, 0), center.offset(0, 1, 44),
                center.offset(-50, 1, 18), center.offset(50, 1, 18)
        }) {
            level.setBlock(pos, Blocks.SEA_LANTERN.defaultBlockState(), 3);
        }

        placePortalSign(level, center.offset(0, 1, -22), 8,
                name, propertyName, "Magia viva", "Banners ativos");
        placeDraconicArmorChest(level, center.offset(8, 0, -24));
        placePortalSign(level, center.offset(11, 1, -24), 12,
                "Armadura", "Magic World", "disponivel", "no bau");

        buildCastleInterior(level, center, wallBlock, floorBlock, accent, banner);
        decorateCastleFlagsAndFires(level, center, banner);
        buildCastleMiningBasement(level, center, accent);
        decorateCastleExterior(level, center, wallBlock, accent, cropState);
    }

    private static void decorateCastleFlagsAndFires(ServerLevel level, BlockPos center, BlockState banner) {
        for (BlockPos pos : new BlockPos[] {
                center.offset(-30, 32, -30), center.offset(30, 32, -30),
                center.offset(-30, 32, 30), center.offset(30, 32, 30),
                center.offset(0, 26, -31), center.offset(-12, 26, -31), center.offset(12, 26, -31)
        }) {
            level.setBlock(pos, banner, 3);
            level.setBlock(pos.below(), Blocks.IRON_BARS.defaultBlockState(), 3);
        }

        for (BlockPos pos : new BlockPos[] {
                center.offset(-8, 1, -36), center.offset(8, 1, -36),
                center.offset(-18, 1, -34), center.offset(18, 1, -34),
                center.offset(-28, 1, -28), center.offset(28, 1, -28)
        }) {
            level.setBlock(pos, Blocks.CAMPFIRE.defaultBlockState(), 3);
            level.setBlock(pos.below(), Blocks.POLISHED_BLACKSTONE.defaultBlockState(), 3);
        }
    }

    private static void buildCastleMiningBasement(ServerLevel level, BlockPos center, BlockState accent) {
        for (int x = -24; x <= 24; x++) {
            for (int z = -24; z <= 24; z++) {
                level.setBlock(center.offset(x, -8, z), Blocks.DEEPSLATE_TILES.defaultBlockState(), 3);
                level.setBlock(center.offset(x, -2, z), Blocks.OAK_PLANKS.defaultBlockState(), 3);
                for (int y = -7; y <= -3; y++) {
                    boolean wall = Math.abs(x) == 24 || Math.abs(z) == 24;
                    level.setBlock(center.offset(x, y, z), wall ? Blocks.DEEPSLATE_BRICKS.defaultBlockState() : Blocks.AIR.defaultBlockState(), 3);
                }
            }
        }

        for (int x = -18; x <= 18; x++) {
            placeBasementRail(level, center.offset(x, -7, 0), x);
        }
        for (int z = -18; z <= 18; z++) {
            placeBasementRail(level, center.offset(18, -7, z), z);
            placeBasementRail(level, center.offset(-18, -7, z), z);
        }
        for (int x = -18; x <= 18; x++) {
            placeBasementRail(level, center.offset(x, -7, 18), x);
        }
        EntityType.MINECART.spawn(level, center.offset(0, -7, 0), EntitySpawnReason.STRUCTURE);

        for (BlockPos pos : new BlockPos[] {
                center.offset(-20, -6, -20), center.offset(20, -6, -20),
                center.offset(-20, -6, 20), center.offset(20, -6, 20),
                center.offset(0, -6, 0), center.offset(0, -6, -20), center.offset(0, -6, 20)
        }) {
            level.setBlock(pos, Blocks.SEA_LANTERN.defaultBlockState(), 3);
            level.setBlock(pos.above(), Blocks.LANTERN.defaultBlockState(), 3);
        }

        for (BlockPos pos : new BlockPos[] {
                center.offset(-14, -7, -14), center.offset(-12, -7, -14), center.offset(-10, -7, -14),
                center.offset(10, -7, -14), center.offset(12, -7, -14), center.offset(14, -7, -14),
                center.offset(-14, -7, 14), center.offset(-12, -7, 14), center.offset(-10, -7, 14),
                center.offset(10, -7, 14), center.offset(12, -7, 14), center.offset(14, -7, 14)
        }) {
            level.setBlock(pos, Blocks.OAK_LOG.defaultBlockState(), 3);
        }

        level.setBlock(center.offset(-8, -7, -18), Blocks.DIAMOND_BLOCK.defaultBlockState(), 3);
        level.setBlock(center.offset(-6, -7, -18), Blocks.EMERALD_BLOCK.defaultBlockState(), 3);
        level.setBlock(center.offset(-4, -7, -18), Blocks.GOLD_BLOCK.defaultBlockState(), 3);
        level.setBlock(center.offset(-2, -7, -18), Blocks.IRON_BLOCK.defaultBlockState(), 3);
        level.setBlock(center.offset(0, -7, -18), Blocks.LAPIS_BLOCK.defaultBlockState(), 3);
        level.setBlock(center.offset(2, -7, -18), Blocks.REDSTONE_BLOCK.defaultBlockState(), 3);
        level.setBlock(center.offset(4, -7, -18), Blocks.COAL_BLOCK.defaultBlockState(), 3);
        level.setBlock(center.offset(6, -7, -18), accent, 3);

        placeChest(level, center.offset(-6, -6, 20), Direction.NORTH);
        placeChest(level, center.offset(0, -6, 20), Direction.NORTH);
        placeChest(level, center.offset(6, -6, 20), Direction.NORTH);
        putItems(level, center.offset(-6, -6, 20),
                new ItemStack(Items.NETHERITE_PICKAXE),
                new ItemStack(Items.DIAMOND_PICKAXE),
                new ItemStack(Items.IRON_PICKAXE),
                new ItemStack(Items.TORCH, 64),
                new ItemStack(Items.LANTERN, 64),
                new ItemStack(Items.WATER_BUCKET));
        putItems(level, center.offset(0, -6, 20),
                new ItemStack(Items.DIAMOND, 64),
                new ItemStack(Items.EMERALD, 64),
                new ItemStack(Items.GOLD_INGOT, 64),
                new ItemStack(Items.IRON_INGOT, 64),
                new ItemStack(Items.LAPIS_LAZULI, 64),
                new ItemStack(Items.REDSTONE, 64));
        putItems(level, center.offset(6, -6, 20),
                new ItemStack(Items.OAK_LOG, 64),
                new ItemStack(Items.SPRUCE_LOG, 64),
                new ItemStack(Items.DARK_OAK_LOG, 64),
                new ItemStack(Items.RAIL, 64),
                new ItemStack(Items.POWERED_RAIL, 32),
                new ItemStack(Items.MINECART, 4));

        for (int step = 0; step <= 8; step++) {
            level.setBlock(center.offset(23, -7 + step, -10 + step), Blocks.STONE_BRICK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.SOUTH), 3);
            level.setBlock(center.offset(22, -7 + step, -10 + step), Blocks.STONE_BRICK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.SOUTH), 3);
        }
    }

    private static void buildCastleInterior(
            ServerLevel level,
            BlockPos center,
            BlockState wallBlock,
            BlockState floorBlock,
            BlockState accent,
            BlockState banner
    ) {
        for (int y = 1; y <= 20; y++) {
            for (int x = -26; x <= 26; x++) {
                if (Math.abs(x) <= 4 && y <= 5) {
                    level.setBlock(center.offset(x, y, -30), Blocks.AIR.defaultBlockState(), 3);
                }
            }
        }

        for (int y = 1; y <= 19; y++) {
            for (int x = -24; x <= 24; x++) {
                if ((x == -8 || x == 8) && y <= 18) {
                    for (int z = -24; z <= 24; z++) {
                        if ((z >= -4 && z <= 4) || (y <= 3 && (z == -18 || z == 18))) {
                            level.setBlock(center.offset(x, y, z), Blocks.AIR.defaultBlockState(), 3);
                        } else {
                            level.setBlock(center.offset(x, y, z), wallBlock, 3);
                        }
                    }
                }
            }
            for (int z = -24; z <= 24; z++) {
                if ((z == -8 || z == 8) && y <= 18) {
                    for (int x = -24; x <= 24; x++) {
                        if ((x >= -4 && x <= 4) || (y <= 3 && (x == -18 || x == 18))) {
                            level.setBlock(center.offset(x, y, z), Blocks.AIR.defaultBlockState(), 3);
                        } else {
                            level.setBlock(center.offset(x, y, z), wallBlock, 3);
                        }
                    }
                }
            }
        }

        for (int floorY : new int[] {1, 5, 9, 13, 17}) {
            for (BlockPos pos : new BlockPos[] {
                    center.offset(-18, floorY, -18), center.offset(18, floorY, -18),
                    center.offset(-18, floorY, 18), center.offset(18, floorY, 18),
                    center.offset(0, floorY, 0), center.offset(0, floorY, -18), center.offset(0, floorY, 18)
            }) {
                level.setBlock(pos, Blocks.SEA_LANTERN.defaultBlockState(), 3);
                level.setBlock(pos.above(), Blocks.LANTERN.defaultBlockState(), 3);
            }
        }

        placeHouseDoor(level, center.offset(-1, 1, -30), Direction.NORTH);
        placeHouseDoor(level, center.offset(0, 1, -30), Direction.NORTH);
        placeHouseDoor(level, center.offset(1, 1, -30), Direction.NORTH);

        for (int x : new int[] {-18, -10, 10, 18}) {
            for (int y : new int[] {3, 7, 11, 15, 19}) {
                placeWindow(level, center.offset(x, y, -30));
                placeWindow(level, center.offset(x, y, 30));
                placeWindow(level, center.offset(-30, y, x));
                placeWindow(level, center.offset(30, y, x));
            }
        }

        for (int step = 0; step <= 20; step++) {
            int y = 1 + step;
            int z = -24 + step;
            level.setBlock(center.offset(-24, y, z), Blocks.STONE_BRICK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.SOUTH), 3);
            level.setBlock(center.offset(-23, y, z), Blocks.STONE_BRICK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.SOUTH), 3);
        }

        buildCastleKitchen(level, center.offset(14, 1, -18));
        buildCastleBedrooms(level, center);
        buildCastleLibrary(level, center.offset(14, 1, 10));
        buildCastleSpecialHall(level, center, accent, banner);
        buildCastleRoof(level, center, wallBlock, floorBlock, accent);
    }

    private static void buildCastleKitchen(ServerLevel level, BlockPos base) {
        level.setBlock(base, Blocks.SMOKER.defaultBlockState(), 3);
        level.setBlock(base.offset(2, 0, 0), Blocks.FURNACE.defaultBlockState(), 3);
        level.setBlock(base.offset(4, 0, 0), Blocks.CAULDRON.defaultBlockState(), 3);
        level.setBlock(base.offset(6, 0, 0), Blocks.BARREL.defaultBlockState(), 3);
        level.setBlock(base.offset(8, 0, 0), Blocks.CRAFTING_TABLE.defaultBlockState(), 3);
        level.setBlock(base.offset(4, 1, -2), Blocks.CAMPFIRE.defaultBlockState(), 3);
        for (int y = 1; y <= 8; y++) {
            level.setBlock(base.offset(4, y, -3), Blocks.BRICKS.defaultBlockState(), 3);
        }
    }

    private static void buildCastleBedrooms(ServerLevel level, BlockPos center) {
        for (int i = 0; i < 6; i++) {
            int x = -21 + (i % 3) * 7;
            int z = 12 + (i / 3) * 7;
            placeBed(level, center.offset(x, 1, z), Direction.SOUTH);
            placeChest(level, center.offset(x + 3, 1, z), Direction.SOUTH);
            level.setBlock(center.offset(x, 1, z + 3), Blocks.BLUE_CARPET.defaultBlockState(), 3);
        }
    }

    private static void buildCastleLibrary(ServerLevel level, BlockPos base) {
        for (int y = 0; y <= 4; y++) {
            for (int z = -6; z <= 6; z++) {
                level.setBlock(base.offset(0, y, z), Blocks.BOOKSHELF.defaultBlockState(), 3);
                level.setBlock(base.offset(8, y, z), Blocks.BOOKSHELF.defaultBlockState(), 3);
            }
        }
        level.setBlock(base.offset(4, 0, 0), Blocks.ENCHANTING_TABLE.defaultBlockState(), 3);
        level.setBlock(base.offset(4, 0, 3), Blocks.LECTERN.defaultBlockState(), 3);
    }

    private static void buildCastleSpecialHall(ServerLevel level, BlockPos center, BlockState accent, BlockState banner) {
        for (int x = -6; x <= 6; x++) {
            for (int z = -6; z <= 6; z++) {
                level.setBlock(center.offset(x, 1, z), Blocks.PURPLE_CARPET.defaultBlockState(), 3);
            }
        }
        level.setBlock(center.offset(0, 1, 0), Blocks.ENCHANTING_TABLE.defaultBlockState(), 3);
        level.setBlock(center.offset(0, 2, 0), Blocks.BEACON.defaultBlockState(), 3);
        level.setBlock(center.offset(-4, 1, 0), accent, 3);
        level.setBlock(center.offset(4, 1, 0), Blocks.AMETHYST_BLOCK.defaultBlockState(), 3);
        level.setBlock(center.offset(0, 1, -4), Blocks.BREWING_STAND.defaultBlockState(), 3);
        level.setBlock(center.offset(0, 2, -6), banner, 3);
        level.setBlock(center.offset(0, 1, 6), Blocks.END_PORTAL_FRAME.defaultBlockState(), 3);
    }

    private static void buildCastleRoof(ServerLevel level, BlockPos center, BlockState wallBlock, BlockState floorBlock, BlockState accent) {
        for (int x = -32; x <= 32; x++) {
            for (int z = -32; z <= 32; z++) {
                boolean edge = Math.abs(x) == 32 || Math.abs(z) == 32;
                level.setBlock(center.offset(x, 24, z), edge ? accent : floorBlock, 3);
                if (edge && (x + z) % 4 == 0) {
                    level.setBlock(center.offset(x, 25, z), wallBlock, 3);
                }
            }
        }
        for (int y = 25; y <= 33; y++) {
            level.setBlock(center.offset(8, y, -22), Blocks.BRICKS.defaultBlockState(), 3);
        }
        level.setBlock(center.offset(8, 34, -22), Blocks.CAMPFIRE.defaultBlockState(), 3);
    }

    private static void decorateCastleExterior(
            ServerLevel level,
            BlockPos center,
            BlockState wallBlock,
            BlockState accent,
            BlockState cropState
    ) {
        for (int x = -56; x <= 56; x += 8) {
            level.setBlock(center.offset(x, 1, -56), flowerFor(x), 3);
            level.setBlock(center.offset(x, 1, 56), flowerFor(x + 1), 3);
        }
        for (int z = -56; z <= 56; z += 8) {
            level.setBlock(center.offset(-56, 1, z), flowerFor(z + 2), 3);
            level.setBlock(center.offset(56, 1, z), flowerFor(z + 3), 3);
        }
        for (int z = -24; z <= 24; z += 6) {
            level.setBlock(center.offset(-31, 1, z), Blocks.VINE.defaultBlockState(), 3);
            level.setBlock(center.offset(31, 1, z), Blocks.VINE.defaultBlockState(), 3);
        }
        buildCropField(level, center.offset(-54, 0, -54), cropState);
        buildCropField(level, center.offset(-30, 0, -54), Blocks.WHEAT.defaultBlockState().setValue(CropBlock.AGE, 7));
        buildAnimalPen(level, center.offset(34, 0, -54), 18, 16, true);
        buildAnimalPen(level, center.offset(34, 0, -30), 18, 16, true);
        for (BlockPos pos : new BlockPos[] {
                center.offset(-58, 0, -58), center.offset(58, 0, -58),
                center.offset(-58, 0, 58), center.offset(58, 0, 58),
                center.offset(0, 0, -58), center.offset(0, 0, 58)
        }) {
            placeLampPost(level, pos);
        }
        level.setBlock(center.offset(-48, 1, 42), Blocks.SMITHING_TABLE.defaultBlockState(), 3);
        level.setBlock(center.offset(-46, 1, 42), Blocks.ANVIL.defaultBlockState(), 3);
        level.setBlock(center.offset(-44, 1, 42), wallBlock, 3);
        level.setBlock(center.offset(-42, 1, 42), accent, 3);
    }

    private static void fillCastleChests(ServerLevel level, BlockPos center) {
        putItems(level, center.offset(-6, 1, 8),
                new ItemStack(Items.NETHERITE_SWORD),
                new ItemStack(Items.NETHERITE_AXE),
                new ItemStack(Items.NETHERITE_PICKAXE),
                new ItemStack(Items.ENCHANTED_GOLDEN_APPLE, 4),
                new ItemStack(Items.TOTEM_OF_UNDYING, 3),
                new ItemStack(Items.ENDER_PEARL, 16));
        putItems(level, center.offset(0, 1, 4),
                new ItemStack(Items.DIAMOND, 32),
                new ItemStack(Items.EMERALD, 64),
                new ItemStack(Items.GOLD_INGOT, 64),
                new ItemStack(Items.NETHERITE_INGOT, 8),
                new ItemStack(Items.EXPERIENCE_BOTTLE, 64),
                new ItemStack(Items.BLAZE_ROD, 16));
        putItems(level, center.offset(6, 1, 8),
                new ItemStack(Items.COOKED_BEEF, 64),
                new ItemStack(Items.GOLDEN_CARROT, 64),
                new ItemStack(Items.TORCH, 64),
                new ItemStack(Items.OAK_LOG, 64),
                new ItemStack(Items.STONE_BRICKS, 64),
                new ItemStack(Items.BUCKET, 4));
    }

    private static void spawnCastleLife(ServerLevel level, BlockPos center) {
        EntityType.IRON_GOLEM.spawn(level, center.offset(0, 1, 0), EntitySpawnReason.STRUCTURE);
        EntityType.HORSE.spawn(level, center.offset(27, 1, 13), EntitySpawnReason.STRUCTURE);
        EntityType.COW.spawn(level, center.offset(25, 1, 18), EntitySpawnReason.STRUCTURE);
        EntityType.SHEEP.spawn(level, center.offset(31, 1, 11), EntitySpawnReason.STRUCTURE);

        spawnCastleCharacters(level, center);
    }

    private static void spawnCastleCharacters(ServerLevel level, BlockPos center) {
        spawnNamedCharacter(level, EntityType.ALLAY, center.offset(-14, 2, -18), "Ferreiro Real");
        spawnNamedCharacter(level, EntityType.ALLAY, center.offset(14, 2, -18), "Cozinheiro Real");
        spawnNamedCharacter(level, EntityType.ALLAY, center.offset(18, 2, -18), "Bibliotecaria Arcana");
        spawnNamedCharacter(level, EntityType.IRON_GOLEM, center.offset(-6, 1, -24), "Guardiao da Entrada");
        spawnNamedCharacter(level, EntityType.SNOW_GOLEM, center.offset(-48, 1, -48), "Agricultor do Castelo");
        spawnNamedCharacter(level, EntityType.SNOW_GOLEM, center.offset(42, 1, -48), "Tratador do Curral");
        spawnNamedCharacter(level, EntityType.ALLAY, center.offset(-52, 2, 42), "Ferreiro Externo");
    }

    private static void spawnNamedCharacter(ServerLevel level, EntityType<?> type, BlockPos pos, String name) {
        var entity = type.spawn(level, pos, EntitySpawnReason.STRUCTURE);
        if (entity == null) {
            return;
        }

        entity.setCustomName(Component.literal(name));
        entity.setCustomNameVisible(true);
        ALLY_ENTITY_IDS.add(entity.getUUID());
        if (entity instanceof Mob mob) {
            mob.setPersistenceRequired();
        }
    }

    private static void placeBed(ServerLevel level, BlockPos foot, Direction facing) {
        level.setBlock(
                foot,
                Blocks.BLUE_BED.defaultBlockState()
                        .setValue(BedBlock.FACING, facing)
                        .setValue(BedBlock.PART, BedPart.FOOT),
                3
        );
        level.setBlock(
                foot.relative(facing),
                Blocks.BLUE_BED.defaultBlockState()
                        .setValue(BedBlock.FACING, facing)
                        .setValue(BedBlock.PART, BedPart.HEAD),
                3
        );
    }

    private static void placeChest(ServerLevel level, BlockPos pos, Direction facing) {
        level.setBlock(
                pos,
                Blocks.CHEST.defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, facing),
                3
        );
    }

    private static void putItems(ServerLevel level, BlockPos pos, ItemStack... items) {
        if (pos == null) {
            return;
        }

        if (level.getBlockEntity(pos) instanceof Container container) {
            if (items.length == 0) {
                return;
            }

            for (int i = 0; i < container.getContainerSize(); i++) {
                ItemStack stack = items[i % items.length].copy();
                if (stack.getMaxStackSize() > 1) {
                    stack.setCount(stack.getMaxStackSize());
                }
                container.setItem(i, stack);
            }
            container.setChanged();
        }
    }

    private static BlockPos placeSafeChest(ServerLevel level, BlockPos preferred, Direction facing) {
        BlockPos safePos = findSafeInteriorFloor(level, preferred, 28, 8);
        if (safePos != null) {
            placeChest(level, safePos, facing);
        }
        return safePos;
    }

    private static void placeSafeBed(ServerLevel level, BlockPos preferred, Direction facing) {
        BlockPos safePos = findSafeInteriorFloor(level, preferred, 28, 8);
        if (safePos == null || !level.getBlockState(safePos.relative(facing)).isAir()) {
            return;
        }
        placeBed(level, safePos, facing);
    }

    private static void cleanupOutdoorImportedHouseFurniture(ServerLevel level, BlockPos base) {
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        for (int x = -36; x <= 36; x++) {
            for (int y = -2; y <= 8; y++) {
                for (int z = -24; z <= 30; z++) {
                    mutable.set(base.getX() + x, base.getY() + y, base.getZ() + z);
                    BlockState state = level.getBlockState(mutable);
                    if ((state.is(Blocks.CHEST) || state.getBlock() instanceof BedBlock)
                            && !hasSolidCeilingAbove(level, mutable, 14)) {
                        level.removeBlock(mutable, false);
                    }
                }
            }
        }
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

    private static boolean hasSolidCeilingAbove(ServerLevel level, BlockPos pos, int maxHeight) {
        for (int y = 2; y <= maxHeight; y++) {
            if (level.getBlockState(pos.above(y)).isSolid()) {
                return true;
            }
        }
        return false;
    }

    private static BlockState stair(Direction facing) {
        return Blocks.POLISHED_DEEPSLATE_STAIRS.defaultBlockState()
                .setValue(StairBlock.FACING, facing);
    }

    private static void placePortalSign(
            ServerLevel level,
            BlockPos pos,
            int rotation,
            String line1,
            String line2,
            String line3,
            String line4
    ) {
        level.setBlock(
                pos,
                Blocks.OAK_SIGN.defaultBlockState()
                        .setValue(StandingSignBlock.ROTATION, rotation),
                3
        );

        if (level.getBlockEntity(pos) instanceof SignBlockEntity sign) {
            SignText text = new SignText()
                    .setMessage(0, Component.literal(line1))
                    .setMessage(1, Component.literal(line2))
                    .setMessage(2, Component.literal(line3))
                    .setMessage(3, Component.literal(line4))
                    .setHasGlowingText(true);
            sign.setText(text, true);
        }
    }

    private static boolean shouldUseStarterPortal(Player player) {
        return Config.starterPortalEnabled
                && MagicWorldWorldOptions.isStarterEstateEnabled()
                && Config.visualExperienceStartMode.equals("locked_until_portal")
                && !player.getPersistentData().getBoolean(PLAYER_ESTATE_CREATED_KEY).orElse(false);
    }

    private static boolean isStarterPortalMarker(Level level, BlockPos pos) {
        if (!Config.starterPortalEnabled || Config.visualExperienceStartMode.equals("no_portal")) {
            return false;
        }

        BlockState state = level.getBlockState(pos);
        return state.is(Blocks.CRYING_OBSIDIAN)
                || state.is(Blocks.END_PORTAL_FRAME)
                || state.is(Blocks.PURPLE_STAINED_GLASS)
                || state.is(Blocks.MAGENTA_STAINED_GLASS)
                || state.is(Blocks.AMETHYST_BLOCK)
                || state.is(Blocks.END_GATEWAY);
    }
}
