package com.magicworld.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.client.renderer.entity.EnderDragonRenderer;
import com.magicworld.MagicWorld;
import com.magicworld.MagicWorldWorldOptions;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.client.network.event.RegisterClientPayloadHandlersEvent;
import net.neoforged.neoforge.common.NeoForge;
import com.magicworld.network.MagicWorldNetwork;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.WeakHashMap;

public class ClientEvents {
    private record MagicCreateWorldPanel(
            CreateWorldScreen screen,
            List<AbstractWidget> vanillaWidgets,
            AbstractWidget magicTabButton,
            List<AbstractWidget> magicWidgets
    ) {
    }

    private record MagicPanelLayout(
            int gridWidth,
            int buttonWidth,
            int left,
            int top,
            int buttonsTop,
            int lineCoverY
    ) {
    }

    private static final WeakHashMap<CreateWorldScreen, MagicCreateWorldPanel> MAGIC_CREATE_WORLD_PANELS = new WeakHashMap<>();
    private static final int MAGIC_PANEL_MAX_WIDTH = 480;
    private static final int MAGIC_PANEL_MIN_WIDTH = 340;
    private static final int MAGIC_PANEL_GAP = 8;
    private static final int MAGIC_PANEL_BUTTON_HEIGHT = 20;
    private static final int MAGIC_PANEL_HEIGHT = 244;

    public static void registerListeners(
            IEventBus modEventBus
    ) {

        modEventBus.addListener(
                ClientEvents::registerKeys
        );

        modEventBus.addListener(
                ClientEvents::registerEntityRenderers
        );

        modEventBus.addListener(
                ClientEvents::registerClientPayloadHandlers
        );

        NeoForge.EVENT_BUS.addListener(
                ClientForgeEvents::onClientTick
        );

        NeoForge.EVENT_BUS.addListener(
                ClientForgeEvents::onScreenOpening
        );

        NeoForge.EVENT_BUS.addListener(
                ClientForgeEvents::onScreenInit
        );
    }

    public static void registerEntityRenderers(
            EntityRenderersEvent.RegisterRenderers event
    ) {

        event.registerEntityRenderer(
                MagicWorld.PEACEFUL_DRAGON.get(),
                EnderDragonRenderer::new
        );
    }

    public static void registerKeys(
            RegisterKeyMappingsEvent event
    ) {

        KeyBindings.register();

        event.register(
                KeyBindings.OPEN_MENU_KEY
        );
    }

    public static void registerClientPayloadHandlers(
            RegisterClientPayloadHandlersEvent event
    ) {
        event.register(
                MagicWorldNetwork.OpenPremiumPortalOptionsPayload.TYPE,
                (payload, context) -> context.enqueueWork(() ->
                        Minecraft.getInstance().setScreen(new PremiumPortalOptionsScreen())
                )
        );
        event.register(
                MagicWorldNetwork.OpenInitialLoadNoticePayload.TYPE,
                (payload, context) -> context.enqueueWork(() -> {
                    InitialLoadNoticeScreen.resetProgress();
                    Minecraft.getInstance().setScreen(new InitialLoadNoticeScreen());
                })
        );
        event.register(
                MagicWorldNetwork.InitialLoadProgressPayload.TYPE,
                (payload, context) -> context.enqueueWork(() ->
                        InitialLoadNoticeScreen.updateProgress(
                                Minecraft.getInstance(),
                                payload.progress(),
                                payload.message(),
                                payload.complete()
                        )
                )
        );
        event.register(
                MagicWorldNetwork.ApplyPremiumPortalVisualPayload.TYPE,
                (payload, context) -> context.enqueueWork(() ->
                        MagicWorldPortalVisualController.applyPortalSelection(
                                payload.active(),
                                payload.resourcePack(),
                                payload.shaderPack()
                        )
                )
        );
    }

    public static class ClientForgeEvents {

        public static void onScreenOpening(
                ScreenEvent.Opening event
        ) {

            if (event.getNewScreen() instanceof CreateWorldScreen) {
                MagicWorldWorldOptions.setStarterEstateEnabled(true);
                MagicWorldWorldOptions.setCastlesEnabled(true);
                MagicWorldWorldOptions.setAuraEnabled(true);
                MagicWorldWorldOptions.setCommandsEnabled(true);
                MagicWorldWorldOptions.setHardwareProfileIndex(3);
                MagicWorldWorldOptions.setStartingGameMode(MagicWorldWorldOptions.StartingGameMode.SURVIVAL);
                MagicWorldWorldOptions.setStartingDifficulty(MagicWorldWorldOptions.StartingDifficulty.NORMAL);
            }

            if (event.getNewScreen() instanceof TitleScreen) {
                event.setNewScreen(
                        new MagicWorldTitleScreen()
                );
            }
        }

        public static void onScreenInit(
                ScreenEvent.Init.Post event
        ) {

            if (!(event.getScreen() instanceof CreateWorldScreen screen)) {
                return;
            }

            List<AbstractWidget> vanillaWidgets = new ArrayList<>();
            for (var listener : event.getListenersList()) {
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
                    .bounds(
                            magicButtonX(screen, vanillaWidgets),
                            magicButtonY(vanillaWidgets),
                            magicButtonWidth(vanillaWidgets),
                            MAGIC_PANEL_BUTTON_HEIGHT
                    )
                    .tooltip(Tooltip.create(Component.literal(
                            "Abre as opcoes Magic World da criacao do mundo."
                    )))
                    .build();

            int gridWidth = layout.gridWidth();
            int gap = MAGIC_PANEL_GAP;
            int buttonWidth = layout.buttonWidth();
            int buttonHeight = MAGIC_PANEL_BUTTON_HEIGHT;
            int left = layout.left();
            int top = layout.top();
            int buttonsTop = layout.buttonsTop();

            MagicCreateWorldLineCover lineCover = new MagicCreateWorldLineCover(
                    0,
                    layout.lineCoverY(),
                    screen.width,
                    8
            );
            MagicCreateWorldBackdrop backdrop = new MagicCreateWorldBackdrop(
                    left - 12,
                    top - 10,
                    gridWidth + 24,
                    MAGIC_PANEL_HEIGHT + 20
            );

            MagicCreateWorldTitle title = new MagicCreateWorldTitle(
                    left,
                    top + 2,
                    gridWidth,
                    18,
                    screen.getFont(),
                    Component.literal("MAGIC WORLD")
            );

            MagicCreateWorldInfo helpText = new MagicCreateWorldInfo(
                    left,
                    top + 25,
                    gridWidth,
                    Math.max(72, buttonsTop - top - 34),
                    screen.getFont()
            );

            Button magicWorldButton = Button.builder(
                            magicWorldButtonLabel(),
                            pressed -> {
                                MagicWorldWorldOptions.toggleStarterEstateEnabled();
                                syncAutomaticCommands(vanillaWidgets);
                                pressed.setMessage(magicWorldButtonLabel());
                            }
                    )
                    .bounds(
                            gridX(left, buttonWidth, gap, 0),
                            gridY(buttonsTop, buttonHeight, gap, 0),
                            buttonWidth,
                            buttonHeight
                    )
                    .tooltip(Tooltip.create(Component.literal(
                            "ON: cria o portal, a casa do personagem e o terreno inicial ao redor."
                    )))
                    .build();

            Button castlesButton = Button.builder(
                            castlesButtonLabel(),
                            pressed -> {
                                MagicWorldWorldOptions.toggleCastlesEnabled();
                                syncAutomaticCommands(vanillaWidgets);
                                pressed.setMessage(castlesButtonLabel());
                            }
                    )
                    .bounds(
                            gridX(left, buttonWidth, gap, 1),
                            gridY(buttonsTop, buttonHeight, gap, 0),
                            buttonWidth,
                            buttonHeight
                    )
                    .tooltip(Tooltip.create(Component.literal(
                            "ON: cria estradas e castelos perto do mundo inicial. OFF: entrada mais leve, sem castelos."
                    )))
                    .build();

            Button auraButton = Button.builder(
                            auraButtonLabel(),
                            pressed -> {
                                MagicWorldWorldOptions.toggleAuraEnabled();
                                syncAutomaticCommands(vanillaWidgets);
                                pressed.setMessage(auraButtonLabel());
                            }
                    )
                    .bounds(
                            gridX(left, buttonWidth, gap, 2),
                            gridY(buttonsTop, buttonHeight, gap, 0),
                            buttonWidth,
                            buttonHeight
                    )
                    .tooltip(Tooltip.create(Component.literal(
                            "ON: ativa poderes do jogador, protecoes, quebra forte e retorno ao local da morte."
                    )))
                    .build();

            Button hardwareButton = Button.builder(
                            hardwareButtonLabel(),
                            pressed -> {
                                MagicWorldGraphicsProfile[] profiles = MagicWorldGraphicsProfile.values();
                                int index = MagicWorldWorldOptions.nextHardwareProfileIndex(profiles.length);
                                profiles[index].apply(Minecraft.getInstance());
                                pressed.setMessage(hardwareButtonLabel());
                            }
                    )
                    .bounds(
                            gridX(left, buttonWidth, gap, 0),
                            gridY(buttonsTop, buttonHeight, gap, 1),
                            buttonWidth,
                            buttonHeight
                    )
                    .tooltip(Tooltip.create(Component.literal(
                            "Troca entre perfis de PC e aplica distancia, graficos, particulas, nuvens e FPS."
                    )))
                    .build();

            Button gameModeButton = Button.builder(
                            gameModeButtonLabel(),
                            pressed -> {
                                MagicWorldWorldOptions.nextStartingGameMode();
                                pressed.setMessage(gameModeButtonLabel());
                            }
                    )
                    .bounds(
                            gridX(left, buttonWidth, gap, 1),
                            gridY(buttonsTop, buttonHeight, gap, 1),
                            buttonWidth,
                            buttonHeight
                    )
                    .tooltip(Tooltip.create(Component.literal(
                            "Escolhe se o mundo vai iniciar em modo normal ou criativo."
                    )))
                    .build();

            Button difficultyButton = Button.builder(
                            difficultyButtonLabel(),
                            pressed -> {
                                MagicWorldWorldOptions.nextStartingDifficulty();
                                pressed.setMessage(difficultyButtonLabel());
                            }
                    )
                    .bounds(
                            gridX(left, buttonWidth, gap, 2),
                            gridY(buttonsTop, buttonHeight, gap, 1),
                            buttonWidth,
                            buttonHeight
                    )
                    .tooltip(Tooltip.create(Component.literal(
                            "Escolhe a dificuldade inicial do mundo."
                    )))
                    .build();

            Button createWorldButton = Button.builder(
                            Component.literal("Criar Mundo"),
                            pressed -> createWorldFromMagicTab(screen, vanillaWidgets)
                    )
                    .bounds(
                            gridX(left, buttonWidth, gap, 0),
                            gridY(buttonsTop, buttonHeight, gap, 2),
                            buttonWidth * 2 + gap,
                            buttonHeight
                    )
                    .build();

            Button backButton = Button.builder(
                            Component.literal("Voltar"),
                            pressed -> showMagicPanel(screen, false)
                    )
                    .bounds(
                            gridX(left, buttonWidth, gap, 2),
                            gridY(buttonsTop, buttonHeight, gap, 2),
                            buttonWidth,
                            buttonHeight
                    )
                    .build();

            List<AbstractWidget> magicWidgets = List.of(
                    lineCover,
                    backdrop,
                    title,
                    helpText,
                    magicWorldButton,
                    castlesButton,
                    auraButton,
                    hardwareButton,
                    gameModeButton,
                    difficultyButton,
                    createWorldButton,
                    backButton
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
                widget.active = show && !(widget instanceof MagicCreateWorldBackdrop) && !(widget instanceof MagicCreateWorldLineCover) && !(widget instanceof MagicCreateWorldInfo) && !(widget instanceof MagicCreateWorldTitle);
            }
        }

        private static MagicPanelLayout magicPanelLayout(CreateWorldScreen screen) {
            int gridWidth = Math.max(
                    Math.min(MAGIC_PANEL_MIN_WIDTH, screen.width - 48),
                    Math.min(MAGIC_PANEL_MAX_WIDTH, screen.width - 56)
            );
            int gap = MAGIC_PANEL_GAP;
            int buttonWidth = (gridWidth - gap * 2) / 3;
            int left = screen.width / 2 - gridWidth / 2;
            int panelHeight = Math.min(MAGIC_PANEL_HEIGHT, Math.max(198, screen.height - 70));
            int top = Math.max(44, Math.min(screen.height - panelHeight - 14, screen.height / 2 - panelHeight / 2 - 26));
            int buttonsTop = top + panelHeight - (MAGIC_PANEL_BUTTON_HEIGHT * 3 + gap * 2) - 8;
            int lineCoverY = Math.max(0, top - 80);
            return new MagicPanelLayout(gridWidth, buttonWidth, left, top, buttonsTop, lineCoverY);
        }

        private static void relayoutMagicPanel(MagicCreateWorldPanel panel) {
            if (panel.magicWidgets().size() < 12) {
                return;
            }

            CreateWorldScreen screen = panel.screen();
            MagicPanelLayout layout = magicPanelLayout(screen);
            int gap = MAGIC_PANEL_GAP;
            int buttonHeight = MAGIC_PANEL_BUTTON_HEIGHT;

            setWidgetBounds(
                    panel.magicTabButton(),
                    magicButtonX(screen, panel.vanillaWidgets()),
                    magicButtonY(panel.vanillaWidgets()),
                    magicButtonWidth(panel.vanillaWidgets()),
                    MAGIC_PANEL_BUTTON_HEIGHT
            );
            setWidgetBounds(panel.magicWidgets().get(0), 0, layout.lineCoverY(), screen.width, 8);
            setWidgetBounds(panel.magicWidgets().get(1), layout.left() - 12, layout.top() - 10, layout.gridWidth() + 24, layout.buttonsTop() - layout.top() + MAGIC_PANEL_BUTTON_HEIGHT * 3 + MAGIC_PANEL_GAP * 2 + 18);
            setWidgetBounds(panel.magicWidgets().get(2), layout.left(), layout.top() + 2, layout.gridWidth(), 18);
            setWidgetBounds(panel.magicWidgets().get(3), layout.left(), layout.top() + 25, layout.gridWidth(), Math.max(72, layout.buttonsTop() - layout.top() - 34));
            setWidgetBounds(panel.magicWidgets().get(4), gridX(layout.left(), layout.buttonWidth(), gap, 0), gridY(layout.buttonsTop(), buttonHeight, gap, 0), layout.buttonWidth(), buttonHeight);
            setWidgetBounds(panel.magicWidgets().get(5), gridX(layout.left(), layout.buttonWidth(), gap, 1), gridY(layout.buttonsTop(), buttonHeight, gap, 0), layout.buttonWidth(), buttonHeight);
            setWidgetBounds(panel.magicWidgets().get(6), gridX(layout.left(), layout.buttonWidth(), gap, 2), gridY(layout.buttonsTop(), buttonHeight, gap, 0), layout.buttonWidth(), buttonHeight);
            setWidgetBounds(panel.magicWidgets().get(7), gridX(layout.left(), layout.buttonWidth(), gap, 0), gridY(layout.buttonsTop(), buttonHeight, gap, 1), layout.buttonWidth(), buttonHeight);
            setWidgetBounds(panel.magicWidgets().get(8), gridX(layout.left(), layout.buttonWidth(), gap, 1), gridY(layout.buttonsTop(), buttonHeight, gap, 1), layout.buttonWidth(), buttonHeight);
            setWidgetBounds(panel.magicWidgets().get(9), gridX(layout.left(), layout.buttonWidth(), gap, 2), gridY(layout.buttonsTop(), buttonHeight, gap, 1), layout.buttonWidth(), buttonHeight);
            setWidgetBounds(panel.magicWidgets().get(10), gridX(layout.left(), layout.buttonWidth(), gap, 0), gridY(layout.buttonsTop(), buttonHeight, gap, 2), layout.buttonWidth() * 2 + gap, buttonHeight);
            setWidgetBounds(panel.magicWidgets().get(11), gridX(layout.left(), layout.buttonWidth(), gap, 2), gridY(layout.buttonsTop(), buttonHeight, gap, 2), layout.buttonWidth(), buttonHeight);
        }

        private static void setWidgetBounds(AbstractWidget widget, int x, int y, int width, int height) {
            widget.setX(x);
            widget.setY(y);
            widget.setWidth(width);
            widget.setHeight(height);
        }

        private static int gridX(int left, int buttonWidth, int gap, int column) {
            return left + column * (buttonWidth + gap);
        }

        private static int gridY(int buttonsTop, int buttonHeight, int gap, int row) {
            return buttonsTop + row * (buttonHeight + gap);
        }

        private static void createWorldFromMagicTab(CreateWorldScreen screen, List<AbstractWidget> vanillaWidgets) {
            syncWorldCreationOptions(vanillaWidgets);
            try {
                Method onCreate = CreateWorldScreen.class.getDeclaredMethod("onCreate");
                onCreate.setAccessible(true);
                onCreate.invoke(screen);
            } catch (ReflectiveOperationException exception) {
                Minecraft.getInstance().setScreen(screen);
            }
        }

        private static void syncWorldCreationOptions(List<AbstractWidget> vanillaWidgets) {
            syncButtonByLabel(
                    findButton(vanillaWidgets, "game mode", "modo de jogo"),
                    MagicWorldWorldOptions.startingGameMode().labelMatches()
            );
            syncButtonByLabel(
                    findButton(vanillaWidgets, "difficulty", "dificuldade"),
                    MagicWorldWorldOptions.startingDifficulty().labelMatches()
            );
            syncAutomaticCommands(vanillaWidgets);
        }

        private static void syncAutomaticCommands(List<AbstractWidget> vanillaWidgets) {
            syncButtonByState(
                    findAllowCommandsButton(vanillaWidgets),
                    MagicWorldWorldOptions.isCommandsEnabled()
            );
        }

        private static void syncButtonByLabel(AbstractWidget widget, String... wantedLabels) {
            if (!(widget instanceof Button button)) {
                return;
            }

            for (int attempt = 0; attempt < 6; attempt++) {
                if (messageContainsAny(button, wantedLabels)) {
                    return;
                }

                button.onPress(null);
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
                button.onPress(null);
            }
        }

        private static boolean buttonStateLooksEnabled(Button button) {
            String message = button.getMessage().getString().toLowerCase();
            if (message.contains("off")
                    || message.contains("desativado")
                    || message.contains("desligado")
                    || message.contains("nao")
                    || message.contains("não")
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
            String message = widget.getMessage().getString().toLowerCase();
            for (String value : values) {
                if (message.contains(value)) {
                    return true;
                }
            }

            return false;
        }

        private static int magicButtonX(CreateWorldScreen screen, List<AbstractWidget> vanillaWidgets) {
            AbstractWidget allowCommandsButton = findAllowCommandsButton(vanillaWidgets);
            if (allowCommandsButton != null) {
                int width = magicButtonWidth(vanillaWidgets);
                return allowCommandsButton.getX() + allowCommandsButton.getWidth() / 2 - width / 2;
            }

            int width = magicButtonWidth(vanillaWidgets);
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
            return Component.literal(
                    MagicWorldWorldOptions.isStarterEstateEnabled()
                            ? "Portal: ON"
                            : "Portal: OFF"
            );
        }

        private static Component auraButtonLabel() {
            return Component.literal(
                    MagicWorldWorldOptions.isAuraEnabled()
                            ? "Aura: ON"
                            : "Aura: OFF"
            );
        }

        private static Component castlesButtonLabel() {
            return Component.literal(
                    MagicWorldWorldOptions.isCastlesEnabled()
                            ? "Castelo: ON"
                            : "Castelo: OFF"
            );
        }

        private static Component hardwareButtonLabel() {
            MagicWorldGraphicsProfile[] profiles = MagicWorldGraphicsProfile.values();
            int index = Math.min(MagicWorldWorldOptions.hardwareProfileIndex(), profiles.length - 1);
            return Component.literal("PC: " + profiles[index].label());
        }

        private static Component gameModeButtonLabel() {
            return Component.literal("Modo: " + MagicWorldWorldOptions.startingGameMode().label());
        }

        private static Component difficultyButtonLabel() {
            return Component.literal("Dificuldade: " + MagicWorldWorldOptions.startingDifficulty().label());
        }

        public static void onClientTick(
                ClientTickEvent.Post event
        ) {

            Minecraft minecraft =
                    Minecraft.getInstance();

            MagicWorldPortalVisualController.onClientTick(minecraft);

            if (minecraft.screen instanceof TitleScreen) {
                minecraft.setScreen(
                        new MagicWorldTitleScreen()
                );
                return;
            }

            if (minecraft.screen instanceof CreateWorldScreen screen) {
                MagicCreateWorldPanel panel = MAGIC_CREATE_WORLD_PANELS.get(screen);
                if (panel != null) {
                    relayoutMagicPanel(panel);
                }
            }

            while (KeyBindings.OPEN_MENU_KEY
                    .consumeClick()) {

                if (minecraft.screen instanceof PremiumMenuScreen) {
                    minecraft.setScreen(null);
                    continue;
                }

                if (minecraft.player == null
                        || minecraft.level == null) {
                    continue;
                }

                if (minecraft.screen != null) {
                    continue;
                }

                minecraft.setScreen(
                        new PremiumMenuScreen()
                );
            }
        }
    }

    private static final class MagicCreateWorldBackdrop extends AbstractWidget {
        private MagicCreateWorldBackdrop(int x, int y, int width, int height) {
            super(x, y, width, height, Component.empty());
            active = false;
        }

        @Override
        protected void extractWidgetRenderState(
                GuiGraphicsExtractor graphics,
                int mouseX,
                int mouseY,
                float partialTick
        ) {
            graphics.fill(getX(), getY(), getX() + getWidth(), getY() + getHeight(), 0xAA000000);
            graphics.outline(getX(), getY(), getWidth(), getHeight(), 0x44D9A441);
            graphics.outline(getX() + 3, getY() + 3, getWidth() - 6, getHeight() - 6, 0x3322D3FF);
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
        protected void extractWidgetRenderState(
                GuiGraphicsExtractor graphics,
                int mouseX,
                int mouseY,
                float partialTick
        ) {
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
        protected void extractWidgetRenderState(
                GuiGraphicsExtractor graphics,
                int mouseX,
                int mouseY,
                float partialTick
        ) {
            int centerX = getX() + getWidth() / 2;
            int y = getY() + 5;
            Component title = getMessage();

            graphics.centeredText(font, title, centerX + 1, y + 1, 0xBB000000);
            graphics.centeredText(font, title, centerX, y, 0xFFFFFFFF);
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput narration) {
        }
    }

    private static final class MagicCreateWorldInfo extends AbstractWidget {
        private static final String[] LEFT_LINES = {
                "- Portal: casa + bau + portal.",
                "- Castelo: estradas e castelos.",
                "- Aura: poderes e protecao.",
                "- Modo: Normal ou Criativo."
        };
        private static final String[] RIGHT_LINES = {
                "- Dificuldade: 4 niveis.",
                "- Fraco: 4-6 chunks, minimo.",
                "- Medio: 8-10 chunks, FPS.",
                "- Forte: 14-20 chunks, visual."
        };

        private final Font font;

        private MagicCreateWorldInfo(int x, int y, int width, int height, Font font) {
            super(x, y, width, height, Component.empty());
            this.font = font;
            active = false;
        }

        @Override
        protected void extractWidgetRenderState(
                GuiGraphicsExtractor graphics,
                int mouseX,
                int mouseY,
                float partialTick
        ) {
            int left = getX() + 14;
            int right = getX() + getWidth() / 2 + 10;
            int columnWidth = getWidth() / 2 - 24;
            int top = getY();

            drawSectionHeader(graphics, left, top, columnWidth, "MENUS");
            drawSectionHeader(graphics, right, top, columnWidth, "PERFIS DE PC");

            int lineY = top + 18;
            for (String line : LEFT_LINES) {
                graphics.text(font, line, left, lineY + 1, 0xAA000000);
                graphics.text(font, line, left, lineY, 0xFFE8F2FF);
                lineY += 14;
            }

            lineY = top + 18;
            for (String line : RIGHT_LINES) {
                graphics.text(font, line, right, lineY + 1, 0xAA000000);
                graphics.text(font, line, right, lineY, 0xFFE8F2FF);
                lineY += 14;
            }

            int noteY = getY() + getHeight() - 16;
            String note = "Comandos: ON automatico com Portal, Castelo ou Aura.";
            int noteX = getX() + getWidth() / 2 - font.width(note) / 2;
            graphics.fill(getX() + 28, noteY - 4, getX() + getWidth() - 28, noteY + 12, 0x33000000);
            graphics.text(font, note, noteX + 1, noteY + 1, 0xAA000000);
            graphics.text(font, note, noteX, noteY, 0xFFFFE6A6);
        }

        private void drawSectionHeader(
                GuiGraphicsExtractor graphics,
                int x,
                int y,
                int width,
                String text
        ) {
            int center = x + width / 2;
            int textWidth = font.width(text);
            int lineY = y + 5;

            graphics.fill(x, lineY, center - textWidth / 2 - 8, lineY + 1, 0x88D9A441);
            graphics.fill(center + textWidth / 2 + 8, lineY, x + width, lineY + 1, 0x88D9A441);
            graphics.centeredText(font, Component.literal(text), center + 1, y + 1, 0xAA000000);
            graphics.centeredText(font, Component.literal(text), center, y, 0xFFFFE6A6);
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput narration) {
        }
    }
}
