package com.magicworld.central;

public record MagicWorldCentralResidentPlan(
        String name,
        String role,
        MagicWorldCentralSector sector,
        String status
) {
}
