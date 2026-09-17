package io.github.jason13official.summons.impl.common.item;

import io.github.jason13official.summons.impl.common.party.CompanionType;
import io.github.jason13official.summons.impl.common.registry.ModItems;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;

/// Dropped by a companion's own combat kills (see `LivingEntityDevilShardDropMixin`); taken to a Julia-esque shopkeeper
/// to forge a fresh level-1 companion of `type`, seeded from 10% of `parentLevel`. Per-stack data
/// (type/parentLevel/generation) lives on vanilla's own [DataComponents#CUSTOM_DATA] rather than a new registered component;
/// one throwaway item, not worth a new registry.
public class DevilShardItem extends Item {

  private static final String TYPE_TAG = "CompanionType";
  private static final String PARENT_LEVEL_TAG = "ParentLevel";
  private static final String GENERATION_TAG = "Generation";

  public DevilShardItem(Properties properties) {
    super(properties);
  }

  public static ItemStack create(CompanionType type, int parentLevel, int generation) {
    ItemStack stack = new ItemStack(ModItems.DEVIL_SHARD);
    CompoundTag tag = new CompoundTag();
    tag.putString(TYPE_TAG, type.name());
    tag.putInt(PARENT_LEVEL_TAG, parentLevel);
    tag.putInt(GENERATION_TAG, generation);
    stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    return stack;
  }

  public static Optional<CompanionType> type(ItemStack stack) {
    return read(stack).flatMap(tag -> {
      try {
        return Optional.of(CompanionType.valueOf(tag.getString(TYPE_TAG)));
      } catch (IllegalArgumentException e) {
        return Optional.empty();
      }
    });
  }

  public static int parentLevel(ItemStack stack) {
    return read(stack).map(tag -> tag.getInt(PARENT_LEVEL_TAG)).orElse(1);
  }

  public static int generation(ItemStack stack) {
    return read(stack).map(tag -> tag.getInt(GENERATION_TAG)).orElse(0);
  }

  private static Optional<CompoundTag> read(ItemStack stack) {
    CustomData data = stack.get(DataComponents.CUSTOM_DATA);
    return data == null ? Optional.empty() : Optional.of(data.copyTag());
  }

  @Override
  public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
    super.appendHoverText(stack, context, tooltip, flag);
    type(stack).ifPresent(type -> tooltip.add(Component.translatable(
        "item.summons.devil_shard.tooltip", type.name(), parentLevel(stack), generation(stack) + 1)));
  }
}
