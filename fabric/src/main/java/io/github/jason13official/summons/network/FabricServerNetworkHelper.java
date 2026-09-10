package io.github.jason13official.summons.network;

import io.github.jason13official.summons.platform.services.IServerNetworkHelper;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

public class FabricServerNetworkHelper implements IServerNetworkHelper {

  @Override
  public void sendToTrackingClients(Entity entity, CustomPacketPayload payload) {
    for (ServerPlayer player : PlayerLookup.tracking(entity)) {
      ServerPlayNetworking.send(player, payload);
    }
  }
}
