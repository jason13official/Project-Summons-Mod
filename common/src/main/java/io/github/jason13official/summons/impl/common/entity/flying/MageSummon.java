package io.github.jason13official.summons.impl.common.entity.flying;

import io.github.jason13official.summons.impl.common.entity.AbstractCompanion;
import io.github.jason13official.summons.impl.common.entity.ability.CompanionAbility;
import io.github.jason13official.summons.impl.common.entity.ai.goal.attack.CompanionRangedAttackGoal;
import io.github.jason13official.summons.impl.common.entity.ground.AbstractGroundCompanion;
import io.github.jason13official.summons.impl.common.evolution.EvoCrystalColor;
import io.github.jason13official.summons.impl.common.evolution.EvolutionForm;
import io.github.jason13official.summons.impl.common.evolution.EvolutionThreshold;
import java.util.List;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/// Mage-Type: physically weak, casts powerful area-of-effect spells (unimplemented).
public class MageSummon extends AbstractFlyingCompanion {

  private static final double SPELL_RADIUS = 12.0;
  private static final double DIRECT_ATTACK_RADIUS = 10.0;
  private static final int DIRECT_ATTACK_COOLDOWN = 40;

  /// client-side render animation phase; one model instance is shared by every Mage
  /// Summon, so this state has to live on the entity, not the model.
  public final AnimationState idleAnimationState = new AnimationState();
  public final AnimationState flyAnimationState = new AnimationState();

  private static final List<CompanionAbility> ABILITIES = List.of(
      CompanionAbility.base("Lightning Strike", 30, (companion, owner) -> {
        if (!(companion.level() instanceof ServerLevel serverLevel)) {
          return;
        }

        LivingEntity target = findNearestTarget(companion, owner, SPELL_RADIUS);
        Vec3 strikeAt = target != null ? target.position() : owner.position();
        LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(serverLevel);
        if (bolt != null) {
          bolt.moveTo(strikeAt.x, strikeAt.y, strikeAt.z);
          serverLevel.addFreshEntity(bolt);
        }
      }),
      // wiki: Talon Rod gets "Circle Scissors, Freeze"
      CompanionAbility.gated("Freeze", 30, Form.TALON_ROD, 5, (companion, owner) -> {
        LivingEntity target = findNearestTarget(companion, owner, SPELL_RADIUS);
        if (target == null) {
          return;
        }

        target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 3));
        target.hurt(companion.damageSources().mobAttack(companion),
            (float) companion.getAttributeValue(Attributes.ATTACK_DAMAGE) * 0.5F);
        spawnAbilityParticles(target, ParticleTypes.SNOWFLAKE, 10);
      }),
      // wiki: base-kit ability alongside Lightning Strike; TODO: approximated as heavy AoE
      // slowness, not a true freeze -> no such mechanic exists in vanilla
      CompanionAbility.base("Time Stop", 40, (companion, owner) -> {
        AABB area = companion.getBoundingBox().inflate(SPELL_RADIUS);
        for (LivingEntity target : companion.level().getEntitiesOfClass(LivingEntity.class, area,
            e -> e != companion && e != owner && e.isAlive())) {
          target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 9));
        }
        spawnAbilityParticles(companion, ParticleTypes.END_ROD, 10);
      }),
      // wiki: Talon Rod gets "Circle Scissors, Freeze"
      CompanionAbility.gated("Circle Scissors", 25, Form.TALON_ROD, 5, (companion, owner) -> {
        LivingEntity target = findNearestTarget(companion, owner, SPELL_RADIUS);
        if (target == null) {
          return;
        }

        float hit = (float) companion.getAttributeValue(Attributes.ATTACK_DAMAGE) * 0.6F;
        target.hurt(companion.damageSources().magic(), hit); // two hits: "scissors"
        target.hurt(companion.damageSources().magic(), hit);
        spawnAbilityParticles(target, ParticleTypes.CRIT, 8);
      }),
      // elemental spell: Fire (Scissor Rod)
      CompanionAbility.gated("Fire Bolt", 25, Form.SCISSOR_ROD, 5, (companion, owner) -> {
        LivingEntity target = findNearestTarget(companion, owner, SPELL_RADIUS);
        if (target == null) {
          return;
        }

        target.setRemainingFireTicks(60);
        target.hurt(companion.damageSources().magic(), (float) companion.getAttributeValue(Attributes.ATTACK_DAMAGE) * 0.5F);
        spawnAbilityParticles(target, ParticleTypes.FLAME, 10);
      }),
      // elemental spell: Ice (Ogre Rod)
      CompanionAbility.gated("Ice Bolt", 25, Form.OGRE_ROD, 8, (companion, owner) -> {
        LivingEntity target = findNearestTarget(companion, owner, SPELL_RADIUS);
        if (target == null) {
          return;
        }

        target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 3));
        target.hurt(companion.damageSources().magic(), (float) companion.getAttributeValue(Attributes.ATTACK_DAMAGE) * 0.5F);
        spawnAbilityParticles(target, ParticleTypes.SNOWFLAKE, 10);
      }),
      // elemental spell: Thunder (Goat Head) -> visual-only bolt, damage dealt explicitly below
      // to avoid double-dipping against LightningBolt's own strike damage
      CompanionAbility.gated("Thunder Bolt", 30, Form.GOAT_HEAD, 8, (companion, owner) -> {
        if (!(companion.level() instanceof ServerLevel serverLevel)) {
          return;
        }

        LivingEntity target = findNearestTarget(companion, owner, SPELL_RADIUS);
        Vec3 strikeAt = target != null ? target.position() : owner.position();
        LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(serverLevel);
        if (bolt != null) {
          bolt.moveTo(strikeAt.x, strikeAt.y, strikeAt.z);
          bolt.setVisualOnly(true);
          serverLevel.addFreshEntity(bolt);
        }

        if (target != null) {
          target.hurt(companion.damageSources().magic(), (float) companion.getAttributeValue(Attributes.ATTACK_DAMAGE));
        }
      }),
      // elemental spell: Light (Eyeball Rod)
      CompanionAbility.gated("Light Bolt", 25, Form.EYEBALL_ROD, 10, (companion, owner) -> {
        LivingEntity target = findNearestTarget(companion, owner, SPELL_RADIUS);
        if (target == null) {
          return;
        }

        target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 100, 0));
        target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 40, 0));
        target.hurt(companion.damageSources().magic(), (float) companion.getAttributeValue(Attributes.ATTACK_DAMAGE) * 0.5F);
        spawnAbilityParticles(target, ParticleTypes.END_ROD, 10);
      }),
      // elemental spell: Earth (Embryo Rod); TODO: no dedicated earth/rubble particle used
      CompanionAbility.gated("Earth Bolt", 30, Form.EMBRYO_ROD, 10, (companion, owner) -> {
        LivingEntity target = findNearestTarget(companion, owner, SPELL_RADIUS);
        if (target == null) {
          return;
        }

        target.knockback(1.0, 0.0, 0.0);
        target.hurt(companion.damageSources().magic(), (float) companion.getAttributeValue(Attributes.ATTACK_DAMAGE) * 0.8F);
        spawnAbilityParticles(target, ParticleTypes.CRIT, 10);
      }),
      // wiki: "the only attack that can permanently kill Blood Skeletons"; TODO: no Blood
      // Skeleton equivalent mob exists yet, so this just hits hard for now
      CompanionAbility.gated("Purify", 40, Form.NAUTILUS_ROD, 10, (companion, owner) -> {
        LivingEntity target = findNearestTarget(companion, owner, SPELL_RADIUS);
        if (target == null) {
          return;
        }

        target.hurt(companion.damageSources().magic(), (float) companion.getAttributeValue(Attributes.ATTACK_DAMAGE) * 2.0F);
        spawnAbilityParticles(target, ParticleTypes.END_ROD, 14);
      })
  );

  public MageSummon(EntityType<? extends AbstractCompanion> entityType, Level level) {
    super(entityType, level);
  }

  public static AttributeSupplier.Builder createAttributes() {

    return AbstractFlyingCompanion.createAttributes().add(Attributes.MAX_HEALTH, (double) 12.0F)
        .add(Attributes.FLYING_SPEED, (double) 0.45F).add(Attributes.MOVEMENT_SPEED, (double) 0.28F)
        .add(Attributes.ATTACK_DAMAGE, (double) 10.0F).add(Attributes.KNOCKBACK_RESISTANCE, (double) 0.3F);
  }

  @Override
  protected void registerGoals() {
    super.registerGoals();
    this.goalSelector.addGoal(1,
        new CompanionRangedAttackGoal(this, 1.0, DIRECT_ATTACK_COOLDOWN, DIRECT_ATTACK_RADIUS, MageSummon::castZap));
  }

  /// small lightning zap: physically weak, so its basic attack is a light
  /// instant spell, a scaled fraction of ATTACK_DAMAGE, no projectile entity.
  private static void castZap(AbstractCompanion companion, LivingEntity target) {
    float damage = (float) companion.getAttributeValue(Attributes.ATTACK_DAMAGE) * 0.3F;
    target.hurt(companion.damageSources().magic(), damage);
    spawnAbilityParticles(target, ParticleTypes.ELECTRIC_SPARK, 6);
    companion.grantDirectAttackExperience();
  }

  @Override
  protected List<CompanionAbility> allAbilities() {
    return ABILITIES;
  }

  @Override
  protected EvolutionForm baseForm() {
    return Form.WOOD_ROD;
  }

  @Override
  protected EvolutionForm resolveForm(String id) {
    try {
      return Form.valueOf(id);
    } catch (IllegalArgumentException e) {
      return Form.WOOD_ROD;
    }
  }

  @Override
  protected List<EvolutionThreshold> evolutionThresholds() {
    return switch ((Form) this.getEvolutionForm()) {
      case WOOD_ROD -> List.of(
          new EvolutionThreshold(EvoCrystalColor.RED, 40, Form.SCISSOR_ROD),
          new EvolutionThreshold(EvoCrystalColor.YELLOW, 40, Form.SCISSOR_ROD),
          new EvolutionThreshold(EvoCrystalColor.BLUE, 40, Form.TALON_ROD),
          new EvolutionThreshold(EvoCrystalColor.GREEN, 40, Form.TALON_ROD),
          new EvolutionThreshold(EvoCrystalColor.WHITE, 40, Form.TALON_ROD));
      case SCISSOR_ROD -> List.of(
          new EvolutionThreshold(EvoCrystalColor.GREEN, 70, Form.NAUTILUS_ROD),
          new EvolutionThreshold(EvoCrystalColor.YELLOW, 70, Form.NAUTILUS_ROD),
          new EvolutionThreshold(EvoCrystalColor.WHITE, 70, Form.NAUTILUS_ROD),
          new EvolutionThreshold(EvoCrystalColor.RED, 70, Form.OGRE_ROD),
          new EvolutionThreshold(EvoCrystalColor.BLUE, 70, Form.OGRE_ROD));
      case TALON_ROD -> List.of(
          new EvolutionThreshold(EvoCrystalColor.RED, 70, Form.OGRE_ROD),
          new EvolutionThreshold(EvoCrystalColor.BLUE, 70, Form.OGRE_ROD),
          new EvolutionThreshold(EvoCrystalColor.WHITE, 70, Form.OGRE_ROD),
          new EvolutionThreshold(EvoCrystalColor.GREEN, 70, Form.GOAT_HEAD),
          new EvolutionThreshold(EvoCrystalColor.YELLOW, 70, Form.GOAT_HEAD));
      case NAUTILUS_ROD -> List.of(
          new EvolutionThreshold(EvoCrystalColor.BLUE, 90, Form.EYEBALL_ROD),
          new EvolutionThreshold(EvoCrystalColor.GREEN, 90, Form.EYEBALL_ROD),
          new EvolutionThreshold(EvoCrystalColor.RED, 90, Form.EMBRYO_ROD),
          new EvolutionThreshold(EvoCrystalColor.YELLOW, 90, Form.EMBRYO_ROD),
          new EvolutionThreshold(EvoCrystalColor.WHITE, 90, Form.EMBRYO_ROD));
      case OGRE_ROD -> List.of(
          new EvolutionThreshold(EvoCrystalColor.RED, 90, Form.EMBRYO_ROD),
          new EvolutionThreshold(EvoCrystalColor.GREEN, 90, Form.EMBRYO_ROD),
          new EvolutionThreshold(EvoCrystalColor.BLUE, 90, Form.CRYSTAL_ROD),
          new EvolutionThreshold(EvoCrystalColor.YELLOW, 90, Form.CRYSTAL_ROD),
          new EvolutionThreshold(EvoCrystalColor.WHITE, 90, Form.CRYSTAL_ROD));
      case GOAT_HEAD -> List.of(
          new EvolutionThreshold(EvoCrystalColor.RED, 90, Form.CRYSTAL_ROD),
          new EvolutionThreshold(EvoCrystalColor.BLUE, 90, Form.CRYSTAL_ROD),
          new EvolutionThreshold(EvoCrystalColor.GREEN, 90, Form.CRYSTAL_ROD),
          new EvolutionThreshold(EvoCrystalColor.YELLOW, 90, Form.CRYSTAL_ROD),
          new EvolutionThreshold(EvoCrystalColor.WHITE, 90, Form.TWINKLE_ROD));
      default -> List.of(); // all Level 4 forms are final
    };
  }

  /// Innocent Devil Data (Mage-Types); all Level 4 forms are final
  public enum Form implements EvolutionForm {
    WOOD_ROD("Wood Rod", 0),
    SCISSOR_ROD("Scissor Rod", 1),
    TALON_ROD("Talon Rod", 1),
    NAUTILUS_ROD("Nautilus Rod", 2),
    OGRE_ROD("Ogre Rod", 2),
    GOAT_HEAD("Goat Head", 2),
    EYEBALL_ROD("Eyeball Rod", 3),
    EMBRYO_ROD("Embryo Rod", 3),
    CRYSTAL_ROD("Crystal Rod", 3),
    TWINKLE_ROD("Twinkle Rod", 3);

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
