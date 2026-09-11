package io.github.jason13official.summons.impl.common.entity;

import io.github.jason13official.summons.impl.common.evolution.EvoCrystalColor;
import io.github.jason13official.summons.impl.common.evolution.EvolutionThreshold;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

/// Evo Crystal spending + evolution-form checks. The crystal-point/form entityData itself
/// stays on AbstractCompanion (thin SynchedEntityData wrappers, and baseForm/resolveForm/
/// evolutionThresholds are per-type override hooks); this is the multi-step logic around it.
final class CompanionEvolution {

  private CompanionEvolution() {
  }

  /// TODO: no evolved-form entities/models exist yet, so this just updates the form, spends
  /// the points, and announces the result.
  static void checkEvolution(AbstractCompanion companion) {
    for (EvolutionThreshold threshold : companion.evolutionThresholds()) {
      int available = threshold.color() != null ? companion.getCrystalPoints(threshold.color()) : totalCrystalPoints(companion);
      if (available < threshold.amount()) {
        continue;
      }

      LivingEntity owner = companion.getOwner();
      if (threshold.requiredWeapon() != null
          && (owner == null || owner.getMainHandItem().getItem() != threshold.requiredWeapon())) {
        continue;
      }

      if (threshold.color() != null) {
        companion.setCrystalPoints(threshold.color(), available - threshold.amount());
      } else {
        spendCrystalPointsAcrossColors(companion, threshold.amount());
      }

      companion.setEvolutionForm(threshold.result());
      companion.markFormReached(threshold.result());

      if (owner instanceof Player player) {
        player.displayClientMessage(Component.literal(companion.getCompanionType().name() + "-Type evolved into "
            + threshold.result().displayName() + "! (not yet implemented visually)"), false);
      }
      return;
    }
  }

  private static int totalCrystalPoints(AbstractCompanion companion) {
    int total = 0;
    for (EvoCrystalColor color : EvoCrystalColor.values()) {
      total += companion.getCrystalPoints(color);
    }
    return total;
  }

  /// drains `amount` across colors in a fixed order; only used by "Any" thresholds
  private static void spendCrystalPointsAcrossColors(AbstractCompanion companion, int amount) {
    for (EvoCrystalColor color : EvoCrystalColor.values()) {
      if (amount <= 0) {
        break;
      }

      int have = companion.getCrystalPoints(color);
      int take = Math.min(have, amount);
      companion.setCrystalPoints(color, have - take);
      amount -= take;
    }
  }
}
