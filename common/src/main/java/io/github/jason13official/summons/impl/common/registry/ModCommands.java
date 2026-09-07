package io.github.jason13official.summons.impl.common.registry;

import com.mojang.brigadier.CommandDispatcher;
import io.github.jason13official.summons.impl.common.command.debug.SummonsDebugCommand;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands.CommandSelection;

public class ModCommands {

  public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher, CommandBuildContext commandBuildContext, CommandSelection commandSelection) {

    SummonsDebugCommand.register(commandDispatcher);
  }
}
