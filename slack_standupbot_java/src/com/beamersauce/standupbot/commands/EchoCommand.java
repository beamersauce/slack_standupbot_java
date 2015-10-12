package com.beamersauce.standupbot.commands;

import java.util.Optional;

import com.beamersauce.standupbot.bot.ICommand;
import com.beamersauce.standupbot.bot.ICommandManager;
import com.beamersauce.standupbot.bot.IRoom;
import com.beamersauce.standupbot.bot.IUser;

public class EchoCommand implements ICommand {

	@Override
	public String id() {
		return "echo";
	}

	@Override
	public String trigger_word() {
		return "echo";
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
		String sub_message = message.substring(message.indexOf(this.trigger_word()) + this.trigger_word().length());
		command_manager.sendMessage(room, "["+user.nickname()+"] said: " + sub_message );
	}

	@Override
	public Optional<String> display_message() {
		return Optional.empty();
	}

	@Override
	public String help_message() {
		return "repeats anything you type back to you, for no reason";
	}

}
