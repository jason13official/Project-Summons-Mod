package io.github.jason13official.summons.impl.common.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public abstract class AbstractCompanion extends PathfinderMob {

  public AbstractCompanion(EntityType<? extends AbstractCompanion> entityType, Level level) {
    super(entityType, level);
  }

  public static AttributeSupplier.Builder createAttributes() {

    // Cow.class
    return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 10.0D).add(Attributes.MOVEMENT_SPEED, 0.2D);
  }

  // region sound
  @Override
  protected SoundEvent getAmbientSound() {

    // Cow.class
    return SoundEvents.COW_AMBIENT;
  }

  @Override
  protected SoundEvent getHurtSound(DamageSource damageSource) {

    // Cow.class
    return SoundEvents.COW_HURT;
  }

  @Override
  protected SoundEvent getDeathSound() {

    // Cow.class
    return SoundEvents.COW_DEATH;
  }

  @Override
  protected void playStepSound(BlockPos pos, BlockState block) {

    // Cow.class
    this.playSound(SoundEvents.COW_STEP, 0.15F, 1.0F);
  }

  @Override
  protected float getSoundVolume() {

    // Cow.class
    return 0.4F;
  }
  // endregion sound
}
