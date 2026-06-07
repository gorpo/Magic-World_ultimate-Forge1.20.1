package com.magicworld.entity;

import com.magicworld.MagicWorld;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraft.world.level.Level;

public class PremiumBat {

    public static void transform(
            Level level,
            Bat bat
    ) {

        if (bat.hasEffect(
                MobEffects.MOVEMENT_SPEED
        )) {

            bat.removeEffect(
                    MobEffects.MOVEMENT_SPEED
            );

            bat.removeEffect(
                    MobEffects.REGENERATION
            );
        }

        else {

            bat.addEffect(
                    new MobEffectInstance(
                            MobEffects.MOVEMENT_SPEED,
                            999999,
                            5
                    )
            );

            bat.addEffect(
                    new MobEffectInstance(
                            MobEffects.REGENERATION,
                            999999,
                            3
                    )
            );
        }

        MagicWorld.effects(
                (ServerLevel) level,
                bat.blockPosition()
        );
    }
}