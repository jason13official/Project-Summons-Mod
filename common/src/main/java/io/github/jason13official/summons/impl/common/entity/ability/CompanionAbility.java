package io.github.jason13official.summons.impl.common.entity.ability;

import io.github.jason13official.summons.impl.common.entity.AbstractCompanion;
import io.github.jason13official.summons.impl.common.evolution.EvolutionForm;
import java.util.Set;
import java.util.function.BiConsumer;
import net.minecraft.world.entity.LivingEntity;

/// A Command-mode ability: display name, how long it makes the companion busy, which
/// [EvolutionForm]s must have ever been reached to have learned it (empty = base kit,
/// always available), the minimum level to have learned it, and what it does. See
/// `AbstractCompanion#allAbilities`/`abilities`.
public record CompanionAbility(String name, int busyTicks, Set<EvolutionForm> requiredForms, int minLevel,
                                BiConsumer<AbstractCompanion, LivingEntity> effect) {

  public static CompanionAbility base(String name, int busyTicks, BiConsumer<AbstractCompanion, LivingEntity> effect) {
    return new CompanionAbility(name, busyTicks, Set.of(), 1, effect);
  }

  public static CompanionAbility gated(String name, int busyTicks, EvolutionForm requiredForm, int minLevel,
                                        BiConsumer<AbstractCompanion, LivingEntity> effect) {
    return new CompanionAbility(name, busyTicks, Set.of(requiredForm), minLevel, effect);
  }
}
