package io.github.jason13official.summons.impl.client.renderer.summon;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.jason13official.summons.Summons;
import io.github.jason13official.summons.impl.client.model.summon.BattleSummonModel;
import io.github.jason13official.summons.impl.client.model.summon.BattleSummonModelArmored;
import io.github.jason13official.summons.impl.client.renderer.GuardFieldRenderer;
import io.github.jason13official.summons.impl.common.entity.AbstractCompanion;
import io.github.jason13official.summons.impl.common.entity.ground.BattleSummon;
import io.github.jason13official.summons.impl.common.entity.ground.BattleSummon.Form;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

public class BattleSummonRenderer extends EntityRenderer<AbstractCompanion> {

  public static final ResourceLocation DEFAULT_TEXTURE_LOCATION = Summons.identifier("textures/entity/summon/battle.png");

  private static final Map<Form, ResourceLocation> TEXTURE_BY_FORM = new HashMap<>();

  static {
    TEXTURE_BY_FORM.put(Form.MAGMARD, DEFAULT_TEXTURE_LOCATION);
    TEXTURE_BY_FORM.put(Form.SPEED_MAIL, Summons.identifier("textures/entity/summon/battle/speed_mail.png"));
    TEXTURE_BY_FORM.put(Form.GOLEM, Summons.identifier("textures/entity/summon/battle/golem.png"));
    TEXTURE_BY_FORM.put(Form.IYTEI, Summons.identifier("textures/entity/summon/battle/iytei.png"));
    TEXTURE_BY_FORM.put(Form.JUGGERNAUT, Summons.identifier("textures/entity/summon/battle/juggernaut.png"));
    TEXTURE_BY_FORM.put(Form.RASETZ, Summons.identifier("textures/entity/summon/battle/rasetz.png"));
    TEXTURE_BY_FORM.put(Form.CORPSEY, Summons.identifier("textures/entity/summon/battle/corpsey.png"));
    TEXTURE_BY_FORM.put(Form.IRONSIDE, Summons.identifier("textures/entity/summon/battle/ironside.png"));
    TEXTURE_BY_FORM.put(Form.LIQUID_GOLEM, Summons.identifier("textures/entity/summon/battle/liquid_golem.png"));
  }

  /// the humanoid/armored evolutions (all four shown wielding a weapon in their references) use [BattleSummonModelArmored] instead of the chunky Magmard-derived rig
  private static final Set<Form> ARMORED_FORMS = EnumSet.of(Form.RASETZ, Form.SPEED_MAIL, Form.IRONSIDE, Form.CORPSEY);

  private final HierarchicalModel<AbstractCompanion> chunkyModel;
  private final HierarchicalModel<AbstractCompanion> armoredModel;

  public BattleSummonRenderer(Context context) {
    super(context);
    this.chunkyModel = new BattleSummonModel(context.bakeLayer(BattleSummonModel.LAYER_LOCATION));
    this.armoredModel = new BattleSummonModelArmored(context.bakeLayer(BattleSummonModelArmored.LAYER_LOCATION));
  }

  @Override
  public ResourceLocation getTextureLocation(AbstractCompanion companionCube) {

    if (!(companionCube instanceof BattleSummon battle) || !(battle.getEvolutionForm() instanceof Form battleForm)) {

      return DEFAULT_TEXTURE_LOCATION;
    }

    return TEXTURE_BY_FORM.get(battleForm);
  }

  private HierarchicalModel<AbstractCompanion> modelFor(AbstractCompanion companion) {

    if (companion instanceof BattleSummon battle && battle.getEvolutionForm() instanceof Form battleForm && ARMORED_FORMS.contains(battleForm)) {

      return this.armoredModel;
    }

    return this.chunkyModel;
  }

  @Override
  public void render(AbstractCompanion cube, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {

    if (cube.isWisp()) {
      return; // no real renderer while wisp; AbstractCompanion#tick spawns particles instead
    }

    poseStack.pushPose();

    // TODO adjust, calc 21.6 (ModEntities height in sized) but doesn't account for initial offset in model. needs 2.4px (total 24.0F px coincidental) more offset upwards
    poseStack.translate(0, 0.0625f * 24F, 0); // translate up by half the model height

    poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - entityYaw)); // BoatRenderer: face model in look direction
    poseStack.scale(-1.0F, -1.0F, 1.0F); // BoatRenderer: invert coordinate space

    HierarchicalModel<AbstractCompanion> model = this.modelFor(cube);
    model.setupAnim(cube, cube.walkAnimation.position(partialTick), Math.min(cube.walkAnimation.speed(partialTick), 1.0F),
        cube.tickCount + partialTick, 0.0F, 0.0F);
    model.renderToBuffer(poseStack, bufferSource.getBuffer(RenderType.entityCutout(getTextureLocation(cube))), packedLight, OverlayTexture.NO_OVERLAY);
    poseStack.popPose();

    GuardFieldRenderer.render(cube, poseStack, bufferSource);

    super.render(cube, entityYaw, partialTick, poseStack, bufferSource, packedLight);
  }
}
