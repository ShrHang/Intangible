package com.shrhang.intangible.content;

import com.mojang.blaze3d.vertex.PoseStack;
import com.shrhang.intangible.Config;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.entity.state.PlayerRenderState;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.context.ContextKey;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.MovementInputUpdateEvent;
import net.neoforged.neoforge.client.renderstate.RegisterRenderStateModifiersEvent;

import static com.shrhang.intangible.Intangible.INTANGIBLE;
import static com.shrhang.intangible.Intangible.rl;

@OnlyIn(Dist.CLIENT)
public class IntangibleRender {
    private static final ContextKey<Boolean> HAS_INTANGIBLE_EFFECT = new ContextKey<>(rl("has_intangible_effect"));

    public static void registerLayers(EntityRenderersEvent.AddLayers event) {
        addLayer(event, PlayerSkin.Model.WIDE);
        addLayer(event, PlayerSkin.Model.SLIM);
    }

    public static void registerRenderStateModifiers(RegisterRenderStateModifiersEvent event) {
        event.registerEntityModifier(PlayerRenderer.class, (AbstractClientPlayer player, PlayerRenderState renderState) ->
                renderState.setRenderData(HAS_INTANGIBLE_EFFECT, player.hasEffect(INTANGIBLE)));
    }

    public static void onMovementInputUpdate(MovementInputUpdateEvent event) {
        if (event.getEntity().hasEffect(INTANGIBLE)) {
            IntangibleEventHandler.keepIntangibleCollisionState(event.getEntity());
        }
    }

    private static void addLayer(EntityRenderersEvent.AddLayers event, PlayerSkin.Model skinModel) {
        PlayerRenderer playerRenderer = event.getSkin(skinModel);
        if (playerRenderer != null) {
            playerRenderer.addLayer(new IntangiblePlayerLayer(playerRenderer));
        }
    }

    private static class IntangiblePlayerLayer extends RenderLayer<PlayerRenderState, PlayerModel> {
        private static final ResourceLocation WHITE_TEXTURE = rl("textures/entity/intangible_overlay.png");

        public IntangiblePlayerLayer(RenderLayerParent<PlayerRenderState, PlayerModel> renderer) {
            super(renderer);
        }

        @Override
        public void render(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, PlayerRenderState renderState,
                           float yRot, float xRot) {
            if (!Config.CLIENT.isIntangibleRender.get()) return;
            if (!Boolean.TRUE.equals(renderState.getRenderData(HAS_INTANGIBLE_EFFECT))) return;
            if (renderState.isSpectator) return;

            var model = getParentModel();
            var renderType = RenderType.itemEntityTranslucentCull(WHITE_TEXTURE);
            var vertexConsumer = bufferSource.getBuffer(renderType);

            model.setupAnim(renderState);
            model.renderToBuffer(
                    poseStack,
                    vertexConsumer,
                    LightTexture.FULL_BRIGHT,
                    0,
                    Config.CLIENT.getIntangibleRenderColor()
            );
        }
    }
}
