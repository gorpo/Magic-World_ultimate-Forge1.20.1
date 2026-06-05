package com.magicworld.entity;

import com.magicworld.MagicWorld;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.level.Level;

public class PremiumCreeper {

    public static void transform(
            Level level,
            Creeper creeper
    ) {

        if (creeper.hasEffect(
                MobEffects.MOVEMENT_SPEED
        )) {

            creeper.removeEffect(
                    MobEffects.MOVEMENT_SPEED
            );

            creeper.removeEffect(
                    MobEffects.DAMAGE_BOOST
            );
        }

        else {

            creeper.addEffect(
                    new MobEffectInstance(
                            MobEffects.MOVEMENT_SPEED,
                            999999,
                            4
                    )
            );

            creeper.addEffect(
                    new MobEffectInstance(
                            MobEffects.DAMAGE_BOOST,
                            999999,
                            4
                    )
            );
        }

        MagicWorld.effects(
                (ServerLevel) level,
                creeper.blockPosition()
        );
    }
}