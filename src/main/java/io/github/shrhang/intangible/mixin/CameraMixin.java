package io.github.shrhang.intangible.mixin;

import io.github.shrhang.intangible.Config;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static io.github.shrhang.intangible.Intangible.INTANGIBLE;

@Mixin(value = Camera.class, priority = 823)
public class CameraMixin {
    @Inject(method = "getMaxZoom", at = @At("HEAD"), cancellable = true)
    private void intangible$skipBlockZoom(float maxZoom, CallbackInfoReturnable<Float> callback) {
        if (!Config.CLIENT.skipCameraBlockZoom.get()) return;

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player != null && minecraft.player.hasEffect(INTANGIBLE)) {
            callback.setReturnValue(maxZoom);
        }
    }
}
