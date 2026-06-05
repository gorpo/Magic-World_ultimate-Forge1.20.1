package com.example.examplemod.event;

import com.example.examplemod.ExampleMod;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

public class PremiumEnemies {

    private static final String PREMIUM_TAG =
            "VarinhaMagicaPremiumEnemy";

    private static final String ORIGINAL_TAG =
            "VarinhaMagicaOriginalEnemy=";

    public static boolean transform(
            Level level,
            Entity target
    ) {

        if (level.isClientSide()) {
            return false;
        }

        String originalEnemy =
                getOriginalEnemy(target);

        if (originalEnemy != null) {
            EntityType<?> originalType =
                    originalEnemyType(originalEnemy);

            if (originalType == null) {
                return false;
            }

            return replaceEntity(
                    level,
                    target,
                    originalType,
                    null
            );
        }

        EntityType<?> peacefulType =
                peacefulType(target.getType());

        if (peacefulType == null) {
            return false;
        }

        String enemyId =
                enemyId(target.getType());

        if (enemyId == null) {
            return false;
        }

        return replaceEntity(
                level,
                target,
                peacefulType,
                enemyId
        );
    }

    public static boolean dropPeacefulSet(
            Entity entity
    ) {

        String originalEnemy =
                getOriginalEnemy(entity);

        if (originalEnemy == null) {
            return false;
        }

        if (!(entity instanceof LivingEntity livingEntity)) {
            return false;
        }

        if (originalEnemy.equals("minecraft:spider")
                || originalEnemy.equals("minecraft:cave_spider")
                || originalEnemy.equals("minecraft:zombie")
                || originalEnemy.equals("minecraft:husk")
                || originalEnemy.equals("minecraft:drowned")
                || originalEnemy.equals("minecraft:skeleton")
                || originalEnemy.equals("minecraft:stray")
                || originalEnemy.equals("minecraft:zombie_villager")) {

            dropIronSet(livingEntity, Items.IRON_BLOCK);
            return true;
        }

        else if (originalEnemy.equals("minecraft:creeper")
                || originalEnemy.equals("minecraft:enderman")
                || originalEnemy.equals("minecraft:witch")
                || originalEnemy.equals("minecraft:phantom")
                || originalEnemy.equals("minecraft:guardian")
                || originalEnemy.equals("minecraft:shulker")) {

            dropDiamondSet(livingEntity, Items.DIAMOND_BLOCK);
            return true;
        }

        else if (originalEnemy.equals("minecraft:blaze")
                || originalEnemy.equals("minecraft:ghast")
                || originalEnemy.equals("minecraft:magma_cube")
                || originalEnemy.equals("minecraft:piglin")
                || originalEnemy.equals("minecraft:hoglin")
                || originalEnemy.equals("minecraft:zombified_piglin")) {

            dropGoldSet(livingEntity, Items.GOLD_BLOCK);
            return true;
        }

        else if (originalEnemy.equals("minecraft:piglin_brute")
                || originalEnemy.equals("minecraft:wither_skeleton")
                || originalEnemy.equals("minecraft:zoglin")) {

            dropNetheriteSet(livingEntity, Items.NETHERITE_BLOCK);
            return true;
        }

        else if (originalEnemy.equals("minecraft:pillager")
                || originalEnemy.equals("minecraft:vindicator")
                || originalEnemy.equals("minecraft:evoker")
                || originalEnemy.equals("minecraft:illusioner")
                || originalEnemy.equals("minecraft:vex")
                || originalEnemy.equals("minecraft:ravager")) {

            dropDiamondSet(livingEntity, Items.EMERALD_BLOCK);
            return true;
        }

        else if (originalEnemy.equals("minecraft:slime")
                || originalEnemy.equals("minecraft:silverfish")
                || originalEnemy.equals("minecraft:endermite")) {

            dropChainmailSet(livingEntity, Items.SLIME_BLOCK);
            return true;
        }

        else if (originalEnemy.equals("minecraft:elder_guardian")) {

            dropDiamondSet(livingEntity, Items.SEA_LANTERN);
            return true;
        }

        else if (originalEnemy.equals("minecraft:giant")) {

            dropDiamondSet(livingEntity, Items.EMERALD_BLOCK);
            return true;
        }

        else if (originalEnemy.equals("minecraft:warden")
                || originalEnemy.equals("minecraft:wither")
                || originalEnemy.equals("minecraft:ender_dragon")) {

            dropNetheriteSet(livingEntity, Items.NETHERITE_BLOCK);
            return true;
        }

        return false;
    }

    private static boolean replaceEntity(
            Level level,
            Entity oldEntity,
            EntityType<?> newType,
            String originalEnemy
    ) {

        Entity newEntity =
                newType.create(level);

        if (newEntity == null) {
            return false;
        }

        newEntity.moveTo(
                oldEntity.getX(),
                oldEntity.getY(),
                oldEntity.getZ(),
                oldEntity.getYRot(),
                oldEntity.getXRot()
        );

        if (originalEnemy != null) {
            newEntity.addTag(PREMIUM_TAG);
            newEntity.addTag(
                    ORIGINAL_TAG + originalEnemy
            );

            if (newEntity instanceof LivingEntity livingEntity) {
                livingEntity.addEffect(
                        new MobEffectInstance(
                                MobEffects.GLOWING,
                                999999,
                                0
                        )
                );
            }
        }

        level.addFreshEntity(
                newEntity
        );

        oldEntity.discard();

        if (level instanceof ServerLevel serverLevel) {
            ExampleMod.effects(
                    serverLevel,
                    oldEntity.blockPosition()
            );
        }

        return true;
    }

    private static String getOriginalEnemy(
            Entity entity
    ) {

        if (!entity.getTags().contains(PREMIUM_TAG)) {
            return null;
        }

        for (String tag : entity.getTags()) {
            if (tag.startsWith(ORIGINAL_TAG)) {
                return tag.substring(
                        ORIGINAL_TAG.length()
                );
            }
        }

        return null;
    }

    private static EntityType<?> peacefulType(
            EntityType<?> enemyType
    ) {

        if (enemyType == EntityType.BLAZE) return EntityType.CHICKEN;
        if (enemyType == EntityType.CAVE_SPIDER) return EntityType.RABBIT;
        if (enemyType == EntityType.CREEPER) return EntityType.COW;
        if (enemyType == EntityType.DROWNED) return EntityType.TURTLE;
        if (enemyType == EntityType.ELDER_GUARDIAN) return EntityType.TURTLE;
        if (enemyType == EntityType.ENDER_DRAGON) return EntityType.HORSE;
        if (enemyType == EntityType.ENDERMAN) return EntityType.HORSE;
        if (enemyType == EntityType.ENDERMITE) return EntityType.RABBIT;
        if (enemyType == EntityType.EVOKER) return EntityType.VILLAGER;
        if (enemyType == EntityType.GHAST) return EntityType.BAT;
        if (enemyType == EntityType.GIANT) return EntityType.IRON_GOLEM;
        if (enemyType == EntityType.GUARDIAN) return EntityType.SQUID;
        if (enemyType == EntityType.HOGLIN) return EntityType.PIG;
        if (enemyType == EntityType.HUSK) return EntityType.CAMEL;
        if (enemyType == EntityType.ILLUSIONER) return EntityType.VILLAGER;
        if (enemyType == EntityType.MAGMA_CUBE) return EntityType.CHICKEN;
        if (enemyType == EntityType.PHANTOM) return EntityType.BAT;
        if (enemyType == EntityType.PIGLIN) return EntityType.PIG;
        if (enemyType == EntityType.PIGLIN_BRUTE) return EntityType.PIG;
        if (enemyType == EntityType.PILLAGER) return EntityType.VILLAGER;
        if (enemyType == EntityType.RAVAGER) return EntityType.COW;
        if (enemyType == EntityType.SHULKER) return EntityType.TURTLE;
        if (enemyType == EntityType.SILVERFISH) return EntityType.RABBIT;
        if (enemyType == EntityType.SKELETON) return EntityType.HORSE;
        if (enemyType == EntityType.SLIME) return EntityType.CHICKEN;
        if (enemyType == EntityType.SPIDER) return EntityType.RABBIT;
        if (enemyType == EntityType.STRAY) return EntityType.SNOW_GOLEM;
        if (enemyType == EntityType.VEX) return EntityType.BAT;
        if (enemyType == EntityType.VINDICATOR) return EntityType.VILLAGER;
        if (enemyType == EntityType.WARDEN) return EntityType.IRON_GOLEM;
        if (enemyType == EntityType.WITCH) return EntityType.CAT;
        if (enemyType == EntityType.WITHER) return EntityType.SNOW_GOLEM;
        if (enemyType == EntityType.WITHER_SKELETON) return EntityType.SKELETON_HORSE;
        if (enemyType == EntityType.ZOGLIN) return EntityType.PIG;
        if (enemyType == EntityType.ZOMBIE) return EntityType.VILLAGER;
        if (enemyType == EntityType.ZOMBIE_VILLAGER) return EntityType.VILLAGER;
        if (enemyType == EntityType.ZOMBIFIED_PIGLIN) return EntityType.PIG;

        return null;
    }

    private static String enemyId(
            EntityType<?> enemyType
    ) {

        if (enemyType == EntityType.BLAZE) return "minecraft:blaze";
        if (enemyType == EntityType.CAVE_SPIDER) return "minecraft:cave_spider";
        if (enemyType == EntityType.CREEPER) return "minecraft:creeper";
        if (enemyType == EntityType.DROWNED) return "minecraft:drowned";
        if (enemyType == EntityType.ELDER_GUARDIAN) return "minecraft:elder_guardian";
        if (enemyType == EntityType.ENDER_DRAGON) return "minecraft:ender_dragon";
        if (enemyType == EntityType.ENDERMAN) return "minecraft:enderman";
        if (enemyType == EntityType.ENDERMITE) return "minecraft:endermite";
        if (enemyType == EntityType.EVOKER) return "minecraft:evoker";
        if (enemyType == EntityType.GHAST) return "minecraft:ghast";
        if (enemyType == EntityType.GIANT) return "minecraft:giant";
        if (enemyType == EntityType.GUARDIAN) return "minecraft:guardian";
        if (enemyType == EntityType.HOGLIN) return "minecraft:hoglin";
        if (enemyType == EntityType.HUSK) return "minecraft:husk";
        if (enemyType == EntityType.ILLUSIONER) return "minecraft:illusioner";
        if (enemyType == EntityType.MAGMA_CUBE) return "minecraft:magma_cube";
        if (enemyType == EntityType.PHANTOM) return "minecraft:phantom";
        if (enemyType == EntityType.PIGLIN) return "minecraft:piglin";
        if (enemyType == EntityType.PIGLIN_BRUTE) return "minecraft:piglin_brute";
        if (enemyType == EntityType.PILLAGER) return "minecraft:pillager";
        if (enemyType == EntityType.RAVAGER) return "minecraft:ravager";
        if (enemyType == EntityType.SHULKER) return "minecraft:shulker";
        if (enemyType == EntityType.SILVERFISH) return "minecraft:silverfish";
        if (enemyType == EntityType.SKELETON) return "minecraft:skeleton";
        if (enemyType == EntityType.SLIME) return "minecraft:slime";
        if (enemyType == EntityType.SPIDER) return "minecraft:spider";
        if (enemyType == EntityType.STRAY) return "minecraft:stray";
        if (enemyType == EntityType.VEX) return "minecraft:vex";
        if (enemyType == EntityType.VINDICATOR) return "minecraft:vindicator";
        if (enemyType == EntityType.WARDEN) return "minecraft:warden";
        if (enemyType == EntityType.WITCH) return "minecraft:witch";
        if (enemyType == EntityType.WITHER) return "minecraft:wither";
        if (enemyType == EntityType.WITHER_SKELETON) return "minecraft:wither_skeleton";
        if (enemyType == EntityType.ZOGLIN) return "minecraft:zoglin";
        if (enemyType == EntityType.ZOMBIE) return "minecraft:zombie";
        if (enemyType == EntityType.ZOMBIE_VILLAGER) return "minecraft:zombie_villager";
        if (enemyType == EntityType.ZOMBIFIED_PIGLIN) return "minecraft:zombified_piglin";

        return null;
    }

    private static EntityType<?> originalEnemyType(
            String enemyId
    ) {

        if (enemyId.equals("minecraft:blaze")) return EntityType.BLAZE;
        if (enemyId.equals("minecraft:cave_spider")) return EntityType.CAVE_SPIDER;
        if (enemyId.equals("minecraft:creeper")) return EntityType.CREEPER;
        if (enemyId.equals("minecraft:drowned")) return EntityType.DROWNED;
        if (enemyId.equals("minecraft:elder_guardian")) return EntityType.ELDER_GUARDIAN;
        if (enemyId.equals("minecraft:ender_dragon")) return EntityType.ENDER_DRAGON;
        if (enemyId.equals("minecraft:enderman")) return EntityType.ENDERMAN;
        if (enemyId.equals("minecraft:endermite")) return EntityType.ENDERMITE;
        if (enemyId.equals("minecraft:evoker")) return EntityType.EVOKER;
        if (enemyId.equals("minecraft:ghast")) return EntityType.GHAST;
        if (enemyId.equals("minecraft:giant")) return EntityType.GIANT;
        if (enemyId.equals("minecraft:guardian")) return EntityType.GUARDIAN;
        if (enemyId.equals("minecraft:hoglin")) return EntityType.HOGLIN;
        if (enemyId.equals("minecraft:husk")) return EntityType.HUSK;
        if (enemyId.equals("minecraft:illusioner")) return EntityType.ILLUSIONER;
        if (enemyId.equals("minecraft:magma_cube")) return EntityType.MAGMA_CUBE;
        if (enemyId.equals("minecraft:phantom")) return EntityType.PHANTOM;
        if (enemyId.equals("minecraft:piglin")) return EntityType.PIGLIN;
        if (enemyId.equals("minecraft:piglin_brute")) return EntityType.PIGLIN_BRUTE;
        if (enemyId.equals("minecraft:pillager")) return EntityType.PILLAGER;
        if (enemyId.equals("minecraft:ravager")) return EntityType.RAVAGER;
        if (enemyId.equals("minecraft:shulker")) return EntityType.SHULKER;
        if (enemyId.equals("minecraft:silverfish")) return EntityType.SILVERFISH;
        if (enemyId.equals("minecraft:skeleton")) return EntityType.SKELETON;
        if (enemyId.equals("minecraft:slime")) return EntityType.SLIME;
        if (enemyId.equals("minecraft:spider")) return EntityType.SPIDER;
        if (enemyId.equals("minecraft:stray")) return EntityType.STRAY;
        if (enemyId.equals("minecraft:vex")) return EntityType.VEX;
        if (enemyId.equals("minecraft:vindicator")) return EntityType.VINDICATOR;
        if (enemyId.equals("minecraft:warden")) return EntityType.WARDEN;
        if (enemyId.equals("minecraft:witch")) return EntityType.WITCH;
        if (enemyId.equals("minecraft:wither")) return EntityType.WITHER;
        if (enemyId.equals("minecraft:wither_skeleton")) return EntityType.WITHER_SKELETON;
        if (enemyId.equals("minecraft:zoglin")) return EntityType.ZOGLIN;
        if (enemyId.equals("minecraft:zombie")) return EntityType.ZOMBIE;
        if (enemyId.equals("minecraft:zombie_villager")) return EntityType.ZOMBIE_VILLAGER;
        if (enemyId.equals("minecraft:zombified_piglin")) return EntityType.ZOMBIFIED_PIGLIN;

        return null;
    }

    private static void dropLeatherSet(
            LivingEntity entity,
            Item blockItem
    ) {

        dropSet(
                entity,
                blockItem,
                Items.LEATHER_HELMET,
                Items.LEATHER_CHESTPLATE,
                Items.LEATHER_LEGGINGS,
                Items.LEATHER_BOOTS
        );
    }

    private static void dropChainmailSet(
            LivingEntity entity,
            Item blockItem
    ) {

        dropSet(
                entity,
                blockItem,
                Items.CHAINMAIL_HELMET,
                Items.CHAINMAIL_CHESTPLATE,
                Items.CHAINMAIL_LEGGINGS,
                Items.CHAINMAIL_BOOTS
        );
    }

    private static void dropIronSet(
            LivingEntity entity,
            Item blockItem
    ) {

        dropSet(
                entity,
                blockItem,
                Items.IRON_HELMET,
                Items.IRON_CHESTPLATE,
                Items.IRON_LEGGINGS,
                Items.IRON_BOOTS
        );
    }

    private static void dropGoldSet(
            LivingEntity entity,
            Item blockItem
    ) {

        dropSet(
                entity,
                blockItem,
                Items.GOLDEN_HELMET,
                Items.GOLDEN_CHESTPLATE,
                Items.GOLDEN_LEGGINGS,
                Items.GOLDEN_BOOTS
        );
    }

    private static void dropDiamondSet(
            LivingEntity entity,
            Item blockItem
    ) {

        dropSet(
                entity,
                blockItem,
                Items.DIAMOND_HELMET,
                Items.DIAMOND_CHESTPLATE,
                Items.DIAMOND_LEGGINGS,
                Items.DIAMOND_BOOTS
        );
    }

    private static void dropNetheriteSet(
            LivingEntity entity,
            Item blockItem
    ) {

        dropSet(
                entity,
                blockItem,
                Items.NETHERITE_HELMET,
                Items.NETHERITE_CHESTPLATE,
                Items.NETHERITE_LEGGINGS,
                Items.NETHERITE_BOOTS
        );
    }

    private static void dropSet(
            LivingEntity entity,
            Item blockItem,
            Item helmet,
            Item chestplate,
            Item leggings,
            Item boots
    ) {

        entity.spawnAtLocation(
                new ItemStack(helmet)
        );

        entity.spawnAtLocation(
                new ItemStack(chestplate)
        );

        entity.spawnAtLocation(
                new ItemStack(leggings)
        );

        entity.spawnAtLocation(
                new ItemStack(boots)
        );

        for (int i = 0; i < 10; i++) {
            entity.spawnAtLocation(
                    new ItemStack(
                            blockItem,
                            64
                    )
            );
        }
    }
}
