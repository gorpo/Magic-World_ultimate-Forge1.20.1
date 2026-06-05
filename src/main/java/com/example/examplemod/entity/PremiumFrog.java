package com.example.examplemod.entity;

import com.example.examplemod.ExampleMod;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.animal.frog.Frog;
import net.minecraft.world.level.Level;

public class PremiumFrog {

    public static void transform(
            Level level,
            Frog frog
    ) {

        if (frog.hasEffect(
                MobEffects.JUMP
        )) {

            frog.removeEffect(
                    MobEffects.JUMP
            );

            frog.removeEffect(
                    MobEffects.MOVEMENT_SPEED
            );
        }

        else {

            frog.addEffect(
                    new MobEffectInstance(
                            MobEffects.JUMP,
                            999999,
                            5
                    )
            );

            frog.addEffect(
                    new MobEffectInstance(
                            MobEffects.MOVEMENT_SPEED,
                            999999,
                            2
                    )
            );
        }

        ExampleMod.effects(
                (ServerLevel) level,
                frog.blockPosition()
        );
    }
}