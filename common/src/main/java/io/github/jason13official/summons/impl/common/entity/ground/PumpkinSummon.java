package io.github.jason13official.summons.impl.common.entity.ground;

import io.github.jason13official.summons.impl.common.entity.AbstractCompanion;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;

/// Pumpkin-Type: bad at fighting and low health, but a big stat boost to the owner
/// (owner stat bonuses not implemented yet).
public class PumpkinSummon extends AbstractGroundCompanion {

  public PumpkinSummon(EntityType<? extends AbstractCompanion> entityType, Level level) {
    super(entityType, level);
  }

  public static AttributeSupplier.Builder createAttributes() {

    return AbstractCompanion.createAttributes().add(Attributes.MAX_HEALTH, (double) 8.0F)
        .add(Attributes.MOVEMENT_SPEED, (double) 0.18F).add(Attributes.ATTACK_DAMAGE, (double) 1.0F);
  }
}
