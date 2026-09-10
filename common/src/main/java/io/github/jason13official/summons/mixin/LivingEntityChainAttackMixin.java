package io.github.jason13official.summons.mixin;

import io.github.jason13official.summons.impl.common.entity.AbstractCompanion;
import io.github.jason13official.summons.impl.common.party.ChainAttackTracker;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/// feeds every landed player melee hit into ChainAttackTracker. `getDirectEntity() == the
/// player` (not just `getEntity()`) rules out arrows/other indirect sources -> only a real
/// melee swing (Player#attack) counts toward a combo.
@Mixin(LivingEntity.class)
public class LivingEntityChainAttackMixin {

  @Inject(method = "hurt", at = @At("TAIL"))
  private void summons$onHurt(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
    if (!cir.getReturnValue()) {
      return;
    }

    LivingEntity self = (LivingEntity) (Object) this;
    if (self instanceof ServerPlayer || self instanceof AbstractCompanion) {
      return;
    }

    if (source.getEntity() instanceof ServerPlayer player && source.getDirectEntity() == player) {
      ChainAttackTracker.onPlayerMeleeHit(player, self);
    }
  }
}
