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

/// Legless "floating blob with wings" rig for Skull Wing and Khaos -> both references are a head/face cluster with
/// wings mounted directly on it and a thin whip-tail, no real torso or legs. Part names match [BirdSummonModel] so
/// it shares [BirdSummonAnimations] unchanged; leg0/leg1 are tiny stub claws just to keep those channels valid.
public class BirdSummonModelOrb extends HierarchicalModel<AbstractCompanion> {

  public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Summons.identifier("bird_orb"), "main");

  private final ModelPart root;
  private final ModelPart body;
  private final ModelPart wing0;
  private final ModelPart wing1;
  private final ModelPart head;
  private final ModelPart tail;
  private final ModelPart leg0;
  private final ModelPart leg1;
  private final ModelPart crystal;

  public BirdSummonModelOrb(ModelPart root) {
    this.root = root;
    this.body = root.getChild("body");
    this.wing0 = this.body.getChild("wing0");
    this.wing1 = this.body.getChild("wing1");
    this.head = this.body.getChild("head");
    this.tail = this.body.getChild("tail");
    this.leg0 = this.body.getChild("leg0");
    this.leg1 = this.body.getChild("leg1");
    this.crystal = this.body.getChild("crystal");
  }

  public static LayerDefinition createBodyLayer() {
    MeshDefinition meshdefinition = new MeshDefinition();
    PartDefinition partdefinition = meshdefinition.getRoot();

    // the blob itself -> everything else (wings, whip-tail, tiny claws) hangs directly off it, no waist/neck.
    // Bumped from 6x6x6 -> at that size plus a first-pass flat fill it read as a plain dark cube in-game, not a mass of faces/a skull
    PartDefinition body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -4.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F)),
        PartPose.offset(0.0F, 17.0F, 0.0F));

    // small front-facing detail nested in the blob's own UV space -> exists so this rig still has a distinct "head" for the animation channels, not to imply a separate anatomical head
    PartDefinition head = body.addOrReplaceChild("head", CubeListBuilder.create().texOffs(32, 0).addBox(-2.0F, -2.0F, -2.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)),
        PartPose.offset(0.0F, -1.0F, -5.0F));

    PartDefinition wing0 = body.addOrReplaceChild("wing0", CubeListBuilder.create().texOffs(0, 16).addBox(-1.0F, 0.0F, -5.5F, 1.0F, 20.0F, 11.0F, new CubeDeformation(0.0F)),
        PartPose.offsetAndRotation(4.0F, -1.0F, 0.0F, -0.1745F, 3.1416F, 0.0F));

    PartDefinition wing1 = body.addOrReplaceChild("wing1", CubeListBuilder.create().texOffs(0, 16).addBox(-1.0F, 0.0F, -5.5F, 1.0F, 20.0F, 11.0F, new CubeDeformation(0.0F)),
        PartPose.offsetAndRotation(-4.0F, -1.0F, 0.0F, -0.1745F, 3.1416F, 0.0F));

    PartDefinition tail = body.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(24, 16).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 12.0F, 2.0F, new CubeDeformation(0.0F)),
        PartPose.offsetAndRotation(0.0F, 4.0F, 0.0F, 0.2F, 0.0F, 0.0F));

    PartDefinition leg0 = body.addOrReplaceChild("leg0", CubeListBuilder.create().texOffs(38, 0).addBox(-0.75F, 0.0F, -0.75F, 1.5F, 3.0F, 1.5F, new CubeDeformation(0.0F)),
        PartPose.offset(2.0F, 3.5F, 2.0F));

    PartDefinition leg1 = body.addOrReplaceChild("leg1", CubeListBuilder.create().texOffs(38, 0).addBox(-0.75F, 0.0F, -0.75F, 1.5F, 3.0F, 1.5F, new CubeDeformation(0.0F)),
        PartPose.offset(-2.0F, 3.5F, 2.0F));

    // universal Innocent Devil crystal cluster -> embedded straight into the blob; nudged up from center so it
    // isn't half-hidden behind the head detail from the front
    PartDefinition crystal = body.addOrReplaceChild("crystal", CubeListBuilder.create(), PartPose.offset(0.0F, -2.0F, 0.0F));

    PartDefinition crystal_r1 = crystal.addOrReplaceChild("crystal_r1", CubeListBuilder.create().texOffs(48, 0).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)),
        PartPose.offsetAndRotation(-2.0F, 1.0F, -1.5F, -0.9599F, 0.5236F, -0.6109F));

    PartDefinition crystal_r2 = crystal.addOrReplaceChild("crystal_r2", CubeListBuilder.create().texOffs(56, 0).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)),
        PartPose.offsetAndRotation(2.0F, 1.0F, -1.5F, -0.9599F, 0.5236F, -0.6109F));

    PartDefinition crystal_r3 = crystal.addOrReplaceChild("crystal_r3", CubeListBuilder.create().texOffs(30, 16).addBox(-2.0F, -3.0F, -1.0F, 4.0F, 4.0F, 7.0F, new CubeDeformation(0.0F)),
        PartPose.offsetAndRotation(0.0F, -1.0F, -1.5F, -0.4363F, 0.0F, 0.0F));

    return LayerDefinition.create(meshdefinition, 64, 48);
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
