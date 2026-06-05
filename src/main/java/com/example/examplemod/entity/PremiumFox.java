package com.example.examplemod.entity;

import com.example.examplemod.ExampleMod;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.animal.Fox;
import net.minecraft.world.level.Level;

public class PremiumFox {

    public static void transform(
            Level level,
            Fox fox
    ) {

        if (!fox.hasEffect(
                MobEffects.MOVEMENT_SPEED
        )) {

            fox.addEffect(
                    new MobEffectInstance(
                            MobEffects.MOVEMENT_SPEED,
                            999999,
                            3
                    )
            );

            fox.addEffect(
                    new MobEffectInstance(
                            MobEffects.REGENERATION,
                            999999,
                            2
                    )
            );
        }

        else {

            fox.removeEffect(
                    MobEffects.MOVEMENT_SPEED
            );

            fox.removeEffect(
                    MobEffects.REGENERATION
            );
        }

        ExampleMod.effects(
                (ServerLevel) level,
                fox.blockPosition()
        );
    }
}