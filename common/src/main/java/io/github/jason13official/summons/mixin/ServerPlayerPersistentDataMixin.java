package io.github.jason13official.summons.mixin;

import io.github.jason13official.summons.api.common.util.SummonsDataHolder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/// player respawns/dimension changes transfer data atypically by swapping in a new server player instance,
/// and copying values. here we copy our companion party/other data (to-be-implemented)
@Mixin(ServerPlayer.class)
public class ServerPlayerPersistentDataMixin {

  @Inject(at = @At("TAIL"), method = "restoreFrom")
  private void summons$restoreFrom(ServerPlayer oldPlayerInstance, boolean keepEverything, CallbackInfo ci) {

    ServerPlayer newPlayerInstance = (ServerPlayer) (Object) this;

    SummonsDataHolder oldDataHolder = (SummonsDataHolder) oldPlayerInstance;
    SummonsDataHolder newDataHolder = (SummonsDataHolder) newPlayerInstance;

    CompoundTag oldPlayerData = oldDataHolder.summons$getPersistentData();
    newDataHolder.summons$setPersistentData(oldPlayerData);
  }
}
