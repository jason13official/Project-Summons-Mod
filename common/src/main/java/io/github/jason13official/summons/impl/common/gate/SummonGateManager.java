package io.github.jason13official.summons.impl.common.gate;

import io.github.jason13official.summons.api.common.util.SummonsDataHolder;
import io.github.jason13official.summons.impl.common.block.SummonGateBlock;
import io.github.jason13official.summons.impl.common.party.CompanionParty;
import io.github.jason13official.summons.impl.common.party.CompanionPartyManager;
import io.github.jason13official.summons.impl.common.party.CompanionType;
import io.github.jason13official.summons.impl.common.registry.ModBlocks;
import io.github.jason13official.summons.impl.common.registry.ModDimensions;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/// the per-player "petrified Innocent Devil" pocket room, ported from Castlevania: Curse of
/// Darkness's green-door pocket dimensions -> each visit unlocks the next [CompanionType] in
/// [CompanionType] enum order (Fairy, Battle, Bird, Mage, Devil, Pumpkin), one per visit.
/// Rooms live in [ModDimensions#SUMMON_POCKET], one deterministic 512-block-spaced cell per
/// player ([SummonGateSavedData]); the player is forced to Adventure mode for the visit (cached
/// + restored on the way out) so nothing in the room can be broken or moved.
public class SummonGateManager {

  private static final String ROOT_TAG = "summons_gate";
  private static final String DIMENSION_TAG = "return_dimension";
  private static final String X_TAG = "return_x";
  private static final String Y_TAG = "return_y";
  private static final String Z_TAG = "return_z";
  private static final String YAW_TAG = "return_yaw";
  private static final String PITCH_TAG = "return_pitch";
  private static final String GAMEMODE_TAG = "cached_gamemode";

  private static final int CELL_SPACING = 512;
  private static final int ROOM_SIZE = 7; // interior footprint, X and Z
  private static final int ROOM_HEIGHT = 5;
  private static final int FLOOR_Y = 64;

  private static final int WARP_DELAY_TICKS = 100; // ~5s statue-glow buildup before the warp home
  private static final Map<UUID, PendingWarp> PENDING = new HashMap<>();

  /// a running "statue awakening" cutscene: the pedestal glows brighter each tick until
  /// `ticksLeft` hits 0, then [SummonGateManager#finishWarp] fires
  private static final class PendingWarp {

    private final ServerLevel level;
    private final BlockPos pos;
    private final CompanionType type;
    private int ticksLeft;

    private PendingWarp(ServerLevel level, BlockPos pos, CompanionType type, int ticksLeft) {
      this.level = level;
      this.pos = pos;
      this.type = type;
      this.ticksLeft = ticksLeft;
    }
  }

  /// right-clicking a [io.github.jason13official.summons.impl.common.block.SummonGateBlock]
  public static void enterGate(ServerPlayer player) {

    ServerLevel pocket = player.server.getLevel(ModDimensions.SUMMON_POCKET);
    if (pocket == null) {
      player.sendSystemMessage(Component.literal("The gate leads nowhere... (the dimension failed to load)"));
      return;
    }

    if (player.serverLevel() == pocket) {
      return; // already inside
    }

    CompanionParty party = CompanionParty.of(player);
    Optional<CompanionType> next = CompanionType.nextLocked(party);
    if (next.isEmpty()) {
      player.sendSystemMessage(Component.literal("The gate is silent... (Every Innocent Devil unlocked)"));
      player.level().playSound(null, player.blockPosition(), SoundEvents.BEACON_DEACTIVATE, SoundSource.BLOCKS, 0.6F, 1.4F);
      return;
    }

    SummonGateSavedData data = pocket.getDataStorage().computeIfAbsent(SummonGateSavedData.factory(), "summon_gate");
    BlockPos origin = cellOrigin(data.cellFor(player.getUUID()));

    buildRoom(pocket, origin, next.get());
    storeReturnPoint(player);
    player.setGameMode(GameType.ADVENTURE);

    BlockPos entrance = origin.offset(ROOM_SIZE / 2, 1, 1);
    player.teleportTo(pocket, entrance.getX() + 0.5, entrance.getY(), entrance.getZ() + 0.5, 0.0F, 0.0F); // yaw 0 faces +Z, toward the pedestal
    player.sendSystemMessage(Component.literal(
        "A petrified " + displayName(next.get()) + "-type Innocent Devil awaits..."));
  }

  /// right-clicking the petrified statue for `type` inside a pocket room; unlocks immediately,
  /// then starts a [PendingWarp] cutscene ([SummonGateManager#tick] drives it) -> the statue
  /// glows brighter each tick instead of the player being warped home on the spot
  public static void unlockAtStatue(ServerPlayer player, ServerLevel level, BlockPos pos, CompanionType type) {

    CompanionParty party = CompanionParty.of(player);
    if (party.isUnlocked(type) || PENDING.containsKey(player.getUUID())) {
      return;
    }

    CompanionPartyManager.unlock(player, type);

    level.playSound(null, pos, SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 1.0F, 0.6F);
    player.sendSystemMessage(Component.literal(
        "The statue trembles... your " + displayName(type) + "-type Innocent Devil stirs..."));

    PENDING.put(player.getUUID(), new PendingWarp(level, pos, type, WARP_DELAY_TICKS));
  }

  /// drives every in-progress [PendingWarp]; call once per server tick
  public static void tick(MinecraftServer server) {

    if (PENDING.isEmpty()) {
      return;
    }

    Iterator<Map.Entry<UUID, PendingWarp>> iterator = PENDING.entrySet().iterator();
    while (iterator.hasNext()) {
      Map.Entry<UUID, PendingWarp> entry = iterator.next();
      PendingWarp warp = entry.getValue();
      ServerPlayer player = server.getPlayerList().getPlayer(entry.getKey());

      if (player == null || player.serverLevel() != warp.level) {
        iterator.remove(); // disconnected, or left the room some other way - abandon the cutscene
        continue;
      }

      warp.ticksLeft--;
      float progress = 1.0F - warp.ticksLeft / (float) WARP_DELAY_TICKS;
      int particles = 2 + Mth.floor(progress * 18.0F);
      warp.level.sendParticles(ParticleTypes.END_ROD, warp.pos.getX() + 0.5, warp.pos.getY() + 0.5, warp.pos.getZ() + 0.5,
          particles, 0.15 + progress * 0.3, 0.25 + progress * 0.4, 0.15 + progress * 0.3, 0.01 + progress * 0.05);

      if (warp.ticksLeft % 15 == 0) {
        warp.level.playSound(null, warp.pos, SoundEvents.BEACON_AMBIENT, SoundSource.BLOCKS, 0.6F, 0.6F + progress * 0.8F);
      }

      if (warp.ticksLeft <= 0) {
        finishWarp(player, warp);
        iterator.remove();
      }
    }
  }

  private static void finishWarp(ServerPlayer player, PendingWarp warp) {

    warp.level.setBlockAndUpdate(warp.pos, Blocks.AIR.defaultBlockState());
    warp.level.sendParticles(ParticleTypes.FLASH, warp.pos.getX() + 0.5, warp.pos.getY() + 0.5, warp.pos.getZ() + 0.5, 1, 0.0, 0.0, 0.0, 0.0);
    warp.level.sendParticles(ParticleTypes.END_ROD, warp.pos.getX() + 0.5, warp.pos.getY() + 0.5, warp.pos.getZ() + 0.5, 80, 0.4, 0.6, 0.4, 0.08);
    warp.level.playSound(null, warp.pos, SoundEvents.BEACON_POWER_SELECT, SoundSource.BLOCKS, 1.0F, 1.2F);
    player.sendSystemMessage(Component.literal(
        "The statue shatters... your " + displayName(warp.type) + "-type Innocent Devil awakens!"));

    leaveGate(player);
  }

  /// teleports back to the stored pre-gate position/dimension and restores the cached gamemode;
  /// a no-op if the player has no stored return point (never entered, or already left)
  public static void leaveGate(ServerPlayer player) {

    CompoundTag root = ((SummonsDataHolder) player).summons$getPersistentData();
    if (!root.contains(ROOT_TAG)) {
      return;
    }

    CompoundTag tag = root.getCompound(ROOT_TAG);

    ResourceKey<Level> dimension = ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse(tag.getString(DIMENSION_TAG)));
    ServerLevel returnLevel = player.server.getLevel(dimension);
    if (returnLevel == null) {
      returnLevel = player.server.overworld();
    }

    player.teleportTo(returnLevel, tag.getDouble(X_TAG), tag.getDouble(Y_TAG), tag.getDouble(Z_TAG),
        tag.getFloat(YAW_TAG), tag.getFloat(PITCH_TAG));

    if (tag.contains(GAMEMODE_TAG)) {
      player.setGameMode(GameType.byName(tag.getString(GAMEMODE_TAG), GameType.SURVIVAL));
    }

    root.remove(ROOT_TAG);
  }

  /// stashes where to send the player back to; walking (not right-clicking) into the gate
  /// leaves them standing *inside* the [io.github.jason13official.summons.impl.common.block.SummonGateBlock]
  /// itself, so caching the raw position would warp them right back into the pocket dimension
  /// the moment [SummonGateManager#leaveGate] lands them there (`entityInside` re-triggers
  /// `enterGate`) -> step backwards along the way they came until standing somewhere that isn't
  /// the gate and won't suffocate them instead
  private static void storeReturnPoint(ServerPlayer player) {

    CompoundTag root = ((SummonsDataHolder) player).summons$getPersistentData();
    CompoundTag tag = new CompoundTag();

    BlockPos safe = findSafeReturnPos(player);

    tag.putString(DIMENSION_TAG, player.level().dimension().location().toString());
    tag.putDouble(X_TAG, safe.getX() + 0.5);
    tag.putDouble(Y_TAG, safe.getY());
    tag.putDouble(Z_TAG, safe.getZ() + 0.5);
    tag.putFloat(YAW_TAG, player.getYRot());
    tag.putFloat(PITCH_TAG, player.getXRot());
    tag.putString(GAMEMODE_TAG, player.gameMode.getGameModeForPlayer().getName());

    root.put(ROOT_TAG, tag);
  }

  /// walks backwards (away from the player's facing direction, i.e. back the way they came)
  /// from their current position, up to a few blocks, looking for a spot that isn't a
  /// [io.github.jason13official.summons.impl.common.block.SummonGateBlock] and has two clear
  /// blocks (feet + head) over solid ground; falls back to the player's raw position if nothing
  /// better turns up within range (e.g. they right-clicked from well outside the gate already)
  private static BlockPos findSafeReturnPos(ServerPlayer player) {

    ServerLevel level = player.serverLevel();
    Direction behind = player.getDirection().getOpposite();
    BlockPos start = player.blockPosition();

    for (int step = 0; step <= 4; step++) {
      BlockPos candidate = start.relative(behind, step);
      if (isSafeStandingSpot(level, candidate)) {
        return candidate;
      }
    }

    return start;
  }

  private static boolean isSafeStandingSpot(ServerLevel level, BlockPos pos) {

    BlockState feet = level.getBlockState(pos);
    BlockState head = level.getBlockState(pos.above());
    BlockState ground = level.getBlockState(pos.below());

    if (feet.getBlock() instanceof SummonGateBlock || head.getBlock() instanceof SummonGateBlock) {
      return false; // would just walk straight back into the gate on arrival
    }

    return feet.getCollisionShape(level, pos).isEmpty()
        && head.getCollisionShape(level, pos.above()).isEmpty()
        && !ground.getCollisionShape(level, pos.below()).isEmpty();
  }

  private static BlockPos cellOrigin(int cell) {
    return new BlockPos(cell * CELL_SPACING, FLOOR_Y, 0);
  }

  /// builds (or rebuilds) the room shell + a fresh petrified statue for `type`; idempotent and
  /// cheap enough to run on every visit, so a re-entry always reflects current progress instead
  /// of relying on a separate "already built" flag
  private static void buildRoom(ServerLevel level, BlockPos origin, CompanionType type) {

    BlockState floor = Blocks.POLISHED_BLACKSTONE.defaultBlockState();
    BlockState wall = Blocks.CRACKED_DEEPSLATE_BRICKS.defaultBlockState();
    BlockState ceiling = Blocks.GLOWSTONE.defaultBlockState(); // lights the room; no skylight in the pocket dimension
    BlockState air = Blocks.AIR.defaultBlockState();

    for (int x = 0; x < ROOM_SIZE; x++) {
      for (int z = 0; z < ROOM_SIZE; z++) {
        for (int y = 0; y <= ROOM_HEIGHT; y++) {
          boolean isCeiling = y == ROOM_HEIGHT;
          boolean edge = x == 0 || z == 0 || x == ROOM_SIZE - 1 || z == ROOM_SIZE - 1 || y == 0 || isCeiling;
          level.setBlockAndUpdate(origin.offset(x, y, z), !edge ? air : isCeiling ? ceiling : wall);
        }
      }
    }

    for (int x = 1; x < ROOM_SIZE - 1; x++) {
      for (int z = 1; z < ROOM_SIZE - 1; z++) {
        level.setBlockAndUpdate(origin.offset(x, 0, z), floor);
      }
    }

    BlockPos pedestal = origin.offset(ROOM_SIZE / 2, 1, ROOM_SIZE / 2);
    level.setBlockAndUpdate(pedestal, ModBlocks.forPetrified(type).defaultBlockState());
  }

  private static String displayName(CompanionType type) {
    return type.name().charAt(0) + type.name().substring(1).toLowerCase();
  }
}
