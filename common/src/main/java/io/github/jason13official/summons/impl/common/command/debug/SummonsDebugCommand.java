package io.github.jason13official.summons.impl.common.command.debug;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.github.jason13official.summons.Constants;
import io.github.jason13official.summons.impl.common.entity.AbstractCompanion;
import io.github.jason13official.summons.impl.common.evolution.EvolutionForm;
import io.github.jason13official.summons.impl.common.gate.SummonGateManager;
import io.github.jason13official.summons.impl.common.item.DevilShardItem;
import io.github.jason13official.summons.impl.common.party.CompanionPartyManager;
import io.github.jason13official.summons.impl.common.party.CompanionType;
import java.util.concurrent.CompletableFuture;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

/// `/summons debug unlockall`; unlocks every [CompanionType] without a real Devil Shard item. `/summons debug addxp <amount>`; grants XP without dozens of manual ability uses.
/// `/summons debug setform <form>`; jumps the active companion straight to any form in its evolution chart, same permission-2 gate as the rest of this command (see also the Evolution Chart
/// screen's own creative-only "click a form" path in `SummonsNetworking#handleSetForm`). `/summons debug devilshard`; forces a Devil Shard drop for the active companion instead of waiting on
/// the 3% combat-kill chance (see LivingEntityDevilShardDropMixin).
public class SummonsDebugCommand {

  public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {

    // region root
    LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal(Constants.MOD_ID);
    root.requires(source -> source.hasPermission(2));
    // endregion root

    // region debug
    LiteralArgumentBuilder<CommandSourceStack> debug = Commands.literal("debug");

    LiteralArgumentBuilder<CommandSourceStack> unlockAll =
        Commands.literal("unlockall")
            .executes(SummonsDebugCommand::unlockAll);
    debug.then(unlockAll);

    LiteralArgumentBuilder<CommandSourceStack> addExperience =
        Commands.literal("addxp")
            .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                .executes(SummonsDebugCommand::addXp));
    debug.then(addExperience);

    LiteralArgumentBuilder<CommandSourceStack> gate =
        Commands.literal("gate")
            .executes(SummonsDebugCommand::gate);
    debug.then(gate);

    LiteralArgumentBuilder<CommandSourceStack> leaveGate =
        Commands.literal("leavegate")
            .executes(SummonsDebugCommand::leaveGate);
    debug.then(leaveGate);

    LiteralArgumentBuilder<CommandSourceStack> setForm =
        Commands.literal("setform")
            .then(Commands.argument("form", StringArgumentType.word())
                .suggests(SummonsDebugCommand::suggestForms)
                .executes(SummonsDebugCommand::setForm));
    debug.then(setForm);

    LiteralArgumentBuilder<CommandSourceStack> devilShard =
        Commands.literal("devilshard")
            .executes(SummonsDebugCommand::devilShard);
    debug.then(devilShard);
    root.then(debug);
    // endregion debug

    dispatcher.register(root);
  }

  private static int unlockAll(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {

    ServerPlayer player = ctx.getSource().getPlayerOrException();

    for (CompanionType type : CompanionType.values()) {
      CompanionPartyManager.unlock(player, type);
    }

    ctx.getSource().sendSuccess(() -> Component.literal("Unlocked all Innocent Devil types for " + player.getGameProfile().getName()), true);
    return 1;
  }

  private static int addXp(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {

    ServerPlayer player = ctx.getSource().getPlayerOrException();
    int amount = IntegerArgumentType.getInteger(ctx, "amount");

    AbstractCompanion active = CompanionPartyManager.findActive(player.serverLevel(), player);
    if (active == null) {
      ctx.getSource().sendFailure(Component.literal("No active companion to grant XP to"));
      return 0;
    }

    active.addExperience(amount);
    ctx.getSource().sendSuccess(() -> Component.literal(
        "Granted " + amount + " XP - now level " + active.getLevel() + " (" + active.getExperience() + "/"
            + AbstractCompanion.experienceToNextLevel(active.getLevel()) + ")"), true);
    return 1;
  }

  /// `/summons debug gate` -> jumps straight to the player's Summon Gate pocket room instead of hunting for a rare world-gen door
  private static int gate(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {

    ServerPlayer player = ctx.getSource().getPlayerOrException();
    SummonGateManager.enterGate(player);
    ctx.getSource().sendSuccess(() -> Component.literal("Sent " + player.getGameProfile().getName() + " to the Summon Gate"), true);
    return 1;
  }

  /// `/summons debug leavegate` -> manual escape hatch back to the stored return point
  private static int leaveGate(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {

    ServerPlayer player = ctx.getSource().getPlayerOrException();
    SummonGateManager.leaveGate(player);
    ctx.getSource().sendSuccess(() -> Component.literal("Pulled " + player.getGameProfile().getName() + " out of the Summon Gate"), true);
    return 1;
  }

  /// `/summons debug setform <form>` -> jumps the active companion straight to any form in its own evolution chart, skipping crystal-point costs
  private static int setForm(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {

    ServerPlayer player = ctx.getSource().getPlayerOrException();
    String formId = StringArgumentType.getString(ctx, "form");

    AbstractCompanion active = CompanionPartyManager.findActive(player.serverLevel(), player);
    if (active == null) {
      ctx.getSource().sendFailure(Component.literal("No active companion to change the form of"));
      return 0;
    }

    if (!active.debugSetEvolutionFormById(formId)) {
      ctx.getSource().sendFailure(Component.literal("'" + formId + "' isn't a valid form for " + active.getCompanionType().name() + "-Type"));
      return 0;
    }

    ctx.getSource().sendSuccess(() -> Component.literal(
        active.getCompanionType().name() + "-Type set to " + active.getEvolutionForm().displayName()), true);
    return 1;
  }

  /// `/summons debug devilshard` -> forces a Devil Shard for the active companion straight into the player's inventory, skipping the 3% per-kill chance
  private static int devilShard(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {

    ServerPlayer player = ctx.getSource().getPlayerOrException();

    AbstractCompanion active = CompanionPartyManager.findActive(player.serverLevel(), player);
    if (active == null) {
      ctx.getSource().sendFailure(Component.literal("No active companion to forge a Devil Shard from"));
      return 0;
    }

    ItemStack shard = DevilShardItem.create(active.getCompanionType(), active.getLevel(), active.getGeneration() + 1);
    if (!player.getInventory().add(shard)) {
      player.drop(shard, false);
    }

    ctx.getSource().sendSuccess(() -> Component.literal(
        "Gave a " + active.getCompanionType().name() + "-Type Devil Shard (Lv." + active.getLevel() + ", Gen." + (active.getGeneration() + 1) + ")"), true);
    return 1;
  }

  /// suggests every form id in the active companion's own chart; empty (not an error) if the source isn't a player or has no active companion
  private static CompletableFuture<Suggestions> suggestForms(CommandContext<CommandSourceStack> ctx, SuggestionsBuilder builder) {
    if (ctx.getSource().getEntity() instanceof ServerPlayer player) {
      AbstractCompanion active = CompanionPartyManager.findActive(player.serverLevel(), player);
      if (active != null) {
        for (EvolutionForm form : active.allForms()) {
          builder.suggest(form.id());
        }
      }
    }
    return builder.buildFuture();
  }
}
