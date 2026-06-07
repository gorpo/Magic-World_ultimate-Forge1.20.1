package com.magicworld.entity;

import com.magicworld.MagicWorld;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.WanderingTrader;
import net.minecraft.world.level.Level;

public class PremiumVillager {

    public static void transform(
            Level level,
            Object target
    ) {

        if (target instanceof Villager villager) {

            WanderingTrader premium =
                    new WanderingTrader(
                            EntityType.WANDERING_TRADER,
                            level
                    );

            premium.moveTo(
                    villager.getX(),
                    villager.getY(),
                    villager.getZ()
            );

            premium.setCustomName(
                    Component.literal(
                            "Â§6ðŸ’° ALDEÃƒO PREMIUM ðŸ’°"
                    )
            );

            level.addFreshEntity(
                    premium
            );

            villager.discard();

            MagicWorld.effects(
                    (ServerLevel) level,
                    villager.blockPosition()
            );
        }

        else if (target instanceof WanderingTrader trader) {

            Villager villager =
                    new Villager(
                            EntityType.VILLAGER,
                            level
                    );

            villager.moveTo(
                    trader.getX(),
                    trader.getY(),
                    trader.getZ()
            );

            level.addFreshEntity(
                    villager
            );

            trader.discard();

            MagicWorld.effects(
                    (ServerLevel) level,
                    trader.blockPosition()
            );
        }
    }
}