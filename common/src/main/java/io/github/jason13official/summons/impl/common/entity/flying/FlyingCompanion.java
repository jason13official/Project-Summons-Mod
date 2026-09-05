package io.github.jason13official.summons.impl.common.entity.flying;

import io.github.jason13official.summons.impl.common.entity.AbstractCompanion;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowMobGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomFlyingGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.entity.animal.FlyingAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public abstract class FlyingCompanion extends AbstractCompanion implements FlyingAnimal {

  public FlyingCompanion(EntityType<? extends AbstractCompanion> entityType, Level level) {
    super(entityType, level);

    // Parrot.class
    this.moveControl = new FlyingMoveControl(this, 10, false);
    this.setPathfindingMalus(PathType.DANGER_FIRE, -1.0F);
    this.setPathfindingMalus(PathType.DAMAGE_FIRE, -1.0F);
  }

  public static AttributeSupplier.Builder createAttributes() {

    // Parrot.class
    return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, (double)6.0F).add(Attributes.FLYING_SPEED, (double)0.4F).add(Attributes.MOVEMENT_SPEED, (double)0.2F).add(Attributes.ATTACK_DAMAGE, (double)3.0F);
  }

  @Override
  protected PathNavigation createNavigation(Level level) {

    // Parrot.class
    FlyingPathNavigation flyingPathNavigation = new FlyingPathNavigation(this, level);
    flyingPathNavigation.setCanOpenDoors(false);
    flyingPathNavigation.setCanFloat(true);
    flyingPathNavigation.setCanPassDoors(true);
    return flyingPathNavigation;
  }

  @Override
  public boolean isFlying() {

    // Parrot.class
    return !this.onGround();
  }

  @Override
  protected void checkFallDamage(double y, boolean onGround, BlockState state, BlockPos pos) {

    // no-op
  }

  @Override
  protected void registerGoals() {

    // Parrot.class
    // this.goalSelector.addGoal(0, new TamableAnimal.TamableAnimalPanicGoal(this, (double)1.25F));
    this.goalSelector.addGoal(0, new FloatGoal(this));
    this.goalSelector.addGoal(1, new LookAtPlayerGoal(this, Player.class, 8.0F));
    // this.goalSelector.addGoal(2, new SitWhenOrderedToGoal(this));
    // this.goalSelector.addGoal(2, new FollowOwnerGoal(this, (double)1.0F, 5.0F, 1.0F));
    this.goalSelector.addGoal(2, new CustomWanderGoal(this, (double)1.0F));
    // this.goalSelector.addGoal(3, new LandOnOwnersShoulderGoal(this));
    this.goalSelector.addGoal(3, new FollowMobGoal(this, (double)1.0F, 3.0F, 7.0F));
  }

  /// Parrot$CustomWanderGoal.class
  public static class CustomWanderGoal extends WaterAvoidingRandomFlyingGoal {
    public CustomWanderGoal(PathfinderMob pathfinder, double speed) {
      super(pathfinder, speed);
    }

    @Nullable
    protected Vec3 getPosition() {
      Vec3 vec3 = null;
      if (this.mob.isInWater()) {
        vec3 = LandRandomPos.getPos(this.mob, 15, 15);
      }

      if (this.mob.getRandom().nextFloat() >= this.probability) {
        vec3 = this.getTreePos();
      }

      return vec3 == null ? super.getPosition() : vec3;
    }

    @Nullable
    private Vec3 getTreePos() {
      BlockPos blockPos = this.mob.blockPosition();
      BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
      BlockPos.MutableBlockPos mutableBlockPos2 = new BlockPos.MutableBlockPos();

      for(BlockPos blockPos2 : BlockPos.betweenClosed(
          Mth.floor(this.mob.getX() - (double)3.0F), Mth.floor(this.mob.getY() - (double)6.0F), Mth.floor(this.mob.getZ() - (double)3.0F), Mth.floor(this.mob.getX() + (double)3.0F), Mth.floor(this.mob.getY() + (double)6.0F), Mth.floor(this.mob.getZ() + (double)3.0F))) {
        if (!blockPos.equals(blockPos2)) {
          BlockState blockState = this.mob.level().getBlockState(mutableBlockPos2.setWithOffset(blockPos2, Direction.DOWN));
          boolean bl = blockState.getBlock() instanceof LeavesBlock || blockState.is(BlockTags.LOGS);
          if (bl && this.mob.level().isEmptyBlock(blockPos2) && this.mob.level().isEmptyBlock(mutableBlockPos.setWithOffset(blockPos2, Direction.UP))) {
            return Vec3.atBottomCenterOf(blockPos2);
          }
        }
      }

      return null;
    }
  }
}
