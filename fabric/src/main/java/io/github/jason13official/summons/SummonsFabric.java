package io.github.jason13official.summons;

import io.github.jason13official.summons.impl.common.network.SummonsNetworking;
import io.github.jason13official.summons.impl.common.network.SummonsNetworking.CompanionInputPayload;
import io.github.jason13official.summons.impl.common.registry.ModBlocks;
import io.github.jason13official.summons.impl.common.registry.ModEntities;
import io.github.jason13official.summons.impl.common.registry.ModItems;
import io.github.jason13official.summons.impl.common.registry.ModMenus;
import io.github.jason13official.summons.impl.common.registry.ModParticles;
import io.github.jason13official.summons.impl.common.registry.ModTabs;
import io.github.jason13official.summons.impl.common.registry.ModTiles;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;

public class SummonsFabric implements ModInitializer {

  @Override
  public void onInitialize() {

    bind(BuiltInRegistries.BLOCK, ModBlocks::register);
    bind(BuiltInRegistries.ENTITY_TYPE, ModEntities::register);
    bind(BuiltInRegistries.ITEM, ModItems::register);
    bind(BuiltInRegistries.PARTICLE_TYPE, ModParticles::register);
    bind(BuiltInRegistries.BLOCK_ENTITY_TYPE, ModTiles::register);
    bind(BuiltInRegistries.MENU, ModMenus::register);
    bind(BuiltInRegistries.CREATIVE_MODE_TAB, ModTabs::register);

    Summons.init();

    // FabricDefaultAttributeRegistry.register(ModEntities.CUBE, Mob.createMobAttributes());
    this.createDefaultAttributes(Summons::createDefaultAttributes);

    ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(new ResourceReloadListener());

    PayloadTypeRegistry.playC2S().register(CompanionInputPayload.TYPE, CompanionInputPayload.STREAM_CODEC);
    ServerPlayNetworking.registerGlobalReceiver(CompanionInputPayload.TYPE, (payload, context) ->
        context.server().execute(() -> SummonsNetworking.handle(context.player(), payload.action())));
  }

  public void createDefaultAttributes(Consumer<BiConsumer<EntityType, AttributeSupplier>> source) {

    source.accept(FabricDefaultAttributeRegistry::register);
  }

  public <T> void bind(Registry<T> registry, Consumer<BiConsumer<T, ResourceLocation>> source) {

    source.accept((t, rl) -> Registry.register(registry, rl, t));
  }

  public static class ResourceReloadListener implements SimpleSynchronousResourceReloadListener {

    @Override
    public ResourceLocation getFabricId() {
      return Summons.identifier(Constants.MOD_ID);
    }

    @Override
    public void onResourceManagerReload(ResourceManager resourceManager) {
      // ModConfig.load(Services.PLATFORM.getConfigDirectory());
    }
  }
}
