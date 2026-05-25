package com.magicworld.entity;

import com.magicworld.MagicWorld;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.chicken.Chicken;
import net.minecraft.world.entity.animal.parrot.Parrot;
import net.minecraft.world.level.Level;

public class PremiumChicken {

    public static boolean transform(
            Level level,
            Object target
    ) {

        if (target instanceof Chicken chicken) {

            Parrot premium =
                    new Parrot(
                            EntityType.PARROT,
                            level
                    );

            premium.setPos(
                    chicken.getX(),
                    chicken.getY(),
                    chicken.getZ()
            );

            premium.addEffect(
                    new MobEffectInstance(
                            MobEffects.SPEED,
                            999999,
                            2
                    )
            );

            PremiumEntityTags.markAnimal(premium, "chicken");
            level.addFreshEntity(
                    premium
            );

            chicken.discard();

            MagicWorld.effects(
                    (ServerLevel) level,
                    chicken.blockPosition()
            );

            return true;
        }

        else if (target instanceof Parrot parrot
                && PremiumEntityTags.isAnimal(parrot, "chicken")) {

            Chicken chicken =
                    new Chicken(
                            EntityType.CHICKEN,
                            level
                    );

            chicken.setPos(
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

            return true;
        }

        return false;
    }
}
