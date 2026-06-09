package com.magicworldlight;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LightBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
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
    private static final int DARK_SKY_LIGHT = 7;
    private static final long NIGHT_START = 12500L;
    private static final long NIGHT_END = 23500L;
    private static final Map<UUID, Set<PlacedLight>> PLAYER_LIGHTS = new HashMap<>();

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);

    public static final RegistryObject<Item> MAGIC_WORLD_LIGHT = ITEMS.register(
            "magic_world_light",
            () -> new Item(new Item.Properties()
                    .stacksTo(1)
                    .rarity(Rarity.EPIC)
                    .fireResistant())
    );

    public MagicWorldLight() {
        var modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        ITEMS.register(modEventBus);
        modEventBus.addListener(this::addCreative);
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
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

        if (hasLightItem(player) || shouldAutoLight(player)) {
            refreshPlayerLights(player);
        } else {
            clearPlayerLights(player);
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
        BlockPos[] candidates = {
                feet.above(),
                feet,
                feet.above(2),
                feet.north(),
                feet.south(),
                feet.east(),
                feet.west(),
                feet.above().north(),
                feet.above().south(),
                feet.above().east(),
                feet.above().west()
        };

        for (BlockPos candidate : candidates) {
            BlockState state = level.getBlockState(candidate);
            if (state.isAir() || state.is(Blocks.LIGHT)) {
                desired.add(new PlacedLight(dimension, candidate));
                break;
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

    private record PlacedLight(ResourceKey<Level> dimension, BlockPos pos) {
    }
}
