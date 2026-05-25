package com.magicworld.entity;

import com.magicworld.MagicWorld;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraft.world.level.Level;

public class PremiumBat {

    public static boolean transform(
            Level level,
            Bat bat
    ) {

        if (PremiumEntityTags.isAnimal(bat, "bat")) {
            PremiumEntityTags.clearAnimal(bat, "bat");

            bat.removeEffect(
                    MobEffects.SPEED
            );

            bat.removeEffect(
                    MobEffects.REGENERATION
            );
        }

        else {
            PremiumEntityTags.markAnimal(bat, "bat");

            bat.addEffect(
                    new MobEffectInstance(
                            MobEffects.SPEED,
                            999999,
                            5
                    )
            );

            bat.addEffect(
                    new MobEffectInstance(
                            MobEffects.REGENERATION,
                            999999,
                            3
                    )
            );
        }

        MagicWorld.effects(
                (ServerLevel) level,
                bat.blockPosition()
        );

        return true;
    }
}
