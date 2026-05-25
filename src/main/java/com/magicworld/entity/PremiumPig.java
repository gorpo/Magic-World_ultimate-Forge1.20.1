package com.magicworld.entity;

import com.magicworld.MagicWorld;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.entity.monster.hoglin.Hoglin;
import net.minecraft.world.level.Level;

public class PremiumPig {

    public static boolean transform(
            Level level,
            Object target
    ) {

        // PREMIUM → NORMAL
        if (target instanceof Hoglin hoglin
                && PremiumEntityTags.isAnimal(hoglin, "pig")) {

            Pig normalPig =
                    new Pig(
                            EntityType.PIG,
                            level
                    );

            normalPig.setPos(
                    hoglin.getX(),
                    hoglin.getY(),
                    hoglin.getZ()
            );

            level.addFreshEntity(
                    normalPig
            );

            hoglin.discard();

            MagicWorld.effects(
                    (ServerLevel) level,
                    hoglin.blockPosition()
            );

            return true;
        }

        // NORMAL → PREMIUM
        else if (target instanceof Pig pig) {

            Hoglin premiumPig =
                    new Hoglin(
                            EntityType.HOGLIN,
                            level
                    );

            premiumPig.setPos(
                    pig.getX(),
                    pig.getY(),
                    pig.getZ()
            );

            premiumPig.addEffect(
                    new MobEffectInstance(
                            MobEffects.SPEED,
                            999999,
                            2
                    )
            );

            premiumPig.addEffect(
                    new MobEffectInstance(
                            MobEffects.REGENERATION,
                            999999,
                            2
                    )
            );

            PremiumEntityTags.markAnimal(premiumPig, "pig");
            level.addFreshEntity(
                    premiumPig
            );

            pig.discard();

            MagicWorld.effects(
                    (ServerLevel) level,
                    pig.blockPosition()
            );

            return true;
        }

        return false;
    }
}
