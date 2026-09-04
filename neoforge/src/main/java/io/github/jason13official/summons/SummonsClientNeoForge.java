package io.github.jason13official.summons;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.EntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

public class SummonsClientNeoForge {

  public SummonsClientNeoForge(final IEventBus modEventBus) {

    modEventBus.addListener((Consumer<FMLClientSetupEvent>) event -> SummonsClient.init());

    this.registerEntityModels(SummonsClient::registerEntityModels);
    this.registerEntityRenderers(SummonsClient::registerEntityRenderers);
  }

  private void registerEntityRenderers(Consumer<BiConsumer<EntityType, EntityRendererProvider>> source) {

    SummonsNeoForge.EVENT_BUS.addListener((EntityRenderersEvent.RegisterRenderers event) -> source.accept(event::registerEntityRenderer));
  }

  private void registerEntityModels(Consumer<BiConsumer<ModelLayerLocation, Supplier<LayerDefinition>>> source) {

    SummonsNeoForge.EVENT_BUS.addListener((EntityRenderersEvent.RegisterLayerDefinitions event) -> source.accept(event::registerLayerDefinition));
  }
}
