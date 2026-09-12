package io.github.jason13official.summons.impl.client.model.summon;

import io.github.jason13official.summons.Summons;
import io.github.jason13official.summons.impl.client.model.anim.FairySummonAnimations;
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

public class FairySummonModel extends HierarchicalModel<AbstractCompanion> {

  // This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
  public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Summons.identifier("fairy"), "main");

  private final ModelPart root;
  private final ModelPart head;
  private final ModelPart rightEar;
  private final ModelPart leftEar;
  private final ModelPart body;
  private final ModelPart rightWing;
  private final ModelPart rightWingTip;
  private final ModelPart leftWing;
  private final ModelPart leftWingTip;

  public FairySummonModel(ModelPart root) {
    this.root = root;
    this.head = root.getChild("head");
    this.rightEar = this.head.getChild("rightEar");
    this.leftEar = this.head.getChild("leftEar");
    this.body = root.getChild("body");
    this.rightWing = this.body.getChild("rightWing");
    this.rightWingTip = this.rightWing.getChild("rightWingTip");
    this.leftWing = this.body.getChild("leftWing");
    this.leftWingTip = this.leftWing.getChild("leftWingTip");
  }

  public static LayerDefinition createBodyLayer() {
    MeshDefinition meshdefinition = new MeshDefinition();
    PartDefinition partdefinition = meshdefinition.getRoot();

    PartDefinition head = partdefinition.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-3.0F, -3.0F, -3.0F, 6.0F, 6.0F, 6.0F, new CubeDeformation(0.0F)),
        PartPose.offset(0.0F, 0.0F, 0.0F));

    PartDefinition rightEar = head.addOrReplaceChild("rightEar", CubeListBuilder.create().texOffs(24, 0).addBox(-4.0F, -6.0F, -2.0F, 3.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)),
        PartPose.offset(0.0F, 0.0F, 0.0F));

    PartDefinition leftEar = head.addOrReplaceChild("leftEar", CubeListBuilder.create().texOffs(24, 0).mirror().addBox(1.0F, -6.0F, -2.0F, 3.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false),
        PartPose.offset(0.0F, 0.0F, 0.0F));

    PartDefinition body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 16).addBox(-3.0F, 4.0F, -3.0F, 6.0F, 12.0F, 6.0F, new CubeDeformation(0.0F))
        .texOffs(0, 34).addBox(-5.0F, 16.0F, 0.0F, 10.0F, 16.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

    PartDefinition rightWing = body.addOrReplaceChild("rightWing", CubeListBuilder.create().texOffs(42, 0).addBox(-12.0F, 1.0F, 1.5F, 10.0F, 16.0F, 1.0F, new CubeDeformation(0.0F)),
        PartPose.offset(0.0F, 0.0F, 0.0F));

    PartDefinition rightWingTip = rightWing.addOrReplaceChild("rightWingTip", CubeListBuilder.create().texOffs(24, 16).addBox(-8.0F, 1.0F, 0.0F, 8.0F, 12.0F, 1.0F, new CubeDeformation(0.0F)),
        PartPose.offset(-12.0F, 1.0F, 1.5F));

    PartDefinition leftWing = body.addOrReplaceChild("leftWing", CubeListBuilder.create().texOffs(42, 0).mirror().addBox(2.0F, 1.0F, 1.5F, 10.0F, 16.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false),
        PartPose.offset(0.0F, 0.0F, 0.0F));

    PartDefinition leftWingTip = leftWing.addOrReplaceChild("leftWingTip",
        CubeListBuilder.create().texOffs(24, 16).mirror().addBox(0.0F, 1.0F, 0.0F, 8.0F, 12.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(12.0F, 1.0F, 1.5F));

    return LayerDefinition.create(meshdefinition, 64, 64);
  }

  @Override
  public ModelPart root() {
    return this.root;
  }

  @Override
  public void setupAnim(AbstractCompanion entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
    this.root().getAllParts().forEach(ModelPart::resetPose);

    if (entity.isAbilityBusy()) {
      this.applyStatic(FairySummonAnimations.ABILITY);
      return;
    }

    if (entity.getAttackAnim(1.0F) > 0.0F) {
      this.applyStatic(FairySummonAnimations.ATTACK);
      return;
    }

    if (!entity.onGround()) {
      this.applyStatic(FairySummonAnimations.FLYING);

      // continuous wing flap; a static keyframe pose alone has zero motion
      float flap = Mth.sin(ageInTicks * 2.0F) * 0.5F;
      this.leftWing.zRot -= flap;
      this.rightWing.zRot += flap;
    }
  }
}