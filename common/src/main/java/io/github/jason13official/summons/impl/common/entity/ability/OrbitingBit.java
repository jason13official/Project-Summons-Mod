package io.github.jason13official.summons.impl.common.entity.ability;

import net.minecraft.core.particles.ParticleOptions;

/// A single "B"-spell bit (Mage's Floating B/Satellite B/etc): orbits the owner's position,
/// damaging living entities it passes through. Simulated per-tick (position + hit-testing),
/// not a real registered Entity -> avoids new registry/renderer infra for a short-lived VFX.
public final class OrbitingBit {
  public double angle;
  public final double radius;
  public final double angularSpeed;
  public final double heightOffset;
  public final float damage;
  public final ParticleOptions particle;
  public final boolean explodeOnHit;
  public int lifespanTicks;
  public int hitCooldown;

  public OrbitingBit(double angle, double radius, double angularSpeed, double heightOffset,
                      float damage, ParticleOptions particle, boolean explodeOnHit, int lifespanTicks) {
    this.angle = angle;
    this.radius = radius;
    this.angularSpeed = angularSpeed;
    this.heightOffset = heightOffset;
    this.damage = damage;
    this.particle = particle;
    this.explodeOnHit = explodeOnHit;
    this.lifespanTicks = lifespanTicks;
  }
}
