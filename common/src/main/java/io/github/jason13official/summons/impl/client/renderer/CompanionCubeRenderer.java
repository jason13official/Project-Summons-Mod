package io.github.jason13official.summons.impl.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.jason13official.summons.Summons;
import io.github.jason13official.summons.impl.client.model.CompanionCubeModel;
import io.github.jason13official.summons.impl.common.entity.AbstractCompanion;
import io.github.jason13official.summons.impl.common.entity.ground.CompanionCube;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

public class CompanionCubeRenderer extends EntityRenderer<AbstractCompanion> {

  public static final ResourceLocation CUBE_LOCATION = Summons.identifier("textures/entity/companion/companion_cube.png");

  private final CompanionCubeModel model;

  public CompanionCubeRenderer(Context context) {
    super(context);
    this.model = new CompanionCubeModel(context.bakeLayer(CompanionCubeModel.LAYER_LOCATION));
  }

  @Override
  public ResourceLocation getTextureLocation(AbstractCompanion companionCube) {

    return CUBE_LOCATION;
  }

  @Override
  public void render(AbstractCompanion cube, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {

    poseStack.pushPose();
    poseStack.translate(0, 0.0625f * 4, 0); // translate up by half the model height

    poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - entityYaw)); // BoatRenderer: face model in look direction
    poseStack.scale(-1.0F, -1.0F, 1.0F); // BoatRenderer: invert coordinate space

    this.model.setupAnim(cube, partialTick, 0.0F, -0.1F, 0.0F, 0.0F); // BoatRenderer: do animations/rotations?
    this.model.renderToBuffer(poseStack, bufferSource.getBuffer(RenderType.entityCutout(CUBE_LOCATION)), packedLight, OverlayTexture.NO_OVERLAY);
    poseStack.popPose();

    super.render(cube, entityYaw, partialTick, poseStack, bufferSource, packedLight);
  }
}
