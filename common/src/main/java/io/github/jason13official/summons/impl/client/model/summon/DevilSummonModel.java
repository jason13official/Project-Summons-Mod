package io.github.jason13official.summons.impl.client.model.summon;

import io.github.jason13official.summons.Summons;
import io.github.jason13official.summons.impl.client.model.anim.DevilSummonAnimations;
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

public class DevilSummonModel extends HierarchicalModel<AbstractCompanion> {

  // This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
  public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Summons.identifier("devil"), "main");

  private final ModelPart root;
  private final ModelPart body;
  private final ModelPart head;
  private final ModelPart hat;
  private final ModelPart rightArm;
  private final ModelPart rightItem;
  private final ModelPart leftArm;
  private final ModelPart rightLeg;
  private final ModelPart leftLeg;

  public DevilSummonModel(ModelPart root) {
    this.root = root;
    this.body = root.getChild("body");
    this.head = this.body.getChild("head");
    this.hat = this.head.getChild("hat");
    this.rightArm = this.body.getChild("rightArm");
    this.rightItem = this.rightArm.getChild("rightItem");
    this.leftArm = this.body.getChild("leftArm");
    this.rightLeg = this.body.getChild("rightLeg");
    this.leftLeg = this.body.getChild("leftLeg");
  }

  public static LayerDefinition createBodyLayer() {
    MeshDefinition meshdefinition = new MeshDefinition();
    PartDefinition partdefinition = meshdefinition.getRoot();

    PartDefinition body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(32, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)),
        PartPose.offset(0.0F, -18.0F, 0.0F));

    PartDefinition head = body.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F)),
        PartPose.offset(0.0F, 0.0F, 0.0F));

    PartDefinition hat = head.addOrReplaceChild("hat", CubeListBuilder.create().texOffs(0, 16).addBox(-4.0F, -7.5F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(-0.5F)),
        PartPose.offset(0.0F, 0.0F, 0.0F));

    PartDefinition rightArm = body.addOrReplaceChild("rightArm", CubeListBuilder.create().texOffs(56, 0).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 30.0F, 2.0F, new CubeDeformation(0.0F)),
        PartPose.offset(-5.0F, 2.0F, 0.0F));

    PartDefinition rightItem = rightArm.addOrReplaceChild("rightItem", CubeListBuilder.create(), PartPose.offset(-3.0F, 21.0F, 1.0F));

    PartDefinition leftArm = body.addOrReplaceChild("leftArm", CubeListBuilder.create().texOffs(56, 0).mirror().addBox(-1.0F, -2.0F, -1.0F, 2.0F, 30.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false),
        PartPose.offset(5.0F, 2.0F, 0.0F));

    PartDefinition rightLeg = body.addOrReplaceChild("rightLeg", CubeListBuilder.create().texOffs(56, 0).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 30.0F, 2.0F, new CubeDeformation(0.0F)),
        PartPose.offset(-2.0F, 12.0F, 0.0F));

    PartDefinition leftLeg = body.addOrReplaceChild("leftLeg", CubeListBuilder.create().texOffs(56, 0).mirror().addBox(-1.0F, 0.0F, -1.0F, 2.0F, 30.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false),
        PartPose.offset(2.0F, 12.0F, 0.0F));

    return LayerDefinition.create(meshdefinition, 64, 32);
  }

  @Override
  public ModelPart root() {
    return this.root;
  }

  @Override
  public void setupAnim(AbstractCompanion entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
    this.root().getAllParts().forEach(ModelPart::resetPose);

    if (entity.getMode() == CompanionMode.DEFEND) {
      this.applyStatic(DevilSummonAnimations.GUARD);
      return;
    }

    if (entity.isAbilityBusy()) {
      this.applyStatic(DevilSummonAnimations.ABILITY);
      return;
    }

    // continuous limbSwingAmount, not onGround() (flickers while landing); scaled way
    // down while airborne since flight speed alone still drives limbSwingAmount
    float legScale = entity.onGround() ? 1.0F : 0.15F;
    this.leftLeg.xRot = Mth.triangleWave(limbSwing, 13.0F) * limbSwingAmount * legScale;
    this.rightLeg.xRot = -Mth.triangleWave(limbSwing, 13.0F) * limbSwingAmount * legScale;

    float swing = entity.getAttackAnim(1.0F);
    if (swing > 0.0F) {
      float amount = Mth.sin(swing * (float) Math.PI) * -1.2F;
      this.leftArm.xRot = amount;
      this.rightArm.xRot = amount;
    } else {
      this.applyStatic(DevilSummonAnimations.BOB); // subtle idle/hover arm sway
    }
  }
}