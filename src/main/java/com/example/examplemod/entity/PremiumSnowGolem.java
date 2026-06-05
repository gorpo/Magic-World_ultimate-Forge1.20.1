package com.example.examplemod.entity;

import com.example.examplemod.ExampleMod;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.animal.SnowGolem;
import net.minecraft.world.level.Level;

public class PremiumSnowGolem {

    public static void transform(
            Level level,
            SnowGolem golem
    ) {

        if (golem.hasEffect(
                MobEffects.REGENERATION
        )) {

            golem.removeEffect(
                    MobEffects.REGENERATION
            );
        }

        else {

            golem.addEffect(
                    new MobEffectInstance(
                            MobEffects.REGENERATION,
                            999999,
                            4
                    )
            );
        }

        ExampleMod.effects(
                (ServerLevel) level,
                golem.blockPosition()
        );
    }
}