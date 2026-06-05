package com.magicworld.client;

import com.magicworld.MagicWorldWorldOptions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.LevelLoadingScreen;
import net.minecraft.client.gui.screens.OptionsScreen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.WeakHashMap;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientEvents {
    @SubscribeEvent
    public static void registerKeys(RegisterKeyMappingsEvent event) {
        KeyBindings.register();
        event.register(KeyBindings.OPEN_MENU_KEY);
    }

    @Mod.EventBusSubscriber(value = Dist.CLIENT)
    public static class ClientForgeEvents {
        private record MagicCreateWorldPanel(
                CreateWorldScreen screen,
                List<AbstractWidget> vanillaWidgets,
                AbstractWidget magicTabButton,
                List<AbstractWidget> magicWidgets
        ) {
        }

        private record MagicPanelLayout(int gridWidth, int buttonWidth, int left, int top, int buttonsTop, int lineCoverY) {
        }

        private static final WeakHashMap<CreateWorldScreen, MagicCreateWorldPanel> MAGIC_CREATE_WORLD_PANELS = new WeakHashMap<>();
        private static final int MAGIC_PANEL_MAX_WIDTH = 520;
        private static final int MAGIC_PANEL_MIN_WIDTH = 340;
        private static final int MAGIC_PANEL_GAP = 8;
        private static final int MAGIC_PANEL_BUTTON_HEIGHT = 20;
        private static final int MAGIC_PANEL_HEIGHT = 260;

        @SubscribeEvent
        public static void onClientTick(TickEvent.ClientTickEvent event) {
            if (event.phase != TickEvent.Phase.END) {
                return;
            }

            Minecraft minecraft = Minecraft.getInstance();

            if (minecraft.screen instanceof TitleScreen
                    && !(minecraft.screen instanceof MagicWorldTitleScreen)) {
                minecraft.setScreen(new MagicWorldTitleScreen());
                return;
            }

            if (minecraft.screen instanceof CreateWorldScreen screen) {
                MagicCreateWorldPanel panel = MAGIC_CREATE_WORLD_PANELS.get(screen);
                if (panel != null) {
                    hideMagicPanelIfVanillaTabTookOver(panel);
                    relayoutMagicPanel(panel);
                    updateMagicTabButton(panel);
                    MagicWorldWorldOptions.setCommandsEnabled(true);
                    forceCreateWorldAllowCommands(screen);
                    syncAutomaticCommands(panel.vanillaWidgets());
                }
            }

            while (KeyBindings.OPEN_MENU_KEY.consumeClick()) {
                if (minecraft.screen instanceof PremiumMenuScreen) {
                    minecraft.setScreen(null);
                    continue;
                }

                if (minecraft.player == null || minecraft.screen != null) {
                    continue;
                }

                minecraft.setScreen(new PremiumMenuScreen());
            }
        }

        @SubscribeEvent
        public static void onScreenOpening(ScreenEvent.Opening event) {
            if (event.getNewScreen() instanceof CreateWorldScreen createWorldScreen) {
                MagicWorldWorldOptions.setStarterEstateEnabled(true);
                MagicWorldWorldOptions.setCastlesEnabled(true);
                MagicWorldWorldOptions.setFarmsEnabled(true);
                MagicWorldWorldOptions.setAuraEnabled(true);
                MagicWorldWorldOptions.setCommandsEnabled(true);
                MagicWorldWorldOptions.setHardwareProfileIndex(3);
                MagicWorldWorldOptions.setStartingGameMode(MagicWorldWorldOptions.StartingGameMode.SURVIVAL);
                MagicWorldWorldOptions.setStartingDifficulty(MagicWorldWorldOptions.StartingDifficulty.NORMAL);
                forceCreateWorldAllowCommands(createWorldScreen);
            }

            if (event.getNewScreen() instanceof TitleScreen
                    && !(event.getNewScreen() instanceof MagicWorldTitleScreen)) {
                event.setNewScreen(new MagicWorldTitleScreen());
            }
        }

        @SubscribeEvent
        public static void onScreenInit(ScreenEvent.Init.Post event) {
            if (!(event.getScreen() instanceof CreateWorldScreen screen)) {
                return;
            }

            List<AbstractWidget> vanillaWidgets = new ArrayList<>();
            forceCreateWorldAllowCommands(screen);
            for (GuiEventListener listener : event.getListenersList()) {
                if (listener instanceof AbstractWidget widget) {
                    vanillaWidgets.add(widget);
                }
            }

            MagicPanelLayout layout = magicPanelLayout(screen);
            Button magicTabButton = Button.builder(
                            Component.literal("Magic World"),
                            pressed -> {
                                syncAutomaticCommands(vanillaWidgets);
                                showMagicPanel(screen, true);
                            }
                    )
                    .bounds(magicButtonX(screen, vanillaWidgets), magicButtonY(vanillaWidgets), magicButtonWidth(vanillaWidgets), MAGIC_PANEL_BUTTON_HEIGHT)
                    .tooltip(Tooltip.create(Component.literal("Abre as opcoes Magic World da criacao do mundo.")))
                    .build();

            int gridWidth = layout.gridWidth();
            int gap = MAGIC_PANEL_GAP;
            int buttonWidth = layout.buttonWidth();
            int buttonHeight = MAGIC_PANEL_BUTTON_HEIGHT;
            int left = layout.left();
            int top = layout.top();
            int buttonsTop = layout.buttonsTop();

            List<AbstractWidget> magicWidgets = List.of(
                    new MagicCreateWorldLineCover(0, layout.lineCoverY(), screen.width, 8),
                    new MagicCreateWorldBackdrop(left - 12, top - 10, gridWidth + 24, MAGIC_PANEL_HEIGHT + 20),
                    new MagicCreateWorldTitle(left, top + 2, gridWidth, 18, Minecraft.getInstance().font, Component.literal("MAGIC WORLD")),
                    new MagicCreateWorldInfo(left, top + 25, gridWidth, Math.max(72, buttonsTop - top - 34), Minecraft.getInstance().font),
                    Button.builder(magicWorldButtonLabel(), pressed -> {
                                MagicWorldWorldOptions.toggleStarterEstateEnabled();
                                syncAutomaticCommands(vanillaWidgets);
                                pressed.setMessage(magicWorldButtonLabel());
                            })
                            .bounds(gridX(left, buttonWidth, gap, 0), gridY(buttonsTop, buttonHeight, gap, 0), buttonWidth, buttonHeight)
                            .tooltip(Tooltip.create(Component.literal("ON: cria portal, casa inicial, bau e terreno inicial.")))
                            .build(),
                    Button.builder(castlesButtonLabel(), pressed -> {
                                MagicWorldWorldOptions.toggleCastlesEnabled();
                                syncAutomaticCommands(vanillaWidgets);
                                pressed.setMessage(castlesButtonLabel());
                            })
                            .bounds(gridX(left, buttonWidth, gap, 1), gridY(buttonsTop, buttonHeight, gap, 0), buttonWidth, buttonHeight)
                            .tooltip(Tooltip.create(Component.literal("ON: reserva castelos para o proximo porte.")))
                            .build(),
                    Button.builder(farmsButtonLabel(), pressed -> {
                                MagicWorldWorldOptions.toggleFarmsEnabled();
                                syncAutomaticCommands(vanillaWidgets);
                                pressed.setMessage(farmsButtonLabel());
                            })
                            .bounds(gridX(left, buttonWidth, gap, 0), gridY(buttonsTop, buttonHeight, gap, 1), buttonWidth, buttonHeight)
                            .tooltip(Tooltip.create(Component.literal("ON: reserva fazendas/villagers para o proximo porte.")))
                            .build(),
                    Button.builder(auraButtonLabel(), pressed -> {
                                MagicWorldWorldOptions.toggleAuraEnabled();
                                syncAutomaticCommands(vanillaWidgets);
                                pressed.setMessage(auraButtonLabel());
                            })
                            .bounds(gridX(left, buttonWidth, gap, 1), gridY(buttonsTop, buttonHeight, gap, 1), buttonWidth, buttonHeight)
                            .tooltip(Tooltip.create(Component.literal("ON: reserva aura para o proximo porte.")))
                            .build(),
                    Button.builder(hardwareButtonLabel(), pressed -> {
                                MagicWorldGraphicsProfile[] profiles = MagicWorldGraphicsProfile.values();
                                MagicWorldWorldOptions.nextHardwareProfileIndex(profiles.length);
                                pressed.setMessage(hardwareButtonLabel());
                            })
                            .bounds(gridX(left, buttonWidth, gap, 2), gridY(buttonsTop, buttonHeight, gap, 0), buttonWidth, buttonHeight)
                            .tooltip(Tooltip.create(Component.literal("Seleciona o perfil de PC do mundo.")))
                            .build(),
                    Button.builder(difficultyButtonLabel(), pressed -> {
                                MagicWorldWorldOptions.nextStartingDifficulty();
                                pressed.setMessage(difficultyButtonLabel());
                            })
                            .bounds(gridX(left, buttonWidth, gap, 2), gridY(buttonsTop, buttonHeight, gap, 1), buttonWidth, buttonHeight)
                            .tooltip(Tooltip.create(Component.literal("Escolhe a dificuldade inicial.")))
                            .build(),
                    Button.builder(gameModeButtonLabel(), pressed -> {
                                MagicWorldWorldOptions.nextStartingGameMode();
                                pressed.setMessage(gameModeButtonLabel());
                            })
                            .bounds(left, gridY(buttonsTop, buttonHeight, gap, 2), gridWidth, buttonHeight)
                            .tooltip(Tooltip.create(Component.literal("Escolhe modo normal ou criativo.")))
                            .build(),
                    Button.builder(Component.literal("Criar Mundo"), pressed -> createWorldFromMagicTab(screen, vanillaWidgets))
                            .bounds(left, gridY(buttonsTop, buttonHeight, gap, 3), (gridWidth - gap) / 2, buttonHeight)
                            .build(),
                    Button.builder(Component.literal("Voltar"), pressed -> showMagicPanel(screen, false))
                            .bounds(left + (gridWidth - gap) / 2 + gap, gridY(buttonsTop, buttonHeight, gap, 3), (gridWidth - gap) / 2, buttonHeight)
                            .build()
            );

            MAGIC_CREATE_WORLD_PANELS.put(screen, new MagicCreateWorldPanel(screen, vanillaWidgets, magicTabButton, magicWidgets));
            syncAutomaticCommands(vanillaWidgets);

            event.addListener(magicTabButton);
            vanillaWidgets.add(magicTabButton);
            for (AbstractWidget widget : magicWidgets) {
                widget.visible = false;
                widget.active = false;
                event.addListener(widget);
            }
        }

        @SubscribeEvent
        public static void onScreenBackground(ScreenEvent.BackgroundRendered event) {
            if (shouldUseMagicBackground(event.getScreen())) {
                MagicWorldStaticBackground.draw(event.getGuiGraphics(), event.getScreen().width, event.getScreen().height);
            }
        }

        @SubscribeEvent
        public static void onScreenRenderPre(ScreenEvent.Render.Pre event) {
            if (shouldUseMagicBackground(event.getScreen())) {
                MagicWorldStaticBackground.draw(event.getGuiGraphics(), event.getScreen().width, event.getScreen().height);
            }
        }

        @SubscribeEvent
        public static void onScreenRenderPost(ScreenEvent.Render.Post event) {
            if (event.getScreen() instanceof CreateWorldScreen screen) {
                MagicCreateWorldPanel panel = MAGIC_CREATE_WORLD_PANELS.get(screen);
                if (panel != null) {
                    relayoutMagicPanel(panel);
                    updateMagicTabButton(panel);
                }
            }
        }

        @SubscribeEvent
        public static void onMouseButtonPressed(ScreenEvent.MouseButtonPressed.Pre event) {
            if (!(event.getScreen() instanceof CreateWorldScreen screen)) {
                return;
            }

            MagicCreateWorldPanel panel = MAGIC_CREATE_WORLD_PANELS.get(screen);
            if (panel != null && isMagicPanelVisible(panel) && event.getMouseY() < magicPanelLayout(screen).top()) {
                showMagicPanel(screen, false);
            }
        }

        private static void showMagicPanel(CreateWorldScreen screen, boolean show) {
            MagicCreateWorldPanel panel = MAGIC_CREATE_WORLD_PANELS.get(screen);
            if (panel == null) {
                return;
            }

            if (show) {
                relayoutMagicPanel(panel);
            }

            for (AbstractWidget widget : panel.vanillaWidgets()) {
                widget.visible = !show;
                widget.active = !show;
            }

            for (AbstractWidget widget : panel.magicWidgets()) {
                widget.visible = show;
                widget.active = show
                        && !(widget instanceof MagicCreateWorldBackdrop)
                        && !(widget instanceof MagicCreateWorldLineCover)
                        && !(widget instanceof MagicCreateWorldInfo)
                        && !(widget instanceof MagicCreateWorldTitle);
            }

            updateMagicTabButton(panel);
        }

        private static void updateMagicTabButton(MagicCreateWorldPanel panel) {
            boolean gameTab = isCreateWorldGameTab(panel.screen());
            if (!gameTab && isMagicPanelVisible(panel)) {
                showMagicPanel(panel.screen(), false);
            }

            if (!isMagicPanelVisible(panel)) {
                panel.magicTabButton().visible = gameTab;
                panel.magicTabButton().active = gameTab;
            }
        }

        private static MagicPanelLayout magicPanelLayout(CreateWorldScreen screen) {
            int gridWidth = Math.max(Math.min(MAGIC_PANEL_MIN_WIDTH, screen.width - 48), Math.min(MAGIC_PANEL_MAX_WIDTH, screen.width - 56));
            int gap = MAGIC_PANEL_GAP;
            int buttonWidth = (gridWidth - gap * 2) / 3;
            int left = screen.width / 2 - gridWidth / 2;
            int panelHeight = Math.min(MAGIC_PANEL_HEIGHT, Math.max(198, screen.height - 70));
            int top = Math.max(44, Math.min(screen.height - panelHeight - 14, screen.height / 2 - panelHeight / 2 - 26));
            int buttonsTop = top + panelHeight - (MAGIC_PANEL_BUTTON_HEIGHT * 4 + gap * 3) - 8;
            int lineCoverY = Math.max(0, top - 80);
            return new MagicPanelLayout(gridWidth, buttonWidth, left, top, buttonsTop, lineCoverY);
        }

        private static void relayoutMagicPanel(MagicCreateWorldPanel panel) {
            if (panel.magicWidgets().size() < 13) {
                return;
            }

            CreateWorldScreen screen = panel.screen();
            MagicPanelLayout layout = magicPanelLayout(screen);
            int gridWidth = layout.gridWidth();
            int gap = MAGIC_PANEL_GAP;
            int buttonWidth = layout.buttonWidth();
            int buttonHeight = MAGIC_PANEL_BUTTON_HEIGHT;
            int left = layout.left();
            int top = layout.top();
            int buttonsTop = layout.buttonsTop();

            panel.magicTabButton().setX(magicButtonX(screen, panel.vanillaWidgets()));
            panel.magicTabButton().setY(magicButtonY(panel.vanillaWidgets()));
            panel.magicTabButton().setWidth(magicButtonWidth(panel.vanillaWidgets()));

            setBounds(panel.magicWidgets().get(0), 0, layout.lineCoverY(), screen.width, 8);
            setBounds(panel.magicWidgets().get(1), left - 12, top - 10, gridWidth + 24, MAGIC_PANEL_HEIGHT + 20);
            setBounds(panel.magicWidgets().get(2), left, top + 2, gridWidth, 18);
            setBounds(panel.magicWidgets().get(3), left, top + 25, gridWidth, Math.max(72, buttonsTop - top - 34));
            setBounds(panel.magicWidgets().get(4), gridX(left, buttonWidth, gap, 0), gridY(buttonsTop, buttonHeight, gap, 0), buttonWidth, buttonHeight);
            setBounds(panel.magicWidgets().get(5), gridX(left, buttonWidth, gap, 1), gridY(buttonsTop, buttonHeight, gap, 0), buttonWidth, buttonHeight);
            setBounds(panel.magicWidgets().get(6), gridX(left, buttonWidth, gap, 0), gridY(buttonsTop, buttonHeight, gap, 1), buttonWidth, buttonHeight);
            setBounds(panel.magicWidgets().get(7), gridX(left, buttonWidth, gap, 1), gridY(buttonsTop, buttonHeight, gap, 1), buttonWidth, buttonHeight);
            setBounds(panel.magicWidgets().get(8), gridX(left, buttonWidth, gap, 2), gridY(buttonsTop, buttonHeight, gap, 0), buttonWidth, buttonHeight);
            setBounds(panel.magicWidgets().get(9), gridX(left, buttonWidth, gap, 2), gridY(buttonsTop, buttonHeight, gap, 1), buttonWidth, buttonHeight);
            setBounds(panel.magicWidgets().get(10), left, gridY(buttonsTop, buttonHeight, gap, 2), gridWidth, buttonHeight);
            setBounds(panel.magicWidgets().get(11), left, gridY(buttonsTop, buttonHeight, gap, 3), (gridWidth - gap) / 2, buttonHeight);
            setBounds(panel.magicWidgets().get(12), left + (gridWidth - gap) / 2 + gap, gridY(buttonsTop, buttonHeight, gap, 3), (gridWidth - gap) / 2, buttonHeight);
        }

        private static void setBounds(AbstractWidget widget, int x, int y, int width, int height) {
            widget.setX(x);
            widget.setY(y);
            widget.setWidth(width);
            widget.setHeight(height);
        }

        private static int gridX(int left, int buttonWidth, int gap, int column) {
            return left + column * (buttonWidth + gap);
        }

        private static int gridY(int top, int buttonHeight, int gap, int row) {
            return top + row * (buttonHeight + gap);
        }

        private static void createWorldFromMagicTab(CreateWorldScreen screen, List<AbstractWidget> vanillaWidgets) {
            MagicWorldWorldOptions.setCommandsEnabled(true);
            syncWorldCreationOptions(vanillaWidgets);
            try {
                Method onCreate = CreateWorldScreen.class.getDeclaredMethod("onCreate");
                onCreate.setAccessible(true);
                onCreate.invoke(screen);
            } catch (ReflectiveOperationException ignored) {
            }
        }

        private static void syncWorldCreationOptions(List<AbstractWidget> vanillaWidgets) {
            syncButtonByLabel(findButton(vanillaWidgets, "game mode", "modo de jogo"), MagicWorldWorldOptions.startingGameMode().labelMatches());
            syncButtonByLabel(findButton(vanillaWidgets, "difficulty", "dificuldade"), MagicWorldWorldOptions.startingDifficulty().labelMatches());
            syncAutomaticCommands(vanillaWidgets);
        }

        private static void forceCreateWorldAllowCommands(CreateWorldScreen screen) {
            try {
                var uiStateField = CreateWorldScreen.class.getDeclaredField("uiState");
                uiStateField.setAccessible(true);
                Object uiState = uiStateField.get(screen);
                Method setAllowCommands = uiState.getClass().getDeclaredMethod("setAllowCommands", boolean.class);
                setAllowCommands.setAccessible(true);
                setAllowCommands.invoke(uiState, true);
            } catch (ReflectiveOperationException ignored) {
            }
        }

        private static void syncAutomaticCommands(List<AbstractWidget> vanillaWidgets) {
            syncButtonByState(findAllowCommandsButton(vanillaWidgets), MagicWorldWorldOptions.isCommandsEnabled());
        }

        private static void syncButtonByLabel(AbstractWidget widget, String... wantedLabels) {
            if (!(widget instanceof Button button)) {
                return;
            }

            for (int attempt = 0; attempt < 6; attempt++) {
                if (messageContainsAny(button, wantedLabels)) {
                    return;
                }
                button.onPress();
            }
        }

        private static void syncButtonByState(AbstractWidget widget, boolean enabled) {
            if (!(widget instanceof Button button)) {
                return;
            }

            for (int attempt = 0; attempt < 4; attempt++) {
                boolean current = buttonStateLooksEnabled(button);
                if (current == enabled) {
                    return;
                }
                button.onPress();
            }
        }

        private static boolean buttonStateLooksEnabled(Button button) {
            String message = normalize(button.getMessage().getString());
            if (message.contains("off")
                    || message.contains("desativado")
                    || message.contains("desligado")
                    || message.contains("nao")
                    || message.contains("false")) {
                return false;
            }

            return message.contains("on")
                    || message.contains("sim")
                    || message.contains("ligado")
                    || message.contains("ativado")
                    || message.contains("enabled")
                    || message.contains("true");
        }

        private static boolean messageContainsAny(AbstractWidget widget, String... values) {
            String message = normalize(widget.getMessage().getString());
            for (String value : values) {
                if (message.contains(normalize(value))) {
                    return true;
                }
            }
            return false;
        }

        private static int magicButtonX(CreateWorldScreen screen, List<AbstractWidget> vanillaWidgets) {
            AbstractWidget allowCommandsButton = findAllowCommandsButton(vanillaWidgets);
            int width = magicButtonWidth(vanillaWidgets);
            if (allowCommandsButton != null) {
                return allowCommandsButton.getX() + allowCommandsButton.getWidth() / 2 - width / 2;
            }
            return screen.width / 2 - width / 2;
        }

        private static int magicButtonY(List<AbstractWidget> vanillaWidgets) {
            AbstractWidget allowCommandsButton = findAllowCommandsButton(vanillaWidgets);
            if (allowCommandsButton != null) {
                return allowCommandsButton.getY() + allowCommandsButton.getHeight() + verticalGapNear(allowCommandsButton, vanillaWidgets);
            }
            return 168;
        }

        private static int magicButtonWidth(List<AbstractWidget> vanillaWidgets) {
            AbstractWidget allowCommandsButton = findAllowCommandsButton(vanillaWidgets);
            return allowCommandsButton != null ? Math.min(allowCommandsButton.getWidth(), 320) : 320;
        }

        private static int verticalGapNear(AbstractWidget anchor, List<AbstractWidget> vanillaWidgets) {
            int bestGap = Integer.MAX_VALUE;
            for (AbstractWidget widget : vanillaWidgets) {
                if (widget == anchor || widget.getX() != anchor.getX() || widget.getWidth() != anchor.getWidth()) {
                    continue;
                }

                int gap = Math.abs(widget.getY() - anchor.getY()) - anchor.getHeight();
                if (gap > 0 && gap < bestGap) {
                    bestGap = gap;
                }
            }
            return bestGap == Integer.MAX_VALUE ? 8 : bestGap;
        }

        private static AbstractWidget findAllowCommandsButton(List<AbstractWidget> vanillaWidgets) {
            return findButton(vanillaWidgets, "allow commands", "allow cheats", "permitir comandos", "permitir cheats");
        }

        private static AbstractWidget findButton(List<AbstractWidget> vanillaWidgets, String... labels) {
            for (AbstractWidget widget : vanillaWidgets) {
                if (messageContainsAny(widget, labels)) {
                    return widget;
                }
            }
            return null;
        }

        private static Component magicWorldButtonLabel() {
            return Component.literal(MagicWorldWorldOptions.isStarterEstateEnabled() ? "Portal: ON" : "Portal: OFF");
        }

        private static Component auraButtonLabel() {
            return Component.literal(MagicWorldWorldOptions.isAuraEnabled() ? "Aura: ON" : "Aura: OFF");
        }

        private static Component castlesButtonLabel() {
            return Component.literal(MagicWorldWorldOptions.isCastlesEnabled() ? "Castelo: ON" : "Castelo: OFF");
        }

        private static Component farmsButtonLabel() {
            return Component.literal(MagicWorldWorldOptions.isFarmsEnabled() ? "Fazendas: ON" : "Fazendas: OFF");
        }

        private static Component hardwareButtonLabel() {
            MagicWorldGraphicsProfile[] profiles = MagicWorldGraphicsProfile.values();
            int index = Math.min(MagicWorldWorldOptions.hardwareProfileIndex(), profiles.length - 1);
            return Component.literal("PC: " + profiles[index].label());
        }

        private static Component difficultyButtonLabel() {
            return Component.literal("Dificuldade: " + MagicWorldWorldOptions.startingDifficulty().label());
        }

        private static Component gameModeButtonLabel() {
            return Component.literal("Modo: " + MagicWorldWorldOptions.startingGameMode().label());
        }

        private static void hideMagicPanelIfVanillaTabTookOver(MagicCreateWorldPanel panel) {
            if (!isMagicPanelVisible(panel)) {
                return;
            }

            int contentTop = magicPanelLayout(panel.screen()).top() - 8;
            for (GuiEventListener listener : panel.screen().children()) {
                if (listener instanceof AbstractWidget widget
                        && widget != panel.magicTabButton()
                        && !panel.magicWidgets().contains(widget)
                        && widget.visible
                        && widget.getY() >= contentTop) {
                    showMagicPanel(panel.screen(), false);
                    return;
                }
            }
        }

        private static boolean isMagicPanelVisible(MagicCreateWorldPanel panel) {
            for (AbstractWidget widget : panel.magicWidgets()) {
                if (widget.visible) {
                    return true;
                }
            }
            return false;
        }

        private static boolean isCreateWorldGameTab(CreateWorldScreen screen) {
            try {
                var tabManagerField = CreateWorldScreen.class.getDeclaredField("tabManager");
                tabManagerField.setAccessible(true);
                Object tabManager = tabManagerField.get(screen);
                Object tab = tabManager.getClass().getMethod("getCurrentTab").invoke(tabManager);
                if (tab == null) {
                    return true;
                }

                Object title = tab.getClass().getMethod("getTabTitle").invoke(tab);
                String titleText = title instanceof Component component ? component.getString() : String.valueOf(title);
                String normalized = normalize(titleText);
                return !normalized.contains("mundo")
                        && !normalized.contains("world")
                        && !normalized.contains("mais")
                        && !normalized.contains("more");
            } catch (ReflectiveOperationException ignored) {
                return true;
            }
        }

        private static boolean shouldUseMagicBackground(Object screen) {
            return screen instanceof net.minecraft.client.gui.screens.Screen minecraftScreen
                    && MagicWorldScreenBackgrounds.shouldUseStaticBackground(minecraftScreen);
        }

        private static String normalize(String text) {
            return text == null
                    ? ""
                    : text.toLowerCase()
                    .replace('ç', 'c')
                    .replace('õ', 'o')
                    .replace('ó', 'o')
                    .replace('á', 'a')
                    .replace('à', 'a')
                    .replace('é', 'e')
                    .replace('í', 'i')
                    .replace('ã', 'a');
        }
    }

    private static final class MagicCreateWorldBackdrop extends AbstractWidget {
        private MagicCreateWorldBackdrop(int x, int y, int width, int height) {
            super(x, y, width, height, Component.empty());
            active = false;
        }

        @Override
        protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            graphics.fill(getX(), getY(), getX() + getWidth(), getY() + getHeight(), 0xAA000000);
            graphics.renderOutline(getX(), getY(), getWidth(), getHeight(), 0x44D9A441);
            graphics.renderOutline(getX() + 3, getY() + 3, getWidth() - 6, getHeight() - 6, 0x3322D3FF);
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput narration) {
        }
    }

    private static final class MagicCreateWorldLineCover extends AbstractWidget {
        private MagicCreateWorldLineCover(int x, int y, int width, int height) {
            super(x, y, width, height, Component.empty());
            active = false;
        }

        @Override
        protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            graphics.fill(getX(), getY(), getX() + getWidth(), getY() + getHeight(), 0xBB000000);
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput narration) {
        }
    }

    private static final class MagicCreateWorldTitle extends AbstractWidget {
        private final Font font;

        private MagicCreateWorldTitle(int x, int y, int width, int height, Font font, Component message) {
            super(x, y, width, height, message);
            this.font = font;
            active = false;
        }

        @Override
        protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            int centerX = getX() + getWidth() / 2;
            int y = getY() + 5;
            Component title = getMessage();
            graphics.drawCenteredString(font, title, centerX + 1, y + 1, 0xBB000000);
            graphics.drawCenteredString(font, title, centerX, y, 0xFFFFFFFF);
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput narration) {
        }
    }

    private static final class MagicCreateWorldInfo extends AbstractWidget {
        private static final String[] LEFT_LINES = {
                "- Portal: casa + bau + portal.",
                "- Fazendas: villagers vanilla.",
                "- Castelo e aura opcionais."
        };
        private static final String[] RIGHT_LINES = {
                "- Modo: Normal ou Criativo.",
                "- Dificuldade: 4 niveis.",
                "- PC: fraco, medio ou forte."
        };

        private final Font font;

        private MagicCreateWorldInfo(int x, int y, int width, int height, Font font) {
            super(x, y, width, height, Component.empty());
            this.font = font;
            active = false;
        }

        @Override
        protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            int left = getX() + 14;
            int right = getX() + getWidth() / 2 + 10;
            int columnWidth = getWidth() / 2 - 24;
            int top = getY();

            drawSectionHeader(graphics, left, top, columnWidth, "MENUS");
            drawSectionHeader(graphics, right, top, columnWidth, "PERFIS DE PC");

            int lineY = top + 18;
            for (String line : LEFT_LINES) {
                graphics.drawString(font, line, left, lineY + 1, 0xAA000000);
                graphics.drawString(font, line, left, lineY, 0xFFE8F2FF);
                lineY += 14;
            }

            lineY = top + 18;
            for (String line : RIGHT_LINES) {
                graphics.drawString(font, line, right, lineY + 1, 0xAA000000);
                graphics.drawString(font, line, right, lineY, 0xFFE8F2FF);
                lineY += 14;
            }
        }

        private void drawSectionHeader(GuiGraphics graphics, int x, int y, int width, String text) {
            int center = x + width / 2;
            int textWidth = font.width(text);
            int lineY = y + 5;

            graphics.fill(x, lineY, center - textWidth / 2 - 8, lineY + 1, 0x88D9A441);
            graphics.fill(center + textWidth / 2 + 8, lineY, x + width, lineY + 1, 0x88D9A441);
            graphics.drawCenteredString(font, Component.literal(text), center + 1, y + 1, 0xAA000000);
            graphics.drawCenteredString(font, Component.literal(text), center, y, 0xFFFFE6A6);
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput narration) {
        }
    }
}
