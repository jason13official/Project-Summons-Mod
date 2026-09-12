package io.github.jason13official.summons.impl.common.worldgen;

import com.mojang.serialization.Codec;
import io.github.jason13official.summons.impl.common.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/// the overworld "green door" landmark (Castlevania: Curse of Darkness) -> a small gothic archway, two blocks deep, housing a walk-through/right-clickable
/// [io.github.jason13official.summons.impl.common.block.SummonGateBlock] opening
public class SummonGateFeature extends Feature<NoneFeatureConfiguration> {

  public SummonGateFeature(Codec<NoneFeatureConfiguration> codec) {
    super(codec);
  }

  @Override
  public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {

    WorldGenLevel level = context.level();
    BlockPos ground = context.origin();

    BlockState frame = Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState();
    BlockState pillar = Blocks.POLISHED_BLACKSTONE_BRICK_WALL.defaultBlockState();
    BlockState lantern = Blocks.SOUL_LANTERN.defaultBlockState();
    BlockState gate = ModBlocks.SUMMON_GATE.defaultBlockState();

    for (int z = 0; z <= 1; z++) {
      for (int y = 0; y <= 4; y++) {
        level.setBlock(ground.offset(-1, y, z), frame, 2);
        level.setBlock(ground.offset(1, y, z), frame, 2);
      }
      level.setBlock(ground.offset(0, 4, z), frame, 2); // lintel

      for (int y = 0; y <= 3; y++) {
        level.setBlock(ground.offset(0, y, z), gate, 2); // walk-through/clickable opening
      }
    }

    level.setBlock(ground.offset(-2, 0, 0), pillar, 2);
    level.setBlock(ground.offset(-2, 1, 0), lantern, 2);
    level.setBlock(ground.offset(2, 0, 0), pillar, 2);
    level.setBlock(ground.offset(2, 1, 0), lantern, 2);

    return true;
  }
}
