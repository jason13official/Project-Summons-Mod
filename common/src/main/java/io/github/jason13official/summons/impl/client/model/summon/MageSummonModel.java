package io.github.jason13official.summons.impl.client.model.summon;

import io.github.jason13official.summons.Summons;
import io.github.jason13official.summons.impl.client.model.anim.MageSummonAnimations;
import io.github.jason13official.summons.impl.common.entity.AbstractCompanion;
import io.github.jason13official.summons.impl.common.entity.flying.MageSummon;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

public class MageSummonModel extends HierarchicalModel<AbstractCompanion> {

  // This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
  public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Summons.identifier("mage"), "main");

  private final ModelPart root;
  private final ModelPart head;
  private final ModelPart headAccessory;
  private final ModelPart body;
  private final ModelPart rightItem;
  private final ModelPart right_arm;
  private final ModelPart left_arm;
  private final ModelPart left_wing;
  private final ModelPart right_wing;
  private final ModelPart crystal;

  public MageSummonModel(ModelPart root) {
    this.root = root.getChild("root");
    this.head = this.root.getChild("head");
    this.headAccessory = this.head.getChild("headAccessory");
    this.body = this.root.getChild("body");
    this.right_arm = this.body.getChild("right_arm");
    this.rightItem = this.right_arm.getChild("rightItem");
    this.left_arm = this.body.getChild("left_arm");
    this.left_wing = this.body.getChild("left_wing");
    this.right_wing = this.body.getChild("right_wing");
    this.crystal = this.body.getChild("crystal");
  }

  public static LayerDefinition createBodyLayer() {
    MeshDefinition meshdefinition = new MeshDefinition();
    PartDefinition partdefinition = meshdefinition.getRoot();

    PartDefinition root = partdefinition.addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

    PartDefinition head = root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-2.5F, -5.01F, -2.5F, 5.0F, 5.0F, 5.0F, new CubeDeformation(0.0F)),
        PartPose.offset(0.0F, -4.0F, 0.0F));

    // generic per-form head ornament slot (horns, crown-point, eye motif, ...) -- same idea as DevilSummonModel's "hat"
    PartDefinition headAccessory = head.addOrReplaceChild("headAccessory", CubeListBuilder.create().texOffs(38, 0).addBox(-2.0F, -1.0F, -2.0F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)),
        PartPose.offset(0.0F, -5.5F, 0.0F));

    PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 10).addBox(-1.5F, 0.0F, -1.0F, 3.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
        .texOffs(0, 16).addBox(-1.5F, 0.0F, -1.0F, 3.0F, 5.0F, 2.0F, new CubeDeformation(-0.2F)), PartPose.offset(0.0F, -4.0F, 0.0F));

    PartDefinition right_arm = body.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(23, 0).addBox(-0.75F, -0.5F, -1.0F, 1.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)),
        PartPose.offset(-1.75F, 0.5F, 0.0F));

    // Rod prop -- every Mage form is named after this (Wood Rod, ... Twinkle Rod); previously an empty anchor that
    // rendered nothing, and even once given geometry was parented to the body instead of the hand so it floated in
    // front of the entity rather than looking held. Now a child of right_arm, gripped near the hand. -0.7 rad
    // still pointed the shaft too far downward and clipped the floor while grounded -- rotating around X toward
    // -90 degrees swings the shaft's reach from "downward" to "forward" (cos shrinks the down-component, sin
    // grows the forward one), so -1.25 keeps it angled like a held wand without reaching the ground. A thin shaft
    // plus a bigger ornament at the tip -- the ornament is where each form's identity actually lives (a plain
    // knob for Wood Rod, an eyeball for Eyeball Rod, a crystal for Crystal Rod, ...), same as how the reference
    // art differs almost entirely at the rod's head, not its shaft.
    PartDefinition rightItem = right_arm.addOrReplaceChild("rightItem", CubeListBuilder.create().texOffs(32, 0).addBox(-0.35F, 0.0F, -0.35F, 0.7F, 12.0F, 0.7F, new CubeDeformation(0.0F))
        .texOffs(54, 0).addBox(-1.25F, 11.5F, -1.25F, 2.5F, 2.5F, 2.5F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 3.5F, 0.0F, -1.25F, 0.0F, 0.0F));

    PartDefinition left_arm = body.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(23, 6).addBox(-0.25F, -0.5F, -1.0F, 1.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)),
        PartPose.offset(1.75F, 0.5F, 0.0F));

    PartDefinition left_wing = body.addOrReplaceChild("left_wing", CubeListBuilder.create().texOffs(16, 14).addBox(0.0F, 0.0F, 0.0F, 0.0F, 5.0F, 8.0F, new CubeDeformation(0.0F)),
        PartPose.offset(0.5F, 1.0F, 1.0F));

    PartDefinition right_wing = body.addOrReplaceChild("right_wing", CubeListBuilder.create().texOffs(16, 14).addBox(0.0F, 0.0F, 0.0F, 0.0F, 5.0F, 8.0F, new CubeDeformation(0.0F)),
        PartPose.offset(-0.5F, 1.0F, 1.0F));

    // universal Innocent Devil crystal cluster -- same pattern/angles as FairySummonModel's, on the chest
    PartDefinition crystal = body.addOrReplaceChild("crystal", CubeListBuilder.create(), PartPose.offset(0.0F, 2.0F, -1.0F));

    PartDefinition crystal_r1 = crystal.addOrReplaceChild("crystal_r1", CubeListBuilder.create().texOffs(32, 16).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)),
        PartPose.offsetAndRotation(-1.0F, 0.0F, 0.5F, -0.9599F, 0.5236F, -0.6109F));

    PartDefinition crystal_r2 = crystal.addOrReplaceChild("crystal_r2", CubeListBuilder.create().texOffs(40, 16).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)),
        PartPose.offsetAndRotation(1.0F, 0.0F, 0.5F, -0.9599F, 0.5236F, -0.6109F));

    PartDefinition crystal_r3 = crystal.addOrReplaceChild("crystal_r3", CubeListBuilder.create().texOffs(32, 20).addBox(-2.0F, -2.5F, -0.5F, 4.0F, 4.0F, 7.0F, new CubeDeformation(0.0F)),
        PartPose.offsetAndRotation(0.0F, 1.0F, 0.5F, -0.4363F, 0.0F, 0.0F));

    return LayerDefinition.create(meshdefinition, 64, 32);
  }

  @Override
  public ModelPart root() {
    return this.root;
  }

  @Override
  public void setupAnim(AbstractCompanion entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
    this.root().getAllParts().forEach(ModelPart::resetPose);

    if (!(entity instanceof MageSummon mage)) {
      return;
    }

    boolean busy = mage.isAbilityBusy() || mage.isAggressive();
    boolean flying = !mage.onGround();

    mage.idleAnimationState.animateWhen(!busy && !flying, mage.tickCount);
    mage.flyAnimationState.animateWhen(!busy && flying, mage.tickCount);
    this.animate(mage.idleAnimationState, MageSummonAnimations.IDLE, ageInTicks);
    this.animate(mage.flyAnimationState, MageSummonAnimations.FLY, ageInTicks);

    if (busy) {
      this.applyStatic(MageSummonAnimations.HOLD_ITEM);
    }
  }
}