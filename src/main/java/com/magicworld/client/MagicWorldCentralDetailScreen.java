package com.magicworld.client;

import com.magicworld.central.MagicWorldCentralSnapshot;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class MagicWorldCentralDetailScreen extends Screen {
    private final Screen parent;
    private final MagicWorldCentralSnapshot.Section section;

    public MagicWorldCentralDetailScreen(Screen parent, MagicWorldCentralSnapshot.Section section) {
        super(Component.literal(section.title()));
        this.parent = parent;
        this.section = section;
    }

    @Override
    protected void init() {
        int panelWidth = Math.min(460, width - 24);
        int left = width / 2 - panelWidth / 2;
        int panelHeight = Math.min(326, height - 18);
        int top = Math.max(8, height / 2 - panelHeight / 2);

        addRenderableWidget(new MagicWorldMenuButton(
                left + 24,
                top + panelHeight - 30,
                panelWidth - 48,
                20,
                Component.literal("Voltar"),
                () -> Minecraft.getInstance().setScreen(parent)
        )).setTooltip(Tooltip.create(Component.literal("Volta para a central MagicWorld.")));
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        int panelWidth = Math.min(460, width - 24);
        int panelHeight = Math.min(326, height - 18);
        int left = width / 2 - panelWidth / 2;
        int top = Math.max(8, height / 2 - panelHeight / 2);

        MagicWorldStaticBackground.draw(graphics, width, height);
        graphics.fill(0, 0, width, height, 0x77030610);
        MagicWorldMenuTheme.drawFrame(graphics, left, top, panelWidth, panelHeight);
        MagicWorldCentralUi.drawLogo(graphics, width / 2, top + 10, panelWidth - 150, 42);
        graphics.drawCenteredString(font, Component.literal(section.title().toUpperCase()), width / 2, top + 66, 0xFFFFFFFF);
        graphics.drawCenteredString(font, Component.literal(section.subtitle()), width / 2, top + 84, 0xFFFFE0A3);

        int boxTop = top + 106;
        graphics.fill(left + 18, boxTop, left + panelWidth - 18, top + panelHeight - 42, 0x661A2438);
        graphics.renderOutline(left + 18, boxTop, panelWidth - 36, panelHeight - 96, 0x557FCBFF);
        int y = boxTop + 14;
        for (String line : section.lines()) {
            graphics.drawString(font, Component.literal("- " + line), left + 32, y, 0xFFEAF6FF);
            y += 18;
        }
        graphics.drawString(font, Component.literal("Status: preview leve. Acoes reais ficam nos botoes principais."), left + 32, top + panelHeight - 58, 0xFFBFD7C8);

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
