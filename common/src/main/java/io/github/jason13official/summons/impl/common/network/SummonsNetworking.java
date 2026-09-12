package io.github.jason13official.summons.impl.common.network;

import io.github.jason13official.summons.Summons;
import io.github.jason13official.summons.impl.common.entity.AbstractCompanion;
import io.github.jason13official.summons.impl.common.party.CompanionPartyManager;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

public class SummonsNetworking {

  public static void handle(ServerPlayer player, Action action) {
    switch (action) {
      case TOGGLE_SUMMON -> CompanionPartyManager.toggle(player);
      case CYCLE_PARTY -> CompanionPartyManager.cycle(player, 1);
      case MODE_UP -> withActive(player, companion -> companion.cycleMode(1));
      case MODE_DOWN -> withActive(player, companion -> companion.cycleMode(-1));
      case ABILITY_LEFT -> withActive(player, companion -> companion.cycleAbility(-1));
      case ABILITY_RIGHT -> withActive(player, companion -> companion.cycleAbility(1));
      case COMMAND -> withActive(player, AbstractCompanion::performCommandAbility);
    }
  }

  /// client-side receivers for the three companion sync payloads; each applied to whichever companion entity matches the id in the client's own level (the normal tracked-entity mirror), if any
  public static void handleProgressSync(CompanionProgressSyncPayload payload) {
    if (findSyncTarget(payload.entityId()) instanceof AbstractCompanion companion) {
      companion.applyProgressSyncPayload(payload);
    }
  }

  public static void handleStateSync(CompanionStateSyncPayload payload) {
    if (findSyncTarget(payload.entityId()) instanceof AbstractCompanion companion) {
      companion.applyStateSyncPayload(payload);
    }
  }

  public static void handleIdentitySync(CompanionIdentitySyncPayload payload) {
    if (findSyncTarget(payload.entityId()) instanceof AbstractCompanion companion) {
      companion.applyIdentitySyncPayload(payload);
    }
  }

  private static Entity findSyncTarget(int entityId) {
    Minecraft mc = Minecraft.getInstance();
    return mc.level != null ? mc.level.getEntity(entityId) : null;
  }

  private static void withActive(ServerPlayer player, Consumer<AbstractCompanion> action) {
    AbstractCompanion active = CompanionPartyManager.findActive(player.serverLevel(), player);
    if (active != null) {
      action.accept(active);
    }
  }

  /// @see io.github.jason13official.summons.impl.client.SummonsKeyBindings
  public enum Action {
    TOGGLE_SUMMON,
    CYCLE_PARTY,
    MODE_UP,
    MODE_DOWN,
    ABILITY_LEFT,
    ABILITY_RIGHT,
    COMMAND
  }

  public record CompanionInputPayload(Action action) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<CompanionInputPayload> TYPE =
        new CustomPacketPayload.Type<>(Summons.identifier("companion_input"));

    public static final StreamCodec<FriendlyByteBuf, CompanionInputPayload> STREAM_CODEC = CustomPacketPayload.codec(
        (payload, buf) -> buf.writeEnum(payload.action()),
        buf -> new CompanionInputPayload(buf.readEnum(Action.class)));

    @Override
    public Type<? extends CustomPacketPayload> type() {

      return TYPE;
    }
  }
}
