package com.magicworld.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.enderdragon.EnderDragonPart;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

public class PeacefulDragon extends EnderDragon {
    private static final String CENTER_X_KEY = "MagicWorldCenterX";
    private static final String CENTER_Y_KEY = "MagicWorldCenterY";
    private static final String CENTER_Z_KEY = "MagicWorldCenterZ";
    private static final double PORTAL_Z_OFFSET = 90.0D;
    private static final double CASTLE_X_OFFSET = 172.0D;
    private static final int PHASE_TICKS = 900;
    private static final int LANDING_TICKS = 220;

    private BlockPos estateCenter = BlockPos.ZERO;

    public PeacefulDragon(EntityType<? extends EnderDragon> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.setNoGravity(true);
        this.setInvulnerable(true);
        this.setPersistenceRequired();
        this.setSilent(false);
    }

    public void setEstateCenter(BlockPos center) {
        this.estateCenter = center.immutable();
        this.setFightOrigin(center);
    }

    @Override
    public void aiStep() {
        this.noPhysics = true;
        this.setNoGravity(true);
        this.setInvulnerable(true);
        this.oFlapTime = this.flapTime;
        this.flapTime += 0.12F;

        if (this.estateCenter.equals(BlockPos.ZERO)) {
            this.setEstateCenter(this.blockPosition());
        }

        int phase = Math.floorMod(this.tickCount / PHASE_TICKS, 6);
        int phaseTick = Math.floorMod(this.tickCount, PHASE_TICKS);
        Vec3 point = routePoint(phase);
        boolean landing = phase % 2 == 1 && phaseTick < LANDING_TICKS;
        double tick = phaseTick * 0.012D;
        double x;
        double y;
        double z;
        double nextX;
        double nextZ;

        if (landing) {
            double landingTick = phaseTick * 0.04D;
            double landingRadius = 6.0D;
            x = point.x + Math.sin(landingTick) * landingRadius;
            z = point.z + Math.cos(landingTick) * landingRadius;
            y = point.y + 7.0D + Math.sin(landingTick * 0.5D) * 1.5D;
            nextX = point.x + Math.sin(landingTick + 0.04D) * landingRadius;
            nextZ = point.z + Math.cos(landingTick + 0.04D) * landingRadius;
        } else {
            double radius = phase == 0 ? 58.0D : phase == 2 ? 22.0D : 120.0D;
            x = point.x + Math.sin(tick) * radius;
            z = point.z + Math.cos(tick) * radius;
            y = point.y + 14.0D + Math.sin(tick * 1.7D) * 6.0D;

            double nextTick = tick + 0.012D;
            nextX = point.x + Math.sin(nextTick) * radius;
            nextZ = point.z + Math.cos(nextTick) * radius;
        }
        float yRot = (float) (Mth.atan2(nextZ - z, nextX - x) * Mth.RAD_TO_DEG) + 90.0F;

        Vec3 previous = this.position();
        this.setYRot(yRot);
        this.yRotO = yRot;
        this.setDeltaMovement(new Vec3(x - previous.x, y - previous.y, z - previous.z));
        this.setPos(x, y, z);
        this.flightHistory.record(this.getY(), this.getYRot());
    }

    private Vec3 routePoint(int phase) {
        double x = this.estateCenter.getX() + 0.5D;
        double y = this.estateCenter.getY();
        double z = this.estateCenter.getZ() + 0.5D;
        return switch (phase) {
            case 0, 1 -> new Vec3(x, y, z - 8.0D);
            case 2, 3 -> new Vec3(x, y, z + PORTAL_Z_OFFSET);
            default -> new Vec3(x + CASTLE_X_OFFSET, y, z + PORTAL_Z_OFFSET);
        };
    }

    @Override
    public boolean canAttack(LivingEntity target) {
        return false;
    }

    @Override
    public boolean hurt(ServerLevel level, EnderDragonPart part, DamageSource source, float damage) {
        return false;
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        return false;
    }

    @Override
    public void onCrystalDestroyed(ServerLevel level, EndCrystal crystal, BlockPos pos, DamageSource source) {
    }

    @Override
    public boolean canUsePortal(boolean allowPassengers) {
        return false;
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putInt(CENTER_X_KEY, this.estateCenter.getX());
        output.putInt(CENTER_Y_KEY, this.estateCenter.getY());
        output.putInt(CENTER_Z_KEY, this.estateCenter.getZ());
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.setEstateCenter(new BlockPos(
                input.getIntOr(CENTER_X_KEY, this.getBlockX()),
                input.getIntOr(CENTER_Y_KEY, this.getBlockY()),
                input.getIntOr(CENTER_Z_KEY, this.getBlockZ())
        ));
    }
}
