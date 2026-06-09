package com.magicworldlight;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.StandingAndWallBlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LightBlock;
import net.minecraft.world.level.block.TorchBlock;
import net.minecraft.world.level.block.WallTorchBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerDestroyItemEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Mod(MagicWorldLight.MODID)
public class MagicWorldLight {
    public static final String MODID = "magicworldlight";

    private static final int LIGHT_LEVEL = 15;
    private static final int REFRESH_TICKS = 4;
    private static final int DYNAMIC_LIGHT_RADIUS = 8;
    private static final int DYNAMIC_LIGHT_STEP = 4;
    private static final int TORCH_FIELD_RADIUS = 12;
    private static final int TORCH_FIELD_STEP = 6;
    private static final int DARK_SKY_LIGHT = 7;
    private static final long NIGHT_START = 12500L;
    private static final long NIGHT_END = 23500L;
    private static final Map<UUID, Set<PlacedLight>> PLAYER_LIGHTS = new HashMap<>();

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);

    public static final RegistryObject<Block> MAGIC_WORLD_LIGHT_BLOCK = BLOCKS.register(
            "magic_world_light",
            () -> new PremiumTorchBlock(BlockBehaviour.Properties.copy(Blocks.TORCH).lightLevel(state -> LIGHT_LEVEL))
    );

    public static final RegistryObject<Block> MAGIC_WORLD_LIGHT_WALL_BLOCK = BLOCKS.register(
            "magic_world_light_wall",
            () -> new PremiumWallTorchBlock(BlockBehaviour.Properties.copy(Blocks.WALL_TORCH).lightLevel(state -> LIGHT_LEVEL))
    );

    public static final RegistryObject<Item> MAGIC_WORLD_LIGHT = ITEMS.register(
            "magic_world_light",
            () -> new PremiumTorchItem(
                    MAGIC_WORLD_LIGHT_BLOCK.get(),
                    MAGIC_WORLD_LIGHT_WALL_BLOCK.get(),
                    new Item.Properties()
                    .stacksTo(1)
                    .rarity(Rarity.EPIC)
                    .fireResistant(),
                    Direction.DOWN)
    );

    public MagicWorldLight() {
        var modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        modEventBus.addListener(this::addCreative);
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES
                || event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
            event.accept(MAGIC_WORLD_LIGHT);
        }
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END
                || !(event.player instanceof ServerPlayer player)
                || player.level().isClientSide()
                || player.tickCount % REFRESH_TICKS != 0) {
            return;
        }

        ensurePermanentLightItem(player);
        if (hasLightItem(player) || shouldAutoLight(player)) {
            refreshPlayerLights(player);
        } else {
            clearPlayerLights(player);
        }
    }

    @SubscribeEvent
    public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            ensurePermanentLightItem(player);
        }
    }

    @SubscribeEvent
    public void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            clearPlayerLights(player);
        }
    }

    @SubscribeEvent
    public void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            clearPlayerLights(player);
        }
    }

    @SubscribeEvent
    public void onPlayerDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            clearPlayerLights(player);
        }
    }

    @SubscribeEvent
    public void onItemToss(ItemTossEvent event) {
        if (isLightItem(event.getEntity().getItem())) {
            event.setCanceled(true);
            if (event.getPlayer() instanceof ServerPlayer player) {
                ensurePermanentLightItem(player);
            }
        }
    }

    @SubscribeEvent
    public void onPlayerDestroyItem(PlayerDestroyItemEvent event) {
        if (!isLightItem(event.getOriginal()) || !(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        InteractionHand hand = event.getHand();
        if (hand != null && player.getItemInHand(hand).isEmpty()) {
            player.setItemInHand(hand, premiumLightStack());
        } else {
            ensurePermanentLightItem(player);
        }
    }

    @SubscribeEvent
    public void onServerStopping(ServerStoppingEvent event) {
        clearAllLights(event.getServer());
    }

    private static void refreshPlayerLights(ServerPlayer player) {
        MinecraftServer server = player.getServer();
        if (server == null) {
            return;
        }

        UUID playerId = player.getUUID();
        Set<PlacedLight> current = PLAYER_LIGHTS.getOrDefault(playerId, Set.of());
        Set<PlacedLight> desired = desiredLights(player);

        for (PlacedLight oldLight : current) {
            if (!desired.contains(oldLight)) {
                removeLight(server, oldLight, playerId);
            }
        }

        ServerLevel level = (ServerLevel) player.level();
        Set<PlacedLight> ownedLights = new HashSet<>();

        for (PlacedLight desiredLight : desired) {
            if (!desiredLight.dimension().equals(level.dimension())) {
                continue;
            }

            BlockState state = level.getBlockState(desiredLight.pos());
            if (state.is(Blocks.LIGHT)) {
                if (current.contains(desiredLight) || isOwnedByAnotherPlayer(desiredLight, playerId)) {
                    ownedLights.add(desiredLight);
                }
                continue;
            }

            if (!state.isAir()) {
                continue;
            }

            level.setBlock(desiredLight.pos(), lightState(), 3);
            if (level.getBlockState(desiredLight.pos()).is(Blocks.LIGHT)) {
                ownedLights.add(desiredLight);
            }
        }

        if (ownedLights.isEmpty()) {
            PLAYER_LIGHTS.remove(playerId);
        } else {
            PLAYER_LIGHTS.put(playerId, ownedLights);
        }
    }

    private static Set<PlacedLight> desiredLights(ServerPlayer player) {
        ResourceKey<Level> dimension = player.level().dimension();
        ServerLevel level = (ServerLevel) player.level();
        BlockPos feet = player.blockPosition();
        Set<PlacedLight> desired = new HashSet<>();

        for (int x = -DYNAMIC_LIGHT_RADIUS; x <= DYNAMIC_LIGHT_RADIUS; x += DYNAMIC_LIGHT_STEP) {
            for (int z = -DYNAMIC_LIGHT_RADIUS; z <= DYNAMIC_LIGHT_RADIUS; z += DYNAMIC_LIGHT_STEP) {
                if ((x * x) + (z * z) > DYNAMIC_LIGHT_RADIUS * DYNAMIC_LIGHT_RADIUS) {
                    continue;
                }

                BlockPos lightPos = findUsableLightPos(level, feet.offset(x, 0, z));
                if (lightPos != null) {
                    desired.add(new PlacedLight(dimension, lightPos));
                }
            }
        }

        return desired;
    }

    private static boolean shouldAutoLight(ServerPlayer player) {
        ServerLevel level = (ServerLevel) player.level();
        return isNight(level) || isNaturallyDark(level, player.blockPosition().above());
    }

    private static boolean isNight(ServerLevel level) {
        if (!level.dimensionType().hasSkyLight()) {
            return true;
        }

        long time = level.getDayTime() % 24000L;
        return time >= NIGHT_START && time <= NIGHT_END;
    }

    private static boolean isNaturallyDark(ServerLevel level, BlockPos pos) {
        return level.getBrightness(LightLayer.SKY, pos) <= DARK_SKY_LIGHT;
    }

    private static boolean hasLightItem(ServerPlayer player) {
        return hasLightItem(player.getInventory().items)
                || hasLightItem(player.getInventory().armor)
                || hasLightItem(player.getInventory().offhand);
    }

    private static boolean hasLightItem(Iterable<ItemStack> stacks) {
        for (ItemStack stack : stacks) {
            if (!stack.isEmpty() && stack.getItem() == MAGIC_WORLD_LIGHT.get()) {
                return true;
            }
        }
        return false;
    }

    private static boolean isLightItem(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() == MAGIC_WORLD_LIGHT.get();
    }

    private static ItemStack premiumLightStack() {
        return new ItemStack(MAGIC_WORLD_LIGHT.get());
    }

    private static void ensurePermanentLightItem(ServerPlayer player) {
        if (hasLightItem(player)) {
            return;
        }

        ItemStack offhand = player.getOffhandItem();
        if (offhand.isEmpty()) {
            player.setItemInHand(InteractionHand.OFF_HAND, premiumLightStack());
            return;
        }

        if (player.getInventory().add(premiumLightStack())) {
            return;
        }

        player.setItemInHand(InteractionHand.OFF_HAND, premiumLightStack());
    }

    private static void clearPlayerLights(ServerPlayer player) {
        MinecraftServer server = player.getServer();
        if (server == null) {
            return;
        }

        UUID playerId = player.getUUID();
        Set<PlacedLight> lights = PLAYER_LIGHTS.remove(playerId);
        if (lights == null || lights.isEmpty()) {
            return;
        }

        for (PlacedLight light : lights) {
            removeLight(server, light, playerId);
        }
    }

    private static void clearAllLights(MinecraftServer server) {
        Set<PlacedLight> lights = new HashSet<>();
        for (Set<PlacedLight> playerLights : PLAYER_LIGHTS.values()) {
            lights.addAll(playerLights);
        }
        PLAYER_LIGHTS.clear();

        for (PlacedLight light : lights) {
            removeLight(server, light);
        }
    }

    private static void removeLight(MinecraftServer server, PlacedLight light, UUID ownerToIgnore) {
        if (isOwnedByAnotherPlayer(light, ownerToIgnore)) {
            return;
        }
        removeLight(server, light);
    }

    private static void removeLight(MinecraftServer server, PlacedLight light) {
        ServerLevel level = server.getLevel(light.dimension());
        if (level != null && level.getBlockState(light.pos()).is(Blocks.LIGHT)) {
            level.setBlock(light.pos(), Blocks.AIR.defaultBlockState(), 3);
        }
    }

    private static boolean isOwnedByAnotherPlayer(PlacedLight light, UUID playerId) {
        for (Map.Entry<UUID, Set<PlacedLight>> entry : PLAYER_LIGHTS.entrySet()) {
            if (!entry.getKey().equals(playerId) && entry.getValue().contains(light)) {
                return true;
            }
        }
        return false;
    }

    private static BlockState lightState() {
        return Blocks.LIGHT.defaultBlockState().setValue(LightBlock.LEVEL, LIGHT_LEVEL);
    }

    private static BlockPos findUsableLightPos(ServerLevel level, BlockPos anchor) {
        BlockPos[] candidates = {
                anchor.above(),
                anchor,
                anchor.above(2),
                anchor.north(),
                anchor.south(),
                anchor.east(),
                anchor.west(),
                anchor.above().north(),
                anchor.above().south(),
                anchor.above().east(),
                anchor.above().west()
        };

        for (BlockPos candidate : candidates) {
            BlockState state = level.getBlockState(candidate);
            if (state.isAir() || state.is(Blocks.LIGHT)) {
                return candidate;
            }
        }

        return null;
    }

    private static void placeTorchLightField(Level level, BlockPos origin) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        for (int x = -TORCH_FIELD_RADIUS; x <= TORCH_FIELD_RADIUS; x += TORCH_FIELD_STEP) {
            for (int y = -TORCH_FIELD_STEP; y <= TORCH_FIELD_STEP; y += TORCH_FIELD_STEP) {
                for (int z = -TORCH_FIELD_RADIUS; z <= TORCH_FIELD_RADIUS; z += TORCH_FIELD_STEP) {
                    if ((x * x) + (z * z) > TORCH_FIELD_RADIUS * TORCH_FIELD_RADIUS) {
                        continue;
                    }

                    BlockPos lightPos = findUsableLightPos(serverLevel, origin.offset(x, y, z));
                    if (lightPos != null && !lightPos.equals(origin)) {
                        BlockState state = serverLevel.getBlockState(lightPos);
                        if (state.isAir()) {
                            serverLevel.setBlock(lightPos, lightState(), 3);
                        }
                    }
                }
            }
        }
    }

    private static void removeTorchLightField(Level level, BlockPos origin) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        for (int x = -TORCH_FIELD_RADIUS; x <= TORCH_FIELD_RADIUS; x += TORCH_FIELD_STEP) {
            for (int y = -TORCH_FIELD_STEP - 2; y <= TORCH_FIELD_STEP + 2; y++) {
                for (int z = -TORCH_FIELD_RADIUS; z <= TORCH_FIELD_RADIUS; z += TORCH_FIELD_STEP) {
                    if ((x * x) + (z * z) > TORCH_FIELD_RADIUS * TORCH_FIELD_RADIUS) {
                        continue;
                    }

                    BlockPos anchor = origin.offset(x, y, z);
                    removeNearbyLightBlock(serverLevel, anchor);
                }
            }
        }
    }

    private static void removeNearbyLightBlock(ServerLevel level, BlockPos anchor) {
        BlockPos[] candidates = {
                anchor,
                anchor.above(),
                anchor.below(),
                anchor.north(),
                anchor.south(),
                anchor.east(),
                anchor.west()
        };

        for (BlockPos candidate : candidates) {
            if (level.getBlockState(candidate).is(Blocks.LIGHT)) {
                level.setBlock(candidate, Blocks.AIR.defaultBlockState(), 3);
                return;
            }
        }
    }

    private static class PremiumTorchBlock extends TorchBlock {
        private PremiumTorchBlock(BlockBehaviour.Properties properties) {
            super(properties, ParticleTypes.FLAME);
        }

        @Override
        public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
            super.onPlace(state, level, pos, oldState, isMoving);
            if (!oldState.is(state.getBlock())) {
                placeTorchLightField(level, pos);
            }
        }

        @Override
        public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
            if (!newState.is(state.getBlock())) {
                removeTorchLightField(level, pos);
            }
            super.onRemove(state, level, pos, newState, isMoving);
        }
    }

    private static class PremiumWallTorchBlock extends WallTorchBlock {
        private PremiumWallTorchBlock(BlockBehaviour.Properties properties) {
            super(properties, ParticleTypes.FLAME);
        }

        @Override
        public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
            super.onPlace(state, level, pos, oldState, isMoving);
            if (!oldState.is(state.getBlock())) {
                placeTorchLightField(level, pos);
            }
        }

        @Override
        public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
            if (!newState.is(state.getBlock())) {
                removeTorchLightField(level, pos);
            }
            super.onRemove(state, level, pos, newState, isMoving);
        }
    }

    private static class PremiumTorchItem extends StandingAndWallBlockItem {
        private PremiumTorchItem(Block floorBlock, Block wallBlock, Item.Properties properties, Direction attachmentDirection) {
            super(floorBlock, wallBlock, properties, attachmentDirection);
        }

        @Override
        public InteractionResult place(BlockPlaceContext context) {
            ItemStack stack = context.getItemInHand();
            Player player = context.getPlayer();
            int countBeforePlacement = stack.getCount();
            InteractionResult result = super.place(context);

            if (result.consumesAction() && player != null && !player.getAbilities().instabuild) {
                stack.setCount(Math.max(1, countBeforePlacement));
            }

            if (result.consumesAction() && player instanceof ServerPlayer serverPlayer) {
                ensurePermanentLightItem(serverPlayer);
            }

            return result;
        }
    }

    private record PlacedLight(ResourceKey<Level> dimension, BlockPos pos) {
    }
}
