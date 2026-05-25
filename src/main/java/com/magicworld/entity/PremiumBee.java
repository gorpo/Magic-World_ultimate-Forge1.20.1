package com.magicworld.entity;

import com.magicworld.MagicWorld;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.bee.Bee;
import net.minecraft.world.entity.animal.allay.Allay;
import net.minecraft.world.level.Level;

public class PremiumBee {

    public static boolean transform(
            Level level,
            Object target
    ) {

        if (target instanceof Bee bee) {

            Allay premium =
                    new Allay(
                            EntityType.ALLAY,
                            level
                    );

            premium.setPos(
                    bee.getX(),
                    bee.getY(),
                    bee.getZ()
            );

            PremiumEntityTags.markAnimal(premium, "bee");
            level.addFreshEntity(
                    premium
            );

            bee.discard();

            MagicWorld.effects(
                    (ServerLevel) level,
                    bee.blockPosition()
            );

            return true;
        }

        else if (target instanceof Allay allay
                && PremiumEntityTags.isAnimal(allay, "bee")) {

            Bee bee =
                    new Bee(
                            EntityType.BEE,
                            level
                    );

            bee.setPos(
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

            return true;
        }

        return false;
    }
}
