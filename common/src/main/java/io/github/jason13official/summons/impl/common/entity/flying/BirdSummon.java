package io.github.jason13official.summons.impl.common.entity.flying;

import io.github.jason13official.summons.impl.common.entity.ability.CompanionAbility;
import io.github.jason13official.summons.impl.common.evolution.EvoCrystalColor;
import io.github.jason13official.summons.impl.common.evolution.EvolutionForm;
import io.github.jason13official.summons.impl.common.evolution.EvolutionThreshold;
import java.util.List;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

/// Bird-Type: air mobility, lifts/carries the owner and juggles light enemies.
/// TODO: "Glide" (carry Hector over gaps) mimicked with Slow Falling (for now); gliding is a movement/input feature and not a Command-mode effect so heavy WIP
public class BirdSummon extends AbstractFlyingCompanion {

  private static final double CARPET_BOMBS_FIND_RADIUS = 10.0;
  private static final double CARPET_BOMBS_AOE_RADIUS = 2.0;

  private static final List<CompanionAbility> ABILITIES = List.of(
      CompanionAbility.base("Glide", 100, (companion, owner) -> {
        owner.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 100));
        spawnAbilityParticles(owner, ParticleTypes.CLOUD, 8);
      }),
      // wiki: Skull Wing gets "Carpet Bombs, Bone Shot"
      CompanionAbility.gated("Carpet Bombs", 30, Form.SKULL_WING, (companion, owner) -> {
        LivingEntity primary = findNearestTarget(companion, owner, CARPET_BOMBS_FIND_RADIUS);
        if (primary == null) {
          return;
        }

        float damage = (float) companion.getAttributeValue(Attributes.ATTACK_DAMAGE);
        AABB blastArea = primary.getBoundingBox().inflate(CARPET_BOMBS_AOE_RADIUS);
        for (LivingEntity target : companion.level().getEntitiesOfClass(LivingEntity.class, blastArea,
            e -> e != companion && e != owner && e.isAlive())) {
          target.hurt(companion.damageSources().mobAttack(companion), damage);
        }

        spawnAbilityParticles(primary, ParticleTypes.POOF, 6);
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
  protected List<CompanionAbility> allAbilities() {
    return ABILITIES;
  }

  @Override
  protected EvolutionForm baseForm() {
    return Form.CROW;
  }

  @Override
  protected EvolutionForm resolveForm(String id) {
    try {
      return Form.valueOf(id);
    } catch (IllegalArgumentException e) {
      return Form.CROW;
    }
  }

  @Override
  protected List<EvolutionThreshold> evolutionThresholds() {
    return switch ((Form) this.getEvolutionForm()) {
      case CROW -> List.of(
          new EvolutionThreshold(EvoCrystalColor.GREEN, 40, Form.GOLDFINCH),
          new EvolutionThreshold(EvoCrystalColor.YELLOW, 40, Form.GOLDFINCH),
          new EvolutionThreshold(EvoCrystalColor.WHITE, 40, Form.GOLDFINCH),
          new EvolutionThreshold(EvoCrystalColor.RED, 40, Form.SKULL_WING),
          new EvolutionThreshold(EvoCrystalColor.BLUE, 40, Form.SKULL_WING));
      case GOLDFINCH -> List.of(EvolutionThreshold.any(70, Form.KHAOS));
      case SKULL_WING -> List.of(
          new EvolutionThreshold(EvoCrystalColor.GREEN, 90, Form.PHOENIX),
          new EvolutionThreshold(EvoCrystalColor.YELLOW, 90, Form.PHOENIX),
          new EvolutionThreshold(EvoCrystalColor.WHITE, 90, Form.PHOENIX),
          new EvolutionThreshold(EvoCrystalColor.RED, 70, Form.WINGOSAURUS),
          new EvolutionThreshold(EvoCrystalColor.BLUE, 70, Form.WINGOSAURUS));
      case KHAOS -> List.of(
          new EvolutionThreshold(EvoCrystalColor.RED, 90, Form.BLAGSDEATH),
          new EvolutionThreshold(EvoCrystalColor.GREEN, 90, Form.BLAGSDEATH),
          new EvolutionThreshold(EvoCrystalColor.YELLOW, 90, Form.BLAGSDEATH),
          new EvolutionThreshold(EvoCrystalColor.BLUE, 90, Form.GARGOYLE),
          new EvolutionThreshold(EvoCrystalColor.WHITE, 90, Form.GARGOYLE));
      case WINGOSAURUS -> List.of(
          new EvolutionThreshold(EvoCrystalColor.GREEN, 90, Form.INDIGO),
          new EvolutionThreshold(EvoCrystalColor.YELLOW, 90, Form.INDIGO),
          new EvolutionThreshold(EvoCrystalColor.RED, 90, Form.CRIMSON),
          new EvolutionThreshold(EvoCrystalColor.BLUE, 90, Form.CRIMSON),
          new EvolutionThreshold(EvoCrystalColor.WHITE, 90, Form.CRIMSON));
      default -> List.of(); // Phoenix (final at Level 3), and all Level 4 forms
    };
  }

  /// Innocent Devil Data (Bird-Types); Phoenix is final at Level 3, everything else at Level 4
  public enum Form implements EvolutionForm {
    CROW("Crow", 0),
    GOLDFINCH("Goldfinch", 1),
    SKULL_WING("Skull Wing", 1),
    KHAOS("Khaos", 2),
    PHOENIX("Phoenix", 2),
    WINGOSAURUS("Wingosaurus", 2),
    BLAGSDEATH("Blagsdeath", 3),
    GARGOYLE("Gargoyle", 3),
    INDIGO("Indigo", 3),
    CRIMSON("Crimson", 3);

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
