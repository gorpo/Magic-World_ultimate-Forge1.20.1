package com.example.examplemod.entity;

import com.example.examplemod.ExampleMod;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.level.Level;

public class PremiumWolf {

    public static void transform(
            Level level,
            Wolf wolf
    ) {

        // premium → normal
        if (wolf.hasEffect(MobEffects.DAMAGE_BOOST)
                || wolf.hasEffect(MobEffects.REGENERATION)
                || wolf.hasEffect(MobEffects.MOVEMENT_SPEED)) {

            wolf.removeEffect(
                    MobEffects.DAMAGE_BOOST
            );

            wolf.removeEffect(
                    MobEffects.REGENERATION
            );

            wolf.removeEffect(
                    MobEffects.MOVEMENT_SPEED
            );

            wolf.removeEffect(
                    MobEffects.FIRE_RESISTANCE
            );

            ExampleMod.effects(
                    (ServerLevel) level,
                    wolf.blockPosition()
            );
        }

        // normal → premium
        else {

            wolf.setHealth(
                    wolf.getMaxHealth()
            );

            wolf.setTame(true);

            wolf.addEffect(
                    new MobEffectInstance(
                            MobEffects.MOVEMENT_SPEED,
                            999999,
                            2
                    )
            );

            wolf.addEffect(
                    new MobEffectInstance(
                            MobEffects.DAMAGE_BOOST,
                            999999,
                            2
                    )
            );

            wolf.addEffect(
                    new MobEffectInstance(
                            MobEffects.REGENERATION,
                            999999,
                            2
                    )
            );

            wolf.addEffect(
                    new MobEffectInstance(
                            MobEffects.FIRE_RESISTANCE,
                            999999,
                            1
                    )
            );

            ExampleMod.effects(
                    (ServerLevel) level,
                    wolf.blockPosition()
            );
        }
    }
}