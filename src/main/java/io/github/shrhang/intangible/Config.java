package io.github.shrhang.intangible;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.commons.lang3.tuple.Pair;

public class Config {
    public static final Client CLIENT;
    public static final Server SERVER;
    static final ForgeConfigSpec clientSpec;
    static final ForgeConfigSpec serverSpec;

    static {
        Pair<Client, ForgeConfigSpec> clientPair = new ForgeConfigSpec.Builder().configure(Client::new);
        CLIENT = clientPair.getLeft();
        clientSpec = clientPair.getRight();

        Pair<Server, ForgeConfigSpec> serverPair = new ForgeConfigSpec.Builder().configure(Server::new);
        SERVER = serverPair.getLeft();
        serverSpec = serverPair.getRight();
    }

    public static void register(FMLJavaModLoadingContext context) {
        context.registerConfig(ModConfig.Type.SERVER, serverSpec);
    }

    public static void registerClient(FMLJavaModLoadingContext context) {
        context.registerConfig(ModConfig.Type.CLIENT, clientSpec);
    }

    public static class Client {
        public final ForgeConfigSpec.BooleanValue isIntangibleRender;
        public final ForgeConfigSpec.LongValue intangibleRenderColor;
        public final ForgeConfigSpec.BooleanValue skipCameraBlockZoom;

        Client(ForgeConfigSpec.Builder builder) {
            builder.push("rendering");
            isIntangibleRender = builder
                    .translation("config.intangible.render.isintangiblerender")
                    .comment("Whether to render the translucent intangible player overlay.")
                    .define("isIntangibleRender", true);
            intangibleRenderColor = builder
                    .translation("config.intangible.render.intangiblerendercolor")
                    .comment("ARGB color for the intangible player overlay. Use the 0xAARRGGBB format. Default is 0x409AE9B6L.")
                    .defineInRange("intangibleRenderColor", 0x409AE9B6L, 0x00000000L, 0xFFFFFFFFL);
            skipCameraBlockZoom = builder
                    .translation("config.intangible.render.skipcamerablockzoom")
                    .comment("Whether third-person camera block zoom should be skipped while the local player is intangible.")
                    .define("skipCameraBlockZoom", true);
            builder.pop();
        }

        public int getIntangibleRenderColor() {
            return (int) (long) intangibleRenderColor.get();
        }
    }

    public static class Server {
        public final ForgeConfigSpec.BooleanValue isSlayTheSpire;
        public final ForgeConfigSpec.IntValue intangibleDurationCostOnDamage;

        Server(ForgeConfigSpec.Builder builder) {
            builder.push("features");
            isSlayTheSpire = builder
                    .translation("config.intangible.features.isslaythespire")
                    .comment("Whether intangible should work like Slay the Spire's Intangible: most incoming damage above 1 is reduced to 1 and consumes effect duration.")
                    .define("isSlayTheSpire", true);
            intangibleDurationCostOnDamage = builder
                    .translation("config.intangible.features.intangibledurationcostondamage")
                    .comment("Duration cost in ticks when intangible reduces incoming damage.")
                    .defineInRange("intangibleDurationCostOnDamage", 600, 1, Integer.MAX_VALUE);
            builder.pop();
        }
    }
}
