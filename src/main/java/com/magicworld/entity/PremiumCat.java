package com.magicworld.entity;

import com.magicworld.MagicWorld;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.entity.animal.Ocelot;
import net.minecraft.world.level.Level;

public class PremiumCat {

    public static void transform(
            Level level,
            Object target
    ) {

        // GATO â†’ PREMIUM
        if (target instanceof Cat cat) {

            if (!cat.hasEffect(
                    MobEffects.MOVEMENT_SPEED
            )) {

                Ocelot premiumCat =
                        new Ocelot(
                                EntityType.OCELOT,
                                level
                        );

                premiumCat.moveTo(
                        cat.getX(),
                        cat.getY(),
                        cat.getZ()
                );

                premiumCat.addEffect(
                        new MobEffectInstance(
                                MobEffects.MOVEMENT_SPEED,
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

                level.addFreshEntity(
                        premiumCat
                );

                cat.discard();

                MagicWorld.effects(
                        (ServerLevel) level,
                        cat.blockPosition()
                );
            }
        }

        // OCELOT â†’ GATO
        else if (target instanceof Ocelot ocelot) {

            Cat cat =
                    new Cat(
                            EntityType.CAT,
                            level
                    );

            cat.moveTo(
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
        }
    }
}