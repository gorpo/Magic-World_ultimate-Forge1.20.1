package com.magicworld.client.menus;

import com.magicworld.client.MagicWorldGraphicsProfile;
import com.magicworld.client.PremiumEntry;
import com.magicworld.client.PremiumMenuScreen;
import net.minecraft.world.item.Items;

import java.util.List;

public final class GraphicsProfilesMenu {
    private GraphicsProfilesMenu() {
    }

    public static void add(
            List<PremiumEntry> entries
    ) {
        profile(entries, MagicWorldGraphicsProfile.ULTRA_FRACO, Items.REDSTONE);
        profile(entries, MagicWorldGraphicsProfile.FRACO, Items.COAL);
        profile(entries, MagicWorldGraphicsProfile.INTERMEDIARIO, Items.COPPER_INGOT);
        profile(entries, MagicWorldGraphicsProfile.MEDIO, Items.IRON_INGOT);
        profile(entries, MagicWorldGraphicsProfile.FORTE, Items.DIAMOND);
        profile(entries, MagicWorldGraphicsProfile.ULTRA_FORTE, Items.NETHER_STAR);
    }

    private static void profile(
            List<PremiumEntry> entries,
            MagicWorldGraphicsProfile profile,
            net.minecraft.world.item.Item icon
    ) {
        MenuEntryFactory.command(
                entries,
                PremiumMenuScreen.MenuTab.GRAPHICS_PROFILES,
                profile.label(),
                profile.name(),
                "Perfil grafico",
                profile.description(),
                "Aplica opcoes vanilla agora. Shader/resource pack e efeitos ficam preparados para evolucao futura.",
                icon,
                "GRAPHICS_PROFILE:" + profile.name()
        );
    }
}
