package io.github.jason13official.summons.network;

import io.github.jason13official.summons.impl.common.network.SummonsNetworking.Action;
import io.github.jason13official.summons.impl.common.network.SummonsNetworking.CompanionInputPayload;
import io.github.jason13official.summons.platform.services.INetworkHelper;
import net.neoforged.neoforge.network.PacketDistributor;

public class NeoForgeNetworkHelper implements INetworkHelper {

  @Override
  public void sendCompanionInput(Action action) {
    PacketDistributor.sendToServer(new CompanionInputPayload(action));
  }
}
