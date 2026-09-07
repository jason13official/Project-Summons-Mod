package io.github.jason13official.summons.mixin;

import io.github.jason13official.summons.impl.common.entity.AbstractCompanion;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/// Protects an owner standing inside their companion's Guard Field. Companion self-immunity
/// is handled separately in [AbstractCompanion#hurt].
@Mixin(LivingEntity.class)
public class LivingEntityGuardFieldMixin {

  @Inject(method = "hurt", at = @At("HEAD"), cancellable = true)
  private void summons$guardField(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
    LivingEntity self = (LivingEntity) (Object) this;
    if (self.level().isClientSide || self instanceof AbstractCompanion) {
      return;
    }

    if (source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
      return;
    }

    if (AbstractCompanion.isProtectedByGuardField(self)) {
      cir.setReturnValue(false);
    }
  }
}
