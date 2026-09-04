package io.github.jason13official.summons.impl.common.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.level.Level;

public abstract class AbstractCompanionEntity extends PathfinderMob {

  public AbstractCompanionEntity(EntityType<? extends AbstractCompanionEntity> entityType, Level level) {
    super(entityType, level);
  }
}
