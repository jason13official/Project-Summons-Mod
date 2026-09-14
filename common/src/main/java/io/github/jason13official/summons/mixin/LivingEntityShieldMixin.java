package io.github.jason13official.summons.mixin;

import io.github.jason13official.summons.impl.common.entity.AbstractCompanion;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/// Mage's "Shield" ability: blocks the owner's next hit completely, once armed. See AbstractCompanion#armOwnerShield/#consumeOwnerShield.
@Mixin(LivingEntity.class)
public class LivingEntityShieldMixin {

  @Inject(method = "hurt", at = @At("HEAD"), cancellable = true)
  private void summons$shield(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
    LivingEntity self = (LivingEntity) (Object) this;
    if (self.level().isClientSide || self instanceof AbstractCompanion) {
      return;
    }

    if (source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
      return;
    }

    if (AbstractCompanion.consumeOwnerShield(self)) {
      cir.setReturnValue(false);
    }
  }
}
