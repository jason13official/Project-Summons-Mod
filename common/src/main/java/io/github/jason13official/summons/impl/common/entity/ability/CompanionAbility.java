package io.github.jason13official.summons.impl.common.entity.ability;

import io.github.jason13official.summons.impl.common.entity.AbstractCompanion;
import java.util.function.BiConsumer;
import net.minecraft.world.entity.LivingEntity;

/// A Command-mode ability: display name, how long it makes the companion busy, and
/// what it does. Companions expose their kit/abilities via `AbstractCompanion#abilities()`.
public record CompanionAbility(String name, int busyTicks, BiConsumer<AbstractCompanion, LivingEntity> effect) {
}
