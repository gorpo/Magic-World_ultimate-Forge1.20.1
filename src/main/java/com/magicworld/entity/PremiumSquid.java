package com.magicworld.entity;

import com.magicworld.MagicWorld;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.animal.squid.Squid;
import net.minecraft.world.level.Level;

public class PremiumSquid {

    public static boolean transform(
            Level level,
            Squid squid
    ) {

        if (PremiumEntityTags.isAnimal(squid, "squid")) {
            PremiumEntityTags.clearAnimal(squid, "squid");

            squid.removeEffect(
                    MobEffects.DOLPHINS_GRACE
            );
        }

        else {
            PremiumEntityTags.markAnimal(squid, "squid");

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

        return true;
    }
}
