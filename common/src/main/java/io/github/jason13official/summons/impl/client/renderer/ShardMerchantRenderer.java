package io.github.jason13official.summons.impl.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.jason13official.summons.Summons;
import io.github.jason13official.summons.impl.client.model.ShardMerchantModel;
import io.github.jason13official.summons.impl.common.entity.misc.ShardMerchant;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

public class ShardMerchantRenderer extends EntityRenderer<ShardMerchant> {

  private static final ResourceLocation TEXTURE = Summons.identifier("textures/entity/shard_merchant.png");

  private final ShardMerchantModel model;

  public ShardMerchantRenderer(Context context) {
    super(context);
    this.model = new ShardMerchantModel(context.bakeLayer(ShardMerchantModel.LAYER_LOCATION));
  }

  @Override
  public ResourceLocation getTextureLocation(ShardMerchant entity) {
    return TEXTURE;
  }

  @Override
  public void render(ShardMerchant entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
    poseStack.pushPose();

    // legs bottom out at model-local y=24 (see ShardMerchantModel, same box scheme as vanilla VillagerModel) -> translate up by that much so the feet land on the ground, same "BoatRenderer
    // trick" every other plain-EntityRenderer renderer in this mod uses (see BattleSummonRenderer/CompanionCubeRenderer): this renderer isn't a MobRenderer, so nothing does this automatically
    poseStack.translate(0, 0.0625F * 24F, 0);

    poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - entityYaw));
    poseStack.scale(-1.0F, -1.0F, 1.0F);

    this.model.setupAnim(entity, entity.walkAnimation.position(partialTick), Math.min(entity.walkAnimation.speed(partialTick), 1.0F),
        entity.tickCount + partialTick, entity.yHeadRot - entity.yBodyRot, entity.getXRot());
    this.model.renderToBuffer(poseStack, bufferSource.getBuffer(RenderType.entityCutout(TEXTURE)), packedLight, OverlayTexture.NO_OVERLAY);

    poseStack.popPose();

    super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
  }
}
