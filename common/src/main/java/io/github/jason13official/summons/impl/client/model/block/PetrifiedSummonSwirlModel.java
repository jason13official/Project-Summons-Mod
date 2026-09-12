package io.github.jason13official.summons.impl.client.model.block;

import io.github.jason13official.summons.Summons;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/// a single inflated cube used as the energy-swirl aura shell around an awakening petrified statue. RenderType.energySwirl needs NEW_ENTITY-format
/// vertices, which a baked block model can't supply -> a small dedicated ModelPart instead, mirroring vanilla's CreeperModel/CreeperPowerLayer
public class PetrifiedSummonSwirlModel {

  public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Summons.identifier("petrified_swirl"), "main");

  private final ModelPart shell;

  public PetrifiedSummonSwirlModel(ModelPart root) {
    this.shell = root.getChild("shell");
  }

  public static LayerDefinition createBodyLayer() {
    MeshDefinition meshdefinition = new MeshDefinition();
    PartDefinition partdefinition = meshdefinition.getRoot();

    // full block bounds (16px cube), inflated so the swirl reads as an aura rather than z-fighting the base cube
    partdefinition.addOrReplaceChild("shell", CubeListBuilder.create().texOffs(0, 0)
            .addBox(-8.0F, -8.0F, -8.0F, 16.0F, 16.0F, 16.0F, new CubeDeformation(0.4F)),
        PartPose.offset(0.0F, 0.0F, 0.0F));

    return LayerDefinition.create(meshdefinition, 32, 32);
  }

  public ModelPart shell() {
    return this.shell;
  }
}
