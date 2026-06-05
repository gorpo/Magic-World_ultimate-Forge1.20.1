package com.example.examplemod.entity;

import com.example.examplemod.ExampleMod;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.animal.Turtle;
import net.minecraft.world.level.Level;

public class PremiumTurtle {

    public static void transform(
            Level level,
            Turtle turtle
    ) {

        if (turtle.hasEffect(
                MobEffects.DAMAGE_RESISTANCE
        )) {

            turtle.removeEffect(
                    MobEffects.DAMAGE_RESISTANCE
            );

            turtle.removeEffect(
                    MobEffects.REGENERATION
            );
        }

        else {

            turtle.addEffect(
                    new MobEffectInstance(
                            MobEffects.DAMAGE_RESISTANCE,
                            999999,
                            4
                    )
            );

            turtle.addEffect(
                    new MobEffectInstance(
                            MobEffects.REGENERATION,
                            999999,
                            3
                    )
            );
        }

        ExampleMod.effects(
                (ServerLevel) level,
                turtle.blockPosition()
        );
    }
}