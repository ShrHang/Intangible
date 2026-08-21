package io.github.shrhang.intangible.content.client;

import io.github.shrhang.intangible.Config;
import io.github.shrhang.intangible.Intangible;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@Mod(value = Intangible.MODID, dist = Dist.CLIENT)
public final class IntangibleClient {
    public IntangibleClient(IEventBus modEventBus, ModContainer modContainer) {
        Config.registerClient(modContainer);
        modContainer.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
        modEventBus.addListener(IntangibleRender::registerLayers);
    }
}