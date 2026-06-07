package com.magicworld;

import com.magicworld.event.CraftEvents;
import com.magicworld.event.AuraEvents;
import com.magicworld.event.MagicWorldTeleportGuard;
import com.magicworld.event.MobEvents;
import com.magicworld.event.StarterPortalEvents;
import com.magicworld.integration.MagicWorldMineColoniesIntegration;
import com.magicworld.network.MagicWorldNetwork;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@Mod(MagicWorld.MODID)
public class MagicWorld {

    public static final String MODID =
            "magicworld";

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(
                    ForgeRegistries.ITEMS,
                    MODID
            );

    public static final RegistryObject<Item> VARINHA_MAGICA =
            ITEMS.register(
                    "varinha_magica",
                    () -> new Item(
                            new Item.Properties()
                                    .stacksTo(1)
                                    .rarity(Rarity.EPIC)
                                    .fireResistant()
                    )
            );

    public static final RegistryObject<Item> DRACONIC_AETHER_HELMET =
            registerDraconicArmor("draconic_aether_helmet", ArmorItem.Type.HELMET);

    public static final RegistryObject<Item> DRACONIC_AETHER_CHESTPLATE =
            registerDraconicArmor("draconic_aether_chestplate", ArmorItem.Type.CHESTPLATE);

    public static final RegistryObject<Item> DRACONIC_AETHER_LEGGINGS =
            registerDraconicArmor("draconic_aether_leggings", ArmorItem.Type.LEGGINGS);

    public static final RegistryObject<Item> DRACONIC_AETHER_BOOTS =
            registerDraconicArmor("draconic_aether_boots", ArmorItem.Type.BOOTS);

    public MagicWorld() {
        MagicWorldNetwork.register();

        ITEMS.register(
                FMLJavaModLoadingContext
                        .get()
                        .getModEventBus()
        );

        MinecraftForge.EVENT_BUS.register(
                new CraftEvents()
        );

        MinecraftForge.EVENT_BUS.register(
                new MobEvents()
        );

        MinecraftForge.EVENT_BUS.register(
                new AuraEvents()
        );

        MinecraftForge.EVENT_BUS.register(
                new StarterPortalEvents()
        );

        MinecraftForge.EVENT_BUS.register(
                new MagicWorldTeleportGuard()
        );

        MinecraftForge.EVENT_BUS.register(
                new MagicWorldMineColoniesIntegration()
        );

        MinecraftForge.EVENT_BUS.register(
                this
        );
    }

    public static void effects(
            ServerLevel level,
            BlockPos pos
    ) {

        level.sendParticles(
                ParticleTypes.DRAGON_BREATH,
                pos.getX() + 0.5,
                pos.getY() + 1,
                pos.getZ() + 0.5,
                30,
                0.4,
                0.4,
                0.4,
                0.03
        );

        level.sendParticles(
                ParticleTypes.PORTAL,
                pos.getX() + 0.5,
                pos.getY() + 1,
                pos.getZ() + 0.5,
                25,
                0.4,
                0.4,
                0.4,
                0.08
        );

        level.playSound(
                null,
                pos,
                SoundEvents.PANDA_PRE_SNEEZE,
                SoundSource.PLAYERS,
                1.4f,
                0.8f
        );
    }

    @SubscribeEvent
    public void addCreative(
            BuildCreativeModeTabContentsEvent event
    ) {

        if (event.getTabKey()
                == CreativeModeTabs.TOOLS_AND_UTILITIES) {

            event.accept(
                    VARINHA_MAGICA
            );
            event.accept(DRACONIC_AETHER_HELMET);
            event.accept(DRACONIC_AETHER_CHESTPLATE);
            event.accept(DRACONIC_AETHER_LEGGINGS);
            event.accept(DRACONIC_AETHER_BOOTS);
        }
    }

    private static RegistryObject<Item> registerDraconicArmor(
            String name,
            ArmorItem.Type type
    ) {
        return ITEMS.register(
                name,
                () -> new ArmorItem(
                        ArmorMaterials.NETHERITE,
                        type,
                        new Item.Properties()
                                .rarity(Rarity.EPIC)
                                .fireResistant()
                )
        );
    }
}
