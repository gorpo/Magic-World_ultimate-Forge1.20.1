package com.example.examplemod.entity;

import com.example.examplemod.ExampleMod;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.monster.hoglin.Hoglin;
import net.minecraft.world.level.Level;

public class PremiumPig {

    public static void transform(
            Level level,
            Object target
    ) {

        // PREMIUM → NORMAL
        if (target instanceof Hoglin hoglin) {

            Pig normalPig =
                    new Pig(
                            EntityType.PIG,
                            level
                    );

            normalPig.moveTo(
                    hoglin.getX(),
                    hoglin.getY(),
                    hoglin.getZ()
            );

            level.addFreshEntity(
                    normalPig
            );

            hoglin.discard();

            ExampleMod.effects(
                    (ServerLevel) level,
                    hoglin.blockPosition()
            );
        }

        // NORMAL → PREMIUM
        else if (target instanceof Pig pig) {

            Hoglin premiumPig =
                    new Hoglin(
                            EntityType.HOGLIN,
                            level
                    );

            premiumPig.moveTo(
                    pig.getX(),
                    pig.getY(),
                    pig.getZ()
            );

            premiumPig.addEffect(
                    new MobEffectInstance(
                            MobEffects.MOVEMENT_SPEED,
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

            level.addFreshEntity(
                    premiumPig
            );

            pig.discard();

            ExampleMod.effects(
                    (ServerLevel) level,
                    pig.blockPosition()
            );
        }
    }
}