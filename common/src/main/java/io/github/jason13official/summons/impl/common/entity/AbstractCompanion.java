package io.github.jason13official.summons.impl.common.entity;

import io.github.jason13official.summons.impl.common.entity.ability.CompanionAbility;
import io.github.jason13official.summons.impl.common.entity.ability.OrbitingBitManager;
import io.github.jason13official.summons.impl.common.entity.ai.goal.target.CompanionOwnerHurtByTargetGoal;
import io.github.jason13official.summons.impl.common.entity.ai.goal.target.CompanionOwnerHurtTargetGoal;
import io.github.jason13official.summons.impl.common.evolution.EvoCrystalColor;
import io.github.jason13official.summons.impl.common.evolution.EvolutionForm;
import io.github.jason13official.summons.impl.common.evolution.EvolutionThreshold;
import io.github.jason13official.summons.impl.common.network.CompanionIdentitySyncPayload;
import io.github.jason13official.summons.impl.common.network.CompanionProgressSyncPayload;
import io.github.jason13official.summons.impl.common.network.CompanionStateSyncPayload;
import io.github.jason13official.summons.impl.common.party.CompanionMode;
import io.github.jason13official.summons.impl.common.party.CompanionType;
import io.github.jason13official.summons.platform.Services;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.TraceableEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractCompanion extends PathfinderMob implements TraceableEntity, OwnableEntity {

  // TamableAnimal.class; UUID owner, round-trips via restoreFrom
  private static final EntityDataAccessor<Optional<UUID>> DATA_OWNER_UUID_ID =
      SynchedEntityData.defineId(AbstractCompanion.class, EntityDataSerializers.OPTIONAL_UUID);

  // Everything below is OUR OWN sync, not vanilla's SynchedEntityData (mods can't safely
  // register new EntityDataSerializers). Plain fields, mirrored to tracking clients via
  // three payloads split by change frequency (see #tick, #build*/#apply*SyncPayload).
  private boolean progressSyncDirty = true; // crystals/level/experience -> changes often
  private boolean stateSyncDirty = true; // mode/ability/chain/guard field -> changes sometimes
  private boolean identitySyncDirty = true; // type/evolution/wisp -> changes rarely
  private static final int PROGRESS_SYNC_INTERVAL_TICKS = 20;
  private static final int STATE_SYNC_INTERVAL_TICKS = 20;
  private static final int IDENTITY_SYNC_INTERVAL_TICKS = 100; // fine to lag further behind

  // which Innocent Devil "slot" this instance represents; assigned by CompanionPartyManager
  private CompanionType companionType = CompanionType.FAIRY;
  private CompanionMode mode = CompanionMode.AUTO;
  private int abilityIndex;
  private boolean abilityBusy;
  private int abilityBusyTicks;

  /// Chain Attack "window" (Battle/Devil only, see CompanionType#isChainAttackCapable):
  /// true while the owner's next landed hit should trigger a bonus companion attack instead
  /// of just damage, see ChainAttackTracker. Synced so SummonsHUD can show the popup.
  private boolean chainArmed;
  private int chainArmedTicks;

  public static final float GUARD_FIELD_MAX_RADIUS = 3.0F;
  public static final float GUARD_FIELD_MIN_RADIUS = 0.75F;

  /// Guard field radius for DEFEND mode; shrinks per hit, regenerates over time.
  private float guardFieldRadius = GUARD_FIELD_MAX_RADIUS;

  private static final double TELEPORT_TO_OWNER_DISTANCE = 24.0;

  private CrystalPoints crystals = CrystalPoints.ZERO;

  // comma-joined form ids ever reached; abilities stay learned once unlocked, even past
  // whatever form originally granted them (matches the wiki: evolving never revokes one)
  private String evolutionForm;
  private String reachedForms;

  private int level = 1;
  private int experience;

  public static final int MAX_LEVEL = 99;
  private static final int ABILITY_USE_XP = 5;
  private static final int DIRECT_ATTACK_XP = 2;

  /// at 0 Hearts the I.D. goes inert instead of dying; no wisp entity/model, renderers
  /// skip drawing and tick() emits particles instead until a Heart revives it
  private boolean wisp;

  public AbstractCompanion(EntityType<? extends AbstractCompanion> entityType, Level level) {
    super(entityType, level);
    this.evolutionForm = this.baseForm().id();
    this.reachedForms = this.baseForm().id();
  }

  public static AttributeSupplier.Builder createAttributes() {

    // Cow.class
    return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 10.0D).add(Attributes.MOVEMENT_SPEED, 0.2D);
  }

  /// Monster.class calls this from its own aiStep() override to drive attackAnim/
  /// getAttackAnim(); plain Mob/PathfinderMob never do, so without this override
  /// getAttackAnim() would stay stuck at 0 forever despite swing() being called correctly.
  @Override
  public void aiStep() {
    this.updateSwingTime();
    super.aiStep();
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
  }

  @Override
  public void tick() {
    super.tick();

    if (this.level().isClientSide) {
      if (this.isWisp()) {
        CompanionWisp.spawnParticles(this);
      } else if (this.getMode() == CompanionMode.DEFEND) {
        CompanionGuardField.spawnParticles(this);
      }
      return;
    }

    this.teleportToOwner();
    if (this.isRemoved()) {
      return; // relocated into the owner's dimension; this instance was replaced
    }

    if (this.isWisp()) {
      CompanionWisp.floatTowardOwner(this); // noAi skips normal goal-based movement entirely
    }

    if (this.abilityBusyTicks > 0 && --this.abilityBusyTicks == 0) {
      this.abilityBusy = false;
      this.stateSyncDirty = true;
    }

    if (this.chainArmedTicks > 0 && --this.chainArmedTicks == 0) {
      this.chainArmed = false;
      this.stateSyncDirty = true;
    }

    this.orbitingBits.tick(this);

    if (this.tickCount % PROGRESS_SYNC_INTERVAL_TICKS == 0) {
      OwnerAttributeBonuses.refresh(this);
    }

    // each tier resends periodically regardless of its own dirty flag too, so a player who
    // starts tracking this companion after its last change still converges on real state
    if (this.progressSyncDirty || this.tickCount % PROGRESS_SYNC_INTERVAL_TICKS == 0) {
      Services.serverNetwork().sendToTrackingClients(this, this.buildProgressSyncPayload());
      this.progressSyncDirty = false;
    }

    if (this.stateSyncDirty || this.tickCount % STATE_SYNC_INTERVAL_TICKS == 0) {
      Services.serverNetwork().sendToTrackingClients(this, this.buildStateSyncPayload());
      this.stateSyncDirty = false;
    }

    if (this.identitySyncDirty || this.tickCount % IDENTITY_SYNC_INTERVAL_TICKS == 0) {
      Services.serverNetwork().sendToTrackingClients(this, this.buildIdentitySyncPayload());
      this.identitySyncDirty = false;
    }

    CompanionGuardField.tick(this);
  }

  private CompanionProgressSyncPayload buildProgressSyncPayload() {
    return new CompanionProgressSyncPayload(this.getId(), this.crystals, this.level, this.experience);
  }

  private CompanionStateSyncPayload buildStateSyncPayload() {
    return new CompanionStateSyncPayload(this.getId(), (byte) this.mode.ordinal(), (byte) this.abilityIndex,
        this.abilityBusy, this.chainArmed, this.guardFieldRadius);
  }

  private CompanionIdentitySyncPayload buildIdentitySyncPayload() {
    return new CompanionIdentitySyncPayload(this.getId(), (byte) this.companionType.ordinal(),
        this.evolutionForm, this.reachedForms, this.wisp);
  }

  /// client-side: overwrites the mirrored fields from a received snapshot directly,
  /// bypassing the public setters (and whatever side effects they carry, like #setMode's
  /// noAi toggle); applying synced state shouldn't re-trigger that kind of business logic
  public void applyProgressSyncPayload(CompanionProgressSyncPayload payload) {
    this.crystals = payload.crystals();
    this.level = payload.level();
    this.experience = payload.experience();
  }

  public void applyStateSyncPayload(CompanionStateSyncPayload payload) {
    this.mode = CompanionMode.values()[payload.mode()];
    this.abilityIndex = payload.abilityIndex();
    this.abilityBusy = payload.abilityBusy();
    this.chainArmed = payload.chainArmed();
    this.guardFieldRadius = payload.guardFieldRadius();
  }

  public void applyIdentitySyncPayload(CompanionIdentitySyncPayload payload) {
    this.companionType = CompanionType.values()[payload.companionType()];
    this.evolutionForm = payload.evolutionForm();
    this.reachedForms = payload.reachedForms();
    this.wisp = payload.wisp();
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
    return this.guardFieldRadius;
  }

  void setGuardFieldRadius(float radius) {
    float clamped = Math.min(GUARD_FIELD_MAX_RADIUS, Math.max(GUARD_FIELD_MIN_RADIUS, radius));
    if (clamped != this.guardFieldRadius) {
      this.guardFieldRadius = clamped;
      this.stateSyncDirty = true;
    }
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
        CompanionGuardField.shrink(this);
        return false;
      }
    }

    return super.hurt(source, amount);
  }

  /// protects an owner standing inside a DEFEND-mode companion's field; also shrinks it,
  /// same as a direct hit would
  public static boolean isProtectedByGuardField(LivingEntity owner) {
    return CompanionGuardField.isProtecting(owner);
  }
  // endregion guard field

  // region wisp
  public boolean isWisp() {
    return this.wisp;
  }

  void setWisp(boolean wisp) {
    if (wisp != this.wisp) {
      this.wisp = wisp;
      this.identitySyncDirty = true;
    }
  }

  /// bridges CompanionWisp back to the real vanilla death (`x.super.method()` isn't legal
  /// from outside this class, hence the one-line forward)
  void callSuperDie(DamageSource source) {
    super.die(source);
  }

  @Override
  public void die(DamageSource source) {
    CompanionWisp.die(this, source);
  }

  /// a Heart revives it; only meaningful while [#isWisp]
  public void reviveFromWisp() {
    CompanionWisp.revive(this);
  }

  /// what touching a Heart does: revives from wisp first if needed, then heals
  public void consumeHeart() {
    CompanionWisp.consumeHeart(this);
  }
  // endregion wisp

  // region evolution
  public int getCrystalPoints(EvoCrystalColor color) {
    return this.crystals.get(color);
  }

  public CrystalPoints getCrystalPoints() {
    return this.crystals;
  }

  void setCrystalPoints(EvoCrystalColor color, int points) {
    CrystalPoints updated = this.crystals.with(color, points);
    if (!updated.equals(this.crystals)) {
      this.crystals = updated;
      this.progressSyncDirty = true;
    }
  }

  /// credits `amount` points of `color`, then checks for an evolution
  public void addCrystalPoints(EvoCrystalColor color, int amount) {
    this.setCrystalPoints(color, this.getCrystalPoints(color) + amount);
    CompanionEvolution.checkEvolution(this);
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
    return this.resolveForm(this.evolutionForm);
  }

  void setEvolutionForm(EvolutionForm form) {
    if (!form.id().equals(this.evolutionForm)) {
      this.evolutionForm = form.id();
      this.identitySyncDirty = true;
    }
  }

  /// has this companion ever reached `form` (including its current one)? Abilities check
  /// this rather than the current form alone, so evolving further never revokes one.
  public boolean hasReachedForm(EvolutionForm form) {
    return ("," + this.reachedForms + ",").contains("," + form.id() + ",");
  }

  void markFormReached(EvolutionForm form) {
    if (this.hasReachedForm(form)) {
      return;
    }
    this.reachedForms = this.reachedForms + "," + form.id();
    this.identitySyncDirty = true;
  }

  /// evolution routes out of this companion's *current* form; override per type, switching
  /// on [#getEvolutionForm]. See [EvolutionThreshold] for the alternate-vs-"Any" distinction.
  protected List<EvolutionThreshold> evolutionThresholds() {
    return List.of();
  }

  // endregion evolution

  // region leveling
  public int getLevel() {
    return this.level;
  }

  public int getExperience() {
    return this.experience;
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

    this.level = level;
    this.experience = level >= MAX_LEVEL ? 0 : xp;
    this.progressSyncDirty = true;
  }
  // endregion leveling

  // region direct attack
  private static final float DIRECT_ATTACK_GROWTH_PER_LEVEL = 0.01F; // +1%/level; level 99 ~ 2x

  /// only the direct attack scales with level; ability damage stays flat (no wiki numbers
  /// for this -> it's this mod's own leveling design, not a Curse of Darkness mechanic).
  public final float directAttackDamageMultiplier() {
    return 1.0F + DIRECT_ATTACK_GROWTH_PER_LEVEL * (this.getLevel() - 1);
  }

  /// Melee hit + XP, with `directAttackDamageMultiplier()` applied; types with a different
  /// basic attack override this instead. Doesn't call `super.doHurtTarget` (no hook there
  /// for a level scalar); replicates its damage+knockback, skipping companion-irrelevant hooks.
  @Override
  public boolean doHurtTarget(Entity entity) {
    this.swing(InteractionHand.MAIN_HAND); // drives client-side getAttackAnim() for the swing pose
    float damage = (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE) * this.directAttackDamageMultiplier();
    DamageSource damageSource = this.damageSources().mobAttack(this);
    boolean success = entity.hurt(damageSource, damage);
    if (success) {
      float knockback = this.getKnockback(entity, damageSource);
      if (knockback > 0.0F && entity instanceof LivingEntity target) {
        target.knockback(knockback * 0.5, Mth.sin(this.getYRot() * ((float) Math.PI / 180.0F)),
            -Mth.cos(this.getYRot() * ((float) Math.PI / 180.0F)));
      }
      this.setLastHurtMob(entity);
      this.grantDirectAttackExperience();
    }
    return success;
  }

  public final void grantDirectAttackExperience() {
    this.addExperience(DIRECT_ATTACK_XP);
  }
  // endregion direct attack

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
    compound.putString("EvolutionForm", this.evolutionForm);
    compound.putString("ReachedForms", this.reachedForms);
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
      this.evolutionForm = compound.getString("EvolutionForm");
    }
    if (compound.contains("ReachedForms")) {
      this.reachedForms = compound.getString("ReachedForms");
    }
    if (compound.contains("Level")) {
      this.level = compound.getInt("Level");
    }
    if (compound.contains("Experience")) {
      this.experience = compound.getInt("Experience");
    }
    if (compound.getBoolean("Wisp")) {
      this.setWisp(true);
      this.setNoAi(true);
    }
    // loaded state needs to reach tracking clients on the next tick, across all three tiers
    this.progressSyncDirty = true;
    this.stateSyncDirty = true;
    this.identitySyncDirty = true;
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
    return this.companionType;
  }

  public void setCompanionType(CompanionType type) {
    if (type != this.companionType) {
      this.companionType = type;
      this.identitySyncDirty = true;
    }
  }
  // endregion party

  // region mode
  public CompanionMode getMode() {
    return this.mode;
  }

  public void setMode(CompanionMode mode) {
    if (mode != this.mode) {
      this.mode = mode;
      this.stateSyncDirty = true;
    }

    // DEFEND disables AI (no wander/chase/attack); direct teleports still work.
    // A wisp stays noAi regardless -> a mode change shouldn't wake it back up.
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
    return this.abilityIndex;
  }

  public void setAbilityIndex(int index) {
    if (index != this.abilityIndex) {
      this.abilityIndex = index;
      this.stateSyncDirty = true;
    }
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
    return this.abilityBusy;
  }

  protected void setAbilityBusy(int ticks) {
    this.abilityBusyTicks = ticks;
    boolean busy = ticks > 0;
    if (busy != this.abilityBusy) {
      this.abilityBusy = busy;
      this.stateSyncDirty = true;
    }
  }

  /// see ChainAttackTracker; whether the owner's next landed hit should trigger this
  /// companion's bonus Chain Attack, for SummonsHUD's popup
  public boolean isChainArmed() {
    return this.chainArmed;
  }

  public void setChainArmed(int ticks) {
    this.chainArmedTicks = ticks;
    boolean armed = ticks > 0;
    if (armed != this.chainArmed) {
      this.chainArmed = armed;
      this.stateSyncDirty = true;
    }
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

  private final OrbitingBitManager orbitingBits = new OrbitingBitManager();

  /// `count` bits orbiting the owner at `radius`/`angularSpeed`, dealing `damage` on contact
  /// (10-tick per-bit cooldown); despawns after `lifespanTicks`, or on first hit if
  /// `explodeOnHit`. Backs Mage's "B" spells (Floating B, Satellite B, etc).
  public final void spawnOrbitingBits(int count, double radius, double angularSpeed, double heightOffset,
                                       float damage, ParticleOptions particle, boolean explodeOnHit, int lifespanTicks) {
    this.orbitingBits.spawn(count, radius, angularSpeed, heightOffset, damage, particle, explodeOnHit, lifespanTicks);
  }

  /// STR/CON/LCK owner buffs; empty (no bonus) for the CUBE/PRISM placeholder types.
  public OwnerStatBonusKit ownerStatBonus() {
    return OwnerStatBonusKit.NONE;
  }

  @Override
  public void remove(RemovalReason reason) {
    OwnerAttributeBonuses.remove(this);
    super.remove(reason);
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
