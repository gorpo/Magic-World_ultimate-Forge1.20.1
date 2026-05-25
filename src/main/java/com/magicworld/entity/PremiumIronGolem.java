package com.magicworld.entity;

import com.magicworld.MagicWorld;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.animal.golem.IronGolem;
import net.minecraft.world.level.Level;

public class PremiumIronGolem {

    public static boolean transform(
            Level level,
            IronGolem golem
    ) {

        if (PremiumEntityTags.isAnimal(golem, "iron_golem")) {
            PremiumEntityTags.clearAnimal(golem, "iron_golem");

            golem.removeEffect(
                    MobEffects.STRENGTH
            );

            golem.removeEffect(
                    MobEffects.SPEED
            );
        }

        else {
            PremiumEntityTags.markAnimal(golem, "iron_golem");

            golem.addEffect(
                    new MobEffectInstance(
                            MobEffects.STRENGTH,
                            999999,
                            5
                    )
            );

            golem.addEffect(
                    new MobEffectInstance(
                            MobEffects.SPEED,
                            999999,
                            3
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
