package io.github.jason13official.summons.impl.common.entity;

import io.github.jason13official.summons.impl.common.party.CompanionMode;
import java.util.List;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.LivingEntity;

/// DEFEND-mode Guard Field: shrinks per hit, regenerates over time, blocks damage to the
/// companion itself and (via #isProtecting) to an owner standing inside it. The radius
/// entityData + its get/set stay on AbstractCompanion (thin SynchedEntityData wrapper also
/// read by GuardFieldRenderer); this is the surrounding business logic.
final class CompanionGuardField {
  private static final float SHRINK_PER_HIT = 0.75F;
  private static final float REGEN_PER_TICK = 0.01F; // ~4.5s min->max

  private CompanionGuardField() {
  }

  static void shrink(AbstractCompanion companion) {
    companion.setGuardFieldRadius(companion.getGuardFieldRadius() - SHRINK_PER_HIT);
  }

  /// call every tick; regenerates while in DEFEND, otherwise snaps back to full so
  /// re-entering DEFEND always starts with a fresh field
  static void tick(AbstractCompanion companion) {
    if (companion.getMode() == CompanionMode.DEFEND) {
      if (companion.getGuardFieldRadius() < AbstractCompanion.GUARD_FIELD_MAX_RADIUS) {
        companion.setGuardFieldRadius(companion.getGuardFieldRadius() + REGEN_PER_TICK);
      }
    } else if (companion.getGuardFieldRadius() != AbstractCompanion.GUARD_FIELD_MAX_RADIUS) {
      companion.setGuardFieldRadius(AbstractCompanion.GUARD_FIELD_MAX_RADIUS);
    }
  }

  /// light rising around the Guard Field edge, plus a few inside it (client-side only)
  static void spawnParticles(AbstractCompanion companion) {
    float radius = companion.getGuardFieldRadius();

    if (companion.getRandom().nextInt(4) == 0) {
      spawnParticle(companion, radius);
    }

    if (companion.getRandom().nextInt(8) == 0) {
      // sqrt so points land uniformly across the disc's area, not bunched at the center
      spawnParticle(companion, radius * (float) Math.sqrt(companion.getRandom().nextDouble()));
    }
  }

  private static void spawnParticle(AbstractCompanion companion, float distanceFromCenter) {
    double angle = companion.getRandom().nextDouble() * Math.PI * 2.0;
    double x = companion.getX() + Math.cos(angle) * distanceFromCenter;
    double z = companion.getZ() + Math.sin(angle) * distanceFromCenter;
    companion.level().addParticle(ParticleTypes.END_ROD, x, companion.getY() + 0.05, z, 0.0, 0.03, 0.0);
  }

  /// protects an owner standing inside a DEFEND-mode companion's field; also shrinks it,
  /// same as a direct hit would
  static boolean isProtecting(LivingEntity owner) {
    List<AbstractCompanion> nearby = owner.level().getEntitiesOfClass(AbstractCompanion.class,
        owner.getBoundingBox().inflate(AbstractCompanion.GUARD_FIELD_MAX_RADIUS),
        companion -> companion.getMode() == CompanionMode.DEFEND && owner.getUUID().equals(companion.getOwnerUUID()));

    for (AbstractCompanion companion : nearby) {
      if (companion.distanceTo(owner) <= companion.getGuardFieldRadius()) {
        shrink(companion);
        return true;
      }
    }

    return false;
  }
}
