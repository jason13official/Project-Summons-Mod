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

/// Humanoid-winged rig for Blagsdeath and Gargoyle: reference art (`C:\Users\jason\Downloads\castlevania\bird\{blagsdeath,gargoyle}.webp`) shows a torso with legs and shoulder-mounted
/// membrane-wing "arms" and a horned head, not the beaked bird/dragon body plan [BirdSummonModelWinged] was actually built for (that rig fits Phoenix/Wingosaurus/Crimson, which really do have
/// a bird-shaped body+beak+tail; Blagsdeath/Gargoyle were bucketed there without an individual reference check). Part names still match
/// [BirdSummonModel] so it shares [BirdSummonAnimations] unchanged; `tail` is a tiny, hidden stub (same trick [BirdSummonModelOrb] uses for its legs) since neither reference shows one. The
/// universal crystal cluster sits at the feet here instead of the chest, matching both references showing crystal boots rather than a chest crystal.
public class BirdSummonModelHumanoid extends HierarchicalModel<AbstractCompanion> {

  public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Summons.identifier("bird_humanoid"), "main");

  private final ModelPart root;
  private final ModelPart body;
  private final ModelPart wing0;
  private final ModelPart wing1;
  private final ModelPart head;
  private final ModelPart tail;
  private final ModelPart leg0;
  private final ModelPart leg1;
  private final ModelPart crystal;

  public BirdSummonModelHumanoid(ModelPart root) {
    this.root = root;
    this.body = root.getChild("body");
    this.wing0 = this.body.getChild("wing0");
    this.wing1 = this.body.getChild("wing1");
    this.head = this.body.getChild("head");
    this.tail = this.body.getChild("tail");
    this.leg0 = root.getChild("leg0");
    this.leg1 = root.getChild("leg1");
    this.crystal = root.getChild("crystal");
  }

  public static LayerDefinition createBodyLayer() {
    MeshDefinition meshdefinition = new MeshDefinition();
    PartDefinition partdefinition = meshdefinition.getRoot();

    PartDefinition body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 16).addBox(-3.0F, 0.0F, -2.5F, 6.0F, 10.0F, 5.0F, new CubeDeformation(0.0F)),
        PartPose.offset(0.0F, 15.0F, 0.0F));

    // horned humanoid head -> one head box plus two small back-swept horn nubs, matching the antenna/horn silhouette both references share
    PartDefinition head = body.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-2.5F, -5.0F, -2.5F, 5.0F, 5.0F, 5.0F, new CubeDeformation(0.0F)),
        PartPose.offset(0.0F, 0.0F, 0.0F));

    PartDefinition horn0 = head.addOrReplaceChild("horn0", CubeListBuilder.create().texOffs(20, 0).addBox(-0.5F, -4.0F, -0.5F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)),
        PartPose.offsetAndRotation(-1.5F, -4.5F, 0.5F, 0.2618F, 0.0F, -0.2618F));

    PartDefinition horn1 = head.addOrReplaceChild("horn1", CubeListBuilder.create().texOffs(20, 0).addBox(-0.5F, -4.0F, -0.5F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)),
        PartPose.offsetAndRotation(1.5F, -4.5F, 0.5F, 0.2618F, 0.0F, 0.2618F));

    // shoulder-mounted wings -> same box shape as BirdSummonModelWinged's wings (kept "long" along the local Y axis for UV economy). Both references show these rising vertically from the
    // shoulder and sitting behind the torso (gargoyle/harpy silhouette), not spread horizontally to the sides like a first pass had it: near-zero Z rotation keeps the box pointing straight up
    // along its own long axis, a small X tilt sweeps the tips back, and the positive Z offset pushes the whole wing behind the shoulder line (positive Z is this rig family's *back*, same
    // convention as every other type's crystal placement).
    PartDefinition wing0 = body.addOrReplaceChild("wing0", CubeListBuilder.create().texOffs(24, 0).addBox(-1.0F, 0.0F, -5.0F, 2.0F, 18.0F, 10.0F, new CubeDeformation(0.0F)),
        PartPose.offsetAndRotation(3.0F, 0.0F, 2.0F, 0.2618F, 0.0F, 0.1745F));

    PartDefinition wing1 = body.addOrReplaceChild("wing1", CubeListBuilder.create().texOffs(24, 0).addBox(-1.0F, 0.0F, -5.0F, 2.0F, 18.0F, 10.0F, new CubeDeformation(0.0F)),
        PartPose.offsetAndRotation(-3.0F, 0.0F, 2.0F, 0.2618F, 0.0F, -0.1745F));

    // no visible tail on either reference -> tiny hidden stub just to keep BirdSummonAnimations' tail channel valid, same trick used elsewhere in this file (MageSummonModel's wings) for an
    // effectively-invisible part: zero on one axis only, not all three (a fully zero-size box is degenerate)
    PartDefinition tail = body.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(0, 0).addBox(0.0F, 0.0F, 0.0F, 0.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)),
        PartPose.offset(0.0F, 5.0F, 2.5F));

    PartDefinition leg0 = partdefinition.addOrReplaceChild("leg0", CubeListBuilder.create().texOffs(0, 31).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 9.0F, 3.0F, new CubeDeformation(0.0F)),
        PartPose.offset(1.6F, 25.0F, 0.0F));

    PartDefinition leg1 = partdefinition.addOrReplaceChild("leg1", CubeListBuilder.create().texOffs(0, 31).mirror().addBox(-1.5F, 0.0F, -1.5F, 3.0F, 9.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false),
        PartPose.offset(-1.6F, 25.0F, 0.0F));

    // universal Innocent Devil crystal cluster -> here it's the crystal *boots* both references actually show, so it's parented at the feet instead of the chest like every other type
    PartDefinition crystal = partdefinition.addOrReplaceChild("crystal", CubeListBuilder.create(), PartPose.offset(0.0F, 34.0F, 0.0F));

    PartDefinition crystal_r1 = crystal.addOrReplaceChild("crystal_r1", CubeListBuilder.create().texOffs(48, 0).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)),
        PartPose.offsetAndRotation(1.6F, -1.0F, 0.0F, -0.9599F, 0.5236F, -0.6109F));

    PartDefinition crystal_r2 = crystal.addOrReplaceChild("crystal_r2", CubeListBuilder.create().texOffs(56, 0).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)),
        PartPose.offsetAndRotation(-1.6F, -1.0F, 0.0F, -0.9599F, 0.5236F, -0.6109F));

    PartDefinition crystal_r3 = crystal.addOrReplaceChild("crystal_r3", CubeListBuilder.create().texOffs(24, 28).addBox(-3.0F, -1.0F, -2.0F, 6.0F, 5.0F, 4.0F, new CubeDeformation(0.0F)),
        PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.2182F, 0.0F, 0.0F));

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
