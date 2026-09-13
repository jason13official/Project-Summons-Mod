package io.github.jason13official.summons.impl.common.registry;

import io.github.jason13official.summons.Summons;
import io.github.jason13official.summons.impl.common.block.PetrifiedSummonBlockEntity;
import io.github.jason13official.summons.impl.common.party.CompanionType;
import java.util.function.BiConsumer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class ModTiles {

  public static BlockEntityType<PetrifiedSummonBlockEntity> PETRIFIED_SUMMON;

  public static void register(BiConsumer<BlockEntityType<?>, ResourceLocation> consumer) {

    petrified(consumer);
  }

  private static void petrified(BiConsumer<BlockEntityType<?>, ResourceLocation> consumer) {
    Block[] petrifiedBlocks = new Block[CompanionType.values().length];
    for (int i = 0; i < petrifiedBlocks.length; i++) {
      petrifiedBlocks[i] = ModBlocks.forPetrified(CompanionType.values()[i]);
    }

    PETRIFIED_SUMMON = BlockEntityType.Builder.of(PetrifiedSummonBlockEntity::new, petrifiedBlocks).build(null);
    consumer.accept(PETRIFIED_SUMMON, Summons.identifier("petrified_summon"));
  }
}
