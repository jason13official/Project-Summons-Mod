package io.github.jason13official.summons.impl.common.registry;

import io.github.jason13official.summons.Summons;
import io.github.jason13official.summons.impl.common.block.PetrifiedSummonBlock;
import io.github.jason13official.summons.impl.common.block.SummonGateBlock;
import io.github.jason13official.summons.impl.common.party.CompanionType;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.BiConsumer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.material.MapColor;

public class ModBlocks {

  public static Block SUMMON_GATE;

  private static final Map<CompanionType, Block> PETRIFIED = new EnumMap<>(CompanionType.class);

  public static void register(BiConsumer<Block, ResourceLocation> consumer) {

    // overworld landmark, not the pocket room itself - normal strength, movable/breakable like
    // any other block (the pocket-side petrified statues below are the ones kept unbreakable)
    SUMMON_GATE = new SummonGateBlock(Properties.of()
        .mapColor(MapColor.COLOR_BLACK)
        .sound(SoundType.STONE)
        .strength(3.5F)
        .requiresCorrectToolForDrops());
    consumer.accept(SUMMON_GATE, Summons.identifier("summon_gate"));

    for (CompanionType type : CompanionType.values()) {
      Block petrified = new PetrifiedSummonBlock(type, Properties.of()
          .mapColor(MapColor.STONE)
          .sound(SoundType.STONE)
          .strength(-1.0F, 3600000.0F)
          .noLootTable()); // never mined - shattered by SummonGateManager.unlockAtStatue instead

      PETRIFIED.put(type, petrified);
      consumer.accept(petrified, Summons.identifier("petrified_" + type.name().toLowerCase()));
    }
  }

  public static Block forPetrified(CompanionType type) {
    return PETRIFIED.get(type);
  }
}
