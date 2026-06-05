package com.example.examplemod;

import com.example.examplemod.event.CraftEvents;
import com.example.examplemod.event.MobEvents;
import com.example.examplemod.event.StarterPortalEvents;
import com.example.examplemod.network.MagicWorldNetwork;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.CreativeModeTabs;
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

@Mod(ExampleMod.MODID)
public class ExampleMod {

    public static final String MODID =
            "examplemod";

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

    public ExampleMod() {
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
                new StarterPortalEvents()
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
        }
    }
}
