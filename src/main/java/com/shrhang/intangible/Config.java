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
    public static final Startup STARTUP;
    static final ModConfigSpec clientSpec;
    static final ModConfigSpec serverSpec;
    static final ModConfigSpec startupSpec;

    static {
        Pair<?, ModConfigSpec> pair = new ModConfigSpec.Builder().configure(Client::new);
        CLIENT = (Client) pair.getLeft();
        clientSpec = pair.getRight();
        pair = new ModConfigSpec.Builder().configure(Server::new);
        SERVER = (Server) pair.getLeft();
        serverSpec = pair.getRight();
        pair = new ModConfigSpec.Builder().configure(Startup::new);
        STARTUP = (Startup) pair.getLeft();
        startupSpec = pair.getRight();
    }

    public static void register(ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.STARTUP, Config.startupSpec);
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
                    .translation("config.intangible.render.isintangiblerender")
                    .comment("Whether to render the translucent intangible player overlay.")
                    .define("isIntangibleRender", true);
            intangibleRenderColor = builder
                    .translation("config.intangible.render.intangiblerendercolor")
                    .comment("ARGB color for the intangible player overlay. Use the 0xAARRGGBB format. Default is 0x409AE9B6L.")
                    .defineInRange("intangibleRenderColor", 0x409AE9B6L, 0x00000000L, 0xFFFFFFFFL);
            builder.pop();
        }

        public int getIntangibleRenderColor() {
            return (int) (long) intangibleRenderColor.get();
        }
    }

    public static class Server {
        public final ModConfigSpec.BooleanValue isSlayTheSpire;
        public final ModConfigSpec.IntValue intangibleDurationCostOnDamage;

        Server(ModConfigSpec.Builder builder) {
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

    public static class Startup {
        public final ModConfigSpec.IntValue intangiblePotionDuration;
        public final ModConfigSpec.IntValue longIntangiblePotionDuration;
        public final ModConfigSpec.BooleanValue isIntangiblePotionRecipeEnabled;
        public final ModConfigSpec.ConfigValue<String> intangiblePotionRecipeInput;
        public final ModConfigSpec.ConfigValue<String> intangiblePotionRecipeIngredient;
        public final ModConfigSpec.BooleanValue isLongIntangiblePotionRecipeEnabled;
        public final ModConfigSpec.ConfigValue<String> longIntangiblePotionRecipeInput;
        public final ModConfigSpec.ConfigValue<String> longIntangiblePotionRecipeIngredient;

        Startup(ModConfigSpec.Builder builder) {
            builder.push("potions");

            builder.push("intangible");
            intangiblePotionDuration = builder
                    .translation("config.intangible.potions.intangible.duration")
                    .comment("Duration of the normal intangible potion effect in ticks.")
                    .defineInRange("duration", 12000, 1, Integer.MAX_VALUE);
            isIntangiblePotionRecipeEnabled = builder
                    .translation("config.intangible.potions.intangible.recipeenabled")
                    .comment("Whether the normal intangible potion brewing recipe is enabled.")
                    .define("recipeEnabled", true);
            intangiblePotionRecipeInput = builder
                    .translation("config.intangible.potions.intangible.recipeinput")
                    .comment("Potion id used as the input for the normal intangible potion brewing recipe.")
                    .define("recipeInput", "minecraft:awkward");
            intangiblePotionRecipeIngredient = builder
                    .translation("config.intangible.potions.intangible.recipeingredient")
                    .comment("Item id used as the ingredient for the normal intangible potion brewing recipe.")
                    .define("recipeIngredient", "minecraft:ender_eye");
            builder.pop();

            builder.push("long_intangible");
            longIntangiblePotionDuration = builder
                    .translation("config.intangible.potions.long_intangible.duration")
                    .comment("Duration of the long intangible potion effect in ticks.")
                    .defineInRange("duration", 24000, 1, Integer.MAX_VALUE);
            isLongIntangiblePotionRecipeEnabled = builder
                    .translation("config.intangible.potions.long_intangible.recipeenabled")
                    .comment("Whether the long intangible potion brewing recipe is enabled.")
                    .define("recipeEnabled", true);
            longIntangiblePotionRecipeInput = builder
                    .translation("config.intangible.potions.long_intangible.recipeinput")
                    .comment("Potion id used as the input for the long intangible potion brewing recipe.")
                    .define("recipeInput", "intangible:intangible");
            longIntangiblePotionRecipeIngredient = builder
                    .translation("config.intangible.potions.long_intangible.recipeingredient")
                    .comment("Item id used as the ingredient for the long intangible potion brewing recipe.")
                    .define("recipeIngredient", "minecraft:redstone");
            builder.pop();

            builder.pop();
        }
    }
}
