package com.magicworld.event;

import com.magicworld.MagicWorldWorldOptions;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AuraEvents {
    private static final String PLAYER_AURA_KEY = "MagicWorldAuraEnabled";
    private static final Map<UUID, DeathReturn> DEATH_RETURNS = new HashMap<>();

    private record DeathReturn(ResourceKey<Level> dimension, Vec3 position, float yRot, float xRot) {
    }

    @SubscribeEvent
    public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && MagicWorldWorldOptions.isAuraEnabled()) {
            player.getPersistentData().putBoolean(PLAYER_AURA_KEY, true);
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal("Magic World: aura inicial ativada."));
        }
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END
                || !(event.player instanceof ServerPlayer player)
                || player.level().isClientSide()
                || !hasAura(player)) {
            return;
        }

        player.getAbilities().mayBuild = true;
        player.getAbilities().setWalkingSpeed(player.isSprinting() ? 0.18F : 0.1F);
        player.onUpdateAbilities();

        player.setRemainingFireTicks(0);
        player.extinguishFire();
        player.setAirSupply(player.getMaxAirSupply());
        player.setTicksFrozen(0);
        player.getFoodData().eat(20, 1.0F);

        addInvisibleEffect(player, MobEffects.FIRE_RESISTANCE, 2);
        addInvisibleEffect(player, MobEffects.WATER_BREATHING, 0);
        addInvisibleEffect(player, MobEffects.DIG_SPEED, 4);
        addInvisibleEffect(player, MobEffects.SATURATION, 0);

        if (player.horizontalCollision && player.onGround()) {
            player.jumpFromGround();
        }
    }

    @SubscribeEvent
    public void onLivingAttack(LivingAttackEvent event) {
        if (event.getEntity() instanceof Player player && hasAura(player) && isProtectedEnvironmentalDamage(event.getSource())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onLivingHurt(LivingHurtEvent event) {
        if (event.getEntity() instanceof Player player && hasAura(player) && isProtectedEnvironmentalDamage(event.getSource())) {
            event.setCanceled(true);
            event.setAmount(0.0F);
        }
    }

    @SubscribeEvent
    public void onFall(LivingFallEvent event) {
        if (event.getEntity() instanceof Player player && hasAura(player)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onAttackEntity(AttackEntityEvent event) {
        if (event.getEntity().level().isClientSide() || !hasAura(event.getEntity())) {
            return;
        }

        if (event.getTarget() instanceof LivingEntity target) {
            event.setCanceled(true);
            target.hurt(target.damageSources().magic(), Float.MAX_VALUE);
        }
    }

    @SubscribeEvent
    public void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        if (event.getHand() != InteractionHand.MAIN_HAND
                || event.getLevel().isClientSide()
                || !hasAura(event.getEntity())) {
            return;
        }

        BlockPos pos = event.getPos();
        if (event.getLevel().getBlockState(pos).isAir()) {
            return;
        }

        event.setCanceled(true);
        boolean destroyed = event.getLevel().destroyBlock(pos, true, event.getEntity());
        if (!destroyed) {
            event.getLevel().setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
        }
    }

    @SubscribeEvent
    public void onLivingDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || !hasAura(player)) {
            return;
        }

        DEATH_RETURNS.put(
                player.getUUID(),
                new DeathReturn(player.level().dimension(), player.position(), player.getYRot(), player.getXRot())
        );
    }

    @SubscribeEvent
    public void onPlayerClone(PlayerEvent.Clone event) {
        if (!event.isWasDeath() || !hasAura(event.getOriginal())) {
            return;
        }

        Player clone = event.getEntity();
        CompoundTag cloneData = clone.getPersistentData();
        cloneData.putBoolean(PLAYER_AURA_KEY, true);
        clone.getInventory().replaceWith(event.getOriginal().getInventory());
        clone.experienceLevel = event.getOriginal().experienceLevel;
        clone.totalExperience = event.getOriginal().totalExperience;
        clone.experienceProgress = event.getOriginal().experienceProgress;
    }

    @SubscribeEvent
    public void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || !hasAura(player)) {
            return;
        }

        DeathReturn deathReturn = DEATH_RETURNS.remove(player.getUUID());
        if (deathReturn == null) {
            return;
        }

        ServerLevel level = player.server.getLevel(deathReturn.dimension());
        if (level == null) {
            return;
        }

        player.teleportTo(
                level,
                deathReturn.position().x(),
                deathReturn.position().y(),
                deathReturn.position().z(),
                player.getYRot(),
                player.getXRot()
        );
    }

    private static boolean hasAura(Player player) {
        return player.getPersistentData().getBoolean(PLAYER_AURA_KEY);
    }

    private static boolean isProtectedEnvironmentalDamage(DamageSource source) {
        if (source.getEntity() != null || source.getDirectEntity() != null) {
            return false;
        }

        return source.is(DamageTypeTags.IS_FIRE)
                || source.is(DamageTypeTags.IS_FALL)
                || source.is(DamageTypeTags.IS_DROWNING)
                || source.is(DamageTypeTags.IS_FREEZING)
                || source.is(DamageTypeTags.IS_LIGHTNING);
    }

    private static void addInvisibleEffect(ServerPlayer player, net.minecraft.world.effect.MobEffect effect, int amplifier) {
        MobEffectInstance current = player.getEffect(effect);
        if (current != null && !current.isVisible() && !current.showIcon() && current.getAmplifier() >= amplifier && current.getDuration() > 20) {
            return;
        }
        if (current != null) {
            player.removeEffect(effect);
        }
        player.addEffect(new MobEffectInstance(effect, 80, amplifier, false, false, false));
    }
}
