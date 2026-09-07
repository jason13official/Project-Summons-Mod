package io.github.jason13official.summons.impl.common.entity.flying;

import io.github.jason13official.summons.impl.common.entity.AbstractCompanion;
import io.github.jason13official.summons.impl.common.entity.ability.CompanionAbility;
import io.github.jason13official.summons.impl.common.entity.ground.AbstractGroundCompanion;
import java.util.List;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/// Mage-Type: physically weak, casts powerful area-of-effect spells (unimplemented).
public class MageSummon extends AbstractFlyingCompanion {

  private static final double SPELL_RADIUS = 12.0;

  private static final List<CompanionAbility> ABILITIES = List.of(
      new CompanionAbility("Lightning Strike", 30, (companion, owner) -> {
        if (!(companion.level() instanceof ServerLevel serverLevel)) {
          return;
        }

        LivingEntity target = findNearestTarget(companion, owner, SPELL_RADIUS);
        Vec3 strikeAt = target != null ? target.position() : owner.position();
        LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(serverLevel);
        if (bolt != null) {
          bolt.moveTo(strikeAt.x, strikeAt.y, strikeAt.z);
          serverLevel.addFreshEntity(bolt);
        }
      }),
      new CompanionAbility("Freeze", 30, (companion, owner) -> {
        LivingEntity target = findNearestTarget(companion, owner, SPELL_RADIUS);
        if (target == null) {
          return;
        }

        target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 3));
        target.hurt(companion.damageSources().mobAttack(companion),
            (float) companion.getAttributeValue(Attributes.ATTACK_DAMAGE) * 0.5F);
        spawnAbilityParticles(target, ParticleTypes.SNOWFLAKE, 10);
      })
  );

  public MageSummon(EntityType<? extends AbstractCompanion> entityType, Level level) {
    super(entityType, level);
  }

  public static AttributeSupplier.Builder createAttributes() {

    return AbstractFlyingCompanion.createAttributes().add(Attributes.MAX_HEALTH, (double) 12.0F)
        .add(Attributes.FLYING_SPEED, (double) 0.45F).add(Attributes.MOVEMENT_SPEED, (double) 0.28F)
        .add(Attributes.ATTACK_DAMAGE, (double) 10.0F).add(Attributes.KNOCKBACK_RESISTANCE, (double) 0.3F);
  }

  @Override
  protected List<CompanionAbility> abilities() {
    return ABILITIES;
  }
}
