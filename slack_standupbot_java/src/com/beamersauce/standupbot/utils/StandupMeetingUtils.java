package com.beamersauce.standupbot.utils;

import java.time.Instant;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.beamersauce.standupbot.bot.ICommandManager;
import com.beamersauce.standupbot.bot.IDataManager;
import com.beamersauce.standupbot.bot.IRoom;
import com.beamersauce.standupbot.bot.IUser;
import com.beamersauce.standupbot.commands.BlacklistCommand;
import com.beamersauce.standupbot.commands.MeetingCommand.Meeting;

public class StandupMeetingUtils {
	private static final Logger logger = LogManager.getLogger();
	/**
	 * Returns back the standard warning message in the format:
	 * 
	 * @all Standup will be conducted in x minutes!
	 * Currently blacklist users are: []
	 * Currently early standup users are: {}
	 * 
	 * @return
	 */
	public static String getWarningMessage(final int minutes_until_standup, final List<String> blacklist_users, final Map<String, String> early_standup_users) {
		return new StringBuilder("@all Standup will be conducted in ")
			.append(minutes_until_standup)
			.append(" minutes!\nCurrently blacklist users are: ")
			.append(blacklist_users.toString())
			.append("\nCurrently early standup users are: ")
			.append(early_standup_users.toString())
			.toString();		
	}
	
	public static String getSummaryMessage(final IRoom room, final IDataManager data_manager) {
		Map<String, String> standup = getStandup(room, data_manager);
		final StringBuilder sb = new StringBuilder();
		sb.append("["+ room.name() +"]");
		standup.forEach((user, message) -> {
			sb.append("\n [" + user + "] " + message);
		});
			
		return sb.toString();
	}
	
	/**
	 * Sets a users standup message, if forcemark is true, always sets users standup
	 * if forcemark is false and user already exists, removes from standup.
	 * 
	 * @param room
	 * @param data_manager
	 * @param user_id
	 * @param message
	 * @param forceMark
	 * @return
	 */
	public static boolean markUserStandup(final IRoom room, final IDataManager data_manager, final IUser user, final Optional<String> message, boolean forceMark) {
		boolean markedUser = false;
		final Map<String, String> standup = getStandup(room, data_manager);
		if ( standup.containsKey(user.nickname()) && !forceMark) {
			//user already existed, and we want to flip them, just remove
			standup.remove(user.nickname());			
		} else {
			//otherwise just overwrite whatever was there (marking user as giving early standup)
			standup.put(user.nickname(), message.map(m->m).orElse(""));
			markedUser = true;
		}
		//save back the updated list
		final Map<String, Object> shared_room_data = data_manager.get_shared_room_data(room);
		shared_room_data.put("standup", standup);
		data_manager.set_shared_room_data(room, shared_room_data);	
		return markedUser;
	}
	
	/**
	 * Removes all users from the standup
	 * 
	 * @param room
	 * @param data_manager
	 * @return
	 */
	public static boolean clearStandup(final IRoom room, final IDataManager data_manager) {
		Map<String, Object> shared_room_data = data_manager.get_shared_room_data(room);
		if ( shared_room_data.containsKey("standup") ) {
			shared_room_data.remove("standup");
			data_manager.set_shared_room_data(room, shared_room_data);
			return true;
		}
		return false;
	}
	
	/**
	 * Returns the current list of standup users in the data entry
	 */
	@SuppressWarnings("unchecked")
	public static Map<String, String> getStandup(final IRoom room, final IDataManager data_manager) {
		Map<String, Object> shared_room_data = data_manager.get_shared_room_data(room);
		if ( shared_room_data.containsKey("standup") ) {
			//TODO fix this?
			return (Map<String, String>) shared_room_data.get("standup"); 			
		} else {
			return new HashMap<String, String>();
		}
	}
	
	/**
	 * Returns back the set of users who have not submitted an early standup or have been called on already
	 * 
	 * @return
	 */
	public static Set<IUser> getRemainingUsers(final IRoom room, final ICommandManager command_manager) {
		final Map<String, String> standup = getStandup(room, command_manager.getDataManager(room));
		logger.debug("Standup: " + standup.keySet());
		final Set<IUser> room_users = command_manager.getRoomUsers(room).stream().filter(u->!u.nickname().equals(UserUtils.getBotNickname())).collect(Collectors.toSet());
		logger.debug("Room: " + room_users.stream().map(u->u.nickname()).collect(Collectors.toList()));
		final Set<String> blacklist_user_ids = BlacklistCommand.getCurrentBlacklist(room, command_manager.getDataManager(room));
		logger.debug("Blacklist: " + blacklist_user_ids);
		//find the room_users who are not in blacklist_user_ids or standup
		final Set<String> excluded_users = Stream.concat(
				standup.keySet().stream().map(nickname->command_manager.findUser(nickname, null).get().id()), 
				blacklist_user_ids.stream()).distinct().collect(Collectors.toSet()
						);
		logger.debug("Excluded: " + excluded_users);
		return room_users.stream().filter(user -> !excluded_users.contains(user.id())).sorted(new Comparator<IUser>() {
			@Override
			public int compare(IUser o1, IUser o2) {
				return o1.id().compareTo(o2.id());
			}
		}).collect(Collectors.toSet());
				
	}
	
	public static Instant getNextStandupTime(final Meeting meeting) {			
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.SECOND, 0); 
		cal.set(Calendar.HOUR, meeting.hour_to_run%12); //TODO this is on 12h need to handle to get proper time
		if ( meeting.hour_to_run > 11)
		{
			cal.set(Calendar.AM_PM, Calendar.PM);
		} else {
			cal.set(Calendar.AM_PM, Calendar.AM);
		}
		cal.set(Calendar.MINUTE, meeting.minute_to_run);
		if ( cal.getTime().getTime() < System.currentTimeMillis() )
		{
			//add a day, we are past todays standup time
			cal.add(Calendar.DATE, 1);
		}
		if ( meeting.days_of_week_indexes.size() > 0 ) {
			while ( !meeting.days_of_week_indexes.contains(cal.get(Calendar.DAY_OF_WEEK)-1)) {
				//keep adding days until we get to one we can run
				cal.add(Calendar.DATE, 1);
			}
		}
		cal.set(Calendar.SECOND, 1); //schedule the meeting 1s forward, so that if it finishes in <1s it wont reschedule immediately
		Date date = cal.getTime();
		return date.toInstant();
	}	
}
