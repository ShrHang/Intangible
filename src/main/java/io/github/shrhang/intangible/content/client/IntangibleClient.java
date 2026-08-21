package io.github.shrhang.intangible.content.client;

import io.github.shrhang.intangible.Config;
import io.github.shrhang.intangible.Intangible;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;

@Mod(value = Intangible.MODID, dist = Dist.CLIENT)
public class IntangibleClient {
        public IntangibleClient(IEventBus modEventBus, ModContainer modContainer) {
            Config.registerClient(modContainer);
            modContainer.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
            modEventBus.addListener(IntangibleRender::registerLayers);
            modEventBus.addListener(IntangibleRender::registerRenderStateModifiers);
            NeoForge.EVENT_BUS.addListener(IntangibleRender::onMovementInputUpdate);
        }

}
