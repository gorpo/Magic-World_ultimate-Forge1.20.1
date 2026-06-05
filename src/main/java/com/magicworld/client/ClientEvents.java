package com.magicworld.client;

import com.magicworld.MagicWorldWorldOptions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.OptionsScreen;
import net.minecraft.client.gui.screens.packs.PackSelectionScreen;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.client.gui.ModListScreen;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.IdentityHashMap;
import java.util.Map;

@Mod.EventBusSubscriber(
        bus = Mod.EventBusSubscriber.Bus.MOD,
        value = Dist.CLIENT
)
public class ClientEvents {

    @SubscribeEvent
    public static void registerKeys(
            RegisterKeyMappingsEvent event
    ) {

        KeyBindings.register();

        event.register(
                KeyBindings.OPEN_MENU_KEY
        );
    }

    @Mod.EventBusSubscriber(
            value = Dist.CLIENT
    )
    public static class ClientForgeEvents {
        private static final Map<CreateWorldScreen, Button> MAGIC_WORLD_BUTTONS =
                new IdentityHashMap<>();
        private static final int MAGIC_BUTTON_WIDTH =
                126;
        private static final int MAGIC_BUTTON_HEIGHT =
                20;

        @SubscribeEvent
        public static void onClientTick(
                TickEvent.ClientTickEvent event
        ) {

            if (event.phase
                    != TickEvent.Phase.END) {
                return;
            }

            Minecraft minecraft =
                    Minecraft.getInstance();

            if (minecraft.screen instanceof TitleScreen
                    && !(minecraft.screen instanceof MagicWorldTitleScreen)) {
                minecraft.setScreen(new MagicWorldTitleScreen());
                return;
            }

            while (KeyBindings.OPEN_MENU_KEY
                    .consumeClick()) {

                if (minecraft.screen instanceof PremiumMenuScreen) {
                    minecraft.setScreen(null);
                    continue;
                }

                if (minecraft.player == null
                        || minecraft.screen != null) {
                    continue;
                }

                minecraft.setScreen(new PremiumMenuScreen());
            }
        }

        @SubscribeEvent
        public static void onScreenInit(
                ScreenEvent.Init.Post event
        ) {

            if (!(event.getScreen() instanceof CreateWorldScreen screen)) {
                return;
            }

            Button button =
                    Button.builder(
                                    magicButtonLabel(),
                                    pressed -> {
                                        MagicWorldWorldOptions.toggleStarterEstateEnabled();
                                        pressed.setMessage(magicButtonLabel());
                                    }
                            )
                            .bounds(
                                    magicButtonX(screen.width),
                                    magicButtonY(screen.height),
                                    MAGIC_BUTTON_WIDTH,
                                    MAGIC_BUTTON_HEIGHT
                            )
                            .build();

            MAGIC_WORLD_BUTTONS.put(screen, button);
            event.addListener(button);
        }

        @SubscribeEvent
        public static void onScreenOpening(
                ScreenEvent.Opening event
        ) {

            if (event.getNewScreen() instanceof TitleScreen
                    && !(event.getNewScreen() instanceof MagicWorldTitleScreen)) {
                event.setNewScreen(new MagicWorldTitleScreen());
            }
        }

        @SubscribeEvent
        public static void onScreenBackground(
                ScreenEvent.BackgroundRendered event
        ) {

            if (shouldUseMagicBackground(event.getScreen())) {
                MagicWorldStaticBackground.draw(
                        event.getGuiGraphics(),
                        event.getScreen().width,
                        event.getScreen().height
                );
            }
        }

        @SubscribeEvent
        public static void onScreenRender(
                ScreenEvent.Render.Post event
        ) {

            if (!(event.getScreen() instanceof CreateWorldScreen screen)) {
                return;
            }

            Button button =
                    MAGIC_WORLD_BUTTONS.get(screen);

            if (button == null) {
                return;
            }

            button.setX(magicButtonX(screen.width));
            button.setY(magicButtonY(screen.height));
            button.setWidth(MAGIC_BUTTON_WIDTH);
            button.setHeight(MAGIC_BUTTON_HEIGHT);
            button.setMessage(magicButtonLabel());
        }

        private static Component magicButtonLabel() {
            return Component.literal(
                    MagicWorldWorldOptions.isStarterEstateEnabled()
                            ? "Magic World: ON"
                            : "Magic World: OFF"
            );
        }

        private static int magicButtonX(int screenWidth) {
            return screenWidth / 2 + 104;
        }

        private static int magicButtonY(int screenHeight) {
            return Math.max(8, screenHeight - 52);
        }

        private static boolean shouldUseMagicBackground(Object screen) {
            return screen instanceof CreateWorldScreen
                    || screen instanceof SelectWorldScreen
                    || screen instanceof JoinMultiplayerScreen
                    || screen instanceof OptionsScreen
                    || screen instanceof PackSelectionScreen
                    || screen instanceof ModListScreen;
        }
    }
}
