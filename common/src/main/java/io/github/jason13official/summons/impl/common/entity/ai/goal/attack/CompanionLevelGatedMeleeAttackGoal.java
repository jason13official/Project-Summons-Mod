package io.github.jason13official.summons.impl.common.entity.ai.goal.attack;

import io.github.jason13official.summons.impl.common.entity.AbstractCompanion;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;

/// [MeleeAttackGoal] that only engages once the companion has reached `minLevel`.
public class CompanionLevelGatedMeleeAttackGoal extends MeleeAttackGoal {

  private final AbstractCompanion companion;
  private final int minLevel;

  public CompanionLevelGatedMeleeAttackGoal(AbstractCompanion companion, double speed, boolean followEvenIfNotSeen,
                                             int minLevel) {
    super(companion, speed, followEvenIfNotSeen);
    this.companion = companion;
    this.minLevel = minLevel;
  }

  @Override
  public boolean canUse() {
    return this.companion.getLevel() >= this.minLevel && super.canUse();
  }

  @Override
  public boolean canContinueToUse() {
    return this.companion.getLevel() >= this.minLevel && super.canContinueToUse();
  }
}
