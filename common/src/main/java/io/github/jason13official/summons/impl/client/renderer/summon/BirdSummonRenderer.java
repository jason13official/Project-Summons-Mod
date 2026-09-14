package io.github.jason13official.summons.impl.client.renderer.summon;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.jason13official.summons.Summons;
import io.github.jason13official.summons.impl.client.model.summon.BirdSummonModel;
import io.github.jason13official.summons.impl.common.entity.AbstractCompanion;
import io.github.jason13official.summons.impl.common.entity.flying.BirdSummon;
import io.github.jason13official.summons.impl.common.entity.flying.BirdSummon.Form;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

public class BirdSummonRenderer extends EntityRenderer<AbstractCompanion> {

  public static final ResourceLocation DEFAULT_TEXTURE_LOCATION = Summons.identifier("textures/entity/summon/bird.png");

  private static final Map<Form, ResourceLocation> TEXTURE_BY_FORM = new HashMap<>();

  static {
    TEXTURE_BY_FORM.put(Form.CROW, DEFAULT_TEXTURE_LOCATION);
    TEXTURE_BY_FORM.put(Form.GOLDFINCH, Summons.identifier("textures/entity/summon/bird/goldfinch.png"));
    TEXTURE_BY_FORM.put(Form.SKULL_WING, Summons.identifier("textures/entity/summon/bird/skull_wing.png"));
    TEXTURE_BY_FORM.put(Form.KHAOS, Summons.identifier("textures/entity/summon/bird/khaos.png"));
    TEXTURE_BY_FORM.put(Form.PHOENIX, Summons.identifier("textures/entity/summon/bird/phoenix.png"));
    TEXTURE_BY_FORM.put(Form.WINGOSAURUS, Summons.identifier("textures/entity/summon/bird/wingosaurus.png"));
    TEXTURE_BY_FORM.put(Form.BLAGSDEATH, Summons.identifier("textures/entity/summon/bird/blagsdeath.png"));
    TEXTURE_BY_FORM.put(Form.GARGOYLE, Summons.identifier("textures/entity/summon/bird/gargoyle.png"));
    TEXTURE_BY_FORM.put(Form.INDIGO, Summons.identifier("textures/entity/summon/bird/indigo.png"));
    TEXTURE_BY_FORM.put(Form.CRIMSON, Summons.identifier("textures/entity/summon/bird/crimson.png"));
  }

  private final BirdSummonModel model;

  public BirdSummonRenderer(Context context) {
    super(context);
    this.model = new BirdSummonModel(context.bakeLayer(BirdSummonModel.LAYER_LOCATION));
  }

  @Override
  public ResourceLocation getTextureLocation(AbstractCompanion companionCube) {

    if (!(companionCube instanceof BirdSummon bird) || !(bird.getEvolutionForm() instanceof Form birdForm)) {

      return DEFAULT_TEXTURE_LOCATION;
    }

    return TEXTURE_BY_FORM.get(birdForm);
  }

  @Override
  public void render(AbstractCompanion cube, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {

    if (cube.isWisp()) {
      return; // no real renderer while wisp; AbstractCompanion#tick spawns particles instead
    }

    poseStack.pushPose();

    // TODO adjust, calc 7.2 (ModEntities height in sized) but doesn't account for initial offset in model. needs --2.4px (total --24.0F px coincidental) more offset upwards
    poseStack.translate(0, 0.0625f * 24F, 0); // translate up by half the model height

    poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - entityYaw)); // BoatRenderer: face model in look direction
    poseStack.scale(-1.0F, -1.0F, 1.0F); // BoatRenderer: invert coordinate space

    this.model.setupAnim(cube, cube.walkAnimation.position(partialTick), Math.min(cube.walkAnimation.speed(partialTick), 1.0F),
        cube.tickCount + partialTick, 0.0F, 0.0F);
    this.model.renderToBuffer(poseStack, bufferSource.getBuffer(RenderType.entityCutout(getTextureLocation(cube))), packedLight, OverlayTexture.NO_OVERLAY);
    poseStack.popPose();

    super.render(cube, entityYaw, partialTick, poseStack, bufferSource, packedLight);
  }
}
