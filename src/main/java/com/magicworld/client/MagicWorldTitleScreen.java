package com.magicworld.client;

import com.mojang.logging.LogUtils;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.options.OptionsScreen;
import net.minecraft.client.gui.screens.options.AccessibilityOptionsScreen;
import net.minecraft.client.gui.screens.options.LanguageSelectScreen;
import net.minecraft.client.gui.screens.options.controls.ControlsScreen;
import net.minecraft.client.gui.screens.packs.PackSelectionScreen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.gui.ModListScreen;
import org.slf4j.Logger;

public class MagicWorldTitleScreen extends Screen {
    private static final Logger LOGGER =
            LogUtils.getLogger();
    private static final int BUTTON_WIDTH = 148;
    private static final int BUTTON_HEIGHT = 20;
    private static final int BUTTON_GAP = 6;
    private static final int MENU_WIDTH = 300;
    private static final int QUICK_BUTTON_SIZE = 24;
    private static final int QUICK_BUTTON_GAP = 9;
    private static final int LOGO_SOURCE_WIDTH = 512;
    private static final int LOGO_SOURCE_HEIGHT = 171;
    private static final int TITLE_SCENE_WIDTH = 2560;
    private static final int TITLE_SCENE_HEIGHT = 1440;
    private static final Identifier TITLE_LOGO =
            Identifier.fromNamespaceAndPath(
                    "magicworld",
                    "textures/gui/title_logo.png"
            );
    private static final Identifier TITLE_BACKGROUND =
            Identifier.fromNamespaceAndPath(
                    "magicworld",
                    "textures/gui/title_background_static.png"
            );
    private static boolean logged;

    public MagicWorldTitleScreen() {
        super(Component.literal("Magic World"));

        if (!logged) {
            LOGGER.info(
                    "Magic World title screen active with static high resolution art."
            );
            logged = true;
        }
    }

    @Override
    protected void init() {
        int x =
                getMenuX() + (MENU_WIDTH - BUTTON_WIDTH) / 2;

        int y =
                getButtonY();

        addRenderableWidget(
                new MagicWorldMenuButton(
                        x,
                        y,
                        BUTTON_WIDTH,
                        BUTTON_HEIGHT,
                        Component.literal("JOGAR"),
                        () -> minecraft.setScreen(
                                new SelectWorldScreen(this)
                        )
                )
        );

        addRenderableWidget(
                new MagicWorldMenuButton(
                        x,
                        y + step(1),
                        BUTTON_WIDTH,
                        BUTTON_HEIGHT,
                        Component.literal("OP\u00c7\u00d5ES"),
                        () -> minecraft.setScreen(
                                new OptionsScreen(
                                        this,
                                        minecraft.options,
                                        false
                                )
                        )
                )
        );

        addRenderableWidget(
                new MagicWorldMenuButton(
                        x,
                        y + step(2),
                        BUTTON_WIDTH,
                        BUTTON_HEIGHT,
                        Component.literal("MODS"),
                        () -> minecraft.setScreen(
                                new ModListScreen(this)
                        )
                )
        );

        addRenderableWidget(
                new MagicWorldMenuButton(
                        x,
                        y + step(3),
                        BUTTON_WIDTH,
                        BUTTON_HEIGHT,
                        Component.literal("SAIR"),
                        () -> minecraft.stop()
                )
        );

        addQuickButtons();
    }

    @Override
    public void extractRenderState(
            GuiGraphicsExtractor graphics,
            int mouseX,
            int mouseY,
            float partialTick
    ) {
        drawStaticTitleScene(graphics);

        graphics.fill(
                0,
                0,
                width,
                height,
                0x1E010610
        );

        int menuX =
                getMenuX();

        drawLogo(
                graphics,
                menuX,
                getLogoY()
        );

        drawScreenOrnaments(
                graphics,
                width,
                height
        );

        super.extractRenderState(
                graphics,
                mouseX,
                mouseY,
                partialTick
        );
    }

    @Override
    public void extractBackground(
            GuiGraphicsExtractor graphics,
            int mouseX,
            int mouseY,
            float partialTick
    ) {
        // The title screen owns its full background so the shared menu mixin cannot draw a static image here.
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private void drawStaticTitleScene(
            GuiGraphicsExtractor graphics
    ) {
        minecraft.gameRenderer.getGameRenderState().guiRenderState.panoramaRenderState = null;

        drawCoverTexture(graphics, TITLE_BACKGROUND, 0, 0, 0);
    }

    private void drawCoverTexture(
            GuiGraphicsExtractor graphics,
            Identifier texture,
            int offsetX,
            int offsetY,
            int bleed
    ) {
        float screenRatio =
                width / (float) height;

        float textureRatio =
                TITLE_SCENE_WIDTH / (float) TITLE_SCENE_HEIGHT;

        int sourceX =
                0;

        int sourceY =
                0;

        int sourceWidth =
                TITLE_SCENE_WIDTH;

        int sourceHeight =
                TITLE_SCENE_HEIGHT;

        if (screenRatio > textureRatio) {
            sourceHeight =
                    Math.max(1, (int) (TITLE_SCENE_WIDTH / screenRatio));

            sourceY =
                    (TITLE_SCENE_HEIGHT - sourceHeight) / 2;
        } else {
            sourceWidth =
                    Math.max(1, (int) (TITLE_SCENE_HEIGHT * screenRatio));

            sourceX =
                    (TITLE_SCENE_WIDTH - sourceWidth) / 2;
        }

        graphics.blit(
                RenderPipelines.GUI_TEXTURED,
                texture,
                -bleed + offsetX,
                -bleed + offsetY,
                sourceX,
                sourceY,
                width + bleed * 2,
                height + bleed * 2,
                sourceWidth,
                sourceHeight,
                TITLE_SCENE_WIDTH,
                TITLE_SCENE_HEIGHT
        );
    }

    private static int step(
            int index
    ) {
        return index * (BUTTON_HEIGHT + BUTTON_GAP);
    }

    private int getMenuX() {
        return Math.max(18, (width - MENU_WIDTH) / 2);
    }

    private int getLogoY() {
        return Math.max(
                12,
                Math.round(height * 0.09F)
        );
    }

    private int getButtonY() {
        int logoHeight =
                getLogoWidth() * LOGO_SOURCE_HEIGHT / LOGO_SOURCE_WIDTH;

        int preferred =
                getLogoY() + logoHeight + 18;

        int max =
                height - step(4) - BUTTON_HEIGHT - QUICK_BUTTON_SIZE - 44;

        return Math.max(
                84,
                Math.min(preferred, max)
        );
    }

    private int getLogoWidth() {
        return Math.max(
                180,
                Math.min(MENU_WIDTH, width / 5)
        );
    }

    private int getQuickButtonY() {
        return getButtonY() + step(4) + 14;
    }

    private void addQuickButtons() {
        int count =
                5;

        int totalWidth =
                count * QUICK_BUTTON_SIZE + (count - 1) * QUICK_BUTTON_GAP;

        int startX =
                getMenuX() + (MENU_WIDTH - totalWidth) / 2;

        int y =
                getQuickButtonY();

        addRenderableWidget(
                new MagicWorldIconButton(
                        startX,
                        y,
                        QUICK_BUTTON_SIZE,
                        Component.literal("Multiplayer"),
                        MagicWorldIconButton.Icon.MULTIPLAYER,
                        () -> minecraft.setScreen(
                                new JoinMultiplayerScreen(this)
                        )
                )
        );

        addRenderableWidget(
                new MagicWorldIconButton(
                        startX + quickStep(1),
                        y,
                        QUICK_BUTTON_SIZE,
                        Component.literal("Idioma"),
                        MagicWorldIconButton.Icon.LANGUAGE,
                        () -> minecraft.setScreen(
                                new LanguageSelectScreen(
                                        this,
                                        minecraft.options,
                                        minecraft.getLanguageManager()
                                )
                        )
                )
        );

        addRenderableWidget(
                new MagicWorldIconButton(
                        startX + quickStep(2),
                        y,
                        QUICK_BUTTON_SIZE,
                        Component.literal("Controles"),
                        MagicWorldIconButton.Icon.CONTROLS,
                        () -> minecraft.setScreen(
                                new ControlsScreen(
                                        this,
                                        minecraft.options
                                )
                        )
                )
        );

        addRenderableWidget(
                new MagicWorldIconButton(
                        startX + quickStep(3),
                        y,
                        QUICK_BUTTON_SIZE,
                        Component.literal("Pacotes"),
                        MagicWorldIconButton.Icon.RESOURCE_PACKS,
                        () -> minecraft.setScreen(
                                new PackSelectionScreen(
                                        minecraft.getResourcePackRepository(),
                                        repository -> {
                                            minecraft.options.updateResourcePacks(repository);
                                            minecraft.setScreen(this);
                                        },
                                        minecraft.getResourcePackDirectory(),
                                        Component.translatable("resourcePack.title")
                                )
                        )
                )
        );

        addRenderableWidget(
                new MagicWorldIconButton(
                        startX + quickStep(4),
                        y,
                        QUICK_BUTTON_SIZE,
                        Component.literal("Acessibilidade"),
                        MagicWorldIconButton.Icon.ACCESSIBILITY,
                        () -> minecraft.setScreen(
                                new AccessibilityOptionsScreen(
                                        this,
                                        minecraft.options
                                )
                        )
                )
        );
    }

    private static int quickStep(
            int index
    ) {
        return index * (QUICK_BUTTON_SIZE + QUICK_BUTTON_GAP);
    }

    private void drawLogo(
            GuiGraphicsExtractor graphics,
            int x,
            int y
    ) {
        int logoWidth =
                getLogoWidth();

        int logoHeight =
                logoWidth * LOGO_SOURCE_HEIGHT / LOGO_SOURCE_WIDTH;

        graphics.blit(
                RenderPipelines.GUI_TEXTURED,
                TITLE_LOGO,
                x + (MENU_WIDTH - logoWidth) / 2,
                y,
                0,
                0,
                logoWidth,
                logoHeight,
                LOGO_SOURCE_WIDTH,
                LOGO_SOURCE_HEIGHT,
                LOGO_SOURCE_WIDTH,
                LOGO_SOURCE_HEIGHT
        );
    }

    private void drawScreenOrnaments(
            GuiGraphicsExtractor graphics,
            int screenWidth,
            int screenHeight
    ) {
        graphics.fill(0, 0, screenWidth, 1, MagicWorldMenuTheme.GOLD_DARK);
        graphics.fill(0, screenHeight - 1, screenWidth, screenHeight, MagicWorldMenuTheme.GOLD_DARK);
        graphics.fill(0, 0, 1, screenHeight, MagicWorldMenuTheme.GOLD_DARK);
        graphics.fill(screenWidth - 1, 0, screenWidth, screenHeight, MagicWorldMenuTheme.GOLD_DARK);

        drawCorner(graphics, 1, 1, 1, 1);
        drawCorner(graphics, screenWidth - 1, 1, -1, 1);
        drawCorner(graphics, 1, screenHeight - 1, 1, -1);
        drawCorner(graphics, screenWidth - 1, screenHeight - 1, -1, -1);
    }

    private void drawCorner(
            GuiGraphicsExtractor graphics,
            int x,
            int y,
            int xDirection,
            int yDirection
    ) {
        fillOriented(graphics, x, y, x + 18 * xDirection, y + yDirection, MagicWorldMenuTheme.GOLD);
        fillOriented(graphics, x, y, x + xDirection, y + 18 * yDirection, MagicWorldMenuTheme.GOLD);
        fillOriented(graphics, x + 5 * xDirection, y + 5 * yDirection, x + 14 * xDirection, y + 6 * yDirection, MagicWorldMenuTheme.BLUE);
        fillOriented(graphics, x + 5 * xDirection, y + 5 * yDirection, x + 6 * xDirection, y + 14 * yDirection, MagicWorldMenuTheme.BLUE);
    }

    private void fillOriented(
            GuiGraphicsExtractor graphics,
            int x1,
            int y1,
            int x2,
            int y2,
            int color
    ) {
        graphics.fill(
                Math.min(x1, x2),
                Math.min(y1, y2),
                Math.max(x1, x2),
                Math.max(y1, y2),
                color
        );
    }
}
