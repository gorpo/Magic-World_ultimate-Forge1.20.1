package com.example.examplemod.entity;

import com.example.examplemod.ExampleMod;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.entity.animal.horse.ZombieHorse;
import net.minecraft.world.level.Level;

public class PremiumHorse {

    public static void transform(
            Level level,
            Object target
    ) {

        if (target instanceof Horse horse) {

            ZombieHorse premium =
                    new ZombieHorse(
                            EntityType.ZOMBIE_HORSE,
                            level
                    );

            premium.moveTo(
                    horse.getX(),
                    horse.getY(),
                    horse.getZ()
            );

            premium.addEffect(
                    new MobEffectInstance(
                            MobEffects.MOVEMENT_SPEED,
                            999999,
                            3
                    )
            );

            level.addFreshEntity(
                    premium
            );

            horse.discard();

            ExampleMod.effects(
                    (ServerLevel) level,
                    horse.blockPosition()
            );
        }

        else if (target instanceof ZombieHorse horse) {

            Horse normal =
                    new Horse(
                            EntityType.HORSE,
                            level
                    );

            normal.moveTo(
                    horse.getX(),
                    horse.getY(),
                    horse.getZ()
            );

            level.addFreshEntity(
                    normal
            );

            horse.discard();

            ExampleMod.effects(
                    (ServerLevel) level,
                    horse.blockPosition()
            );
        }
    }
}