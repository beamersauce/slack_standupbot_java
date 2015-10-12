package com.beamersauce.standupbot.commands;

import java.util.Optional;

import com.beamersauce.standupbot.bot.ICommand;
import com.beamersauce.standupbot.bot.ICommandManager;
import com.beamersauce.standupbot.bot.IRoom;
import com.beamersauce.standupbot.bot.IUser;

public class DisplayCommand implements ICommand {

	private static final String id = "display";
	private boolean enabled = true;
	
	@Override
	public String id() {
		return id;
	}
	
	@Override
	public String trigger_word() {
		return "display";
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
	public Optional<String> display_message() {
		return Optional.empty();
	}

	@Override
	public String help_message() {
		return "displays all available commands";
	}
	
	@Override
	public void onNewMessage(ICommandManager command_manager, IRoom room, IUser user, String message) {
		//TODO read save file or something to see all commands and output their help
		String display_message = "DISPLAY:\n[meeting] - Next meeting is never";  
		command_manager.sendMessage(room, display_message);
	}
}
