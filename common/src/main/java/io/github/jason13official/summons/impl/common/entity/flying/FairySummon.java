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
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;

/// Fairy-Type: support only, heals/cures the owner, does not attack.
public class FairySummon extends AbstractFlyingCompanion {

  private static final List<CompanionAbility> ABILITIES = List.of(
      CompanionAbility.base("Heal Lv.1", 20, (companion, owner) -> {
        owner.heal(2.0F);
        spawnAbilityParticles(owner, ParticleTypes.HEART, 5);
      }),
      // wiki: Leaffle gets "Time Heal, Poison Powder" - we only have Time Heal implemented
      CompanionAbility.gated("Time Heal", 100, Form.LEAFFLE, 5, (companion, owner) -> {
        owner.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 100, 1)); // ~4.0F over the duration
        spawnAbilityParticles(owner, ParticleTypes.HEART, 5);
      }),
      // wiki: Herbest gets "Antidote, Heal Lv.2"
      CompanionAbility.gated("Heal Lv.2", 30, Form.HERBEST, 5, (companion, owner) -> {
        owner.heal(6.0F);
        spawnAbilityParticles(owner, ParticleTypes.HEART, 5);
      }),
      CompanionAbility.gated("Antidote", 20, Form.HERBEST, 5, (companion, owner) -> {
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
  protected List<CompanionAbility> allAbilities() {
    return ABILITIES;
  }

  @Override
  protected EvolutionForm baseForm() {
    return Form.INFANT_FAIRY;
  }

  @Override
  protected EvolutionForm resolveForm(String id) {
    try {
      return Form.valueOf(id);
    } catch (IllegalArgumentException e) {
      return Form.INFANT_FAIRY;
    }
  }

  @Override
  protected List<EvolutionThreshold> evolutionThresholds() {
    return switch ((Form) this.getEvolutionForm()) {
      case INFANT_FAIRY -> List.of(
          new EvolutionThreshold(EvoCrystalColor.RED, 40, Form.LEAFFLE),
          new EvolutionThreshold(EvoCrystalColor.YELLOW, 40, Form.LEAFFLE),
          new EvolutionThreshold(EvoCrystalColor.BLUE, 40, Form.HERBEST),
          new EvolutionThreshold(EvoCrystalColor.GREEN, 40, Form.HERBEST),
          new EvolutionThreshold(EvoCrystalColor.WHITE, 40, Form.HERBEST));
      case LEAFFLE -> List.of(
          new EvolutionThreshold(EvoCrystalColor.BLUE, 70, Form.HONEY_BEE),
          new EvolutionThreshold(EvoCrystalColor.YELLOW, 70, Form.HONEY_BEE),
          new EvolutionThreshold(EvoCrystalColor.WHITE, 70, Form.HONEY_BEE),
          new EvolutionThreshold(EvoCrystalColor.RED, 70, Form.KILLER_BEE),
          new EvolutionThreshold(EvoCrystalColor.GREEN, 70, Form.KILLER_BEE));
      case HERBEST -> List.of(
          new EvolutionThreshold(EvoCrystalColor.RED, 70, Form.KILLER_BEE),
          new EvolutionThreshold(EvoCrystalColor.GREEN, 70, Form.KILLER_BEE),
          new EvolutionThreshold(EvoCrystalColor.BLUE, 70, Form.HORNET),
          new EvolutionThreshold(EvoCrystalColor.YELLOW, 70, Form.HORNET),
          new EvolutionThreshold(EvoCrystalColor.WHITE, 70, Form.HORNET));
      case HONEY_BEE -> List.of(
          new EvolutionThreshold(EvoCrystalColor.WHITE, 90, Form.PROBOSCIS_FAIRY),
          new EvolutionThreshold(EvoCrystalColor.RED, 90, Form.TIRAMISU),
          new EvolutionThreshold(EvoCrystalColor.BLUE, 90, Form.TIRAMISU),
          new EvolutionThreshold(EvoCrystalColor.GREEN, 90, Form.TIRAMISU),
          new EvolutionThreshold(EvoCrystalColor.YELLOW, 90, Form.TIRAMISU));
      case KILLER_BEE -> List.of(
          new EvolutionThreshold(EvoCrystalColor.BLUE, 90, Form.TIRAMISU),
          new EvolutionThreshold(EvoCrystalColor.WHITE, 90, Form.TIRAMISU),
          new EvolutionThreshold(EvoCrystalColor.RED, 90, Form.TIARA),
          new EvolutionThreshold(EvoCrystalColor.GREEN, 90, Form.TIARA),
          new EvolutionThreshold(EvoCrystalColor.YELLOW, 90, Form.TIARA));
      case HORNET -> List.of(
          new EvolutionThreshold(EvoCrystalColor.RED, 90, Form.TIARA),
          new EvolutionThreshold(EvoCrystalColor.BLUE, 90, Form.TIARA),
          new EvolutionThreshold(EvoCrystalColor.GREEN, 90, Form.TIARA),
          new EvolutionThreshold(EvoCrystalColor.YELLOW, 90, Form.COMET_STAR),
          new EvolutionThreshold(EvoCrystalColor.WHITE, 90, Form.COMET_STAR));
      default -> List.of(); // Level 4 forms are final
    };
  }

  /// Innocent Devil Data (Fairy-Types); Level 4 forms are final, no further evolution
  public enum Form implements EvolutionForm {
    INFANT_FAIRY("Infant Fairy", 0),
    LEAFFLE("Leaffle", 1),
    HERBEST("Herbest", 1),
    HONEY_BEE("Honey Bee", 2),
    KILLER_BEE("Killer Bee", 2),
    HORNET("Hornet", 2),
    PROBOSCIS_FAIRY("Proboscis Fairy", 3),
    TIRAMISU("Tiramisu", 3),
    TIARA("Tiara", 3),
    COMET_STAR("Comet Star", 3);

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
