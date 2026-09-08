package io.github.jason13official.summons.impl.common.entity.flying;

import io.github.jason13official.summons.impl.common.entity.AbstractCompanion;
import io.github.jason13official.summons.impl.common.entity.OwnerStatBonusKit;
import io.github.jason13official.summons.impl.common.entity.ability.CompanionAbility;
import io.github.jason13official.summons.impl.common.entity.ai.goal.attack.CompanionDiveAttackGoal;
import io.github.jason13official.summons.impl.common.evolution.EvoCrystalColor;
import io.github.jason13official.summons.impl.common.evolution.EvolutionForm;
import io.github.jason13official.summons.impl.common.evolution.EvolutionThreshold;
import java.util.List;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

/// Bird-Type: air mobility, lifts/carries the owner and juggles light enemies.
/// TODO: "Glide" (carry Hector over gaps) mimicked with Slow Falling (for now); gliding is a movement/input feature and not a Command-mode effect so heavy WIP
public class BirdSummon extends AbstractFlyingCompanion {

  // wiki: CON +4 initial, +12 growth
  private static final OwnerStatBonusKit OWNER_STAT_BONUS = new OwnerStatBonusKit(0, 0, 4, 12, 0, 0);

  private static final double CARPET_BOMBS_FIND_RADIUS = 10.0;
  private static final double CARPET_BOMBS_AOE_RADIUS = 2.0;
  private static final double DIVE_ATTACK_HIT_RADIUS = 1.5;
  private static final int DIVE_ATTACK_COOLDOWN = 20;

  private static final List<CompanionAbility> ABILITIES = List.of(
      // "Uses legs to propel Hector long distances. Allows access to places a normal jump
      // cannot reach." -> grabbing/carrying is a movement/input feature, not a Command
      // effect, so proxied as Slow Falling
      CompanionAbility.base("Glide", 100, (companion, owner) -> {
        owner.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 100));
        spawnAbilityParticles(owner, ParticleTypes.CLOUD, 8);
      }),
      // "Spreads explosive caltrops, which explode after a brief period." TODO: no delayed
      // detonation, proxied as an immediate AoE hit around the target instead
      CompanionAbility.gated("Caltrops", 30, Form.GOLDFINCH, 5, (companion, owner) -> {
        LivingEntity target = findNearestTarget(companion, owner, CARPET_BOMBS_FIND_RADIUS);
        if (target == null) {
          return;
        }

        AABB blastArea = target.getBoundingBox().inflate(2.5);
        float damage = (float) companion.getAttributeValue(Attributes.ATTACK_DAMAGE) * 0.8F;
        for (LivingEntity t : companion.level().getEntitiesOfClass(LivingEntity.class, blastArea,
            e -> e != companion && e != owner && e.isAlive())) {
          t.hurt(companion.damageSources().mobAttack(companion), damage);
          t.knockback(0.4, companion.getX() - t.getX(), companion.getZ() - t.getZ());
        }

        spawnAbilityParticles(target, ParticleTypes.CRIT, 10);
      }),
      // "Drops a carpet of bone-bombs for a brief period."
      CompanionAbility.gated("Carpet Bombs", 30, Form.SKULL_WING, 5, (companion, owner) -> {
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
      }),
      // "Sends countless sharpened bones ripping through foes." -> a real fired arrow, since
      // the basic direct attack is now a physical swoop/dive-bomb instead (see registerGoals)
      CompanionAbility.gated("Bone Shot", 20, Form.SKULL_WING, 5, (companion, owner) -> {
        LivingEntity target = findNearestTarget(companion, owner, CARPET_BOMBS_FIND_RADIUS);
        if (target != null) {
          shootArrow(companion, target);
        }
      }),
      // "Gathers latent rage in surrounding areas & spits it out as a sphere that slowly
      // moves toward foes." TODO: no homing projectile entity, proxied as an instant hit
      CompanionAbility.gated("Sphere of Darkness", 25, Form.KHAOS, 5, (companion, owner) -> {
        LivingEntity target = findNearestTarget(companion, owner, CARPET_BOMBS_FIND_RADIUS);
        if (target == null) {
          return;
        }

        target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 0));
        target.hurt(companion.damageSources().mobAttack(companion), (float) companion.getAttributeValue(Attributes.ATTACK_DAMAGE));
        spawnAbilityParticles(target, ParticleTypes.SQUID_INK, 8);
      }),
      // "Trades own life for a massive explosion that completely wipes out all enemies in
      // the vicinity." -> "the only attack besides Purify that can kill Isaac's Abel," and
      // it also kills the Phoenix using it
      CompanionAbility.gated("Big Bang", 60, Form.PHOENIX, 8, (companion, owner) -> {
        AABB area = companion.getBoundingBox().inflate(4.0);
        float damage = (float) companion.getAttributeValue(Attributes.ATTACK_DAMAGE) * 2.0F;

        for (LivingEntity target : companion.level().getEntitiesOfClass(LivingEntity.class, area,
            e -> e != owner && e.isAlive())) {
          target.hurt(companion.damageSources().mobAttack(companion), damage); // includes the companion itself
        }

        spawnAbilityParticles(companion, ParticleTypes.FLAME, 16);
      }),
      // "Smashes into foes with its flaming body, burning them to a crisp."
      CompanionAbility.gated("Fire Bird", 25, Form.PHOENIX, 8, (companion, owner) -> {
        LivingEntity target = findNearestTarget(companion, owner, CARPET_BOMBS_FIND_RADIUS);
        if (target == null) {
          return;
        }

        target.setRemainingFireTicks(100);
        target.hurt(companion.damageSources().mobAttack(companion), (float) companion.getAttributeValue(Attributes.ATTACK_DAMAGE) * 1.5F);
        spawnAbilityParticles(target, ParticleTypes.FLAME, 12);
      }),
      // "Uses legs to propel Hector long distances" (upgraded) -> reaches the Tower of Evermore
      CompanionAbility.gated("Long Glide", 200, Form.WINGOSAURUS, 8, (companion, owner) -> {
        owner.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 200));
        spawnAbilityParticles(owner, ParticleTypes.CLOUD, 12);
      }),
      // "Drains HP & gives it to Hector."
      CompanionAbility.gated("Deadly Absorb", 30, Form.WINGOSAURUS, 8, (companion, owner) -> {
        LivingEntity target = findNearestTarget(companion, owner, CARPET_BOMBS_FIND_RADIUS);
        if (target == null) {
          return;
        }

        float damage = (float) companion.getAttributeValue(Attributes.ATTACK_DAMAGE);
        target.hurt(companion.damageSources().mobAttack(companion), damage);
        owner.heal(damage * 0.5F);
        spawnAbilityParticles(target, ParticleTypes.HEART, 6);
      }),
      // "Attacks enemies with a series of deadly kicks."
      CompanionAbility.gated("Beat Progress", 30, Form.BLAGSDEATH, 10, (companion, owner) -> {
        LivingEntity target = findNearestTarget(companion, owner, CARPET_BOMBS_FIND_RADIUS);
        if (target == null) {
          return;
        }

        float hit = (float) companion.getAttributeValue(Attributes.ATTACK_DAMAGE) * 0.5F;
        for (int i = 0; i < 3; i++) {
          target.hurt(companion.damageSources().mobAttack(companion), hit);
        }
        target.knockback(0.5, companion.getX() - target.getX(), companion.getZ() - target.getZ());
        spawnAbilityParticles(target, ParticleTypes.CRIT, 10);
      }),
      // "Puts everything into one mighty kick, so energized that contact with the foe
      // causes an explosion."
      CompanionAbility.gated("Conflict Fall", 35, Form.GARGOYLE, 10, (companion, owner) -> {
        LivingEntity target = findNearestTarget(companion, owner, CARPET_BOMBS_FIND_RADIUS);
        if (target == null) {
          return;
        }

        AABB blastArea = target.getBoundingBox().inflate(3.0);
        float damage = (float) companion.getAttributeValue(Attributes.ATTACK_DAMAGE) * 2.0F;
        for (LivingEntity t : companion.level().getEntitiesOfClass(LivingEntity.class, blastArea,
            e -> e != companion && e != owner && e.isAlive())) {
          t.hurt(companion.damageSources().mobAttack(companion), damage);
          t.knockback(1.0, companion.getX() - t.getX(), companion.getZ() - t.getZ());
        }

        spawnAbilityParticles(target, ParticleTypes.EXPLOSION, 4);
      }),
      // "Transforms into a Magic Circle & launches countless beams at the foe."
      CompanionAbility.gated("Force Cannon", 30, Form.GARGOYLE, 10, (companion, owner) -> {
        LivingEntity target = findNearestTarget(companion, owner, CARPET_BOMBS_FIND_RADIUS);
        if (target == null) {
          return;
        }

        float hit = (float) companion.getAttributeValue(Attributes.ATTACK_DAMAGE) * 0.4F;
        for (int i = 0; i < 5; i++) {
          target.hurt(companion.damageSources().magic(), hit);
        }
        spawnAbilityParticles(target, ParticleTypes.END_ROD, 14);
      }),
      // "A sharpened solid icicle rips foes apart."
      CompanionAbility.gated("Icicle Shot", 25, Form.INDIGO, 10, (companion, owner) -> {
        LivingEntity target = findNearestTarget(companion, owner, CARPET_BOMBS_FIND_RADIUS);
        if (target == null) {
          return;
        }

        target.hurt(companion.damageSources().mobAttack(companion), (float) companion.getAttributeValue(Attributes.ATTACK_DAMAGE) * 1.5F);
        spawnAbilityParticles(target, ParticleTypes.SNOWFLAKE, 10);
      }),
      // "Frozen breath turns foes into ice." -> AoE, unlike the single-target Icicle Shot
      CompanionAbility.gated("Blizzard Breath", 35, Form.INDIGO, 10, (companion, owner) -> {
        AABB area = companion.getBoundingBox().inflate(4.0);
        for (LivingEntity target : companion.level().getEntitiesOfClass(LivingEntity.class, area,
            e -> e != companion && e != owner && e.isAlive())) {
          target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 4));
          target.hurt(companion.damageSources().mobAttack(companion), (float) companion.getAttributeValue(Attributes.ATTACK_DAMAGE) * 0.6F);
        }

        spawnAbilityParticles(companion, ParticleTypes.SNOWFLAKE, 16);
      }),
      // "Launches a flaming ball towards the foe, sending up a pillar of fire where it lands."
      CompanionAbility.gated("Ignition Blow", 25, Form.CRIMSON, 10, (companion, owner) -> {
        LivingEntity target = findNearestTarget(companion, owner, CARPET_BOMBS_FIND_RADIUS);
        if (target == null) {
          return;
        }

        target.setRemainingFireTicks(100);
        target.hurt(companion.damageSources().mobAttack(companion), (float) companion.getAttributeValue(Attributes.ATTACK_DAMAGE) * 1.5F);
        spawnAbilityParticles(target, ParticleTypes.FLAME, 10);
      }),
      // "Flaming breath burns up all foes in the vicinity." -> AoE, unlike single-target Ignition Blow
      CompanionAbility.gated("Flame Breath", 35, Form.CRIMSON, 10, (companion, owner) -> {
        AABB area = companion.getBoundingBox().inflate(4.0);
        for (LivingEntity target : companion.level().getEntitiesOfClass(LivingEntity.class, area,
            e -> e != companion && e != owner && e.isAlive())) {
          target.setRemainingFireTicks(80);
          target.hurt(companion.damageSources().mobAttack(companion), (float) companion.getAttributeValue(Attributes.ATTACK_DAMAGE) * 0.6F);
        }

        spawnAbilityParticles(companion, ParticleTypes.FLAME, 16);
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
  protected void registerGoals() {
    super.registerGoals();
    this.goalSelector.addGoal(1,
        new CompanionDiveAttackGoal(this, 1.4, DIVE_ATTACK_HIT_RADIUS, DIVE_ATTACK_COOLDOWN, BirdSummon::diveAttack));
  }

  /// physical swoop/dive-bomb -> Bird's basic direct attack
  private static void diveAttack(AbstractCompanion companion, LivingEntity target) {
    companion.swing(InteractionHand.MAIN_HAND);
    float damage = (float) companion.getAttributeValue(Attributes.ATTACK_DAMAGE);
    target.hurt(companion.damageSources().mobAttack(companion), damage);
    target.knockback(0.6, companion.getX() - target.getX(), companion.getZ() - target.getZ());
    spawnAbilityParticles(target, ParticleTypes.CLOUD, 4);
    companion.grantDirectAttackExperience();
  }

  /// fires a real Arrow entity -> used by the Bone Shot ability
  private static void shootArrow(AbstractCompanion companion, LivingEntity target) {
    if (!(companion.level() instanceof ServerLevel serverLevel)) {
      return;
    }

    companion.swing(InteractionHand.MAIN_HAND);
    Arrow arrow = new Arrow(serverLevel, companion, new ItemStack(Items.ARROW), null);
    arrow.setBaseDamage(companion.getAttributeValue(Attributes.ATTACK_DAMAGE) * 0.5);

    double dx = target.getX() - companion.getX();
    double dy = target.getY(0.3333333333333333) - arrow.getY();
    double dz = target.getZ() - companion.getZ();
    double horizontalDistance = Math.sqrt(dx * dx + dz * dz);
    arrow.shoot(dx, dy + horizontalDistance * 0.2, dz, 1.6F, 6.0F);

    serverLevel.addFreshEntity(arrow);
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
