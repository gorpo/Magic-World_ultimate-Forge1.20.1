package com.magicworld.entity;

import com.magicworld.MagicWorld;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.animal.MushroomCow;
import net.minecraft.world.level.Level;

public class PremiumCow {

    public static void transform(
            Level level,
            Object target
    ) {

        // MOOSHROOM â†’ VACA
        if (target instanceof MushroomCow mooshroom) {

            Cow premiumCow =
                    new Cow(
                            EntityType.COW,
                            level
                    );

            premiumCow.moveTo(
                    mooshroom.getX(),
                    mooshroom.getY(),
                    mooshroom.getZ()
            );

            premiumCow.addEffect(
                    new MobEffectInstance(
                            MobEffects.MOVEMENT_SPEED,
                            999999,
                            2
                    )
            );

            premiumCow.addEffect(
                    new MobEffectInstance(
                            MobEffects.REGENERATION,
                            999999,
                            2
                    )
            );

            level.addFreshEntity(
                    premiumCow
            );

            mooshroom.discard();

            MagicWorld.effects(
                    (ServerLevel) level,
                    mooshroom.blockPosition()
            );
        }

        // VACA â†’ PREMIUM
        else if (target instanceof Cow cow) {

            if (!cow.hasEffect(
                    MobEffects.MOVEMENT_SPEED
            )) {

                MushroomCow premiumMooshroom =
                        new MushroomCow(
                                EntityType.MOOSHROOM,
                                level
                        );

                premiumMooshroom.moveTo(
                        cow.getX(),
                        cow.getY(),
                        cow.getZ()
                );

                premiumMooshroom.addEffect(
                        new MobEffectInstance(
                                MobEffects.MOVEMENT_SPEED,
                                999999,
                                2
                        )
                );

                premiumMooshroom.addEffect(
                        new MobEffectInstance(
                                MobEffects.REGENERATION,
                                999999,
                                2
                        )
                );

                level.addFreshEntity(
                        premiumMooshroom
                );

                cow.discard();

                MagicWorld.effects(
                        (ServerLevel) level,
                        cow.blockPosition()
                );
            }

            else {

                Cow normalCow =
                        new Cow(
                                EntityType.COW,
                                level
                        );

                normalCow.moveTo(
                        cow.getX(),
                        cow.getY(),
                        cow.getZ()
                );

                level.addFreshEntity(
                        normalCow
                );

                cow.discard();

                MagicWorld.effects(
                        (ServerLevel) level,
                        cow.blockPosition()
                );
            }
        }
    }
}