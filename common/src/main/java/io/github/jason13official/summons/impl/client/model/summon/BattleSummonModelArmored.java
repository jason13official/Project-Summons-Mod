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

/// Waist-articulated rig for the armored/humanoid Battle-Type evolutions (Rasetz, Speed Mail, Ironside, Corpsey).
/// Second pass: the first pass matched the chunky rig's bulk but still read as a flat gray tower in-game --
/// same-color torso/arms with no gap between them fused into one silhouette, the crystal was buried inside the
/// chest's own solid geometry instead of poking out, and the weapon barely swung clear of the leg. This pass adds
/// horns and shoulder pauldrons (the two biggest silhouette breaks in every armored reference), widens the arm/
/// torso gap, and pushes the crystal and weapon swing out far enough to actually clear the body. Part names
/// (body/head/arm0/arm1/leg0/leg1) still match [BattleSummonModel] so both rigs share [BattleSummonAnimations].
public class BattleSummonModelArmored extends HierarchicalModel<AbstractCompanion> {

  public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Summons.identifier("battle_armored"), "main");

  private final ModelPart root;
  private final ModelPart body;
  private final ModelPart head;
  private final ModelPart arm0;
  private final ModelPart weapon;
  private final ModelPart arm1;
  private final ModelPart leg0;
  private final ModelPart leg1;
  private final ModelPart crystal;

  public BattleSummonModelArmored(ModelPart root) {
    this.root = root;
    this.body = root.getChild("body");
    this.head = this.body.getChild("head");
    this.arm0 = this.body.getChild("arm0");
    this.weapon = this.arm0.getChild("weapon");
    this.arm1 = this.body.getChild("arm1");
    this.leg0 = this.body.getChild("leg0");
    this.leg1 = this.body.getChild("leg1");
    this.crystal = this.body.getChild("crystal");
  }

  public static LayerDefinition createBodyLayer() {
    MeshDefinition meshdefinition = new MeshDefinition();
    PartDefinition partdefinition = meshdefinition.getRoot();

    // narrower than the first pass (10 vs 12 wide) so there's a visible gap to the arms once those sit further out
    PartDefinition body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-5.0F, -10.0F, -3.5F, 10.0F, 10.0F, 7.0F, new CubeDeformation(0.0F))
        .texOffs(0, 17).addBox(-4.0F, 0.0F, -3.0F, 8.0F, 7.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -7.0F, 0.0F));

    PartDefinition head = body.addOrReplaceChild("head", CubeListBuilder.create().texOffs(34, 0).addBox(-4.5F, -10.0F, -4.5F, 9.0F, 10.0F, 9.0F, new CubeDeformation(0.0F)),
        PartPose.offset(0.0F, -10.0F, 0.0F));

    // horns -> the single biggest silhouette read in every armored reference (curved horns, helmet spikes, ...)
    PartDefinition hornR = head.addOrReplaceChild("hornR", CubeListBuilder.create().texOffs(70, 0).addBox(-0.5F, -4.0F, -0.5F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)),
        PartPose.offsetAndRotation(2.5F, -9.5F, -1.0F, 0.0F, 0.0F, -0.45F));

    PartDefinition hornL = head.addOrReplaceChild("hornL", CubeListBuilder.create().texOffs(70, 0).addBox(-0.5F, -4.0F, -0.5F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)),
        PartPose.offsetAndRotation(-2.5F, -9.5F, -1.0F, 0.0F, 0.0F, 0.45F));

    // pauldrons -> flare the shoulders out past the chest so arms don't visually fuse into one flat tower
    PartDefinition pauldronR = body.addOrReplaceChild("pauldronR", CubeListBuilder.create().texOffs(70, 4).addBox(-2.0F, -1.5F, -2.5F, 4.0F, 3.0F, 5.0F, new CubeDeformation(0.3F)),
        PartPose.offset(8.0F, -9.0F, 0.0F));

    PartDefinition pauldronL = body.addOrReplaceChild("pauldronL", CubeListBuilder.create().texOffs(70, 4).mirror().addBox(-2.0F, -1.5F, -2.5F, 4.0F, 3.0F, 5.0F, new CubeDeformation(0.3F)).mirror(false),
        PartPose.offset(-8.0F, -9.0F, 0.0F));

    PartDefinition arm0 = body.addOrReplaceChild("arm0", CubeListBuilder.create().texOffs(0, 30).addBox(-12.0F, -2.0F, -2.5F, 5.0F, 24.0F, 5.0F, new CubeDeformation(0.0F)),
        PartPose.offset(0.0F, -9.0F, 0.0F));

    // held weapon -> third pass rotated the wrong axis: zRot swings the blade sideways in the X/Y plane, which
    // pokes it through the torso edge-on instead of out in front. xRot swings Y (down) toward Z (forward) instead
    // -> the same fix the Mage rod needed -> so the blade now angles forward away from the body like a held
    // sword, with a slight zRot for a natural off-center grip.
    PartDefinition weapon = arm0.addOrReplaceChild("weapon", CubeListBuilder.create().texOffs(90, 0).addBox(-1.0F, 0.0F, -3.0F, 2.0F, 22.0F, 6.0F, new CubeDeformation(0.0F)),
        PartPose.offsetAndRotation(-9.5F, 19.0F, 0.0F, -1.1F, 0.0F, -0.25F));

    PartDefinition crossguard = weapon.addOrReplaceChild("crossguard", CubeListBuilder.create().texOffs(90, 28).addBox(-3.0F, -1.0F, -0.5F, 6.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)),
        PartPose.offset(0.0F, 0.0F, 0.0F));

    PartDefinition arm1 = body.addOrReplaceChild("arm1", CubeListBuilder.create().texOffs(20, 30).mirror().addBox(7.0F, -2.0F, -2.5F, 5.0F, 24.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false),
        PartPose.offset(0.0F, -9.0F, 0.0F));

    PartDefinition leg0 = body.addOrReplaceChild("leg0", CubeListBuilder.create().texOffs(40, 30).addBox(-3.0F, 0.0F, -3.0F, 6.0F, 22.0F, 6.0F, new CubeDeformation(0.0F)),
        PartPose.offset(-4.0F, 7.0F, 0.0F));

    PartDefinition leg1 = body.addOrReplaceChild("leg1", CubeListBuilder.create().texOffs(64, 30).mirror().addBox(-3.0F, 0.0F, -3.0F, 6.0F, 22.0F, 6.0F, new CubeDeformation(0.0F)).mirror(false),
        PartPose.offset(4.0F, 7.0F, 0.0F));

    // universal Innocent Devil crystal cluster -> second pass had the sign backwards (positive z reads as the
    // *back* on this rig, same as the direction the chunky rig's own crystal offset is negative for) and
    // overshot the surface, so it floated detached behind the character instead of sitting on the chest. Negative
    // and just past the chest's front face (z=-3.5) now, so it actually touches.
    PartDefinition crystal = body.addOrReplaceChild("crystal", CubeListBuilder.create(), PartPose.offset(0.0F, -5.0F, 0.0F));

    PartDefinition crystal_r1 = crystal.addOrReplaceChild("crystal_r1", CubeListBuilder.create().texOffs(90, 36).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)),
        PartPose.offsetAndRotation(-1.5F, 0.0F, -4.5F, -0.9599F, 0.5236F, -0.6109F));

    PartDefinition crystal_r2 = crystal.addOrReplaceChild("crystal_r2", CubeListBuilder.create().texOffs(98, 36).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)),
        PartPose.offsetAndRotation(1.5F, 0.0F, -4.5F, -0.9599F, 0.5236F, -0.6109F));

    PartDefinition crystal_r3 = crystal.addOrReplaceChild("crystal_r3", CubeListBuilder.create().texOffs(0, 59).addBox(-3.0F, -3.5F, -1.0F, 6.0F, 6.0F, 8.0F, new CubeDeformation(0.0F)),
        PartPose.offsetAndRotation(0.0F, 1.5F, -5.0F, -0.4363F, 0.0F, 0.0F));

    return LayerDefinition.create(meshdefinition, 110, 80);
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

    this.leg0.xRot = -1.5F * Mth.triangleWave(limbSwing, 13.0F) * limbSwingAmount;
    this.leg1.xRot = 1.5F * Mth.triangleWave(limbSwing, 13.0F) * limbSwingAmount;

    float swing = entity.getAttackAnim(1.0F);
    if (swing > 0.0F) {
      float amount = Mth.sin(swing * (float) Math.PI) * -1.2F;
      this.arm0.xRot = amount;
      this.arm1.xRot = amount;
    } else {
      float idle = Mth.sin(ageInTicks * 0.05F) * 0.03F;
      this.arm0.xRot = (-0.2F + 1.5F * Mth.triangleWave(limbSwing, 13.0F)) * limbSwingAmount + idle;
      this.arm1.xRot = (-0.2F - 1.5F * Mth.triangleWave(limbSwing, 13.0F)) * limbSwingAmount - idle;
    }
  }
}
