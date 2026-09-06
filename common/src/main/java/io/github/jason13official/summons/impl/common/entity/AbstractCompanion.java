package io.github.jason13official.summons.impl.common.entity;

import io.github.jason13official.summons.impl.common.entity.ai.goal.target.CompanionOwnerHurtByTargetGoal;
import io.github.jason13official.summons.impl.common.entity.ai.goal.target.CompanionOwnerHurtTargetGoal;
import io.github.jason13official.summons.impl.common.party.CompanionMode;
import io.github.jason13official.summons.impl.common.party.CompanionType;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
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

  // TamableAnimal.class - owner tracked by UUID (not a raw reference) so it round-trips
  // through NBT save/load on its own, including across dimension changes via restoreFrom.
  private static final EntityDataAccessor<Optional<UUID>> DATA_OWNER_UUID_ID =
      SynchedEntityData.defineId(AbstractCompanion.class, EntityDataSerializers.OPTIONAL_UUID);

  // which Innocent Devil "slot" this instance represents; assigned by CompanionPartyManager
  private static final EntityDataAccessor<Byte> DATA_COMPANION_TYPE_ID =
      SynchedEntityData.defineId(AbstractCompanion.class, EntityDataSerializers.BYTE);

  private static final EntityDataAccessor<Byte> DATA_MODE_ID =
      SynchedEntityData.defineId(AbstractCompanion.class, EntityDataSerializers.BYTE);

  private static final EntityDataAccessor<Byte> DATA_ABILITY_INDEX_ID =
      SynchedEntityData.defineId(AbstractCompanion.class, EntityDataSerializers.BYTE);

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
  }

  // region owner
  @Override
  protected void defineSynchedData(SynchedEntityData.Builder builder) {
    super.defineSynchedData(builder);
    builder.define(DATA_OWNER_UUID_ID, Optional.empty());
    builder.define(DATA_COMPANION_TYPE_ID, (byte) CompanionType.FAIRY.ordinal());
    builder.define(DATA_MODE_ID, (byte) CompanionMode.AUTO.ordinal());
    builder.define(DATA_ABILITY_INDEX_ID, (byte) 0);
  }

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

  /// how many Command-mode abilities this companion currently has;
  /// TODO: 0 until we implement real abilities, then this should be overridden per companion
  public int getAbilityCount() {
    return 0;
  }

  public void cycleAbility(int direction) {
    int count = this.getAbilityCount();
    if (count <= 0) {
      return;
    }

    this.setAbilityIndex(Math.floorMod(this.getAbilityIndex() + direction, count));
  }

  /// hook for COMMAND keybind while in Command mode;
  /// no-op until we implement real abilities
  public void performCommandAbility() {
  }
  // endregion mode
}
