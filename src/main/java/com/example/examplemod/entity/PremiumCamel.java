package com.example.examplemod.entity;

import com.example.examplemod.ExampleMod;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.animal.camel.Camel;
import net.minecraft.world.level.Level;

public class PremiumCamel {

    public static void transform(
            Level level,
            Camel camel
    ) {

        if (camel.hasEffect(
                MobEffects.MOVEMENT_SPEED
        )) {

            camel.removeEffect(
                    MobEffects.MOVEMENT_SPEED
            );

            camel.removeEffect(
                    MobEffects.REGENERATION
            );
        }

        else {

            camel.addEffect(
                    new MobEffectInstance(
                            MobEffects.MOVEMENT_SPEED,
                            999999,
                            4
                    )
            );

            camel.addEffect(
                    new MobEffectInstance(
                            MobEffects.REGENERATION,
                            999999,
                            2
                    )
            );
        }

        ExampleMod.effects(
                (ServerLevel) level,
                camel.blockPosition()
        );
    }
}