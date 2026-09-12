package io.github.jason13official.summons.impl.common.network;

import io.github.jason13official.summons.Summons;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/// Server -> tracking clients: rarest-changing fields (type set once at spawn, form/wisp only on evolution/death). Split out so these almost-static fields aren't resent on every crystal pickup or
/// ability use.
public record CompanionIdentitySyncPayload(int entityId, byte companionType, String evolutionForm,
                                           String reachedForms, boolean wisp) implements CustomPacketPayload {

  public static final CustomPacketPayload.Type<CompanionIdentitySyncPayload> TYPE =
      new CustomPacketPayload.Type<>(Summons.identifier("companion_sync_identity"));

  public static final StreamCodec<FriendlyByteBuf, CompanionIdentitySyncPayload> STREAM_CODEC =
      StreamCodec.of(CompanionIdentitySyncPayload::write, CompanionIdentitySyncPayload::read);

  private static void write(FriendlyByteBuf buf, CompanionIdentitySyncPayload payload) {
    buf.writeVarInt(payload.entityId);
    buf.writeByte(payload.companionType);
    buf.writeUtf(payload.evolutionForm);
    buf.writeUtf(payload.reachedForms);
    buf.writeBoolean(payload.wisp);
  }

  private static CompanionIdentitySyncPayload read(FriendlyByteBuf buf) {
    int entityId = buf.readVarInt();
    byte companionType = buf.readByte();
    String evolutionForm = buf.readUtf();
    String reachedForms = buf.readUtf();
    boolean wisp = buf.readBoolean();
    return new CompanionIdentitySyncPayload(entityId, companionType, evolutionForm, reachedForms, wisp);
  }

  @Override
  public Type<? extends CustomPacketPayload> type() {
    return TYPE;
  }
}
