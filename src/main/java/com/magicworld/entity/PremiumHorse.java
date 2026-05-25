package com.magicworld.entity;

import com.magicworld.MagicWorld;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.equine.Horse;
import net.minecraft.world.entity.animal.equine.ZombieHorse;
import net.minecraft.world.level.Level;

public class PremiumHorse {

    public static boolean transform(
            Level level,
            Object target
    ) {

        if (target instanceof Horse horse) {

            ZombieHorse premium =
                    new ZombieHorse(
                            EntityType.ZOMBIE_HORSE,
                            level
                    );

            premium.setPos(
                    horse.getX(),
                    horse.getY(),
                    horse.getZ()
            );

            premium.addEffect(
                    new MobEffectInstance(
                            MobEffects.SPEED,
                            999999,
                            3
                    )
            );

            PremiumEntityTags.markAnimal(premium, "horse");
            level.addFreshEntity(
                    premium
            );

            horse.discard();

            MagicWorld.effects(
                    (ServerLevel) level,
                    horse.blockPosition()
            );

            return true;
        }

        else if (target instanceof ZombieHorse horse
                && PremiumEntityTags.isAnimal(horse, "horse")) {

            Horse normal =
                    new Horse(
                            EntityType.HORSE,
                            level
                    );

            normal.setPos(
                    horse.getX(),
                    horse.getY(),
                    horse.getZ()
            );

            level.addFreshEntity(
                    normal
            );

            horse.discard();

            MagicWorld.effects(
                    (ServerLevel) level,
                    horse.blockPosition()
            );

            return true;
        }

        return false;
    }
}
