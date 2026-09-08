package io.github.jason13official.summons.impl.common.evolution;

import org.jetbrains.annotations.Nullable;

/// One evolution requirement; reaching `amount` of `color` alone triggers it (alternates,
/// not combined), except a null `color` ("Any"), which sums every color together instead.
public record EvolutionThreshold(@Nullable EvoCrystalColor color, int amount, EvolutionForm result) {

  public static EvolutionThreshold any(int amount, EvolutionForm result) {
    return new EvolutionThreshold(null, amount, result);
  }
}
