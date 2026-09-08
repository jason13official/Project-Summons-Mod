package io.github.jason13official.summons.impl.common.entity.flying;

import io.github.jason13official.summons.Summons;
import io.github.jason13official.summons.impl.common.entity.OwnerStatBonusKit;
import io.github.jason13official.summons.impl.common.entity.ability.CompanionAbility;
import io.github.jason13official.summons.impl.common.evolution.EvolutionForm;
import io.github.jason13official.summons.impl.common.evolution.EvolutionThreshold;
import java.util.List;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

/// Devil-Type: balance of Battle- and Bird-Type, high mobility and attack power, aggressive.
public class DevilSummon extends AbstractFlyingCompanion {

  // wiki: STR +4/CON +2/LCK +1 initial, +14/+4/+5 growth
  private static final OwnerStatBonusKit OWNER_STAT_BONUS = new OwnerStatBonusKit(4, 14, 2, 4, 1, 5);

  private static final double MAGIC_CIRCLE_RADIUS = 3.0;
  // wiki: Magic Circle turns Hector (and the I.D.) into a magic circle to slide under low
  // gaps -> proxied as temporarily shrinking the owner via the SCALE attribute instead of a
  // real traversal mechanic
  private static final int MAGIC_CIRCLE_DURATION = 100;
  private static final double MAGIC_CIRCLE_SCALE_DELTA = -0.5;
  private static final ResourceLocation MAGIC_CIRCLE_SCALE_MODIFIER_ID = Summons.identifier("magic_circle_scale");

  private int magicCircleTicksRemaining;

  private static final List<CompanionAbility> ABILITIES = List.of(
      CompanionAbility.base("Magic Circle", 30, (companion, owner) -> {
        AttributeInstance scale = owner.getAttribute(Attributes.SCALE);
        if (scale != null) {
          scale.addOrUpdateTransientModifier(
              new AttributeModifier(MAGIC_CIRCLE_SCALE_MODIFIER_ID, MAGIC_CIRCLE_SCALE_DELTA, AttributeModifier.Operation.ADD_VALUE));
        }

        ((DevilSummon) companion).magicCircleTicksRemaining = MAGIC_CIRCLE_DURATION;
        spawnAbilityParticles(owner, ParticleTypes.PORTAL, 12);
      }),
      // Devil-Type FAQ: Brow gets "Scissor M. Circle, Needle M. Circle"
      CompanionAbility.gated("Needle M. Circle", 30, Form.BROW, 5, (companion, owner) -> {
        LivingEntity target = findNearestTarget(companion, owner, MAGIC_CIRCLE_RADIUS * 2.0);
        if (target == null) {
          return;
        }

        float damage = (float) companion.getAttributeValue(Attributes.ATTACK_DAMAGE) * 1.5F;
        target.hurt(companion.damageSources().mobAttack(companion), damage);
        spawnAbilityParticles(target, ParticleTypes.PORTAL, 6);
      }),
      // wider AoE, two hits each
      CompanionAbility.gated("Scissor M. Circle", 25, Form.BROW, 8, (companion, owner) -> {
        AABB area = companion.getBoundingBox().inflate(MAGIC_CIRCLE_RADIUS * 1.5);
        float damage = (float) companion.getAttributeValue(Attributes.ATTACK_DAMAGE) * 0.75F;

        for (LivingEntity target : companion.level().getEntitiesOfClass(LivingEntity.class, area,
            e -> e != companion && e != owner && e.isAlive())) {
          target.hurt(companion.damageSources().mobAttack(companion), damage);
          target.hurt(companion.damageSources().mobAttack(companion), damage);
        }

        spawnAbilityParticles(companion, ParticleTypes.PORTAL, 16);
      })
      // wiki: The End's "Exploding M. Circle" needs the Chauve-souris weapon and The End is
      // already permanently unreachable (no item-requirement infra) -> intentionally not added
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
    this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.3, true));
  }

  @Override
  public void tick() {
    super.tick();
    if (this.level().isClientSide) {
      return;
    }

    if (this.magicCircleTicksRemaining > 0 && --this.magicCircleTicksRemaining == 0) {
      LivingEntity owner = this.getOwner();
      AttributeInstance scale = owner != null ? owner.getAttribute(Attributes.SCALE) : null;
      if (scale != null) {
        scale.removeModifier(MAGIC_CIRCLE_SCALE_MODIFIER_ID);
      }
    }
  }

  @Override
  protected List<CompanionAbility> allAbilities() {
    return ABILITIES;
  }

  @Override
  public OwnerStatBonusKit ownerStatBonus() {
    return OWNER_STAT_BONUS;
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
