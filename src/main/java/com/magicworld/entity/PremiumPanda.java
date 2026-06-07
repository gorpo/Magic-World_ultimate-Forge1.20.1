package com.magicworld.entity;

import com.magicworld.MagicWorld;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.animal.Panda;
import net.minecraft.world.level.Level;

public class PremiumPanda {

    public static void transform(
            Level level,
            Panda panda
    ) {

        if (panda.hasEffect(
                MobEffects.DAMAGE_BOOST
        )) {

            panda.removeEffect(
                    MobEffects.DAMAGE_BOOST
            );

            panda.removeEffect(
                    MobEffects.REGENERATION
            );
        }

        else {

            panda.addEffect(
                    new MobEffectInstance(
                            MobEffects.DAMAGE_BOOST,
                            999999,
                            4
                    )
            );

            panda.addEffect(
                    new MobEffectInstance(
                            MobEffects.REGENERATION,
                            999999,
                            4
                    )
            );
        }

        MagicWorld.effects(
                (ServerLevel) level,
                panda.blockPosition()
        );
    }
}