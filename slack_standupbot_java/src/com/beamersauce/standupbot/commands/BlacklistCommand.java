package com.beamersauce.standupbot.commands;

import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.beamersauce.standupbot.bot.ICommand;
import com.beamersauce.standupbot.bot.ICommandManager;
import com.beamersauce.standupbot.bot.IDataManager;
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
				final boolean wasBlacklisted = blacklistUser(room, command_manager.getDataManager(room), found_user.get().id());
				if ( wasBlacklisted )
					command_manager.sendMessage(room, "[" + user_name + "] is now blacklisted");
				else
					command_manager.sendMessage(room, "[" + user_name + "] is no longer blacklisted");
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
	public Optional<String> display_message(ICommandManager command_manager, IRoom room) {
		final Set<String> blacklist_ids = getCurrentBlacklist(room, command_manager.getDataManager(room));
		final Set<String> blacklist_names = blacklist_ids.stream().map(id -> {
			return command_manager.findUser(null, id).get().nickname();
		}).collect(Collectors.toSet());
		return Optional.of("Blacklisted users are: [" + blacklist_names.toString() + "]");
	}

	@Override
	public String help_message() {
		return MessageUtils.createDefaultCommandHelpMessage(Optional.of(trigger_word()), new String[]{"user_name"}, "disables or enables a user for participation in standup");
	}
	
	/**
	 * Returns the current list of blacklist users in the data entry
	 */
	@SuppressWarnings("unchecked")
	private Set<String> getCurrentBlacklist(IRoom room, IDataManager data_manager) {
		Map<String, Object> shared_room_data = data_manager.get_shared_room_data(room);
		if ( shared_room_data.containsKey("blacklist") ) {
			//TODO fix this?
			return (Set<String>) shared_room_data.get("blacklist");
		} else {
			return new HashSet<String>();
		}
	}
	
	/**
	 * Saves the user to the blacklist data entry
	 * 
	 * @param user_id
	 * @return true if the user was blacklisted, false if the user was enabled
	 */
	private boolean blacklistUser(final IRoom room, final IDataManager data_manager, final String user_id) {
		Set<String> blacklist_users = getCurrentBlacklist(room, data_manager);
		//loop through removing the user if they are already a member of blacklisted set
		if ( blacklist_users.stream().anyMatch(b -> b.equals(user_id)) ) {
			blacklist_users.remove(user_id);
			return false;
		} else {
			blacklist_users.add(user_id); //TODO this breaks final, need to create a second list with extra user
			final Map<String, Object> shared_room_data = data_manager.get_shared_room_data(room);
			shared_room_data.put("blacklist", blacklist_users);
			data_manager.set_shared_room_data(room, shared_room_data);
			return true;
		}
	}

}
