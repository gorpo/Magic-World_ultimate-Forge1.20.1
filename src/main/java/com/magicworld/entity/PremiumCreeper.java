package com.magicworld.entity;

import com.magicworld.MagicWorld;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.level.Level;

public class PremiumCreeper {

    public static boolean transform(
            Level level,
            Creeper creeper
    ) {

        if (PremiumEntityTags.isAnimal(creeper, "creeper")) {
            PremiumEntityTags.clearAnimal(creeper, "creeper");

            creeper.removeEffect(
                    MobEffects.SPEED
            );

            creeper.removeEffect(
                    MobEffects.STRENGTH
            );
        }

        else {
            PremiumEntityTags.markAnimal(creeper, "creeper");

            creeper.addEffect(
                    new MobEffectInstance(
                            MobEffects.SPEED,
                            999999,
                            4
                    )
            );

            creeper.addEffect(
                    new MobEffectInstance(
                            MobEffects.STRENGTH,
                            999999,
                            4
                    )
            );
        }

        MagicWorld.effects(
                (ServerLevel) level,
                creeper.blockPosition()
        );

        return true;
    }
}
