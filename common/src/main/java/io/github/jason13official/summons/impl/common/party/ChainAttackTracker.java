package io.github.jason13official.summons.impl.common.party;

import io.github.jason13official.summons.impl.common.entity.AbstractCompanion;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.phys.Vec3;

/// Battle-/Devil-Type "Chain Attack" (wiki: land the final hit of a combo, a "Chain!"
/// prompt appears, pressing attack again warps the companion in for a bonus hit). Per-player
/// combo state kept here, not on the owner/companion entity; transient combat bookkeeping,
/// not something that needs to survive a relog or round-trip through NBT.
public final class ChainAttackTracker {

  private static final int COMBO_HITS_TO_ARM = 3; // consecutive same-weapon hits to arm Chain!
  private static final int COMBO_HIT_WINDOW_TICKS = 40; // combo resets if hits are slower than this
  private static final int CHAIN_WINDOW_TICKS = 30; // window to land the trigger hit once armed
  private static final double CHAIN_WARP_DISTANCE = 1.2;

  private static final Map<UUID, State> STATES = new HashMap<>();

  private static final class State {
    UUID targetId;
    Item weapon;
    int comboHits;
    int lastHitTick;
  }

  /// called from LivingEntityChainAttackMixin on every landed player melee hit
  public static void onPlayerMeleeHit(ServerPlayer player, LivingEntity target) {
    AbstractCompanion active = CompanionPartyManager.findActive(player.serverLevel(), player);
    if (active == null || !active.getCompanionType().isChainAttackCapable()) {
      return;
    }

    if (active.isChainArmed()) {
      active.setChainArmed(0);
      STATES.remove(player.getUUID());
      triggerChain(active, target);
      return;
    }

    State state = STATES.computeIfAbsent(player.getUUID(), id -> new State());
    Item weapon = player.getMainHandItem().getItem();
    int now = player.tickCount;

    boolean sameCombo = target.getUUID().equals(state.targetId) && weapon == state.weapon
        && now - state.lastHitTick <= COMBO_HIT_WINDOW_TICKS;
    state.comboHits = sameCombo ? state.comboHits + 1 : 1;
    state.targetId = target.getUUID();
    state.weapon = weapon;
    state.lastHitTick = now;

    if (state.comboHits >= COMBO_HITS_TO_ARM) {
      state.comboHits = 0;
      active.setChainArmed(CHAIN_WINDOW_TICKS);
    }
  }

  /// warps the companion to just beside the target (not on top of it) and lands a real
  /// direct attack via #doHurtTarget, so it's a genuine bonus hit, not a scripted number
  private static void triggerChain(AbstractCompanion companion, LivingEntity target) {
    if (!(companion.level() instanceof ServerLevel level)) {
      return;
    }

    level.sendParticles(ParticleTypes.POOF, companion.getX(), companion.getY() + 0.5, companion.getZ(), 10, 0.3, 0.3, 0.3, 0.0);

    Vec3 fromTarget = companion.position().subtract(target.position());
    if (fromTarget.lengthSqr() < 1.0E-4) {
      fromTarget = new Vec3(0.0, 0.0, 1.0);
    }
    Vec3 warpPos = target.position().add(fromTarget.normalize().scale(CHAIN_WARP_DISTANCE));
    float yaw = (float) (Math.toDegrees(Math.atan2(target.getZ() - warpPos.z, target.getX() - warpPos.x))) - 90.0F;
    companion.moveTo(warpPos.x, warpPos.y, warpPos.z, yaw, 0.0F);

    level.sendParticles(ParticleTypes.CLOUD, companion.getX(), companion.getY() + 0.5, companion.getZ(), 10, 0.3, 0.3, 0.3, 0.0);
    companion.doHurtTarget(target);
  }
}
