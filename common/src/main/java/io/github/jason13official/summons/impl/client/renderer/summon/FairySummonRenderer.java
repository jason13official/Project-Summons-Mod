package io.github.jason13official.summons.impl.client.renderer.summon;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.jason13official.summons.Summons;
import io.github.jason13official.summons.impl.client.model.summon.FairySummonModel;
import io.github.jason13official.summons.impl.common.entity.AbstractCompanion;
import io.github.jason13official.summons.impl.common.entity.flying.FairySummon;
import io.github.jason13official.summons.impl.common.entity.flying.FairySummon.Form;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

public class FairySummonRenderer extends EntityRenderer<AbstractCompanion> {

  public static final ResourceLocation DEFAULT_TEXTURE_LOCATION = Summons.identifier("textures/entity/summon/fairy.png");

  private static final Map<Form, ResourceLocation> TEXTURE_BY_FORM = new HashMap<>();

  static {
    TEXTURE_BY_FORM.put(Form.INFANT_FAIRY, DEFAULT_TEXTURE_LOCATION);
    TEXTURE_BY_FORM.put(Form.LEAFFLE, Summons.identifier("textures/entity/summon/fairy_leaffle.png"));
    TEXTURE_BY_FORM.put(Form.HERBEST, Summons.identifier("textures/entity/summon/fairy_herbest.png"));
    TEXTURE_BY_FORM.put(Form.HONEY_BEE, Summons.identifier("textures/entity/summon/fairy_honey_bee.png"));
    TEXTURE_BY_FORM.put(Form.KILLER_BEE, Summons.identifier("textures/entity/summon/fairy_killer_bee.png"));
    TEXTURE_BY_FORM.put(Form.HORNET, Summons.identifier("textures/entity/summon/fairy_hornet.png"));
    TEXTURE_BY_FORM.put(Form.PROBOSCIS_FAIRY, Summons.identifier("textures/entity/summon/fairy_proboscis_fairy.png"));
    TEXTURE_BY_FORM.put(Form.TIRAMISU, Summons.identifier("textures/entity/summon/fairy_tiramisu.png"));
    TEXTURE_BY_FORM.put(Form.TIARA, Summons.identifier("textures/entity/summon/fairy_tiara.png"));
    TEXTURE_BY_FORM.put(Form.COMET_STAR, Summons.identifier("textures/entity/summon/fairy_comet_star.png"));
  }

  private final FairySummonModel model;

  public FairySummonRenderer(Context context) {
    super(context);
    this.model = new FairySummonModel(context.bakeLayer(FairySummonModel.LAYER_LOCATION));
  }

  @Override
  public ResourceLocation getTextureLocation(AbstractCompanion companion) {

    if (!(companion instanceof FairySummon fairy) || !(fairy.getEvolutionForm() instanceof Form fairySummonForm)) {

      return DEFAULT_TEXTURE_LOCATION;
    }

    return TEXTURE_BY_FORM.get(fairySummonForm);
  }

  @Override
  public void render(AbstractCompanion cube, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {

    if (cube.isWisp()) {
      return; // no real renderer while wisp; AbstractCompanion#tick spawns particles instead
    }

    poseStack.pushPose();

    poseStack.translate(0, 0.0625f * 12F, 0); // translate up by half the model height

    poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - entityYaw)); // BoatRenderer: face model in look direction

    poseStack.scale(0.5f, 0.5f, 0.5f); // custom
    poseStack.scale(-1.0F, -1.0F, 1.0F); // BoatRenderer: invert coordinate space

    this.model.setupAnim(cube, cube.walkAnimation.position(partialTick), Math.min(cube.walkAnimation.speed(partialTick), 1.0F),
        cube.tickCount + partialTick, 0.0F, 0.0F);
    // entityCutoutNoCull, not entityCutout: the wings are thin double-sided planes that
    // rotate freely, and GL backface culling made one side of them disappear mid-flap
    this.model.renderToBuffer(poseStack, bufferSource.getBuffer(RenderType.entityCutoutNoCull(DEFAULT_TEXTURE_LOCATION)), packedLight, OverlayTexture.NO_OVERLAY);
    poseStack.popPose();

    super.render(cube, entityYaw, partialTick, poseStack, bufferSource, packedLight);
  }
}
