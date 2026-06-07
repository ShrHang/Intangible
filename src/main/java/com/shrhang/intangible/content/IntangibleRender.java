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
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

import static com.shrhang.intangible.Intangible.INTANGIBLE;
import static com.shrhang.intangible.Intangible.rl;

@OnlyIn(Dist.CLIENT)
public class IntangibleRender {

    public static void registerLayers(EntityRenderersEvent.AddLayers event) {
        addLayer(event, PlayerSkin.Model.WIDE);
        addLayer(event, PlayerSkin.Model.SLIM);
    }

    private static void addLayer(EntityRenderersEvent.AddLayers event, PlayerSkin.Model skinModel) {
        var renderer = event.getSkin(skinModel);
        if (renderer instanceof PlayerRenderer playerRenderer) {
            playerRenderer.addLayer(new IntangiblePlayerLayer(playerRenderer));
        }
    }

    private static class IntangiblePlayerLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
        private static final ResourceLocation WHITE_TEXTURE = rl("textures/entity/intangible_overlay.png");

        public IntangiblePlayerLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> renderer) {
            super(renderer);
        }

        @Override
        public void render(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, AbstractClientPlayer player,
                           float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
            if (!Config.CLIENT.isIntangibleRender.get()) return;
            if (!player.hasEffect(INTANGIBLE)) return;
            if (player.isSpectator()) return;

            var model = getParentModel();
            var renderType = RenderType.itemEntityTranslucentCull(WHITE_TEXTURE);
            var vertexConsumer = bufferSource.getBuffer(renderType);

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
