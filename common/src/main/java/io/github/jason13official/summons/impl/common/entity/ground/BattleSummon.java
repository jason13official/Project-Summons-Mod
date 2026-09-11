package io.github.jason13official.summons.impl.common.entity.ground;

import io.github.jason13official.summons.impl.common.entity.AbstractCompanion;
import io.github.jason13official.summons.impl.common.entity.OwnerStatBonusKit;
import io.github.jason13official.summons.impl.common.entity.ability.CompanionAbility;
import io.github.jason13official.summons.impl.common.evolution.EvoCrystalColor;
import io.github.jason13official.summons.impl.common.evolution.EvolutionForm;
import io.github.jason13official.summons.impl.common.evolution.EvolutionThreshold;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;

/// Battle-Type: physically strong, lots of health and hits heavy
public class BattleSummon extends AbstractGroundCompanion {

  private static final double AURA_BLAST_RADIUS = 3.0;

  // wiki: STR +10 initial, +40 growth
  private static final OwnerStatBonusKit OWNER_STAT_BONUS = new OwnerStatBonusKit(10, 40, 0, 0, 0, 0);

  /// Battle-Type https://gamefaqs.gamespot.com/ps2/925894-castlevania-curse-of-darkness/faqs
  private static final List<CompanionAbility> ABILITIES = List.of(
      // "Sends out a wave of energy that damages surrounding opponents."
      CompanionAbility.base("Aura Blast", 20, (companion, owner) -> {
        AABB area = companion.getBoundingBox().inflate(AURA_BLAST_RADIUS);
        float damage = (float) companion.getAttributeValue(Attributes.ATTACK_DAMAGE);

        for (LivingEntity target : companion.level().getEntitiesOfClass(LivingEntity.class, area,
            e -> e != companion && e != owner && e.isAlive())) {
          target.hurt(companion.damageSources().mobAttack(companion), damage);
        }

        spawnAbilityParticles(companion, ParticleTypes.EXPLOSION, 2);
      }),
      // "Used to lift up heavy objects... all battle type IDs will get this skill after a
      // certain point in the game" -> unlike every other ability here, this isn't tied to any
      // one evolution branch, so it's ungated by form (empty requiredForms), just by level
      new CompanionAbility("Brute Force", 40, Set.of(), 5, (companion, owner) -> {
        BlockPos center = companion.blockPosition();
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-1, -1, -1), center.offset(1, 2, 1))) {
          Block block = companion.level().getBlockState(pos).getBlock();
          if (block == Blocks.IRON_DOOR || block == Blocks.IRON_TRAPDOOR) {
            companion.level().removeBlock(pos, false);
            spawnAbilityParticles(companion, ParticleTypes.POOF, 10);
            break;
          }
        }
      }),
      // "Rains swords down on your enemies... lasts a long time and does decent dmg."
      CompanionAbility.gated("Heavenly Sword", 25, Form.SPEED_MAIL, 5, (companion, owner) -> {
        LivingEntity target = findNearestTarget(companion, owner, AURA_BLAST_RADIUS * 2.0);
        if (target == null) {
          return;
        }

        float damage = (float) companion.getAttributeValue(Attributes.ATTACK_DAMAGE) * 1.3F;
        target.hurt(companion.damageSources().mobAttack(companion), damage);
        spawnAbilityParticles(target, ParticleTypes.CRIT, 12);
      }),
      // "Shakes the ground and causes some damage."
      CompanionAbility.gated("Hip Press", 30, Form.GOLEM, 5, (companion, owner) -> {
        LivingEntity target = findNearestTarget(companion, owner, AURA_BLAST_RADIUS);
        if (target == null) {
          return;
        }

        float damage = (float) companion.getAttributeValue(Attributes.ATTACK_DAMAGE) * 1.5F;
        target.hurt(companion.damageSources().mobAttack(companion), damage);
        target.knockback(0.8, companion.getX() - target.getX(), companion.getZ() - target.getZ());
        spawnAbilityParticles(target, ParticleTypes.CRIT, 8);
      }),
      // upgraded Hip Press ("Succesfully complete 100 chain combos as Golem" -> no Chain
      // Attack system exists yet, proxied as a level gate instead)
      CompanionAbility.gated("Hip Press Lv.2", 30, Form.GOLEM, 10, (companion, owner) -> {
        LivingEntity target = findNearestTarget(companion, owner, AURA_BLAST_RADIUS);
        if (target == null) {
          return;
        }

        float damage = (float) companion.getAttributeValue(Attributes.ATTACK_DAMAGE) * 2.0F;
        target.hurt(companion.damageSources().mobAttack(companion), damage);
        target.knockback(1.2, companion.getX() - target.getX(), companion.getZ() - target.getZ());
        spawnAbilityParticles(target, ParticleTypes.CRIT, 12);
      }),
      // "Hector gets on Iyeti's shoulders and rides around... You can also attack with
      // Iyeti." -> owner literally mounts the companion for the ability's busy duration,
      // auto-dismounting once it ends (see #tick)
      CompanionAbility.gated("Shoulder Ride", 100, Form.IYTEI, 8, (companion, owner) -> {
        owner.startRiding(companion, true);
        spawnAbilityParticles(owner, ParticleTypes.CLOUD, 6);
      }),
      // "Iyeti screams and stuns surrounding enemies." TODO: no true stun exists, proxied as
      // a near-total AoE Slowness
      CompanionAbility.gated("Ultra Scream", 20, Form.IYTEI, 8, (companion, owner) -> {
        AABB area = companion.getBoundingBox().inflate(AURA_BLAST_RADIUS);
        for (LivingEntity target : companion.level().getEntitiesOfClass(LivingEntity.class, area,
            e -> e != companion && e != owner && e.isAlive())) {
          target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 9));
        }
        spawnAbilityParticles(companion, ParticleTypes.SNEEZE, 10);
      }),
      // "Five floating eye bombs pop out above your ID and home in on an enemy."
      CompanionAbility.gated("Homing Eye", 30, Form.JUGGERNAUT, 8, (companion, owner) -> {
        LivingEntity target = findNearestTarget(companion, owner, AURA_BLAST_RADIUS * 2.0);
        if (target == null) {
          return;
        }

        float hit = (float) companion.getAttributeValue(Attributes.ATTACK_DAMAGE) * 0.3F;
        for (int i = 0; i < 5; i++) {
          target.hurt(companion.damageSources().mobAttack(companion), hit);
        }
        spawnAbilityParticles(target, ParticleTypes.CRIT, 10);
      }),
      // "Only Juggernaut can learn this skill" -> upgraded Brute Force, breaks a wider range
      // of blocks over a bigger area
      CompanionAbility.gated("Brute Force Lv.2", 40, Form.JUGGERNAUT, 10, (companion, owner) -> {
        BlockPos center = companion.blockPosition();
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-2, -1, -2), center.offset(2, 3, 2))) {
          Block block = companion.level().getBlockState(pos).getBlock();
          if (block == Blocks.IRON_DOOR || block == Blocks.IRON_TRAPDOOR || block == Blocks.IRON_BARS) {
            companion.level().removeBlock(pos, false);
            spawnAbilityParticles(companion, ParticleTypes.POOF, 14);
            break;
          }
        }
      }),
      // "Sends a wave of energy out in front of the ID, causing decent damage" -> shared by
      // Rasetz and Corpsey per the FAQ
      CompanionAbility.gated("Grand Wave", 15, Form.RASETZ, 10, BattleSummon::grandWave),
      CompanionAbility.gated("Grand Wave", 15, Form.CORPSEY, 10, BattleSummon::grandWave),
      // "Shoots small pieces of blue fire that home in on enemies and cause pretty good
      // damage."
      CompanionAbility.gated("Glow Soul", 35, Form.RASETZ, 10, (companion, owner) -> {
        LivingEntity target = findNearestTarget(companion, owner, AURA_BLAST_RADIUS * 2.0);
        if (target == null) {
          return;
        }

        float hit = (float) companion.getAttributeValue(Attributes.ATTACK_DAMAGE) * 0.4F;
        for (int i = 0; i < 5; i++) {
          target.hurt(companion.damageSources().mobAttack(companion), hit);
        }
        spawnAbilityParticles(target, ParticleTypes.SOUL_FIRE_FLAME, 10);
      }),
      // "Corpsey unleashes a torrent of bone shards that rip through enemies... takes no
      // damage while using it." -> the companion gets a brief Resistance buff to match
      CompanionAbility.gated("Bone Storm", 20, Form.CORPSEY, 10, (companion, owner) -> {
        companion.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 20, 4));
        AABB area = companion.getBoundingBox().inflate(AURA_BLAST_RADIUS * 1.5);
        float damage = (float) companion.getAttributeValue(Attributes.ATTACK_DAMAGE) * 1.3F;
        for (LivingEntity target : companion.level().getEntitiesOfClass(LivingEntity.class, area,
            e -> e != companion && e != owner && e.isAlive())) {
          target.hurt(companion.damageSources().mobAttack(companion), damage);
        }
        spawnAbilityParticles(companion, ParticleTypes.CRIT, 16);
      }),
      // "Ironside's hands shoot out at the enemy... limited area, but does a lot of damage."
      CompanionAbility.gated("Chain Punch", 25, Form.IRONSIDE, 10, (companion, owner) -> {
        LivingEntity target = findNearestTarget(companion, owner, AURA_BLAST_RADIUS);
        if (target == null) {
          return;
        }

        float damage = (float) companion.getAttributeValue(Attributes.ATTACK_DAMAGE) * 2.2F;
        target.hurt(companion.damageSources().mobAttack(companion), damage);
        spawnAbilityParticles(target, ParticleTypes.CRIT, 14);
      }),
      // "Ironside sprays the room with machine gun fire... can be damaged while shooting" -
      // unlike Bone Storm, no self-immunity here
      CompanionAbility.gated("Machine Gun Shot", 25, Form.IRONSIDE, 10, (companion, owner) -> {
        AABB area = companion.getBoundingBox().inflate(AURA_BLAST_RADIUS * 1.5);
        float damage = (float) companion.getAttributeValue(Attributes.ATTACK_DAMAGE) * 0.8F;
        for (LivingEntity target : companion.level().getEntitiesOfClass(LivingEntity.class, area,
            e -> e != companion && e != owner && e.isAlive())) {
          target.hurt(companion.damageSources().mobAttack(companion), damage);
        }
        spawnAbilityParticles(companion, ParticleTypes.CRIT, 16);
      }),
      // "Liquid Golem turns into a giant blob of mercury and flies around the room, hitting
      // enemies randomly... little to no damage" -> kept intentionally weak per the FAQ
      CompanionAbility.gated("Mercury Sphere", 25, Form.LIQUID_GOLEM, 10, (companion, owner) -> {
        LivingEntity target = findNearestTarget(companion, owner, AURA_BLAST_RADIUS * 2.0);
        if (target != null) {
          target.hurt(companion.damageSources().mobAttack(companion), (float) companion.getAttributeValue(Attributes.ATTACK_DAMAGE) * 0.5F);
          spawnAbilityParticles(target, ParticleTypes.DRIPPING_LAVA, 8);
        }
      }),
      // "Liquid Golem turns burning hot and runs around the room... does less damage than
      // Aura Blast" -> a brief self-buff plus a single, deliberately weak hit
      CompanionAbility.gated("Magma Form", 40, Form.LIQUID_GOLEM, 10, (companion, owner) -> {
        companion.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 100, 0));
        LivingEntity target = findNearestTarget(companion, owner, AURA_BLAST_RADIUS);
        if (target != null) {
          target.setRemainingFireTicks(40);
          target.hurt(companion.damageSources().mobAttack(companion), (float) companion.getAttributeValue(Attributes.ATTACK_DAMAGE) * 0.6F);
        }
        spawnAbilityParticles(companion, ParticleTypes.LAVA, 10);
      })
  );

  private static void grandWave(AbstractCompanion companion, LivingEntity owner) {
    LivingEntity target = findNearestTarget(companion, owner, AURA_BLAST_RADIUS * 2.5);
    if (target == null) {
      return;
    }

    float damage = (float) companion.getAttributeValue(Attributes.ATTACK_DAMAGE) * 0.8F;
    target.hurt(companion.damageSources().mobAttack(companion), damage);
    spawnAbilityParticles(target, ParticleTypes.SWEEP_ATTACK, 6);
  }

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
  public void tick() {
    super.tick();
    if (!this.level().isClientSide && this.isVehicle() && !this.isAbilityBusy()) {
      this.ejectPassengers(); // Shoulder Ride's busy window doubles as the ride duration
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
  public EvolutionForm[] allForms() {
    return Form.values();
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
