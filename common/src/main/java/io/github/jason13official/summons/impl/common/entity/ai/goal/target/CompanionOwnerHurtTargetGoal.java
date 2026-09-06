package io.github.jason13official.summons.impl.common.entity.ai.goal.target;

import io.github.jason13official.summons.impl.common.entity.AbstractCompanion;
import java.util.EnumSet;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;

public class CompanionOwnerHurtTargetGoal extends TargetGoal {

  private final AbstractCompanion companion;
  private LivingEntity ownerLastHurt;
  private int timestamp;

  public CompanionOwnerHurtTargetGoal(AbstractCompanion companion) {
    super(companion, false);
    this.companion = companion;
    this.setFlags(EnumSet.of(Goal.Flag.TARGET));
  }

  @Override
  public boolean canUse() {
    LivingEntity owner = this.companion.getOwner();
    if (owner == null) {
      return false;
    }

    this.ownerLastHurt = owner.getLastHurtMob();
    int i = owner.getLastHurtMobTimestamp();
    return i != this.timestamp && this.canAttack(this.ownerLastHurt, TargetingConditions.DEFAULT);
  }

  @Override
  public void start() {
    this.mob.setTarget(this.ownerLastHurt);
    LivingEntity owner = this.companion.getOwner();
    if (owner != null) {
      this.timestamp = owner.getLastHurtMobTimestamp();
    }

    super.start();
  }
}
