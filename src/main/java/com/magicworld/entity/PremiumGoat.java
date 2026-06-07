package com.magicworld.entity;

import com.magicworld.MagicWorld;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.animal.goat.Goat;
import net.minecraft.world.level.Level;

public class PremiumGoat {

    public static void transform(
            Level level,
            Goat goat
    ) {

        if (goat.hasEffect(
                MobEffects.DAMAGE_BOOST
        )) {

            goat.removeEffect(
                    MobEffects.DAMAGE_BOOST
            );

            goat.removeEffect(
                    MobEffects.MOVEMENT_SPEED
            );
        }

        else {

            goat.addEffect(
                    new MobEffectInstance(
                            MobEffects.DAMAGE_BOOST,
                            999999,
                            3
                    )
            );

            goat.addEffect(
                    new MobEffectInstance(
                            MobEffects.MOVEMENT_SPEED,
                            999999,
                            2
                    )
            );

            MagicWorld.effects(
                    (ServerLevel) level,
                    goat.blockPosition()
            );
        }
    }
}