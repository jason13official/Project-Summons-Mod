package io.github.jason13official.summons.mixin;

import io.github.jason13official.summons.api.common.util.SummonsDataHolder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public class EntityPersistentDataMixin implements SummonsDataHolder {

  @Unique
  private static final String SUMMONS$PERSISTENT_DATA_TAG = "summons.persistent_data";

  @Unique
  private CompoundTag summons$persistentData;

  @Inject(at = @At("TAIL"), method = "load")
  private void summons$load(CompoundTag compound, CallbackInfo ci) {
    if (compound.contains(SUMMONS$PERSISTENT_DATA_TAG)) {
      this.summons$persistentData = compound.getCompound(SUMMONS$PERSISTENT_DATA_TAG);
    }
  }

  @Inject(at = @At("TAIL"), method = "saveWithoutId")
  private void summons$saveWithoutId(CompoundTag compound, CallbackInfoReturnable<CompoundTag> cir) {
    if (this.summons$persistentData != null) {
      compound.put(SUMMONS$PERSISTENT_DATA_TAG, this.summons$persistentData);
    }
  }

  @Override
  public CompoundTag summons$getPersistentData() {
    if (this.summons$persistentData == null) {
      this.summons$persistentData = new CompoundTag();
    }
    return this.summons$persistentData;
  }

  @Override
  public void summons$setPersistentData(CompoundTag compoundTag) {
    this.summons$persistentData = compoundTag;
  }
}
