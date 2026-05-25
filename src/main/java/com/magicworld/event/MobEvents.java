package com.magicworld.event;

import com.magicworld.MagicWorld;
import com.magicworld.entity.*;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraft.world.entity.animal.allay.Allay;
import net.minecraft.world.entity.animal.axolotl.Axolotl;
import net.minecraft.world.entity.animal.camel.Camel;
import net.minecraft.world.entity.animal.bee.Bee;
import net.minecraft.world.entity.animal.chicken.Chicken;
import net.minecraft.world.entity.animal.cow.Cow;
import net.minecraft.world.entity.animal.cow.MushroomCow;
import net.minecraft.world.entity.animal.feline.Cat;
import net.minecraft.world.entity.animal.feline.Ocelot;
import net.minecraft.world.entity.animal.fox.Fox;
import net.minecraft.world.entity.animal.frog.Frog;
import net.minecraft.world.entity.animal.golem.IronGolem;
import net.minecraft.world.entity.animal.golem.SnowGolem;
import net.minecraft.world.entity.animal.goat.Goat;
import net.minecraft.world.entity.animal.equine.Horse;
import net.minecraft.world.entity.animal.equine.Llama;
import net.minecraft.world.entity.animal.equine.ZombieHorse;
import net.minecraft.world.entity.animal.panda.Panda;
import net.minecraft.world.entity.animal.parrot.Parrot;
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.entity.animal.rabbit.Rabbit;
import net.minecraft.world.entity.animal.sheep.Sheep;
import net.minecraft.world.entity.animal.squid.Squid;
import net.minecraft.world.entity.animal.turtle.Turtle;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.hoglin.Hoglin;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.npc.wanderingtrader.WanderingTrader;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;

public class MobEvents {

    public void onLeftClickEntity(
            AttackEntityEvent event
    ) {

        Level level =
                event.getEntity().level();

        ItemStack held =
                event.getEntity()
                        .getMainHandItem();

        if (held.getItem()
                != MagicWorld.VARINHA_MAGICA.get())
            return;

        event.setCanceled(true);

        if (level.isClientSide()) return;

        var target =
                event.getTarget();

        if (event.getEntity().isShiftKeyDown()) {
            destroyTarget(
                    level,
                    target
            );

            return;
        }

        if (PremiumEntityTags.isPremiumAnimal(target)
                && transformAnimal(level, target)) {
            return;
        }

        if (PremiumEnemies.transform(
                level,
                target
        )) {
            return;
        }

        transformAnimal(level, target);
    }

    private static boolean transformAnimal(
            Level level,
            Entity target
    ) {
        if (PremiumEntityTags.isAnimal(target, "axolotl")) {
            return PremiumAxolotl.transform(level, target);
        }

        // WOLF
        if (target instanceof Wolf wolf) {
            return PremiumWolf.transform(level, wolf);
        }

        // COW
        else if (target instanceof Cow
                || target instanceof MushroomCow) {

            return PremiumCow.transform(level, target);
        }

        // PIG
        else if (target instanceof Pig
                || target instanceof Hoglin) {

            return PremiumPig.transform(level, target);
        }

        // CAT
        else if (target instanceof Cat
                || target instanceof Ocelot) {

            return PremiumCat.transform(level, target);
        }

        // CHICKEN
        else if (target instanceof Chicken
                || target instanceof Parrot) {

            return PremiumChicken.transform(level, target);
        }

        // FOX
        else if (target instanceof Fox) {

            return PremiumFox.transform(
                    level,
                    (Fox) target
            );
        }

        // RABBIT
        else if (target instanceof Rabbit) {

            return PremiumRabbit.transform(
                    level,
                    (Rabbit) target
            );
        }

        // GOAT
        else if (target instanceof Goat) {

            return PremiumGoat.transform(
                    level,
                    (Goat) target
            );
        }

        // HORSE
        else if (target instanceof Horse
                || target instanceof ZombieHorse) {

            return PremiumHorse.transform(level, target);
        }

        // SHEEP
        else if (target instanceof Sheep
                || target instanceof Llama) {

            return PremiumSheep.transform(level, target);
        }

        // BEE
        else if (target instanceof Bee
                || target instanceof Allay) {

            return PremiumBee.transform(level, target);
        }

        // AXOLOTL
        else if (target instanceof Axolotl axolotl) {
            return PremiumAxolotl.transform(level, axolotl);
        }

        // CAMEL
        else if (target instanceof Camel camel) {
            return PremiumCamel.transform(level, camel);
        }

        // FROG
        else if (target instanceof Frog frog) {
            return PremiumFrog.transform(level, frog);
        }

        // TURTLE
        else if (target instanceof Turtle turtle) {
            return PremiumTurtle.transform(level, turtle);
        }

        // SQUID
        else if (target instanceof Squid squid) {
            return PremiumSquid.transform(level, squid);
        }

        // BAT
        else if (target instanceof Bat bat) {
            return PremiumBat.transform(level, bat);
        }

        // VILLAGER
        else if (target instanceof Villager
                || target instanceof WanderingTrader) {

            return PremiumVillager.transform(level, target);
        }

        // IRON GOLEM
        else if (target instanceof IronGolem golem) {
            return PremiumIronGolem.transform(level, golem);
        }

        // SNOW GOLEM
        else if (target instanceof SnowGolem golem) {
            return PremiumSnowGolem.transform(level, golem);
        }

        // PANDA
        else if (target instanceof Panda panda) {
            return PremiumPanda.transform(level, panda);
        }

        // CREEPER
        else if (target instanceof Creeper creeper) {
            return PremiumCreeper.transform(level, creeper);
        }

        return false;
    }

    private static void destroyTarget(
            Level level,
            Entity target
    ) {

        if (target instanceof LivingEntity living) {
            living.hurt(
                    level.damageSources().magic(),
                    Float.MAX_VALUE
            );
        }

        if (!target.isRemoved()) {
            target.discard();
        }

        MagicWorld.effects(
                (ServerLevel) level,
                target.blockPosition()
        );
    }

    public void onLivingDeath(
            LivingDeathEvent event
    ) {

        Level level =
                event.getEntity().level();

        if (level.isClientSide()) return;

        var entity =
                event.getEntity();

        if (PremiumEnemies.dropPeacefulSet(entity)) {
            return;
        }

        // WOLF PREMIUM
        if (PremiumEntityTags.isAnimal(entity, "wolf")
                && entity instanceof Wolf wolf) {

            drop(wolf, new ItemStack(Items.OAK_LOG, 64));
            drop(wolf, new ItemStack(Items.OAK_LOG, 64));
            drop(wolf, new ItemStack(Items.OAK_LOG, 64));

            drop(wolf, new ItemStack(Items.OAK_PLANKS, 64));
            drop(wolf, new ItemStack(Items.OAK_PLANKS, 64));
            drop(wolf, new ItemStack(Items.OAK_PLANKS, 64));

            drop(wolf, new ItemStack(Items.GLASS, 64));
            drop(wolf, new ItemStack(Items.COBBLESTONE, 64));
            drop(wolf, new ItemStack(Items.TORCH, 64));
        }

        // COW PREMIUM = MOOSHROOM
        if (PremiumEntityTags.isAnimal(entity, "cow")
                && entity instanceof MushroomCow cow) {

            drop(cow, new ItemStack(Items.IRON_BLOCK, 64));
            drop(cow, new ItemStack(Items.IRON_BLOCK, 64));
            drop(cow, new ItemStack(Items.IRON_BLOCK, 64));

            drop(cow, new ItemStack(Items.IRON_SWORD));
            drop(cow, new ItemStack(Items.IRON_AXE));
            drop(cow, new ItemStack(Items.IRON_PICKAXE));
            drop(cow, new ItemStack(Items.IRON_SHOVEL));
            drop(cow, new ItemStack(Items.IRON_HOE));

            drop(cow, new ItemStack(Items.SHIELD));

            drop(cow, new ItemStack(Items.IRON_HELMET));
            drop(cow, new ItemStack(Items.IRON_CHESTPLATE));
            drop(cow, new ItemStack(Items.IRON_LEGGINGS));
            drop(cow, new ItemStack(Items.IRON_BOOTS));
        }

        // PIG PREMIUM = HOGLIN
        if (PremiumEntityTags.isAnimal(entity, "pig")
                && entity instanceof Hoglin pig) {

            drop(pig, new ItemStack(Items.GOLD_BLOCK, 64));
            drop(pig, new ItemStack(Items.GOLD_BLOCK, 64));
            drop(pig, new ItemStack(Items.GOLD_BLOCK, 64));

            drop(pig, new ItemStack(Items.GOLDEN_SWORD));
            drop(pig, new ItemStack(Items.GOLDEN_AXE));
            drop(pig, new ItemStack(Items.GOLDEN_PICKAXE));
            drop(pig, new ItemStack(Items.GOLDEN_SHOVEL));
            drop(pig, new ItemStack(Items.GOLDEN_HOE));

            drop(pig, new ItemStack(Items.GOLDEN_HELMET));
            drop(pig, new ItemStack(Items.GOLDEN_CHESTPLATE));
            drop(pig, new ItemStack(Items.GOLDEN_LEGGINGS));
            drop(pig, new ItemStack(Items.GOLDEN_BOOTS));
        }

        // CHICKEN PREMIUM = PARROT
        if (PremiumEntityTags.isAnimal(entity, "chicken")
                && entity instanceof Parrot chicken) {

            drop(chicken, new ItemStack(Items.DIAMOND_BLOCK, 64));
            drop(chicken, new ItemStack(Items.DIAMOND_BLOCK, 64));
            drop(chicken, new ItemStack(Items.DIAMOND_BLOCK, 64));

            drop(chicken, new ItemStack(Items.DIAMOND_SWORD));
            drop(chicken, new ItemStack(Items.DIAMOND_AXE));
            drop(chicken, new ItemStack(Items.DIAMOND_PICKAXE));
            drop(chicken, new ItemStack(Items.DIAMOND_SHOVEL));
            drop(chicken, new ItemStack(Items.DIAMOND_HOE));

            drop(chicken, new ItemStack(Items.DIAMOND_HELMET));
            drop(chicken, new ItemStack(Items.DIAMOND_CHESTPLATE));
            drop(chicken, new ItemStack(Items.DIAMOND_LEGGINGS));
            drop(chicken, new ItemStack(Items.DIAMOND_BOOTS));
        }

        // SHEEP PREMIUM = LLAMA
        if (PremiumEntityTags.isAnimal(entity, "sheep")
                && entity instanceof Llama sheep) {

            drop(sheep, new ItemStack(Items.ANCIENT_DEBRIS, 64));
            drop(sheep, new ItemStack(Items.ANCIENT_DEBRIS, 64));
            drop(sheep, new ItemStack(Items.ANCIENT_DEBRIS, 64));

            drop(sheep, new ItemStack(Items.NETHERITE_SWORD));
            drop(sheep, new ItemStack(Items.NETHERITE_AXE));
            drop(sheep, new ItemStack(Items.NETHERITE_PICKAXE));
            drop(sheep, new ItemStack(Items.NETHERITE_SHOVEL));
            drop(sheep, new ItemStack(Items.NETHERITE_HOE));

            drop(sheep, new ItemStack(Items.NETHERITE_HELMET));
            drop(sheep, new ItemStack(Items.NETHERITE_CHESTPLATE));
            drop(sheep, new ItemStack(Items.NETHERITE_LEGGINGS));
            drop(sheep, new ItemStack(Items.NETHERITE_BOOTS));
        }

        // CAT PREMIUM = OCELOT
        if (PremiumEntityTags.isAnimal(entity, "cat")
                && entity instanceof Ocelot cat) {

            drop(cat, new ItemStack(Items.QUARTZ_BLOCK, 64));
            drop(cat, new ItemStack(Items.QUARTZ_BLOCK, 64));
            drop(cat, new ItemStack(Items.QUARTZ_BLOCK, 64));

            drop(cat, new ItemStack(Items.WHITE_CONCRETE, 64));
            drop(cat, new ItemStack(Items.WHITE_CONCRETE, 64));
            drop(cat, new ItemStack(Items.GLASS, 64));

            drop(cat, new ItemStack(Items.SEA_LANTERN, 64));
            drop(cat, new ItemStack(Items.LANTERN, 64));
        }

        // FOX PREMIUM
        if (PremiumEntityTags.isAnimal(entity, "fox")
                && entity instanceof Fox fox) {

            drop(fox, new ItemStack(Items.SPRUCE_LOG, 64));
            drop(fox, new ItemStack(Items.SPRUCE_LOG, 64));
            drop(fox, new ItemStack(Items.SPRUCE_LOG, 64));

            drop(fox, new ItemStack(Items.DARK_OAK_PLANKS, 64));
            drop(fox, new ItemStack(Items.DARK_OAK_PLANKS, 64));

            drop(fox, new ItemStack(Items.STONE_BRICKS, 64));
            drop(fox, new ItemStack(Items.STONE_BRICKS, 64));

            drop(fox, new ItemStack(Items.LANTERN, 64));
            drop(fox, new ItemStack(Items.IRON_CHAIN, 64));
        }

        // RABBIT PREMIUM
        if (PremiumEntityTags.isAnimal(entity, "rabbit")
                && entity instanceof Rabbit rabbit) {

            drop(rabbit, new ItemStack(Items.WHEAT_SEEDS, 64));
            drop(rabbit, new ItemStack(Items.CARROT, 64));
            drop(rabbit, new ItemStack(Items.POTATO, 64));
            drop(rabbit, new ItemStack(Items.BEETROOT_SEEDS, 64));

            drop(rabbit, new ItemStack(Items.BONE_MEAL, 64));
            drop(rabbit, new ItemStack(Items.HAY_BLOCK, 64));

            drop(rabbit, new ItemStack(Items.OAK_FENCE, 64));
        }

        // HORSE PREMIUM = ZOMBIE HORSE
        if (PremiumEntityTags.isAnimal(entity, "horse")
                && entity instanceof ZombieHorse horse) {

            drop(horse, new ItemStack(Items.REDSTONE, 64));
            drop(horse, new ItemStack(Items.OBSERVER, 64));
            drop(horse, new ItemStack(Items.PISTON, 64));
            drop(horse, new ItemStack(Items.STICKY_PISTON, 64));
            drop(horse, new ItemStack(Items.HOPPER, 64));
            drop(horse, new ItemStack(Items.REPEATER, 64));
            drop(horse, new ItemStack(Items.COMPARATOR, 64));
            drop(horse, new ItemStack(Items.SLIME_BLOCK, 64));
        }

        // GOAT PREMIUM
        if (PremiumEntityTags.isAnimal(entity, "goat")
                && entity instanceof Goat goat) {

            drop(goat, new ItemStack(Items.STONE_BRICKS, 64));
            drop(goat, new ItemStack(Items.STONE_BRICKS, 64));
            drop(goat, new ItemStack(Items.STONE_BRICKS, 64));

            drop(goat, new ItemStack(Items.COBBLESTONE, 64));
            drop(goat, new ItemStack(Items.COBBLESTONE, 64));
            drop(goat, new ItemStack(Items.COBBLESTONE, 64));

            drop(goat, new ItemStack(Items.DEEPSLATE, 64));
            drop(goat, new ItemStack(Items.LANTERN, 64));
            drop(goat, new ItemStack(Items.IRON_BARS, 64));
        }

        // BEE PREMIUM = ALLAY
        if (PremiumEntityTags.isAnimal(entity, "bee")
                && entity instanceof Allay bee) {

            drop(bee, new ItemStack(Items.HONEY_BLOCK, 64));
            drop(bee, new ItemStack(Items.HONEYCOMB_BLOCK, 64));
            drop(bee, new ItemStack(Items.MOSS_BLOCK, 64));

            drop(bee, new ItemStack(Items.OAK_SAPLING, 64));
            drop(bee, new ItemStack(Items.BIRCH_SAPLING, 64));

            drop(bee, new ItemStack(Items.SUNFLOWER, 64));
        }

        // AXOLOTL PREMIUM = FROG
        if (PremiumEntityTags.isAnimal(entity, "axolotl")
                && entity instanceof Frog axolotl) {

            drop(axolotl, new ItemStack(Items.PRISMARINE, 64));
            drop(axolotl, new ItemStack(Items.SEA_LANTERN, 64));
            drop(axolotl, new ItemStack(Items.GLASS, 64));

            drop(axolotl, new ItemStack(Items.TROPICAL_FISH, 64));
        }

        // CAMEL PREMIUM
        if (PremiumEntityTags.isAnimal(entity, "camel")
                && entity instanceof Camel camel) {

            drop(camel, new ItemStack(Items.SANDSTONE, 64));
            drop(camel, new ItemStack(Items.SANDSTONE, 64));
            drop(camel, new ItemStack(Items.SANDSTONE, 64));

            drop(camel, new ItemStack(Items.SMOOTH_SANDSTONE, 64));
            drop(camel, new ItemStack(Items.GLASS, 64));
            drop(camel, new ItemStack(Items.LANTERN, 64));
        }

        // FROG PREMIUM
        if (PremiumEntityTags.isAnimal(entity, "frog")
                && entity instanceof Frog frog) {

            drop(frog, new ItemStack(Items.MUD, 64));
            drop(frog, new ItemStack(Items.MANGROVE_LOG, 64));
            drop(frog, new ItemStack(Items.VINE, 64));

            drop(frog, new ItemStack(Items.LILY_PAD, 64));
            drop(frog, new ItemStack(Items.LANTERN, 64));
        }

        // TURTLE PREMIUM
        if (PremiumEntityTags.isAnimal(entity, "turtle")
                && entity instanceof Turtle turtle) {

            drop(turtle, new ItemStack(Items.PRISMARINE, 64));
            drop(turtle, new ItemStack(Items.SEA_LANTERN, 64));
            drop(turtle, new ItemStack(Items.GLASS, 64));

            drop(turtle, new ItemStack(Items.SAND, 64));
        }

        // SQUID PREMIUM
        if (PremiumEntityTags.isAnimal(entity, "squid")
                && entity instanceof Squid squid) {

            drop(squid, new ItemStack(Items.BLACK_CONCRETE, 64));
            drop(squid, new ItemStack(Items.BLACK_CONCRETE, 64));
            drop(squid, new ItemStack(Items.TINTED_GLASS, 64));

            drop(squid, new ItemStack(Items.OBSIDIAN, 64));
            drop(squid, new ItemStack(Items.INK_SAC, 64));
        }

        // BAT PREMIUM
        if (PremiumEntityTags.isAnimal(entity, "bat")
                && entity instanceof Bat bat) {

            drop(bat, new ItemStack(Items.COAL_BLOCK, 64));
            drop(bat, new ItemStack(Items.IRON_INGOT, 64));
            drop(bat, new ItemStack(Items.GOLD_INGOT, 64));

            drop(bat, new ItemStack(Items.DIAMOND, 32));
            drop(bat, new ItemStack(Items.EMERALD, 32));

            drop(bat, new ItemStack(Items.TORCH, 64));
        }

        // VILLAGER PREMIUM = WANDERING TRADER
        if (PremiumEntityTags.isAnimal(entity, "villager")
                && entity instanceof WanderingTrader villager) {

            drop(villager, new ItemStack(Items.EMERALD_BLOCK, 64));
            drop(villager, new ItemStack(Items.EMERALD_BLOCK, 64));

            drop(villager, new ItemStack(Items.BREAD, 64));
            drop(villager, new ItemStack(Items.BOOKSHELF, 64));

            drop(villager, new ItemStack(Items.ENCHANTING_TABLE));
            drop(villager, new ItemStack(Items.ANVIL));

            drop(villager, new ItemStack(Items.BLAST_FURNACE));
            drop(villager, new ItemStack(Items.SMITHING_TABLE));
        }

        // SNOW GOLEM PREMIUM
        if (PremiumEntityTags.isAnimal(entity, "snow_golem")
                && entity instanceof SnowGolem snow) {

            drop(snow, new ItemStack(Items.PACKED_ICE, 64));
            drop(snow, new ItemStack(Items.BLUE_ICE, 64));
            drop(snow, new ItemStack(Items.SNOW_BLOCK, 64));

            drop(snow, new ItemStack(Items.LANTERN, 64));
        }

        // IRON GOLEM PREMIUM
        if (PremiumEntityTags.isAnimal(entity, "iron_golem")
                && entity instanceof IronGolem golem) {

            drop(golem, new ItemStack(Items.IRON_BLOCK, 64));
            drop(golem, new ItemStack(Items.IRON_BLOCK, 64));
            drop(golem, new ItemStack(Items.IRON_BLOCK, 64));

            drop(golem, new ItemStack(Items.SHIELD));
            drop(golem, new ItemStack(Items.CROSSBOW));
            drop(golem, new ItemStack(Items.ARROW, 64));

            drop(golem, new ItemStack(Items.IRON_HELMET));
            drop(golem, new ItemStack(Items.IRON_CHESTPLATE));
            drop(golem, new ItemStack(Items.IRON_LEGGINGS));
            drop(golem, new ItemStack(Items.IRON_BOOTS));
        }

        // PANDA PREMIUM
        if (PremiumEntityTags.isAnimal(entity, "panda")
                && entity instanceof Panda panda) {

            drop(panda, new ItemStack(Items.BAMBOO_BLOCK, 64));
            drop(panda, new ItemStack(Items.BAMBOO_BLOCK, 64));
            drop(panda, new ItemStack(Items.BAMBOO, 64));

            drop(panda, new ItemStack(Items.LANTERN, 64));
            drop(panda, new ItemStack(Items.PAPER, 64));
            drop(panda, new ItemStack(Items.SCAFFOLDING, 64));

            drop(panda, new ItemStack(Items.CHERRY_LOG, 64));
        }

        // CREEPER PREMIUM
        if (PremiumEntityTags.isAnimal(entity, "creeper")
                && entity instanceof Creeper creeper) {

            drop(creeper, new ItemStack(Items.TNT, 64));
            drop(creeper, new ItemStack(Items.GUNPOWDER, 64));

            drop(creeper, new ItemStack(Items.FLINT_AND_STEEL));
            drop(creeper, new ItemStack(Items.FIRE_CHARGE, 64));
        }
    }

    private static void drop(
            Entity entity,
            ItemStack stack
    ) {

        if (entity.level() instanceof ServerLevel serverLevel) {
            entity.spawnAtLocation(
                    serverLevel,
                    stack
            );
        }
    }
}
