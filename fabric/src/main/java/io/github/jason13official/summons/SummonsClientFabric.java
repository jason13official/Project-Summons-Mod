package io.github.jason13official.summons;

import net.fabricmc.api.ClientModInitializer;

public class SummonsClientFabric implements ClientModInitializer {

  @Override
  public void onInitializeClient() {

    SummonsClient.init();
  }
}
