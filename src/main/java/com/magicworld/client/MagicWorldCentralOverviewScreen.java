package com.magicworld.client;

import com.magicworld.central.MagicWorldCentralData;
import com.magicworld.central.MagicWorldCentralSnapshot;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class MagicWorldCentralOverviewScreen extends Screen {
    private final Screen parent;
    private final MagicWorldCentralSnapshot snapshot;

    public MagicWorldCentralOverviewScreen(Screen parent) {
        super(Component.literal("Central MagicWorld"));
        this.parent = parent;
        this.snapshot = MagicWorldCentralData.snapshot(Minecraft.getInstance());
    }

    @Override
    protected void init() {
        int panelWidth = Math.min(560, width - 24);
        int left = width / 2 - panelWidth / 2;
        int panelHeight = Math.min(400, height - 18);
        int top = Math.max(8, height / 2 - panelHeight / 2);
        int gap = 6;
        int buttonWidth = (panelWidth - 48 - gap) / 2;
        int buttonHeight = 20;
        int startY = top + 140;

        var sections = snapshot.sections();
        for (int i = 0; i < sections.size(); i++) {
            MagicWorldCentralSnapshot.Section section = sections.get(i);
            int column = i % 2;
            int row = i / 2;
            int x = left + 24 + column * (buttonWidth + gap);
            int y = startY + row * 26;
            addRenderableWidget(new MagicWorldMenuButton(
                    x,
                    y,
                    buttonWidth,
                    buttonHeight,
                    Component.literal(section.title()),
                    () -> Minecraft.getInstance().setScreen(new MagicWorldCentralDetailScreen(this, section))
            )).setTooltip(Tooltip.create(Component.literal(section.subtitle())));
        }

        int bottomWidth = (panelWidth - 54) / 2;
        addRenderableWidget(new MagicWorldMenuButton(
                left + 24,
                top + panelHeight - 30,
                bottomWidth,
                20,
                Component.literal("Menu completo"),
                () -> Minecraft.getInstance().setScreen(new PremiumMenuScreen())
        ));
        addRenderableWidget(new MagicWorldMenuButton(
                left + 30 + bottomWidth,
                top + panelHeight - 30,
                bottomWidth,
                20,
                Component.literal("Voltar"),
                () -> Minecraft.getInstance().setScreen(parent)
        ));
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        int panelWidth = Math.min(560, width - 24);
        int panelHeight = Math.min(400, height - 18);
        int left = width / 2 - panelWidth / 2;
        int top = Math.max(8, height / 2 - panelHeight / 2);

        MagicWorldStaticBackground.draw(graphics, width, height);
        graphics.fill(0, 0, width, height, 0x77030610);
        MagicWorldMenuTheme.drawFrame(graphics, left, top, panelWidth, panelHeight);
        MagicWorldCentralUi.drawLogo(graphics, width / 2, top + 10, panelWidth - 160, 46);
        graphics.drawCenteredString(font, Component.literal("CENTRAL MAGICWORLD"), width / 2, top + 70, 0xFFFFFFFF);
        graphics.drawCenteredString(font, Component.literal("Varinha, tempo, portais, fazendas e poderes"), width / 2, top + 88, 0xFFFFE0A3);

        int boxTop = top + 108;
        graphics.fill(left + 18, boxTop, left + panelWidth - 18, boxTop + 18, 0x661A2438);
        graphics.renderOutline(left + 18, boxTop, panelWidth - 36, 18, 0x557FCBFF);
        graphics.drawCenteredString(font, Component.literal("Escolha um caminho magico"), width / 2, boxTop + 5, 0xFFBFD7FF);

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
