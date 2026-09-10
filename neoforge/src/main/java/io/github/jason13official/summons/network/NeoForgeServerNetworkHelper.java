package io.github.jason13official.summons.network;

import io.github.jason13official.summons.platform.services.IServerNetworkHelper;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.network.PacketDistributor;

public class NeoForgeServerNetworkHelper implements IServerNetworkHelper {

  @Override
  public void sendToTrackingClients(Entity entity, CustomPacketPayload payload) {
    PacketDistributor.sendToPlayersTrackingEntity(entity, payload);
  }
}
