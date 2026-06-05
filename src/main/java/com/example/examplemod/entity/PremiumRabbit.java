package com.example.examplemod.entity;

import com.example.examplemod.ExampleMod;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.animal.Rabbit;
import net.minecraft.world.level.Level;

public class PremiumRabbit {

    public static void transform(
            Level level,
            Rabbit rabbit
    ) {

        if (rabbit.hasEffect(
                MobEffects.MOVEMENT_SPEED
        )) {

            rabbit.removeEffect(
                    MobEffects.MOVEMENT_SPEED
            );

            rabbit.removeEffect(
                    MobEffects.REGENERATION
            );
        }

        else {

            rabbit.addEffect(
                    new MobEffectInstance(
                            MobEffects.MOVEMENT_SPEED,
                            999999,
                            3
                    )
            );

            rabbit.addEffect(
                    new MobEffectInstance(
                            MobEffects.REGENERATION,
                            999999,
                            2
                    )
            );
        }

        ExampleMod.effects(
                (ServerLevel) level,
                rabbit.blockPosition()
        );
    }
}