package io.github.jason13official.summons.mixin;

import io.github.jason13official.summons.impl.common.entity.AbstractCompanion;
import io.github.jason13official.summons.impl.common.party.CompanionPartyManager;
import io.github.jason13official.summons.impl.common.registry.ModItems;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/// Heart drops on kill, same shape as Evo Crystals: a chance on any kill credited to a player or their owned companion, only while they have an active companion to receive one.
@Mixin(LivingEntity.class)
public class LivingEntityHeartDropMixin {

  @Unique
  private static final float SUMMONS$DROP_CHANCE = 0.15F;

  @Unique
  private static Player summons$resolveCreditedPlayer(Entity killer) {
    if (killer instanceof ServerPlayer player) {
      return player;
    }
    if (killer instanceof AbstractCompanion companion && companion.getOwner() instanceof ServerPlayer player) {
      return player;
    }
    return null;
  }

  @Inject(method = "die", at = @At("HEAD"))
  private void summons$die(DamageSource source, CallbackInfo ci) {
    LivingEntity self = (LivingEntity) (Object) this;
    if (!(self.level() instanceof ServerLevel level) || self instanceof AbstractCompanion
        || self instanceof WitherBoss || self instanceof EnderDragon) {
      return;
    }

    Entity killer = source.getEntity();
    Player player = summons$resolveCreditedPlayer(killer);
    if (player == null || level.random.nextFloat() >= SUMMONS$DROP_CHANCE) {
      return;
    }

    if (CompanionPartyManager.findActive(level, player) == null) {
      return;
    }

    ItemStack stack = new ItemStack(ModItems.HEART);
    level.addFreshEntity(new ItemEntity(level, self.getX(), self.getY(), self.getZ(), stack));
  }
}
