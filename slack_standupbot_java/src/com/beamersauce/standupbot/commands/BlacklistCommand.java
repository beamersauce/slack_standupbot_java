package com.beamersauce.standupbot.commands;

import java.util.Optional;

import com.beamersauce.standupbot.bot.ICommand;
import com.beamersauce.standupbot.bot.ICommandManager;
import com.beamersauce.standupbot.bot.IRoom;
import com.beamersauce.standupbot.bot.IUser;
import com.beamersauce.standupbot.utils.MessageUtils;
import com.beamersauce.standupbot.utils.UserUtils;

public class BlacklistCommand implements ICommand {
	
	@Override
	public String id() {
		return "blacklist";
	}

	@Override
	public String trigger_word() {
		return "blacklist";
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
		final String[] splits = message.split("\\s+");
		if ( splits.length > 2 ) {
			final String user_name = splits[2];
			final Optional<IUser> found_user = command_manager.findUser(null, UserUtils.convertSlackUserTagToID(user_name));
			if ( found_user.isPresent()) {
				//TODO blacklist this user, or re-enable them - change output message to display what happened
				command_manager.sendMessage(room, "[" + user_name + "] is now blacklisted");
			} else {
				command_manager.sendMessage(room, "error, could not find user [" + user_name + "]");
			}
		} else {
			command_manager.sendMessage(room, "error, inproperly formated: " + help_message());
		}
		String sub_message = message.substring(message.indexOf(this.trigger_word()) + this.trigger_word().length());
		command_manager.sendMessage(room, "["+user.nickname()+"] said: " + sub_message );
	}

	@Override
	public Optional<String> display_message() {
		//TODO get blacklisted users
		return Optional.of("Blacklisted users are: [" + "TODO" + "]");
	}

	@Override
	public String help_message() {
		return MessageUtils.createDefaultCommandHelpMessage(Optional.of(trigger_word()), new String[]{"user_name"}, "disables or enables a user for participation in standup");
	}

}
