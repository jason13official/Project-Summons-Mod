package io.github.jason13official.summons.impl.common.entity.ai.goal.movement;

import io.github.jason13official.summons.impl.common.entity.AbstractCompanion;
import java.util.EnumSet;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;

/// Keeps a companion near its owner. Replaces vanilla `FollowMobGoal`, which actually
/// follows any nearby *different-species* mob, not the owner (copied from Parrot) -> the
/// real reason companions wandered off and needed the teleport safety net so often.
public class CompanionFollowOwnerGoal extends Goal {
  private final AbstractCompanion companion;
  private final double speedModifier;
  private final float startDistance;
  private final float stopDistance;
  private int timeToRecalcPath;

  public CompanionFollowOwnerGoal(AbstractCompanion companion, double speedModifier, float startDistance, float stopDistance) {
    this.companion = companion;
    this.speedModifier = speedModifier;
    this.startDistance = startDistance;
    this.stopDistance = stopDistance;
    this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
  }

  @Override
  public boolean canUse() {
    LivingEntity owner = this.companion.getOwner();
    return owner != null && !this.companion.isLeashed() && !this.companion.isVehicle()
        && this.companion.distanceToSqr(owner) >= (double) (this.startDistance * this.startDistance);
  }

  @Override
  public boolean canContinueToUse() {
    LivingEntity owner = this.companion.getOwner();
    return owner != null && !this.companion.getNavigation().isDone()
        && this.companion.distanceToSqr(owner) > (double) (this.stopDistance * this.stopDistance);
  }

  @Override
  public void start() {
    this.timeToRecalcPath = 0;
  }

  @Override
  public void stop() {
    this.companion.getNavigation().stop();
  }

  @Override
  public void tick() {
    LivingEntity owner = this.companion.getOwner();
    if (owner == null) {
      return;
    }

    this.companion.getLookControl().setLookAt(owner, 10.0F, (float) this.companion.getMaxHeadXRot());
    if (--this.timeToRecalcPath <= 0) {
      this.timeToRecalcPath = this.adjustedTickDelay(10);
      this.companion.getNavigation().moveTo(owner, this.speedModifier);
    }
  }
}
