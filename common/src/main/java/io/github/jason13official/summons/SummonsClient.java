package io.github.jason13official.summons;

import io.github.jason13official.summons.impl.client.SummonsKeyBindings;
import io.github.jason13official.summons.impl.client.gui.SummonsHUD;
import io.github.jason13official.summons.impl.client.model.CompanionCubeModel;
import io.github.jason13official.summons.impl.client.model.CompanionPrismModel;
import io.github.jason13official.summons.impl.client.model.block.PetrifiedSummonBodyModel;
import io.github.jason13official.summons.impl.client.model.block.PetrifiedSummonSwirlModel;
import io.github.jason13official.summons.impl.client.model.summon.BattleSummonModel;
import io.github.jason13official.summons.impl.client.model.summon.BirdSummonModel;
import io.github.jason13official.summons.impl.client.model.summon.DevilSummonModel;
import io.github.jason13official.summons.impl.client.model.summon.FairySummonModel;
import io.github.jason13official.summons.impl.client.model.summon.MageSummonModel;
import io.github.jason13official.summons.impl.client.model.summon.PumpkinSummonModel;
import io.github.jason13official.summons.impl.client.renderer.CompanionCubeRenderer;
import io.github.jason13official.summons.impl.client.renderer.CompanionPrismRenderer;
import io.github.jason13official.summons.impl.client.renderer.block.PetrifiedSummonBlockEntityRenderer;
import io.github.jason13official.summons.impl.client.renderer.summon.BattleSummonRenderer;
import io.github.jason13official.summons.impl.client.renderer.summon.BirdSummonRenderer;
import io.github.jason13official.summons.impl.client.renderer.summon.DevilSummonRenderer;
import io.github.jason13official.summons.impl.client.renderer.summon.FairySummonRenderer;
import io.github.jason13official.summons.impl.client.renderer.summon.MageSummonRenderer;
import io.github.jason13official.summons.impl.client.renderer.summon.PumpkinSummonRenderer;
import io.github.jason13official.summons.impl.common.registry.ModEntities;
import io.github.jason13official.summons.impl.common.registry.ModTiles;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class SummonsClient {

  public static void init() {
  }

  public static void registerKeyBindings(Consumer<KeyMapping> consumer) {

    for (KeyMapping mapping : SummonsKeyBindings.all()) {
      consumer.accept(mapping);
    }
  }

  public static void registerHUD(BiConsumer<ResourceLocation, BiConsumer<GuiGraphics, DeltaTracker>> consumer) {
    consumer.accept(Summons.identifier("hud"), SummonsHUD::render);
  }

  public static void registerEntityRenderers(BiConsumer<EntityType, EntityRendererProvider> consumer) {

    consumer.accept(ModEntities.CUBE, CompanionCubeRenderer::new);
    consumer.accept(ModEntities.PRISM, CompanionPrismRenderer::new);

    consumer.accept(ModEntities.FLYING_CUBE, CompanionCubeRenderer::new);
    consumer.accept(ModEntities.FLYING_PRISM, CompanionPrismRenderer::new);

    consumer.accept(ModEntities.FAIRY, FairySummonRenderer::new);
    consumer.accept(ModEntities.BATTLE, BattleSummonRenderer::new);
    consumer.accept(ModEntities.BIRD, BirdSummonRenderer::new);
    consumer.accept(ModEntities.MAGE, MageSummonRenderer::new);
    consumer.accept(ModEntities.DEVIL, DevilSummonRenderer::new);
    consumer.accept(ModEntities.PUMPKIN, PumpkinSummonRenderer::new);
  }

  public static void registerEntityModels(BiConsumer<ModelLayerLocation, Supplier<LayerDefinition>> consumer) {

    consumer.accept(CompanionCubeModel.LAYER_LOCATION, CompanionCubeModel::createBodyLayer);
    consumer.accept(CompanionPrismModel.LAYER_LOCATION, CompanionPrismModel::createBodyLayer);

    consumer.accept(FairySummonModel.LAYER_LOCATION, FairySummonModel::createBodyLayer);
    consumer.accept(BattleSummonModel.LAYER_LOCATION, BattleSummonModel::createBodyLayer);
    consumer.accept(BirdSummonModel.LAYER_LOCATION, BirdSummonModel::createBodyLayer);
    consumer.accept(MageSummonModel.LAYER_LOCATION, MageSummonModel::createBodyLayer);
    consumer.accept(DevilSummonModel.LAYER_LOCATION, DevilSummonModel::createBodyLayer);
    consumer.accept(PumpkinSummonModel.LAYER_LOCATION, PumpkinSummonModel::createBodyLayer);

    consumer.accept(PetrifiedSummonBodyModel.LAYER_LOCATION, PetrifiedSummonBodyModel::createBodyLayer);
    consumer.accept(PetrifiedSummonSwirlModel.LAYER_LOCATION, PetrifiedSummonSwirlModel::createBodyLayer);
  }

  public static void registerBlockEntityRenderers(BiConsumer<BlockEntityType, BlockEntityRendererProvider> consumer) {

    consumer.accept(ModTiles.PETRIFIED_SUMMON, PetrifiedSummonBlockEntityRenderer::new);
  }
}