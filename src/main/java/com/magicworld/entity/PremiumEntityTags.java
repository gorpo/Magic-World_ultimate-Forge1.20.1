package com.magicworld.entity;

import net.minecraft.world.entity.Entity;

public final class PremiumEntityTags {

    private static final String ANIMAL_PREFIX =
            "VarinhaMagicaPremiumAnimal=";

    private PremiumEntityTags() {
    }

    public static void markAnimal(
            Entity entity,
            String origin
    ) {
        entity.addTag(
                ANIMAL_PREFIX + origin
        );
    }

    public static boolean isAnimal(
            Entity entity,
            String origin
    ) {
        return entity.entityTags().contains(
                ANIMAL_PREFIX + origin
        );
    }

    public static boolean isPremiumAnimal(
            Entity entity
    ) {
        return entity.entityTags()
                .stream()
                .anyMatch(tag -> tag.startsWith(ANIMAL_PREFIX));
    }

    public static void clearAnimal(
            Entity entity,
            String origin
    ) {
        entity.removeTag(
                ANIMAL_PREFIX + origin
        );
    }
}
