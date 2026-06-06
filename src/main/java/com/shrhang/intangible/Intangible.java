package com.shrhang.intangible;

import com.shrhang.intangible.content.IntangibleMobEffect;
import com.shrhang.intangible.content.IntangibleState;
import com.shrhang.intangible.content.IntangibleRender;
import com.shrhang.intangible.content.IntangibleEventHandler;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.Potions;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.brewing.RegisterBrewingRecipesEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

@Mod(Intangible.MODID)
public class Intangible {
    public static final String MODID = "intangible";

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
            POTIONS.register("intangible", () -> new Potion(new MobEffectInstance(INTANGIBLE, 12000, 8)));
    public static final Holder<Potion> LONG_INTANGIBLE_POTION =
            POTIONS.register("long_intangible", () -> new Potion("intangible", new MobEffectInstance(INTANGIBLE, 24000, 6)));
    public static final Holder<Potion> STRONG_INTANGIBLE_POTION =
            POTIONS.register("strong_intangible", () -> new Potion("intangible", new MobEffectInstance(INTANGIBLE, 6000, 14)));

    public static final TagKey<DamageType> BYPASSES_INTANGIBLE =
            TagKey.create(Registries.DAMAGE_TYPE, rl("bypasses_intangible"));

    public Intangible(IEventBus modEventBus, ModContainer modContainer) {
        Config.register(modContainer);
        if (FMLEnvironment.dist.isClient()) modEventBus.addListener(IntangibleRender::registerLayers);
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
        event.getBuilder().addMix(Potions.AWKWARD, Items.ECHO_SHARD, INTANGIBLE_POTION);
        event.getBuilder().addMix(INTANGIBLE_POTION, Items.REDSTONE, LONG_INTANGIBLE_POTION);
        event.getBuilder().addMix(INTANGIBLE_POTION, Items.GLOWSTONE_DUST, STRONG_INTANGIBLE_POTION);
    }

    public static ResourceLocation rl(String id) {
        return ResourceLocation.fromNamespaceAndPath(MODID, id);
    }
}
