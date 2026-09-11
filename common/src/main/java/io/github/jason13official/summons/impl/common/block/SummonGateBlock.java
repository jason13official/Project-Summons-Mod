package io.github.jason13official.summons.impl.common.block;

import io.github.jason13official.summons.impl.common.gate.SummonGateManager;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/// the green-door "pocket dimension" entrance (Castlevania: Curse of Darkness's Innocent Devil
/// rooms); rare overworld world-gen spawn, sends the player to their [SummonGateManager] pocket
/// room to unlock their next [io.github.jason13official.summons.impl.common.party.CompanionType]
public class SummonGateBlock extends Block {

  public SummonGateBlock(Properties properties) {
    super(properties);
  }

  @Override
  protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
    if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
      SummonGateManager.enterGate(serverPlayer);
    }

    return InteractionResult.sidedSuccess(level.isClientSide);
  }
}
