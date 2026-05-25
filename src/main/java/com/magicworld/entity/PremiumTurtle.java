package com.magicworld.entity;

import com.magicworld.MagicWorld;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.animal.turtle.Turtle;
import net.minecraft.world.level.Level;

public class PremiumTurtle {

    public static boolean transform(
            Level level,
            Turtle turtle
    ) {

        if (PremiumEntityTags.isAnimal(turtle, "turtle")) {
            PremiumEntityTags.clearAnimal(turtle, "turtle");

            turtle.removeEffect(
                    MobEffects.RESISTANCE
            );

            turtle.removeEffect(
                    MobEffects.REGENERATION
            );
        }

        else {
            PremiumEntityTags.markAnimal(turtle, "turtle");

            turtle.addEffect(
                    new MobEffectInstance(
                            MobEffects.RESISTANCE,
                            999999,
                            4
                    )
            );

            turtle.addEffect(
                    new MobEffectInstance(
                            MobEffects.REGENERATION,
                            999999,
                            3
                    )
            );
        }

        MagicWorld.effects(
                (ServerLevel) level,
                turtle.blockPosition()
        );

        return true;
    }
}
