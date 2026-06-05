package com.magicworld.entity;

import com.magicworld.MagicWorld;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.level.Level;

public class PremiumIronGolem {

    public static void transform(
            Level level,
            IronGolem golem
    ) {

        if (golem.hasEffect(
                MobEffects.DAMAGE_BOOST
        )) {

            golem.removeEffect(
                    MobEffects.DAMAGE_BOOST
            );

            golem.removeEffect(
                    MobEffects.MOVEMENT_SPEED
            );
        }

        else {

            golem.addEffect(
                    new MobEffectInstance(
                            MobEffects.DAMAGE_BOOST,
                            999999,
                            5
                    )
            );

            golem.addEffect(
                    new MobEffectInstance(
                            MobEffects.MOVEMENT_SPEED,
                            999999,
                            3
                    )
            );
        }

        MagicWorld.effects(
                (ServerLevel) level,
                golem.blockPosition()
        );
    }
}