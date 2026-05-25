package com.magicworld.entity;

import com.magicworld.MagicWorld;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.animal.panda.Panda;
import net.minecraft.world.level.Level;

public class PremiumPanda {

    public static boolean transform(
            Level level,
            Panda panda
    ) {

        if (PremiumEntityTags.isAnimal(panda, "panda")) {
            PremiumEntityTags.clearAnimal(panda, "panda");

            panda.removeEffect(
                    MobEffects.STRENGTH
            );

            panda.removeEffect(
                    MobEffects.REGENERATION
            );
        }

        else {
            PremiumEntityTags.markAnimal(panda, "panda");

            panda.addEffect(
                    new MobEffectInstance(
                            MobEffects.STRENGTH,
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

        return true;
    }
}
