package com.magicworld.entity;

import com.magicworld.MagicWorld;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.npc.wanderingtrader.WanderingTrader;
import net.minecraft.world.level.Level;

public class PremiumVillager {

    public static boolean transform(
            Level level,
            Object target
    ) {

        if (target instanceof Villager villager) {

            WanderingTrader premium =
                    new WanderingTrader(
                            EntityType.WANDERING_TRADER,
                            level
                    );

            premium.setPos(
                    villager.getX(),
                    villager.getY(),
                    villager.getZ()
            );

            premium.setCustomName(
                    Component.literal(
                            "§6💰 ALDEÃO PREMIUM 💰"
                    )
            );

            PremiumEntityTags.markAnimal(premium, "villager");
            level.addFreshEntity(
                    premium
            );

            villager.discard();

            MagicWorld.effects(
                    (ServerLevel) level,
                    villager.blockPosition()
            );

            return true;
        }

        else if (target instanceof WanderingTrader trader
                && PremiumEntityTags.isAnimal(trader, "villager")) {

            Villager villager =
                    new Villager(
                            EntityType.VILLAGER,
                            level
                    );

            villager.setPos(
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

            return true;
        }

        return false;
    }
}
