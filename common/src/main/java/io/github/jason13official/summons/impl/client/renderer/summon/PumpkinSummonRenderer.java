package io.github.jason13official.summons.impl.client.renderer.summon;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.jason13official.summons.Summons;
import io.github.jason13official.summons.impl.client.model.summon.PumpkinSummonModel;
import io.github.jason13official.summons.impl.common.entity.AbstractCompanion;
import io.github.jason13official.summons.impl.common.entity.ground.PumpkinSummon;
import io.github.jason13official.summons.impl.common.entity.ground.PumpkinSummon.Form;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

public class PumpkinSummonRenderer extends EntityRenderer<AbstractCompanion> {

  public static final ResourceLocation DEFAULT_TEXTURE_LOCATION = Summons.identifier("textures/entity/summon/pumpkin.png");

  private static final Map<Form, ResourceLocation> TEXTURE_BY_FORM = new HashMap<>();

  static {
    TEXTURE_BY_FORM.put(Form.PUMPKIN, DEFAULT_TEXTURE_LOCATION);
    TEXTURE_BY_FORM.put(Form.QUEEN, Summons.identifier("textures/entity/summon/pumpkin/queen.png"));
    TEXTURE_BY_FORM.put(Form.BLOODY, Summons.identifier("textures/entity/summon/pumpkin/bloody.png"));
    TEXTURE_BY_FORM.put(Form.TINY_KING, Summons.identifier("textures/entity/summon/pumpkin/tiny_king.png"));
    TEXTURE_BY_FORM.put(Form.CLOWN_NOSE, Summons.identifier("textures/entity/summon/pumpkin/clown_nose.png"));
    TEXTURE_BY_FORM.put(Form.NEW_DELI, Summons.identifier("textures/entity/summon/pumpkin/new_deli.png"));
    TEXTURE_BY_FORM.put(Form.CURSED_PUMPKIN, Summons.identifier("textures/entity/summon/pumpkin/cursed_pumpkin.png"));
    TEXTURE_BY_FORM.put(Form.WHIMSICAL_ANGEL, Summons.identifier("textures/entity/summon/pumpkin/whimsical_angel.png"));
    TEXTURE_BY_FORM.put(Form.GENIUS_CHEF, Summons.identifier("textures/entity/summon/pumpkin/genius_chef.png"));
  }

  private final PumpkinSummonModel model;

  public PumpkinSummonRenderer(Context context) {
    super(context);
    this.model = new PumpkinSummonModel(context.bakeLayer(PumpkinSummonModel.LAYER_LOCATION));
  }

  @Override
  public ResourceLocation getTextureLocation(AbstractCompanion companionCube) {

    if (!(companionCube instanceof PumpkinSummon pumpkin) || !(pumpkin.getEvolutionForm() instanceof Form pumpkinForm)) {

      return DEFAULT_TEXTURE_LOCATION;
    }

    return TEXTURE_BY_FORM.get(pumpkinForm);
  }

  @Override
  public void render(AbstractCompanion cube, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {

    if (cube.isWisp()) {
      return; // no real renderer while wisp; AbstractCompanion#tick spawns particles instead
    }

    poseStack.pushPose();

    // TODO adjust, calc 21.6 (ModEntities height in sized) but doesn't account for initial offset in model. needs 2.4px (total 24.0F px coincidental) more offset upwards
    poseStack.translate(0, 0.0625f * 23.5F, 0); // translate up by half the model height

    poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - entityYaw)); // BoatRenderer: face model in look direction
    poseStack.scale(-1.0F, -1.0F, 1.0F); // BoatRenderer: invert coordinate space

    this.model.setupAnim(cube, cube.walkAnimation.position(partialTick), Math.min(cube.walkAnimation.speed(partialTick), 1.0F),
        cube.tickCount + partialTick, 0.0F, 0.0F);
    this.model.renderToBuffer(poseStack, bufferSource.getBuffer(RenderType.entityCutout(getTextureLocation(cube))), packedLight, OverlayTexture.NO_OVERLAY);
    poseStack.popPose();

    super.render(cube, entityYaw, partialTick, poseStack, bufferSource, packedLight);
  }
}
