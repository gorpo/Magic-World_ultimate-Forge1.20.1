package com.magicworld.entity;

import com.magicworld.MagicWorld;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.animal.golem.SnowGolem;
import net.minecraft.world.level.Level;

public class PremiumSnowGolem {

    public static boolean transform(
            Level level,
            SnowGolem golem
    ) {

        if (PremiumEntityTags.isAnimal(golem, "snow_golem")) {
            PremiumEntityTags.clearAnimal(golem, "snow_golem");

            golem.removeEffect(
                    MobEffects.REGENERATION
            );
        }

        else {
            PremiumEntityTags.markAnimal(golem, "snow_golem");

            golem.addEffect(
                    new MobEffectInstance(
                            MobEffects.REGENERATION,
                            999999,
                            4
                    )
            );
        }

        MagicWorld.effects(
                (ServerLevel) level,
                golem.blockPosition()
        );

        return true;
    }
}
