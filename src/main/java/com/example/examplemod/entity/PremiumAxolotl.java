package com.example.examplemod.entity;

import com.example.examplemod.ExampleMod;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.axolotl.Axolotl;
import net.minecraft.world.entity.animal.frog.Frog;
import net.minecraft.world.level.Level;

public class PremiumAxolotl {

    public static void transform(
            Level level,
            Axolotl axolotl
    ) {

        Frog premium =
                new Frog(
                        EntityType.FROG,
                        level
                );

        premium.moveTo(
                axolotl.getX(),
                axolotl.getY(),
                axolotl.getZ()
        );

        level.addFreshEntity(
                premium
        );

        axolotl.discard();

        ExampleMod.effects(
                (ServerLevel) level,
                axolotl.blockPosition()
        );
    }
}