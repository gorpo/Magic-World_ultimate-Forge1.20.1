package com.magicworld.entity;

import com.magicworld.MagicWorld;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.axolotl.Axolotl;
import net.minecraft.world.entity.animal.frog.Frog;
import net.minecraft.world.level.Level;

public class PremiumAxolotl {

    public static boolean transform(
            Level level,
            Object target
    ) {

        if (target instanceof Axolotl axolotl) {
            Frog premium =
                    new Frog(
                            EntityType.FROG,
                            level
                    );

            premium.setPos(
                    axolotl.getX(),
                    axolotl.getY(),
                    axolotl.getZ()
            );

            PremiumEntityTags.markAnimal(premium, "axolotl");
            level.addFreshEntity(premium);
            axolotl.discard();

            MagicWorld.effects(
                    (ServerLevel) level,
                    axolotl.blockPosition()
            );

            return true;
        }

        if (target instanceof Frog frog
                && PremiumEntityTags.isAnimal(frog, "axolotl")) {
            Axolotl axolotl =
                    new Axolotl(
                            EntityType.AXOLOTL,
                            level
                    );

            axolotl.setPos(
                    frog.getX(),
                    frog.getY(),
                    frog.getZ()
            );

            level.addFreshEntity(axolotl);
            frog.discard();

            MagicWorld.effects(
                    (ServerLevel) level,
                    frog.blockPosition()
            );

            return true;
        }

        return false;
    }
}
