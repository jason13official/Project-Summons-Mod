package io.github.jason13official.summons.impl.common.evolution;

import org.jetbrains.annotations.Nullable;

/// One evolution requirement out of a specific [EvolutionForm]: reaching `amount` points of
/// `color` alone triggers it (colors are alternates, not combined); a null `color` ("Any")
/// sums every color together instead (e.g. Devil-Type's Gale -> Brow). A form can list
/// several thresholds as alternate routes to different results.
public record EvolutionThreshold(@Nullable EvoCrystalColor color, int amount, EvolutionForm result) {

  public static EvolutionThreshold any(int amount, EvolutionForm result) {
    return new EvolutionThreshold(null, amount, result);
  }
}
