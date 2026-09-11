package io.github.jason13official.summons.mixin;

import io.github.jason13official.summons.impl.common.entity.AbstractCompanion;
import io.github.jason13official.summons.impl.common.item.HeartItem;
import io.github.jason13official.summons.impl.common.party.CompanionPartyManager;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/// Touching a Heart instantly consumes it to heal/revive the player's active companion; it
/// never enters the inventory, active companion or not.
@Mixin(ItemEntity.class)
public class ItemEntityHeartPickupMixin {

  @Inject(method = "playerTouch", at = @At("HEAD"), cancellable = true)
  private void summons$playerTouch(Player player, CallbackInfo ci) {
    ItemEntity self = (ItemEntity) (Object) this;
    if (self.level().isClientSide || !(self.getItem().getItem() instanceof HeartItem)) {
      return;
    }

    AbstractCompanion active = CompanionPartyManager.findActive(self.level(), player);
    if (active != null) {
      active.consumeHeart(self.getItem().getCount());
    }

    self.discard();
    ci.cancel();
  }
}
