package io.github.jason13official.summons.impl.common.command.debug;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.jason13official.summons.Constants;
import io.github.jason13official.summons.impl.common.party.CompanionPartyManager;
import io.github.jason13official.summons.impl.common.party.CompanionType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/// `/summons debug unlockall` - unlocks every [CompanionType] for the executing player,
/// so the party keybinds/HUD can be exercised without a real Devil Shard-equivalent item yet.
public class SummonsDebugCommand {

  public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {

    dispatcher.register(Commands.literal(Constants.MOD_ID).requires(source -> source.hasPermission(2))
        .then(Commands.literal("debug")
            .then(Commands.literal("unlockall").executes(SummonsDebugCommand::unlockAll))));
  }

  private static int unlockAll(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {

    ServerPlayer player = ctx.getSource().getPlayerOrException();

    for (CompanionType type : CompanionType.values()) {
      CompanionPartyManager.unlock(player, type);
    }

    ctx.getSource().sendSuccess(() -> Component.literal("Unlocked all Innocent Devil types for " + player.getGameProfile().getName()), true);
    return 1;
  }
}
