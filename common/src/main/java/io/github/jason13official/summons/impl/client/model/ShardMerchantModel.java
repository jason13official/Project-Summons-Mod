package io.github.jason13official.summons.impl.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.jason13official.summons.Summons;
import io.github.jason13official.summons.impl.common.entity.misc.ShardMerchant;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;

/// Placeholder shopkeeper rig, no real art yet (same "no assets" state as most of this mod) -> box layout copied straight from vanilla's own `VillagerModel`
/// (`D:\_decomp\1.21.1\minecraft\net\minecraft\client\model\VillagerModel.java`: same head/body/arms/leg boxes and UV offsets, jacket layer included) rather than invented from scratch, since
/// that's a proven-good silhouette for exactly this kind of humanoid NPC. Not an actual Villager subclass reuse (see ShardMerchant), so it needs its own model class; this one just borrows the
/// geometry.
public class ShardMerchantModel extends EntityModel<ShardMerchant> {

  public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Summons.identifier("shard_merchant"), "main");

  private final ModelPart root;
  private final ModelPart head;
  private final ModelPart body;
  private final ModelPart arms;
  private final ModelPart legRight;
  private final ModelPart legLeft;

  public ShardMerchantModel(ModelPart root) {
    this.root = root;
    this.head = root.getChild("head");
    this.body = root.getChild("body");
    this.arms = root.getChild("arms");
    this.legRight = root.getChild("right_leg");
    this.legLeft = root.getChild("left_leg");
  }

  public static LayerDefinition createBodyLayer() {
    MeshDefinition meshdefinition = new MeshDefinition();
    PartDefinition partdefinition = meshdefinition.getRoot();

    PartDefinition head = partdefinition.addOrReplaceChild("head",
        CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -10.0F, -4.0F, 8.0F, 10.0F, 8.0F, new CubeDeformation(0.0F)),
        PartPose.ZERO);
    head.addOrReplaceChild("nose",
        CubeListBuilder.create().texOffs(24, 0).addBox(-1.0F, -1.0F, -6.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)),
        PartPose.offset(0.0F, -2.0F, 0.0F));

    PartDefinition body = partdefinition.addOrReplaceChild("body",
        CubeListBuilder.create().texOffs(16, 20).addBox(-4.0F, 0.0F, -3.0F, 8.0F, 12.0F, 6.0F, new CubeDeformation(0.0F)),
        PartPose.ZERO);
    body.addOrReplaceChild("jacket",
        CubeListBuilder.create().texOffs(0, 38).addBox(-4.0F, 0.0F, -3.0F, 8.0F, 20.0F, 6.0F, new CubeDeformation(0.5F)),
        PartPose.ZERO);

    // matches Villager's forward-crossed shopkeeper arm pose (a single part with mirrored side boxes plus a hands-together center box), not two independently side-swinging arms
    partdefinition.addOrReplaceChild("arms",
        CubeListBuilder.create()
            .texOffs(44, 22).addBox(-8.0F, -2.0F, -2.0F, 4.0F, 8.0F, 4.0F, new CubeDeformation(0.0F))
            .texOffs(44, 22).mirror().addBox(4.0F, -2.0F, -2.0F, 4.0F, 8.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false)
            .texOffs(40, 38).addBox(-4.0F, 2.0F, -2.0F, 8.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)),
        PartPose.offsetAndRotation(0.0F, 3.0F, -1.0F, -0.75F, 0.0F, 0.0F));

    partdefinition.addOrReplaceChild("right_leg",
        CubeListBuilder.create().texOffs(0, 22).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)),
        PartPose.offset(-2.0F, 12.0F, 0.0F));
    partdefinition.addOrReplaceChild("left_leg",
        CubeListBuilder.create().texOffs(0, 22).mirror().addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false),
        PartPose.offset(2.0F, 12.0F, 0.0F));

    return LayerDefinition.create(meshdefinition, 64, 64);
  }

  @Override
  public void setupAnim(ShardMerchant entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
    this.head.yRot = netHeadYaw * ((float) Math.PI / 180F);
    this.head.xRot = headPitch * ((float) Math.PI / 180F);

    this.legRight.xRot = Mth.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount * 0.5F;
    this.legLeft.xRot = Mth.cos(limbSwing * 0.6662F + (float) Math.PI) * 1.4F * limbSwingAmount * 0.5F;
  }

  @Override
  public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int packedColor) {
    this.head.render(poseStack, vertexConsumer, packedLight, packedOverlay, packedColor);
    this.body.render(poseStack, vertexConsumer, packedLight, packedOverlay, packedColor);
    this.arms.render(poseStack, vertexConsumer, packedLight, packedOverlay, packedColor);
    this.legRight.render(poseStack, vertexConsumer, packedLight, packedOverlay, packedColor);
    this.legLeft.render(poseStack, vertexConsumer, packedLight, packedOverlay, packedColor);
  }
}
