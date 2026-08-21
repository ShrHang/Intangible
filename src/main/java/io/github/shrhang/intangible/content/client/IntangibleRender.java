package io.github.shrhang.intangible.content.client;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.shrhang.intangible.Config;
import io.github.shrhang.intangible.Intangible;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid = Intangible.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class IntangibleRender {
    @SubscribeEvent
    public static void registerLayers(EntityRenderersEvent.AddLayers event) {
        addLayer(event, "default");
        addLayer(event, "slim");
    }

    private static void addLayer(EntityRenderersEvent.AddLayers event, String skinModel) {
        EntityRenderer<?> renderer = event.getPlayerSkin(skinModel);
        if (renderer instanceof PlayerRenderer playerRenderer) {
            playerRenderer.addLayer(new IntangiblePlayerLayer(playerRenderer));
        }
    }

    private static class IntangiblePlayerLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
        private static final ResourceLocation WHITE_TEXTURE = Intangible.rl("textures/entity/intangible_overlay.png");

        public IntangiblePlayerLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> renderer) {
            super(renderer);
        }

        @Override
        public void render(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, AbstractClientPlayer player,
                           float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
            if (!Config.CLIENT.isIntangibleRender.get()) return;
            if (!player.hasEffect(Intangible.INTANGIBLE.get())) return;
            if (player.isSpectator()) return;

            int color = Config.CLIENT.getIntangibleRenderColor();
            float alpha = ((color >> 24) & 0xFF) / 255.0F;
            float red = ((color >> 16) & 0xFF) / 255.0F;
            float green = ((color >> 8) & 0xFF) / 255.0F;
            float blue = (color & 0xFF) / 255.0F;

            var model = getParentModel();
            var renderType = RenderType.itemEntityTranslucentCull(WHITE_TEXTURE);
            var vertexConsumer = bufferSource.getBuffer(renderType);

            model.renderToBuffer(
                    poseStack,
                    vertexConsumer,
                    LightTexture.FULL_BRIGHT,
                    0,
                    red,
                    green,
                    blue,
                    alpha
            );
        }
    }
}
