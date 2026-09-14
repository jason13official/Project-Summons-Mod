package io.github.jason13official.summons.impl.client.renderer.summon;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.jason13official.summons.Summons;
import io.github.jason13official.summons.impl.client.model.summon.MageSummonModel;
import io.github.jason13official.summons.impl.common.entity.AbstractCompanion;
import io.github.jason13official.summons.impl.common.entity.flying.MageSummon;
import io.github.jason13official.summons.impl.common.entity.flying.MageSummon.Form;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

public class MageSummonRenderer extends EntityRenderer<AbstractCompanion> {

  public static final ResourceLocation DEFAULT_TEXTURE_LOCATION = Summons.identifier("textures/entity/summon/mage.png");

  private static final Map<Form, ResourceLocation> TEXTURE_BY_FORM = new HashMap<>();

  static {
    TEXTURE_BY_FORM.put(Form.WOOD_ROD, DEFAULT_TEXTURE_LOCATION);
    TEXTURE_BY_FORM.put(Form.SCISSOR_ROD, Summons.identifier("textures/entity/summon/mage/scissor_rod.png"));
    TEXTURE_BY_FORM.put(Form.TALON_ROD, Summons.identifier("textures/entity/summon/mage/talon_rod.png"));
    TEXTURE_BY_FORM.put(Form.NAUTILUS_ROD, Summons.identifier("textures/entity/summon/mage/nautilus_rod.png"));
    TEXTURE_BY_FORM.put(Form.OGRE_ROD, Summons.identifier("textures/entity/summon/mage/ogre_rod.png"));
    TEXTURE_BY_FORM.put(Form.GOAT_HEAD, Summons.identifier("textures/entity/summon/mage/goat_head.png"));
    TEXTURE_BY_FORM.put(Form.EYEBALL_ROD, Summons.identifier("textures/entity/summon/mage/eyeball_rod.png"));
    TEXTURE_BY_FORM.put(Form.EMBRYO_ROD, Summons.identifier("textures/entity/summon/mage/embryo_rod.png"));
    TEXTURE_BY_FORM.put(Form.CRYSTAL_ROD, Summons.identifier("textures/entity/summon/mage/crystal_rod.png"));
    TEXTURE_BY_FORM.put(Form.TWINKLE_ROD, Summons.identifier("textures/entity/summon/mage/twinkle_rod.png"));
  }

  private final MageSummonModel model;

  public MageSummonRenderer(Context context) {
    super(context);
    this.model = new MageSummonModel(context.bakeLayer(MageSummonModel.LAYER_LOCATION));
  }

  @Override
  public ResourceLocation getTextureLocation(AbstractCompanion companionCube) {

    if (!(companionCube instanceof MageSummon mage) || !(mage.getEvolutionForm() instanceof Form mageForm)) {

      return DEFAULT_TEXTURE_LOCATION;
    }

    return TEXTURE_BY_FORM.get(mageForm);
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
    // entityCutoutNoCull, not entityCutout: the wings are thin double-sided planes and GL
    // backface culling made them only visible from one side
    this.model.renderToBuffer(poseStack, bufferSource.getBuffer(RenderType.entityCutoutNoCull(getTextureLocation(cube))), packedLight, OverlayTexture.NO_OVERLAY);
    poseStack.popPose();

    super.render(cube, entityYaw, partialTick, poseStack, bufferSource, packedLight);
  }
}
