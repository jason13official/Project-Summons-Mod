package io.github.jason13official.summons.impl.common.network;

import io.github.jason13official.summons.Summons;
import io.github.jason13official.summons.impl.client.gui.screen.ShardForgeScreen;
import io.github.jason13official.summons.impl.common.entity.AbstractCompanion;
import io.github.jason13official.summons.impl.common.item.DevilShardItem;
import io.github.jason13official.summons.impl.common.party.CompanionPartyManager;
import io.github.jason13official.summons.impl.common.party.CompanionShopRoster;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;

public class SummonsNetworking {

  public static void handle(ServerPlayer player, Action action) {
    switch (action) {
      case TOGGLE_SUMMON -> CompanionPartyManager.toggle(player);
      case CYCLE_PARTY -> CompanionPartyManager.cycle(player, 1);
      case MODE_UP -> withActive(player, companion -> companion.cycleMode(1));
      case MODE_DOWN -> withActive(player, companion -> companion.cycleMode(-1));
      case ABILITY_LEFT -> withActive(player, companion -> companion.cycleAbility(-1));
      case ABILITY_RIGHT -> withActive(player, companion -> companion.cycleAbility(1));
      case COMMAND -> withActive(player, AbstractCompanion::performCommandAbility);
    }
  }

  /// Evolution Chart screen "click a form to set it" -> only while the player is in creative; the debug command has its own, separate permission-2 gate. Silently no-ops on rejection/bad id,
  /// same as the rest of this class's handlers.
  public static void handleSetForm(ServerPlayer player, String formId) {
    if (!player.isCreative()) {
      return;
    }

    AbstractCompanion active = CompanionPartyManager.findActive(player.serverLevel(), player);
    if (active != null && active.debugSetEvolutionFormById(formId)) {
      player.displayClientMessage(Component.literal(
          active.getCompanionType().name() + "-Type set to " + active.getEvolutionForm().displayName()), false);
    }
  }

  /// ShardForgeScreen "forge" button -> consumes one Devil Shard from the given inventory slot and parks a new companion entry in the player's [CompanionShopRoster]. Re-validates the slot
  /// server-side rather than trusting the client's own read of its inventory.
  public static void handleForgeShard(ServerPlayer player, int inventorySlot) {
    ItemStack stack = player.getInventory().getItem(inventorySlot);
    var type = DevilShardItem.type(stack);
    if (type.isEmpty()) {
      return;
    }

    int parentLevel = DevilShardItem.parentLevel(stack);
    int generation = DevilShardItem.generation(stack);
    int seededLevel = Math.max(1, Math.round(parentLevel * 0.1F));

    stack.shrink(1);
    CompanionShopRoster.of(player).add(type.get(), seededLevel, generation);
    player.displayClientMessage(Component.literal(
        "Forged a new level-" + seededLevel + " " + type.get().name() + "-Type (Gen." + generation + ") - see it at the shop"), false);
  }

  /// ShardMerchant#mobInteract -> client opens the Forge screen
  public static void handleOpenShardForge() {
    Minecraft.getInstance().setScreen(new ShardForgeScreen());
  }

  /// client-side receivers for the three companion sync payloads; each applied to whichever companion entity matches the id in the client's own level (the normal tracked-entity mirror), if any
  public static void handleProgressSync(CompanionProgressSyncPayload payload) {
    if (findSyncTarget(payload.entityId()) instanceof AbstractCompanion companion) {
      companion.applyProgressSyncPayload(payload);
    }
  }

  public static void handleStateSync(CompanionStateSyncPayload payload) {
    if (findSyncTarget(payload.entityId()) instanceof AbstractCompanion companion) {
      companion.applyStateSyncPayload(payload);
    }
  }

  public static void handleIdentitySync(CompanionIdentitySyncPayload payload) {
    if (findSyncTarget(payload.entityId()) instanceof AbstractCompanion companion) {
      companion.applyIdentitySyncPayload(payload);
    }
  }

  private static Entity findSyncTarget(int entityId) {
    Minecraft mc = Minecraft.getInstance();
    return mc.level != null ? mc.level.getEntity(entityId) : null;
  }

  private static void withActive(ServerPlayer player, Consumer<AbstractCompanion> action) {
    AbstractCompanion active = CompanionPartyManager.findActive(player.serverLevel(), player);
    if (active != null) {
      action.accept(active);
    }
  }

  /// @see io.github.jason13official.summons.impl.client.SummonsKeyBindings
  public enum Action {
    TOGGLE_SUMMON,
    CYCLE_PARTY,
    MODE_UP,
    MODE_DOWN,
    ABILITY_LEFT,
    ABILITY_RIGHT,
    COMMAND
  }

  public record CompanionInputPayload(Action action) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<CompanionInputPayload> TYPE =
        new CustomPacketPayload.Type<>(Summons.identifier("companion_input"));

    public static final StreamCodec<FriendlyByteBuf, CompanionInputPayload> STREAM_CODEC = CustomPacketPayload.codec(
        (payload, buf) -> buf.writeEnum(payload.action()),
        buf -> new CompanionInputPayload(buf.readEnum(Action.class)));

    @Override
    public Type<? extends CustomPacketPayload> type() {

      return TYPE;
    }
  }

  /// @see io.github.jason13official.summons.impl.client.gui.screen.EvolutionChartScreen
  public record CompanionSetFormPayload(String formId) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<CompanionSetFormPayload> TYPE =
        new CustomPacketPayload.Type<>(Summons.identifier("companion_set_form"));

    public static final StreamCodec<FriendlyByteBuf, CompanionSetFormPayload> STREAM_CODEC = CustomPacketPayload.codec(
        (payload, buf) -> buf.writeUtf(payload.formId()),
        buf -> new CompanionSetFormPayload(buf.readUtf()));

    @Override
    public Type<? extends CustomPacketPayload> type() {

      return TYPE;
    }
  }

  /// @see io.github.jason13official.summons.impl.client.gui.screen.ShardForgeScreen
  public record ForgeShardPayload(int inventorySlot) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<ForgeShardPayload> TYPE =
        new CustomPacketPayload.Type<>(Summons.identifier("forge_shard"));

    public static final StreamCodec<FriendlyByteBuf, ForgeShardPayload> STREAM_CODEC = CustomPacketPayload.codec(
        (payload, buf) -> buf.writeVarInt(payload.inventorySlot()),
        buf -> new ForgeShardPayload(buf.readVarInt()));

    @Override
    public Type<? extends CustomPacketPayload> type() {
      return TYPE;
    }
  }

  /// server -> the interacting player only; @see io.github.jason13official.summons.impl.common.entity.misc.ShardMerchant
  public record OpenShardForgeScreenPayload() implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<OpenShardForgeScreenPayload> TYPE =
        new CustomPacketPayload.Type<>(Summons.identifier("open_shard_forge_screen"));

    public static final StreamCodec<FriendlyByteBuf, OpenShardForgeScreenPayload> STREAM_CODEC = CustomPacketPayload.codec(
        (payload, buf) -> { },
        buf -> new OpenShardForgeScreenPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
      return TYPE;
    }
  }
}
