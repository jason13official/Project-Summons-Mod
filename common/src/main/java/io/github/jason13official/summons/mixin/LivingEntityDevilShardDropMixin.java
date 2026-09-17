package io.github.jason13official.summons.mixin;

import io.github.jason13official.summons.impl.common.entity.AbstractCompanion;
import io.github.jason13official.summons.impl.common.item.DevilShardItem;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/// Devil Shard drops on a kill the *active companion itself* lands (unlike Evo Crystal/Heart, which also credit the owner directly);
/// wiki: "I.D.s occasionally drop these," i.e. it's the companion's own drop during combat, not a generic
/// enemy drop. See DevilShardItem for the per-stack type/level/generation data.
@Mixin(LivingEntity.class)
public class LivingEntityDevilShardDropMixin {

  @Unique
  private static final float SUMMONS$DROP_CHANCE = 0.03F;

  @Inject(method = "die", at = @At("HEAD"))
  private void summons$die(DamageSource source, CallbackInfo ci) {
    LivingEntity self = (LivingEntity) (Object) this;
    if (!(self.level() instanceof ServerLevel level) || self instanceof AbstractCompanion) {
      return;
    }

    Entity killer = source.getEntity();
    if (!(killer instanceof AbstractCompanion companion) || level.random.nextFloat() >= SUMMONS$DROP_CHANCE) {
      return;
    }

    ItemStack stack = DevilShardItem.create(companion.getCompanionType(), companion.getLevel(), companion.getGeneration() + 1);
    level.addFreshEntity(new ItemEntity(level, self.getX(), self.getY(), self.getZ(), stack));
  }
}
