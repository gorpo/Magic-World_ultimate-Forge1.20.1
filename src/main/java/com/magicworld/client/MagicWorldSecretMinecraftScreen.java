package com.magicworld.client;

import com.magicworld.network.MagicWorldNetwork;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.packs.PackSelectionScreen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MagicWorldSecretMinecraftScreen extends Screen {
    private static final int TEXT = 0xFFE8F2FF;
    private static final int MUTED = 0xFF9EB6E8;
    private static final int GOLD = 0xFFD9A441;
    private static final int BLUE = 0xFF22D3FF;
    private static final int CARD = 0xCC050814;
    private static final int CARD_HOVER = 0xE00B1740;
    private static final int SLOT = 28;
    private static final String[] ITEM_CATEGORIES = {
            "Todos", "Blocos", "Comida", "Combate", "Armad.", "Tools", "Redstone", "Ovos"
    };

    private final Screen parent;
    private Tab activeTab = Tab.PLAYER;
    private int itemCategory;
    private int scroll;
    private int panelX;
    private int panelY;
    private int panelWidth;
    private int panelHeight;
    private int contentTop;
    private int contentBottom;

    public MagicWorldSecretMinecraftScreen(Screen parent) {
        super(Component.literal("Menu Secreto do Minecraft"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        updateLayout();
        addRenderableWidget(Button.builder(Component.literal("Fechar"), button -> minecraft.setScreen(null))
                .bounds(panelX + 14, panelY + panelHeight - 28, 70, 20)
                .build());
        addRenderableWidget(Button.builder(Component.literal("Voltar"), button -> minecraft.setScreen(parent))
                .bounds(panelX + panelWidth - 84, panelY + panelHeight - 28, 70, 20)
                .build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        updateLayout();
        MagicWorldStaticBackground.draw(graphics, width, height);
        graphics.fill(0, 0, width, height, 0x88030611);
        MagicWorldMenuTheme.drawFrame(graphics, panelX, panelY, panelWidth, panelHeight);
        drawHeader(graphics);
        drawTabs(graphics, mouseX, mouseY);

        if (activeTab == Tab.ITEMS) {
            drawItemMenu(graphics, mouseX, mouseY);
        } else {
            drawActionMenu(graphics, mouseX, mouseY);
        }

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        Tab clickedTab = getClickedTab(mouseX, mouseY);
        if (clickedTab != null) {
            activeTab = clickedTab;
            scroll = 0;
            return true;
        }

        if (activeTab == Tab.ITEMS) {
            int clickedCategory = getClickedItemCategory(mouseX, mouseY);
            if (clickedCategory >= 0) {
                itemCategory = clickedCategory;
                scroll = 0;
                return true;
            }

            Item item = getClickedItem(mouseX, mouseY);
            if (item != null) {
                ResourceLocation id = BuiltInRegistries.ITEM.getKey(item);
                sendServerAction("secret_give:" + id);
                return true;
            }
        } else {
            SecretAction action = getClickedAction(mouseX, mouseY);
            if (action != null) {
                runAction(action);
                return true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (mouseY < contentTop || mouseY > contentBottom) {
            return super.mouseScrolled(mouseX, mouseY, delta);
        }

        int maxScroll = activeTab == Tab.ITEMS ? maxItemScroll() : maxActionScroll();
        scroll = Math.max(0, Math.min(maxScroll, scroll - (int) Math.signum(delta)));
        return true;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private void updateLayout() {
        panelWidth = Math.min(780, width - 20);
        panelHeight = Math.min(444, height - 16);
        panelX = width / 2 - panelWidth / 2;
        panelY = Math.max(8, height / 2 - panelHeight / 2);
        contentTop = panelY + 94;
        contentBottom = panelY + panelHeight - 38;
    }

    private void drawHeader(GuiGraphics graphics) {
        graphics.drawCenteredString(font, title, width / 2 + 1, panelY + 18, 0xAA000000);
        graphics.drawCenteredString(font, title, width / 2, panelY + 17, TEXT);
        graphics.drawCenteredString(font, Component.literal("Poderes, mundo, teleportes, itens e resources em abas funcionais."),
                width / 2, panelY + 33, GOLD);
    }

    private void drawTabs(GuiGraphics graphics, int mouseX, int mouseY) {
        Tab[] tabs = Tab.values();
        int gap = 6;
        int tabWidth = Math.max(76, Math.min(112, (panelWidth - 28 - gap * (tabs.length - 1)) / tabs.length));
        int totalWidth = tabs.length * tabWidth + (tabs.length - 1) * gap;
        int x = panelX + panelWidth / 2 - totalWidth / 2;
        int y = panelY + 56;

        for (Tab tab : tabs) {
            MagicWorldMenuTheme.drawButton(graphics, font, x, y, tabWidth, 22,
                    Component.literal(tab.label), mouseX, mouseY, tab == activeTab);
            x += tabWidth + gap;
        }
    }

    private void drawActionMenu(GuiGraphics graphics, int mouseX, int mouseY) {
        List<SecretAction> actions = actionsFor(activeTab);
        int columns = panelWidth < 560 ? 1 : 2;
        int gap = 10;
        int cardHeight = 48;
        int cardWidth = (panelWidth - 36 - gap * (columns - 1)) / columns;
        int visibleRows = Math.max(1, (contentBottom - contentTop) / (cardHeight + gap));
        int first = scroll * columns;

        drawScrollHint(graphics, actions.size(), columns, visibleRows);

        for (int index = first; index < actions.size() && index < first + visibleRows * columns; index++) {
            int local = index - first;
            int x = panelX + 18 + (local % columns) * (cardWidth + gap);
            int y = contentTop + (local / columns) * (cardHeight + gap);
            drawActionCard(graphics, x, y, cardWidth, cardHeight, actions.get(index), mouseX, mouseY);
        }
    }

    private void drawActionCard(GuiGraphics graphics, int x, int y, int cardWidth, int cardHeight, SecretAction action, int mouseX, int mouseY) {
        boolean hovered = isInside(mouseX, mouseY, x, y, cardWidth, cardHeight);
        graphics.fill(x + 3, y + 3, x + cardWidth + 3, y + cardHeight + 3, 0x99000000);
        graphics.fill(x, y, x + cardWidth, y + cardHeight, hovered ? CARD_HOVER : CARD);
        graphics.renderOutline(x, y, cardWidth, cardHeight, hovered ? BLUE : GOLD);
        graphics.renderItem(new ItemStack(action.icon), x + 10, y + 15);
        graphics.drawString(font, Component.literal(action.title), x + 34, y + 10, hovered ? BLUE : TEXT, false);
        graphics.drawString(font, Component.literal(action.description), x + 34, y + 25, MUTED, false);
    }

    private void drawItemMenu(GuiGraphics graphics, int mouseX, int mouseY) {
        drawItemCategories(graphics, mouseX, mouseY);
        List<Item> items = filteredItems();
        int gridTop = contentTop + 32;
        int columns = Math.max(4, (panelWidth - 36) / SLOT);
        int rows = Math.max(1, (contentBottom - gridTop) / SLOT);
        int first = scroll * columns;

        drawScrollHint(graphics, items.size(), columns, rows);

        for (int index = first; index < items.size() && index < first + rows * columns; index++) {
            int local = index - first;
            int x = panelX + 18 + (local % columns) * SLOT;
            int y = gridTop + (local / columns) * SLOT;
            Item item = items.get(index);
            boolean hovered = isInside(mouseX, mouseY, x, y, 24, 24);
            graphics.fill(x + 2, y + 2, x + 26, y + 26, 0x99000000);
            graphics.fill(x, y, x + 24, y + 24, hovered ? CARD_HOVER : CARD);
            graphics.renderOutline(x, y, 24, 24, hovered ? BLUE : 0x885C7CBA);
            graphics.renderItem(new ItemStack(item), x + 4, y + 4);
            if (hovered) {
                graphics.renderTooltip(font, new ItemStack(item), mouseX, mouseY);
            }
        }
    }

    private void drawItemCategories(GuiGraphics graphics, int mouseX, int mouseY) {
        int gap = 4;
        int x = panelX + 24;
        int y = contentTop;
        int available = panelWidth - 48;
        int categoryWidth = Math.max(48, (available - gap * (ITEM_CATEGORIES.length - 1)) / ITEM_CATEGORIES.length);

        for (int i = 0; i < ITEM_CATEGORIES.length; i++) {
            drawCenteredSmallButton(graphics, x, y, categoryWidth, 20, ITEM_CATEGORIES[i], mouseX, mouseY, i == itemCategory);
            x += categoryWidth + gap;
        }
    }

    private void drawCenteredSmallButton(GuiGraphics graphics, int x, int y, int buttonWidth, int buttonHeight, String label, int mouseX, int mouseY, boolean selected) {
        boolean hovered = isInside(mouseX, mouseY, x, y, buttonWidth, buttonHeight);
        int fill = selected || hovered ? CARD_HOVER : CARD;
        graphics.fill(x + 2, y + 2, x + buttonWidth + 2, y + buttonHeight + 2, 0x99000000);
        graphics.fill(x, y, x + buttonWidth, y + buttonHeight, fill);
        graphics.renderOutline(x, y, buttonWidth, buttonHeight, selected || hovered ? BLUE : GOLD);
        graphics.fill(x + 4, y + 3, x + buttonWidth - 4, y + 4, selected || hovered ? BLUE : 0xAA0C315C);
        graphics.drawCenteredString(font, Component.literal(label), x + buttonWidth / 2, y + buttonHeight / 2 - 4,
                selected || hovered ? BLUE : TEXT);
    }

    private void drawScrollHint(GuiGraphics graphics, int totalEntries, int columns, int visibleRows) {
        int totalRows = Math.max(1, (totalEntries + columns - 1) / columns);
        if (totalRows <= visibleRows) {
            return;
        }
        String text = "Rolagem " + (scroll + 1) + "/" + (totalRows - visibleRows + 1);
        graphics.drawString(font, Component.literal(text), panelX + panelWidth - 104, contentBottom - 11, MUTED, false);
    }

    private Tab getClickedTab(double mouseX, double mouseY) {
        Tab[] tabs = Tab.values();
        int gap = 6;
        int tabWidth = Math.max(76, Math.min(112, (panelWidth - 28 - gap * (tabs.length - 1)) / tabs.length));
        int totalWidth = tabs.length * tabWidth + (tabs.length - 1) * gap;
        int x = panelX + panelWidth / 2 - totalWidth / 2;
        int y = panelY + 56;

        for (Tab tab : tabs) {
            if (isInside(mouseX, mouseY, x, y, tabWidth, 22)) {
                return tab;
            }
            x += tabWidth + gap;
        }
        return null;
    }

    private int getClickedItemCategory(double mouseX, double mouseY) {
        int gap = 4;
        int x = panelX + 24;
        int y = contentTop;
        int available = panelWidth - 48;
        int categoryWidth = Math.max(48, (available - gap * (ITEM_CATEGORIES.length - 1)) / ITEM_CATEGORIES.length);

        for (int i = 0; i < ITEM_CATEGORIES.length; i++) {
            if (isInside(mouseX, mouseY, x, y, categoryWidth, 20)) {
                return i;
            }
            x += categoryWidth + gap;
        }
        return -1;
    }

    private Item getClickedItem(double mouseX, double mouseY) {
        List<Item> items = filteredItems();
        int gridTop = contentTop + 32;
        int columns = Math.max(4, (panelWidth - 36) / SLOT);
        int rows = Math.max(1, (contentBottom - gridTop) / SLOT);
        int first = scroll * columns;

        for (int index = first; index < items.size() && index < first + rows * columns; index++) {
            int local = index - first;
            int x = panelX + 18 + (local % columns) * SLOT;
            int y = gridTop + (local / columns) * SLOT;
            if (isInside(mouseX, mouseY, x, y, 24, 24)) {
                return items.get(index);
            }
        }
        return null;
    }

    private SecretAction getClickedAction(double mouseX, double mouseY) {
        List<SecretAction> actions = actionsFor(activeTab);
        int columns = panelWidth < 560 ? 1 : 2;
        int gap = 10;
        int cardHeight = 48;
        int cardWidth = (panelWidth - 36 - gap * (columns - 1)) / columns;
        int visibleRows = Math.max(1, (contentBottom - contentTop) / (cardHeight + gap));
        int first = scroll * columns;

        for (int index = first; index < actions.size() && index < first + visibleRows * columns; index++) {
            int local = index - first;
            int x = panelX + 18 + (local % columns) * (cardWidth + gap);
            int y = contentTop + (local / columns) * (cardHeight + gap);
            if (isInside(mouseX, mouseY, x, y, cardWidth, cardHeight)) {
                return actions.get(index);
            }
        }
        return null;
    }

    private int maxActionScroll() {
        int columns = panelWidth < 560 ? 1 : 2;
        int rows = Math.max(1, (actionsFor(activeTab).size() + columns - 1) / columns);
        int visibleRows = Math.max(1, (contentBottom - contentTop) / 58);
        return Math.max(0, rows - visibleRows);
    }

    private int maxItemScroll() {
        int gridTop = contentTop + 32;
        int columns = Math.max(4, (panelWidth - 36) / SLOT);
        int rows = Math.max(1, (filteredItems().size() + columns - 1) / columns);
        int visibleRows = Math.max(1, (contentBottom - gridTop) / SLOT);
        return Math.max(0, rows - visibleRows);
    }

    private void runAction(SecretAction action) {
        if (action.clientAction) {
            runClientAction(action.action);
        } else {
            sendServerAction(action.action);
        }
    }

    private void runClientAction(String action) {
        Minecraft client = Minecraft.getInstance();
        switch (action) {
            case "camera_first" -> {
                client.options.setCameraType(CameraType.FIRST_PERSON);
                client.options.save();
            }
            case "camera_back" -> {
                client.options.setCameraType(CameraType.THIRD_PERSON_BACK);
                client.options.save();
            }
            case "camera_front" -> {
                client.options.setCameraType(CameraType.THIRD_PERSON_FRONT);
                client.options.save();
            }
            case "resource_screen" -> client.setScreen(new PackSelectionScreen(
                    client.getResourcePackRepository(),
                    repository -> {
                        client.options.updateResourcePacks(repository);
                        client.setScreen(this);
                    },
                    client.getResourcePackDirectory(),
                    Component.translatable("resourcePack.title")
            ));
            case "resource_reload" -> client.reloadResourcePacks();
            case "resource_magicworld" -> {
                MagicWorldMenuTheme.toast("Use a tela de pacotes para ativar os packs MagicWorld 1.20.1.");
                client.reloadResourcePacks();
            }
            case "open_wand_menu" -> client.setScreen(new PremiumMenuScreen());
            default -> {
            }
        }
    }

    private void sendServerAction(String action) {
        MagicWorldNetwork.sendPanelAction(action);
    }

    private List<SecretAction> actionsFor(Tab tab) {
        List<SecretAction> actions = new ArrayList<>();
        switch (tab) {
            case PLAYER -> {
                actions.add(action("Imortalidade", "Resistencia, regen, fogo, agua e queda.", Items.TOTEM_OF_UNDYING, "secret_god"));
                actions.add(action("Protecao total", "Lava, agua, queda e visao noturna.", Items.ENCHANTED_GOLDEN_APPLE, "secret_protection"));
                actions.add(action("Velocidade", "Corrida, pulo, pressa e mineracao.", Items.SUGAR, "secret_speed"));
                actions.add(action("Limpar efeitos", "Remove todos os efeitos ativos.", Items.MILK_BUCKET, "secret_clear_effects"));
                actions.add(action("Criativo", "Troca para gamemode creative.", Items.COMMAND_BLOCK, "secret_gamemode_creative"));
                actions.add(action("Sobrevivencia", "Volta para survival.", Items.GRASS_BLOCK, "secret_gamemode_survival"));
                actions.add(action("Camera 1a pessoa", "Volta para a camera normal.", Items.SPYGLASS, "camera_first", true));
                actions.add(action("Camera 3a costas", "Terceira pessoa olhando para frente.", Items.ENDER_EYE, "camera_back", true));
                actions.add(action("Camera 3a frente", "Terceira pessoa olhando o rosto.", Items.CARVED_PUMPKIN, "camera_front", true));
            }
            case WORLD -> {
                actions.add(action("Dia", "Define tempo para dia.", Items.SUNFLOWER, "time_day"));
                actions.add(action("Noite", "Define tempo para noite.", Items.CLOCK, "time_night"));
                actions.add(action("Sol", "Remove chuva e trovoes.", Items.YELLOW_DYE, "weather_clear"));
                actions.add(action("Chuva", "Ativa chuva.", Items.WATER_BUCKET, "weather_rain"));
                actions.add(action("Keep inventory ON", "Nao perde itens ao morrer.", Items.CHEST, "secret_keep_inventory_on"));
                actions.add(action("Keep inventory OFF", "Volta regra vanilla.", Items.BARRIER, "secret_keep_inventory_off"));
                actions.add(action("Random tick normal", "Volta crescimento vanilla.", Items.REPEATER, "secret_tick_normal"));
                actions.add(action("Random tick rapido", "Acelera plantas e updates.", Items.COMPARATOR, "secret_tick_fast"));
                actions.add(action("Random tick turbo", "Acelera muito o ambiente.", Items.REDSTONE_BLOCK, "secret_tick_turbo"));
                actions.add(action("Random tick lento", "Reduz updates naturais.", Items.SOUL_SAND, "secret_tick_slow"));
            }
            case TELEPORT -> {
                actions.add(action("Minha casa", "Teleporta para a casa inicial.", Items.RED_BED, "teleport_home"));
                actions.add(action("Spawn aqui", "Define seu respawn no local atual.", Items.RESPAWN_ANCHOR, "secret_spawn_here"));
                actions.add(action("Spawn na casa", "Define respawn na casa inicial.", Items.WHITE_BED, "secret_spawn_home"));
                actions.add(action("Subir +64", "Sobe rapido se ficar preso.", Items.ELYTRA, "secret_tp_up"));
                actions.add(action("Kit Nether", "Entrega itens para portal Nether.", Items.OBSIDIAN, "secret_kit_nether"));
                actions.add(action("Kit Fim", "Entrega olhos e blocos do End.", Items.END_PORTAL_FRAME, "secret_kit_end"));
            }
            case RESOURCES -> {
                actions.add(action("Tela de pacotes", "Abre o organizador vanilla de resources.", Items.BOOKSHELF, "resource_screen", true));
                actions.add(action("Aplicar MagicWorld", "Ativa o visual MagicWorld dos portais.", Items.AMETHYST_SHARD, "resource_magicworld", true));
                actions.add(action("Recarregar", "Recarrega resource packs ativos.", Items.NETHER_STAR, "resource_reload", true));
                actions.add(action("Menu varinha", "Abre todos os menus completos.", Items.BLAZE_ROD, "open_wand_menu", true));
            }
            case ITEMS -> {
            }
        }
        return actions;
    }

    private List<Item> filteredItems() {
        List<Item> items = new ArrayList<>();
        for (Item item : BuiltInRegistries.ITEM) {
            if (item != Items.AIR && matchesCategory(item)) {
                items.add(item);
            }
        }
        return items;
    }

    private boolean matchesCategory(Item item) {
        String id = BuiltInRegistries.ITEM.getKey(item).toString().toLowerCase(Locale.ROOT);
        ItemStack stack = new ItemStack(item);
        return switch (itemCategory) {
            case 1 -> item instanceof BlockItem;
            case 2 -> stack.isEdible();
            case 3 -> id.endsWith("_sword")
                    || id.endsWith("_bow")
                    || id.contains("crossbow")
                    || id.contains("trident")
                    || id.contains("arrow")
                    || id.contains("totem");
            case 4 -> id.endsWith("_helmet")
                    || id.endsWith("_chestplate")
                    || id.endsWith("_leggings")
                    || id.endsWith("_boots")
                    || id.contains("elytra")
                    || id.contains("shield");
            case 5 -> id.endsWith("_pickaxe")
                    || id.endsWith("_axe")
                    || id.endsWith("_shovel")
                    || id.endsWith("_hoe")
                    || id.contains("shears")
                    || id.contains("fishing_rod")
                    || id.contains("flint_and_steel")
                    || id.contains("brush");
            case 6 -> id.contains("redstone")
                    || id.contains("repeater")
                    || id.contains("comparator")
                    || id.contains("piston")
                    || id.contains("observer")
                    || id.contains("hopper")
                    || id.contains("dispenser")
                    || id.contains("dropper");
            case 7 -> id.endsWith("_spawn_egg");
            default -> true;
        };
    }

    private SecretAction action(String title, String description, Item icon, String action) {
        return action(title, description, icon, action, false);
    }

    private SecretAction action(String title, String description, Item icon, String action, boolean clientAction) {
        return new SecretAction(title, description, icon, action, clientAction);
    }

    private static boolean isInside(double mouseX, double mouseY, int x, int y, int w, int h) {
        return mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h;
    }

    private enum Tab {
        PLAYER("Jogador"),
        WORLD("Mundo"),
        TELEPORT("Teleporte"),
        ITEMS("Itens"),
        RESOURCES("Resources");

        private final String label;

        Tab(String label) {
            this.label = label;
        }
    }

    private record SecretAction(String title, String description, Item icon, String action, boolean clientAction) {
    }
}
