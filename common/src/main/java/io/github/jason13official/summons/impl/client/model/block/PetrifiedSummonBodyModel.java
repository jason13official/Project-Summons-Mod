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

/// the statue's own solid cube, rendered directly by [io.github.jason13official.summons.impl.client.renderer.block.PetrifiedSummonBlockEntityRenderer]
/// instead of a baked block model, so its scale/white-flash overlay can be driven per-frame the same way a mob renderer would (entity-format
/// ModelPart -> RenderType.entityCutout supports the overlay channel; a baked BLOCK-format model does not)
public class PetrifiedSummonBodyModel {

  public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Summons.identifier("petrified_body"), "main");

  private final ModelPart body;

  public PetrifiedSummonBodyModel(ModelPart root) {
    this.body = root.getChild("body");
  }

  public static LayerDefinition createBodyLayer() {
    MeshDefinition meshdefinition = new MeshDefinition();
    PartDefinition partdefinition = meshdefinition.getRoot();

    partdefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0)
            .addBox(-8.0F, -8.0F, -8.0F, 16.0F, 16.0F, 16.0F, new CubeDeformation(0.0F)),
        PartPose.offset(0.0F, 0.0F, 0.0F));

    return LayerDefinition.create(meshdefinition, 64, 32);
  }

  public ModelPart body() {
    return this.body;
  }
}
