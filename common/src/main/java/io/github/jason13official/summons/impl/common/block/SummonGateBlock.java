package io.github.jason13official.summons.impl.common.block;

import com.mojang.serialization.MapCodec;
import io.github.jason13official.summons.impl.common.gate.SummonGateManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.portal.PortalShape;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/// the green-door "pocket dimension" entrance (Castlevania: Curse of Darkness's Innocent Devil rooms);
/// this is the walk-through opening of `SummonGateFeature`'s archway -> sends the player to their `SummonGateManager` pocket room,
/// to unlock their next `CompanionType`, on right-click or by simply walking into it
public class SummonGateBlock extends Block {

  public static final EnumProperty<Axis> AXIS = BlockStateProperties.HORIZONTAL_AXIS;
  public static final MapCodec<SummonGateBlock> CODEC = simpleCodec(SummonGateBlock::new);
  public static final VoxelShape X_AXIS_AABB = Block.box(0.0F, 0.0F, 6.0F, 16.0F, 16.0F, 10.0F);
  public static final VoxelShape Z_AXIS_AABB = Block.box(6.0F, 0.0F, 0.0F, 10.0F, 16.0F, 16.0F);

  public SummonGateBlock(Properties properties) {
    super(properties);
    this.registerDefaultState(this.stateDefinition.any().setValue(AXIS, Axis.X));
  }

  @Override
  public MapCodec<SummonGateBlock> codec() {

    return CODEC;
  }

  public void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {

    builder.add(AXIS);
  }

  @Override
  public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {

    if (state.getValue(AXIS) == Axis.Z) {
      return Z_AXIS_AABB;
    }

    return X_AXIS_AABB;
  }

  @Override
  public BlockState updateShape(BlockState state, Direction neighborDirection, BlockState neighborState, LevelAccessor level, BlockPos currentPos, BlockPos neighborPos) {

    Direction.Axis selfAxis = state.getValue(AXIS);
    Direction.Axis neighborAxis = neighborDirection.getAxis();

    boolean neighborPerpendicular = selfAxis != neighborAxis && neighborAxis.isHorizontal();

    PortalShape portalShape = new PortalShape(level, currentPos, selfAxis);

    if (!neighborPerpendicular && !neighborState.is(this) && !portalShape.isComplete()) {
      return Blocks.AIR.defaultBlockState();
    }

    return super.updateShape(state, neighborDirection, neighborState, level, currentPos, neighborPos);
  }

  @Override
  public BlockState rotate(BlockState state, Rotation rot) {

    if (rot == Rotation.CLOCKWISE_90) {
      if (state.getValue(AXIS) == Axis.Z) {
        return state.setValue(AXIS, Axis.X);
      } else if (state.getValue(AXIS) == Axis.X) {
        return state.setValue(AXIS, Axis.Z);
      }
    }

    return state;
  }

  @Override
  public InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
    if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
      SummonGateManager.enterGate(serverPlayer);
    }

    return InteractionResult.sidedSuccess(level.isClientSide);
  }

  @Override
  public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
    if (!level.isClientSide() && entity instanceof ServerPlayer serverPlayer) {
      SummonGateManager.enterGate(serverPlayer);
    }
  }
}
