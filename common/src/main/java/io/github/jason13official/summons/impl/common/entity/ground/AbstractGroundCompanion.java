package io.github.jason13official.summons.impl.common.entity.ground;

import io.github.jason13official.summons.impl.common.entity.AbstractCompanion;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public abstract class AbstractGroundCompanion extends AbstractCompanion {

  public AbstractGroundCompanion(EntityType<? extends AbstractCompanion> entityType, Level level) {
    super(entityType, level);
  }

  @Override
  protected void registerGoals() {

    // Cow.class
    this.goalSelector.addGoal(0, new FloatGoal(this));
    this.goalSelector.addGoal(1, new PanicGoal(this, (double)2.0F));
    // this.goalSelector.addGoal(2, new BreedGoal(this, (double)1.0F));
    this.goalSelector.addGoal(3, new TemptGoal(this, (double)1.25F, (p_335386_) -> p_335386_.is(ItemTags.COW_FOOD), false));
    // this.goalSelector.addGoal(4, new FollowParentGoal(this, (double)1.25F));
    this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, (double)1.0F));
    this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 6.0F));
    this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
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
