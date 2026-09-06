package io.github.jason13official.summons.impl.client.renderer.summon;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.jason13official.summons.Summons;
import io.github.jason13official.summons.impl.client.model.CompanionCubeModel;
import io.github.jason13official.summons.impl.client.model.summon.BattleSummonModel;
import io.github.jason13official.summons.impl.common.entity.AbstractCompanion;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

public class BattleSummonRenderer extends EntityRenderer<AbstractCompanion> {

  public static final ResourceLocation TEXTURE_LOCATION = Summons.identifier("textures/entity/summon/battle.png");

  private final BattleSummonModel model;

  public BattleSummonRenderer(Context context) {
    super(context);
    this.model = new BattleSummonModel(context.bakeLayer(BattleSummonModel.LAYER_LOCATION));
  }

  @Override
  public ResourceLocation getTextureLocation(AbstractCompanion companionCube) {

    return TEXTURE_LOCATION;
  }

  @Override
  public void render(AbstractCompanion cube, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {

    poseStack.pushPose();

    // TODO adjust, calc 21.6 (ModEntities height in sized) but doesn't account for initial offset in model. needs 2.4px (total 24.0F px coincidental) more offset upwards
    poseStack.translate(0, 0.0625f * 24F, 0); // translate up by half the model height

    poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - entityYaw)); // BoatRenderer: face model in look direction
    poseStack.scale(-1.0F, -1.0F, 1.0F); // BoatRenderer: invert coordinate space

    this.model.setupAnim(cube, partialTick, 0.0F, -0.1F, 0.0F, 0.0F); // BoatRenderer: do animations/rotations?
    this.model.renderToBuffer(poseStack, bufferSource.getBuffer(RenderType.entityCutout(TEXTURE_LOCATION)), packedLight, OverlayTexture.NO_OVERLAY);
    poseStack.popPose();

    super.render(cube, entityYaw, partialTick, poseStack, bufferSource, packedLight);
  }
}
