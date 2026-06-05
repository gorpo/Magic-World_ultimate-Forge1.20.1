package com.magicworld.event;

import com.magicworld.MagicWorld;
import com.magicworld.entity.*;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraft.world.entity.animal.*;
import net.minecraft.world.entity.animal.allay.Allay;
import net.minecraft.world.entity.animal.axolotl.Axolotl;
import net.minecraft.world.entity.animal.camel.Camel;
import net.minecraft.world.entity.animal.frog.Frog;
import net.minecraft.world.entity.animal.goat.Goat;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.entity.animal.horse.Llama;
import net.minecraft.world.entity.animal.horse.ZombieHorse;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.hoglin.Hoglin;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.WanderingTrader;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class MobEvents {

    @SubscribeEvent
    public void onLeftClickEntity(
            AttackEntityEvent event
    ) {

        Level level =
                event.getEntity().level();

        if (level.isClientSide()) return;

        ItemStack held =
                event.getEntity()
                        .getMainHandItem();

        if (held.getItem()
                != MagicWorld.VARINHA_MAGICA.get())
            return;

        event.setCanceled(true);

        var target =
                event.getTarget();

        if (event.getEntity().isShiftKeyDown()) {
            destroyTarget(
                    level,
                    target
            );

            return;
        }

        if (PremiumEnemies.transform(
                level,
                target
        )) {
            return;
        }

        // WOLF
        if (target instanceof Wolf wolf) {
            PremiumWolf.transform(level, wolf);
        }

        // COW
        else if (target instanceof Cow
                || target instanceof MushroomCow) {

            PremiumCow.transform(level, target);
        }

        // PIG
        else if (target instanceof Pig
                || target instanceof Hoglin) {

            PremiumPig.transform(level, target);
        }

        // CAT
        else if (target instanceof Cat
                || target instanceof Ocelot) {

            PremiumCat.transform(level, target);
        }

        // CHICKEN
        else if (target instanceof Chicken
                || target instanceof Parrot) {

            PremiumChicken.transform(level, target);
        }

        // FOX
        else if (target instanceof Fox) {

            PremiumFox.transform(
                    level,
                    (Fox) target
            );
        }

        // RABBIT
        else if (target instanceof Rabbit) {

            PremiumRabbit.transform(
                    level,
                    (Rabbit) target
            );
        }

        // GOAT
        else if (target instanceof Goat) {

            PremiumGoat.transform(
                    level,
                    (Goat) target
            );
        }

        // HORSE
        else if (target instanceof Horse
                || target instanceof ZombieHorse) {

            PremiumHorse.transform(level, target);
        }


        // SHEEP
        else if (target instanceof Sheep
                || target instanceof Llama) {

            PremiumSheep.transform(level, target);
        }

        // BEE
        else if (target instanceof Bee
                || target instanceof Allay) {

            PremiumBee.transform(level, target);
        }


        // AXOLOTL
        else if (target instanceof Axolotl axolotl) {
            PremiumAxolotl.transform(level, axolotl);
        }

        // CAMEL
        else if (target instanceof Camel camel) {
            PremiumCamel.transform(level, camel);
        }

        // FROG
        else if (target instanceof Frog frog) {
            PremiumFrog.transform(level, frog);
        }

        // TURTLE
        else if (target instanceof Turtle turtle) {
            PremiumTurtle.transform(level, turtle);
        }

        // SQUID
        else if (target instanceof Squid squid) {
            PremiumSquid.transform(level, squid);
        }

        // BAT
        else if (target instanceof Bat bat) {
            PremiumBat.transform(level, bat);
        }

        // VILLAGER
        else if (target instanceof Villager
                || target instanceof WanderingTrader) {

            PremiumVillager.transform(level, target);
        }

        // IRON GOLEM
        else if (target instanceof IronGolem golem) {
            PremiumIronGolem.transform(level, golem);
        }

        // SNOW GOLEM
        else if (target instanceof SnowGolem golem) {
            PremiumSnowGolem.transform(level, golem);
        }

        // PANDA
        else if (target instanceof Panda panda) {
            PremiumPanda.transform(level, panda);
        }

        // CREEPER
        else if (target instanceof Creeper creeper) {
            PremiumCreeper.transform(level, creeper);
        }
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

    @SubscribeEvent
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
        if (entity instanceof Wolf wolf
                && wolf.hasEffect(MobEffects.DAMAGE_BOOST)) {

            wolf.spawnAtLocation(new ItemStack(Items.OAK_LOG, 64));
            wolf.spawnAtLocation(new ItemStack(Items.OAK_LOG, 64));
            wolf.spawnAtLocation(new ItemStack(Items.OAK_LOG, 64));

            wolf.spawnAtLocation(new ItemStack(Items.OAK_PLANKS, 64));
            wolf.spawnAtLocation(new ItemStack(Items.OAK_PLANKS, 64));
            wolf.spawnAtLocation(new ItemStack(Items.OAK_PLANKS, 64));

            wolf.spawnAtLocation(new ItemStack(Items.GLASS, 64));
            wolf.spawnAtLocation(new ItemStack(Items.COBBLESTONE, 64));
            wolf.spawnAtLocation(new ItemStack(Items.TORCH, 64));
        }

        // COW PREMIUM = MOOSHROOM
        if (entity instanceof MushroomCow cow
                && cow.hasEffect(MobEffects.MOVEMENT_SPEED)) {

            cow.spawnAtLocation(new ItemStack(Items.IRON_BLOCK, 64));
            cow.spawnAtLocation(new ItemStack(Items.IRON_BLOCK, 64));
            cow.spawnAtLocation(new ItemStack(Items.IRON_BLOCK, 64));

            cow.spawnAtLocation(new ItemStack(Items.IRON_SWORD));
            cow.spawnAtLocation(new ItemStack(Items.IRON_AXE));
            cow.spawnAtLocation(new ItemStack(Items.IRON_PICKAXE));
            cow.spawnAtLocation(new ItemStack(Items.IRON_SHOVEL));
            cow.spawnAtLocation(new ItemStack(Items.IRON_HOE));

            cow.spawnAtLocation(new ItemStack(Items.SHIELD));

            cow.spawnAtLocation(new ItemStack(Items.IRON_HELMET));
            cow.spawnAtLocation(new ItemStack(Items.IRON_CHESTPLATE));
            cow.spawnAtLocation(new ItemStack(Items.IRON_LEGGINGS));
            cow.spawnAtLocation(new ItemStack(Items.IRON_BOOTS));
        }

        // PIG PREMIUM = HOGLIN
        if (entity instanceof Hoglin pig
                && pig.hasEffect(MobEffects.REGENERATION)) {

            pig.spawnAtLocation(new ItemStack(Items.GOLD_BLOCK, 64));
            pig.spawnAtLocation(new ItemStack(Items.GOLD_BLOCK, 64));
            pig.spawnAtLocation(new ItemStack(Items.GOLD_BLOCK, 64));

            pig.spawnAtLocation(new ItemStack(Items.GOLDEN_SWORD));
            pig.spawnAtLocation(new ItemStack(Items.GOLDEN_AXE));
            pig.spawnAtLocation(new ItemStack(Items.GOLDEN_PICKAXE));
            pig.spawnAtLocation(new ItemStack(Items.GOLDEN_SHOVEL));
            pig.spawnAtLocation(new ItemStack(Items.GOLDEN_HOE));

            pig.spawnAtLocation(new ItemStack(Items.GOLDEN_HELMET));
            pig.spawnAtLocation(new ItemStack(Items.GOLDEN_CHESTPLATE));
            pig.spawnAtLocation(new ItemStack(Items.GOLDEN_LEGGINGS));
            pig.spawnAtLocation(new ItemStack(Items.GOLDEN_BOOTS));
        }

        // CHICKEN PREMIUM = PARROT
        if (entity instanceof Parrot chicken
                && chicken.hasEffect(MobEffects.MOVEMENT_SPEED)) {

            chicken.spawnAtLocation(new ItemStack(Items.DIAMOND_BLOCK, 64));
            chicken.spawnAtLocation(new ItemStack(Items.DIAMOND_BLOCK, 64));
            chicken.spawnAtLocation(new ItemStack(Items.DIAMOND_BLOCK, 64));

            chicken.spawnAtLocation(new ItemStack(Items.DIAMOND_SWORD));
            chicken.spawnAtLocation(new ItemStack(Items.DIAMOND_AXE));
            chicken.spawnAtLocation(new ItemStack(Items.DIAMOND_PICKAXE));
            chicken.spawnAtLocation(new ItemStack(Items.DIAMOND_SHOVEL));
            chicken.spawnAtLocation(new ItemStack(Items.DIAMOND_HOE));

            chicken.spawnAtLocation(new ItemStack(Items.DIAMOND_HELMET));
            chicken.spawnAtLocation(new ItemStack(Items.DIAMOND_CHESTPLATE));
            chicken.spawnAtLocation(new ItemStack(Items.DIAMOND_LEGGINGS));
            chicken.spawnAtLocation(new ItemStack(Items.DIAMOND_BOOTS));
        }

        // SHEEP PREMIUM = LLAMA
        if (entity instanceof Llama sheep
                && sheep.hasEffect(MobEffects.MOVEMENT_SPEED)) {

            sheep.spawnAtLocation(new ItemStack(Items.ANCIENT_DEBRIS, 64));
            sheep.spawnAtLocation(new ItemStack(Items.ANCIENT_DEBRIS, 64));
            sheep.spawnAtLocation(new ItemStack(Items.ANCIENT_DEBRIS, 64));

            sheep.spawnAtLocation(new ItemStack(Items.NETHERITE_SWORD));
            sheep.spawnAtLocation(new ItemStack(Items.NETHERITE_AXE));
            sheep.spawnAtLocation(new ItemStack(Items.NETHERITE_PICKAXE));
            sheep.spawnAtLocation(new ItemStack(Items.NETHERITE_SHOVEL));
            sheep.spawnAtLocation(new ItemStack(Items.NETHERITE_HOE));

            sheep.spawnAtLocation(new ItemStack(Items.NETHERITE_HELMET));
            sheep.spawnAtLocation(new ItemStack(Items.NETHERITE_CHESTPLATE));
            sheep.spawnAtLocation(new ItemStack(Items.NETHERITE_LEGGINGS));
            sheep.spawnAtLocation(new ItemStack(Items.NETHERITE_BOOTS));
        }

        // CAT PREMIUM = OCELOT
        if (entity instanceof Ocelot cat
                && cat.hasEffect(MobEffects.MOVEMENT_SPEED)) {

            cat.spawnAtLocation(new ItemStack(Items.QUARTZ_BLOCK, 64));
            cat.spawnAtLocation(new ItemStack(Items.QUARTZ_BLOCK, 64));
            cat.spawnAtLocation(new ItemStack(Items.QUARTZ_BLOCK, 64));

            cat.spawnAtLocation(new ItemStack(Items.WHITE_CONCRETE, 64));
            cat.spawnAtLocation(new ItemStack(Items.WHITE_CONCRETE, 64));
            cat.spawnAtLocation(new ItemStack(Items.GLASS, 64));

            cat.spawnAtLocation(new ItemStack(Items.SEA_LANTERN, 64));
            cat.spawnAtLocation(new ItemStack(Items.LANTERN, 64));
        }

// FOX PREMIUM
        if (entity instanceof Fox fox
                && fox.hasEffect(MobEffects.MOVEMENT_SPEED)) {

            fox.spawnAtLocation(new ItemStack(Items.SPRUCE_LOG, 64));
            fox.spawnAtLocation(new ItemStack(Items.SPRUCE_LOG, 64));
            fox.spawnAtLocation(new ItemStack(Items.SPRUCE_LOG, 64));

            fox.spawnAtLocation(new ItemStack(Items.DARK_OAK_PLANKS, 64));
            fox.spawnAtLocation(new ItemStack(Items.DARK_OAK_PLANKS, 64));

            fox.spawnAtLocation(new ItemStack(Items.STONE_BRICKS, 64));
            fox.spawnAtLocation(new ItemStack(Items.STONE_BRICKS, 64));

            fox.spawnAtLocation(new ItemStack(Items.LANTERN, 64));
            fox.spawnAtLocation(new ItemStack(Items.CHAIN, 64));
        }

// RABBIT PREMIUM
        if (entity instanceof Rabbit rabbit
                && rabbit.hasEffect(MobEffects.MOVEMENT_SPEED)) {

            rabbit.spawnAtLocation(new ItemStack(Items.WHEAT_SEEDS, 64));
            rabbit.spawnAtLocation(new ItemStack(Items.CARROT, 64));
            rabbit.spawnAtLocation(new ItemStack(Items.POTATO, 64));
            rabbit.spawnAtLocation(new ItemStack(Items.BEETROOT_SEEDS, 64));

            rabbit.spawnAtLocation(new ItemStack(Items.BONE_MEAL, 64));
            rabbit.spawnAtLocation(new ItemStack(Items.HAY_BLOCK, 64));

            rabbit.spawnAtLocation(new ItemStack(Items.OAK_FENCE, 64));
        }

// HORSE PREMIUM = ZOMBIE HORSE
        if (entity instanceof ZombieHorse horse
                && horse.hasEffect(MobEffects.MOVEMENT_SPEED)) {

            horse.spawnAtLocation(new ItemStack(Items.REDSTONE, 64));
            horse.spawnAtLocation(new ItemStack(Items.OBSERVER, 64));
            horse.spawnAtLocation(new ItemStack(Items.PISTON, 64));
            horse.spawnAtLocation(new ItemStack(Items.STICKY_PISTON, 64));
            horse.spawnAtLocation(new ItemStack(Items.HOPPER, 64));
            horse.spawnAtLocation(new ItemStack(Items.REPEATER, 64));
            horse.spawnAtLocation(new ItemStack(Items.COMPARATOR, 64));
            horse.spawnAtLocation(new ItemStack(Items.SLIME_BLOCK, 64));
        }

        // GOAT PREMIUM
        if (entity instanceof Goat goat
                && goat.hasEffect(MobEffects.DAMAGE_BOOST)) {

            goat.spawnAtLocation(new ItemStack(Items.STONE_BRICKS, 64));
            goat.spawnAtLocation(new ItemStack(Items.STONE_BRICKS, 64));
            goat.spawnAtLocation(new ItemStack(Items.STONE_BRICKS, 64));

            goat.spawnAtLocation(new ItemStack(Items.COBBLESTONE, 64));
            goat.spawnAtLocation(new ItemStack(Items.COBBLESTONE, 64));
            goat.spawnAtLocation(new ItemStack(Items.COBBLESTONE, 64));

            goat.spawnAtLocation(new ItemStack(Items.DEEPSLATE, 64));
            goat.spawnAtLocation(new ItemStack(Items.LANTERN, 64));
            goat.spawnAtLocation(new ItemStack(Items.IRON_BARS, 64));
        }

        // BEE PREMIUM = ALLAY
        if (entity instanceof Allay bee) {

            bee.spawnAtLocation(new ItemStack(Items.HONEY_BLOCK, 64));
            bee.spawnAtLocation(new ItemStack(Items.HONEYCOMB_BLOCK, 64));
            bee.spawnAtLocation(new ItemStack(Items.MOSS_BLOCK, 64));

            bee.spawnAtLocation(new ItemStack(Items.OAK_SAPLING, 64));
            bee.spawnAtLocation(new ItemStack(Items.BIRCH_SAPLING, 64));

            bee.spawnAtLocation(new ItemStack(Items.SUNFLOWER, 64));
        }

        // AXOLOTL PREMIUM = FROG
        if (entity instanceof Frog axolotl) {

            axolotl.spawnAtLocation(new ItemStack(Items.PRISMARINE, 64));
            axolotl.spawnAtLocation(new ItemStack(Items.SEA_LANTERN, 64));
            axolotl.spawnAtLocation(new ItemStack(Items.GLASS, 64));

            axolotl.spawnAtLocation(new ItemStack(Items.TROPICAL_FISH, 64));
        }

        // CAMEL PREMIUM
        if (entity instanceof Camel camel
                && camel.hasEffect(MobEffects.MOVEMENT_SPEED)) {

            camel.spawnAtLocation(new ItemStack(Items.SANDSTONE, 64));
            camel.spawnAtLocation(new ItemStack(Items.SANDSTONE, 64));
            camel.spawnAtLocation(new ItemStack(Items.SANDSTONE, 64));

            camel.spawnAtLocation(new ItemStack(Items.SMOOTH_SANDSTONE, 64));
            camel.spawnAtLocation(new ItemStack(Items.GLASS, 64));
            camel.spawnAtLocation(new ItemStack(Items.LANTERN, 64));
        }

        // FROG PREMIUM
        if (entity instanceof Frog frog
                && frog.hasEffect(MobEffects.JUMP)) {

            frog.spawnAtLocation(new ItemStack(Items.MUD, 64));
            frog.spawnAtLocation(new ItemStack(Items.MANGROVE_LOG, 64));
            frog.spawnAtLocation(new ItemStack(Items.VINE, 64));

            frog.spawnAtLocation(new ItemStack(Items.LILY_PAD, 64));
            frog.spawnAtLocation(new ItemStack(Items.LANTERN, 64));
        }

        // TURTLE PREMIUM
        if (entity instanceof Turtle turtle
                && turtle.hasEffect(MobEffects.DAMAGE_RESISTANCE)) {

            turtle.spawnAtLocation(new ItemStack(Items.PRISMARINE, 64));
            turtle.spawnAtLocation(new ItemStack(Items.SEA_LANTERN, 64));
            turtle.spawnAtLocation(new ItemStack(Items.GLASS, 64));

            turtle.spawnAtLocation(new ItemStack(Items.SAND, 64));
        }


        // SQUID PREMIUM
        if (entity instanceof Squid squid
                && squid.hasEffect(MobEffects.DOLPHINS_GRACE)) {

            squid.spawnAtLocation(new ItemStack(Items.BLACK_CONCRETE, 64));
            squid.spawnAtLocation(new ItemStack(Items.BLACK_CONCRETE, 64));
            squid.spawnAtLocation(new ItemStack(Items.TINTED_GLASS, 64));

            squid.spawnAtLocation(new ItemStack(Items.OBSIDIAN, 64));
            squid.spawnAtLocation(new ItemStack(Items.INK_SAC, 64));
        }

        // BAT PREMIUM
        if (entity instanceof Bat bat
                && bat.hasEffect(MobEffects.MOVEMENT_SPEED)) {

            bat.spawnAtLocation(new ItemStack(Items.COAL_BLOCK, 64));
            bat.spawnAtLocation(new ItemStack(Items.IRON_INGOT, 64));
            bat.spawnAtLocation(new ItemStack(Items.GOLD_INGOT, 64));

            bat.spawnAtLocation(new ItemStack(Items.DIAMOND, 32));
            bat.spawnAtLocation(new ItemStack(Items.EMERALD, 32));

            bat.spawnAtLocation(new ItemStack(Items.TORCH, 64));
        }

        // VILLAGER PREMIUM = WANDERING TRADER
        if (entity instanceof WanderingTrader villager) {

            villager.spawnAtLocation(new ItemStack(Items.EMERALD_BLOCK, 64));
            villager.spawnAtLocation(new ItemStack(Items.EMERALD_BLOCK, 64));

            villager.spawnAtLocation(new ItemStack(Items.BREAD, 64));
            villager.spawnAtLocation(new ItemStack(Items.BOOKSHELF, 64));

            villager.spawnAtLocation(new ItemStack(Items.ENCHANTING_TABLE));
            villager.spawnAtLocation(new ItemStack(Items.ANVIL));

            villager.spawnAtLocation(new ItemStack(Items.BLAST_FURNACE));
            villager.spawnAtLocation(new ItemStack(Items.SMITHING_TABLE));
        }

        // SNOW GOLEM PREMIUM
        if (entity instanceof SnowGolem snow
                && snow.hasEffect(MobEffects.REGENERATION)) {

            snow.spawnAtLocation(new ItemStack(Items.PACKED_ICE, 64));
            snow.spawnAtLocation(new ItemStack(Items.BLUE_ICE, 64));
            snow.spawnAtLocation(new ItemStack(Items.SNOW_BLOCK, 64));

            snow.spawnAtLocation(new ItemStack(Items.LANTERN, 64));
        }

        // IRON GOLEM PREMIUM
        if (entity instanceof IronGolem golem
                && golem.hasEffect(MobEffects.DAMAGE_BOOST)) {

            golem.spawnAtLocation(new ItemStack(Items.IRON_BLOCK, 64));
            golem.spawnAtLocation(new ItemStack(Items.IRON_BLOCK, 64));
            golem.spawnAtLocation(new ItemStack(Items.IRON_BLOCK, 64));

            golem.spawnAtLocation(new ItemStack(Items.SHIELD));
            golem.spawnAtLocation(new ItemStack(Items.CROSSBOW));
            golem.spawnAtLocation(new ItemStack(Items.ARROW, 64));

            golem.spawnAtLocation(new ItemStack(Items.IRON_HELMET));
            golem.spawnAtLocation(new ItemStack(Items.IRON_CHESTPLATE));
            golem.spawnAtLocation(new ItemStack(Items.IRON_LEGGINGS));
            golem.spawnAtLocation(new ItemStack(Items.IRON_BOOTS));
        }

        // PANDA PREMIUM
        if (entity instanceof Panda panda
                && panda.hasEffect(MobEffects.DAMAGE_BOOST)) {

            panda.spawnAtLocation(new ItemStack(Items.BAMBOO_BLOCK, 64));
            panda.spawnAtLocation(new ItemStack(Items.BAMBOO_BLOCK, 64));
            panda.spawnAtLocation(new ItemStack(Items.BAMBOO, 64));

            panda.spawnAtLocation(new ItemStack(Items.LANTERN, 64));
            panda.spawnAtLocation(new ItemStack(Items.PAPER, 64));
            panda.spawnAtLocation(new ItemStack(Items.SCAFFOLDING, 64));

            panda.spawnAtLocation(new ItemStack(Items.CHERRY_LOG, 64));
        }

        // CREEPER PREMIUM
        if (entity instanceof Creeper creeper
                && creeper.hasEffect(MobEffects.MOVEMENT_SPEED)) {

            creeper.spawnAtLocation(new ItemStack(Items.TNT, 64));
            creeper.spawnAtLocation(new ItemStack(Items.GUNPOWDER, 64));

            creeper.spawnAtLocation(new ItemStack(Items.FLINT_AND_STEEL));
            creeper.spawnAtLocation(new ItemStack(Items.FIRE_CHARGE, 64));
        }


        //----------------------------END CODE------------------------------------------------>>
    }
}
