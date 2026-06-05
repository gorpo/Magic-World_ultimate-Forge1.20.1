package com.magicworld.client;

public enum MagicWorldGraphicsProfile {
    ULTRA_FRACO("Ultra fraco"),
    FRACO("Fraco"),
    INTERMEDIARIO("Intermediario"),
    MEDIO("Medio"),
    FORTE("Forte"),
    ULTRA_FORTE("Ultra forte");

    private final String label;

    MagicWorldGraphicsProfile(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }
}