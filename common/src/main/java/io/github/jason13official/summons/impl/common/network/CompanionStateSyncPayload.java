package io.github.jason13official.summons.impl.common.network;

import io.github.jason13official.summons.Summons;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/// Server -> tracking clients: mid-frequency companion fields (mode swaps, ability selection/busy, Chain Attack armed, Guard Field radius); combat/control state that changes less often than
/// crystals/XP but far more often than identity/evolution.
public record CompanionStateSyncPayload(int entityId, byte mode, byte abilityIndex, boolean abilityBusy,
                                        boolean chainArmed, float guardFieldRadius) implements CustomPacketPayload {

  public static final CustomPacketPayload.Type<CompanionStateSyncPayload> TYPE =
      new CustomPacketPayload.Type<>(Summons.identifier("companion_sync_state"));

  public static final StreamCodec<FriendlyByteBuf, CompanionStateSyncPayload> STREAM_CODEC =
      StreamCodec.of(CompanionStateSyncPayload::write, CompanionStateSyncPayload::read);

  private static void write(FriendlyByteBuf buf, CompanionStateSyncPayload payload) {
    buf.writeVarInt(payload.entityId);
    buf.writeByte(payload.mode);
    buf.writeByte(payload.abilityIndex);
    buf.writeBoolean(payload.abilityBusy);
    buf.writeBoolean(payload.chainArmed);
    buf.writeFloat(payload.guardFieldRadius);
  }

  private static CompanionStateSyncPayload read(FriendlyByteBuf buf) {
    int entityId = buf.readVarInt();
    byte mode = buf.readByte();
    byte abilityIndex = buf.readByte();
    boolean abilityBusy = buf.readBoolean();
    boolean chainArmed = buf.readBoolean();
    float guardFieldRadius = buf.readFloat();
    return new CompanionStateSyncPayload(entityId, mode, abilityIndex, abilityBusy, chainArmed, guardFieldRadius);
  }

  @Override
  public Type<? extends CustomPacketPayload> type() {
    return TYPE;
  }
}
