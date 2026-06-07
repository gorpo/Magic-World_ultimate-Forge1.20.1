package com.magicworld.client;

import com.magicworld.network.MagicWorldNetwork;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class PremiumPortalOptionsScreen extends Screen {
    private static final int ROWS_PER_PAGE = 7;

    private final List<String> selectedResourcePacks = new ArrayList<>();
    private String selectedShaderPack;
    private Popup popup = Popup.NONE;
    private int resourcePage;
    private int shaderPage;

    private enum Popup {
        NONE,
        RESOURCES,
        SHADERS
    }

    public PremiumPortalOptionsScreen() {
        super(Component.literal("Portal Premium"));
        selectedResourcePacks.addAll(MagicWorldPortalVisualController.defaultResourcePackIds());
        selectedShaderPack = MagicWorldPortalVisualController.defaultShaderPackName();
    }

    @Override
    protected void init() {
        if (popup == Popup.RESOURCES) {
            initResourcePopup();
            return;
        }
        if (popup == Popup.SHADERS) {
            initShaderPopup();
            return;
        }
        initMainButtons();
    }

    private void initMainButtons() {
        int panelWidth = Math.min(390, width - 32);
        int left = width / 2 - panelWidth / 2;
        int top = Math.max(12, height / 2 - 142);
        int buttonWidth = panelWidth - 64;
        int buttonLeft = left + 32;

        addRenderableWidget(Button.builder(
                        Component.literal("Escolher ResourcePacks (" + selectedResourcePacks.size() + ")"),
                        button -> openPopup(Popup.RESOURCES)
                )
                .bounds(buttonLeft, top + 54, buttonWidth, 22)
                .build());

        addRenderableWidget(Button.builder(
                        Component.literal("Escolher Shader: " + shaderLabel()),
                        button -> openPopup(Popup.SHADERS)
                )
                .bounds(buttonLeft, top + 84, buttonWidth, 22)
                .build());

        addRenderableWidget(Button.builder(
                        Component.literal("Aplicar ResourcePacks"),
                        button -> confirm(true, false, false)
                )
                .bounds(buttonLeft, top + 114, buttonWidth, 22)
                .build());

        addRenderableWidget(Button.builder(
                        Component.literal("Aplicar ShaderPack"),
                        button -> confirm(false, true, false)
                )
                .bounds(buttonLeft, top + 144, buttonWidth, 22)
                .build());

        addRenderableWidget(Button.builder(
                        Component.literal("Aplicar Shader + Resource"),
                        button -> confirm(false, false, true)
                )
                .bounds(buttonLeft, top + 174, buttonWidth, 22)
                .build());

        addRenderableWidget(Button.builder(
                        Component.literal("Remover efeitos"),
                        button -> confirm(false, false, false)
                )
                .bounds(buttonLeft, top + 204, buttonWidth, 22)
                .build());

        addRenderableWidget(Button.builder(
                        Component.literal("Cancelar"),
                        button -> onClose()
                )
                .bounds(buttonLeft, top + 236, buttonWidth, 20)
                .build());
    }

    private void initResourcePopup() {
        List<MagicWorldPortalVisualController.ResourcePackChoice> packs = MagicWorldPortalVisualController.availableResourcePacks();
        int panelWidth = Math.min(470, width - 32);
        int left = width / 2 - panelWidth / 2;
        int top = Math.max(10, height / 2 - 148);
        int buttonLeft = left + 22;
        int buttonWidth = panelWidth - 44;
        int start = resourcePage * ROWS_PER_PAGE;
        int end = Math.min(packs.size(), start + ROWS_PER_PAGE);

        for (int i = start; i < end; i++) {
            MagicWorldPortalVisualController.ResourcePackChoice pack = packs.get(i);
            int row = i - start;
            addRenderableWidget(Button.builder(
                            Component.literal((selectedResourcePacks.contains(pack.id()) ? "[X] " : "[ ] ") + pack.name()),
                            button -> {
                                toggleResourcePack(pack.id());
                                rebuild();
                            }
                    )
                    .bounds(buttonLeft, top + 54 + row * 24, buttonWidth, 20)
                    .build());
        }

        int navY = top + 226;
        addRenderableWidget(Button.builder(Component.literal("<"), button -> {
                    resourcePage = Math.max(0, resourcePage - 1);
                    rebuild();
                })
                .bounds(buttonLeft, navY, 42, 20)
                .build());
        addRenderableWidget(Button.builder(Component.literal("Padrao Magic World"), button -> {
                    selectedResourcePacks.clear();
                    selectedResourcePacks.addAll(MagicWorldPortalVisualController.defaultResourcePackIds());
                    resourcePage = 0;
                    rebuild();
                })
                .bounds(buttonLeft + 50, navY, buttonWidth - 100, 20)
                .build());
        addRenderableWidget(Button.builder(Component.literal(">"), button -> {
                    int maxPage = Math.max(0, (packs.size() - 1) / ROWS_PER_PAGE);
                    resourcePage = Math.min(maxPage, resourcePage + 1);
                    rebuild();
                })
                .bounds(buttonLeft + buttonWidth - 42, navY, 42, 20)
                .build());
        addRenderableWidget(Button.builder(Component.literal("Concluir"), button -> openPopup(Popup.NONE))
                .bounds(buttonLeft, navY + 28, buttonWidth, 20)
                .build());
    }

    private void initShaderPopup() {
        List<MagicWorldPortalVisualController.ShaderPackChoice> shaders = MagicWorldPortalVisualController.availableShaderPacks();
        int panelWidth = Math.min(470, width - 32);
        int left = width / 2 - panelWidth / 2;
        int top = Math.max(10, height / 2 - 148);
        int buttonLeft = left + 22;
        int buttonWidth = panelWidth - 44;
        int start = shaderPage * ROWS_PER_PAGE;
        int end = Math.min(shaders.size(), start + ROWS_PER_PAGE);

        addRenderableWidget(Button.builder(Component.literal((selectedShaderPack.isBlank() ? "[X] " : "[ ] ") + "Sem shader"), button -> {
                    selectedShaderPack = "";
                    rebuild();
                })
                .bounds(buttonLeft, top + 54, buttonWidth, 20)
                .build());

        for (int i = start; i < end; i++) {
            MagicWorldPortalVisualController.ShaderPackChoice shader = shaders.get(i);
            int row = i - start + 1;
            addRenderableWidget(Button.builder(
                            Component.literal((shader.name().equals(selectedShaderPack) ? "[X] " : "[ ] ") + shader.name()),
                            button -> {
                                selectedShaderPack = shader.name();
                                rebuild();
                            }
                    )
                    .bounds(buttonLeft, top + 54 + row * 24, buttonWidth, 20)
                    .build());
        }

        int navY = top + 226;
        addRenderableWidget(Button.builder(Component.literal("<"), button -> {
                    shaderPage = Math.max(0, shaderPage - 1);
                    rebuild();
                })
                .bounds(buttonLeft, navY, 42, 20)
                .build());
        addRenderableWidget(Button.builder(Component.literal("Shader Magic World"), button -> {
                    selectedShaderPack = MagicWorldPortalVisualController.defaultShaderPackName();
                    shaderPage = 0;
                    rebuild();
                })
                .bounds(buttonLeft + 50, navY, buttonWidth - 100, 20)
                .build());
        addRenderableWidget(Button.builder(Component.literal(">"), button -> {
                    int maxPage = Math.max(0, (shaders.size() - 1) / ROWS_PER_PAGE);
                    shaderPage = Math.min(maxPage, shaderPage + 1);
                    rebuild();
                })
                .bounds(buttonLeft + buttonWidth - 42, navY, 42, 20)
                .build());
        addRenderableWidget(Button.builder(Component.literal("Concluir"), button -> openPopup(Popup.NONE))
                .bounds(buttonLeft, navY + 28, buttonWidth, 20)
                .build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);

        int panelWidth = popup == Popup.NONE ? Math.min(390, width - 32) : Math.min(470, width - 32);
        int panelHeight = popup == Popup.NONE ? 274 : 292;
        int left = width / 2 - panelWidth / 2;
        int top = popup == Popup.NONE ? Math.max(12, height / 2 - 142) : Math.max(10, height / 2 - 148);

        graphics.fill(0, 0, width, height, 0xAA030611);
        graphics.fill(left, top, left + panelWidth, top + panelHeight, 0xEE0B1020);
        graphics.renderOutline(left, top, panelWidth, panelHeight, 0xCC8C5CFF);
        graphics.renderOutline(left + 4, top + 4, panelWidth - 8, panelHeight - 8, 0x773AD7FF);

        graphics.drawCenteredString(font, titleForPopup(), width / 2, top + 14, 0xFFFFFFFF);
        graphics.drawCenteredString(font, subtitleForPopup(), width / 2, top + 31, 0xFFBFD7FF);

        if (popup == Popup.RESOURCES) {
            graphics.drawCenteredString(font, Component.literal("Screen Overlays entra automaticamente por ultimo."), width / 2, top + 248, 0xFFFFE0A3);
        }

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    private void confirm(boolean resourcePack, boolean shaderPack, boolean completePack) {
        MagicWorldNetwork.confirmPremiumPortalOptions(
                resourcePack,
                shaderPack,
                completePack,
                selectedResourcePacks,
                selectedShaderPack == null ? "" : selectedShaderPack
        );
        onClose();
    }

    private void openPopup(Popup popup) {
        this.popup = popup;
        rebuild();
    }

    private void rebuild() {
        clearWidgets();
        init();
    }

    private void toggleResourcePack(String id) {
        if (selectedResourcePacks.contains(id)) {
            selectedResourcePacks.remove(id);
        } else {
            selectedResourcePacks.add(id);
        }
    }

    private String shaderLabel() {
        if (selectedShaderPack == null || selectedShaderPack.isBlank()) {
            return "Nenhum";
        }
        return selectedShaderPack.length() > 24 ? selectedShaderPack.substring(0, 21) + "..." : selectedShaderPack;
    }

    private Component titleForPopup() {
        return switch (popup) {
            case RESOURCES -> Component.literal("SELECIONE RESOURCEPACKS");
            case SHADERS -> Component.literal("SELECIONE SHADERPACK");
            default -> Component.literal("MODO VISUAL MAGIC WORLD");
        };
    }

    private Component subtitleForPopup() {
        return switch (popup) {
            case RESOURCES -> Component.literal("Marque os packs; overlays de tela ficam por cima.");
            case SHADERS -> Component.literal("Escolha um shader da pasta shaderpacks.");
            default -> Component.literal("Escolha packs, shader ou remova os efeitos.");
        };
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
