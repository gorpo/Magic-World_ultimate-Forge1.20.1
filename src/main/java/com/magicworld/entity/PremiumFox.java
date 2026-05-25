package com.magicworld.entity;

import com.magicworld.MagicWorld;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.animal.fox.Fox;
import net.minecraft.world.level.Level;

public class PremiumFox {

    public static boolean transform(
            Level level,
            Fox fox
    ) {

        if (!PremiumEntityTags.isAnimal(fox, "fox")) {
            PremiumEntityTags.markAnimal(fox, "fox");

            fox.addEffect(
                    new MobEffectInstance(
                            MobEffects.SPEED,
                            999999,
                            3
                    )
            );

            fox.addEffect(
                    new MobEffectInstance(
                            MobEffects.REGENERATION,
                            999999,
                            2
                    )
            );
        }

        else {
            PremiumEntityTags.clearAnimal(fox, "fox");

            fox.removeEffect(
                    MobEffects.SPEED
            );

            fox.removeEffect(
                    MobEffects.REGENERATION
            );
        }

        MagicWorld.effects(
                (ServerLevel) level,
                fox.blockPosition()
        );

        return true;
    }
}
