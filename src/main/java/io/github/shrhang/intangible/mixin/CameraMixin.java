package io.github.shrhang.intangible.mixin;

import io.github.shrhang.intangible.Config;
import io.github.shrhang.intangible.Intangible;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = Camera.class, priority = 823)
public class CameraMixin {
    @Inject(method = "getMaxZoom", at = @At("HEAD"), cancellable = true)
    private void intangible$skipBlockZoom(double distance, CallbackInfoReturnable<Double> callback) {
        if (!Config.CLIENT.skipCameraBlockZoom.get()) return;

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player != null && minecraft.player.hasEffect(Intangible.INTANGIBLE.get())) {
            callback.setReturnValue(distance);
        }
    }
}
