package io.github.jason13official.summons.impl.common.entity.ground;

import io.github.jason13official.summons.impl.common.entity.AbstractCompanion;
import io.github.jason13official.summons.impl.common.entity.OwnerStatBonusKit;
import io.github.jason13official.summons.impl.common.entity.ability.CompanionAbility;
import io.github.jason13official.summons.impl.common.evolution.EvoCrystalColor;
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

/// Pumpkin-Type: bad at fighting and low health, but a big stat boost to the owner
/// (owner stat bonuses not implemented yet).
public class PumpkinSummon extends AbstractGroundCompanion {

  private static final double POSE_RADIUS = 2.0;

  // wiki: STR +10/CON +4/LCK +5 initial, +60/+18/+50 growth; biggest owner buff of any type
  private static final OwnerStatBonusKit OWNER_STAT_BONUS = new OwnerStatBonusKit(10, 60, 4, 18, 5, 50);

  /// "They only have one ability, Pose, which inflicts minimum damage to nearby enemies,
  /// although Pumpkin will be completely vulnerable while performing it."
  private static final List<CompanionAbility> ABILITIES = List.of(
      CompanionAbility.base("Pose", 40, (companion, owner) -> {
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
  protected void registerGoals() {
    super.registerGoals();
    this.goalSelector.addGoal(0, new MeleeAttackGoal(this, 1.0, true));
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
    return Form.PUMPKIN;
  }

  @Override
  protected EvolutionForm resolveForm(String id) {
    try {
      return Form.valueOf(id);
    } catch (IllegalArgumentException e) {
      return Form.PUMPKIN;
    }
  }

  @Override
  public EvolutionForm[] allForms() {
    return Form.values();
  }

  @Override
  protected List<EvolutionThreshold> evolutionThresholds() {
    return switch ((Form) this.getEvolutionForm()) {
      case PUMPKIN -> List.of(
          new EvolutionThreshold(EvoCrystalColor.YELLOW, 70, Form.QUEEN),
          new EvolutionThreshold(EvoCrystalColor.BLUE, 70, Form.BLOODY),
          new EvolutionThreshold(EvoCrystalColor.RED, 70, Form.TINY_KING),
          new EvolutionThreshold(EvoCrystalColor.WHITE, 70, Form.CLOWN_NOSE),
          new EvolutionThreshold(EvoCrystalColor.GREEN, 70, Form.NEW_DELI));
      // Bloody/Tiny King/Clown Nose each need a specific weapon for Level 3, not just
      // crystals; Queen/New Deli are simply final. None reachable yet either way.
      default -> List.of();
    };
  }

  /// Innocent Devil Data (Pumpkin-Types); evolutions are purely cosmetic; Pose stays the
  /// only ability regardless. Queen/New Deli are final at Level 2; the rest need a specific
  /// weapon (Death's Scythe/Short Sword/Frying Pan) for their Level 3, not just crystals.
  public enum Form implements EvolutionForm {
    PUMPKIN("Pumpkin", 0),
    QUEEN("Queen", 1),
    BLOODY("Bloody", 1),
    TINY_KING("Tiny King", 1),
    CLOWN_NOSE("Clown Nose", 1),
    NEW_DELI("New Deli", 1),
    CURSED_PUMPKIN("Cursed Pumpkin", 2),
    WHIMSICAL_ANGEL("Whimsical Angel", 2),
    GENIUS_CHEF("Genius Chef", 2);

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
