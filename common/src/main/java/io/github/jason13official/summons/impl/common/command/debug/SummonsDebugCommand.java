package io.github.jason13official.summons.impl.common.command.debug;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.jason13official.summons.Constants;
import io.github.jason13official.summons.impl.common.entity.AbstractCompanion;
import io.github.jason13official.summons.impl.common.gate.SummonGateManager;
import io.github.jason13official.summons.impl.common.party.CompanionPartyManager;
import io.github.jason13official.summons.impl.common.party.CompanionType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/// `/summons debug unlockall`; unlocks every [CompanionType] without a real Devil Shard item.
/// `/summons debug addxp <amount>`; grants XP without dozens of manual ability uses.
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

  /// `/summons debug gate` -> jumps straight to the player's Summon Gate pocket room instead of
  /// hunting for a rare world-gen door
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
}
