package io.github.jason13official.summons.impl.common.entity;

import java.util.List;
import net.minecraft.world.entity.LivingEntity;

/// Mage's "Shield" ability: "Blocks one enemy attack completely" -> a real one-hit block instead of the old Absorption proxy. Arms on cast, consumed by
/// LivingEntityShieldMixin on the owner's next non-bypassing hit, however long that takes (not a timed buff).
final class CompanionShield {

  private CompanionShield() {
  }

  static void arm(AbstractCompanion companion) {
    companion.ownerShieldArmed = true;
  }

  /// finds a still-armed Shield near `owner` and consumes it; true means the incoming hit should be fully blocked
  static boolean consume(LivingEntity owner) {
    List<AbstractCompanion> armed = owner.level().getEntitiesOfClass(AbstractCompanion.class,
        owner.getBoundingBox().inflate(16.0),
        companion -> companion.ownerShieldArmed && owner.getUUID().equals(companion.getOwnerUUID()));

    if (armed.isEmpty()) {
      return false;
    }

    armed.get(0).ownerShieldArmed = false;
    return true;
  }
}
