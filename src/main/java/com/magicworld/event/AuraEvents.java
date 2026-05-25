package com.magicworld.event;

import com.magicworld.MagicWorldWorldOptions;
import net.minecraft.core.BlockPos;
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
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class AuraEvents {
    private static final String PLAYER_AURA_KEY = "MagicWorldAuraEnabled";
    private static final Map<UUID, DeathReturn> DEATH_RETURNS = new HashMap<>();

    private AuraEvents() {
    }

    private record DeathReturn(ResourceKey<Level> dimension, Vec3 position, float yRot, float xRot) {
    }

    public static void registerListeners() {
        NeoForge.EVENT_BUS.addListener(AuraEvents::onPlayerLoggedIn);
        NeoForge.EVENT_BUS.addListener(AuraEvents::onPlayerTick);
        NeoForge.EVENT_BUS.addListener(AuraEvents::onIncomingDamage);
        NeoForge.EVENT_BUS.addListener(AuraEvents::onFall);
        NeoForge.EVENT_BUS.addListener(AuraEvents::onAttackEntity);
        NeoForge.EVENT_BUS.addListener(AuraEvents::onLeftClickBlock);
        NeoForge.EVENT_BUS.addListener(AuraEvents::onLivingDeath);
        NeoForge.EVENT_BUS.addListener(AuraEvents::onPlayerClone);
        NeoForge.EVENT_BUS.addListener(AuraEvents::onPlayerRespawn);
    }

    private static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && MagicWorldWorldOptions.isAuraEnabled()) {
            player.getPersistentData().putBoolean(PLAYER_AURA_KEY, true);
        }
    }

    private static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || player.level().isClientSide() || !hasAura(player)) {
            return;
        }

        player.getAbilities().mayBuild = true;
        player.getAbilities().setWalkingSpeed(player.isSprinting() ? 0.2F : 0.1F);
        player.onUpdateAbilities();

        player.setRemainingFireTicks(0);
        player.extinguishFire();
        player.setAirSupply(player.getMaxAirSupply());
        player.setTicksFrozen(0);
        player.getFoodData().eat(20, 1.0F);

        addInvisibleEffect(player, MobEffects.FIRE_RESISTANCE, 3);
        addInvisibleEffect(player, MobEffects.WATER_BREATHING, 0);
        addInvisibleEffect(player, MobEffects.HASTE, 15);
        addInvisibleEffect(player, MobEffects.SATURATION, 0);

        if (player.horizontalCollision && player.onGround()) {
            player.jumpFromGround();
        }
    }

    private static void onIncomingDamage(LivingIncomingDamageEvent event) {
        if (event.getEntity() instanceof Player player && hasAura(player)) {
            DamageSource source = event.getSource();
            if (isProtectedEnvironmentalDamage(source)) {
                event.setCanceled(true);
                event.setAmount(0.0F);
                event.setInvulnerabilityTicks(20);
            }
        }
    }

    private static void onFall(LivingFallEvent event) {
        if (event.getEntity() instanceof Player player && hasAura(player)) {
            event.setCanceled(true);
        }
    }

    private static void onAttackEntity(AttackEntityEvent event) {
        if (event.getEntity().level().isClientSide() || !hasAura(event.getEntity())) {
            return;
        }

        if (event.getTarget() instanceof LivingEntity target && event.getEntity().level() instanceof ServerLevel level) {
            event.setCanceled(true);
            target.kill(level);
        }
    }

    private static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        if (event.getHand() != InteractionHand.MAIN_HAND || event.getLevel().isClientSide() || !hasAura(event.getEntity())) {
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

    private static void onLivingDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || !hasAura(player)) {
            return;
        }

        DEATH_RETURNS.put(
                player.getUUID(),
                new DeathReturn(player.level().dimension(), player.position(), player.getYRot(), player.getXRot())
        );
    }

    private static void onPlayerClone(PlayerEvent.Clone event) {
        if (!event.isWasDeath() || !hasAura(event.getOriginal())) {
            return;
        }

        event.getEntity().getPersistentData().putBoolean(PLAYER_AURA_KEY, true);
        event.getEntity().getInventory().replaceWith(event.getOriginal().getInventory());
        event.getEntity().experienceLevel = event.getOriginal().experienceLevel;
        event.getEntity().totalExperience = event.getOriginal().totalExperience;
        event.getEntity().experienceProgress = event.getOriginal().experienceProgress;
    }

    private static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || !hasAura(player)) {
            return;
        }

        DeathReturn deathReturn = DEATH_RETURNS.remove(player.getUUID());
        if (deathReturn == null) {
            return;
        }

        ServerLevel level = player.level().getServer().getLevel(deathReturn.dimension());
        if (level == null) {
            return;
        }

        player.teleportTo(
                level,
                deathReturn.position().x(),
                deathReturn.position().y(),
                deathReturn.position().z(),
                java.util.Set.of(),
                deathReturn.yRot(),
                deathReturn.xRot(),
                false
        );
    }

    private static boolean hasAura(Player player) {
        return player.getPersistentData().getBoolean(PLAYER_AURA_KEY).orElse(false);
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

    private static void addInvisibleEffect(ServerPlayer player, net.minecraft.core.Holder<net.minecraft.world.effect.MobEffect> effect, int amplifier) {
        player.addEffect(new MobEffectInstance(effect, 60, amplifier, false, false, false));
    }
}
