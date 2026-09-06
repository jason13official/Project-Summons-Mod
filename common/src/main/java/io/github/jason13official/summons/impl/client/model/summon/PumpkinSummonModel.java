package io.github.jason13official.summons.impl.client.model.summon;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.jason13official.summons.Summons;
import io.github.jason13official.summons.impl.common.entity.AbstractCompanion;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

public class PumpkinSummonModel extends EntityModel<AbstractCompanion> {

  // This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
  public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Summons.identifier("pumpkin"), "main");

	private final ModelPart piece2;
	private final ModelPart piece1;
	private final ModelPart head;
	private final ModelPart pumpkin;
	private final ModelPart arm1;
	private final ModelPart arm2;

	public PumpkinSummonModel(ModelPart root) {
		this.piece2 = root.getChild("piece2");
		this.piece1 = this.piece2.getChild("piece1");
		this.head = this.piece1.getChild("head");
		this.pumpkin = this.head.getChild("pumpkin");
		this.arm1 = this.piece1.getChild("arm1");
		this.arm2 = this.piece1.getChild("arm2");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition piece2 = partdefinition.addOrReplaceChild("piece2", CubeListBuilder.create().texOffs(0, 36).addBox(-6.0F, -12.0F, -6.0F, 12.0F, 12.0F, 12.0F, new CubeDeformation(-0.5F)), PartPose.offset(0.0F, 24.0F, 0.0F));

		PartDefinition piece1 = piece2.addOrReplaceChild("piece1", CubeListBuilder.create().texOffs(0, 16).addBox(-5.0F, -10.0F, -5.0F, 10.0F, 10.0F, 10.0F, new CubeDeformation(-0.5F)), PartPose.offset(0.0F, -11.0F, 0.0F));

		PartDefinition head = piece1.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(-0.5F)), PartPose.offset(0.0F, -9.0F, 0.0F));

		PartDefinition pumpkin = head.addOrReplaceChild("pumpkin", CubeListBuilder.create().texOffs(0, 60).addBox(-8.0F, -8.0F, -8.0F, 16.0F, 16.0F, 16.0F, new CubeDeformation(-3.0F)), PartPose.offset(0.0F, -5.5F, 0.0F));

		PartDefinition arm1 = piece1.addOrReplaceChild("arm1", CubeListBuilder.create().texOffs(32, 0).addBox(1.0F, -4.0F, -1.0F, 12.0F, 2.0F, 2.0F, new CubeDeformation(-0.5F)), PartPose.offsetAndRotation(0.0F, -7.0F, 0.0F, 0.0F, 0.0F, 1.0001F));

		PartDefinition arm2 = piece1.addOrReplaceChild("arm2", CubeListBuilder.create().texOffs(32, 0).mirror().addBox(-13.0F, -4.0F, -1.0F, 12.0F, 2.0F, 2.0F, new CubeDeformation(-0.5F)).mirror(false), PartPose.offsetAndRotation(0.0F, -7.0F, 0.0F, 0.0F, 0.0F, -1.0001F));

		return LayerDefinition.create(meshdefinition, 128, 128);
	}

  @Override
  public void setupAnim(AbstractCompanion entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {

  }

  @Override
  public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
    piece2.render(poseStack, vertexConsumer, packedLight, packedOverlay);
  }
}