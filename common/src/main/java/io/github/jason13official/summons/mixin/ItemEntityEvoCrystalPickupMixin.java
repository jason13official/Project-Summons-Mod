package io.github.jason13official.summons.mixin;

import io.github.jason13official.summons.impl.common.entity.AbstractCompanion;
import io.github.jason13official.summons.impl.common.item.EvoCrystalItem;
import io.github.jason13official.summons.impl.common.party.CompanionPartyManager;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/// Touching an Evo Crystal credits it straight to the player's active companion instead of
/// going to their inventory; falls through to a normal pickup if nothing is summoned.
@Mixin(ItemEntity.class)
public class ItemEntityEvoCrystalPickupMixin {

  @Inject(method = "playerTouch", at = @At("HEAD"), cancellable = true)
  private void summons$playerTouch(Player player, CallbackInfo ci) {
    ItemEntity self = (ItemEntity) (Object) this;
    if (self.level().isClientSide || !(self.getItem().getItem() instanceof EvoCrystalItem crystal)) {
      return;
    }

    AbstractCompanion active = CompanionPartyManager.findActive(self.level(), player);
    if (active == null) {
      self.discard();
      ci.cancel();
      return;
    }

    active.addCrystalPoints(crystal.color(), self.getItem().getCount());
    self.discard();
    ci.cancel();
  }
}
