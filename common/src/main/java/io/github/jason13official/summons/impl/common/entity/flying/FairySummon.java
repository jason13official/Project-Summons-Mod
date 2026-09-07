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

/// Fairy-Type: support only, heals/cures the owner, does not attack.
/// TODO: real abilities are evolution gated (Infant Fairy only has Heal Lv.1); testing to try the HUD before evolution impl
public class FairySummon extends AbstractFlyingCompanion {

  private static final List<CompanionAbility> ABILITIES = List.of(
      new CompanionAbility("Heal Lv.1", 20, (companion, owner) -> {
        owner.heal(2.0F);
        spawnAbilityParticles(owner, ParticleTypes.HEART, 5);
      }),
      new CompanionAbility("Time Heal", 100, (companion, owner) -> {
        owner.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 100, 1)); // ~4.0F over the duration
        spawnAbilityParticles(owner, ParticleTypes.HEART, 5);
      }),
      new CompanionAbility("Heal Lv.2", 30, (companion, owner) -> {
        owner.heal(6.0F);
        spawnAbilityParticles(owner, ParticleTypes.HEART, 5);
      }),
      new CompanionAbility("Antidote", 20, (companion, owner) -> {
        owner.removeEffect(MobEffects.POISON);
        owner.removeEffect(MobEffects.WITHER);
        spawnAbilityParticles(owner, ParticleTypes.HEART, 5);
      })
  );

  public FairySummon(EntityType<? extends AbstractFlyingCompanion> entityType, Level level) {
    super(entityType, level);
  }

  public static AttributeSupplier.Builder createAttributes() {

    return AbstractFlyingCompanion.createAttributes().add(Attributes.MAX_HEALTH, (double) 6.0F)
        .add(Attributes.FLYING_SPEED, (double) 0.5F).add(Attributes.MOVEMENT_SPEED, (double) 0.3F)
        .add(Attributes.ATTACK_DAMAGE, (double) 0.0F);
  }

  @Override
  protected List<CompanionAbility> abilities() {
    return ABILITIES;
  }
}
