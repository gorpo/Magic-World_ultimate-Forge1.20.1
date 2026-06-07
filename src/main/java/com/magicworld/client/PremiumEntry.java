package com.magicworld.client;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

public class PremiumEntry {

    private final String name;
    private final String englishName;
    private final String category;
    private final String transformation;
    private final String attributes;
    private final Item iconItem;
    private final boolean mobTexture;
    private final PremiumMenuScreen.MenuTab tab;
    private final String normalCommand;
    private final String modifiedCommand;

    public PremiumEntry(
            String name,
            String englishName,
            String category
    ) {

        this(
                PremiumMenuScreen.MenuTab.ANIMALS,
                name,
                englishName,
                category,
                "Encantamentos",
                "+ Bonus encantamento",
                Items.EMERALD,
                true,
                "",
                ""
        );
    }

    public PremiumEntry(
            PremiumMenuScreen.MenuTab tab,
            String name,
            String englishName,
            String category,
            String transformation,
            String attributes,
            Item iconItem,
            boolean mobTexture
    ) {

        this(
                tab,
                name,
                englishName,
                category,
                transformation,
                attributes,
                iconItem,
                mobTexture,
                "",
                ""
        );
    }

    public PremiumEntry(
            PremiumMenuScreen.MenuTab tab,
            String name,
            String englishName,
            String category,
            String transformation,
            String attributes,
            Item iconItem,
            boolean mobTexture,
            String normalCommand,
            String modifiedCommand
    ) {

        this.tab = tab;
        this.name = name;
        this.englishName = englishName;
        this.category = category;
        this.transformation = transformation;
        this.attributes = attributes;
        this.iconItem = iconItem;
        this.mobTexture = mobTexture;
        this.normalCommand = normalCommand;
        this.modifiedCommand = modifiedCommand;
    }

    public String getName() {
        return name;
    }

    public String getEnglishName() {
        return englishName;
    }

    public String getCategory() {
        return category;
    }

    public String getTransformation() {
        return transformation;
    }

    public String getAttributes() {
        return attributes;
    }

    public Item getIconItem() {
        return iconItem;
    }

    public boolean usesMobTexture() {
        return mobTexture;
    }

    public PremiumMenuScreen.MenuTab getTab() {
        return tab;
    }

    public String getNormalCommand() {
        return normalCommand;
    }

    public String getModifiedCommand() {
        return modifiedCommand;
    }

    public String getDisplayName() {

        return name
                + " - "
                + englishName;
    }
}
