package io.github.jason13official.summons.platform.services;

import io.github.jason13official.summons.impl.common.network.SummonsNetworking.Action;

public interface INetworkHelper {

  void sendCompanionInput(Action action);

  /// @see io.github.jason13official.summons.impl.client.gui.screen.EvolutionChartScreen
  void sendDebugSetForm(String formId);

  /// @see io.github.jason13official.summons.impl.client.gui.screen.ShardForgeScreen
  void sendForgeShard(int inventorySlot);
}
