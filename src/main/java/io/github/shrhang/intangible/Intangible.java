package io.github.shrhang.intangible;

import com.mojang.logging.LogUtils;
import io.github.shrhang.intangible.content.IntangibleEventHandler;
import io.github.shrhang.intangible.content.IntangibleMobEffect;
import io.github.shrhang.intangible.content.IntangibleState;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.alchemy.Potion;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.brewing.RegisterBrewingRecipesEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.slf4j.Logger;

import java.util.function.Supplier;

@Mod(Intangible.MODID)
public class Intangible {
    public static final String MODID = "intangible";
    private static final Logger LOGGER = LogUtils.getLogger();

    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, Intangible.MODID);
    public static final Supplier<AttachmentType<IntangibleState>> INTANGIBLE_STATE =
            ATTACHMENT_TYPES.register("intangible_state",
                    () -> AttachmentType.builder(IntangibleState::new).build());

    public static final DeferredRegister<MobEffect> MOB_EFFECTS =
            DeferredRegister.create(BuiltInRegistries.MOB_EFFECT, Intangible.MODID);
    public static final Holder<MobEffect> INTANGIBLE =
            MOB_EFFECTS.register("intangible", IntangibleMobEffect::new);

    public static final DeferredRegister<Potion> POTIONS =
            DeferredRegister.create(BuiltInRegistries.POTION, Intangible.MODID);
    public static final Holder<Potion> INTANGIBLE_POTION =
            POTIONS.register("intangible", () -> new Potion(intangiblePotionEffect(
                    Config.STARTUP.intangiblePotionDuration.get()
            )));
    public static final Holder<Potion> LONG_INTANGIBLE_POTION =
            POTIONS.register("long_intangible", () -> new Potion("intangible", intangiblePotionEffect(
                    Config.STARTUP.longIntangiblePotionDuration.get()
            )));

    public static final TagKey<DamageType> BYPASSES_INTANGIBLE =
            TagKey.create(Registries.DAMAGE_TYPE, rl("bypasses_intangible"));
    public static final TagKey<DamageType> INTANGIBLE_IMMUNE_TO =
            TagKey.create(Registries.DAMAGE_TYPE, rl("intangible_immune_to"));

    public Intangible(IEventBus modEventBus, ModContainer modContainer) {
        Config.register(modContainer);
        if (FMLEnvironment.dist.isClient()) ;
        modEventBus.addListener(this::commonSetup);
        ATTACHMENT_TYPES.register(modEventBus);
        MOB_EFFECTS.register(modEventBus);
        POTIONS.register(modEventBus);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        IntangibleEventHandler.init();
        NeoForge.EVENT_BUS.addListener(this::registerBrewingRecipes);
    }

    private void registerBrewingRecipes(final RegisterBrewingRecipesEvent event) {
        addConfiguredBrewingMix(
                event,
                Config.STARTUP.isIntangiblePotionRecipeEnabled.get(),
                Config.STARTUP.intangiblePotionRecipeInput.get(),
                Config.STARTUP.intangiblePotionRecipeIngredient.get(),
                INTANGIBLE_POTION
        );
        addConfiguredBrewingMix(
                event,
                Config.STARTUP.isLongIntangiblePotionRecipeEnabled.get(),
                Config.STARTUP.longIntangiblePotionRecipeInput.get(),
                Config.STARTUP.longIntangiblePotionRecipeIngredient.get(),
                LONG_INTANGIBLE_POTION
        );
    }

    private static MobEffectInstance intangiblePotionEffect(int duration) {
        return new MobEffectInstance(INTANGIBLE, duration);
    }

    private static void addConfiguredBrewingMix(RegisterBrewingRecipesEvent event, boolean enabled, String inputPotionId, String ingredientId, Holder<Potion> result) {
        if (!enabled) return;

        ResourceLocation inputPotionLocation = ResourceLocation.tryParse(inputPotionId);
        if (inputPotionLocation == null) {
            LOGGER.warn("Skipping intangible potion recipe with invalid input potion id '{}'", inputPotionId);
            return;
        }

        Holder<Potion> input = BuiltInRegistries.POTION.getHolder(inputPotionLocation).orElse(null);
        if (input == null) {
            LOGGER.warn("Skipping intangible potion recipe with unknown input potion '{}'", inputPotionId);
            return;
        }

        ResourceLocation ingredientLocation = ResourceLocation.tryParse(ingredientId);
        if (ingredientLocation == null) {
            LOGGER.warn("Skipping intangible potion recipe with invalid ingredient id '{}'", ingredientId);
            return;
        }

        Item ingredient = BuiltInRegistries.ITEM.getOptional(ingredientLocation).orElse(null);
        if (ingredient == null) {
            LOGGER.warn("Skipping intangible potion recipe with unknown ingredient item '{}'", ingredientId);
            return;
        }

        event.getBuilder().addMix(input, ingredient, result);
    }

    public static ResourceLocation rl(String id) {
        return ResourceLocation.fromNamespaceAndPath(MODID, id);
    }
}
