package io.github.shrhang.intangible.content.client;

import com.google.common.reflect.TypeToken;
import com.mojang.blaze3d.vertex.PoseStack;
import io.github.shrhang.intangible.Config;
import io.github.shrhang.intangible.content.IntangibleEventHandler;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.entity.player.PlayerModelType;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.MovementInputUpdateEvent;
import net.neoforged.neoforge.client.renderstate.RegisterRenderStateModifiersEvent;

import static io.github.shrhang.intangible.Intangible.INTANGIBLE;
import static io.github.shrhang.intangible.Intangible.id;

public class IntangibleRender {
    private static final ContextKey<Boolean> HAS_INTANGIBLE_EFFECT = new ContextKey<>(id("has_intangible_effect"));

    public static void registerLayers(EntityRenderersEvent.AddLayers event) {
        addLayer(event, PlayerModelType.WIDE);
        addLayer(event, PlayerModelType.SLIM);
    }

    public static void registerRenderStateModifiers(RegisterRenderStateModifiersEvent event) {
        event.registerEntityModifier(new TypeToken<AvatarRenderer<?>>() {}, (avatar, renderState) ->
                renderState.setRenderData(HAS_INTANGIBLE_EFFECT, avatar instanceof AbstractClientPlayer player && player.hasEffect(INTANGIBLE)));
    }

    public static void onMovementInputUpdate(MovementInputUpdateEvent event) {
        if (event.getEntity().hasEffect(INTANGIBLE)) {
            IntangibleEventHandler.keepIntangibleCollisionState(event.getEntity());
        }
    }

    private static void addLayer(EntityRenderersEvent.AddLayers event, PlayerModelType skinModel) {
        AvatarRenderer<AbstractClientPlayer> playerRenderer = event.getPlayerRenderer(skinModel);
        if (playerRenderer != null) {
            playerRenderer.addLayer(new IntangiblePlayerLayer(playerRenderer));
        }
    }

    private static class IntangiblePlayerLayer extends RenderLayer<AvatarRenderState, PlayerModel> {
        private static final Identifier WHITE_TEXTURE = id("textures/entity/intangible_overlay.png");

        public IntangiblePlayerLayer(RenderLayerParent<AvatarRenderState, PlayerModel> renderer) {
            super(renderer);
        }

        @Override
        public void submit(PoseStack poseStack, SubmitNodeCollector nodeCollector, int packedLight, AvatarRenderState renderState,
                           float yRot, float xRot) {
            if (!Config.CLIENT.isIntangibleRender.get()) return;
            if (!Boolean.TRUE.equals(renderState.getRenderData(HAS_INTANGIBLE_EFFECT))) return;
            if (renderState.isSpectator) return;

            var model = getParentModel();
            var renderType = RenderTypes.itemEntityTranslucentCull(WHITE_TEXTURE);
            nodeCollector.submitModel(
                    model,
                    renderState,
                    poseStack,
                    renderType,
                    LightTexture.FULL_BRIGHT,
                    OverlayTexture.NO_OVERLAY,
                    Config.CLIENT.getIntangibleRenderColor(),
                    null,
                    renderState.outlineColor,
                    null
            );
        }
    }
}
