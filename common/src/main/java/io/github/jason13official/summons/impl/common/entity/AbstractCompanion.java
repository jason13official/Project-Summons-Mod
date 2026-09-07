package io.github.jason13official.summons.impl.common.entity;

import io.github.jason13official.summons.impl.common.entity.ability.CompanionAbility;
import io.github.jason13official.summons.impl.common.entity.ai.goal.target.CompanionOwnerHurtByTargetGoal;
import io.github.jason13official.summons.impl.common.entity.ai.goal.target.CompanionOwnerHurtTargetGoal;
import io.github.jason13official.summons.impl.common.party.CompanionMode;
import io.github.jason13official.summons.impl.common.party.CompanionType;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
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
import net.minecraft.world.level.Level;
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
  }

  @Override
  public void tick() {
    super.tick();

    if (this.level().isClientSide) {
      if (this.getMode() == CompanionMode.DEFEND) {
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

  /// DEFEND mode blocks damage and shrinks the field; bypass-invulnerability sources still go through
  @Override
  public boolean hurt(DamageSource source, float amount) {
    if (!this.level().isClientSide && this.getMode() == CompanionMode.DEFEND
        && !source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
      this.setGuardFieldRadius(this.getGuardFieldRadius() - GUARD_FIELD_SHRINK_PER_HIT);
      return false;
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

  @Override
  public void addAdditionalSaveData(CompoundTag compound) {
    super.addAdditionalSaveData(compound);
    if (this.getOwnerUUID() != null) {
      compound.putUUID("Owner", this.getOwnerUUID());
    }

    compound.putString("CompanionType", this.getCompanionType().name());
    compound.putString("CompanionMode", this.getMode().name());
    compound.putInt("AbilityIndex", this.getAbilityIndex());
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

    // DEFEND disables AI (no wander/chase/attack); direct teleports still work
    if (!this.level().isClientSide) {
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

  /// this companion's Command-mode abilities/kit; override per type, e.g. `FairySummon#abilities`
  protected List<CompanionAbility> abilities() {
    return List.of();
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

  /// COMMAND keybind hook; gated to Command mode and not already busy
  public final void performCommandAbility() {
    if (this.level().isClientSide || this.getMode() != CompanionMode.COMMAND || this.isAbilityBusy()) {
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
  }

  /// small particle burst at `target`'s head, for ability effects to call
  protected static void spawnAbilityParticles(LivingEntity target, ParticleOptions particle, int count) {
    if (target.level() instanceof ServerLevel serverLevel) {
      serverLevel.sendParticles(particle, target.getX(), target.getY() + target.getBbHeight(), target.getZ(), count,
          0.3, 0.3, 0.3, 0.0);
    }
  }
  // endregion mode
}
