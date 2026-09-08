package io.github.jason13official.summons.mixin;

import io.github.jason13official.summons.impl.common.entity.AbstractCompanion;
import io.github.jason13official.summons.impl.common.party.CompanionPartyManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/// CON owner buff: reduces the duration of a harmful effect landing on an owner with an active companion,
/// capped at 80% -> no vanilla attribute for this, so it's applied here instead of via
/// AttributeModifier (see AbstractCompanion#ownerStatBonus).
@Mixin(LivingEntity.class)
public class LivingEntityEffectResistMixin {

  @ModifyVariable(method = "addEffect(Lnet/minecraft/world/effect/MobEffectInstance;Lnet/minecraft/world/entity/Entity;)Z", at = @At("HEAD"), argsOnly = true)
  private MobEffectInstance summons$resistNegativeEffects(MobEffectInstance effectInstance) {

    LivingEntity self = (LivingEntity) (Object) this;

    if (!(self instanceof ServerPlayer player) || effectInstance.getEffect().value().getCategory() != MobEffectCategory.HARMFUL) {
      return effectInstance;
    }

    AbstractCompanion active = CompanionPartyManager.findActive(player.serverLevel(), player);
    if (active == null) {
      return effectInstance;
    }

    double resistPercent = Math.min(active.ownerStatBonus().con(active.getLevel()), 80.0);
    if (resistPercent <= 0.0) {
      return effectInstance;
    }

    int newDuration = (int) (effectInstance.getDuration() * (1.0 - resistPercent / 100.0));
    return new MobEffectInstance(effectInstance.getEffect(), newDuration, effectInstance.getAmplifier(), effectInstance.isAmbient(), effectInstance.isVisible(), effectInstance.showIcon());
  }
}
