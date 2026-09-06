package io.github.jason13official.summons.api.common.util;

import net.minecraft.nbt.CompoundTag;

public interface SummonsDataHolder {

  CompoundTag summons$getPersistentData();

  void summons$setPersistentData(CompoundTag compoundTag);
}
