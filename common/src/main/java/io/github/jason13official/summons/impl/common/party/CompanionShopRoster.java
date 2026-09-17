package io.github.jason13official.summons.impl.common.party;

import io.github.jason13official.summons.api.common.util.SummonsDataHolder;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;

/// A player's forged-but-unplaced Innocent Devils, parked at a Julia-esque shopkeeper (see ShardMerchant/DevilShardItem) rather than occupying one of [CompanionParty]'s six slots; matches Curse of
/// Darkness's actual model, where the shop holds every extra I.D. of a type Hector already carries. Pulling an entry into the active party (a swap with whatever's currently in that type's slot) is
/// not implemented yet; this class is currently write-only from the forge flow, read-only for whatever UI eventually lists it.
public class CompanionShopRoster {

  private static final String ROOT_TAG = "summons_shop_roster";
  private static final String ID_TAG = "Id";
  private static final String TYPE_TAG = "Type";
  private static final String LEVEL_TAG = "Level";
  private static final String GENERATION_TAG = "Generation";

  private final ListTag entries;

  private CompanionShopRoster(ListTag entries) {
    this.entries = entries;
  }

  public static CompanionShopRoster of(ServerPlayer player) {
    CompoundTag root = ((SummonsDataHolder) player).summons$getPersistentData();
    if (!(root.get(ROOT_TAG) instanceof ListTag)) {
      root.put(ROOT_TAG, new ListTag());
    }

    return new CompanionShopRoster(root.getList(ROOT_TAG, Tag.TAG_COMPOUND));
  }

  /// records a freshly-forged companion, seeded from a Devil Shard; returns its new entry id
  public String add(CompanionType type, int level, int generation) {
    String id = UUID.randomUUID().toString();

    CompoundTag entry = new CompoundTag();
    entry.putString(ID_TAG, id);
    entry.putString(TYPE_TAG, type.name());
    entry.putInt(LEVEL_TAG, level);
    entry.putInt(GENERATION_TAG, generation);
    this.entries.add(entry);

    return id;
  }

  public boolean remove(String id) {
    return this.entries.removeIf(tag -> tag instanceof CompoundTag compound && id.equals(compound.getString(ID_TAG)));
  }

  public List<Entry> entries() {
    List<Entry> result = new ArrayList<>();
    for (Tag tag : this.entries) {
      if (!(tag instanceof CompoundTag compound)) {
        continue;
      }

      try {
        result.add(new Entry(compound.getString(ID_TAG), CompanionType.valueOf(compound.getString(TYPE_TAG)),
            compound.getInt(LEVEL_TAG), compound.getInt(GENERATION_TAG)));
      } catch (IllegalArgumentException ignored) {
        // a type was renamed/removed since this entry was written; skip it rather than crash the listing
      }
    }

    return result;
  }

  public record Entry(String id, CompanionType type, int level, int generation) {
  }
}
