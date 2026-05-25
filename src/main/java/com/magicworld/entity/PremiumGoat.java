package com.magicworld.entity;

import com.magicworld.MagicWorld;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.animal.goat.Goat;
import net.minecraft.world.level.Level;

public class PremiumGoat {

    public static boolean transform(
            Level level,
            Goat goat
    ) {

        if (PremiumEntityTags.isAnimal(goat, "goat")) {
            PremiumEntityTags.clearAnimal(goat, "goat");

            goat.removeEffect(
                    MobEffects.STRENGTH
            );

            goat.removeEffect(
                    MobEffects.SPEED
            );
        }

        else {
            PremiumEntityTags.markAnimal(goat, "goat");

            goat.addEffect(
                    new MobEffectInstance(
                            MobEffects.STRENGTH,
                            999999,
                            3
                    )
            );

            goat.addEffect(
                    new MobEffectInstance(
                            MobEffects.SPEED,
                            999999,
                            2
                    )
            );

        }

        MagicWorld.effects(
                (ServerLevel) level,
                goat.blockPosition()
        );

        return true;
    }
}
