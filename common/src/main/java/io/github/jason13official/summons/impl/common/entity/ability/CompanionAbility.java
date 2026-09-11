package io.github.jason13official.summons.impl.common.entity.ability;

import io.github.jason13official.summons.impl.common.entity.AbstractCompanion;
import io.github.jason13official.summons.impl.common.evolution.EvolutionForm;
import java.util.Set;
import java.util.function.BiConsumer;
import net.minecraft.world.entity.LivingEntity;

/// A Command-mode ability; `requiredForms` empty means base kit, always available.
/// `description` is flavor text for the Companion screen, straight from the wiki.
/// See `AbstractCompanion#allAbilities`/`abilities`.
public record CompanionAbility(String name, int busyTicks, Set<EvolutionForm> requiredForms, int minLevel,
                                String description, BiConsumer<AbstractCompanion, LivingEntity> effect) {

  public static CompanionAbility base(String name, int busyTicks, String description,
                                       BiConsumer<AbstractCompanion, LivingEntity> effect) {
    return new CompanionAbility(name, busyTicks, Set.of(), 1, description, effect);
  }

  public static CompanionAbility gated(String name, int busyTicks, EvolutionForm requiredForm, int minLevel,
                                        String description, BiConsumer<AbstractCompanion, LivingEntity> effect) {
    return new CompanionAbility(name, busyTicks, Set.of(requiredForm), minLevel, description, effect);
  }
}
