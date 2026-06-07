package com.magicworld.client;

import com.magicworld.network.MagicWorldNetwork;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;

public class MagicWorldPremiumLocationsScreen extends Screen {
    private static final int CARD_HEIGHT = 48;
    private static final int GAP = 8;
    private static final List<LocationCard> CARDS = List.of(
            new LocationCard("Minha casa", "Spawn seguro", "Casa inicial, ao lado da cama.", "location_teleport_home", Items.RED_BED, true),
            new LocationCard("Santuario", "Estrutura", "Santuario Violeta abaixo do castelo.", "location_teleport_sanctuary", Items.AMETHYST_BLOCK, true),
            new LocationCard("Praca de portais", "Portais", "Centro dos portais funcionais.", "location_teleport_portal_plaza", Items.OBSIDIAN, true),
            new LocationCard("Castelo", "Estrutura", "Castelo Magic World principal.", "location_teleport_castle", Items.BELL, true),
            new LocationCard("Registrar aqui", "Marcador", "Salva sua posicao atual como marcador manual.", "location_register_current", Items.MAP, false),
            new LocationCard("Marcador manual", "Teleporte", "Volta ao ultimo marcador manual salvo.", "location_teleport_manual", Items.FILLED_MAP, true),
            new LocationCard("Ultimo externo", "Protecao", "Ponto onde MCA/MineColonies moveu voce.", "location_teleport_external", Items.RECOVERY_COMPASS, true),
            new LocationCard("Atualizar JourneyMap", "Mapa", "Recria waypoints oficiais Magic World.", "location_update_waypoints", Items.WRITABLE_BOOK, false),
            new LocationCard("Registrar colonia", "MineColonies", "Salva sua posicao como colonia atual.", "minecolonies_register_current", Items.OAK_SIGN, false),
            new LocationCard("Ultima colonia", "MineColonies", "Volta para a ultima colonia registrada.", "location_teleport_last_colony", Items.COMPASS, true),
            new LocationCard("Town Hall", "MineColonies", "Volta ao ultimo Town Hall usado.", "location_teleport_town_hall", Items.OAK_DOOR, true),
            new LocationCard("Ultima construcao", "MineColonies", "Volta ao ultimo bloco MineColonies usado.", "location_teleport_last_building", Items.BRICKS, true),
            new LocationCard("Por que fui movido?", "Ajuda", "Explica teleporte externo e retorno seguro.", "location_explain_external_teleport", Items.BOOK, false)
    );

    private final Screen parent;
    private EditBox coordinateBox;
    private int panelX;
    private int panelY;
    private int panelWidth;
    private int panelHeight;
    private int contentTop;
    private int contentBottom;
    private int scroll;
    private String statusMessage = "JourneyMap: atualize os pontos para manter mapa/lista sem beacons 3D.";

    public MagicWorldPremiumLocationsScreen(Screen parent) {
        super(Component.literal("Locais Magic World"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        updateLayout();
        int coordinateY = panelY + panelHeight - 60;
        int coordinateButtonWidth = Math.min(210, Math.max(160, panelWidth / 3));
        int coordinateBoxWidth = Math.max(120, panelWidth - 54 - coordinateButtonWidth);
        coordinateBox = new EditBox(font, panelX + 18, coordinateY, coordinateBoxWidth, 20, Component.literal("X Y Z"));
        coordinateBox.setHint(Component.literal("Coordenada manual: X Y Z"));
        coordinateBox.setMaxLength(64);
        addRenderableWidget(coordinateBox);

        addRenderableWidget(new MagicWorldMenuButton(panelX + panelWidth - 18 - coordinateButtonWidth, coordinateY, coordinateButtonWidth, 20,
                Component.literal("Ir para coordenada"), this::teleportManualCoordinate));
        addRenderableWidget(new MagicWorldMenuButton(panelX + 18, panelY + panelHeight - 30, 110, 20,
                Component.literal("Voltar"), () -> Minecraft.getInstance().setScreen(parent)));
        addRenderableWidget(new MagicWorldMenuButton(panelX + panelWidth - 128, panelY + panelHeight - 30, 110, 20,
                Component.literal("Fechar"), () -> Minecraft.getInstance().setScreen(null)));
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        updateLayout();
        MagicWorldStaticBackground.draw(graphics, width, height);
        graphics.fill(0, 0, width, height, 0x8A030611);
        MagicWorldMenuTheme.drawFrame(graphics, panelX, panelY, panelWidth, panelHeight);
        drawHeader(graphics);
        drawCards(graphics, mouseX, mouseY);
        drawFooterHelp(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        LocationCard clicked = clickedCard(mouseX, mouseY);
        if (clicked != null) {
            MagicWorldNetwork.sendPanelAction(clicked.action());
            if (clicked.closeAfterClick()) {
                Minecraft.getInstance().setScreen(null);
            } else {
                statusMessage = statusFor(clicked.action());
            }
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (mouseY < contentTop || mouseY > contentBottom) {
            return super.mouseScrolled(mouseX, mouseY, delta);
        }
        scroll = clamp(scroll - (int) Math.signum(delta), 0, maxScroll());
        return true;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private void updateLayout() {
        panelWidth = Math.min(860, width - 24);
        panelHeight = Math.min(560, height - 20);
        panelX = width / 2 - panelWidth / 2;
        panelY = Math.max(10, height / 2 - panelHeight / 2);
        contentTop = panelY + 86;
        contentBottom = panelY + panelHeight - 116;
        scroll = clamp(scroll, 0, maxScroll());
    }

    private void drawHeader(GuiGraphics graphics) {
        int logoWidth = Math.min(150, Math.max(108, panelWidth / 5));
        int logoHeight = logoWidth * MagicWorldStaticBackground.LOGO_HEIGHT / MagicWorldStaticBackground.LOGO_WIDTH;
        graphics.blit(
                MagicWorldStaticBackground.FULL_LOGO,
                panelX + panelWidth / 2 - logoWidth / 2,
                panelY + 9,
                logoWidth,
                logoHeight,
                0,
                0,
                MagicWorldStaticBackground.LOGO_WIDTH,
                MagicWorldStaticBackground.LOGO_HEIGHT,
                MagicWorldStaticBackground.LOGO_WIDTH,
                MagicWorldStaticBackground.LOGO_HEIGHT
        );
        graphics.drawCenteredString(font, Component.literal("Locais Magic World"), panelX + panelWidth / 2, panelY + 48, MagicWorldMenuTheme.WHITE);
        graphics.drawCenteredString(font, Component.literal("Casa, santuario, portais, marcadores e MineColonies em uma tela propria."), panelX + panelWidth / 2, panelY + 62, 0xFFFFE0A3);
    }

    private void drawCards(GuiGraphics graphics, int mouseX, int mouseY) {
        int columns = panelWidth < 640 ? 1 : 2;
        int cardWidth = (panelWidth - 36 - GAP * (columns - 1)) / columns;
        int visibleRows = Math.max(1, (contentBottom - contentTop) / (CARD_HEIGHT + GAP));
        int first = scroll * columns;

        graphics.enableScissor(panelX + 12, contentTop - 4, panelX + panelWidth - 12, contentBottom + 4);
        for (int index = first; index < CARDS.size() && index < first + visibleRows * columns; index++) {
            int local = index - first;
            int x = panelX + 18 + (local % columns) * (cardWidth + GAP);
            int y = contentTop + (local / columns) * (CARD_HEIGHT + GAP);
            drawCard(graphics, x, y, cardWidth, CARDS.get(index), mouseX, mouseY);
        }
        graphics.disableScissor();

        if (maxScroll() > 0) {
            graphics.drawString(font, Component.literal("Rolagem " + (scroll + 1) + "/" + (maxScroll() + 1)), panelX + panelWidth - 104, contentBottom + 8, 0xFF9EB6E8, false);
        }
    }

    private void drawCard(GuiGraphics graphics, int x, int y, int cardWidth, LocationCard card, int mouseX, int mouseY) {
        boolean hovered = isInside(mouseX, mouseY, x, y, cardWidth, CARD_HEIGHT);
        int fill = hovered ? 0xE00B1740 : 0xCC050814;
        graphics.fill(x + 3, y + 3, x + cardWidth + 3, y + CARD_HEIGHT + 3, 0xAA000000);
        graphics.fill(x, y, x + cardWidth, y + CARD_HEIGHT, fill);
        graphics.renderOutline(x, y, cardWidth, CARD_HEIGHT, hovered ? MagicWorldMenuTheme.BLUE : MagicWorldMenuTheme.GOLD);
        graphics.fill(x + 4, y + 4, x + cardWidth - 4, y + 5, hovered ? MagicWorldMenuTheme.BLUE : 0xAA0C315C);
        graphics.renderItem(new ItemStack(card.icon()), x + 10, y + 14);
        graphics.drawString(font, Component.literal(card.title()), x + 34, y + 8, hovered ? MagicWorldMenuTheme.BLUE : MagicWorldMenuTheme.WHITE, false);
        graphics.drawString(font, Component.literal(card.coordinateText()), x + 34, y + 22, 0xFFFFE0A3, false);
        graphics.drawString(font, Component.literal(card.description()), x + 34, y + 34, 0xFF9EB6E8, false);
    }

    private void drawFooterHelp(GuiGraphics graphics) {
        int y = panelY + panelHeight - 104;
        graphics.drawString(font, Component.literal(statusMessage), panelX + 18, y, 0xFFFFE0A3, false);
        graphics.drawString(font, Component.literal("JourneyMap: pontos ficam no mapa/lista; beacons 3D ficam desligados."),
                panelX + 18, y + 12, 0xFF9EB6E8, false);
        graphics.drawString(font, Component.literal("MineColonies: Town Hall, Supply Camp e construcoes salvam retorno seguro."),
                panelX + 18, y + 24, 0xFF9EB6E8, false);
    }

    private static String statusFor(String action) {
        return switch (action) {
            case "location_update_waypoints" -> "Waypoints atualizados: sem linhas/beacons 3D no mundo.";
            case "location_register_current" -> "Marcador manual salvo. Use Marcador manual para voltar.";
            case "minecolonies_register_current" -> "Colonia atual registrada para retorno seguro.";
            case "location_explain_external_teleport" -> "Ajuda enviada no chat: teleporte externo e retorno para casa.";
            default -> "Acao enviada ao Magic World.";
        };
    }

    private LocationCard clickedCard(double mouseX, double mouseY) {
        int columns = panelWidth < 640 ? 1 : 2;
        int cardWidth = (panelWidth - 36 - GAP * (columns - 1)) / columns;
        int visibleRows = Math.max(1, (contentBottom - contentTop) / (CARD_HEIGHT + GAP));
        int first = scroll * columns;
        for (int index = first; index < CARDS.size() && index < first + visibleRows * columns; index++) {
            int local = index - first;
            int x = panelX + 18 + (local % columns) * (cardWidth + GAP);
            int y = contentTop + (local / columns) * (CARD_HEIGHT + GAP);
            if (isInside(mouseX, mouseY, x, y, cardWidth, CARD_HEIGHT)) {
                return CARDS.get(index);
            }
        }
        return null;
    }

    private void teleportManualCoordinate() {
        String normalized = coordinateBox.getValue().trim().replace(',', ' ').replace(';', ' ');
        String[] parts = normalized.split("\\s+");
        if (parts.length < 3) {
            coordinateBox.setValue("Digite: X Y Z");
            return;
        }
        try {
            int x = Integer.parseInt(parts[0]);
            int y = Integer.parseInt(parts[1]);
            int z = Integer.parseInt(parts[2]);
            MagicWorldNetwork.sendPanelAction("location_teleport_manual_coords:" + x + ":" + y + ":" + z);
            Minecraft.getInstance().setScreen(null);
        } catch (NumberFormatException ignored) {
            coordinateBox.setValue("Coordenada invalida");
        }
    }

    private int maxScroll() {
        int columns = panelWidth < 640 ? 1 : 2;
        int rows = Math.max(1, (CARDS.size() + columns - 1) / columns);
        int visibleRows = Math.max(1, (contentBottom - contentTop) / (CARD_HEIGHT + GAP));
        return Math.max(0, rows - visibleRows);
    }

    private static boolean isInside(double mouseX, double mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private record LocationCard(String title, String coordinateText, String description, String action, Item icon, boolean closeAfterClick) {
    }
}
