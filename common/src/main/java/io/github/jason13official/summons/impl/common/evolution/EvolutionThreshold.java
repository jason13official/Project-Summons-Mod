package io.github.jason13official.summons.impl.common.evolution;

import org.jetbrains.annotations.Nullable;

/// One evolution requirement: reaching `amount` points of `color` alone triggers it (colors
/// are alternates, not combined); except a null `color` ("Any"), which sums every color
/// together instead, per the wiki (i.e. Devil-type). A companion can list
/// several as alternate routes (different colors/totals leading to different `resultName`s).
public record EvolutionThreshold(@Nullable EvoCrystalColor color, int amount, String resultName) {

  public static EvolutionThreshold any(int amount, String resultName) {
    return new EvolutionThreshold(null, amount, resultName);
  }
}
