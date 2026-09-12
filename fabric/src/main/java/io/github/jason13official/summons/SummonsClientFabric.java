package io.github.jason13official.summons;

import io.github.jason13official.summons.impl.client.SummonsKeyBindings;
import io.github.jason13official.summons.impl.common.network.CompanionIdentitySyncPayload;
import io.github.jason13official.summons.impl.common.network.CompanionProgressSyncPayload;
import io.github.jason13official.summons.impl.common.network.CompanionStateSyncPayload;
import io.github.jason13official.summons.impl.common.network.SummonsNetworking;
import io.github.jason13official.summons.impl.common.registry.ModBlocks;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.EntityType;

public class SummonsClientFabric implements ClientModInitializer {

  @Override
  public void onInitializeClient() {

    SummonsClient.init();

    this.registerEntityModels(SummonsClient::registerEntityModels);
    this.registerEntityRenderers(SummonsClient::registerEntityRenderers);

    SummonsClient.registerKeyBindings(this::registerKeyBinding);
    ClientTickEvents.END_CLIENT_TICK.register(client -> SummonsKeyBindings.tickKeyBindings());

    SummonsClient.registerHUD((id, renderer) -> HudRenderCallback.EVENT.register(renderer::accept));

    ClientPlayNetworking.registerGlobalReceiver(CompanionProgressSyncPayload.TYPE, (payload, context) ->
        context.client().execute(() -> SummonsNetworking.handleProgressSync(payload)));
    ClientPlayNetworking.registerGlobalReceiver(CompanionStateSyncPayload.TYPE, (payload, context) ->
        context.client().execute(() -> SummonsNetworking.handleStateSync(payload)));
    ClientPlayNetworking.registerGlobalReceiver(CompanionIdentitySyncPayload.TYPE, (payload, context) ->
        context.client().execute(() -> SummonsNetworking.handleIdentitySync(payload)));

    BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.SUMMON_GATE, RenderType.translucent());
  }

  private void registerKeyBinding(KeyMapping mapping) {
    KeyBindingHelper.registerKeyBinding(mapping);
  }

  private void registerEntityRenderers(Consumer<BiConsumer<EntityType, EntityRendererProvider>> source) {

    source.accept(EntityRendererRegistry::register);
  }

  /// fabric prefers `TexturedModelDataProvider` so we have to translate
  /// with a method reference to fulfill the functional interface contract
  private void registerEntityModels(Consumer<BiConsumer<ModelLayerLocation, Supplier<LayerDefinition>>> source) {

    // source.accept(EntityModelLayerRegistry::registerModelLayer);
    source.accept((modelLayerLocation, layerSupplier) -> {
      EntityModelLayerRegistry.registerModelLayer(modelLayerLocation, layerSupplier::get);
    });
  }
}
