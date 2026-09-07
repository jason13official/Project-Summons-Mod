package io.github.jason13official.summons.mixin;

import io.github.jason13official.summons.impl.common.entity.AbstractCompanion;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/// "If Hector stands in the Guard Field he will also be protected from enemy attacks"
/// - a companion's own Guard-mode immunity is handled directly in [AbstractCompanion#hurt];
/// this covers the owner standing inside one of their companion's Guard Fields instead.
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
