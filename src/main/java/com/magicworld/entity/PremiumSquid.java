package com.magicworld.entity;

import com.magicworld.MagicWorld;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.animal.Squid;
import net.minecraft.world.level.Level;

public class PremiumSquid {

    public static void transform(
            Level level,
            Squid squid
    ) {

        if (squid.hasEffect(
                MobEffects.DOLPHINS_GRACE
        )) {

            squid.removeEffect(
                    MobEffects.DOLPHINS_GRACE
            );
        }

        else {

            squid.addEffect(
                    new MobEffectInstance(
                            MobEffects.DOLPHINS_GRACE,
                            999999,
                            3
                    )
            );
        }

        MagicWorld.effects(
                (ServerLevel) level,
                squid.blockPosition()
        );
    }
}