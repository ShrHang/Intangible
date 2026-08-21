package io.github.shrhang.intangible.content;

import io.github.shrhang.intangible.Config;
import io.github.shrhang.intangible.Intangible;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class IntangibleEventHandler {
    private static final Map<UUID, IntangibleState> STATES = new ConcurrentHashMap<>();

    public static void init() {
        MinecraftForge.EVENT_BUS.addListener(IntangibleEventHandler::onAttack);
        MinecraftForge.EVENT_BUS.addListener(IntangibleEventHandler::onDamage);
        MinecraftForge.EVENT_BUS.addListener(IntangibleEventHandler::onEffectAdded);
        MinecraftForge.EVENT_BUS.addListener(IntangibleEventHandler::onPlayerTick);
        MinecraftForge.EVENT_BUS.addListener(IntangibleEventHandler::onPlayerLoggedIn);
        MinecraftForge.EVENT_BUS.addListener(IntangibleEventHandler::onPlayerLoggedOut);
        MinecraftForge.EVENT_BUS.addListener(IntangibleEventHandler::onPlayerClone);
    }

    private static void onAttack(final LivingAttackEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (!player.hasEffect(Intangible.INTANGIBLE.get())) return;
        var source = event.getSource();
        if (source.is(Intangible.BYPASSES_INTANGIBLE)) return;
        if (source.is(Intangible.INTANGIBLE_IMMUNE_TO)) {
            event.setCanceled(true);
        }
    }

    private static void onDamage(final LivingDamageEvent event) {
        if (!Config.SERVER.isSlayTheSpire.get()) return;

        var entity = event.getEntity();
        var effect = entity.getEffect(Intangible.INTANGIBLE.get());
        if (effect == null) return;

        var source = event.getSource();
        if (source.is(DamageTypeTags.BYPASSES_EFFECTS) || source.is(Intangible.BYPASSES_INTANGIBLE)) return;
        if (event.getAmount() <= 1.0f) return;

        event.setAmount(1.0F);

        int duration = effect.getDuration() - Config.SERVER.intangibleDurationCostOnDamage.get();

        if (duration > 0) {
            entity.removeEffectNoUpdate(Intangible.INTANGIBLE.get());
            entity.addEffect(new MobEffectInstance(
                    Intangible.INTANGIBLE.get(),
                    duration,
                    effect.getAmplifier(),
                    effect.isAmbient(),
                    effect.isVisible(),
                    effect.showIcon()
            ));
        } else {
            entity.removeEffect(Intangible.INTANGIBLE.get());
        }
    }

    private static void onEffectAdded(final MobEffectEvent.Added event) {
        if (event.getEffectInstance().getEffect() == Intangible.INTANGIBLE.get() && event.getEntity() instanceof Player player) {
            IntangibleState state = STATES.computeIfAbsent(player.getUUID(), ignored -> new IntangibleState());
            if (!state.isActive()) state.captureBeforeEffect(player);
        }
    }

    private static void onPlayerTick(final TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        Player player = event.player;
        IntangibleState state = STATES.get(player.getUUID());
        if (state != null && state.isActive() && !player.hasEffect(Intangible.INTANGIBLE.get())) {
            restore(player, state);
            STATES.remove(player.getUUID());
        } else if (player.hasEffect(Intangible.INTANGIBLE.get())) {
            keepIntangiblePoseState(player);
        }
    }

    private static void onPlayerLoggedIn(final PlayerEvent.PlayerLoggedInEvent event) {
        Player player = event.getEntity();
        if (!player.hasEffect(Intangible.INTANGIBLE.get())) return;

        IntangibleState state = STATES.computeIfAbsent(player.getUUID(), ignored -> new IntangibleState());
        state.recaptureAfterLogin(player);
        if (player instanceof ServerPlayer serverPlayer) serverPlayer.onUpdateAbilities();
    }

    private static void onPlayerLoggedOut(final PlayerEvent.PlayerLoggedOutEvent event) {
        Player player = event.getEntity();
        IntangibleState state = STATES.remove(player.getUUID());
        if (state != null && state.isActive()) restore(player, state);
    }

    private static void onPlayerClone(final PlayerEvent.Clone event) {
        IntangibleState state = STATES.remove(event.getOriginal().getUUID());
        if (state != null && state.isActive()) restore(event.getOriginal(), state);
    }

    private static void restore(Player player, IntangibleState state) {
        player.setForcedPose(null);
        state.restoreBeforeEffect(player);
        if (player instanceof ServerPlayer serverPlayer) serverPlayer.onUpdateAbilities();
    }

    public static void keepIntangiblePoseState(Player player) {
        player.setForcedPose(Pose.STANDING);
        player.setPose(Pose.STANDING);
    }
}
