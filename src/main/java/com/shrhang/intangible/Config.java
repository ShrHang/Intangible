package com.shrhang.intangible;

import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public class Config {
    private static final String ARGB_HEX_PATTERN = "0x[0-9a-fA-F]{8}";

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
        public final ModConfigSpec.ConfigValue<String> intangibleRenderColor;

        Client(ModConfigSpec.Builder builder) {
            builder.push("rendering");
            isIntangibleRender = builder
                    .comment("Whether to render the translucent intangible player overlay.")
                    .define("isIntangibleRender", true);
            intangibleRenderColor = builder
                    .comment("ARGB color for the intangible player overlay. Use the 0xAARRGGBB format.")
                    .define("intangibleRenderColor", "0x409AE9B6", Config::isArgbHexColor);
            builder.pop();
        }

        public int getIntangibleRenderColor() {
            String color = intangibleRenderColor.get();
            if (!isArgbHexColor(color)) {
                throw new IllegalArgumentException("Invalid intangible render color: " + color + ". Expected 0xAARRGGBB.");
            }
            return (int) Long.parseUnsignedLong(color.substring(2), 16);
        }
    }

    public static class Server {
        public final ModConfigSpec.BooleanValue isSlayTheSpire;
        Server(ModConfigSpec.Builder builder) {
            builder.push("features");
            isSlayTheSpire = builder.define("isSlayTheSpire", false);
            builder.pop();
        }
    }

    private static boolean isArgbHexColor(Object value) {
        return value instanceof String string && string.matches(ARGB_HEX_PATTERN);
    }
}
