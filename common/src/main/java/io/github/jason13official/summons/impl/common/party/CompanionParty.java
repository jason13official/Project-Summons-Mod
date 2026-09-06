package io.github.jason13official.summons.impl.common.party;

import io.github.jason13official.summons.api.common.util.SummonsDataHolder;
import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;

/// per-player collection of up to one Innocent Devil per [CompanionType]; only one may be
/// summoned into the world at a time.
/// @see CompanionPartyManager
/// @see io.github.jason13official.summons.mixin.ServerPlayerPersistentDataMixin
public class CompanionParty {

  private static final String ROOT_TAG = "summons_party";
  private static final String ACTIVE_TYPE_TAG = "active_type";
  private static final String LAST_ACTIVE_TYPE_TAG = "last_active_type";
  private static final String UNLOCKED_TAG = "unlocked";
  private static final String SNAPSHOT_TAG = "snapshot";
  private static final String ENTITY_TYPE_TAG = "entity_type";

  private final CompoundTag data;

  private CompanionParty(CompoundTag data) {
    this.data = data;
  }

  public static CompanionParty of(ServerPlayer player) {
    CompoundTag root = ((SummonsDataHolder) player).summons$getPersistentData();
    if (!root.contains(ROOT_TAG)) {
      root.put(ROOT_TAG, new CompoundTag());
    }

    return new CompanionParty(root.getCompound(ROOT_TAG));
  }

  private CompoundTag slot(CompanionType type) {
    String key = type.name();
    if (!this.data.contains(key)) {
      this.data.put(key, new CompoundTag());
    }

    return this.data.getCompound(key);
  }

  public boolean isUnlocked(CompanionType type) {
    return this.data.contains(type.name()) && this.slot(type).getBoolean(UNLOCKED_TAG);
  }

  public Set<CompanionType> unlockedTypes() {
    Set<CompanionType> result = EnumSet.noneOf(CompanionType.class);
    for (CompanionType type : CompanionType.values()) {
      if (this.isUnlocked(type)) {
        result.add(type);
      }
    }

    return result;
  }

  /// record a newly-acquired Innocent Devil into its slot; no-op if already unlocked
  public void unlock(CompanionType type, String entityTypeId, CompoundTag snapshot) {
    if (this.isUnlocked(type)) {
      return;
    }

    CompoundTag slot = this.slot(type);
    slot.putBoolean(UNLOCKED_TAG, true);
    slot.putString(ENTITY_TYPE_TAG, entityTypeId);
    slot.put(SNAPSHOT_TAG, snapshot);
  }

  public Optional<String> getEntityTypeId(CompanionType type) {
    if (!this.isUnlocked(type)) {
      return Optional.empty();
    }

    return Optional.of(this.slot(type).getString(ENTITY_TYPE_TAG));
  }

  /// removes and returns the slot's stored snapshot, if any;
  /// a companion has one while dismissed, and none while it is the live, summoned entity
  public Optional<CompoundTag> takeSnapshot(CompanionType type) {
    CompoundTag slot = this.slot(type);
    if (!slot.contains(SNAPSHOT_TAG)) {
      return Optional.empty();
    }

    CompoundTag snapshot = slot.getCompound(SNAPSHOT_TAG);
    slot.remove(SNAPSHOT_TAG);
    return Optional.of(snapshot);
  }

  public void putSnapshot(CompanionType type, CompoundTag snapshot) {
    this.slot(type).put(SNAPSHOT_TAG, snapshot);
  }

  public Optional<CompanionType> getActiveType() {
    if (!this.data.contains(ACTIVE_TYPE_TAG)) {
      return Optional.empty();
    }

    try {
      return Optional.of(CompanionType.valueOf(this.data.getString(ACTIVE_TYPE_TAG)));
    } catch (IllegalArgumentException e) {
      return Optional.empty();
    }
  }

  public void setActiveType(CompanionType type) {
    this.data.putString(ACTIVE_TYPE_TAG, type.name());
    this.data.putString(LAST_ACTIVE_TYPE_TAG, type.name());
  }

  public void clearActiveType() {
    this.data.remove(ACTIVE_TYPE_TAG);
  }

  public Optional<CompanionType> getLastActiveType() {
    if (!this.data.contains(LAST_ACTIVE_TYPE_TAG)) {
      return Optional.empty();
    }

    try {
      return Optional.of(CompanionType.valueOf(this.data.getString(LAST_ACTIVE_TYPE_TAG)));
    } catch (IllegalArgumentException e) {
      return Optional.empty();
    }
  }
}
