package com.magicworld.central;

public enum MagicWorldCentralSector {
    HOUSE("Casa", "Casa e apoio"),
    CASTLE("Castelo", "Marco central e defesa"),
    FARM("Fazenda", "Plantacoes e comida"),
    STABLES("Estabulos", "Animais e currais"),
    PORTAL("Portal", "Chegada e efeitos magicos"),
    DEFENSE("Defesa", "Guardas e protecao"),
    SUPPORT("Apoio", "Baus, recursos e servicos");

    private final String label;
    private final String description;

    MagicWorldCentralSector(String label, String description) {
        this.label = label;
        this.description = description;
    }

    public String label() {
        return label;
    }

    public String description() {
        return description;
    }
}
