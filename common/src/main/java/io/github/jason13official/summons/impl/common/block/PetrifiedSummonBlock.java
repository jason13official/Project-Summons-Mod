package io.github.jason13official.summons.impl.common.block;

import io.github.jason13official.summons.impl.common.gate.SummonGateManager;
import io.github.jason13official.summons.impl.common.party.CompanionType;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/// a petrified Innocent Devil waiting in its Summon Gate pocket room; right-clicking shatters
/// the statue and unlocks `type` into the player's
/// [io.github.jason13official.summons.impl.common.party.CompanionParty]
public class PetrifiedSummonBlock extends Block {

  private final CompanionType type;

  public PetrifiedSummonBlock(CompanionType type, Properties properties) {
    super(properties);
    this.type = type;
  }

  public CompanionType type() {
    return this.type;
  }

  @Override
  protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
    if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer && level instanceof ServerLevel serverLevel) {
      SummonGateManager.unlockAtStatue(serverPlayer, serverLevel, pos, this.type);
    }

    return InteractionResult.sidedSuccess(level.isClientSide);
  }
}
