package com.magicworld.entity;

import com.magicworld.MagicWorld;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.cow.Cow;
import net.minecraft.world.entity.animal.cow.MushroomCow;
import net.minecraft.world.level.Level;

public class PremiumCow {

    public static boolean transform(
            Level level,
            Object target
    ) {

        // MOOSHROOM → VACA
        if (target instanceof MushroomCow mooshroom) {
            if (!PremiumEntityTags.isAnimal(mooshroom, "cow")) {
                return false;
            }

            Cow premiumCow =
                    new Cow(
                            EntityType.COW,
                            level
                    );

            premiumCow.setPos(
                    mooshroom.getX(),
                    mooshroom.getY(),
                    mooshroom.getZ()
            );

            level.addFreshEntity(
                    premiumCow
            );

            mooshroom.discard();

            MagicWorld.effects(
                    (ServerLevel) level,
                    mooshroom.blockPosition()
            );

            return true;
        }

        // VACA → PREMIUM
        else if (target instanceof Cow cow) {

            MushroomCow premiumMooshroom =
                    new MushroomCow(
                            EntityType.MOOSHROOM,
                            level
                    );

            premiumMooshroom.setPos(
                    cow.getX(),
                    cow.getY(),
                    cow.getZ()
            );

            premiumMooshroom.addEffect(
                    new MobEffectInstance(
                            MobEffects.SPEED,
                            999999,
                            2
                    )
            );

            premiumMooshroom.addEffect(
                    new MobEffectInstance(
                            MobEffects.REGENERATION,
                            999999,
                            2
                    )
            );

            PremiumEntityTags.markAnimal(premiumMooshroom, "cow");
            level.addFreshEntity(
                    premiumMooshroom
            );

            cow.discard();

            MagicWorld.effects(
                    (ServerLevel) level,
                    cow.blockPosition()
            );

            return true;
        }

        return false;
    }
}
