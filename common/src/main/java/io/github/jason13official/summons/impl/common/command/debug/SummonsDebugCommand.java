package io.github.jason13official.summons.impl.common.command.debug;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.jason13official.summons.Constants;
import io.github.jason13official.summons.impl.common.entity.AbstractCompanion;
import io.github.jason13official.summons.impl.common.party.CompanionPartyManager;
import io.github.jason13official.summons.impl.common.party.CompanionType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/// - `/summons debug unlockall` -> unlocks every [CompanionType] for the executing player,
/// so the party keybinds/HUD can be exercised without a real Devil Shard-equivalent item yet.
/// - `/summons debug addxp <amount>` -> grants XP to the active companion, so leveling/ability
/// level-gates can be exercised without dozens of manual ability uses.
public class SummonsDebugCommand {

  public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {

//    dispatcher.register(Commands.literal(Constants.MOD_ID).requires(source -> source.hasPermission(2))
//        .then(Commands.literal("debug")
//            .then(Commands.literal("unlockall").executes(SummonsDebugCommand::unlockAll))
//            .then(Commands.literal("addxp")
//                .then(Commands.argument("amount", IntegerArgumentType.integer(1))
//                    .executes(SummonsDebugCommand::addXp)))));

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
}
