package io.github.jason13official.summons.impl.common.entity;

import io.github.jason13official.summons.impl.common.party.CompanionMode;
import io.github.jason13official.summons.impl.common.party.CompanionParty;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

/// "sparking wisp": at 0 Hearts the I.D. goes inert instead of dying, until a Heart revives it. isWisp()/DATA_WISP_ID stays on AbstractCompanion (thin SynchedEntityData wrapper); this is the
/// die/revive/heart-consume/hover-movement logic around it.
final class CompanionWisp {

  private static final float REVIVE_HEALTH = 1.0F;
  private static final float HEART_HEAL_AMOUNT = 4.0F;

  private CompanionWisp() {
  }

  /// intercepts a lethal hit; I.D.s go inert instead of dying. Only the dismiss keybind fully removes one; a bypass-invulnerability source snapshots it back as a wisp instead
  static void die(AbstractCompanion companion, DamageSource source) {
    if (companion.level().isClientSide || companion.isWisp()) {
      companion.callSuperDie(source);
      return;
    }

    if (source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
      retreatToPartyAsWisp(companion);
      return;
    }

    companion.setWisp(true);
    companion.setHealth(REVIVE_HEALTH);
    companion.setNoAi(true);
  }

  /// stores the companion back in its party slot like the dismiss keybind, pre-marked as a wisp; falls back to a real discard if there's no owner left to return it to
  private static void retreatToPartyAsWisp(AbstractCompanion companion) {
    if (!(companion.getOwner() instanceof ServerPlayer player)) {
      companion.callSuperDie(companion.damageSources().generic());
      return;
    }

    companion.setWisp(true);

    CompanionParty party = CompanionParty.of(player);
    party.putSnapshot(companion.getCompanionType(), companion.saveWithoutId(new CompoundTag()));
    party.clearActiveType();
    companion.discard();
  }

  static void revive(AbstractCompanion companion) {
    if (!companion.isWisp()) {
      return;
    }

    companion.setWisp(false);
    companion.setNoAi(companion.getMode() == CompanionMode.DEFEND);
  }

  static void consumeHeart(AbstractCompanion companion) {
    if (companion.isWisp()) {
      revive(companion);
    }
    companion.heal(HEART_HEAL_AMOUNT);
  }

  /// the only "visual" a sparking wisp has -> a few sparkles drifting around its position
  static void spawnParticles(AbstractCompanion companion) {
    double x = companion.getX() + (companion.getRandom().nextDouble() - 0.5) * 0.6;
    double y = companion.getY() + companion.getRandom().nextDouble() * companion.getBbHeight();
    double z = companion.getZ() + (companion.getRandom().nextDouble() - 0.5) * 0.6;
    companion.level().addParticle(ParticleTypes.SOUL, x, y, z, 0.0, 0.02, 0.0);
  }

  /// noAi skips goal-based movement, so a wisp needs hand-rolled hover; eases toward the owner's head, no gravity/collision. AbstractCompanion#teleportToOwner covers big jumps
  static void floatTowardOwner(AbstractCompanion companion) {
    LivingEntity owner = companion.getOwner();
    if (owner == null) {
      return;
    }

    Vec3 target = new Vec3(owner.getX(), owner.getY() + owner.getEyeHeight() + 0.5, owner.getZ());
    Vec3 delta = target.subtract(companion.position());
    if (delta.lengthSqr() < 0.04) {
      return;
    }

    Vec3 step = delta.scale(0.08);
    companion.setPos(companion.getX() + step.x, companion.getY() + step.y, companion.getZ() + step.z);
  }
}
