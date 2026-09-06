package io.github.jason13official.summons;

import io.github.jason13official.summons.impl.client.model.CompanionCubeModel;
import io.github.jason13official.summons.impl.client.model.CompanionPrismModel;
import io.github.jason13official.summons.impl.client.model.summon.BattleSummonModel;
import io.github.jason13official.summons.impl.client.renderer.CompanionCubeRenderer;
import io.github.jason13official.summons.impl.client.renderer.CompanionPrismRenderer;
import io.github.jason13official.summons.impl.client.renderer.summon.BattleSummonRenderer;
import io.github.jason13official.summons.impl.common.registry.ModEntities;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.EntityType;

public class SummonsClient {

  public static void init() {
  }

  public static void registerEntityRenderers(BiConsumer<EntityType, EntityRendererProvider> consumer) {

    consumer.accept(ModEntities.CUBE, CompanionCubeRenderer::new);
    consumer.accept(ModEntities.PRISM, CompanionPrismRenderer::new);

    consumer.accept(ModEntities.FLYING_CUBE, CompanionCubeRenderer::new);
    consumer.accept(ModEntities.FLYING_PRISM, CompanionPrismRenderer::new);

    consumer.accept(ModEntities.BATTLE, BattleSummonRenderer::new);
  }

  public static void registerEntityModels(BiConsumer<ModelLayerLocation, Supplier<LayerDefinition>> consumer) {

    consumer.accept(CompanionCubeModel.LAYER_LOCATION, CompanionCubeModel::createBodyLayer);
    consumer.accept(CompanionPrismModel.LAYER_LOCATION, CompanionPrismModel::createBodyLayer);

    consumer.accept(BattleSummonModel.LAYER_LOCATION, BattleSummonModel::createBodyLayer);
  }
}