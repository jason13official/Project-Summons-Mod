package io.github.jason13official.summons.impl.common.entity.ai.goal.attack;

import io.github.jason13official.summons.impl.common.entity.AbstractCompanion;
import java.util.EnumSet;
import java.util.function.BiConsumer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;

/// Generic ranged basic attack: closes to `attackRadius`, then fires `effect` on cooldown. Used by types whose basic attack isn't melee (Bird's arrows, Mage's zap).
public class CompanionRangedAttackGoal extends Goal {

  private final AbstractCompanion companion;
  private final double speed;
  private final int cooldownTicks;
  private final double attackRadius;
  private final BiConsumer<AbstractCompanion, LivingEntity> effect;
  private int cooldown;

  public CompanionRangedAttackGoal(AbstractCompanion companion, double speed, int cooldownTicks,
      double attackRadius, BiConsumer<AbstractCompanion, LivingEntity> effect) {
    this.companion = companion;
    this.speed = speed;
    this.cooldownTicks = cooldownTicks;
    this.attackRadius = attackRadius;
    this.effect = effect;
    this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
  }

  @Override
  public boolean canUse() {
    LivingEntity target = this.companion.getTarget();
    return target != null && target.isAlive();
  }

  @Override
  public boolean canContinueToUse() {
    return this.canUse();
  }

  @Override
  public void start() {
    this.cooldown = 0;
    this.companion.setAggressive(true);
  }

  @Override
  public void stop() {
    this.companion.getNavigation().stop();
    this.companion.setAggressive(false);
  }

  @Override
  public void tick() {
    LivingEntity target = this.companion.getTarget();
    if (target == null) {
      return;
    }

    this.companion.getLookControl().setLookAt(target, 30.0F, 30.0F);
    double radiusSqr = this.attackRadius * this.attackRadius;
    double distanceSqr = this.companion.distanceToSqr(target);

    if (distanceSqr > radiusSqr) {
      this.companion.getNavigation().moveTo(target, this.speed);
    } else {
      this.companion.getNavigation().stop();
    }

    if (this.cooldown > 0) {
      this.cooldown--;
      return;
    }

    if (distanceSqr <= radiusSqr && this.companion.hasLineOfSight(target)) {
      this.effect.accept(this.companion, target);
      this.cooldown = this.cooldownTicks;
    }
  }
}
