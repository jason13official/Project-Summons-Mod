package io.github.jason13official.summons.impl.common.entity.flying;

import io.github.jason13official.summons.impl.common.entity.AbstractCompanion;
import io.github.jason13official.summons.impl.common.entity.OwnerStatBonusKit;
import io.github.jason13official.summons.impl.common.entity.ability.CompanionAbility;
import io.github.jason13official.summons.impl.common.entity.ai.goal.attack.CompanionRangedAttackGoal;
import io.github.jason13official.summons.impl.common.entity.ground.AbstractGroundCompanion;
import io.github.jason13official.summons.impl.common.evolution.EvoCrystalColor;
import io.github.jason13official.summons.impl.common.evolution.EvolutionForm;
import io.github.jason13official.summons.impl.common.evolution.EvolutionThreshold;
import java.util.Comparator;
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

  // wiki: STR +5/CON +2 initial, +20/+6 growth
  private static final OwnerStatBonusKit OWNER_STAT_BONUS = new OwnerStatBonusKit(5, 20, 2, 6, 0, 0);

  private static final double SPELL_RADIUS = 12.0;
  private static final double DIRECT_ATTACK_RADIUS = 10.0;
  private static final int DIRECT_ATTACK_COOLDOWN = 40;

  /// client-side render animation phase; one model instance is shared by every Mage
  /// Summon, so this state has to live on the entity, not the model.
  public final AnimationState idleAnimationState = new AnimationState();
  public final AnimationState flyAnimationState = new AnimationState();

  /// Mage-type https://gamefaqs.gamespot.com/ps2/925894-castlevania-curse-of-darkness/faqs
  private static final List<CompanionAbility> ABILITIES = List.of(
      // "Stops time for enemies, leaving Hector to beat on them unopposed." TODO: no true
      // freeze exists, proxied as heavy AoE slowness
      CompanionAbility.base("Time Stop", 40, (companion, owner) -> {
        AABB area = companion.getBoundingBox().inflate(SPELL_RADIUS);
        for (LivingEntity target : companion.level().getEntitiesOfClass(LivingEntity.class, area,
            e -> e != companion && e != owner && e.isAlive())) {
          target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 9));
        }
        spawnAbilityParticles(companion, ParticleTypes.END_ROD, 10);
      }),
      // "Rains lightning down on a foe (sometimes hits multiple enemies)."
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
      // "Sends out three balls of fire" that revolve around Hector and burn on contact
      CompanionAbility.gated("Floating B", 20, Form.SCISSOR_ROD, 5, (companion, owner) ->
          companion.spawnOrbitingBits(3, 2.5, 0.3, 1.0,
              (float) companion.getAttributeValue(Attributes.ATTACK_DAMAGE) * 0.4F, ParticleTypes.FLAME, false, 100)),
      // "Shoots a beam of fire in a slow sweep across the room... excellent against undead."
      CompanionAbility.gated("Sorcery Flame", 20, Form.SCISSOR_ROD, 5, (companion, owner) -> {
        AABB area = companion.getBoundingBox().inflate(SPELL_RADIUS);
        float damage = (float) companion.getAttributeValue(Attributes.ATTACK_DAMAGE) * 0.6F;
        for (LivingEntity target : companion.level().getEntitiesOfClass(LivingEntity.class, area,
            e -> e != companion && e != owner && e.isAlive())) {
          target.setRemainingFireTicks(80);
          target.hurt(companion.damageSources().magic(), damage);
        }
        spawnAbilityParticles(companion, ParticleTypes.FLAME, 14);
      }),
      // "Creates a glowing blue ring around Hector that damages anything that gets close."
      CompanionAbility.gated("Circle Scissors", 20, Form.TALON_ROD, 5, (companion, owner) -> {
        AABB area = owner.getBoundingBox().inflate(3.0);
        float damage = (float) companion.getAttributeValue(Attributes.ATTACK_DAMAGE) * 0.5F;
        for (LivingEntity target : companion.level().getEntitiesOfClass(LivingEntity.class, area,
            e -> e != companion && e != owner && e.isAlive())) {
          target.hurt(companion.damageSources().magic(), damage);
        }
        spawnAbilityParticles(owner, ParticleTypes.CRIT, 10);
      }),
      // "Freezes an enemy in a block of ice... causes ice damage."
      CompanionAbility.gated("Freeze", 30, Form.TALON_ROD, 5, (companion, owner) -> {
        LivingEntity target = findNearestTarget(companion, owner, SPELL_RADIUS);
        if (target == null) {
          return;
        }

        target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 3));
        target.hurt(companion.damageSources().mobAttack(companion),
            (float) companion.getAttributeValue(Attributes.ATTACK_DAMAGE) * 0.5F);
        spawnAbilityParticles(target, ParticleTypes.SNOWFLAKE, 10);
      }),
      // "This is the skill that allows the Mage ID to kill blood skeletons... affects all
      // undead." TODO: no Blood Skeleton equivalent mob exists, just hits hard for now
      CompanionAbility.gated("Purify", 40, Form.NAUTILUS_ROD, 8, (companion, owner) -> {
        LivingEntity target = findNearestTarget(companion, owner, SPELL_RADIUS);
        if (target == null) {
          return;
        }

        target.hurt(companion.damageSources().magic(), (float) companion.getAttributeValue(Attributes.ATTACK_DAMAGE) * 2.0F);
        spawnAbilityParticles(target, ParticleTypes.END_ROD, 14);
      }),
      // "Summons three small laser-cannons that shoot at enemies for decent damage."
      CompanionAbility.gated("Satellite B", 20, Form.NAUTILUS_ROD, 8, (companion, owner) ->
          companion.spawnOrbitingBits(3, 3.0, 0.4, 1.2,
              (float) companion.getAttributeValue(Attributes.ATTACK_DAMAGE) * 0.5F, ParticleTypes.END_ROD, false, 100)),
      // "Shoots out holy chain-lightning that hits enemies in front of it at random."
      // -> hits up to 3 nearby targets in one cast
      CompanionAbility.gated("Agnea", 20, Form.OGRE_ROD, 8, (companion, owner) -> {
        AABB area = companion.getBoundingBox().inflate(SPELL_RADIUS);
        float damage = (float) companion.getAttributeValue(Attributes.ATTACK_DAMAGE) * 0.6F;
        companion.level().getEntitiesOfClass(LivingEntity.class, area, e -> e != companion && e != owner && e.isAlive())
            .stream().sorted(Comparator.comparingDouble(companion::distanceToSqr)).limit(3)
            .forEach(target -> {
              target.hurt(companion.damageSources().magic(), damage);
              spawnAbilityParticles(target, ParticleTypes.ELECTRIC_SPARK, 8);
            });
      }),
      // "Summons three floating saucers that attack as Hector attacks."
      CompanionAbility.gated("Synchron Saucer B", 20, Form.OGRE_ROD, 8, (companion, owner) ->
          companion.spawnOrbitingBits(3, 2.0, 0.5, 0.8,
              (float) companion.getAttributeValue(Attributes.ATTACK_DAMAGE) * 0.45F, ParticleTypes.ELECTRIC_SPARK, false, 100)),
      // "Summons blades that rapidly fly around Hector, damaging any enemies that come
      // near... also protect above Hector."
      CompanionAbility.gated("Argent B", 20, Form.GOAT_HEAD, 8, (companion, owner) ->
          companion.spawnOrbitingBits(4, 1.5, 0.8, 1.5,
              (float) companion.getAttributeValue(Attributes.ATTACK_DAMAGE) * 0.4F, ParticleTypes.CRIT, false, 100)),
      // "Summons a serpent of fire that hits enemies at random, popping in and out of the
      // ground" -> hits up to 3 nearby targets
      CompanionAbility.gated("Salamander", 30, Form.GOAT_HEAD, 8, (companion, owner) -> {
        AABB area = companion.getBoundingBox().inflate(SPELL_RADIUS);
        float damage = (float) companion.getAttributeValue(Attributes.ATTACK_DAMAGE) * 0.7F;
        companion.level().getEntitiesOfClass(LivingEntity.class, area, e -> e != companion && e != owner && e.isAlive())
            .stream().sorted(Comparator.comparingDouble(companion::distanceToSqr)).limit(3)
            .forEach(target -> {
              target.setRemainingFireTicks(60);
              target.hurt(companion.damageSources().magic(), damage);
              spawnAbilityParticles(target, ParticleTypes.FLAME, 10);
            });
      }),
      // "Boosts Hector's ATK stat for a short time... the only Mage skill that directly
      // enhances Hector's stats."
      CompanionAbility.gated("Tension Boost", 20, Form.EYEBALL_ROD, 10, (companion, owner) -> {
        owner.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 200, 1));
        spawnAbilityParticles(owner, ParticleTypes.CRIT, 10);
      }),
      // "Fires homing lasers at enemies."
      CompanionAbility.gated("Homing", 20, Form.EYEBALL_ROD, 10, (companion, owner) -> {
        LivingEntity target = findNearestTarget(companion, owner, SPELL_RADIUS);
        if (target == null) {
          return;
        }

        float hit = (float) companion.getAttributeValue(Attributes.ATTACK_DAMAGE) * 0.4F;
        for (int i = 0; i < 3; i++) {
          target.hurt(companion.damageSources().magic(), hit);
        }
        spawnAbilityParticles(target, ParticleTypes.END_ROD, 10);
      }),
      // "Summons three floating orbs around Hector that explode on contact with an enemy,
      // causing damage and knockback."
      CompanionAbility.gated("Explosion B", 20, Form.EMBRYO_ROD, 10, (companion, owner) ->
          companion.spawnOrbitingBits(3, 2.0, 0.35, 1.0,
              (float) companion.getAttributeValue(Attributes.ATTACK_DAMAGE) * 1.2F, ParticleTypes.FLASH, true, 100)),
      // "Unleashes a huge explosion that damages enemies over a huge range."
      CompanionAbility.gated("Demonic Disaster", 35, Form.EMBRYO_ROD, 10, (companion, owner) -> {
        AABB area = companion.getBoundingBox().inflate(SPELL_RADIUS * 1.5);
        float damage = (float) companion.getAttributeValue(Attributes.ATTACK_DAMAGE) * 1.5F;
        for (LivingEntity target : companion.level().getEntitiesOfClass(LivingEntity.class, area,
            e -> e != companion && e != owner && e.isAlive())) {
          target.hurt(companion.damageSources().magic(), damage);
        }
        spawnAbilityParticles(companion, ParticleTypes.EXPLOSION, 6);
      }),
      // "Blocks one enemy attack completely." -> proxied as Absorption hearts, not a literal
      // one-hit block
      CompanionAbility.gated("Shield", 20, Form.CRYSTAL_ROD, 10, (companion, owner) -> {
        owner.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 200, 1));
        spawnAbilityParticles(owner, ParticleTypes.END_ROD, 10);
      }),
      // "Summons a meteor from the sky to crash down on an enemy."
      CompanionAbility.gated("Meteo", 40, Form.CRYSTAL_ROD, 10, (companion, owner) -> {
        LivingEntity target = findNearestTarget(companion, owner, SPELL_RADIUS);
        if (target == null) {
          return;
        }

        target.setRemainingFireTicks(60);
        target.hurt(companion.damageSources().magic(), (float) companion.getAttributeValue(Attributes.ATTACK_DAMAGE) * 2.0F);
        spawnAbilityParticles(target, ParticleTypes.FLAME, 16);
      }),
      // "Exactly the same as Meteo but with Holy element instead of Fire."
      CompanionAbility.gated("Twinkle Star", 40, Form.TWINKLE_ROD, 10, (companion, owner) -> {
        LivingEntity target = findNearestTarget(companion, owner, SPELL_RADIUS);
        if (target == null) {
          return;
        }

        target.hurt(companion.damageSources().magic(), (float) companion.getAttributeValue(Attributes.ATTACK_DAMAGE) * 2.0F);
        spawnAbilityParticles(target, ParticleTypes.END_ROD, 16);
      }),
      // "Summons three smiley-faced stars that circle around Hector and do Holy damage to
      // enemies that come close."
      CompanionAbility.gated("Dancing Star", 20, Form.TWINKLE_ROD, 10, (companion, owner) ->
          companion.spawnOrbitingBits(3, 2.2, 0.4, 1.0,
              (float) companion.getAttributeValue(Attributes.ATTACK_DAMAGE) * 0.5F, ParticleTypes.END_ROD, false, 100))
  );

  public MageSummon(EntityType<? extends AbstractCompanion> entityType, Level level) {
    super(entityType, level);
  }

  public static AttributeSupplier.Builder createAttributes() {

    // physically weak per lore ("low DEF... open to enemy attacks"); ATTACK_DAMAGE still
    // carries spell power for the ability kit above, so it's toned down, not gutted
    return AbstractFlyingCompanion.createAttributes().add(Attributes.MAX_HEALTH, (double) 12.0F)
        .add(Attributes.FLYING_SPEED, (double) 0.45F).add(Attributes.MOVEMENT_SPEED, (double) 0.28F)
        .add(Attributes.ATTACK_DAMAGE, (double) 6.0F).add(Attributes.KNOCKBACK_RESISTANCE, (double) 0.0F);
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
    float damage = (float) companion.getAttributeValue(Attributes.ATTACK_DAMAGE) * 0.3F * companion.directAttackDamageMultiplier();
    target.hurt(companion.damageSources().magic(), damage);
    spawnAbilityParticles(target, ParticleTypes.ELECTRIC_SPARK, 6);
    companion.grantDirectAttackExperience();
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
