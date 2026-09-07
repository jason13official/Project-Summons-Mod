package io.github.jason13official.summons.impl.common.entity.ground;

import io.github.jason13official.summons.impl.common.entity.AbstractCompanion;
import io.github.jason13official.summons.impl.common.entity.ability.CompanionAbility;
import java.util.List;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

/// Pumpkin-Type: bad at fighting and low health, but a big stat boost to the owner
/// (owner stat bonuses not implemented yet).
public class PumpkinSummon extends AbstractGroundCompanion {

  private static final double POSE_RADIUS = 2.0;

  /// "They only have one ability, Pose, which inflicts minimum damage to nearby enemies,
  /// although Pumpkin will be completely vulnerable while performing it."
  private static final List<CompanionAbility> ABILITIES = List.of(
      new CompanionAbility("Pose", 40, (companion, owner) -> {
        AABB area = companion.getBoundingBox().inflate(POSE_RADIUS);
        float damage = (float) companion.getAttributeValue(Attributes.ATTACK_DAMAGE);

        for (LivingEntity target : companion.level().getEntitiesOfClass(LivingEntity.class, area,
            e -> e != companion && e != owner && e.isAlive())) {
          target.hurt(companion.damageSources().mobAttack(companion), damage);
        }

        spawnAbilityParticles(companion, ParticleTypes.POOF, 8);
      })
  );

  public PumpkinSummon(EntityType<? extends AbstractCompanion> entityType, Level level) {
    super(entityType, level);
  }

  public static AttributeSupplier.Builder createAttributes() {

    return AbstractCompanion.createAttributes().add(Attributes.MAX_HEALTH, (double) 8.0F)
        .add(Attributes.MOVEMENT_SPEED, (double) 0.18F).add(Attributes.ATTACK_DAMAGE, (double) 1.0F);
  }

  @Override
  protected List<CompanionAbility> abilities() {
    return ABILITIES;
  }
}
