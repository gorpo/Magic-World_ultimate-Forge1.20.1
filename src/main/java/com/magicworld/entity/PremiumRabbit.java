package com.magicworld.entity;

import com.magicworld.MagicWorld;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.animal.rabbit.Rabbit;
import net.minecraft.world.level.Level;

public class PremiumRabbit {

    public static boolean transform(
            Level level,
            Rabbit rabbit
    ) {

        if (PremiumEntityTags.isAnimal(rabbit, "rabbit")) {
            PremiumEntityTags.clearAnimal(rabbit, "rabbit");

            rabbit.removeEffect(
                    MobEffects.SPEED
            );

            rabbit.removeEffect(
                    MobEffects.REGENERATION
            );
        }

        else {
            PremiumEntityTags.markAnimal(rabbit, "rabbit");

            rabbit.addEffect(
                    new MobEffectInstance(
                            MobEffects.SPEED,
                            999999,
                            3
                    )
            );

            rabbit.addEffect(
                    new MobEffectInstance(
                            MobEffects.REGENERATION,
                            999999,
                            2
                    )
            );
        }

        MagicWorld.effects(
                (ServerLevel) level,
                rabbit.blockPosition()
        );

        return true;
    }
}
