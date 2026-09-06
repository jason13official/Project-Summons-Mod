package io.github.jason13official.summons.impl.common.entity.ai.goal.target;

import io.github.jason13official.summons.impl.common.entity.AbstractCompanion;
import java.util.EnumSet;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;

public class CompanionOwnerHurtByTargetGoal extends TargetGoal {

  private final AbstractCompanion companion;
  private LivingEntity ownerLastHurtBy;
  private int timestamp;

  public CompanionOwnerHurtByTargetGoal(AbstractCompanion companion) {
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

    this.ownerLastHurtBy = owner.getLastHurtByMob();
    int i = owner.getLastHurtByMobTimestamp();
    return i != this.timestamp && this.canAttack(this.ownerLastHurtBy, TargetingConditions.DEFAULT);
  }

  @Override
  public void start() {
    this.mob.setTarget(this.ownerLastHurtBy);
    LivingEntity owner = this.companion.getOwner();
    if (owner != null) {
      this.timestamp = owner.getLastHurtByMobTimestamp();
    }

    super.start();
  }
}
