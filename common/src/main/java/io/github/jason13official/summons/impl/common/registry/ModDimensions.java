package io.github.jason13official.summons.impl.common.registry;

import io.github.jason13official.summons.Summons;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

/// the shared per-player Innocent Devil pocket dimension; see `impl.common.gate.SummonGateManager`
public class ModDimensions {

  public static final ResourceKey<Level> SUMMON_POCKET =
      ResourceKey.create(Registries.DIMENSION, Summons.identifier("summon_pocket"));
}
