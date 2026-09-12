package io.github.jason13official.summons.impl.common.network;

import io.github.jason13official.summons.Summons;
import io.github.jason13official.summons.impl.common.entity.CrystalPoints;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/// Server -> tracking clients: fastest-changing companion fields (crystal pickups, XP). Split from CompanionStateSyncPayload/CompanionIdentitySyncPayload so a crystal tick doesn't also resend
/// rarely-changing fields like evolution form.
public record CompanionProgressSyncPayload(int entityId, CrystalPoints crystals, int level,
                                           int experience) implements CustomPacketPayload {

  public static final CustomPacketPayload.Type<CompanionProgressSyncPayload> TYPE =
      new CustomPacketPayload.Type<>(Summons.identifier("companion_sync_progress"));

  public static final StreamCodec<FriendlyByteBuf, CompanionProgressSyncPayload> STREAM_CODEC =
      StreamCodec.of(CompanionProgressSyncPayload::write, CompanionProgressSyncPayload::read);

  private static void write(FriendlyByteBuf buf, CompanionProgressSyncPayload payload) {
    buf.writeVarInt(payload.entityId);
    CrystalPoints.STREAM_CODEC.encode(buf, payload.crystals);
    buf.writeVarInt(payload.level);
    buf.writeVarInt(payload.experience);
  }

  private static CompanionProgressSyncPayload read(FriendlyByteBuf buf) {
    int entityId = buf.readVarInt();
    CrystalPoints crystals = CrystalPoints.STREAM_CODEC.decode(buf);
    int level = buf.readVarInt();
    int experience = buf.readVarInt();
    return new CompanionProgressSyncPayload(entityId, crystals, level, experience);
  }

  @Override
  public Type<? extends CustomPacketPayload> type() {
    return TYPE;
  }
}
