package io.github.jason13official.summons.impl.client.model.summon;

import io.github.jason13official.summons.Summons;
import io.github.jason13official.summons.impl.client.model.anim.PumpkinSummonAnimations;
import io.github.jason13official.summons.impl.common.entity.AbstractCompanion;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;

public class PumpkinSummonModel extends HierarchicalModel<AbstractCompanion> {

  // This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
  public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Summons.identifier("pumpkin"), "main");

  private final ModelPart root;
  private final ModelPart piece2;
  private final ModelPart piece1;
  private final ModelPart head;
  private final ModelPart pumpkin;
  private final ModelPart accessory;
  private final ModelPart arm1;
  private final ModelPart arm2;
  private final ModelPart crystal;

  public PumpkinSummonModel(ModelPart root) {
    this.root = root;
    this.piece2 = root.getChild("piece2");
    this.piece1 = this.piece2.getChild("piece1");
    this.head = this.piece1.getChild("head");
    this.pumpkin = this.head.getChild("pumpkin");
    this.accessory = this.head.getChild("accessory");
    this.arm1 = this.piece1.getChild("arm1");
    this.arm2 = this.piece1.getChild("arm2");
    this.crystal = this.piece1.getChild("crystal");
  }

  public static LayerDefinition createBodyLayer() {
    MeshDefinition meshdefinition = new MeshDefinition();
    PartDefinition partdefinition = meshdefinition.getRoot();

    PartDefinition piece2 = partdefinition.addOrReplaceChild("piece2", CubeListBuilder.create().texOffs(0, 36).addBox(-6.0F, -12.0F, -6.0F, 12.0F, 12.0F, 12.0F, new CubeDeformation(-0.5F)),
        PartPose.offset(0.0F, 24.0F, 0.0F));

    PartDefinition piece1 = piece2.addOrReplaceChild("piece1", CubeListBuilder.create().texOffs(0, 16).addBox(-5.0F, -10.0F, -5.0F, 10.0F, 10.0F, 10.0F, new CubeDeformation(-0.5F)),
        PartPose.offset(0.0F, -11.0F, 0.0F));

    PartDefinition head = piece1.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(-0.5F)),
        PartPose.offset(0.0F, -9.0F, 0.0F));

    PartDefinition pumpkin = head.addOrReplaceChild("pumpkin", CubeListBuilder.create().texOffs(0, 60).addBox(-8.0F, -8.0F, -8.0F, 16.0F, 16.0F, 16.0F, new CubeDeformation(-3.0F)),
        PartPose.offset(0.0F, -5.5F, 0.0F));

    // generic worn-accessory slot: sits on top of the pumpkin head; textured per form as a crown,
    // chef hat, halo, mask brim, etc. -- one blocky shape standing in for very different silhouettes
    PartDefinition accessory = head.addOrReplaceChild("accessory", CubeListBuilder.create().texOffs(64, 0).addBox(-4.0F, -4.0F, -4.0F, 8.0F, 4.0F, 8.0F, new CubeDeformation(-0.5F)),
        PartPose.offset(0.0F, -12.0F, 0.0F));

    PartDefinition arm1 = piece1.addOrReplaceChild("arm1", CubeListBuilder.create().texOffs(32, 0).addBox(1.0F, -4.0F, -1.0F, 12.0F, 2.0F, 2.0F, new CubeDeformation(-0.5F)),
        PartPose.offsetAndRotation(0.0F, -7.0F, 0.0F, 0.0F, 0.0F, 1.0001F));

    PartDefinition arm2 = piece1.addOrReplaceChild("arm2", CubeListBuilder.create().texOffs(32, 0).mirror().addBox(-13.0F, -4.0F, -1.0F, 12.0F, 2.0F, 2.0F, new CubeDeformation(-0.5F)).mirror(false),
        PartPose.offsetAndRotation(0.0F, -7.0F, 0.0F, 0.0F, 0.0F, -1.0001F));

    // universal Innocent Devil crystal cluster -- same pattern/angles as FairySummonModel's, on the chest
    PartDefinition crystal = piece1.addOrReplaceChild("crystal", CubeListBuilder.create(), PartPose.offset(0.0F, -5.0F, 0.0F));

    PartDefinition crystal_r1 = crystal.addOrReplaceChild("crystal_r1", CubeListBuilder.create().texOffs(64, 16).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)),
        PartPose.offsetAndRotation(-1.5F, -1.0F, 6.0F, -0.9599F, 0.5236F, -0.6109F));

    PartDefinition crystal_r2 = crystal.addOrReplaceChild("crystal_r2", CubeListBuilder.create().texOffs(72, 16).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)),
        PartPose.offsetAndRotation(1.5F, -1.0F, 6.0F, -0.9599F, 0.5236F, -0.6109F));

    PartDefinition crystal_r3 = crystal.addOrReplaceChild("crystal_r3", CubeListBuilder.create().texOffs(64, 20).addBox(-2.0F, -3.0F, -1.0F, 4.0F, 4.0F, 7.0F, new CubeDeformation(0.0F)),
        PartPose.offsetAndRotation(0.0F, 0.0F, 6.0F, -0.4363F, 0.0F, 0.0F));

    return LayerDefinition.create(meshdefinition, 128, 128);
  }

  @Override
  public ModelPart root() {
    return this.root;
  }

  @Override
  public void setupAnim(AbstractCompanion entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
    this.root().getAllParts().forEach(ModelPart::resetPose);

    if (entity.isAbilityBusy()) {
      this.applyStatic(PumpkinSummonAnimations.POSE);
      return;
    }

    float swing = entity.getAttackAnim(1.0F);
    if (swing > 0.0F) {
      float amount = Mth.sin(swing * (float) Math.PI) * -0.8F;
      this.arm1.xRot += amount;
      this.arm2.xRot += amount;
    } else {
      // no torso lean -> only the arms sway with limbSwing, piece1 stays put.
      // the limbSwing term alone zeroes out at a full stop, so add a slow idle sway that never does
      float idle = Mth.sin(ageInTicks * 0.05F) * 0.05F;
      float sway = Mth.triangleWave(limbSwing, 13.0F) * limbSwingAmount * 0.3F + idle;
      this.arm1.zRot += sway;
      this.arm2.zRot -= sway;
    }
  }
}