package io.github.shrhang.intangible.content;

import io.github.shrhang.intangible.Config;
import io.github.shrhang.intangible.Intangible;
import io.github.shrhang.intangible.mixin.LivingEntityInvoker;
import io.github.shrhang.intangible.mixin.MobEffectInstanceAccessor;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityInvulnerabilityCheckEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import static io.github.shrhang.intangible.Intangible.INTANGIBLE;
import static io.github.shrhang.intangible.Intangible.INTANGIBLE_STATE;

public class IntangibleEventHandler {
    public static void init() {
        NeoForge.EVENT_BUS.addListener(IntangibleEventHandler::onInvulnerabilityCheck);
        NeoForge.EVENT_BUS.addListener(IntangibleEventHandler::onPreDamage);
        NeoForge.EVENT_BUS.addListener(IntangibleEventHandler::onEffectAdded);
        NeoForge.EVENT_BUS.addListener(IntangibleEventHandler::onPlayerTickPre);
        NeoForge.EVENT_BUS.addListener(IntangibleEventHandler::onPlayerTickPost);
        NeoForge.EVENT_BUS.addListener(IntangibleEventHandler::onPlayerLoggedIn);
        NeoForge.EVENT_BUS.addListener(IntangibleEventHandler::onPlayerLoggedOut);
    }

    private static void onInvulnerabilityCheck(final EntityInvulnerabilityCheckEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (!player.hasEffect(INTANGIBLE)) return;
        var source = event.getSource();
        if (source.is(Intangible.BYPASSES_INTANGIBLE)) return;
        if (source.is(Intangible.INTANGIBLE_IMMUNE_TO)) {
            event.setInvulnerable(true);
        }
    }

    private static void onPlayerTickPre(final PlayerTickEvent.Pre event) {
        Player player = event.getEntity();
        if (player.level().isClientSide() && player.hasEffect(INTANGIBLE)) {
            IntangibleState.applyFlight(player);
            keepIntangiblePoseState(player);
            player.setOnGround(false);
        }
    }

    private static void onPreDamage(final LivingDamageEvent.Pre event) {
        if (!Config.SERVER.isSlayTheSpire.get()) return;

        var entity = event.getEntity();
        var effect = entity.getEffect(INTANGIBLE);
        if (effect == null) return;

        var source = event.getSource();
        if (source.is(Intangible.BYPASSES_INTANGIBLE)) return;
        if (event.getNewDamage() <= 1.0f) return;

        event.setNewDamage(1.0F);
        if (effect.isInfiniteDuration()) return;

        // 保留至少 1 tick，让原版在归零时恢复被覆盖的效果或结束当前效果。
        int remainingDuration = Math.max(1, effect.getDuration() - Config.SERVER.intangibleDurationCostOnDamage.get());
        ((MobEffectInstanceAccessor) effect).intangible$setDuration(remainingDuration);
        ((LivingEntityInvoker) entity).intangible$onEffectUpdated(effect, false, null);
    }

    /**
     * 在效果被添加时捕获玩家的当前状态，如果是无实体则保存状态以便后续恢复。
     */
    private static void onEffectAdded(final MobEffectEvent.Added event) {
        if (event.getEffectInstance().is(INTANGIBLE) && event.getEntity() instanceof Player player) {
            IntangibleState state = player.getData(INTANGIBLE_STATE);
            if (!state.isActive()) state.captureBeforeEffect(player);
        }
    }

    /**
     * 在双方的玩家 tick 末尾维持或恢复姿势，并在需要时恢复飞行状态。
     */
    private static void onPlayerTickPost(final PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.hasEffect(INTANGIBLE)) {
            keepIntangiblePoseState(player);
        } else {
            IntangibleState state = player.getExistingData(INTANGIBLE_STATE).orElse(null);
            if (state != null) restore(player, state);
        }
    }

    /**
     * 在玩家登录时检查无实体状态，如果玩家处于无实体状态则重新捕获玩家的状态以防止数据错误，并进行服务端数据同步。
     */
    private static void onPlayerLoggedIn(final PlayerEvent.PlayerLoggedInEvent event) {
        Player player = event.getEntity();
        if (!player.hasEffect(INTANGIBLE)) return;

        IntangibleState state = player.getData(INTANGIBLE_STATE);
        state.recaptureAfterLogin(player);
        if (player instanceof ServerPlayer serverPlayer) serverPlayer.onUpdateAbilities();
    }

    /**
     * 在玩家登出时检查无实体状态，如果玩家处于无实体状态则复原玩家的能力以防止数据丢失。
     */
    private static void onPlayerLoggedOut(final PlayerEvent.PlayerLoggedOutEvent event) {
        Player player = event.getEntity();
        IntangibleState state = player.getExistingData(INTANGIBLE_STATE).orElse(null);
        if (state != null) restore(player, state);
    }

    /**
     * 用于复原玩家状态的辅助方法，并进行服务端数据同步。
     */
    private static void restore(Player player, IntangibleState state) {
        state.restorePose(player);
        if (!state.isActive()) return;

        state.restoreBeforeEffect(player);
        if (player instanceof ServerPlayer) player.onUpdateAbilities();
    }

    public static void keepIntangibleCollisionState(Player player) {
        player.noPhysics = true;
        keepIntangiblePoseState(player);
        player.setOnGround(false);
        player.resetFallDistance();
    }

    private static void keepIntangiblePoseState(Player player) {
        // 客户端通过效果同步包获得效果时不会触发 MobEffectEvent.Added。
        IntangibleState state = player.getData(INTANGIBLE_STATE);
        if (player.isSleeping()) {
            state.restorePose(player);
        } else {
            state.forceStandingPose(player);
        }
    }
}
