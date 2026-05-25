package com.magicworld.entity;

import com.magicworld.MagicWorld;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.equine.Llama;
import net.minecraft.world.entity.animal.sheep.Sheep;
import net.minecraft.world.level.Level;

public class PremiumSheep {

    public static boolean transform(
            Level level,
            Object target
    ) {

        if (target instanceof Sheep sheep) {

            Llama premium =
                    new Llama(
                            EntityType.LLAMA,
                            level
                    );

            premium.setPos(
                    sheep.getX(),
                    sheep.getY(),
                    sheep.getZ()
            );

            premium.addEffect(
                    new MobEffectInstance(
                            MobEffects.SPEED,
                            999999,
                            2
                    )
            );

            PremiumEntityTags.markAnimal(premium, "sheep");
            level.addFreshEntity(
                    premium
            );

            sheep.discard();

            MagicWorld.effects(
                    (ServerLevel) level,
                    sheep.blockPosition()
            );

            return true;
        }

        else if (target instanceof Llama llama
                && PremiumEntityTags.isAnimal(llama, "sheep")) {

            Sheep sheep =
                    new Sheep(
                            EntityType.SHEEP,
                            level
                    );

            sheep.setPos(
                    llama.getX(),
                    llama.getY(),
                    llama.getZ()
            );

            level.addFreshEntity(
                    sheep
            );

            llama.discard();

            MagicWorld.effects(
                    (ServerLevel) level,
                    llama.blockPosition()
            );

            return true;
        }

        return false;
    }
}
