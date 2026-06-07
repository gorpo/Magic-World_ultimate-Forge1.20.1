package com.magicworld.entity;

import com.magicworld.MagicWorld;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.entity.animal.Parrot;
import net.minecraft.world.level.Level;

public class PremiumChicken {

    public static void transform(
            Level level,
            Object target
    ) {

        if (target instanceof Chicken chicken) {

            Parrot premium =
                    new Parrot(
                            EntityType.PARROT,
                            level
                    );

            premium.moveTo(
                    chicken.getX(),
                    chicken.getY(),
                    chicken.getZ()
            );

            premium.addEffect(
                    new MobEffectInstance(
                            MobEffects.MOVEMENT_SPEED,
                            999999,
                            2
                    )
            );

            level.addFreshEntity(
                    premium
            );

            chicken.discard();

            MagicWorld.effects(
                    (ServerLevel) level,
                    chicken.blockPosition()
            );
        }

        else if (target instanceof Parrot parrot) {

            Chicken chicken =
                    new Chicken(
                            EntityType.CHICKEN,
                            level
                    );

            chicken.moveTo(
                    parrot.getX(),
                    parrot.getY(),
                    parrot.getZ()
            );

            level.addFreshEntity(
                    chicken
            );

            parrot.discard();

            MagicWorld.effects(
                    (ServerLevel) level,
                    parrot.blockPosition()
            );
        }
    }
}