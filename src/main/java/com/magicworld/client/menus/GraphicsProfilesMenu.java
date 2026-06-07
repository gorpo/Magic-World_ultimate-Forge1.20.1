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
        MenuEntryFactory.command(
                entries,
                PremiumMenuScreen.MenuTab.GRAPHICS_PROFILES,
                "Verificar shaders",
                "CheckShaderLoader",
                "Shaders / Oculus",
                "Confere se ha loader de shaders instalado no Forge.",
                "Usa Oculus no Forge 1.20.1 com Embeddium para abrir shaders.",
                Items.SPYGLASS,
                "CHECK_SHADER_LOADER"
        );
        MenuEntryFactory.command(
                entries,
                PremiumMenuScreen.MenuTab.GRAPHICS_PROFILES,
                "Pasta shaderpacks",
                "OpenShaderpacksFolder",
                "Shaders",
                "Abre a pasta local de shaderpacks deste run.",
                "Coloque os ZIPs aqui e selecione pelo menu do Oculus.",
                Items.LANTERN,
                "OPEN_SHADERPACKS_FOLDER"
        );
        MenuEntryFactory.command(
                entries,
                PremiumMenuScreen.MenuTab.GRAPHICS_PROFILES,
                "Pasta resourcepacks",
                "OpenResourcepacksFolder",
                "Resource packs",
                "Abre a pasta local de resourcepacks deste run.",
                "Os packs MagicWorld locais foram preparados para pack_format 15 da 1.20.1.",
                Items.PAINTING,
                "OPEN_RESOURCEPACKS_FOLDER"
        );

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
                "Aplica opcoes vanilla agora. Shaders dependem de loader externo compativel.",
                icon,
                "GRAPHICS_PROFILE:" + profile.name()
        );
    }
}
