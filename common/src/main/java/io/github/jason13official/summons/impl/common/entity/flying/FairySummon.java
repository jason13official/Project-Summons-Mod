package io.github.jason13official.summons.impl.common.entity.flying;

import io.github.jason13official.summons.impl.common.entity.OwnerStatBonusKit;
import io.github.jason13official.summons.impl.common.entity.ability.CompanionAbility;
import io.github.jason13official.summons.impl.common.entity.ai.goal.attack.CompanionLevelGatedMeleeAttackGoal;
import io.github.jason13official.summons.impl.common.evolution.EvoCrystalColor;
import io.github.jason13official.summons.impl.common.evolution.EvolutionForm;
import io.github.jason13official.summons.impl.common.evolution.EvolutionThreshold;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.LeverBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/// Fairy-Type: support only, heals/cures the owner. Per wiki, most forms don't attack at
/// all; here it learns a weak poison-tick basic attack at [#DIRECT_ATTACK_MIN_LEVEL].
public class FairySummon extends AbstractFlyingCompanion {

  private static final int DIRECT_ATTACK_MIN_LEVEL = 4;

  // wiki: LCK +1 initial, +14 growth
  private static final OwnerStatBonusKit OWNER_STAT_BONUS = new OwnerStatBonusKit(0, 0, 0, 0, 1, 14);

  /// Fairy-type https://gamefaqs.gamespot.com/ps2/925894-castlevania-curse-of-darkness/faqs
  private static final List<CompanionAbility> ABILITIES = List.of(
      // "Allows Hector to open chests." TODO: no chest-opening logic exists, proxied as a
      // temporary Luck boost (better loot access)
      CompanionAbility.base("Unlock", 20, (companion, owner) -> {
        owner.addEffect(new MobEffectInstance(MobEffects.LUCK, 200, 0));
        spawnAbilityParticles(owner, ParticleTypes.HAPPY_VILLAGER, 5);
      }),
      // "Heals 20 HP."
      CompanionAbility.base("Heal Lv.1", 10, (companion, owner) -> {
        owner.heal(2.0F);
        spawnAbilityParticles(owner, ParticleTypes.HEART, 5);
      }),
      // "Heals approximately 50 HP gradually over a short period of time."
      CompanionAbility.gated("Time Heal", 6, Form.LEAFFLE, 5, (companion, owner) -> {
        owner.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 100, 1));
        spawnAbilityParticles(owner, ParticleTypes.HEART, 5);
      }),
      // "Fairy attacks enemies by poisoning them" -> automatic in-game, triggered here instead
      CompanionAbility.gated("Poison Powder", 0, Form.LEAFFLE, 5, (companion, owner) -> {
        AABB area = companion.getBoundingBox().inflate(3.0);
        for (LivingEntity target : companion.level().getEntitiesOfClass(LivingEntity.class, area,
            e -> e != companion && e != owner && e.isAlive())) {
          target.addEffect(new MobEffectInstance(MobEffects.POISON, 60, 0));
        }
        spawnAbilityParticles(companion, ParticleTypes.WITCH, 8);
      }),
      // "Heals 50 HP."
      CompanionAbility.gated("Heal Lv.2", 20, Form.HERBEST, 5, (companion, owner) -> {
        owner.heal(6.0F);
        spawnAbilityParticles(owner, ParticleTypes.HEART, 5);
      }),
      // "Cures poison automatically" -> Herbest's cure is poison-only; Curse Breaker (Killer
      // Bee) and Stone Breaker (Hornet) cover the other two status ailments
      CompanionAbility.gated("Antidote", 5, Form.HERBEST, 5, (companion, owner) ->
          owner.removeEffect(MobEffects.POISON)),
      // "Heals 100 HP instantly."
      CompanionAbility.gated("Heal Lv.3", 30, Form.KILLER_BEE, 5, (companion, owner) -> {
        owner.heal(12.0F);
        spawnAbilityParticles(owner, ParticleTypes.HEART, 8);
      }),
      // "Removes curse automatically." TODO: no dedicated Curse status -> proxied as clearing
      // Wither, the closest existing curse-like debuff
      CompanionAbility.gated("Curse Breaker", 5, Form.KILLER_BEE, 5, (companion, owner) ->
          owner.removeEffect(MobEffects.WITHER)),
      // "Creates a small circle that restores HP rapidly when Hector stands inside... 10
      // hearts for 100 HP" -> the best heal in the game per the FAQ
      CompanionAbility.gated("Healing Field", 10, Form.HONEY_BEE, 8, (companion, owner) -> {
        owner.heal(12.0F);
        owner.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 60, 1));
        spawnAbilityParticles(owner, ParticleTypes.HEART, 12);
      }),
      // "Cures stoning automatically." TODO: no petrification status exists in this mod
      // either, so this never actually triggers yet -> kept for completeness
      CompanionAbility.gated("Stone Breaker", 5, Form.HORNET, 5, (companion, owner) -> {
        owner.removeEffect(MobEffects.DIG_SLOWDOWN);
        owner.removeEffect(MobEffects.WEAKNESS);
      }),
      // "Play a slot machine to determine how much HP is healed... sometimes it doesn't
      // even work." -> a random amount, sometimes zero
      CompanionAbility.gated("Lucky Slot", 10, Form.HORNET, 5, (companion, owner) -> {
        float healed = companion.getRandom().nextInt(4) * 4.0F; // 0, 4, 8 or 12
        if (healed > 0.0F) {
          owner.heal(healed);
        }
        spawnAbilityParticles(owner, ParticleTypes.HAPPY_VILLAGER, 6);
      }),
      // "Fairy will press hard to reach buttons for you... you need it to get the whole
      // Castle." -> literally presses the nearest lever/button in range
      CompanionAbility.gated("Press It and See", 10, Form.PROBOSCIS_FAIRY, 10, (companion, owner) -> {
        BlockPos center = companion.blockPosition();
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-3, -2, -3), center.offset(3, 2, 3))) {
          BlockState state = companion.level().getBlockState(pos);
          Block block = state.getBlock();
          Player player = owner instanceof Player p ? p : null;

          if (block instanceof ButtonBlock button) {
            button.press(state, companion.level(), pos, player);
            spawnAbilityParticles(companion, ParticleTypes.HAPPY_VILLAGER, 6);
            break;
          } else if (block instanceof LeverBlock lever) {
            lever.pull(state, companion.level(), pos, player);
            spawnAbilityParticles(companion, ParticleTypes.HAPPY_VILLAGER, 6);
            break;
          }
        }
      }),
      // "Heals 1 HP, then disappears from use for a brief time." -> the FAQ author calls this
      // the worst skill in the game, kept just as weak here
      CompanionAbility.gated("Just a Little", 5, Form.PROBOSCIS_FAIRY, 10, (companion, owner) -> {
        owner.heal(0.5F);
        spawnAbilityParticles(owner, ParticleTypes.HEART, 2);
      }),
      // "Drops a bomb that damages anyone in range, including Hector."
      CompanionAbility.gated("Skull Bomb", 15, Form.PROBOSCIS_FAIRY, 10, (companion, owner) -> {
        AABB area = companion.getBoundingBox().inflate(3.0);
        float damage = (float) companion.getAttributeValue(Attributes.ATTACK_DAMAGE) + 1.0F;
        for (LivingEntity target : companion.level().getEntitiesOfClass(LivingEntity.class, area,
            e -> e != companion && e.isAlive())) { // includes owner, matching "Hector will get hit too"
          target.hurt(companion.damageSources().mobAttack(companion), damage);
        }
        spawnAbilityParticles(companion, ParticleTypes.POOF, 10);
      }),
      // "Cures all status ailments automatically... an all in one protection." Broader than
      // Antidote/Curse Breaker/Stone Breaker -> clears every harmful effect at once
      CompanionAbility.gated("Refresh", 5, Form.PROBOSCIS_FAIRY, 10, (companion, owner) -> {
        for (MobEffectInstance effect : new ArrayList<>(owner.getActiveEffects())) {
          if (effect.getEffect().value().getCategory() == MobEffectCategory.HARMFUL) {
            owner.removeEffect(effect.getEffect());
          }
        }
        spawnAbilityParticles(owner, ParticleTypes.HEART, 8);
      }),
      // "Pay the Fairy $5,000 gold for full HP." TODO: no gold-cost economy, proxied as a
      // free full heal
      CompanionAbility.gated("Gold Heal", 30, Form.TIRAMISU, 10, (companion, owner) -> {
        owner.heal(owner.getMaxHealth());
        spawnAbilityParticles(owner, ParticleTypes.HAPPY_VILLAGER, 12);
      }),
      // "Allows Hector to read ancient script." TODO: no such structure exists, proxied as
      // revealing nearby hostiles (Glowing) plus owner Night Vision
      CompanionAbility.gated("Decipher", 20, Form.TIRAMISU, 10, (companion, owner) -> {
        owner.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 1200, 0));
        AABB area = companion.getBoundingBox().inflate(16.0);
        for (LivingEntity target : companion.level().getEntitiesOfClass(LivingEntity.class, area,
            e -> e != companion && e != owner && e.isAlive())) {
          target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 200, 0));
        }
        spawnAbilityParticles(owner, ParticleTypes.ENCHANT, 10);
      }),
      // "Renders Hector invincible for about 10 seconds." -> proxied with a heavy, timed
      // Resistance buff rather than a literal invulnerability flag
      CompanionAbility.gated("Invincible Vase", 40, Form.TIRAMISU, 10, (companion, owner) -> {
        owner.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 200, 4));
        spawnAbilityParticles(owner, ParticleTypes.TOTEM_OF_UNDYING, 14);
      }),
      // "Causes enemies to drop healing spheres when hit for a brief period... I see no use
      // for this at all." -> kept intentionally weak per the FAQ's own assessment
      CompanionAbility.gated("Healing Drop", 8, Form.TIARA, 10, (companion, owner) -> {
        owner.heal(3.0F);
        spawnAbilityParticles(owner, ParticleTypes.HAPPY_VILLAGER, 4);
      }),
      // "Renders Hector invisible for a brief period of time."
      CompanionAbility.gated("Crystal Skull", 25, Form.TIARA, 10, (companion, owner) -> {
        owner.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 100, 0));
        spawnAbilityParticles(owner, ParticleTypes.SMOKE, 10);
      }),
      // "Comet Star draws stars in the air and shoots them at enemies... ultra-slow."
      CompanionAbility.gated("Twinkle Star", 20, Form.COMET_STAR, 10, (companion, owner) -> {
        LivingEntity target = findNearestTarget(companion, owner, 8.0);
        if (target == null) {
          return;
        }

        target.hurt(companion.damageSources().magic(), (float) companion.getAttributeValue(Attributes.ATTACK_DAMAGE) + 1.0F);
        spawnAbilityParticles(target, ParticleTypes.END_ROD, 8);
      }),
      // "Comet Star puts nearby enemies to sleep... gives you a huge advantage" -> the FAQ's
      // one genuinely useful Comet Star skill
      CompanionAbility.gated("Lullaby", 15, Form.COMET_STAR, 10, (companion, owner) -> {
        AABB area = companion.getBoundingBox().inflate(6.0);
        for (LivingEntity target : companion.level().getEntitiesOfClass(LivingEntity.class, area,
            e -> e != companion && e != owner && e.isAlive())) {
          target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 160, 9));
          target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 160, 2));
          target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 160, 0));
        }
        spawnAbilityParticles(companion, ParticleTypes.SNEEZE, 10);
      }),
      // "Heals 200 HP."
      CompanionAbility.gated("Heal Lv.4", 50, Form.COMET_STAR, 10, (companion, owner) -> {
        owner.heal(24.0F);
        spawnAbilityParticles(owner, ParticleTypes.HEART, 14);
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
  protected void registerGoals() {
    super.registerGoals();
    this.goalSelector.addGoal(1, new CompanionLevelGatedMeleeAttackGoal(this, 1.0, true, DIRECT_ATTACK_MIN_LEVEL));
  }

  @Override
  public boolean doHurtTarget(Entity entity) {
    this.swing(InteractionHand.MAIN_HAND);
    if (!(entity instanceof LivingEntity target)) {
      return false;
    }

    // ATTACK_DAMAGE is 0 per lore, nothing to scale numerically -> poison duration scales
    // with level instead, so leveling still means something for Fairy's direct attack
    int duration = (int) (30 * this.directAttackDamageMultiplier());
    target.addEffect(new MobEffectInstance(MobEffects.POISON, duration, 0));
    this.grantDirectAttackExperience();
    return true;
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
  public EvolutionForm[] allForms() {
    return Form.values();
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
