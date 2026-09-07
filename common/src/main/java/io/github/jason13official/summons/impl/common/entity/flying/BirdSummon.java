package io.github.jason13official.summons.impl.common.entity.flying;

import io.github.jason13official.summons.impl.common.entity.ability.CompanionAbility;
import java.util.List;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;

/// Bird-Type: air mobility, lifts/carries the owner and juggles light enemies.
/// TODO: "Glide" (carry Hector over gaps) mimicked with Slow Falling (for now); gliding is a movement/input feature and not a Command-mode effect so heavy WIP
public class BirdSummon extends AbstractFlyingCompanion {

  private static final List<CompanionAbility> ABILITIES = List.of(
      new CompanionAbility("Glide", 100, (companion, owner) -> {
        owner.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 100));
        spawnAbilityParticles(owner, ParticleTypes.CLOUD, 8);
      })
  );

  public BirdSummon(EntityType<? extends AbstractFlyingCompanion> entityType, Level level) {
    super(entityType, level);
  }

  public static AttributeSupplier.Builder createAttributes() {

    return AbstractFlyingCompanion.createAttributes().add(Attributes.MAX_HEALTH, (double) 14.0F)
        .add(Attributes.FLYING_SPEED, (double) 0.5F).add(Attributes.MOVEMENT_SPEED, (double) 0.25F)
        .add(Attributes.ATTACK_DAMAGE, (double) 4.0F);
  }

  @Override
  protected List<CompanionAbility> abilities() {
    return ABILITIES;
  }
}
