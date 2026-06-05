package com.magicworld.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

public class KeyBindings {

    public static final String KEY_CATEGORY =
            "key.category.magicworld";

    public static final String KEY_OPEN_MENU =
            "key.magicworld.open_menu";

    public static KeyMapping OPEN_MENU_KEY;

    public static void register() {

        OPEN_MENU_KEY =
                new KeyMapping(
                        KEY_OPEN_MENU,
                        InputConstants.Type.KEYSYM,
                        GLFW.GLFW_KEY_H,
                        KEY_CATEGORY
                );
    }
}