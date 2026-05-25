package com.magicworld.event;

import com.magicworld.Config;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

public class PlayerJoinEvents {

    public static void registerListeners() {
        NeoForge.EVENT_BUS.addListener(PlayerJoinEvents::onPlayerJoin);
    }

    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {

        event.getEntity().sendSystemMessage(
                Component.literal(
                        "Pressione H para abrir o menu Magic Wand"
                )
        );

        if (Config.visualExperienceStartMode.equals("locked_until_portal")
                && !StarterPortalEvents.isVisualExperienceUnlocked(event.getEntity())) {
            event.getEntity().sendSystemMessage(
                    Component.literal(
                            "Magic World: experiencia visual especial bloqueada ate o portal inicial."
                    )
            );
        }
    }
}
