package io.github.jason13official.summons.impl.common.registry;

import io.github.jason13official.summons.Summons;
import io.github.jason13official.summons.impl.common.block.PetrifiedSummonBlockEntity;
import io.github.jason13official.summons.impl.common.party.CompanionType;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.function.BiConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class ModTiles {

  public static BlockEntityType<PetrifiedSummonBlockEntity> PETRIFIED_SUMMON;

  public static void register(BiConsumer<BlockEntityType<?>, ResourceLocation> consumer) {

    Block[] petrifiedBlocks = new Block[CompanionType.values().length];
    for (int i = 0; i < petrifiedBlocks.length; i++) {
      petrifiedBlocks[i] = ModBlocks.forPetrified(CompanionType.values()[i]);
    }

    PETRIFIED_SUMMON = newPetrifiedSummonType(petrifiedBlocks);
    consumer.accept(PETRIFIED_SUMMON, Summons.identifier("petrified_summon"));
  }

  /// BlockEntityType.Builder#of's factory parameter type (BlockEntityType.BlockEntitySupplier) is package-private in vanilla, so mod code can't name
  /// it directly; a dynamic proxy stands in for it instead, since reflection only needs the interface's Class object, not source-level access
  @SuppressWarnings("unchecked")
  private static BlockEntityType<PetrifiedSummonBlockEntity> newPetrifiedSummonType(Block[] blocks) {
    try {
      Method of = null;
      for (Method candidate : BlockEntityType.Builder.class.getMethods()) {
        if (candidate.getName().equals("of") && candidate.getParameterCount() == 2 && candidate.getParameterTypes()[1] == Block[].class) {
          of = candidate;
          break;
        }
      }
      if (of == null) {
        throw new IllegalStateException("BlockEntityType.Builder#of(supplier, Block...) not found");
      }

      Class<?> supplierClass = of.getParameterTypes()[0];
      Object supplier = Proxy.newProxyInstance(supplierClass.getClassLoader(), new Class[]{supplierClass},
          (proxy, interfaceMethod, args) -> new PetrifiedSummonBlockEntity((BlockPos) args[0], (BlockState) args[1]));

      BlockEntityType.Builder<PetrifiedSummonBlockEntity> builder = (BlockEntityType.Builder<PetrifiedSummonBlockEntity>) of.invoke(null, supplier, blocks);
      return builder.build(null);
    } catch (ReflectiveOperationException e) {
      throw new RuntimeException("Failed to build the petrified summon BlockEntityType", e);
    }
  }
}
