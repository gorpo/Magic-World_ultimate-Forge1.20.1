package com.magicworld;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@EventBusSubscriber(modid = MagicWorld.MODID)
public class Config {

    private static final ModConfigSpec.Builder BUILDER =
            new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue LOG_DIRT_BLOCK =
            BUILDER
                    .comment("Whether to log the dirt block on common setup")
                    .define("logDirtBlock", true);

    public static final ModConfigSpec.IntValue MAGIC_NUMBER =
            BUILDER
                    .comment("A magic number")
                    .defineInRange(
                            "magicNumber",
                            42,
                            0,
                            Integer.MAX_VALUE
                    );

    public static final ModConfigSpec.ConfigValue<String> MAGIC_NUMBER_INTRODUCTION =
            BUILDER
                    .comment("What you want the introduction message to be for the magic number")
                    .define(
                            "magicNumberIntroduction",
                            "The magic number is... "
                    );

    public static final ModConfigSpec.ConfigValue<List<? extends String>> ITEM_STRINGS =
            BUILDER
                    .comment("A list of items to log on common setup.")
                    .defineListAllowEmpty(
                            "items",
                            List.of("minecraft:iron_ingot"),
                            () -> "",
                            Config::validateItemName
                    );

    public static final ModConfigSpec.BooleanValue STARTER_PORTAL_ENABLED =
            BUILDER
                    .comment("Enables the Magic World starter portal logical marker.")
                    .define("starterPortalEnabled", true);

    public static final ModConfigSpec.ConfigValue<String> VISUAL_EXPERIENCE_START_MODE =
            BUILDER
                    .comment(
                            "Controls the initial visual experience flow.",
                            "Allowed values:",
                            "portal_active - visual experience starts unlocked.",
                            "no_portal - no portal progression is required.",
                            "locked_until_portal - visual experience unlocks when the player interacts with or enters a starter portal marker."
                    )
                    .define(
                            "visualExperienceStartMode",
                            "locked_until_portal",
                            Config::validateVisualStartMode
                    );

    static final ModConfigSpec SPEC =
            BUILDER.build();

    public static boolean logDirtBlock;
    public static int magicNumber;
    public static String magicNumberIntroduction;
    public static Set<Item> items;
    public static boolean starterPortalEnabled = true;
    public static String visualExperienceStartMode = "locked_until_portal";

    private static boolean validateItemName(
            final Object obj
    ) {
        return obj instanceof final String itemName
                && BuiltInRegistries.ITEM.containsKey(
                Identifier.parse(itemName)
        );
    }

    private static boolean validateVisualStartMode(
            final Object obj
    ) {
        if (!(obj instanceof String value)) {
            return false;
        }

        return value.equals("portal_active")
                || value.equals("no_portal")
                || value.equals("locked_until_portal");
    }

    @SubscribeEvent
    static void onLoad(
            final ModConfigEvent event
    ) {
        logDirtBlock =
                LOG_DIRT_BLOCK.get();

        magicNumber =
                MAGIC_NUMBER.get();

        magicNumberIntroduction =
                MAGIC_NUMBER_INTRODUCTION.get();

        items =
                ITEM_STRINGS
                        .get()
                        .stream()
                        .map(itemName ->
                                BuiltInRegistries.ITEM.getValue(
                                        Identifier.parse(itemName)
                                )
                        )
                        .collect(Collectors.toSet());

        starterPortalEnabled =
                STARTER_PORTAL_ENABLED.get();

        visualExperienceStartMode =
                VISUAL_EXPERIENCE_START_MODE.get();
    }
}
