package com.beamersauce.standupbot.commands;

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
import com.beamersauce.standupbot.utils.StandupMeetingUtils;
import com.beamersauce.standupbot.utils.UserUtils;

public class WarningCommand implements ICommand {
	
	@Override
	public String id() {
		return "warning";
	}

	@Override
	public Optional<String> trigger_word() {
		return Optional.of("warning");
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
			try {
				final Integer minutes = Integer.parseInt(splits[2]);
				WarningCommand.setWarningMinutes(room, command_manager.getDataManager(room), Optional.of(minutes));
				command_manager.sendMessage(room, "Set warning to " + minutes + " minutes before standup");
			} catch (Exception ex) {
				command_manager.sendMessage(room, "Error: " + splits[2] + " is not a valid number.");
			}					
		} else {
			command_manager.sendMessage(room, "error, inproperly formated: " + help_message());
		}		
	}

	@Override
	public Optional<String> display_message(ICommandManager command_manager, IRoom room) {
		final Optional<Integer> warning_mins = WarningCommand.getWarningMinutes(room, command_manager.getDataManager(room));
		if ( warning_mins.isPresent() ) 
			return Optional.of("Warning set to warn users:  " + warning_mins.get() + " minutes before standup");
		else
			return Optional.of("Warning not set, will not warn users of impending standup");
	}

	@Override
	public String help_message() {
		return MessageUtils.createDefaultCommandHelpMessage(trigger_word(), new String[]{"minutes"}, "set the number of minutes before a standup to warn users, submit 0 to disable");
	}
	
	/**
	 * Returns the current list of blacklist users in the data entry
	 */
	public static Optional<Integer> getWarningMinutes(IRoom room, IDataManager data_manager) {
		Map<String, Object> shared_room_data = data_manager.get_shared_room_data(room);
		if ( shared_room_data.containsKey("warning") ) {
			return Optional.of((Integer) shared_room_data.get("warning"));
		} else {
			return Optional.empty();
		}
	}
	
	/**
	 * Saves the user to the blacklist data entry
	 * 
	 * @param user_id
	 * @return true if the user was blacklisted, false if the user was enabled
	 */
	private static void setWarningMinutes(final IRoom room, final IDataManager data_manager, final Optional<Integer> minutes) {				
		final Map<String, Object> shared_room_data = data_manager.get_shared_room_data(room);
		if ( minutes.isPresent() ) {
			if ( minutes.get() > 0 )
				shared_room_data.put("warning", minutes.get());
			else
				shared_room_data.remove("warning");
		} else {
			shared_room_data.remove("warning");
		}
		data_manager.set_shared_room_data(room, shared_room_data);
	}

	@Override
	public void initialize(ICommandManager command_manager, IRoom room) {
		// TODO Auto-generated method stub
		
	}

	public static String getWarningMessage(final IRoom room, final ICommandManager command_manager) {
		final IDataManager data_manager = command_manager.getDataManager(room);
		Integer warning_minutes = getWarningMinutes(room, data_manager).get();
		Set<String> blacklist_users = BlacklistCommand.getCurrentBlacklist(room, data_manager);
		Map<String,String> standup = StandupMeetingUtils.getStandup(room, data_manager);
		final StringBuilder sb = new StringBuilder();
		sb.append("<@channel> Standup will be conducted in ").append(warning_minutes).append(" minutes!")
		.append("\nCurrent blacklist users are: ").append(blacklist_users.stream().map(id->UserUtils.convertUserIDToNickname(id, command_manager, false)).collect(Collectors.toList()))
		.append("\nCurrent early standup users are: ").append(standup);
		return sb.toString();
	}

}
