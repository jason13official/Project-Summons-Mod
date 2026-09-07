package io.github.jason13official.summons.impl.common.evolution;

import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TridentItem;

/// Evo Crystal color; matches the weapon Hector's holding at the kill (wiki: Sword=Red,
/// Axe=Blue, Spear=Green, Knuckle=Yellow, Special=White).
public enum EvoCrystalColor {
  RED,
  BLUE,
  GREEN,
  YELLOW,
  WHITE;

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
    if (heldItem.getItem() instanceof TridentItem) {
      return GREEN; // closest vanilla analog to Spear
    }
    return WHITE; // Special Weapon
  }
}
