package io.github.jason13official.summons.impl.common.item;

import io.github.jason13official.summons.impl.common.evolution.EvoCrystalColor;
import net.minecraft.world.item.Item;

public class EvoCrystalItem extends Item {

  private final EvoCrystalColor color;

  public EvoCrystalItem(EvoCrystalColor color, Properties properties) {
    super(properties);
    this.color = color;
  }

  public EvoCrystalColor color() {
    return this.color;
  }
}
