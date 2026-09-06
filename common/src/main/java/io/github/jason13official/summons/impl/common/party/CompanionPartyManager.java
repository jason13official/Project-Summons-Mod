package io.github.jason13official.summons.impl.common.party;

import io.github.jason13official.summons.impl.common.entity.AbstractCompanion;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.entity.EntityTypeTest;
import org.jetbrains.annotations.Nullable;

/// Logical-side "only one active companion at a time" rule for [CompanionParty].
/// Other owners (e.g. a boss that controls several [AbstractCompanion]s at
/// once) spawn and own them directly and never touch this class -> the restriction is
/// only for player parties, not something baked into the summons
public class CompanionPartyManager {

  /// accepts `Level` rather than `ServerLevel` so it can resolve client-side too (e.g. for our HUD)
  ///
  /// @see net.minecraft.world.entity.monster.Vex#getOwner()
  @Nullable
  public static AbstractCompanion findActive(Level level, Player owner) {
    List<AbstractCompanion> found = level.getEntities(EntityTypeTest.forClass(AbstractCompanion.class),
        owner.getBoundingBox().inflate(64.0), companion -> owner.getUUID().equals(companion.getOwnerUUID()));

    return found.isEmpty() ? null : found.get(0);
  }

  /// record a newly acquired Innocent Devil/Summon into its party slot; no-op if that slot is
  /// already unlocked. TODO impl
  public static void unlock(ServerPlayer player, CompanionType type, EntityType<? extends AbstractCompanion> entityType) {
    CompanionParty party = CompanionParty.of(player);
    if (party.isUnlocked(type)) {
      return;
    }

    Entity fresh = entityType.create(player.serverLevel());
    if (!(fresh instanceof AbstractCompanion companion)) {
      return;
    }

    companion.setCompanionType(type);

    ResourceLocation entityTypeId = BuiltInRegistries.ENTITY_TYPE.getKey(entityType);
    party.unlock(type, entityTypeId.toString(), companion.saveWithoutId(new CompoundTag()));
  }

  /// swap the player's summoned companion to `type`, dismissing whatever was active first.
  /// returns false if `type` was never unlocked for this player
  public static boolean setActive(ServerPlayer player, CompanionType type) {
    CompanionParty party = CompanionParty.of(player);
    if (!party.isUnlocked(type)) {
      return false;
    }

    if (party.getActiveType().filter(active -> active == type).isPresent()) {
      return true; // already out
    }

    dismissActive(player);

    Optional<String> entityTypeId = party.getEntityTypeId(type);
    Optional<CompoundTag> snapshot = party.takeSnapshot(type);
    if (entityTypeId.isEmpty() || snapshot.isEmpty()) {
      return false;
    }

    EntityType<?> entityType = BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.parse(entityTypeId.get()));
    if (entityType == null) {
      return false;
    }

    ServerLevel level = player.serverLevel();
    Entity created = entityType.create(level);
    if (!(created instanceof AbstractCompanion companion)) {
      return false;
    }

    companion.load(snapshot.get());
    companion.setCompanionType(type);
    companion.setOwner(player);
    companion.moveTo(player.getX(), player.getY(), player.getZ(), player.getYRot(), 0.0F);

    level.addFreshEntity(companion);
    party.setActiveType(type);
    return true;
  }

  /// snapshot and discard whatever companion the player currently has summoned, if any
  public static void dismissActive(ServerPlayer player) {
    CompanionParty party = CompanionParty.of(player);
    Optional<CompanionType> activeType = party.getActiveType();
    if (activeType.isEmpty()) {
      return;
    }

    AbstractCompanion active = findActive(player.serverLevel(), player);
    if (active != null) {
      party.putSnapshot(activeType.get(), active.saveWithoutId(new CompoundTag()));
      active.discard();
    }

    party.clearActiveType();
  }

  /// summon/dismiss keybind: toggles the currently (or most recently) active slot
  public static void toggle(ServerPlayer player) {
    CompanionParty party = CompanionParty.of(player);

    if (party.getActiveType().isPresent()) {
      dismissActive(player);
      return;
    }

    Optional<CompanionType> target = party.getLastActiveType().filter(party::isUnlocked);
    if (target.isEmpty()) {
      Set<CompanionType> unlocked = party.unlockedTypes();
      if (unlocked.isEmpty()) {
        return; // nothing unlocked yet
      }

      target = Optional.of(unlocked.iterator().next());
    }

    setActive(player, target.get());
  }

  /// cycles the active slot to the next unlocked [CompanionType] (wrapping); `direction` is +1 or -1
  public static void cycle(ServerPlayer player, int direction) {
    CompanionParty party = CompanionParty.of(player);

    CompanionType[] all = CompanionType.values();
    Set<CompanionType> unlocked = party.unlockedTypes();
    if (unlocked.isEmpty()) {
      return;
    }

    CompanionType current = party.getActiveType().or(party::getLastActiveType).orElse(all[0]);

    int index = current.ordinal();
    for (int step = 1; step <= all.length; step++) {
      CompanionType candidate = all[Math.floorMod(index + direction * step, all.length)];
      if (unlocked.contains(candidate)) {
        setActive(player, candidate);
        return;
      }
    }
  }
}
