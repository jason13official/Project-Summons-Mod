package io.github.jason13official.summons.impl.common.gate;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;

/// per-player cell assignment inside the shared Summon pocket dimension; each player gets a permanent, deterministically-spaced cell the first time they use a
/// [io.github.jason13official.summons.impl.common.block.SummonGateBlock]
public class SummonGateSavedData extends SavedData {

  private static final String CELLS_TAG = "cells";
  private static final String UUID_TAG = "uuid";
  private static final String INDEX_TAG = "index";

  private final Map<UUID, Integer> cells;
  private int nextIndex;

  private SummonGateSavedData(Map<UUID, Integer> cells, int nextIndex) {
    this.cells = cells;
    this.nextIndex = nextIndex;
  }

  public static SummonGateSavedData create() {
    return new SummonGateSavedData(new HashMap<>(), 0);
  }

  public static SavedData.Factory<SummonGateSavedData> factory() {
    return new SavedData.Factory<>(SummonGateSavedData::create, SummonGateSavedData::load, DataFixTypes.LEVEL);
  }

  public static SummonGateSavedData load(CompoundTag tag, HolderLookup.Provider registries) {
    Map<UUID, Integer> cells = new HashMap<>();
    int highest = -1;

    ListTag list = tag.getList(CELLS_TAG, Tag.TAG_COMPOUND);
    for (int i = 0; i < list.size(); i++) {
      CompoundTag entry = list.getCompound(i);
      UUID uuid = entry.getUUID(UUID_TAG);
      int index = entry.getInt(INDEX_TAG);
      cells.put(uuid, index);
      highest = Math.max(highest, index);
    }

    return new SummonGateSavedData(cells, highest + 1);
  }

  @Override
  public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
    ListTag list = new ListTag();
    for (Map.Entry<UUID, Integer> entry : this.cells.entrySet()) {
      CompoundTag compound = new CompoundTag();
      compound.putUUID(UUID_TAG, entry.getKey());
      compound.putInt(INDEX_TAG, entry.getValue());
      list.add(compound);
    }

    tag.put(CELLS_TAG, list);
    return tag;
  }

  /// assigns a permanent cell index to `uuid` on first call, reusing it on every later call
  public int cellFor(UUID uuid) {
    Integer existing = this.cells.get(uuid);
    if (existing != null) {
      return existing;
    }

    int assigned = this.nextIndex++;
    this.cells.put(uuid, assigned);
    this.setDirty();
    return assigned;
  }
}
