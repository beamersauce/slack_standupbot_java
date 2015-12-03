package com.beamersauce.standupbot.commands;

import java.util.Comparator;
import java.util.Optional;
import java.util.Set;

import com.beamersauce.standupbot.bot.ICommand;
import com.beamersauce.standupbot.bot.ICommandManager;
import com.beamersauce.standupbot.bot.IRoom;
import com.beamersauce.standupbot.bot.IUser;
import com.beamersauce.standupbot.utils.MessageUtils;

public class DisplayCommand implements ICommand {

	private static final String id = "display";
	private boolean enabled = true;
	
	@Override
	public String id() {
		return id;
	}
	
	@Override
	public Optional<String> trigger_word() {
		return Optional.of("display");
	}

	@Override
	public boolean enabled() {
		//TODO read this from the save file
		return enabled;
	}

	@Override
	public boolean receive_full_chat_stream() {
		return false;
	}

	@Override
	public Optional<String> display_message(ICommandManager command_manager, IRoom room) {
		return Optional.empty();
	}

	@Override
	public String help_message() {
		return MessageUtils.createDefaultCommandHelpMessage(trigger_word(), new String[0], "displays status of all enabled commands");
	}
	
	@Override
	public void onNewMessage(ICommandManager command_manager, IRoom room, IUser user, String message) {
		//get all other commands and generate their help message
		final Set<ICommand> all_commands = command_manager.getRoomCommands(room);
		final StringBuilder sb = new StringBuilder("Standup Display");
		Comparator<ICommand> comparator_id = (c1, c2) -> c1.id().compareTo(c2.id()); 
		all_commands.stream().sorted(comparator_id).forEachOrdered(command -> {
			if ( command.display_message(command_manager, room).isPresent()) {
				sb.append(System.lineSeparator()).append("[").append(command.id()).append("] - ");			
				sb.append(command.display_message(command_manager, room).get());							
			}
		});
		//output message
		command_manager.sendMessage(room, sb.toString());
	}

	@Override
	public void intialize(ICommandManager command_manager, IRoom room) {
		// TODO Auto-generated method stub
		
	}
}
