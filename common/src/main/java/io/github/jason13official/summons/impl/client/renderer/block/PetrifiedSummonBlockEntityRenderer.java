package io.github.jason13official.summons.impl.client.renderer.block;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.jason13official.summons.Summons;
import io.github.jason13official.summons.impl.client.model.block.PetrifiedSummonBodyModel;
import io.github.jason13official.summons.impl.client.model.block.PetrifiedSummonSwirlModel;
import io.github.jason13official.summons.impl.common.block.PetrifiedSummonBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

/// draws the petrified statue entirely by hand (no baked block model; see [io.github.jason13official.summons.impl.common.block.PetrifiedSummonBlock#getRenderShape]):
/// a plain textured cube, plus, while awakening (the countdown mirrored onto [PetrifiedSummonBlockEntity] from SummonGateManager's PendingWarp), a
/// growing scale + PrimedTnt-style white flash on the cube, and a Creeper-charged-style scrolling energy-swirl aura around it.
public class PetrifiedSummonBlockEntityRenderer implements BlockEntityRenderer<PetrifiedSummonBlockEntity> {

  private static final ResourceLocation BODY_TEXTURE = Summons.identifier("textures/block/petrified_statue_body.png");
  private static final ResourceLocation SWIRL_TEXTURE = Summons.identifier("textures/block/petrified_swirl.png");
  private static final float MAX_GROWTH = 0.25F;
  private static final int SWIRL_TINT = 0xFFE6E6FF; // pale blue-white, ARGB

  private final PetrifiedSummonBodyModel bodyModel;
  private final PetrifiedSummonSwirlModel swirlModel;

  public PetrifiedSummonBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    this.bodyModel = new PetrifiedSummonBodyModel(context.bakeLayer(PetrifiedSummonBodyModel.LAYER_LOCATION));
    this.swirlModel = new PetrifiedSummonSwirlModel(context.bakeLayer(PetrifiedSummonSwirlModel.LAYER_LOCATION));
  }

  @Override
  public void render(PetrifiedSummonBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource,
      int packedLight, int packedOverlay) {

    float progress = blockEntity.progress(partialTick);

    poseStack.pushPose();
    poseStack.translate(0.5, 0.5, 0.5);

    // ModelPart cube coordinates already bake down to block-space units, so no /16 needed here
    float scale = 1.0F + progress * progress * MAX_GROWTH;
    poseStack.scale(scale, scale, scale);

    boolean flash = progress > 0.0F && (blockEntity.ticksLeft() / 5) % 2 == 0;
    int flashOverlay = flash ? OverlayTexture.pack(OverlayTexture.u(1.0F), 10) : OverlayTexture.NO_OVERLAY;
    this.bodyModel.body().render(poseStack, bufferSource.getBuffer(RenderType.entityCutout(BODY_TEXTURE)), packedLight, flashOverlay);

    if (progress > 0.0F) {
      Level level = blockEntity.getLevel();
      float ageInTicks = (level != null ? level.getGameTime() : 0L) + partialTick;
      float scroll = (ageInTicks * 0.02F) % 1.0F;
      this.swirlModel.shell().render(poseStack, bufferSource.getBuffer(RenderType.energySwirl(SWIRL_TEXTURE, scroll, scroll)),
          packedLight, OverlayTexture.NO_OVERLAY, SWIRL_TINT);
    }

    poseStack.popPose();
  }
}
