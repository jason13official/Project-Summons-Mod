package io.github.jason13official.summons.impl.common.entity;

import io.github.jason13official.summons.impl.common.entity.ability.CompanionAbility;
import io.github.jason13official.summons.impl.common.entity.ai.goal.target.CompanionOwnerHurtByTargetGoal;
import io.github.jason13official.summons.impl.common.entity.ai.goal.target.CompanionOwnerHurtTargetGoal;
import io.github.jason13official.summons.impl.common.evolution.EvoCrystalColor;
import io.github.jason13official.summons.impl.common.evolution.EvolutionForm;
import io.github.jason13official.summons.impl.common.evolution.EvolutionThreshold;
import io.github.jason13official.summons.impl.common.party.CompanionMode;
import io.github.jason13official.summons.impl.common.party.CompanionParty;
import io.github.jason13official.summons.impl.common.party.CompanionType;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.TraceableEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractCompanion extends PathfinderMob implements TraceableEntity, OwnableEntity {

  // TamableAnimal.class; UUID owner, round-trips via restoreFrom
  private static final EntityDataAccessor<Optional<UUID>> DATA_OWNER_UUID_ID =
      SynchedEntityData.defineId(AbstractCompanion.class, EntityDataSerializers.OPTIONAL_UUID);

  // which Innocent Devil "slot" this instance represents; assigned by CompanionPartyManager
  private static final EntityDataAccessor<Byte> DATA_COMPANION_TYPE_ID =
      SynchedEntityData.defineId(AbstractCompanion.class, EntityDataSerializers.BYTE);

  private static final EntityDataAccessor<Byte> DATA_MODE_ID =
      SynchedEntityData.defineId(AbstractCompanion.class, EntityDataSerializers.BYTE);

  private static final EntityDataAccessor<Byte> DATA_ABILITY_INDEX_ID =
      SynchedEntityData.defineId(AbstractCompanion.class, EntityDataSerializers.BYTE);

  private static final EntityDataAccessor<Boolean> DATA_ABILITY_BUSY_ID =
      SynchedEntityData.defineId(AbstractCompanion.class, EntityDataSerializers.BOOLEAN);

  private int abilityBusyTicks;

  /// Guard field radius for DEFEND mode; shrinks per hit, regenerates over time.
  private static final EntityDataAccessor<Float> DATA_GUARD_FIELD_RADIUS_ID =
      SynchedEntityData.defineId(AbstractCompanion.class, EntityDataSerializers.FLOAT);

  public static final float GUARD_FIELD_MAX_RADIUS = 3.0F;
  public static final float GUARD_FIELD_MIN_RADIUS = 0.75F;
  private static final float GUARD_FIELD_SHRINK_PER_HIT = 0.75F;
  private static final float GUARD_FIELD_REGEN_PER_TICK = 0.01F; // ~4.5s min->max

  private static final double TELEPORT_TO_OWNER_DISTANCE = 24.0;

  private static final EntityDataAccessor<Integer> DATA_CRYSTAL_RED_ID =
      SynchedEntityData.defineId(AbstractCompanion.class, EntityDataSerializers.INT);
  private static final EntityDataAccessor<Integer> DATA_CRYSTAL_BLUE_ID =
      SynchedEntityData.defineId(AbstractCompanion.class, EntityDataSerializers.INT);
  private static final EntityDataAccessor<Integer> DATA_CRYSTAL_GREEN_ID =
      SynchedEntityData.defineId(AbstractCompanion.class, EntityDataSerializers.INT);
  private static final EntityDataAccessor<Integer> DATA_CRYSTAL_YELLOW_ID =
      SynchedEntityData.defineId(AbstractCompanion.class, EntityDataSerializers.INT);
  private static final EntityDataAccessor<Integer> DATA_CRYSTAL_WHITE_ID =
      SynchedEntityData.defineId(AbstractCompanion.class, EntityDataSerializers.INT);

  private static final EntityDataAccessor<String> DATA_EVOLUTION_FORM_ID =
      SynchedEntityData.defineId(AbstractCompanion.class, EntityDataSerializers.STRING);
  // comma-joined form ids ever reached; abilities stay learned once unlocked, even past
  // whatever form originally granted them (matches the wiki: evolving never revokes one)
  private static final EntityDataAccessor<String> DATA_REACHED_FORMS_ID =
      SynchedEntityData.defineId(AbstractCompanion.class, EntityDataSerializers.STRING);

  private static final EntityDataAccessor<Integer> DATA_LEVEL_ID =
      SynchedEntityData.defineId(AbstractCompanion.class, EntityDataSerializers.INT);
  private static final EntityDataAccessor<Integer> DATA_EXPERIENCE_ID =
      SynchedEntityData.defineId(AbstractCompanion.class, EntityDataSerializers.INT);

  public static final int MAX_LEVEL = 99;
  private static final int ABILITY_USE_XP = 5;

  /// "sparking wisp": at 0 Hearts the I.D. doesn't die, it goes inert and floats near the
  /// owner until a Heart revives it. No dedicated wisp entity/model; renderers skip
  /// drawing while this is true (see e.g. BattleSummonRenderer#render) and tick() below
  /// emits particles at its position instead
  private static final EntityDataAccessor<Boolean> DATA_WISP_ID =
      SynchedEntityData.defineId(AbstractCompanion.class, EntityDataSerializers.BOOLEAN);
  private static final float WISP_REVIVE_HEALTH = 1.0F;
  private static final float HEART_HEAL_AMOUNT = 4.0F;

  public AbstractCompanion(EntityType<? extends AbstractCompanion> entityType, Level level) {
    super(entityType, level);
  }

  public static AttributeSupplier.Builder createAttributes() {

    // Cow.class
    return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 10.0D).add(Attributes.MOVEMENT_SPEED, 0.2D);
  }

  @Override
  protected void registerGoals() {
    this.targetSelector.addGoal(1, new CompanionOwnerHurtByTargetGoal(this));
    this.targetSelector.addGoal(2, new CompanionOwnerHurtTargetGoal(this));
    // TODO: face owner's target/last attacker/nearest enemy, even while noAi (DEFEND)
  }

  // region owner
  @Override
  protected void defineSynchedData(SynchedEntityData.Builder builder) {
    super.defineSynchedData(builder);
    builder.define(DATA_OWNER_UUID_ID, Optional.empty());
    builder.define(DATA_COMPANION_TYPE_ID, (byte) CompanionType.FAIRY.ordinal());
    builder.define(DATA_MODE_ID, (byte) CompanionMode.AUTO.ordinal());
    builder.define(DATA_ABILITY_INDEX_ID, (byte) 0);
    builder.define(DATA_ABILITY_BUSY_ID, false);
    builder.define(DATA_GUARD_FIELD_RADIUS_ID, GUARD_FIELD_MAX_RADIUS);
    builder.define(DATA_CRYSTAL_RED_ID, 0);
    builder.define(DATA_CRYSTAL_BLUE_ID, 0);
    builder.define(DATA_CRYSTAL_GREEN_ID, 0);
    builder.define(DATA_CRYSTAL_YELLOW_ID, 0);
    builder.define(DATA_CRYSTAL_WHITE_ID, 0);
    builder.define(DATA_EVOLUTION_FORM_ID, this.baseForm().id());
    builder.define(DATA_REACHED_FORMS_ID, this.baseForm().id());
    builder.define(DATA_LEVEL_ID, 1);
    builder.define(DATA_EXPERIENCE_ID, 0);
    builder.define(DATA_WISP_ID, false);
  }

  @Override
  public void tick() {
    super.tick();

    if (this.level().isClientSide) {
      if (this.isWisp()) {
        this.spawnWispParticles();
      } else if (this.getMode() == CompanionMode.DEFEND) {
        this.spawnGuardFieldParticles();
      }
      return;
    }

    this.teleportToOwner();
    if (this.isRemoved()) {
      return; // relocated into the owner's dimension; this instance was replaced
    }

    if (this.abilityBusyTicks > 0 && --this.abilityBusyTicks == 0) {
      this.entityData.set(DATA_ABILITY_BUSY_ID, false);
    }

    if (this.getMode() == CompanionMode.DEFEND) {
      if (this.getGuardFieldRadius() < GUARD_FIELD_MAX_RADIUS) {
        this.setGuardFieldRadius(this.getGuardFieldRadius() + GUARD_FIELD_REGEN_PER_TICK);
      }
    } else if (this.getGuardFieldRadius() != GUARD_FIELD_MAX_RADIUS) {
      this.setGuardFieldRadius(GUARD_FIELD_MAX_RADIUS); // reset so re-entering DEFEND always starts full
    }
  }

  /// light rising around the Guard Field edge, plus a few inside it
  private void spawnGuardFieldParticles() {
    float radius = this.getGuardFieldRadius();

    if (this.random.nextInt(4) == 0) {
      this.spawnGuardFieldParticle(radius);
    }

    if (this.random.nextInt(8) == 0) {
      // sqrt so points land uniformly across the disc's area, not bunched at the center
      this.spawnGuardFieldParticle(radius * (float) Math.sqrt(this.random.nextDouble()));
    }
  }

  private void spawnGuardFieldParticle(float distanceFromCenter) {
    double angle = this.random.nextDouble() * Math.PI * 2.0;
    double x = this.getX() + Math.cos(angle) * distanceFromCenter;
    double z = this.getZ() + Math.sin(angle) * distanceFromCenter;
    this.level().addParticle(ParticleTypes.END_ROD, x, this.getY() + 0.05, z, 0.0, 0.03, 0.0);
  }

  /// the only "visual" a sparking wisp has - a few sparkles drifting around its position
  private void spawnWispParticles() {
    double x = this.getX() + (this.random.nextDouble() - 0.5) * 0.6;
    double y = this.getY() + this.random.nextDouble() * this.getBbHeight();
    double z = this.getZ() + (this.random.nextDouble() - 0.5) * 0.6;
    this.level().addParticle(ParticleTypes.SOUL, x, y, z, 0.0, 0.02, 0.0);
  }

  /// safety net for dimension changes, respawns, or falling too far behind
  private void teleportToOwner() {
    if (!(this.level() instanceof ServerLevel level)) {
      return;
    }

    LivingEntity owner = this.getOwner();
    if (owner == null || !(owner.level() instanceof ServerLevel ownerLevel)) {
      return;
    }

    boolean differentDimension = !level.dimension().equals(ownerLevel.dimension());
    if (!differentDimension && this.distanceTo(owner) <= TELEPORT_TO_OWNER_DISTANCE) {
      return;
    }

    double x = owner.getX() + (this.random.nextFloat() * 2.0F - 1.0F);
    double z = owner.getZ() + (this.random.nextFloat() * 2.0F - 1.0F);
    this.teleportTo(ownerLevel, x, owner.getY(), z, Set.of(), owner.getYRot(), 0.0F);
  }

  // region guard field
  public float getGuardFieldRadius() {
    return this.entityData.get(DATA_GUARD_FIELD_RADIUS_ID);
  }

  private void setGuardFieldRadius(float radius) {
    this.entityData.set(DATA_GUARD_FIELD_RADIUS_ID,
        Math.min(GUARD_FIELD_MAX_RADIUS, Math.max(GUARD_FIELD_MIN_RADIUS, radius)));
  }

  /// a sparking wisp is already "spent"; DEFEND mode blocks damage and shrinks the field.
  /// Bypass-invulnerability sources (void, /kill, creative) still go through either way.
  @Override
  public boolean hurt(DamageSource source, float amount) {
    if (!this.level().isClientSide && !source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
      if (this.isWisp()) {
        return false;
      }

      if (this.getMode() == CompanionMode.DEFEND) {
        this.setGuardFieldRadius(this.getGuardFieldRadius() - GUARD_FIELD_SHRINK_PER_HIT);
        return false;
      }
    }

    return super.hurt(source, amount);
  }

  /// protects an owner standing inside a DEFEND-mode companion's field; also shrinks it,
  /// same as a direct hit would
  public static boolean isProtectedByGuardField(LivingEntity owner) {
    List<AbstractCompanion> nearby = owner.level().getEntitiesOfClass(AbstractCompanion.class,
        owner.getBoundingBox().inflate(GUARD_FIELD_MAX_RADIUS),
        companion -> companion.getMode() == CompanionMode.DEFEND && owner.getUUID().equals(companion.getOwnerUUID()));

    for (AbstractCompanion companion : nearby) {
      if (companion.distanceTo(owner) <= companion.getGuardFieldRadius()) {
        companion.setGuardFieldRadius(companion.getGuardFieldRadius() - GUARD_FIELD_SHRINK_PER_HIT);
        return true;
      }
    }

    return false;
  }
  // endregion guard field

  // region wisp
  public boolean isWisp() {
    return this.entityData.get(DATA_WISP_ID);
  }

  /// intercepts a lethal hit: I.D.s "cannot permanently die" (wiki), they go inert instead.
  /// The *only* way to fully remove a companion is the dismiss keybind; even a bypass-
  /// invulnerability source (void, /kill, creative) doesn't discard it here, it just gets
  /// snapshotted back into the party like a dismiss, flagged as a wisp for next time.
  @Override
  public void die(DamageSource source) {
    if (this.level().isClientSide || this.isWisp()) {
      super.die(source);
      return;
    }

    if (source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
      this.retreatToPartyAsWisp();
      return;
    }

    this.entityData.set(DATA_WISP_ID, true);
    this.setHealth(WISP_REVIVE_HEALTH);
    this.setNoAi(true);
  }

  /// used when something would otherwise truly remove the entity (void, /kill, creative);
  /// stores it back in its party slot exactly like the dismiss keybind, but pre-marked as a
  /// wisp so the next summon still needs a Heart. Falls back to a real discard only if there's
  /// no owner to return it to (e.g. the owning player no longer exists).
  private void retreatToPartyAsWisp() {
    if (!(this.getOwner() instanceof ServerPlayer player)) {
      super.die(this.damageSources().generic());
      return;
    }

    this.entityData.set(DATA_WISP_ID, true);

    CompanionParty party = CompanionParty.of(player);
    party.putSnapshot(this.getCompanionType(), this.saveWithoutId(new CompoundTag()));
    party.clearActiveType();
    this.discard();
  }

  /// a Heart revives it; only meaningful while [#isWisp]
  public void reviveFromWisp() {
    if (!this.isWisp()) {
      return;
    }

    this.entityData.set(DATA_WISP_ID, false);
    this.setNoAi(this.getMode() == CompanionMode.DEFEND);
  }

  /// what touching a Heart does: revives from wisp first if needed, then heals
  public void consumeHeart() {
    this.reviveFromWisp();
    this.heal(HEART_HEAL_AMOUNT);
  }
  // endregion wisp

  // region evolution
  public int getCrystalPoints(EvoCrystalColor color) {
    return this.entityData.get(switch (color) {
      case RED -> DATA_CRYSTAL_RED_ID;
      case BLUE -> DATA_CRYSTAL_BLUE_ID;
      case GREEN -> DATA_CRYSTAL_GREEN_ID;
      case YELLOW -> DATA_CRYSTAL_YELLOW_ID;
      case WHITE -> DATA_CRYSTAL_WHITE_ID;
    });
  }

  private void setCrystalPoints(EvoCrystalColor color, int points) {
    this.entityData.set(switch (color) {
      case RED -> DATA_CRYSTAL_RED_ID;
      case BLUE -> DATA_CRYSTAL_BLUE_ID;
      case GREEN -> DATA_CRYSTAL_GREEN_ID;
      case YELLOW -> DATA_CRYSTAL_YELLOW_ID;
      case WHITE -> DATA_CRYSTAL_WHITE_ID;
    }, Math.max(0, points));
  }

  /// credits `amount` points of `color`, then checks for an evolution
  public void addCrystalPoints(EvoCrystalColor color, int amount) {
    this.setCrystalPoints(color, this.getCrystalPoints(color) + amount);
    this.checkEvolution();
  }

  private static final EvolutionForm NONE_FORM = new EvolutionForm() {
    @Override
    public String id() {
      return "NONE";
    }

    @Override
    public String displayName() {
      return "None";
    }

    @Override
    public int stage() {
      return 0;
    }
  };

  /// this type's unevolved starting form; override per lore type. Called during
  /// construction (via [#defineSynchedData]), so it must not depend on instance state.
  protected EvolutionForm baseForm() {
    return NONE_FORM;
  }

  /// resolves a stored form id back to this type's concrete [EvolutionForm] constant;
  /// override per lore type alongside [#baseForm]
  protected EvolutionForm resolveForm(String id) {
    return NONE_FORM;
  }

  public EvolutionForm getEvolutionForm() {
    return this.resolveForm(this.entityData.get(DATA_EVOLUTION_FORM_ID));
  }

  private void setEvolutionForm(EvolutionForm form) {
    this.entityData.set(DATA_EVOLUTION_FORM_ID, form.id());
  }

  /// has this companion ever reached `form` (including its current one)? Abilities check
  /// this rather than the current form alone, so evolving further never revokes one.
  public boolean hasReachedForm(EvolutionForm form) {
    String reached = this.entityData.get(DATA_REACHED_FORMS_ID);
    return ("," + reached + ",").contains("," + form.id() + ",");
  }

  private void markFormReached(EvolutionForm form) {
    if (this.hasReachedForm(form)) {
      return;
    }
    this.entityData.set(DATA_REACHED_FORMS_ID, this.entityData.get(DATA_REACHED_FORMS_ID) + "," + form.id());
  }

  /// evolution routes out of this companion's *current* form; override per type, switching
  /// on [#getEvolutionForm]. See [EvolutionThreshold] for the alternate-vs-"Any" distinction.
  protected List<EvolutionThreshold> evolutionThresholds() {
    return List.of();
  }

  /// TODO: no evolved-form entities/models exist yet, so this just updates the form, spends
  /// the points, and announces the result.
  private void checkEvolution() {
    for (EvolutionThreshold threshold : this.evolutionThresholds()) {
      int available = threshold.color() != null ? this.getCrystalPoints(threshold.color()) : this.totalCrystalPoints();
      if (available < threshold.amount()) {
        continue;
      }

      if (threshold.color() != null) {
        this.setCrystalPoints(threshold.color(), available - threshold.amount());
      } else {
        this.spendCrystalPointsAcrossColors(threshold.amount());
      }

      this.setEvolutionForm(threshold.result());
      this.markFormReached(threshold.result());

      LivingEntity owner = this.getOwner();
      if (owner instanceof Player player) {
        player.displayClientMessage(Component.literal(this.getCompanionType().name() + "-Type evolved into "
            + threshold.result().displayName() + "! (not yet implemented visually)"), false);
      }
      return;
    }
  }

  private int totalCrystalPoints() {
    int total = 0;
    for (EvoCrystalColor color : EvoCrystalColor.values()) {
      total += this.getCrystalPoints(color);
    }
    return total;
  }

  /// drains `amount` across colors in a fixed order; only used by "Any" thresholds
  private void spendCrystalPointsAcrossColors(int amount) {
    for (EvoCrystalColor color : EvoCrystalColor.values()) {
      if (amount <= 0) {
        break;
      }

      int have = this.getCrystalPoints(color);
      int take = Math.min(have, amount);
      this.setCrystalPoints(color, have - take);
      amount -= take;
    }
  }
  // endregion evolution

  // region leveling
  public int getLevel() {
    return this.entityData.get(DATA_LEVEL_ID);
  }

  public int getExperience() {
    return this.entityData.get(DATA_EXPERIENCE_ID);
  }

  /// XP needed to advance from `level` to `level + 1`
  public static int experienceToNextLevel(int level) {
    return 10 * level;
  }

  /// grants XP, leveling up (possibly several times) while there's enough; caps at [#MAX_LEVEL]
  public void addExperience(int amount) {
    int level = this.getLevel();
    int xp = this.getExperience() + amount;

    while (level < MAX_LEVEL && xp >= experienceToNextLevel(level)) {
      xp -= experienceToNextLevel(level);
      level++;
    }

    this.entityData.set(DATA_LEVEL_ID, level);
    this.entityData.set(DATA_EXPERIENCE_ID, level >= MAX_LEVEL ? 0 : xp);
  }
  // endregion leveling

  @Override
  public void addAdditionalSaveData(CompoundTag compound) {
    super.addAdditionalSaveData(compound);
    if (this.getOwnerUUID() != null) {
      compound.putUUID("Owner", this.getOwnerUUID());
    }

    compound.putString("CompanionType", this.getCompanionType().name());
    compound.putString("CompanionMode", this.getMode().name());
    compound.putInt("AbilityIndex", this.getAbilityIndex());

    for (EvoCrystalColor color : EvoCrystalColor.values()) {
      compound.putInt("Crystal" + color.name(), this.getCrystalPoints(color));
    }
    compound.putString("EvolutionForm", this.entityData.get(DATA_EVOLUTION_FORM_ID));
    compound.putString("ReachedForms", this.entityData.get(DATA_REACHED_FORMS_ID));
    compound.putInt("Level", this.getLevel());
    compound.putInt("Experience", this.getExperience());
    compound.putBoolean("Wisp", this.isWisp());
  }

  @Override
  public void readAdditionalSaveData(CompoundTag compound) {
    super.readAdditionalSaveData(compound);
    if (compound.hasUUID("Owner")) {
      this.setOwnerUUID(compound.getUUID("Owner"));
    }

    if (compound.contains("CompanionType")) {
      try {
        this.setCompanionType(CompanionType.valueOf(compound.getString("CompanionType")));
      } catch (IllegalArgumentException ignored) {
        // pre-dates this tag, or a type was renamed/removed; fall back to the synced default
      }
    }

    if (compound.contains("CompanionMode")) {
      try {
        this.setMode(CompanionMode.valueOf(compound.getString("CompanionMode")));
      } catch (IllegalArgumentException ignored) {
      }
    }

    if (compound.contains("AbilityIndex")) {
      this.setAbilityIndex(compound.getInt("AbilityIndex"));
    }

    for (EvoCrystalColor color : EvoCrystalColor.values()) {
      String key = "Crystal" + color.name();
      if (compound.contains(key)) {
        this.setCrystalPoints(color, compound.getInt(key));
      }
    }

    if (compound.contains("EvolutionForm")) {
      this.entityData.set(DATA_EVOLUTION_FORM_ID, compound.getString("EvolutionForm"));
    }
    if (compound.contains("ReachedForms")) {
      this.entityData.set(DATA_REACHED_FORMS_ID, compound.getString("ReachedForms"));
    }
    if (compound.contains("Level")) {
      this.entityData.set(DATA_LEVEL_ID, compound.getInt("Level"));
    }
    if (compound.contains("Experience")) {
      this.entityData.set(DATA_EXPERIENCE_ID, compound.getInt("Experience"));
    }
    if (compound.getBoolean("Wisp")) {
      this.entityData.set(DATA_WISP_ID, true);
      this.setNoAi(true);
    }
  }

  // TraceableEntity and OwnableEntity getOwner() collide once both are implemented;
  // we override it ourselves to resolve the mutiple implementations
  @Nullable
  @Override
  public LivingEntity getOwner() {

    return OwnableEntity.super.getOwner();
  }

  @Nullable
  @Override
  public UUID getOwnerUUID() {
    return this.entityData.get(DATA_OWNER_UUID_ID).orElse(null);
  }

  public void setOwnerUUID(@Nullable UUID uuid) {
    this.entityData.set(DATA_OWNER_UUID_ID, Optional.ofNullable(uuid));
  }

  public void setOwner(LivingEntity owner) {
    this.setOwnerUUID(owner.getUUID());
  }
  // endregion owner

  // region party
  public CompanionType getCompanionType() {
    return CompanionType.values()[this.entityData.get(DATA_COMPANION_TYPE_ID)];
  }

  public void setCompanionType(CompanionType type) {
    this.entityData.set(DATA_COMPANION_TYPE_ID, (byte) type.ordinal());
  }
  // endregion party

  // region mode
  public CompanionMode getMode() {
    return CompanionMode.values()[this.entityData.get(DATA_MODE_ID)];
  }

  public void setMode(CompanionMode mode) {
    this.entityData.set(DATA_MODE_ID, (byte) mode.ordinal());

    // DEFEND disables AI (no wander/chase/attack); direct teleports still work.
    // A wisp stays noAi regardless - a mode change shouldn't wake it back up.
    if (!this.level().isClientSide && !this.isWisp()) {
      this.setNoAi(mode == CompanionMode.DEFEND);
    }
  }

  /// Fairy/Bird/Mage/Pumpkin-Type only have Auto and Command; Battle- and Devil-Type add Defend
  public CompanionMode[] getAvailableModes() {
    return this.getCompanionType().isDefendCapable()
        ? new CompanionMode[]{CompanionMode.AUTO, CompanionMode.COMMAND, CompanionMode.DEFEND}
        : new CompanionMode[]{CompanionMode.AUTO, CompanionMode.COMMAND};
  }

  public void cycleMode(int direction) {
    CompanionMode[] available = this.getAvailableModes();

    int index = 0;
    for (int i = 0; i < available.length; i++) {
      if (available[i] == this.getMode()) {
        index = i;
        break;
      }
    }

    this.setMode(available[Math.floorMod(index + direction, available.length)]);
  }

  public int getAbilityIndex() {
    return this.entityData.get(DATA_ABILITY_INDEX_ID);
  }

  public void setAbilityIndex(int index) {
    this.entityData.set(DATA_ABILITY_INDEX_ID, (byte) index);
  }

  /// this companion type's full Command-mode kit, gated or not; override per type, e.g.
  /// `FairySummon#allAbilities`. [#abilities] filters this down to what's actually unlocked.
  protected List<CompanionAbility> allAbilities() {
    return List.of();
  }

  /// this companion's currently-unlocked Command-mode abilities: [CompanionAbility#requiredForms]
  /// empty, or [#hasReachedForm] true for at least one of them, AND [#getLevel] at least
  /// [CompanionAbility#minLevel] (soft-gated by level on top of evolution form)
  public final List<CompanionAbility> abilities() {
    List<CompanionAbility> unlocked = new ArrayList<>();
    for (CompanionAbility ability : this.allAbilities()) {
      boolean formOk = ability.requiredForms().isEmpty() || ability.requiredForms().stream().anyMatch(this::hasReachedForm);
      if (formOk && this.getLevel() >= ability.minLevel()) {
        unlocked.add(ability);
      }
    }
    return unlocked;
  }

  public final int getAbilityCount() {
    return this.abilities().size();
  }

  public final String getAbilityName(int index) {
    List<CompanionAbility> abilities = this.abilities();
    return index >= 0 && index < abilities.size() ? abilities.get(index).name() : "???";
  }

  public boolean isAbilityBusy() {
    return this.entityData.get(DATA_ABILITY_BUSY_ID);
  }

  protected void setAbilityBusy(int ticks) {
    this.abilityBusyTicks = ticks;
    this.entityData.set(DATA_ABILITY_BUSY_ID, ticks > 0);
  }

  public void cycleAbility(int direction) {
    int count = this.getAbilityCount();
    if (count <= 0) {
      return;
    }

    this.setAbilityIndex(Math.floorMod(this.getAbilityIndex() + direction, count));
  }

  /// COMMAND keybind hook; gated to Command mode, not already busy, and not a wisp
  public final void performCommandAbility() {
    if (this.level().isClientSide || this.isWisp() || this.getMode() != CompanionMode.COMMAND
        || this.isAbilityBusy()) {
      return;
    }

    List<CompanionAbility> abilities = this.abilities();
    int index = this.getAbilityIndex();
    if (index < 0 || index >= abilities.size()) {
      return;
    }

    LivingEntity owner = this.getOwner();
    if (owner == null) {
      return;
    }

    CompanionAbility ability = abilities.get(index);
    ability.effect().accept(this, owner);
    this.setAbilityBusy(ability.busyTicks());
    this.addExperience(ABILITY_USE_XP);
  }

  /// small particle burst at `target`'s head and feet, for ability effects to call
  protected static void spawnAbilityParticles(LivingEntity target, ParticleOptions particle, int count) {
    if (target.level() instanceof ServerLevel level) {

      // at feet
      level.sendParticles(particle, target.getX(), target.getY(), target.getZ(), count,
          randOffsetThird(level), randOffsetThird(level), randOffsetThird(level), 0.0);

      // at head
      level.sendParticles(particle, target.getX(), target.getY() + target.getBbHeight(), target.getZ(), count,
          randOffsetThird(level), randOffsetThird(level), randOffsetThird(level), 0.0);
    }
  }

  /// nearest living target to `companion` within `radius`, excluding itself and `owner`
  protected static LivingEntity findNearestTarget(AbstractCompanion companion, LivingEntity owner, double radius) {
    AABB area = companion.getBoundingBox().inflate(radius);
    return companion.level().getEntitiesOfClass(LivingEntity.class, area,
            e -> e != companion && e != owner && e.isAlive())
        .stream().min(Comparator.comparingDouble(companion::distanceToSqr)).orElse(null);
  }

  private static double randOffsetThird(Level level) {

    return ((level.getRandom().nextDouble() * 2.0D) - 1.0D) / 3.0D; // random*2-1 [-1, 1), / 3 [-0.3, 0.3)
  }
  // endregion mode
}
