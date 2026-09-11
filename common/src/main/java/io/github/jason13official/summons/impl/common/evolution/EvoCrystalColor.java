package io.github.jason13official.summons.impl.common.evolution;

import io.github.jason13official.summons.impl.common.item.SpearItem;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TridentItem;
import org.jetbrains.annotations.Nullable;

/// Evo Crystal color; matches the weapon Hector's holding at the kill (wiki: Sword=Red,
/// Axe=Blue, Spear=Green, Knuckle=Yellow, Special=White).
public enum EvoCrystalColor {
  RED,
  BLUE,
  GREEN,
  YELLOW,
  WHITE;

  /// null for anything that isn't Sword/Axe/Spear/bare-fist(Knuckle)
  /// TODO we don't have Special weapons yet, so WHITE has nothing to map to
  @Nullable
  public static EvoCrystalColor fromWeapon(ItemStack heldItem) {
    if (heldItem.isEmpty()) {
      return YELLOW; // bare fists -> Knuckle
    }
    if (heldItem.is(ItemTags.SWORDS)) {
      return RED;
    }
    if (heldItem.is(ItemTags.AXES)) {
      return BLUE;
    }
    if (heldItem.getItem() instanceof SpearItem || heldItem.getItem() instanceof TridentItem) {
      return GREEN;
    }
    return null;
  }
}
