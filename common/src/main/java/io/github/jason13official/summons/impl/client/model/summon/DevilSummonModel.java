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
  private final ModelPart crystal;

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
    this.crystal = this.body.getChild("crystal");
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

    // held trident (Devil's evolution art shows one across all three forms). -1.25 rad on X swings the shaft's reach from "hanging down" toward "held forward", same trick as the Mage Rod/Battle
    // armored weapon (rotating on Z instead pokes it through the arm edge-on). rightArm's own box spans local y -2..28 (30 units long) -> the wrist/hand is near the *bottom* of that (y~27), not
    // the top; a first pass anchored this near the shoulder instead, which floated it up near the chest rather than looking held. A real three-prong head (crossbar + center/left/right tines,
    // center prong longest) replaces the single ornament cube a first pass used here -> a plain cube read as a hammer/mace head, not a trident.
    PartDefinition rightItem = rightArm.addOrReplaceChild("rightItem", CubeListBuilder.create()
        .texOffs(0, 32).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 13.0F, 1.0F, new CubeDeformation(0.0F))
        .texOffs(4, 32).addBox(-2.5F, 13.0F, -0.5F, 5.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
        .texOffs(4, 34).addBox(-2.2F, 13.0F, -0.3F, 0.6F, 4.0F, 0.6F, new CubeDeformation(0.0F))
        .texOffs(8, 34).addBox(-0.3F, 13.0F, -0.3F, 0.6F, 5.0F, 0.6F, new CubeDeformation(0.0F))
        .texOffs(12, 34).addBox(1.6F, 13.0F, -0.3F, 0.6F, 4.0F, 0.6F, new CubeDeformation(0.0F)),
        PartPose.offsetAndRotation(0.0F, 27.0F, 0.0F, -1.25F, 0.0F, 0.0F));

    PartDefinition leftArm = body.addOrReplaceChild("leftArm", CubeListBuilder.create().texOffs(56, 0).mirror().addBox(-1.0F, -2.0F, -1.0F, 2.0F, 30.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false),
        PartPose.offset(5.0F, 2.0F, 0.0F));

    PartDefinition rightLeg = body.addOrReplaceChild("rightLeg", CubeListBuilder.create().texOffs(56, 0).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 30.0F, 2.0F, new CubeDeformation(0.0F)),
        PartPose.offset(-2.0F, 12.0F, 0.0F));

    PartDefinition leftLeg = body.addOrReplaceChild("leftLeg", CubeListBuilder.create().texOffs(56, 0).mirror().addBox(-1.0F, 0.0F, -1.0F, 2.0F, 30.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false),
        PartPose.offset(2.0F, 12.0F, 0.0F));

    // universal Innocent Devil crystal cluster -> same pattern/angles as every other type's; Devil's own chest box is
    // shallower (depth 4 vs Battle's 11) so the Z offset is scaled down to match, same "positive Z is this rig's back" gotcha from the other five
    PartDefinition crystal = body.addOrReplaceChild("crystal", CubeListBuilder.create(), PartPose.offset(0.0F, 2.0F, -1.0F));

    PartDefinition crystal_r1 = crystal.addOrReplaceChild("crystal_r1", CubeListBuilder.create().texOffs(32, 32).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)),
        PartPose.offsetAndRotation(-1.5F, 0.0F, 1.0F, -0.9599F, 0.5236F, -0.6109F));

    PartDefinition crystal_r2 = crystal.addOrReplaceChild("crystal_r2", CubeListBuilder.create().texOffs(40, 32).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)),
        PartPose.offsetAndRotation(1.5F, 0.0F, 1.0F, -0.9599F, 0.5236F, -0.6109F));

    PartDefinition crystal_r3 = crystal.addOrReplaceChild("crystal_r3", CubeListBuilder.create().texOffs(32, 36).addBox(-2.0F, -3.0F, -0.5F, 4.0F, 4.0F, 7.0F, new CubeDeformation(0.0F)),
        PartPose.offsetAndRotation(0.0F, 1.0F, 1.0F, -0.4363F, 0.0F, 0.0F));

    return LayerDefinition.create(meshdefinition, 64, 48);
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
      // BOB's own keyframe is a fixed tilt (applyStatic alone has zero motion); add the actual bob over time
      this.applyStatic(DevilSummonAnimations.BOB);
      float bob = Mth.sin(ageInTicks * 0.08F) * 0.05F;
      this.leftArm.zRot += bob;
      this.rightArm.zRot -= bob;
    }
  }
}