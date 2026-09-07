package io.github.jason13official.summons.impl.common.entity.flying;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;

/// Fairy-Type: support only, heals/cures the owner, does not attack.
/// TODO: real abilities are evolution gated (Infant Fairy only has Heal Lv.1); testing to try the HUD before evolution impl
public class FairySummon extends AbstractFlyingCompanion {

  private static final int HEAL_1 = 0;
  private static final int TIME_HEAL = 1;
  private static final int HEAL_2 = 2;
  private static final int ANTIDOTE = 3;

  public FairySummon(EntityType<? extends AbstractFlyingCompanion> entityType, Level level) {
    super(entityType, level);
  }

  public static AttributeSupplier.Builder createAttributes() {

    return AbstractFlyingCompanion.createAttributes().add(Attributes.MAX_HEALTH, (double) 6.0F)
        .add(Attributes.FLYING_SPEED, (double) 0.5F).add(Attributes.MOVEMENT_SPEED, (double) 0.3F)
        .add(Attributes.ATTACK_DAMAGE, (double) 0.0F);
  }

  @Override
  public int getAbilityCount() {
    return 4;
  }

  @Override
  public String getAbilityName(int index) {
    return switch (index) {
      case HEAL_1 -> "Heal Lv.1";
      case TIME_HEAL -> "Time Heal";
      case HEAL_2 -> "Heal Lv.2";
      case ANTIDOTE -> "Antidote";
      default -> super.getAbilityName(index);
    };
  }

  @Override
  protected void useAbility(int index) {
    LivingEntity owner = this.getOwner();
    if (owner == null) {
      return;
    }

    switch (index) {
      case HEAL_1 -> {
        owner.heal(2.0F);
        this.setAbilityBusy(20);
      }
      case TIME_HEAL -> {
        owner.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 100, 1)); // ~ 4.0F
        this.setAbilityBusy(100);
      }
      case HEAL_2 -> {
        owner.heal(6.0F);
        this.setAbilityBusy(30);
      }
      case ANTIDOTE -> {
        owner.removeEffect(MobEffects.POISON);
        owner.removeEffect(MobEffects.WITHER);
        this.setAbilityBusy(20);
      }
      default -> {
        return;
      }
    }

    if (this.level() instanceof ServerLevel serverLevel) {
      serverLevel.sendParticles(ParticleTypes.HEART, owner.getX(), owner.getY() + owner.getBbHeight(), owner.getZ(),
          5, 0.3, 0.3, 0.3, 0.0);
    }
  }
}
