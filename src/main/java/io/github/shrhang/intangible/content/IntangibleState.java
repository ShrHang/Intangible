package io.github.shrhang.intangible.content;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.NeoForgeMod;

import static io.github.shrhang.intangible.Intangible.rl;

public class IntangibleState {
    private static final ResourceLocation FLIGHT_MODIFIER = rl("intangible_flight");

    private boolean active;
    private boolean flying;

    /**
     * @return 是否已经保存过进入无实体前的状态。
     */
    public boolean isActive() {
        return active;
    }

    /**
     * 保存玩家进入无实体前的飞行状态。
     * 只应在无实体效果刚被添加，或登录后需要重建状态时调用。
     */
    public void captureBeforeEffect(Player player) {
        var abilities = player.getAbilities();
        active = true;
        flying = abilities.flying;
    }

    /**
     * 恢复玩家进入无实体前的飞行状态，并撤销无实体授予的飞行能力。
     */
    public void restoreBeforeEffect(Player player) {
        if (!active) return;

        var abilities = player.getAbilities();
        player.noPhysics = player.isSpectator();
        if (!player.isSpectator()) {
            player.setNoGravity(false);
        }

        revokeFlight(player);
        abilities.flying = flying;
        active = false;
    }

    /**
     * 清理上一次退出游戏时遗留的无实体飞行，再重新保存当前真实状态，并立即应用无实体飞行。
     */
    public void recaptureAfterLogin(Player player) {
        revokeFlight(player);
        if (!hasExternalFlight(player)) {
            player.getAbilities().flying = false;
        }

        captureBeforeEffect(player);
        applyFlight(player);
    }

    /**
     * 在无实体效果生效期间持续应用飞行能力。
     * 创造、旁观或其他模组提供飞行能力时，不强制覆盖飞行速度；
     * 只有飞行能力来自无实体本身时，才使用无实体的低速飞行。
     */
    public static boolean applyFlight(Player player) {
        boolean changed = grantFlight(player);

        var abilities = player.getAbilities();
        changed |= !abilities.flying;
        abilities.flying = true;

        return changed;
    }

    /**
     * 通过 NeoForge 的创造飞行属性授予飞行能力。
     */
    private static boolean grantFlight(Player player) {
        var attribute = player.getAttribute(NeoForgeMod.CREATIVE_FLIGHT);
        if (attribute == null || attribute.hasModifier(FLIGHT_MODIFIER)) {
            return false;
        }

        attribute.addTransientModifier(new AttributeModifier(
                FLIGHT_MODIFIER,
                1.0,
                AttributeModifier.Operation.ADD_VALUE
        ));
        return true;
    }

    /**
     * 移除无实体添加的创造飞行属性修饰符。
     */
    private static void revokeFlight(Player player) {
        var attribute = player.getAttribute(NeoForgeMod.CREATIVE_FLIGHT);
        if (attribute != null) {
            attribute.removeModifier(FLIGHT_MODIFIER);
        }
    }

    /**
     * 判断玩家是否拥有非无实体来源的飞行能力。
     */
    private static boolean hasExternalFlight(Player player) {
        if (player.isCreative() || player.isSpectator()) return true;

        var attribute = player.getAttribute(NeoForgeMod.CREATIVE_FLIGHT);
        if (attribute == null) return false;

        double value = attribute.getValue();
        if (attribute.hasModifier(FLIGHT_MODIFIER)) {
            value -= 1.0;
        }
        return value > 0;
    }
}
