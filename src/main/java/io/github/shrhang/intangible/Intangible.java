package io.github.shrhang.intangible;

import io.github.shrhang.intangible.content.IntangibleEventHandler;
import io.github.shrhang.intangible.content.IntangibleMobEffect;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.brewing.BrewingRecipeRegistry;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@Mod(Intangible.MODID)
public class Intangible {
    public static final String MODID = "intangible";

    public static final DeferredRegister<MobEffect> MOB_EFFECTS =
            DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, MODID);
    public static final RegistryObject<MobEffect> INTANGIBLE =
            MOB_EFFECTS.register("intangible", IntangibleMobEffect::new);

    public static final DeferredRegister<Potion> POTIONS =
            DeferredRegister.create(ForgeRegistries.POTIONS, MODID);
    public static final RegistryObject<Potion> INTANGIBLE_POTION =
            POTIONS.register("intangible", () -> new Potion(intangiblePotionEffect(
                    StartupConfig.intangiblePotionDuration()
            )));
    public static final RegistryObject<Potion> LONG_INTANGIBLE_POTION =
            POTIONS.register("long_intangible", () -> new Potion("intangible", intangiblePotionEffect(
                    StartupConfig.longIntangiblePotionDuration()
            )));

    public static final TagKey<DamageType> BYPASSES_INTANGIBLE =
            TagKey.create(Registries.DAMAGE_TYPE, rl("bypasses_intangible"));
    public static final TagKey<DamageType> INTANGIBLE_IMMUNE_TO =
            TagKey.create(Registries.DAMAGE_TYPE, rl("intangible_immune_to"));

    public Intangible(FMLJavaModLoadingContext context) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                io.github.shrhang.intangible.content.client.IntangibleClient.init(context));

        IEventBus modEventBus = context.getModEventBus();

        StartupConfig.load();
        Config.register(context);
        modEventBus.addListener(this::commonSetup);
        MOB_EFFECTS.register(modEventBus);
        POTIONS.register(modEventBus);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        IntangibleEventHandler.init();
        event.enqueueWork(this::registerBrewingRecipes);
    }

    private void registerBrewingRecipes() {
        StartupConfig.Recipe intangibleRecipe = StartupConfig.intangibleRecipe();
        if (intangibleRecipe.enabled()) {
            addBrewingMix(intangibleRecipe.input(), intangibleRecipe.ingredient(), INTANGIBLE_POTION.get());
        }

        StartupConfig.Recipe longIntangibleRecipe = StartupConfig.longIntangibleRecipe(INTANGIBLE_POTION.get());
        if (longIntangibleRecipe.enabled()) {
            addBrewingMix(longIntangibleRecipe.input(), longIntangibleRecipe.ingredient(), LONG_INTANGIBLE_POTION.get());
        }
    }

    private static MobEffectInstance intangiblePotionEffect(int duration) {
        return new MobEffectInstance(INTANGIBLE.get(), duration);
    }

    private static void addBrewingMix(Potion input, Item ingredient, Potion result) {
        addBrewingRecipe(Items.POTION.getDefaultInstance(), input, ingredient, result);
        addBrewingRecipe(Items.SPLASH_POTION.getDefaultInstance(), input, ingredient, result);
        addBrewingRecipe(Items.LINGERING_POTION.getDefaultInstance(), input, ingredient, result);
    }

    private static void addBrewingRecipe(ItemStack bottle, Potion input, Item ingredient, Potion result) {
        ItemStack inputStack = PotionUtils.setPotion(bottle.copy(), input);
        ItemStack outputStack = PotionUtils.setPotion(bottle.copy(), result);
        BrewingRecipeRegistry.addRecipe(
                Ingredient.of(inputStack),
                Ingredient.of(ingredient),
                outputStack
        );
    }

    public static ResourceLocation rl(String id) {
        return ResourceLocation.fromNamespaceAndPath(MODID, id);
    }
}
