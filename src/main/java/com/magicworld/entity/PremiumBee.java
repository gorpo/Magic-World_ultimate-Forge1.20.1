package com.magicworld.entity;

import com.magicworld.MagicWorld;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Bee;
import net.minecraft.world.entity.animal.allay.Allay;
import net.minecraft.world.level.Level;

public class PremiumBee {

    public static void transform(
            Level level,
            Object target
    ) {

        if (target instanceof Bee bee) {

            Allay premium =
                    new Allay(
                            EntityType.ALLAY,
                            level
                    );

            premium.moveTo(
                    bee.getX(),
                    bee.getY(),
                    bee.getZ()
            );

            level.addFreshEntity(
                    premium
            );

            bee.discard();

            MagicWorld.effects(
                    (ServerLevel) level,
                    bee.blockPosition()
            );
        }

        else if (target instanceof Allay allay) {

            Bee bee =
                    new Bee(
                            EntityType.BEE,
                            level
                    );

            bee.moveTo(
                    allay.getX(),
                    allay.getY(),
                    allay.getZ()
            );

            level.addFreshEntity(
                    bee
            );

            allay.discard();

            MagicWorld.effects(
                    (ServerLevel) level,
                    allay.blockPosition()
            );
        }
    }
}