package com.shrhang.intangible;

import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public class Config {
    public static final Client CLIENT;
    public static final Server SERVER;
    static final ModConfigSpec clientSpec;
    static final ModConfigSpec serverSpec;

    static {
        Pair<?, ModConfigSpec> pair = new ModConfigSpec.Builder().configure(Client::new);
        CLIENT = (Client) pair.getLeft();
        clientSpec = pair.getRight();
        pair = new ModConfigSpec.Builder().configure(Server::new);
        SERVER = (Server) pair.getLeft();
        serverSpec = pair.getRight();
    }

    public static void register(ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.SERVER, Config.serverSpec);
        if (FMLEnvironment.dist.isClient()) {
            modContainer.registerConfig(ModConfig.Type.CLIENT, Config.clientSpec);
            modContainer.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
        }
    }

    public static class Client {
        public final ModConfigSpec.BooleanValue isIntangibleRender;
        public final ModConfigSpec.LongValue intangibleRenderColor;

        Client(ModConfigSpec.Builder builder) {
            builder.push("rendering");
            isIntangibleRender = builder
                    .comment("Whether to render the translucent intangible player overlay.")
                    .define("isIntangibleRender", true);
            intangibleRenderColor = builder
                    .comment("ARGB color for the intangible player overlay. Use the 0xAARRGGBB format. Default is 0x409AE9B6 (a semi-transparent light green).")
                    .defineInRange("intangibleRenderColor", 0x409AE9B6, 0x00000000L, 0xFFFFFFFFL);
            builder.pop();
        }

        public int getIntangibleRenderColor() {
            return (int) (long) intangibleRenderColor.get();
        }
    }

    public static class Server {
        public final ModConfigSpec.BooleanValue isSlayTheSpire;
        Server(ModConfigSpec.Builder builder) {
            builder.push("features");
            isSlayTheSpire = builder
                    .comment("Whether intangible should work like Slay the Spire's Intangible: most incoming damage above 1 is reduced to 1 and consumes one amplifier level.")
                    .define("isSlayTheSpire", true);
            builder.pop();
        }
    }
}
