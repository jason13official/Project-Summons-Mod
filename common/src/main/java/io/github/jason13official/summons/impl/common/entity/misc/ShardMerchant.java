package io.github.jason13official.summons.impl.common.entity.misc;

import io.github.jason13official.summons.impl.common.network.SummonsNetworking.OpenShardForgeScreenPayload;
import io.github.jason13official.summons.platform.Services;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/// Julia-esque shopkeeper NPC: opens the Devil Shard forge menu on interact ([ShardForgeScreen]/[io.github.jason13official.summons.impl.common.party.CompanionShopRoster]), otherwise entirely
/// passive -> no combat AI, no trading (vanilla Villager trades produce items, not party-data changes, so this isn't a Villager subclass). Placeholder model/texture, same "no assets yet" state as
/// most of this mod. Spawn-egg only for now, no natural spawn/structure.
public class ShardMerchant extends PathfinderMob {

  public ShardMerchant(EntityType<? extends ShardMerchant> entityType, Level level) {
    super(entityType, level);
    this.setPersistenceRequired();
  }

  public static AttributeSupplier.Builder createAttributes() {
    return Mob.createMobAttributes()
        .add(Attributes.MAX_HEALTH, 20.0)
        .add(Attributes.MOVEMENT_SPEED, 0.3)
        .add(Attributes.FOLLOW_RANGE, 16.0);
  }

  @Override
  protected void registerGoals() {
    this.goalSelector.addGoal(0, new FloatGoal(this));
    this.goalSelector.addGoal(1, new WaterAvoidingRandomStrollGoal(this, 0.6));
    this.goalSelector.addGoal(2, new LookAtPlayerGoal(this, Player.class, 6.0F));
    this.goalSelector.addGoal(3, new RandomLookAroundGoal(this));
  }

  @Override
  protected InteractionResult mobInteract(Player player, InteractionHand hand) {
    if (!this.level().isClientSide && player instanceof ServerPlayer serverPlayer) {
      Services.serverNetwork().sendToPlayer(serverPlayer, new OpenShardForgeScreenPayload());
      return InteractionResult.CONSUME;
    }

    return InteractionResult.SUCCESS;
  }

  // region sound
  @Override
  protected SoundEvent getAmbientSound() {
    return SoundEvents.VILLAGER_AMBIENT;
  }

  @Override
  protected SoundEvent getHurtSound(DamageSource damageSource) {
    return SoundEvents.VILLAGER_HURT;
  }

  @Override
  protected SoundEvent getDeathSound() {
    return SoundEvents.VILLAGER_DEATH;
  }
  // endregion sound
}
