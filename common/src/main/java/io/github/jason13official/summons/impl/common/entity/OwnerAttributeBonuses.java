package io.github.jason13official.summons.impl.common.entity;

import io.github.jason13official.summons.Summons;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

/// STR -> owner ATTACK_DAMAGE, CON -> owner MAX_HEALTH; the two OwnerStatBonusKit stats
/// backed by a real vanilla attribute (CON also cuts harmful-effect duration via
/// LivingEntityEffectResistMixin, and LCK is read on demand by LivingEntityLuckyDropMixin -
/// neither of those needs a modifier here, so they're not handled by this class).
final class OwnerAttributeBonuses {
  private static final ResourceLocation STR_MODIFIER_ID = Summons.identifier("owner_str_bonus");
  private static final ResourceLocation CON_MODIFIER_ID = Summons.identifier("owner_con_bonus");

  static void refresh(AbstractCompanion companion) {
    LivingEntity owner = companion.getOwner();
    if (owner == null) {
      return;
    }

    OwnerStatBonusKit bonus = companion.ownerStatBonus();
    apply(owner, Attributes.ATTACK_DAMAGE, STR_MODIFIER_ID, bonus.str(companion.getLevel()));
    apply(owner, Attributes.MAX_HEALTH, CON_MODIFIER_ID, bonus.con(companion.getLevel()));
  }

  static void remove(AbstractCompanion companion) {
    LivingEntity owner = companion.getOwner();
    if (owner == null) {
      return;
    }

    removeModifier(owner, Attributes.ATTACK_DAMAGE, STR_MODIFIER_ID);
    removeModifier(owner, Attributes.MAX_HEALTH, CON_MODIFIER_ID);
  }

  private static void apply(LivingEntity owner, Holder<Attribute> attribute, ResourceLocation id, double amount) {
    AttributeInstance instance = owner.getAttribute(attribute);
    if (instance != null) {
      instance.addOrUpdateTransientModifier(new AttributeModifier(id, amount, AttributeModifier.Operation.ADD_VALUE));
    }
  }

  private static void removeModifier(LivingEntity owner, Holder<Attribute> attribute, ResourceLocation id) {
    AttributeInstance instance = owner.getAttribute(attribute);
    if (instance != null) {
      instance.removeModifier(id);
    }
  }
}
