package io.github.jason13official.summons.impl.common.entity.ai.goal.attack;

import io.github.jason13official.summons.impl.common.entity.AbstractCompanion;
import java.util.EnumSet;
import java.util.function.BiConsumer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;

/// Physical swoop/dive-bomb basic attack for flying types: closes on the target directly
/// (no ground pathfinding) and fires `onHit` on contact, cooldown-gated.
public class CompanionDiveAttackGoal extends Goal {

  private final AbstractCompanion companion;
  private final double speed;
  private final double hitRadius;
  private final int hitCooldownTicks;
  private final BiConsumer<AbstractCompanion, LivingEntity> onHit;
  private int cooldown;

  public CompanionDiveAttackGoal(AbstractCompanion companion, double speed, double hitRadius, int hitCooldownTicks,
                                  BiConsumer<AbstractCompanion, LivingEntity> onHit) {
    this.companion = companion;
    this.speed = speed;
    this.hitRadius = hitRadius;
    this.hitCooldownTicks = hitCooldownTicks;
    this.onHit = onHit;
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
    this.companion.setAggressive(false);
    this.companion.getNavigation().stop();
  }

  @Override
  public void tick() {
    LivingEntity target = this.companion.getTarget();
    if (target == null) {
      return;
    }

    this.companion.getLookControl().setLookAt(target, 30.0F, 30.0F);
    this.companion.getNavigation().moveTo(target, this.speed);

    if (this.cooldown > 0) {
      this.cooldown--;
      return;
    }

    if (this.companion.distanceTo(target) <= this.hitRadius) {
      this.onHit.accept(this.companion, target);
      this.cooldown = this.hitCooldownTicks;
    }
  }
}
