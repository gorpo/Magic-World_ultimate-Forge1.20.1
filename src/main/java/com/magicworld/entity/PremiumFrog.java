package com.magicworld.entity;

import com.magicworld.MagicWorld;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.animal.frog.Frog;
import net.minecraft.world.level.Level;

public class PremiumFrog {

    public static boolean transform(
            Level level,
            Frog frog
    ) {

        if (PremiumEntityTags.isAnimal(frog, "frog")) {
            PremiumEntityTags.clearAnimal(frog, "frog");

            frog.removeEffect(
                    MobEffects.JUMP_BOOST
            );

            frog.removeEffect(
                    MobEffects.SPEED
            );
        }

        else {
            PremiumEntityTags.markAnimal(frog, "frog");

            frog.addEffect(
                    new MobEffectInstance(
                            MobEffects.JUMP_BOOST,
                            999999,
                            5
                    )
            );

            frog.addEffect(
                    new MobEffectInstance(
                            MobEffects.SPEED,
                            999999,
                            2
                    )
            );
        }

        MagicWorld.effects(
                (ServerLevel) level,
                frog.blockPosition()
        );

        return true;
    }
}
