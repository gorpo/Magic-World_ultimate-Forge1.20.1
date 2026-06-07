package com.magicworld.client;

import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;

public final class MagicWorldEntityCulling {
    private static final boolean ENABLED = booleanProperty("magicworld.entity_culling", true);
    private static final int CACHE_TICKS = intProperty("magicworld.entity_culling_cache_ticks", 5);
    private static final double NEAR_DISTANCE_SQR = square(doubleProperty("magicworld.entity_culling_near_blocks", 28.0D));
    private static final double OCCLUSION_DISTANCE_SQR = square(doubleProperty("magicworld.entity_culling_occlusion_blocks", 36.0D));
    private static final double SMALL_ENTITY_DISTANCE_SQR = square(doubleProperty("magicworld.entity_culling_small_entity_blocks", 96.0D));
    private static final int MAX_CACHE_SIZE = intProperty("magicworld.entity_culling_cache_size", 4096);
    private static final Map<Integer, CacheEntry> CACHE = new HashMap<>();

    private MagicWorldEntityCulling() {
    }

    public static boolean shouldSkip(Entity entity, double cameraX, double cameraY, double cameraZ) {
        if (!ENABLED || entity == null) {
            return false;
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || entity.level() != minecraft.level || alwaysVisible(minecraft, entity)) {
            return false;
        }

        double distanceSqr = entity.distanceToSqr(cameraX, cameraY, cameraZ);
        if (distanceSqr <= NEAR_DISTANCE_SQR) {
            return false;
        }

        AABB box = entity.getBoundingBox();
        if (isSmallEntity(box) && distanceSqr >= SMALL_ENTITY_DISTANCE_SQR) {
            return true;
        }

        if (distanceSqr < OCCLUSION_DISTANCE_SQR) {
            return false;
        }

        long gameTime = minecraft.level.getGameTime();
        int cameraCellX = cell(cameraX);
        int cameraCellY = cell(cameraY);
        int cameraCellZ = cell(cameraZ);
        int entityCellX = cell(entity.getX());
        int entityCellY = cell(entity.getY());
        int entityCellZ = cell(entity.getZ());
        CacheEntry cached = CACHE.get(entity.getId());
        if (cached != null
                && gameTime - cached.gameTime <= CACHE_TICKS
                && cached.cameraCellX == cameraCellX
                && cached.cameraCellY == cameraCellY
                && cached.cameraCellZ == cameraCellZ
                && cached.entityCellX == entityCellX
                && cached.entityCellY == entityCellY
                && cached.entityCellZ == entityCellZ) {
            return cached.skip;
        }

        boolean skip = isOccluded(minecraft, entity, box, new Vec3(cameraX, cameraY, cameraZ));
        if (CACHE.size() > MAX_CACHE_SIZE) {
            CACHE.clear();
        }
        CACHE.put(entity.getId(), new CacheEntry(
                gameTime,
                cameraCellX,
                cameraCellY,
                cameraCellZ,
                entityCellX,
                entityCellY,
                entityCellZ,
                skip
        ));
        return skip;
    }

    private static boolean alwaysVisible(Minecraft minecraft, Entity entity) {
        return entity == minecraft.getCameraEntity()
                || entity instanceof Player
                || entity.getVehicle() != null
                || !entity.getPassengers().isEmpty();
    }

    private static boolean isSmallEntity(AABB box) {
        return box.getXsize() <= 0.7D && box.getYsize() <= 0.7D && box.getZsize() <= 0.7D;
    }

    private static boolean isOccluded(Minecraft minecraft, Entity entity, AABB box, Vec3 camera) {
        Vec3 center = box.getCenter();
        if (!blocked(minecraft, entity, camera, center)) {
            return false;
        }

        Vec3 top = new Vec3(center.x, box.maxY - Math.min(0.15D, box.getYsize() * 0.2D), center.z);
        if (!blocked(minecraft, entity, camera, top)) {
            return false;
        }

        Vec3 feet = new Vec3(center.x, box.minY + Math.min(0.2D, box.getYsize() * 0.2D), center.z);
        if (!blocked(minecraft, entity, camera, feet)) {
            return false;
        }

        if (box.getXsize() > 1.0D || box.getZsize() > 1.0D) {
            double padX = Math.min(0.45D, box.getXsize() * 0.35D);
            double padZ = Math.min(0.45D, box.getZsize() * 0.35D);
            if (!blocked(minecraft, entity, camera, center.add(padX, 0.0D, 0.0D))
                    || !blocked(minecraft, entity, camera, center.add(-padX, 0.0D, 0.0D))
                    || !blocked(minecraft, entity, camera, center.add(0.0D, 0.0D, padZ))
                    || !blocked(minecraft, entity, camera, center.add(0.0D, 0.0D, -padZ))) {
                return false;
            }
        }

        return true;
    }

    private static boolean blocked(Minecraft minecraft, Entity entity, Vec3 camera, Vec3 target) {
        HitResult hit = minecraft.level.clip(new ClipContext(
                camera,
                target,
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                minecraft.getCameraEntity() != null ? minecraft.getCameraEntity() : entity
        ));
        return hit.getType() != HitResult.Type.MISS
                && hit.getLocation().distanceToSqr(camera) + 0.35D < target.distanceToSqr(camera);
    }

    private static int cell(double value) {
        return Mth.floor(value) >> 1;
    }

    private static double square(double value) {
        return value * value;
    }

    private static boolean booleanProperty(String name, boolean fallback) {
        String value = System.getProperty(name);
        return value == null ? fallback : Boolean.parseBoolean(value);
    }

    private static int intProperty(String name, int fallback) {
        String value = System.getProperty(name);
        if (value == null) {
            return fallback;
        }
        try {
            return Math.max(1, Integer.parseInt(value));
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }

    private static double doubleProperty(String name, double fallback) {
        String value = System.getProperty(name);
        if (value == null) {
            return fallback;
        }
        try {
            return Math.max(1.0D, Double.parseDouble(value));
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }

    private record CacheEntry(
            long gameTime,
            int cameraCellX,
            int cameraCellY,
            int cameraCellZ,
            int entityCellX,
            int entityCellY,
            int entityCellZ,
            boolean skip
    ) {
    }
}
