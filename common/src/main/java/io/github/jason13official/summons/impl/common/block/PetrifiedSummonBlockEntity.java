package io.github.jason13official.summons.impl.common.block;

import io.github.jason13official.summons.impl.common.registry.ModTiles;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/// mirrors [io.github.jason13official.summons.impl.common.gate.SummonGateManager]'s PendingWarp countdown for rendering; the block entity is not the countdown's authority, just a client-visible copy of its progress
public class PetrifiedSummonBlockEntity extends BlockEntity {

  private int ticksLeft;
  private int totalTicks;

  public PetrifiedSummonBlockEntity(BlockPos pos, BlockState state) {
    super(ModTiles.PETRIFIED_SUMMON, pos, state);
  }

  public void setAwakenProgress(int ticksLeft, int totalTicks) {

    this.ticksLeft = ticksLeft;
    this.totalTicks = totalTicks;
    this.setChanged();

    if (this.level != null) {
      this.level.sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), Block.UPDATE_CLIENTS);
    }
  }

  public int ticksLeft() {
    return this.ticksLeft;
  }

  /// 0 while idle, ramping to 1 across the countdown
  public float progress(float partialTick) {

    if (this.totalTicks <= 0) {
      return 0.0F;
    }

    return Mth.clamp(1.0F - (this.ticksLeft - partialTick) / (float) this.totalTicks, 0.0F, 1.0F);
  }

  @Override
  protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
    super.saveAdditional(tag, registries);
    tag.putInt("ticks_left", this.ticksLeft);
    tag.putInt("total_ticks", this.totalTicks);
  }

  @Override
  protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
    super.loadAdditional(tag, registries);
    this.ticksLeft = tag.getInt("ticks_left");
    this.totalTicks = tag.getInt("total_ticks");
  }

  @Nullable
  @Override
  public ClientboundBlockEntityDataPacket getUpdatePacket() {
    return ClientboundBlockEntityDataPacket.create(this);
  }

  @Override
  public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
    return this.saveCustomOnly(registries);
  }
}
