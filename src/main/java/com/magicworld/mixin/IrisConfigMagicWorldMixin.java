package com.magicworld.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(targets = "net.irisshaders.iris.compat.sodium.config.IrisConfig", remap = false)
public abstract class IrisConfigMagicWorldMixin {
    private static final int MAGICWORLD_SHADER_THEME = 0xFF00D9FF;

    @ModifyConstant(method = "registerConfigLate", constant = @Constant(stringValue = "Iris"), remap = false)
    private String magicworld$renameShaderMenu(String original) {
        return "Magic World Shaders";
    }

    @ModifyConstant(method = "registerConfigLate", constant = @Constant(stringValue = "Settings"), remap = false)
    private String magicworld$translateSettingsPage(String original) {
        return "Configuracoes";
    }

    @ModifyConstant(method = "registerConfigLate", constant = @Constant(stringValue = "Packs"), remap = false)
    private String magicworld$translatePacksTooltip(String original) {
        return "Pacotes de shader";
    }

    @ModifyConstant(method = "registerConfigLate", constant = @Constant(stringValue = "None"), remap = false)
    private String magicworld$translateNone(String original) {
        return "Nenhuma";
    }

    @ModifyConstant(
            method = "lambda$registerConfigLate$13",
            constant = @Constant(stringValue = "This option is not relevant when a shader pack is active."),
            remap = false
    )
    private static String magicworld$translateInactiveShaderOption(String original) {
        return "Esta opcao nao e relevante enquanto um shader pack esta ativo.";
    }

    @ModifyConstant(
            method = "lambda$registerConfigLate$11",
            constant = @Constant(stringValue = " (RGSS is not usable with shaders on.)"),
            remap = false
    )
    private static String magicworld$translateRgssWarning(String original) {
        return " (RGSS nao pode ser usado com shaders ativados.)";
    }

    @ModifyConstant(method = "registerConfigLate", constant = @Constant(intValue = -698654), remap = false)
    private int magicworld$themeShaderMenu(int original) {
        return MAGICWORLD_SHADER_THEME;
    }
}
