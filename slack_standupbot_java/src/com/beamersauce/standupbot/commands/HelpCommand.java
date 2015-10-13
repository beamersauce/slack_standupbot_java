package com.beamersauce.standupbot.commands;

import java.util.Comparator;
import java.util.Optional;
import java.util.Set;

import com.beamersauce.standupbot.bot.ICommand;
import com.beamersauce.standupbot.bot.ICommandManager;
import com.beamersauce.standupbot.bot.IRoom;
import com.beamersauce.standupbot.bot.IUser;
import com.beamersauce.standupbot.utils.MessageUtils;

public class HelpCommand implements ICommand {

	@Override
	public String id() {
		return "help";
	}

	@Override
	public String trigger_word() {
		return "help";
	}

	@Override
	public boolean enabled() {
		return true;
	}

	@Override
	public boolean receive_full_chat_stream() {
		return false;
	}

	@Override
	public void onNewMessage(ICommandManager command_manager, IRoom room,
			IUser user, String message) {
		//get all other commands and generate their help message
		//TODO sort commands by command.id()
		final Set<ICommand> all_commands = command_manager.getRoomCommands(room);
		final StringBuilder sb = new StringBuilder("Standup Help");
		Comparator<ICommand> comparator_id = (c1, c2) -> c1.id().compareTo(c2.id()); 
		all_commands.stream().sorted(comparator_id).forEachOrdered(command -> {
			sb.append(System.lineSeparator()).append("[").append(command.id()).append("] - ");
			final String help_message = command.help_message();
			if ( help_message != null ) {				
				sb.append(help_message);
			}
		});
		//output message
		command_manager.sendMessage(room, sb.toString());
	}

	@Override
	public Optional<String> display_message() {
		return Optional.empty();
	}

	@Override
	public String help_message() {
		return MessageUtils.createDefaultCommandHelpMessage(Optional.of(trigger_word()), new String[0], "displays all commands help page");
	}

}
