package io.github.jason13official.summons.impl.common.item;

import net.minecraft.world.item.Item;

/// marker type so `ItemEntityHeartPickupMixin` can recognize it; never actually enters an inventory, see that mixin
public class HeartItem extends Item {

  public HeartItem(Properties properties) {
    super(properties);
  }
}
