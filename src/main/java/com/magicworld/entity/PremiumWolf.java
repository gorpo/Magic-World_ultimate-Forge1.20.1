package com.magicworld.entity;

import com.magicworld.MagicWorld;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.level.Level;

public class PremiumWolf {

    public static boolean transform(
            Level level,
            Wolf wolf
    ) {

        // premium → normal
        if (PremiumEntityTags.isAnimal(wolf, "wolf")) {
            PremiumEntityTags.clearAnimal(wolf, "wolf");

            wolf.removeEffect(
                    MobEffects.STRENGTH
            );

            wolf.removeEffect(
                    MobEffects.REGENERATION
            );

            wolf.removeEffect(
                    MobEffects.SPEED
            );

            wolf.removeEffect(
                    MobEffects.FIRE_RESISTANCE
            );

            MagicWorld.effects(
                    (ServerLevel) level,
                    wolf.blockPosition()
            );
        }

        // normal → premium
        else {
            PremiumEntityTags.markAnimal(wolf, "wolf");

            wolf.setHealth(
                    wolf.getMaxHealth()
            );

            wolf.setTame(true, true);

            wolf.addEffect(
                    new MobEffectInstance(
                            MobEffects.SPEED,
                            999999,
                            2
                    )
            );

            wolf.addEffect(
                    new MobEffectInstance(
                            MobEffects.STRENGTH,
                            999999,
                            2
                    )
            );

            wolf.addEffect(
                    new MobEffectInstance(
                            MobEffects.REGENERATION,
                            999999,
                            2
                    )
            );

            wolf.addEffect(
                    new MobEffectInstance(
                            MobEffects.FIRE_RESISTANCE,
                            999999,
                            1
                    )
            );

            MagicWorld.effects(
                    (ServerLevel) level,
                    wolf.blockPosition()
            );
        }

        return true;
    }
}
