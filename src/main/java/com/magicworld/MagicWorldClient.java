package com.magicworld;

import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import org.slf4j.Logger;

@Mod(value = MagicWorld.MODID, dist = Dist.CLIENT)
@EventBusSubscriber(
        modid = MagicWorld.MODID,
        value = Dist.CLIENT
)
public class MagicWorldClient {

    private static final Logger LOGGER =
            LogUtils.getLogger();

    public MagicWorldClient(
            ModContainer container
    ) {

        container.registerExtensionPoint(
                IConfigScreenFactory.class,
                ConfigurationScreen::new
        );
    }

    @SubscribeEvent
    static void onClientSetup(
            FMLClientSetupEvent event
    ) {

        LOGGER.info(
                "HELLO FROM CLIENT SETUP"
        );

        LOGGER.info(
                "MINECRAFT NAME >> {}",
                Minecraft.getInstance()
                        .getUser()
                        .getName()
        );
    }
}