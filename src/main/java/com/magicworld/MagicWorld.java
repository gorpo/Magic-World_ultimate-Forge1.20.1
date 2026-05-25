package com.magicworld;

import com.magicworld.client.ClientEvents;
import com.magicworld.client.MagicWorldClientCompat;
import com.magicworld.entity.PeacefulDragon;
import com.magicworld.event.AuraEvents;
import com.magicworld.event.CraftEvents;
import com.magicworld.event.MobEvents;
import com.magicworld.event.PlayerJoinEvents;
import com.magicworld.event.StarterPortalEvents;
import com.magicworld.network.MagicWorldNetwork;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.PowerParticleOption;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.Map;

@Mod(MagicWorld.MODID)
public class MagicWorld {

    public static final String MODID =
            "magicworld";

    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(MODID);

    public static final DeferredRegister.Entities ENTITIES =
            DeferredRegister.createEntities(MODID);

    public static final DeferredHolder<EntityType<?>, EntityType<PeacefulDragon>> PEACEFUL_DRAGON =
            ENTITIES.registerEntityType(
                    "peaceful_dragon",
                    PeacefulDragon::new,
                    MobCategory.CREATURE,
                    builder -> builder
                            .sized(16.0F, 8.0F)
                            .fireImmune()
                            .clientTrackingRange(1024)
                            .updateInterval(2)
            );

    public static final ResourceKey<EquipmentAsset> DRACONIC_AETHER_EQUIPMENT_ASSET =
            ResourceKey.create(
                    EquipmentAssets.ROOT_ID,
                    Identifier.fromNamespaceAndPath(MODID, "draconic_aether")
            );

    public static final ArmorMaterial DRACONIC_AETHER_ARMOR_MATERIAL =
            new ArmorMaterial(
                    45,
                    makeArmorDefense(4, 7, 9, 4, 12),
                    30,
                    SoundEvents.ARMOR_EQUIP_NETHERITE,
                    5.0F,
                    0.2F,
                    ItemTags.REPAIRS_NETHERITE_ARMOR,
                    DRACONIC_AETHER_EQUIPMENT_ASSET
            );

    public static final DeferredItem<Item> VARINHA_MAGICA =
            ITEMS.registerSimpleItem(
                    "varinha_magica",
                    properties -> properties
                            .stacksTo(1)
                            .rarity(Rarity.EPIC)
                            .fireResistant()
            );

    public static final DeferredItem<Item> DRACONIC_AETHER_HELMET =
            registerDraconicArmor("draconic_aether_helmet", ArmorType.HELMET);

    public static final DeferredItem<Item> DRACONIC_AETHER_CHESTPLATE =
            registerDraconicArmor("draconic_aether_chestplate", ArmorType.CHESTPLATE);

    public static final DeferredItem<Item> DRACONIC_AETHER_LEGGINGS =
            registerDraconicArmor("draconic_aether_leggings", ArmorType.LEGGINGS);

    public static final DeferredItem<Item> DRACONIC_AETHER_BOOTS =
            registerDraconicArmor("draconic_aether_boots", ArmorType.BOOTS);

    public MagicWorld(
            IEventBus modEventBus,
            ModContainer modContainer
    ) {

        ITEMS.register(modEventBus);
        ENTITIES.register(modEventBus);
        MagicWorldNetwork.register(modEventBus);

        CraftEvents craftEvents = new CraftEvents();

        NeoForge.EVENT_BUS.addListener(
                craftEvents::onLeftClickBlock
        );

        MobEvents mobEvents = new MobEvents();

        NeoForge.EVENT_BUS.addListener(
                mobEvents::onLeftClickEntity
        );

        NeoForge.EVENT_BUS.addListener(
                mobEvents::onLivingDeath
        );

        modEventBus.addListener(this::addCreative);
        modEventBus.addListener(this::registerEntityAttributes);

        modContainer.registerConfig(
                ModConfig.Type.COMMON,
                Config.SPEC
        );

        PlayerJoinEvents.registerListeners();
        StarterPortalEvents.registerListeners();
        AuraEvents.registerListeners();

        if (FMLEnvironment.getDist() == Dist.CLIENT) {
            MagicWorldClientCompat.prepareDistantHorizonsConfig();
            ClientEvents.registerListeners(modEventBus);
        }
    }

    private static DeferredItem<Item> registerDraconicArmor(String name, ArmorType type) {
        return ITEMS.registerSimpleItem(
                name,
                properties -> properties
                        .humanoidArmor(DRACONIC_AETHER_ARMOR_MATERIAL, type)
                        .rarity(Rarity.EPIC)
                        .fireResistant()
        );
    }

    private static Map<ArmorType, Integer> makeArmorDefense(
            int boots,
            int leggings,
            int chestplate,
            int helmet,
            int body
    ) {
        return Map.of(
                ArmorType.BOOTS, boots,
                ArmorType.LEGGINGS, leggings,
                ArmorType.CHESTPLATE, chestplate,
                ArmorType.HELMET, helmet,
                ArmorType.BODY, body
        );
    }

    private void registerEntityAttributes(EntityAttributeCreationEvent event) {
        event.put(
                PEACEFUL_DRAGON.get(),
                PeacefulDragon.createAttributes().build()
        );
    }

    public static void effects(
            ServerLevel level,
            BlockPos pos
    ) {

        level.sendParticles(
                PowerParticleOption.create(
                        ParticleTypes.DRAGON_BREATH,
                        1.0F
                ),
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
}
