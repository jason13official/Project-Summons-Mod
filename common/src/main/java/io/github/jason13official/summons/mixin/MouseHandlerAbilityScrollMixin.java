package io.github.jason13official.summons.mixin;

import io.github.jason13official.summons.impl.client.SummonsKeyBindings;
import io.github.jason13official.summons.impl.common.network.SummonsNetworking.Action;
import io.github.jason13official.summons.platform.Services;
import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/// Holding the ability-scroll modifier key steals the scroll wheel from vanilla's hotbar switch and cycles the active companion's ability instead.
@Mixin(MouseHandler.class)
public class MouseHandlerAbilityScrollMixin {

  @Shadow
  private net.minecraft.client.Minecraft minecraft;

  @Inject(method = "onScroll", at = @At("HEAD"), cancellable = true)
  private void summons$onScroll(long windowPointer, double xOffset, double yOffset, CallbackInfo ci) {
    if (this.minecraft.screen != null || this.minecraft.player == null || this.minecraft.getOverlay() != null) {
      return;
    }
    if (!SummonsKeyBindings.ABILITY_SCROLL_MODIFIER.isDown()) {
      return;
    }

    double delta = yOffset != 0 ? yOffset : xOffset;
    if (delta == 0) {
      return;
    }

    // Services.network().sendCompanionInput(delta > 0 ? Action.ABILITY_RIGHT : Action.ABILITY_LEFT);

    // invert to match vanilla hotbar directionality
    Services.network().sendCompanionInput(delta > 0 ? Action.ABILITY_LEFT : Action.ABILITY_RIGHT);

    ci.cancel();
  }
}
