package io.github.jason13official.summons.impl.common.entity;

/// Per-type passive owner buff: `total = initial + growth * (level-1) / 98` (wiki formula). STR -> owner ATTACK_DAMAGE; CON -> % harmful-effect duration cut; LCK -> % chance to duplicate a kill's
/// drop. Only STR maps to a real attribute; CON/LCK read on demand.
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
