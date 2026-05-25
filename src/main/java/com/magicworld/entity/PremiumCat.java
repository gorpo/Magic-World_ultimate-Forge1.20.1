package com.magicworld.entity;

import com.magicworld.MagicWorld;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.feline.Cat;
import net.minecraft.world.entity.animal.feline.Ocelot;
import net.minecraft.world.level.Level;

public class PremiumCat {

    public static boolean transform(
            Level level,
            Object target
    ) {

        // GATO → PREMIUM
        if (target instanceof Cat cat) {

            Ocelot premiumCat =
                    new Ocelot(
                            EntityType.OCELOT,
                            level
                    );

            premiumCat.setPos(
                    cat.getX(),
                    cat.getY(),
                    cat.getZ()
            );

            premiumCat.addEffect(
                    new MobEffectInstance(
                            MobEffects.SPEED,
                            999999,
                            2
                    )
            );

            premiumCat.addEffect(
                    new MobEffectInstance(
                            MobEffects.REGENERATION,
                            999999,
                            2
                    )
            );

            PremiumEntityTags.markAnimal(premiumCat, "cat");
            level.addFreshEntity(
                    premiumCat
            );

            cat.discard();

            MagicWorld.effects(
                    (ServerLevel) level,
                    cat.blockPosition()
            );

            return true;
        }

        // OCELOT → GATO
        else if (target instanceof Ocelot ocelot
                && PremiumEntityTags.isAnimal(ocelot, "cat")) {

            Cat cat =
                    new Cat(
                            EntityType.CAT,
                            level
                    );

            cat.setPos(
                    ocelot.getX(),
                    ocelot.getY(),
                    ocelot.getZ()
            );

            level.addFreshEntity(
                    cat
            );

            ocelot.discard();

            MagicWorld.effects(
                    (ServerLevel) level,
                    ocelot.blockPosition()
            );

            return true;
        }

        return false;
    }
}
