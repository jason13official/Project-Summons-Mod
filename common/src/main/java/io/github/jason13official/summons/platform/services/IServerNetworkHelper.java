package io.github.jason13official.summons.platform.services;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.Entity;

/// Server -> tracking-clients push; the mirror of INetworkHelper's client -> server send. A separate interface since the two run on different physical sides and use different loader APIs (Fabric's
/// PlayerLookup.tracking vs NeoForge's PacketDistributor).
public interface IServerNetworkHelper {

  void sendToTrackingClients(Entity entity, CustomPacketPayload payload);
}
