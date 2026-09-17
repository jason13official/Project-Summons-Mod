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

/// Long serpentine rig for Indigo -- elongated neck (folded into the "head" part as a second box) and tail, small
/// torso, bat-style wings, small clawed feet. Part names match [BirdSummonModel] so it shares
/// [BirdSummonAnimations] unchanged.
public class BirdSummonModelSerpent extends HierarchicalModel<AbstractCompanion> {

  public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Summons.identifier("bird_serpent"), "main");

  private final ModelPart root;
  private final ModelPart body;
  private final ModelPart wing0;
  private final ModelPart wing1;
  private final ModelPart head;
  private final ModelPart tail;
  private final ModelPart leg0;
  private final ModelPart leg1;
  private final ModelPart crystal;

  public BirdSummonModelSerpent(ModelPart root) {
    this.root = root;
    this.body = root.getChild("body");
    this.wing0 = this.body.getChild("wing0");
    this.wing1 = this.body.getChild("wing1");
    this.head = root.getChild("head");
    this.tail = root.getChild("tail");
    this.leg0 = root.getChild("leg0");
    this.leg1 = root.getChild("leg1");
    this.crystal = this.head.getChild("crystal");
  }

  public static LayerDefinition createBodyLayer() {
    MeshDefinition meshdefinition = new MeshDefinition();
    PartDefinition partdefinition = meshdefinition.getRoot();

    // head + a single long "neck" box fused into the same part -- not independently posable, but reads as an elongated neck
    PartDefinition head = partdefinition.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -2.0F, -4.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
        .texOffs(16, 0).addBox(-1.5F, -1.5F, -10.0F, 3.0F, 3.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 15.0F, -5.0F));

    PartDefinition body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 8).addBox(-2.0F, 0.0F, -2.5F, 4.0F, 6.0F, 5.0F, new CubeDeformation(0.0F)),
        PartPose.offsetAndRotation(0.0F, 16.5F, 0.0F, 0.2F, 0.0F, 0.0F));

    PartDefinition wing0 = body.addOrReplaceChild("wing0", CubeListBuilder.create().texOffs(28, 19).addBox(-1.0F, 0.0F, -4.0F, 1.0F, 14.0F, 8.0F, new CubeDeformation(0.0F)),
        PartPose.offsetAndRotation(2.0F, 0.5F, 0.0F, -0.1745F, 3.1416F, 0.0F));

    PartDefinition wing1 = body.addOrReplaceChild("wing1", CubeListBuilder.create().texOffs(28, 19).addBox(-1.0F, 0.0F, -4.0F, 1.0F, 14.0F, 8.0F, new CubeDeformation(0.0F)),
        PartPose.offsetAndRotation(-2.0F, 0.5F, 0.0F, -0.1745F, 3.1416F, 0.0F));

    // single long tapering tail box -- the serpentine counterpart to the elongated neck
    PartDefinition tail = partdefinition.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(0, 19).addBox(-1.0F, -1.0F, 0.0F, 2.0F, 2.0F, 12.0F, new CubeDeformation(0.0F)),
        PartPose.offsetAndRotation(0.0F, 19.0F, 5.5F, 0.3F, 0.0F, 0.0F));

    PartDefinition leg0 = partdefinition.addOrReplaceChild("leg0", CubeListBuilder.create().texOffs(42, 0).addBox(-0.75F, -0.5F, -0.75F, 1.5F, 3.0F, 1.5F, new CubeDeformation(0.0F)),
        PartPose.offset(2.0F, 22.0F, 1.0F));

    PartDefinition leg1 = partdefinition.addOrReplaceChild("leg1", CubeListBuilder.create().texOffs(42, 0).addBox(-0.75F, -0.5F, -0.75F, 1.5F, 3.0F, 1.5F, new CubeDeformation(0.0F)),
        PartPose.offset(-2.0F, 22.0F, 1.0F));

    // universal Innocent Devil crystal cluster -- on the head, matching Indigo's reference
    PartDefinition crystal = head.addOrReplaceChild("crystal", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, -3.0F));

    PartDefinition crystal_r1 = crystal.addOrReplaceChild("crystal_r1", CubeListBuilder.create().texOffs(48, 0).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)),
        PartPose.offsetAndRotation(-1.0F, -1.0F, 0.0F, -0.9599F, 0.5236F, -0.6109F));

    PartDefinition crystal_r2 = crystal.addOrReplaceChild("crystal_r2", CubeListBuilder.create().texOffs(56, 0).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)),
        PartPose.offsetAndRotation(1.0F, -1.0F, 0.0F, -0.9599F, 0.5236F, -0.6109F));

    PartDefinition crystal_r3 = crystal.addOrReplaceChild("crystal_r3", CubeListBuilder.create().texOffs(46, 5).addBox(-1.5F, -1.5F, -0.5F, 3.0F, 3.0F, 5.0F, new CubeDeformation(0.0F)),
        PartPose.offsetAndRotation(0.0F, -2.0F, 0.0F, -0.4363F, 0.0F, 0.0F));

    return LayerDefinition.create(meshdefinition, 72, 48);
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

    float amplitude = !entity.onGround() ? 0.9F : 0.15F;
    float rate = !entity.onGround() ? 2.0F : 0.5F;
    float delta = Mth.sin(ageInTicks * rate) * amplitude;
    this.wing0.zRot = Mth.clamp(this.wing0.zRot - delta, -1.2F, 1.2F);
    this.wing1.zRot = Mth.clamp(this.wing1.zRot + delta, -1.2F, 1.2F);
  }
}
