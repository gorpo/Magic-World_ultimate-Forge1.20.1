package com.example.examplemod.entity;

import com.example.examplemod.ExampleMod;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.horse.Llama;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.level.Level;

public class PremiumSheep {

    public static void transform(
            Level level,
            Object target
    ) {

        if (target instanceof Sheep sheep) {

            Llama premium =
                    new Llama(
                            EntityType.LLAMA,
                            level
                    );

            premium.moveTo(
                    sheep.getX(),
                    sheep.getY(),
                    sheep.getZ()
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

            sheep.discard();

            ExampleMod.effects(
                    (ServerLevel) level,
                    sheep.blockPosition()
            );
        }

        else if (target instanceof Llama llama) {

            Sheep sheep =
                    new Sheep(
                            EntityType.SHEEP,
                            level
                    );

            sheep.moveTo(
                    llama.getX(),
                    llama.getY(),
                    llama.getZ()
            );

            level.addFreshEntity(
                    sheep
            );

            llama.discard();

            ExampleMod.effects(
                    (ServerLevel) level,
                    llama.blockPosition()
            );
        }
    }
}