package io.github.jason13official.summons.impl.common.entity;

/// Per-type passive owner buff, from the wiki's own formula:
/// `total = initial + growth * (level-1) / 98` (level 99 = growth fully applied).
/// STR -> owner ATTACK_DAMAGE (real attribute modifier); CON -> % duration reduction on
/// harmful effects; LCK -> % chance to duplicate a kill's drop. Neither CON nor LCK map to
/// a vanilla attribute, so they're read on demand by mixins instead (no attribute to hold).
public record OwnerStatBonusKit(double strInitial, double strGrowth, double conInitial, double conGrowth,
                                 double lckInitial, double lckGrowth) {

  public static final OwnerStatBonusKit NONE = new OwnerStatBonusKit(0, 0, 0, 0, 0, 0);

  public double str(int level) {
    return this.strInitial + this.strGrowth * (level - 1) / 98.0;
  }

  public double con(int level) {
    return this.conInitial + this.conGrowth * (level - 1) / 98.0;
  }

  public double lck(int level) {
    return this.lckInitial + this.lckGrowth * (level - 1) / 98.0;
  }
}
