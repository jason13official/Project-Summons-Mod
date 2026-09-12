package io.github.jason13official.summons.impl.common.evolution;

import net.minecraft.world.item.Item;
import org.jetbrains.annotations.Nullable;

/// One evolution requirement; reaching `amount` of `color` alone triggers it, except a null
/// `color` ("Any"), which sums every color together instead. `requiredWeapon`, if set, must
/// also be the owner's held item.
public record EvolutionThreshold(@Nullable EvoCrystalColor color, int amount, EvolutionForm result,
                                  @Nullable Item requiredWeapon) {

  public EvolutionThreshold(@Nullable EvoCrystalColor color, int amount, EvolutionForm result) {
    this(color, amount, result, null);
  }

  public static EvolutionThreshold any(int amount, EvolutionForm result) {
    return new EvolutionThreshold(null, amount, result, null);
  }
}
