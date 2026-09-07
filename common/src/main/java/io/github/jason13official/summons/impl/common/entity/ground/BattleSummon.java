package io.github.jason13official.summons.impl.common.entity.ground;

import io.github.jason13official.summons.impl.common.entity.AbstractCompanion;
import io.github.jason13official.summons.impl.common.entity.ability.CompanionAbility;
import io.github.jason13official.summons.impl.common.evolution.EvoCrystalColor;
import io.github.jason13official.summons.impl.common.evolution.EvolutionForm;
import io.github.jason13official.summons.impl.common.evolution.EvolutionThreshold;
import java.util.List;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

/// Battle-Type: physically strong, lots of health and hits heavy
public class BattleSummon extends AbstractGroundCompanion {

  private static final double AURA_BLAST_RADIUS = 3.0;

  private static final List<CompanionAbility> ABILITIES = List.of(
      CompanionAbility.base("Aura Blast", 20, (companion, owner) -> {
        AABB area = companion.getBoundingBox().inflate(AURA_BLAST_RADIUS);
        float damage = (float) companion.getAttributeValue(Attributes.ATTACK_DAMAGE);

        for (LivingEntity target : companion.level().getEntitiesOfClass(LivingEntity.class, area,
            e -> e != companion && e != owner && e.isAlive())) {
          target.hurt(companion.damageSources().mobAttack(companion), damage);
        }

        spawnAbilityParticles(companion, ParticleTypes.EXPLOSION, 2);
      }),
      // wiki: Golem gets "Hip Press, Hip Press Lv.2"
      CompanionAbility.gated("Hip Press", 30, Form.GOLEM, 5, (companion, owner) -> {
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
  protected void registerGoals() {
    super.registerGoals();
    this.goalSelector.addGoal(0, new MeleeAttackGoal(this, 1.2, true));
  }

  @Override
  protected List<CompanionAbility> allAbilities() {
    return ABILITIES;
  }

  @Override
  protected EvolutionForm baseForm() {
    return Form.MAGMARD;
  }

  @Override
  protected EvolutionForm resolveForm(String id) {
    try {
      return Form.valueOf(id);
    } catch (IllegalArgumentException e) {
      return Form.MAGMARD;
    }
  }

  @Override
  protected List<EvolutionThreshold> evolutionThresholds() {
    return switch ((Form) this.getEvolutionForm()) {
      case MAGMARD -> List.of(
          new EvolutionThreshold(EvoCrystalColor.BLUE, 40, Form.SPEED_MAIL),
          new EvolutionThreshold(EvoCrystalColor.YELLOW, 40, Form.SPEED_MAIL),
          new EvolutionThreshold(EvoCrystalColor.GREEN, 40, Form.SPEED_MAIL),
          new EvolutionThreshold(EvoCrystalColor.RED, 40, Form.GOLEM),
          new EvolutionThreshold(EvoCrystalColor.WHITE, 40, Form.GOLEM));
      case SPEED_MAIL -> List.of(
          new EvolutionThreshold(EvoCrystalColor.BLUE, 90, Form.RASETZ),
          new EvolutionThreshold(EvoCrystalColor.WHITE, 90, Form.RASETZ),
          new EvolutionThreshold(EvoCrystalColor.RED, 90, Form.CORPSEY),
          new EvolutionThreshold(EvoCrystalColor.GREEN, 90, Form.CORPSEY),
          new EvolutionThreshold(EvoCrystalColor.YELLOW, 90, Form.CORPSEY));
      case GOLEM -> List.of(
          new EvolutionThreshold(EvoCrystalColor.WHITE, 90, Form.IYTEI),
          new EvolutionThreshold(EvoCrystalColor.RED, 70, Form.JUGGERNAUT),
          new EvolutionThreshold(EvoCrystalColor.BLUE, 70, Form.JUGGERNAUT),
          new EvolutionThreshold(EvoCrystalColor.GREEN, 70, Form.JUGGERNAUT),
          new EvolutionThreshold(EvoCrystalColor.YELLOW, 70, Form.JUGGERNAUT));
      case JUGGERNAUT -> List.of(
          new EvolutionThreshold(EvoCrystalColor.BLUE, 90, Form.IRONSIDE),
          new EvolutionThreshold(EvoCrystalColor.WHITE, 90, Form.IRONSIDE),
          new EvolutionThreshold(EvoCrystalColor.RED, 90, Form.LIQUID_GOLEM),
          new EvolutionThreshold(EvoCrystalColor.GREEN, 90, Form.LIQUID_GOLEM),
          new EvolutionThreshold(EvoCrystalColor.YELLOW, 90, Form.LIQUID_GOLEM));
      default -> List.of(); // Iytei (final at Level 3), and all Level 4 forms
    };
  }

  /// Innocent Devil Data (Battle-Types); Iytei is final at Level 3, everything else at Level 4
  public enum Form implements EvolutionForm {
    MAGMARD("Magmard", 0),
    SPEED_MAIL("Speed Mail", 1),
    GOLEM("Golem", 1),
    IYTEI("Iytei", 2),
    JUGGERNAUT("Juggernaut", 2),
    RASETZ("Rasetz", 3),
    CORPSEY("Corpsey", 3),
    IRONSIDE("Ironside", 3),
    LIQUID_GOLEM("Liquid Golem", 3);

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
