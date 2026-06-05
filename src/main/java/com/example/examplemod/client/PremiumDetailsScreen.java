package com.example.examplemod.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class PremiumDetailsScreen extends Screen {

    // Palette sampled from the paired MagicWorld resource pack GUI textures.
    private static final int NEON =
            0xFF22D3FF;

    private static final int WHITE =
            0xFFE2DBFF;

    private static final int PANEL =
            0xDD050814;

    private static final int STONE =
            0xFF120D29;

    private static final int STONE_LIGHT =
            0xFFA96DF2;

    private static final int STONE_DARK =
            0xFF050814;

    private static final int VANILLA_BUTTON =
            0xFF0C0D1F;

    private static final int VANILLA_BUTTON_LIGHT =
            0xFF22D3FF;

    private static final int VANILLA_BUTTON_DARK =
            0xFF140E2A;

    private static final int BUTTON_HOVER =
            0xFF170F2F;

    private static final int BORDER =
            0xFFB373FF;

    private static final int INSET =
            0xDD050814;

    private static final int SHADOW =
            0x88000000;

    private static final int TEXT_SHADOW =
            0xFF050814;

    private final PremiumEntry entry;
    private LivingEntity previewEntity;
    private String previewEntityId =
            "";

    public PremiumDetailsScreen(PremiumEntry entry) {
        super(Component.literal(entry.getDisplayName()));
        this.entry = entry;
    }

    @Override
    protected void init() {

    }

    @Override
    public void render(
            GuiGraphics guiGraphics,
            int mouseX,
            int mouseY,
            float partialTick
    ) {

        int panelWidth =
                Math.min(350, width - 24);

        int panelHeight =
                Math.min(245, height - 24);

        int x =
                (width - panelWidth) / 2;

        int y =
                (height - panelHeight) / 2;

        drawMinecraftPanel(
                guiGraphics,
                x,
                y,
                panelWidth,
                panelHeight
        );

        guiGraphics.drawCenteredString(
                font,
                entry.getDisplayName(),
                width / 2,
                y + 10,
                NEON
        );

        int iconX =
                x + 18;

        int iconY =
                y + 38;

        int iconSize =
                92;

        drawInsetBox(
                guiGraphics,
                iconX,
                iconY,
                iconSize,
                iconSize
        );

        drawIcon(
                guiGraphics,
                iconX,
                iconY,
                iconSize,
                mouseX,
                mouseY
        );

        int textX =
                x + 126;

        int textY =
                y + 38;

        guiGraphics.drawString(
                font,
                "Categoria:",
                textX + 1,
                textY + 1,
                TEXT_SHADOW
        );

        guiGraphics.drawString(
                font,
                "Categoria:",
                textX,
                textY,
                NEON
        );

        guiGraphics.drawString(
                font,
                entry.getCategory(),
                textX,
                textY + 12,
                WHITE
        );

        guiGraphics.drawString(
                font,
                "Transformacao:",
                textX + 1,
                textY + 35,
                TEXT_SHADOW
        );

        guiGraphics.drawString(
                font,
                "Transformacao:",
                textX,
                textY + 34,
                NEON
        );

        drawWrapped(
                guiGraphics,
                entry.getTransformation(),
                textX,
                textY + 46,
                panelWidth - 144,
                WHITE
        );

        guiGraphics.drawString(
                font,
                "Atributos:",
                x + 19,
                y + 139,
                TEXT_SHADOW
        );

        guiGraphics.drawString(
                font,
                "Atributos:",
                x + 18,
                y + 138,
                NEON
        );

        drawWrapped(
                guiGraphics,
                entry.getAttributes(),
                x + 18,
                y + 150,
                panelWidth - 36,
                WHITE
        );

        int normalX =
                x + 18;

        int normalWidth =
                96;

        int modifiedWidth =
                122;

        int modifiedX =
                x + panelWidth - 18 - modifiedWidth;

        int commandY =
                y + panelHeight - 52;

        drawButton(
                guiGraphics,
                normalX,
                commandY,
                normalWidth,
                18,
                "Spawnar normal",
                mouseX,
                mouseY
        );

        drawButton(
                guiGraphics,
                modifiedX,
                commandY,
                modifiedWidth,
                18,
                "Spawnar modificado",
                mouseX,
                mouseY
        );

        int bx =
                width / 2 - 42;

        int by =
                y + panelHeight - 28;

        drawButton(
                guiGraphics,
                bx,
                by,
                84,
                18,
                "Voltar",
                mouseX,
                mouseY
        );
    }

    @Override
    public boolean mouseClicked(
            double mouseX,
            double mouseY,
            int button
    ) {

        int panelWidth =
                Math.min(350, width - 24);

        int panelHeight =
                Math.min(245, height - 24);

        int x =
                (width - panelWidth) / 2;

        int y =
                (height - panelHeight) / 2;

        int normalX =
                x + 18;

        int normalWidth =
                96;

        int modifiedWidth =
                122;

        int modifiedX =
                x + panelWidth - 18 - modifiedWidth;

        int commandY =
                y + panelHeight - 52;

        if (isInside(mouseX, mouseY, normalX, commandY, normalWidth, 18)) {
            runCommand(entry.getNormalCommand());
            return true;
        }

        if (isInside(mouseX, mouseY, modifiedX, commandY, modifiedWidth, 18)) {
            runCommand(entry.getModifiedCommand());
            return true;
        }

        int bx =
                width / 2 - 42;

        int by =
                y + panelHeight - 28;

        if (mouseX >= bx
                && mouseX <= bx + 84
                && mouseY >= by
                && mouseY <= by + 18) {

            minecraft.setScreen(
                    new PremiumMenuScreen(
                            entry.getTab()
                    )
            );

            return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void drawButton(
            GuiGraphics guiGraphics,
            int x,
            int y,
            int width,
            int height,
            String text,
            int mouseX,
            int mouseY
    ) {

        boolean hovered =
                isInside(mouseX, mouseY, x, y, width, height);

        guiGraphics.fill(x + 2, y + 2, x + width + 2, y + height + 2, SHADOW);
        guiGraphics.fill(x, y, x + width, y + height, hovered ? BUTTON_HOVER : VANILLA_BUTTON);
        guiGraphics.fill(x, y, x + width, y + 1, VANILLA_BUTTON_LIGHT);
        guiGraphics.fill(x, y, x + 1, y + height, VANILLA_BUTTON_LIGHT);
        guiGraphics.fill(x, y + height - 1, x + width, y + height, VANILLA_BUTTON_DARK);
        guiGraphics.fill(x + width - 1, y, x + width, y + height, VANILLA_BUTTON_DARK);

        if (hovered) {
            drawThinGreenBox(
                    guiGraphics,
                    x + 2,
                    y + 2,
                    width - 4,
                    height - 4
            );
        }

        guiGraphics.drawCenteredString(
                font,
                text,
                x + width / 2 + 1,
                y + 6,
                TEXT_SHADOW
        );

        guiGraphics.drawCenteredString(
                font,
                text,
                x + width / 2,
                y + 5,
                NEON
        );
    }

    private boolean isInside(
            double mouseX,
            double mouseY,
            int x,
            int y,
            int width,
            int height
    ) {

        return mouseX >= x
                && mouseX <= x + width
                && mouseY >= y
                && mouseY <= y + height;
    }

    private void runCommand(
            String command
    ) {

        if (command == null
                || command.trim().isEmpty()
                || minecraft == null
                || minecraft.player == null
                || minecraft.getConnection() == null) {
            return;
        }

        minecraft.getConnection().sendCommand(
                command
        );
    }

    private void drawIcon(
            GuiGraphics guiGraphics,
            int x,
            int y,
            int size,
            int mouseX,
            int mouseY
    ) {

        String entityId =
                entityIdFromCommand(
                        entry.getNormalCommand()
                );

        if (!entityId.isEmpty()) {
            LivingEntity livingEntity =
                    getPreviewEntity(entityId);

            if (livingEntity != null) {
                guiGraphics.enableScissor(
                        x + 3,
                        y + 3,
                        x + size - 3,
                        y + size - 3
                );

                InventoryScreen.renderEntityInInventoryFollowsMouse(
                        guiGraphics,
                        x + size / 2 + entityXOffset(entityId),
                        y + entityBottom(entityId, size),
                        entityScale(entityId, 46),
                        (float) (x + size / 2 - mouseX),
                        (float) (y + size / 2 - mouseY),
                        livingEntity
                );

                guiGraphics.disableScissor();
                return;
            }
        }

        if (entry.usesMobTexture()) {
            drawMobFace(
                    guiGraphics,
                    x + 32,
                    y + 32
            );
            return;
        }

        guiGraphics.enableScissor(
                x + 3,
                y + 3,
                x + size - 3,
                y + size - 3
        );

        renderItemPreview(
                guiGraphics,
                new ItemStack(
                        entry.getIconItem()
                ),
                x + size / 2,
                y + size / 2,
                3.25F,
                mouseX,
                mouseY
        );

        guiGraphics.disableScissor();
    }

    private LivingEntity getPreviewEntity(
            String entityId
    ) {

        if (!entityId.equals(previewEntityId)) {
            previewEntityId = entityId;
            previewEntity =
                    createPreviewEntity(entityId);
        }

        return previewEntity;
    }

    private LivingEntity createPreviewEntity(
            String entityId
    ) {

        if (minecraft == null
                || minecraft.level == null
                || entityId.isEmpty()) {
            return null;
        }

        EntityType<?> entityType =
                EntityType.byString(entityId)
                        .orElse(null);

        if (entityType == null) {
            return null;
        }

        Entity entity =
                entityType.create(minecraft.level);

        if (entity instanceof LivingEntity livingEntity) {
            return livingEntity;
        }

        return null;
    }

    private String entityIdFromCommand(
            String command
    ) {

        if (command == null
                || !command.startsWith("summon ")) {
            return "";
        }

        int start =
                "summon ".length();

        int end =
                command.indexOf(" ", start);

        if (end <= start) {
            return "";
        }

        return command.substring(
                start,
                end
        );
    }

    private int entityScale(
            String entityId,
            int baseScale
    ) {

        if (entityId.contains("ender_dragon")) return Math.max(8, baseScale / 3);
        if (entityId.contains("giant")) return Math.max(8, baseScale / 3);
        if (entityId.contains("ghast")) return Math.max(11, baseScale / 2);
        if (entityId.contains("wither")) return Math.max(11, baseScale / 2);
        if (entityId.contains("ravager")) return Math.max(12, baseScale / 2);
        if (entityId.contains("warden")) return Math.max(12, baseScale / 2);
        if (entityId.contains("elder_guardian")) return Math.max(12, baseScale / 2);
        if (entityId.contains("iron_golem")) return Math.max(14, baseScale - 10);
        if (entityId.contains("camel")) return Math.max(14, baseScale - 10);
        if (entityId.contains("horse")) return Math.max(14, baseScale - 8);
        if (entityId.contains("hoglin")) return Math.max(14, baseScale - 8);

        return baseScale;
    }

    private int entityBottom(
            String entityId,
            int size
    ) {

        if (entityId.contains("bee")) return size - 12;
        if (entityId.contains("bat")) return size - 12;
        if (entityId.contains("vex")) return size - 12;
        if (entityId.contains("allay")) return size - 12;
        if (entityId.contains("ghast")) return size - 10;
        if (entityId.contains("squid")) return size - 10;
        if (entityId.contains("ender_dragon")) return size - 10;
        if (entityId.contains("giant")) return size - 7;

        return size - 7;
    }

    private int entityXOffset(
            String entityId
    ) {

        if (entityId.contains("ender_dragon")) return -1;
        if (entityId.contains("squid")) return -1;

        return 0;
    }

    private void renderItemPreview(
            GuiGraphics guiGraphics,
            ItemStack itemStack,
            int x,
            int y,
            float scale,
            int mouseX,
            int mouseY
    ) {

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(x, y, 120);
        guiGraphics.pose().scale(scale, scale, 1.0F);
        guiGraphics.renderItem(itemStack, -8, -8);
        guiGraphics.pose().popPose();
    }

    private void drawWrapped(
            GuiGraphics guiGraphics,
            String text,
            int x,
            int y,
            int width,
            int color
    ) {

        List<FormattedCharSequence> lines =
                font.split(
                        Component.literal(text),
                        width
                );

        int lineY =
                y;

        for (FormattedCharSequence line : lines) {
            guiGraphics.drawString(
                    font,
                    line,
                    x,
                    lineY,
                    color
            );

            lineY += 10;
        }
    }

    private void drawMobFace(
            GuiGraphics guiGraphics,
            int x,
            int y
    ) {

        String fileName =
                entry.getEnglishName()
                        .toLowerCase()
                        .replace(" ", "_");

        ResourceLocation texture =
                ResourceLocation.tryParse(
                        "examplemod:textures/gui/mobs/"
                                + fileName
                                + ".png"
                );

        RenderSystem.enableBlend();

        guiGraphics.blit(
                texture,
                x,
                y,
                0,
                0,
                28,
                28,
                28,
                28
        );
    }

    private void drawGreenBox(
            GuiGraphics guiGraphics,
            int x,
            int y,
            int width,
            int height
    ) {

        guiGraphics.fill(x, y, x + width, y + 2, STONE_LIGHT);
        guiGraphics.fill(x, y, x + 2, y + height, STONE_LIGHT);
        guiGraphics.fill(x + width - 2, y, x + width, y + height, STONE_DARK);
        guiGraphics.fill(x, y + height - 2, x + width, y + height, STONE_DARK);
    }

    private void drawThinGreenBox(
            GuiGraphics guiGraphics,
            int x,
            int y,
            int width,
            int height
    ) {

        guiGraphics.fill(x, y, x + width, y + 1, STONE_LIGHT);
        guiGraphics.fill(x, y, x + 1, y + height, STONE_LIGHT);
        guiGraphics.fill(x + width - 1, y, x + width, y + height, STONE_DARK);
        guiGraphics.fill(x, y + height - 1, x + width, y + height, STONE_DARK);
    }

    private void drawMinecraftPanel(
            GuiGraphics guiGraphics,
            int x,
            int y,
            int width,
            int height
    ) {

        guiGraphics.fill(x + 6, y + 7, x + width + 6, y + height + 7, SHADOW);
        guiGraphics.fill(x + 2, y + 2, x + width - 2, y + height - 2, PANEL);

        guiGraphics.fill(x, y, x + width, y + 2, STONE_LIGHT);
        guiGraphics.fill(x, y, x + 2, y + height, STONE_LIGHT);
        guiGraphics.fill(x, y + height - 2, x + width, y + height, STONE_DARK);
        guiGraphics.fill(x + width - 2, y, x + width, y + height, STONE_DARK);

        drawBorder(guiGraphics, x + 4, y + 4, width - 8, height - 8);
    }

    private void drawRaisedBox(
            GuiGraphics guiGraphics,
            int x,
            int y,
            int width,
            int height,
            int fill
    ) {

        guiGraphics.fill(x + 2, y + 2, x + width + 2, y + height + 2, SHADOW);
        guiGraphics.fill(x, y, x + width, y + height, fill);
        guiGraphics.fill(x, y, x + width, y + 1, STONE_LIGHT);
        guiGraphics.fill(x, y, x + 1, y + height, STONE_LIGHT);
        guiGraphics.fill(x, y + height - 1, x + width, y + height, STONE_DARK);
        guiGraphics.fill(x + width - 1, y, x + width, y + height, STONE_DARK);
    }

    private void drawInsetBox(
            GuiGraphics guiGraphics,
            int x,
            int y,
            int width,
            int height
    ) {

        guiGraphics.fill(x, y, x + width, y + height, INSET);
        guiGraphics.fill(x, y, x + width, y + 1, STONE_DARK);
        guiGraphics.fill(x, y, x + 1, y + height, STONE_DARK);
        guiGraphics.fill(x, y + height - 1, x + width, y + height, STONE_LIGHT);
        guiGraphics.fill(x + width - 1, y, x + width, y + height, STONE_LIGHT);
        drawBorder(guiGraphics, x + 2, y + 2, width - 4, height - 4);
    }

    private void drawBorder(
            GuiGraphics guiGraphics,
            int x,
            int y,
            int width,
            int height
    ) {

        guiGraphics.fill(x, y, x + width, y + 1, BORDER);
        guiGraphics.fill(x, y, x + 1, y + height, BORDER);
        guiGraphics.fill(x + width - 1, y, x + width, y + height, BORDER);
        guiGraphics.fill(x, y + height - 1, x + width, y + height, BORDER);
    }

    private void drawCornerTopLeft(GuiGraphics g, int x, int y) {
        g.fill(x, y, x + 6, y + 1, WHITE);
        g.fill(x, y, x + 1, y + 6, WHITE);
    }

    private void drawCornerTopRight(GuiGraphics g, int x, int y) {
        g.fill(x - 5, y, x + 1, y + 1, WHITE);
        g.fill(x, y, x + 1, y + 6, WHITE);
    }

    private void drawCornerBottomLeft(GuiGraphics g, int x, int y) {
        g.fill(x, y, x + 6, y + 1, WHITE);
        g.fill(x, y - 5, x + 1, y + 1, WHITE);
    }

    private void drawCornerBottomRight(GuiGraphics g, int x, int y) {
        g.fill(x - 5, y, x + 1, y + 1, WHITE);
        g.fill(x, y - 5, x + 1, y + 1, WHITE);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
