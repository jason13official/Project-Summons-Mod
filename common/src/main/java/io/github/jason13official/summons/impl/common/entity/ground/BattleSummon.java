package io.github.jason13official.summons.impl.common.entity.ground;

import io.github.jason13official.summons.impl.common.entity.AbstractCompanion;
import io.github.jason13official.summons.impl.common.entity.ability.CompanionAbility;
import java.util.List;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

/// Battle-Type: physically strong, lots of health and hits heavy
public class BattleSummon extends AbstractGroundCompanion {

  private static final double AURA_BLAST_RADIUS = 3.0;

  private static final List<CompanionAbility> ABILITIES = List.of(
      new CompanionAbility("Aura Blast", 20, (companion, owner) -> {
        AABB area = companion.getBoundingBox().inflate(AURA_BLAST_RADIUS);
        float damage = (float) companion.getAttributeValue(Attributes.ATTACK_DAMAGE);

        for (LivingEntity target : companion.level().getEntitiesOfClass(LivingEntity.class, area,
            e -> e != companion && e != owner && e.isAlive())) {
          target.hurt(companion.damageSources().mobAttack(companion), damage);
        }

        spawnAbilityParticles(companion, ParticleTypes.EXPLOSION, 2);
      }),
      new CompanionAbility("Hip Press", 30, (companion, owner) -> {
        LivingEntity target = findNearestTarget(companion, owner, AURA_BLAST_RADIUS);
        if (target == null) {
          return;
        }

        float damage = (float) companion.getAttributeValue(Attributes.ATTACK_DAMAGE) * 1.5F;
        target.hurt(companion.damageSources().mobAttack(companion), damage);
        target.knockback(0.8, companion.getX() - target.getX(), companion.getZ() - target.getZ());
        spawnAbilityParticles(target, ParticleTypes.CRIT, 8);
      })
  );

  public BattleSummon(EntityType<? extends AbstractCompanion> entityType, Level level) {
    super(entityType, level);
  }

  public static AttributeSupplier.Builder createAttributes() {
    return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, (double)100.0F).add(Attributes.MOVEMENT_SPEED, (double)0.25F).add(Attributes.KNOCKBACK_RESISTANCE, (double)1.0F).add(Attributes.ATTACK_DAMAGE, (double)15.0F).add(Attributes.STEP_HEIGHT, (double)1.0F);
  }

  @Override
  protected List<CompanionAbility> abilities() {
    return ABILITIES;
  }
}
