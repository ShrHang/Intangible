package io.github.shrhang.intangible.content;

import net.minecraft.world.entity.player.Player;

public class IntangibleState {
    private boolean active;
    private boolean mayfly;
    private boolean flying;

    public boolean isActive() {
        return active;
    }

    public void captureBeforeEffect(Player player) {
        var abilities = player.getAbilities();
        active = true;
        mayfly = abilities.mayfly;
        flying = abilities.flying;
    }

    public void restoreBeforeEffect(Player player) {
        if (!active) return;

        var abilities = player.getAbilities();
        player.noPhysics = player.isSpectator();
        if (!player.isSpectator()) {
            player.setNoGravity(false);
        }

        abilities.mayfly = mayfly;
        abilities.flying = flying;
        active = false;
    }

    public void recaptureAfterLogin(Player player) {
        player.getAbilities().mayfly = player.isCreative() || player.isSpectator();
        if (!player.getAbilities().mayfly) {
            player.getAbilities().flying = false;
        }

        captureBeforeEffect(player);
        applyFlight(player);
    }

    public static boolean applyFlight(Player player) {
        var abilities = player.getAbilities();
        boolean changed = !abilities.mayfly || !abilities.flying;

        abilities.mayfly = true;
        abilities.flying = true;

        return changed;
    }
}
