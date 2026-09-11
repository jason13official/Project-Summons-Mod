package io.github.jason13official.summons.impl.common.item;

import io.github.jason13official.summons.impl.common.evolution.EvoCrystalColor;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.ItemAttributeModifiers;

/// Curse of Darkness "Spear" weapon class (pole weapons: Deck Brush, Naginata, Trident,
/// etc). No vanilla equivalent -> a distinct marker type so [EvoCrystalColor#fromWeapon]
/// can recognize it (GREEN, mirroring the wiki's Sword/Axe/Spear/Knuckle mapping).
public class SpearItem extends Item {

  public SpearItem(float attackDamage, float attackSpeed, Properties properties) {
    super(properties.attributes(createAttributes(attackDamage, attackSpeed)));
  }

  private static ItemAttributeModifiers createAttributes(float attackDamage, float attackSpeed) {
    return ItemAttributeModifiers.builder()
        .add(Attributes.ATTACK_DAMAGE,
            new AttributeModifier(BASE_ATTACK_DAMAGE_ID, attackDamage, AttributeModifier.Operation.ADD_VALUE),
            EquipmentSlotGroup.MAINHAND)
        .add(Attributes.ATTACK_SPEED,
            new AttributeModifier(BASE_ATTACK_SPEED_ID, attackSpeed, AttributeModifier.Operation.ADD_VALUE),
            EquipmentSlotGroup.MAINHAND)
        .build();
  }
}
