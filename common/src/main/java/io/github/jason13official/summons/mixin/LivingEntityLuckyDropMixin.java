package io.github.jason13official.summons.mixin;

import io.github.jason13official.summons.impl.common.entity.AbstractCompanion;
import io.github.jason13official.summons.impl.common.party.CompanionPartyManager;
import java.util.List;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/// LCK owner buff: a % chance to duplicate one of a kill's vanilla drops, credited the same
/// way as Evo Crystals/Hearts. Injects at TAIL, after dropAllDeathLoot already spawned items.
@Mixin(LivingEntity.class)
public class LivingEntityLuckyDropMixin {

  @Inject(method = "die", at = @At("TAIL"))
  private void summons$dieLucky(DamageSource source, CallbackInfo ci) {
    LivingEntity self = (LivingEntity) (Object) this;
    if (!(self.level() instanceof ServerLevel level) || self instanceof AbstractCompanion) {
      return;
    }

    Player player = summons$resolveCreditedPlayer(source.getEntity());
    if (player == null) {
      return;
    }

    AbstractCompanion active = CompanionPartyManager.findActive(level, player);
    if (active == null) {
      return;
    }

    double luckPercent = active.ownerStatBonus().lck(active.getLevel());
    if (luckPercent <= 0.0 || level.random.nextDouble() * 100.0 >= luckPercent) {
      return;
    }

    AABB area = new AABB(self.getX() - 1, self.getY() - 1, self.getZ() - 1, self.getX() + 1, self.getY() + 2, self.getZ() + 1);
    List<ItemEntity> drops = level.getEntitiesOfClass(ItemEntity.class, area);
    if (drops.isEmpty()) {
      return;
    }

    ItemEntity original = drops.get(level.random.nextInt(drops.size()));
    level.addFreshEntity(new ItemEntity(level, original.getX(), original.getY(), original.getZ(), original.getItem().copy()));
  }

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
}
