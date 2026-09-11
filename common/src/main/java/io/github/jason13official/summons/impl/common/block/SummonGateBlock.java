package io.github.jason13official.summons.impl.common.block;

import io.github.jason13official.summons.impl.common.gate.SummonGateManager;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/// the green-door "pocket dimension" entrance (Castlevania: Curse of Darkness's Innocent Devil
/// rooms); the walk-through opening of [io.github.jason13official.summons.impl.common.worldgen.SummonGateFeature]'s
/// archway -> sends the player to their [SummonGateManager] pocket room to unlock their next
/// [io.github.jason13official.summons.impl.common.party.CompanionType], on right-click or on
/// simply walking into it (`noCollission`, like a Nether Portal -> the block keeps its normal
/// outline/hitbox for right-click, it just doesn't physically stop the player)
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

  @Override
  protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
    if (!level.isClientSide && entity instanceof ServerPlayer serverPlayer) {
      SummonGateManager.enterGate(serverPlayer);
    }
  }
}
