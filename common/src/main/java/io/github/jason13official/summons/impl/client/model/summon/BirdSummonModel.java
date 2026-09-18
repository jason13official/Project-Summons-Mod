package io.github.jason13official.summons.impl.client.model.summon;

import io.github.jason13official.summons.Summons;
import io.github.jason13official.summons.impl.client.model.anim.BirdSummonAnimations;
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

public class BirdSummonModel extends HierarchicalModel<AbstractCompanion> {

  // This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
  public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Summons.identifier("bird"), "main");

  private final ModelPart root;
  private final ModelPart body;
  private final ModelPart wing0;
  private final ModelPart wing1;
  private final ModelPart head;
  private final ModelPart tail;
  private final ModelPart leg0;
  private final ModelPart leg1;
  private final ModelPart crystal;

  public BirdSummonModel(ModelPart root) {
    this.root = root;
    this.body = root.getChild("body");
    this.wing0 = this.body.getChild("wing0");
    this.wing1 = this.body.getChild("wing1");
    this.head = root.getChild("head");
    this.tail = root.getChild("tail");
    this.leg0 = root.getChild("leg0");
    this.leg1 = root.getChild("leg1");
    this.crystal = this.body.getChild("crystal");
  }

  public static LayerDefinition createBodyLayer() {
    MeshDefinition meshdefinition = new MeshDefinition();
    PartDefinition partdefinition = meshdefinition.getRoot();

    // body/wing/tail bumped up from the original tiny nubs (~1.6x) -> still recognizably a bird (Crow, Goldfinch), just no longer sparrow-scale
    PartDefinition body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(24, 0).addBox(-2.5F, 0.0F, -2.5F, 5.0F, 9.0F, 5.0F, new CubeDeformation(0.0F)),
        PartPose.offsetAndRotation(0.0F, 16.5F, -3.0F, 0.4363F, 0.0F, 0.0F));

    PartDefinition wing0 = body.addOrReplaceChild("wing0", CubeListBuilder.create().texOffs(44, 0).addBox(-1.0F, 0.0F, -2.5F, 2.0F, 9.0F, 5.0F, new CubeDeformation(0.0F)),
        PartPose.offsetAndRotation(2.5F, 0.5F, 0.3F, -0.1745F, 3.1416F, 0.0F));

    PartDefinition wing1 = body.addOrReplaceChild("wing1", CubeListBuilder.create().texOffs(44, 0).addBox(-1.0F, 0.0F, -2.5F, 2.0F, 9.0F, 5.0F, new CubeDeformation(0.0F)),
        PartPose.offsetAndRotation(-2.5F, 0.5F, 0.3F, -0.1745F, 3.1416F, 0.0F));

    // head cluster left untouched -> small/detailed already reads fine at this bucket's still-birdlike scale
    PartDefinition head = partdefinition.addOrReplaceChild("head", CubeListBuilder.create().texOffs(2, 2).addBox(-1.0F, -1.5F, -1.0F, 2.0F, 3.0F, 2.0F, new CubeDeformation(0.0F))
        .texOffs(10, 0).addBox(-1.0F, -2.5F, -3.0F, 2.0F, 1.0F, 4.0F, new CubeDeformation(0.0F))
        .texOffs(11, 7).addBox(-0.5F, -1.5F, -1.9F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
        .texOffs(16, 7).addBox(-0.5F, -1.5F, -2.9F, 1.0F, 1.7F, 1.0F, new CubeDeformation(0.0F))
        .texOffs(2, 18).addBox(0.0F, -5.8F, -2.1F, 0.0F, 5.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 15.7F, -2.8F));

    PartDefinition tail = partdefinition.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(58, 0).addBox(-2.0F, -1.5F, -1.0F, 4.0F, 6.0F, 1.0F, new CubeDeformation(0.0F)),
        PartPose.offsetAndRotation(0.0F, 22.0F, 1.2F, 0.8727F, 0.0F, 0.0F));

    PartDefinition leg0 = partdefinition.addOrReplaceChild("leg0", CubeListBuilder.create().texOffs(14, 18).addBox(-1.0F, -0.5F, -1.0F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)),
        PartPose.offset(1.5F, 25.5F, -0.5F));

    PartDefinition leg1 = partdefinition.addOrReplaceChild("leg1", CubeListBuilder.create().texOffs(14, 18).addBox(-1.0F, -0.5F, -1.0F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)),
        PartPose.offset(-0.5F, 25.5F, -0.5F));

    // universal Innocent Devil crystal cluster, scaled down to fit this bucket's small frame
    PartDefinition crystal = body.addOrReplaceChild("crystal", CubeListBuilder.create(), PartPose.offset(0.0F, 4.0F, -2.0F));

    PartDefinition crystal_r1 = crystal.addOrReplaceChild("crystal_r1", CubeListBuilder.create().texOffs(68, 0).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)),
        PartPose.offsetAndRotation(-0.8F, 0.0F, -0.5F, -0.9599F, 0.5236F, -0.6109F));

    PartDefinition crystal_r2 = crystal.addOrReplaceChild("crystal_r2", CubeListBuilder.create().texOffs(76, 0).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)),
        PartPose.offsetAndRotation(0.8F, 0.0F, -0.5F, -0.9599F, 0.5236F, -0.6109F));

    PartDefinition crystal_r3 = crystal.addOrReplaceChild("crystal_r3", CubeListBuilder.create().texOffs(68, 4).addBox(-1.5F, -1.5F, -0.5F, 3.0F, 3.0F, 5.0F, new CubeDeformation(0.0F)),
        PartPose.offsetAndRotation(0.0F, 0.8F, -0.5F, -0.4363F, 0.0F, 0.0F));

    return LayerDefinition.create(meshdefinition, 96, 32);
  }

  @Override
  public ModelPart root() {
    return this.root;
  }

  @Override
  public void setupAnim(AbstractCompanion entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
    this.root().getAllParts().forEach(ModelPart::resetPose);

    if (entity.isAbilityBusy()) {
      this.applyStatic(BirdSummonAnimations.ABILITY);
    } else if (entity.getAttackAnim(1.0F) > 0.0F) {
      this.applyStatic(BirdSummonAnimations.ATTACK);
    } else if (!entity.onGround()) {
      this.applyStatic(BirdSummonAnimations.FLYING);
    } else if (limbSwingAmount > 0.05F) {
      this.applyStatic(BirdSummonAnimations.MOVING);
      this.leg0.xRot += Mth.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;
      this.leg1.xRot += Mth.cos(limbSwing * 0.6662F + (float) Math.PI) * 1.4F * limbSwingAmount;
    } else {
      this.applyStatic(BirdSummonAnimations.STANDING);
    }

    // bounded flap while flying, subtle idle sway while grounded -> a static pose alone
    // has zero motion. Explicitly clamped so it can never rotate the wing into the body.
    float amplitude = !entity.onGround() ? 0.9F : 0.15F;
    float rate = !entity.onGround() ? 2.0F : 0.5F;
    float delta = Mth.sin(ageInTicks * rate) * amplitude;
    this.wing0.zRot = Mth.clamp(this.wing0.zRot - delta, -1.2F, 1.2F);
    this.wing1.zRot = Mth.clamp(this.wing1.zRot + delta, -1.2F, 1.2F);
  }
}