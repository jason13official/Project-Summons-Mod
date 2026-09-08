package io.github.jason13official.summons.mixin;

import io.github.jason13official.summons.impl.common.entity.flying.BirdSummon;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/// While riding a Bird-Type Summon (see BirdSummon#beginGlide), pose the rider's arms
/// reaching straight up as if gripping the companion's legs, overriding whatever pose the
/// rest of setupAnim computed; irrelevant anyway since the rider isn't walking/attacking.
@Mixin(HumanoidModel.class)
public class HumanoidModelBirdGripMixin {

  @Inject(method = "setupAnim", at = @At("TAIL"))
  private void summons$gripBirdLegs(LivingEntity entity, float limbSwing, float limbSwingAmount,
                                     float ageInTicks, float netHeadYaw, float headPitch, CallbackInfo ci) {
    if (!(entity instanceof Player player) || !(player.getVehicle() instanceof BirdSummon)) {
      return;
    }

    HumanoidModel<?> self = (HumanoidModel<?>) (Object) this;
    self.rightArm.xRot = (float) (-Math.PI * 0.9);
    self.rightArm.zRot = -0.1F;
    self.leftArm.xRot = (float) (-Math.PI * 0.9);
    self.leftArm.zRot = 0.1F;
  }
}
