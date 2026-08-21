package io.github.shrhang.intangible;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.WritingMode;
import com.electronwill.nightconfig.toml.TomlFormat;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.registries.ForgeRegistries;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class StartupConfig {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String FILE_NAME = Intangible.MODID + "-startup.config";

    private static final String INTANGIBLE_DURATION = "potions.intangible.duration";
    private static final String INTANGIBLE_RECIPE_ENABLED = "potions.intangible.recipeEnabled";
    private static final String INTANGIBLE_RECIPE_INPUT = "potions.intangible.recipeInput";
    private static final String INTANGIBLE_RECIPE_INGREDIENT = "potions.intangible.recipeIngredient";
    private static final String LONG_INTANGIBLE_DURATION = "potions.long_intangible.duration";
    private static final String LONG_INTANGIBLE_RECIPE_ENABLED = "potions.long_intangible.recipeEnabled";
    private static final String LONG_INTANGIBLE_RECIPE_INPUT = "potions.long_intangible.recipeInput";
    private static final String LONG_INTANGIBLE_RECIPE_INGREDIENT = "potions.long_intangible.recipeIngredient";

    private static PotionConfig intangiblePotion = new PotionConfig(12000, true, "minecraft:awkward", "minecraft:ender_eye");
    private static PotionConfig longIntangiblePotion = new PotionConfig(24000, true, "intangible:intangible", "minecraft:redstone");

    private StartupConfig() {
    }

    public static void load() {
        Path path = FMLPaths.CONFIGDIR.get().resolve(FILE_NAME);
        ensureFileExists(path);

        try (CommentedFileConfig config = CommentedFileConfig.builder(path, TomlFormat.instance())
                .sync()
                .writingMode(WritingMode.REPLACE)
                .build()) {
            config.load();
            load(config);
        } catch (RuntimeException exception) {
            LOGGER.warn("Failed to load {}, using default brewing recipes.", path, exception);
        }
    }

    private static void load(CommentedFileConfig config) {
        intangiblePotion = new PotionConfig(
                getDuration(config, INTANGIBLE_DURATION, 12000),
                config.getOrElse(INTANGIBLE_RECIPE_ENABLED, true),
                config.getOrElse(INTANGIBLE_RECIPE_INPUT, "minecraft:awkward"),
                config.getOrElse(INTANGIBLE_RECIPE_INGREDIENT, "minecraft:ender_eye")
        );
        longIntangiblePotion = new PotionConfig(
                getDuration(config, LONG_INTANGIBLE_DURATION, 24000),
                config.getOrElse(LONG_INTANGIBLE_RECIPE_ENABLED, true),
                config.getOrElse(LONG_INTANGIBLE_RECIPE_INPUT, "intangible:intangible"),
                config.getOrElse(LONG_INTANGIBLE_RECIPE_INGREDIENT, "minecraft:redstone")
        );
    }

    public static int intangiblePotionDuration() {
        return intangiblePotion.duration();
    }

    public static int longIntangiblePotionDuration() {
        return longIntangiblePotion.duration();
    }

    public static Recipe intangibleRecipe() {
        return resolve(intangiblePotion, Potions.AWKWARD, Items.ENDER_EYE);
    }

    public static Recipe longIntangibleRecipe(Potion defaultInput) {
        return resolve(longIntangiblePotion, defaultInput, Items.REDSTONE);
    }

    private static void ensureFileExists(Path path) {
        if (Files.exists(path)) {
            return;
        }

        try {
            Files.createDirectories(path.getParent());
            Files.writeString(path, defaultContent());
        } catch (IOException exception) {
            LOGGER.warn("Failed to create default brewing config {}.", path, exception);
        }
    }

    private static String defaultContent() {
        return """
                # Intangible brewing recipe config.
                # Potion and item values are registry ids, for example minecraft:awkward or minecraft:ender_eye.
                # This file is loaded before the mod adds brewing recipes. Keep client and server values consistent.

                [potions.intangible]
                # Duration of the normal intangible potion effect in ticks.
                # Default: 12000
                # Range: > 1
                duration = 12000
                # Whether the normal intangible potion brewing recipe is enabled.
                recipeEnabled = true
                # Potion id used as the input for the normal intangible potion brewing recipe.
                recipeInput = "minecraft:awkward"
                # Item id used as the ingredient for the normal intangible potion brewing recipe.
                recipeIngredient = "minecraft:ender_eye"

                [potions.long_intangible]
                # Duration of the long intangible potion effect in ticks.
                # Default: 24000
                # Range: > 1
                duration = 24000
                # Whether the long intangible potion brewing recipe is enabled.
                recipeEnabled = true
                # Potion id used as the input for the long intangible potion brewing recipe.
                recipeInput = "intangible:intangible"
                # Item id used as the ingredient for the long intangible potion brewing recipe.
                recipeIngredient = "minecraft:redstone"
                """;
    }

    private static int getDuration(CommentedFileConfig config, String key, int defaultValue) {
        int duration = config.getOrElse(key, defaultValue);
        if (duration <= 1) {
            LOGGER.warn("Invalid duration '{}' in {}, using default '{}'.", duration, FILE_NAME, defaultValue);
            return defaultValue;
        }
        return duration;
    }

    private static Recipe resolve(PotionConfig potion, Potion defaultInput, Item defaultIngredient) {
        return new Recipe(
                potion.recipeEnabled(),
                getPotion(potion.recipeInputId(), defaultInput),
                getItem(potion.recipeIngredientId(), defaultIngredient)
        );
    }

    private static Potion getPotion(String value, Potion defaultValue) {
        ResourceLocation id = ResourceLocation.tryParse(value);
        if (id == null || !ForgeRegistries.POTIONS.containsKey(id)) {
            LOGGER.warn("Unknown potion id '{}' in {}, using default '{}'.", value, FILE_NAME,
                    ForgeRegistries.POTIONS.getKey(defaultValue));
            return defaultValue;
        }
        return ForgeRegistries.POTIONS.getValue(id);
    }

    private static Item getItem(String value, Item defaultValue) {
        ResourceLocation id = ResourceLocation.tryParse(value);
        if (id == null || !ForgeRegistries.ITEMS.containsKey(id)) {
            LOGGER.warn("Unknown item id '{}' in {}, using default '{}'.", value, FILE_NAME,
                    ForgeRegistries.ITEMS.getKey(defaultValue));
            return defaultValue;
        }
        return ForgeRegistries.ITEMS.getValue(id);
    }

    private record PotionConfig(int duration, boolean recipeEnabled, String recipeInputId, String recipeIngredientId) {
    }

    public record Recipe(boolean enabled, Potion input, Item ingredient) {
    }
}
