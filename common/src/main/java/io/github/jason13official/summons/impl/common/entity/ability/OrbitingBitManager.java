package io.github.jason13official.summons.impl.common.entity.ability;

import io.github.jason13official.summons.impl.common.entity.AbstractCompanion;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;

/// Owns the short-lived "orbiting bit" VFX+hitboxes a companion can spawn (Mage's "B" spells, such as Floating B, Satellite B, etc). One instance per companion, held as a field rather than static,
/// since bits belong to whichever companion cast the ability.
public final class OrbitingBitManager {

  private final List<OrbitingBit> bits = new ArrayList<>();

  public void spawn(int count, double radius, double angularSpeed, double heightOffset,
      float damage, ParticleOptions particle, boolean explodeOnHit, int lifespanTicks) {
    for (int i = 0; i < count; i++) {
      double startAngle = (2.0 * Math.PI / count) * i;
      this.bits.add(new OrbitingBit(startAngle, radius, angularSpeed, heightOffset, damage, particle, explodeOnHit, lifespanTicks));
    }
  }

  /// ticks every bit's orbit/particles/contact damage around `companion`'s owner
  public void tick(AbstractCompanion companion) {
    if (this.bits.isEmpty()) {
      return;
    }

    LivingEntity owner = companion.getOwner();
    if (owner == null) {
      this.bits.clear();
      return;
    }

    ServerLevel serverLevel = companion.level() instanceof ServerLevel level ? level : null;
    Iterator<OrbitingBit> iterator = this.bits.iterator();
    while (iterator.hasNext()) {
      OrbitingBit bit = iterator.next();
      bit.angle += bit.angularSpeed;
      bit.lifespanTicks--;
      if (bit.hitCooldown > 0) {
        bit.hitCooldown--;
      }

      double x = owner.getX() + Math.cos(bit.angle) * bit.radius;
      double y = owner.getY() + bit.heightOffset;
      double z = owner.getZ() + Math.sin(bit.angle) * bit.radius;

      if (serverLevel != null) {
        serverLevel.sendParticles(bit.particle, x, y, z, 1, 0.0, 0.0, 0.0, 0.0);
      }

      if (bit.hitCooldown == 0) {
        AABB hitBox = new AABB(x - 0.5, y - 0.5, z - 0.5, x + 0.5, y + 0.5, z + 0.5);
        for (LivingEntity target : companion.level().getEntitiesOfClass(LivingEntity.class, hitBox,
            e -> e != owner && e != companion && e.isAlive())) {
          target.hurt(companion.damageSources().magic(), bit.damage);
          target.knockback(0.3, x - target.getX(), z - target.getZ());
          bit.hitCooldown = 10;
          if (bit.explodeOnHit) {
            bit.lifespanTicks = 0;
          }
          break;
        }
      }

      if (bit.lifespanTicks <= 0) {
        iterator.remove();
      }
    }
  }
}
