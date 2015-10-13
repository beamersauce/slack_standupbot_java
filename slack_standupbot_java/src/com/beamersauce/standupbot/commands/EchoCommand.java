package com.beamersauce.standupbot.commands;

import java.util.Optional;

import com.beamersauce.standupbot.bot.ICommand;
import com.beamersauce.standupbot.bot.ICommandManager;
import com.beamersauce.standupbot.bot.IRoom;
import com.beamersauce.standupbot.bot.IUser;
import com.beamersauce.standupbot.utils.MessageUtils;

public class EchoCommand implements ICommand {

	long num_times_triggered = 0;
	
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
		num_times_triggered++;
		String sub_message = message.substring(message.indexOf(this.trigger_word()) + this.trigger_word().length());
		command_manager.sendMessage(room, "["+user.nickname()+"] said: " + sub_message );
	}

	@Override
	public Optional<String> display_message() {
		return Optional.of("triggered " + num_times_triggered + " times since last server restart");
	}

	@Override
	public String help_message() {
		return MessageUtils.createDefaultCommandHelpMessage(Optional.of(trigger_word()), new String[0], "repeats anything you type back to you");
	}

}
