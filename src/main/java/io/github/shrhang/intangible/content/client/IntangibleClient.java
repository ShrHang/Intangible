package io.github.shrhang.intangible.content.client;

import io.github.shrhang.intangible.Config;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

public class IntangibleClient {
    public static void init(FMLJavaModLoadingContext context) {
        Config.registerClient(context);
        context.getModEventBus().addListener(IntangibleRender::registerLayers);
    }
}
