package io.github.jason13official.summons.impl.client.model.summon;

import io.github.jason13official.summons.Summons;
import io.github.jason13official.summons.impl.client.model.anim.BattleSummonAnimations;
import io.github.jason13official.summons.impl.common.entity.AbstractCompanion;
import io.github.jason13official.summons.impl.common.party.CompanionMode;
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

public class BattleSummonModel extends HierarchicalModel<AbstractCompanion> {

  // This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
  public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Summons.identifier("battle"), "main");

  private final ModelPart root;
  private final ModelPart body;
  private final ModelPart head;
  private final ModelPart arm0;
  private final ModelPart arm1;
  private final ModelPart leg0;
  private final ModelPart leg1;
  private final ModelPart crystal;

  public BattleSummonModel(ModelPart root) {
    this.root = root;
    this.body = root.getChild("body");
    this.head = this.body.getChild("head");
    this.arm0 = this.body.getChild("arm0");
    this.arm1 = this.body.getChild("arm1");
    this.leg0 = this.body.getChild("leg0");
    this.leg1 = this.body.getChild("leg1");
    this.crystal = this.body.getChild("crystal");
  }

  public static LayerDefinition createBodyLayer() {
    MeshDefinition meshdefinition = new MeshDefinition();
    PartDefinition partdefinition = meshdefinition.getRoot();

    PartDefinition body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 40).addBox(-9.0F, -2.0F, -6.0F, 18.0F, 12.0F, 11.0F, new CubeDeformation(0.0F))
        .texOffs(0, 70).addBox(-4.5F, 10.0F, -3.0F, 9.0F, 5.0F, 6.0F, new CubeDeformation(0.5F)), PartPose.offset(0.0F, -7.0F, 0.0F));

    // bumped up from 8x10x8 -> better head-to-body ratio for the more humanoid evolutions, while staying inside this canvas's free UV space (leg0 starts at x=37)
    PartDefinition head = body.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.5F, -13.0F, -6.0F, 9.0F, 11.0F, 9.0F, new CubeDeformation(0.0F))
        .texOffs(24, 0).addBox(-1.0F, -5.0F, -7.5F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, -2.0F));

    PartDefinition arm0 = body.addOrReplaceChild("arm0", CubeListBuilder.create().texOffs(60, 21).addBox(-13.0F, -2.5F, -3.0F, 4.0F, 30.0F, 6.0F, new CubeDeformation(0.0F)),
        PartPose.offset(0.0F, 0.0F, 0.0F));

    PartDefinition arm1 = body.addOrReplaceChild("arm1", CubeListBuilder.create().texOffs(60, 58).addBox(9.0F, -2.5F, -3.0F, 4.0F, 30.0F, 6.0F, new CubeDeformation(0.0F)),
        PartPose.offset(0.0F, 0.0F, 0.0F));

    PartDefinition leg0 = body.addOrReplaceChild("leg0", CubeListBuilder.create().texOffs(37, 0).addBox(-3.5F, -3.0F, -3.0F, 6.0F, 16.0F, 5.0F, new CubeDeformation(0.0F)),
        PartPose.offset(-4.0F, 18.0F, 0.0F));

    PartDefinition leg1 = body.addOrReplaceChild("leg1", CubeListBuilder.create().texOffs(60, 0).mirror().addBox(-3.5F, -3.0F, -3.0F, 6.0F, 16.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false),
        PartPose.offset(5.0F, 18.0F, 0.0F));

    // universal Innocent Devil crystal cluster -> same pattern/angles as FairySummonModel's, on the chest; parked in this canvas's wide-open x[78,128) strip
    PartDefinition crystal = body.addOrReplaceChild("crystal", CubeListBuilder.create(), PartPose.offset(0.0F, 2.0F, -4.0F));

    PartDefinition crystal_r1 = crystal.addOrReplaceChild("crystal_r1", CubeListBuilder.create().texOffs(80, 0).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)),
        PartPose.offsetAndRotation(-2.5F, 0.0F, 1.0F, -0.9599F, 0.5236F, -0.6109F));

    PartDefinition crystal_r2 = crystal.addOrReplaceChild("crystal_r2", CubeListBuilder.create().texOffs(88, 0).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)),
        PartPose.offsetAndRotation(2.5F, 0.0F, 1.0F, -0.9599F, 0.5236F, -0.6109F));

    PartDefinition crystal_r3 = crystal.addOrReplaceChild("crystal_r3", CubeListBuilder.create().texOffs(80, 4).addBox(-3.0F, -4.0F, -1.0F, 6.0F, 6.0F, 8.0F, new CubeDeformation(0.0F)),
        PartPose.offsetAndRotation(0.0F, 1.0F, 1.0F, -0.4363F, 0.0F, 0.0F));

    return LayerDefinition.create(meshdefinition, 128, 128);
  }

  @Override
  public ModelPart root() {
    return this.root;
  }

  @Override
  public void setupAnim(AbstractCompanion entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
    this.root().getAllParts().forEach(ModelPart::resetPose);

    if (entity.getMode() == CompanionMode.DEFEND) {
      this.applyStatic(BattleSummonAnimations.GUARD);
      return;
    }

    if (entity.isAbilityBusy()) {
      this.applyStatic(BattleSummonAnimations.MOVE_TO_TARGET);
      return;
    }

    // IronGolem.class: legs always follow limbSwing/limbSwingAmount, not a discrete walk pose
    this.leg0.xRot = -1.5F * Mth.triangleWave(limbSwing, 13.0F) * limbSwingAmount;
    this.leg1.xRot = 1.5F * Mth.triangleWave(limbSwing, 13.0F) * limbSwingAmount;

    float swing = entity.getAttackAnim(1.0F);
    if (swing > 0.0F) {
      float amount = Mth.sin(swing * (float) Math.PI) * -1.2F; // one swing-down-and-back cycle over the hit
      this.arm0.xRot = amount;
      this.arm1.xRot = amount;
    } else {
      // the limbSwing terms alone zero out at a full stop, so add a slow idle sway that never does
      float idle = Mth.sin(ageInTicks * 0.05F) * 0.03F;
      this.arm0.xRot = (-0.2F + 1.5F * Mth.triangleWave(limbSwing, 13.0F)) * limbSwingAmount + idle;
      this.arm1.xRot = (-0.2F - 1.5F * Mth.triangleWave(limbSwing, 13.0F)) * limbSwingAmount - idle;
    }
  }
}