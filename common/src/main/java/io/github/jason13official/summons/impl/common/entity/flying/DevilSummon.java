package io.github.jason13official.summons.impl.common.entity.flying;

import io.github.jason13official.summons.impl.common.entity.ability.CompanionAbility;
import io.github.jason13official.summons.impl.common.evolution.EvolutionForm;
import io.github.jason13official.summons.impl.common.evolution.EvolutionThreshold;
import java.util.List;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

/// Devil-Type: balance of Battle- and Bird-Type, high mobility and attack power, aggressive.
/// TODO: "Magic Circle" (lets Hector slide under low openings) proxy with a knockback pulse for now, since the real ability is traversal, heavy WIP
public class DevilSummon extends AbstractFlyingCompanion {

  private static final double MAGIC_CIRCLE_RADIUS = 3.0;
  private static final double MAGIC_CIRCLE_KNOCKBACK = 0.6;

  private static final List<CompanionAbility> ABILITIES = List.of(
      CompanionAbility.base("Magic Circle", 20, (companion, owner) -> {
        AABB area = companion.getBoundingBox().inflate(MAGIC_CIRCLE_RADIUS);
        float damage = (float) companion.getAttributeValue(Attributes.ATTACK_DAMAGE) * 0.5F;

        for (LivingEntity target : companion.level().getEntitiesOfClass(LivingEntity.class, area,
            e -> e != companion && e != owner && e.isAlive())) {
          target.hurt(companion.damageSources().mobAttack(companion), damage);
          target.knockback(MAGIC_CIRCLE_KNOCKBACK, companion.getX() - target.getX(), companion.getZ() - target.getZ());
        }

        spawnAbilityParticles(companion, ParticleTypes.PORTAL, 12);
      }),
      // wiki: Brow gets "M. Circle Scissors, Needle Magic Circle"
      CompanionAbility.gated("Needle Magic Circle", 30, Form.BROW, 5, (companion, owner) -> {
        LivingEntity target = findNearestTarget(companion, owner, MAGIC_CIRCLE_RADIUS * 2.0);
        if (target == null) {
          return;
        }

        float damage = (float) companion.getAttributeValue(Attributes.ATTACK_DAMAGE) * 1.5F;
        target.hurt(companion.damageSources().mobAttack(companion), damage);
        spawnAbilityParticles(target, ParticleTypes.PORTAL, 6);
      })
  );

  public DevilSummon(EntityType<? extends AbstractFlyingCompanion> entityType, Level level) {
    super(entityType, level);
  }

  public static AttributeSupplier.Builder createAttributes() {

    return AbstractFlyingCompanion.createAttributes().add(Attributes.MAX_HEALTH, (double) 40.0F)
        .add(Attributes.FLYING_SPEED, (double) 0.45F).add(Attributes.MOVEMENT_SPEED, (double) 0.28F)
        .add(Attributes.ATTACK_DAMAGE, (double) 10.0F).add(Attributes.KNOCKBACK_RESISTANCE, (double) 0.3F);
  }

  @Override
  protected void registerGoals() {
    super.registerGoals();
    this.goalSelector.addGoal(4, new MeleeAttackGoal(this, 1.3, true));
  }

  @Override
  protected List<CompanionAbility> allAbilities() {
    return ABILITIES;
  }

  @Override
  protected EvolutionForm baseForm() {
    return Form.GALE;
  }

  @Override
  protected EvolutionForm resolveForm(String id) {
    try {
      return Form.valueOf(id);
    } catch (IllegalArgumentException e) {
      return Form.GALE;
    }
  }

  @Override
  protected List<EvolutionThreshold> evolutionThresholds() {
    return switch ((Form) this.getEvolutionForm()) {
      // Gale -> Brow: 200 of any color combined (not alternates; the wiki's one example of a
      // non-branching, cumulative-across-colors threshold)
      case GALE -> List.of(EvolutionThreshold.any(200, Form.BROW));
      // Brow -> The End needs the Chauve-souris weapon, not just crystals; we have no
      // item-requirement infra or that weapon yet, so this stays unreachable for now
      default -> List.of();
    };
  }

  /// Innocent Devil Data (Devil-Types); the only type with a single, non-branching line
  public enum Form implements EvolutionForm {
    GALE("Gale", 0),
    BROW("Brow", 1),
    THE_END("The End", 2);

    private final String displayName;
    private final int stage;

    Form(String displayName, int stage) {
      this.displayName = displayName;
      this.stage = stage;
    }

    @Override
    public String id() {
      return this.name();
    }

    @Override
    public String displayName() {
      return this.displayName;
    }

    @Override
    public int stage() {
      return this.stage;
    }
  }
}
