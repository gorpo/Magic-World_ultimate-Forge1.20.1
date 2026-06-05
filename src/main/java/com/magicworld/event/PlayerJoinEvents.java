package com.magicworld.event;

import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraft.network.chat.Component;

@Mod.EventBusSubscriber
public class PlayerJoinEvents {

    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {

        event.getEntity().sendSystemMessage(
                Component.literal(
                        "Pressione H para abrir o menu Magic Wand"
                )
        );
    }
}
