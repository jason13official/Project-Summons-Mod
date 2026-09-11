package io.github.jason13official.summons.impl.common.registry;

import io.github.jason13official.summons.Summons;
import io.github.jason13official.summons.impl.common.worldgen.SummonGateFeature;
import java.util.function.BiConsumer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class ModFeatures {

  public static Feature<NoneFeatureConfiguration> SUMMON_GATE;

  public static void register(BiConsumer<Feature<?>, ResourceLocation> consumer) {

    SUMMON_GATE = new SummonGateFeature(NoneFeatureConfiguration.CODEC);
    consumer.accept(SUMMON_GATE, Summons.identifier("summon_gate"));
  }
}
