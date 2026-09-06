package io.github.jason13official.summons.network;

import io.github.jason13official.summons.impl.common.network.SummonsNetworking.Action;
import io.github.jason13official.summons.impl.common.network.SummonsNetworking.CompanionInputPayload;
import io.github.jason13official.summons.platform.services.INetworkHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public class FabricNetworkHelper implements INetworkHelper {

  @Override
  public void sendCompanionInput(Action action) {
    ClientPlayNetworking.send(new CompanionInputPayload(action));
  }
}
