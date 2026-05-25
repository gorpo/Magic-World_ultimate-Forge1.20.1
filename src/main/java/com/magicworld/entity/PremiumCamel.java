package com.magicworld.entity;

import com.magicworld.MagicWorld;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.animal.camel.Camel;
import net.minecraft.world.level.Level;

public class PremiumCamel {

    public static boolean transform(
            Level level,
            Camel camel
    ) {

        if (PremiumEntityTags.isAnimal(camel, "camel")) {
            PremiumEntityTags.clearAnimal(camel, "camel");

            camel.removeEffect(
                    MobEffects.SPEED
            );

            camel.removeEffect(
                    MobEffects.REGENERATION
            );
        }

        else {
            PremiumEntityTags.markAnimal(camel, "camel");

            camel.addEffect(
                    new MobEffectInstance(
                            MobEffects.SPEED,
                            999999,
                            4
                    )
            );

            camel.addEffect(
                    new MobEffectInstance(
                            MobEffects.REGENERATION,
                            999999,
                            2
                    )
            );
        }

        MagicWorld.effects(
                (ServerLevel) level,
                camel.blockPosition()
        );

        return true;
    }
}
