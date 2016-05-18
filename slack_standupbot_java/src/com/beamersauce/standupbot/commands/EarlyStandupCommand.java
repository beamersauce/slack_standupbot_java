package com.beamersauce.standupbot.commands;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.beamersauce.standupbot.bot.ICommand;
import com.beamersauce.standupbot.bot.ICommandManager;
import com.beamersauce.standupbot.bot.IRoom;
import com.beamersauce.standupbot.bot.IUser;
import com.beamersauce.standupbot.utils.MessageUtils;
import com.beamersauce.standupbot.utils.StandupMeetingUtils;
import com.beamersauce.standupbot.utils.UserUtils;

public class EarlyStandupCommand implements ICommand {
	private static Logger logger = LogManager.getLogger();
//	private Set<String> early_words = new HashSet<String>();
	
	public EarlyStandupCommand() {
//		early_words.add("yesterday");
//		early_words.add("friday");
	}
	
	@Override
	public String id() {
		return "early_standup";
	}

	@Override
	public Optional<String> trigger_word() {
		return Optional.empty();
	}

	@Override
	public boolean enabled() {
		return true;
	}

	@Override
	public boolean receive_full_chat_stream() {
		return true;
	}

	@Override
	public void onNewMessage(ICommandManager command_manager, IRoom room,
			IUser user, String message) {
		logger.debug("early received a message: " + message);
		//split message, figure out if its in the form:
		//A. early standup {username}
		//B. early standup yesterday xxx today yyy
		//C. ignore anything else		
		if ( message.contains("early standup") ) {
			String sub_message = message.substring(message.indexOf("early standup") + ("early standup").length()).trim();
			if ( sub_message.charAt(0) == ':')
				sub_message = sub_message.substring(1).trim();
			logger.debug("EARLY: " + sub_message);
			//TODO change this to not be slack specific (e.g. hipchat users start with @)
			if ( sub_message.startsWith("<")) {
				final Optional<IUser> found_user = command_manager.findUser(null, UserUtils.convertSlackUserTagToID(sub_message));
				if ( found_user.isPresent() ) {
					//earlystandup this user
					final boolean wasEarlyStandup = StandupMeetingUtils.markUserStandup(room, command_manager.getDataManager(room), found_user.get(), Optional.empty(), false);
					if ( wasEarlyStandup )
						command_manager.sendMessage(room, "[" + found_user.get().nickname() + "] is now marked as giving early standup");
					else
						command_manager.sendMessage(room, "[" + found_user.get().nickname() + "] is no longer marked as giving early standup");
					
				} else {
					command_manager.sendMessage(room, "error, could not find user [" + sub_message + "]");
				}
			} else {
				//accept anything they write after, we don't care if it starts with "yesterday x today y"
				logger.debug("found early message match, marking this user as submitting early standup");
				StandupMeetingUtils.markUserStandup(room, command_manager.getDataManager(room), user, Optional.of(sub_message), true);
				command_manager.sendMessage(room, "[" + user.nickname() + "] is now marked as giving early standup");
			}	
		}
	}

//	private boolean matchesEarlyWord(String sub_message) {		
//		long matches = early_words.stream().mapToLong(word ->{
//			if ( sub_message.startsWith(word) ){
//				return 1;
//			}
//			return 0;
//		}).sum();
//		return matches > 0;		
//	}

	@Override
	public Optional<String> display_message(ICommandManager command_manager, IRoom room) {
		StringBuilder sb = new StringBuilder();
		sb.append("Users marked as early (skipping next standup): " + StandupMeetingUtils.getStandup(room, command_manager.getDataManager(room)).keySet().toString());
//		.append("\nEarly trigger words: " + early_words.toString());
		return Optional.of(sb.toString());
	}

	@Override
	public String help_message() {
		return MessageUtils.createDefaultCommandHelpMessage(trigger_word(), new String[0], "allows submitting your standup early via 'early standup yesterday {x} today {y}' or 'early standup {username}' to add/remove from early standup list");
	}
	
	/**
	 * Returns the current list of early standup users in the data entry
	 */
//	@SuppressWarnings("unchecked")
//	public static Map<String, String> getEarlyStandup(IRoom room, IDataManager data_manager) {
//		Map<String, Object> shared_room_data = data_manager.get_shared_room_data(room);
//		if ( shared_room_data.containsKey("early_standup") ) {
//			//TODO fix this?
//			return (Map<String, String>) shared_room_data.get("early_standup"); 			
//		} else {
//			return new HashMap<String, String>();
//		}
//	}
	
	/**
	 * Saves the user to the early standup data entry
	 * 
	 * @param user_id
	 * @return true if the user was set as giving early standup, false if the user was removed from early standup
	 */
//	private static boolean markUserAsEarlyStandup(final IRoom room, final IDataManager data_manager, final String user_id, final Optional<String> message) {
//		return markUserAsEarlyStandup(room, data_manager, user_id, message, false);
//	}
//	
//	private static boolean markUserAsEarlyStandup(final IRoom room, final IDataManager data_manager, final String user_id, final Optional<String> message, boolean forceMark) {
//		boolean markedAsEarly = false;
//		Map<String, String> early_standup = getEarlyStandup(room, data_manager);
//		if ( early_standup.containsKey(user_id) && !forceMark) {
//			//user already existed, and we want to flip them, just remove
//			early_standup.remove(user_id);			
//		} else {
//			//otherwise just overwrite whatever was there (marking user as giving early standup)
//			early_standup.put(user_id, message.map(m->m).orElse(""));
//			markedAsEarly = true;
//		}
//		//save back the updated list
//		final Map<String, Object> shared_room_data = data_manager.get_shared_room_data(room);
//		shared_room_data.put("early_standup", early_standup);
//		data_manager.set_shared_room_data(room, shared_room_data);	
//		return markedAsEarly;
//	}
//	
//	public static boolean clearEarlyStandup(final IRoom room, final IDataManager data_manager) {
//		Map<String, Object> shared_room_data = data_manager.get_shared_room_data(room);
//		if ( shared_room_data.containsKey("early_standup") ) {
//			shared_room_data.remove("early_standup");
//			return true;
//		}
//		return false;
//	}

	@Override
	public void initialize(ICommandManager command_manager, IRoom room) {
		// TODO Auto-generated method stub
		
	}

}
