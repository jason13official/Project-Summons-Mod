package io.github.jason13official.summons;

import io.github.jason13official.summons.impl.client.SummonsKeyBindings;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.EntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.common.NeoForge;

public class SummonsClientNeoForge {

  public SummonsClientNeoForge(final IEventBus modEventBus) {

    modEventBus.addListener((Consumer<FMLClientSetupEvent>) event -> SummonsClient.init());

    this.registerEntityModels(SummonsClient::registerEntityModels);
    this.registerEntityRenderers(SummonsClient::registerEntityRenderers);

    modEventBus.addListener((RegisterKeyMappingsEvent event) -> SummonsClient.registerKeyBindings(event::register));
    NeoForge.EVENT_BUS.addListener((ClientTickEvent.Post event) -> SummonsKeyBindings.tickKeyBindings());

    SummonsClient.registerHUD((id, renderer) ->
        modEventBus.addListener((RegisterGuiLayersEvent event) -> event.registerAboveAll(id, renderer::accept)));
  }

  private void registerEntityRenderers(Consumer<BiConsumer<EntityType, EntityRendererProvider>> source) {

    SummonsNeoForge.EVENT_BUS.addListener((EntityRenderersEvent.RegisterRenderers event) -> source.accept(event::registerEntityRenderer));
  }

  private void registerEntityModels(Consumer<BiConsumer<ModelLayerLocation, Supplier<LayerDefinition>>> source) {

    SummonsNeoForge.EVENT_BUS.addListener((EntityRenderersEvent.RegisterLayerDefinitions event) -> source.accept(event::registerLayerDefinition));
  }
}
