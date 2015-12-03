package com.beamersauce.standupbot.commands;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.beamersauce.standupbot.bot.ICommand;
import com.beamersauce.standupbot.bot.ICommandManager;
import com.beamersauce.standupbot.bot.IDataManager;
import com.beamersauce.standupbot.bot.IRoom;
import com.beamersauce.standupbot.bot.IUser;
import com.beamersauce.standupbot.commands.MeetingCommand.Meeting;
import com.beamersauce.standupbot.commands.meeting.MeetingActor;
import com.beamersauce.standupbot.utils.StandupMeetingUtils;
import com.beamersauce.standupbot.utils.UserUtils;

/**
 * Handles running a meeting, runs it at the time found in the db submitted by the "meeting" command.
 * 
 * @author Burch
 *
 */
public class MeetingInterceptorCommand implements ICommand {	
	private static final Logger logger = LogManager.getLogger();
	private Timer timer_standup = new Timer();
	private Timer timer_warning = new Timer();
	private Optional<Instant> next_meeting_time = Optional.empty();
	private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

	private static Map<String, MeetingActor> meeting_actors = new HashMap<String, MeetingActor>();
	
	@Override
	public void intialize(ICommandManager command_manager, IRoom room) {
		//setup meeting check event
//		scheduler.scheduleAtFixedRate(new ScheduledMeetingCheckerRunnable(this, command_manager, room), 5, 5, TimeUnit.SECONDS);
		
		//TODO setup existing meeting actors
	}
	
	@Override
	public String id() {
		return "meeting";
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
		//pass messages on to meeting actors if they exist					
		if ( meeting_actors.containsKey(room.id())) {
			meeting_actors.get(room.id()).onMessage(message, user);
		}
	}

	@Override
	public Optional<String> display_message(ICommandManager command_manager, IRoom room) {		
		return Optional.empty();  		
	}

	@Override
	public String help_message() {
		return null;
	}
	
	public static void addMeetingActor(final ICommandManager command_manager, final IRoom room, final Meeting meeting) {
		if (!meeting_actors.containsKey(room.id())) {
			logger.debug("Added meeting actor for room: " + room.name());
			meeting_actors.put(room.id(), new MeetingActor(command_manager, room));			
		}
		logger.debug("changed meeting time, sent to actor");
		meeting_actors.get(room.id()).changeMeetingTime(meeting);
	}
	
	public static void removeMeetingActor(final IRoom room) {		
		meeting_actors.remove(room.id());		
	}

	//TODO probably get rid of this
//	public static class ScheduledMeetingCheckerRunnable implements Runnable {
//		private final MeetingInterceptorCommand interceptor;
//		private final ICommandManager command_manager;
//		private final IRoom room;
//		private Meeting last_known_meeting = null;		
//		
//		public ScheduledMeetingCheckerRunnable(final MeetingInterceptorCommand intercepter, final ICommandManager command_manager, IRoom room) {
//			this.interceptor = intercepter;
//			this.command_manager = command_manager;
//			this.room = room;
//		}
//		
//		@Override
//		public void run() {
//			_logger.debug("Checking if meeting changed in room: " + room.name());
//			//checks the DB for a new meeting time
//			if ( command_manager.getDataManager(room).get_shared_room_data(room).containsKey("meeting") ) {
//				final Meeting meeting = (Meeting) command_manager.getDataManager(room).get_shared_room_data(room).get("meeting");
//				//see if this meeting is different from our last
//				if ( last_known_meeting == null ) {
//					last_known_meeting = meeting;
//					interceptor.setupNextMeeting(command_manager, room, last_known_meeting);
//				} else if ( !meeting.equals(last_known_meeting) ) {
//					//something changed, reschedule
//					last_known_meeting = meeting;
//					interceptor.setupNextMeeting(command_manager, room, last_known_meeting);
//				}
//			} else if ( last_known_meeting != null ) {
//				//meeting no longer exists, cancel an existing meeting if one is scheduled				
//				last_known_meeting = null;
//				interceptor.setupNextMeeting(command_manager, room, last_known_meeting);				
//			}
//		}
//		
//	}	
//	
//	public Instant setupNextMeeting(final ICommandManager command_manager, final IRoom room, final Meeting meeting) {
//		//cancel any previously running timers
//		timer_standup.cancel(); 
//		timer_standup = new Timer();
//		timer_warning.cancel();
//		timer_warning = new Timer();
//		if ( meeting != null ) {
//			//figure out when next meeting should be
//			final Instant nextStandupTime = MeetingInterceptorCommand.getNextStandupTime(meeting);
//			//set up a scheduler to run the next standup at this time					
//			timer_standup.schedule(new StandupTask(this, command_manager, room), new Date(nextStandupTime.toEpochMilli()));
//			//set up a scheduler to run the warning if its set at x min before the next meeting
//			setWarningTimer(command_manager, room, nextStandupTime);			
//			return nextStandupTime;
//		}
//		return null;
//	}
//	
//	private void setWarningTimer(ICommandManager command_manager, IRoom room, Instant nextStandupTime) {		
//		//check if we have a warning
//		final Optional<Integer> warning_min = WarningCommand.getWarningMinutes(room, command_manager.getDataManager(room));
//		if ( warning_min.isPresent() ) {
//			timer_warning.schedule(new WarningTask(command_manager, room, warning_min.get()), new Date(nextStandupTime.minus(warning_min.get(), ChronoUnit.MINUTES).toEpochMilli()));
//		}		
//	}
//
//	//TODO change this to use instant if we are capable of that?  See what the latest standard is
//	private static Instant getNextStandupTime(final Meeting meeting) {			
//		Calendar cal = Calendar.getInstance();
//		cal.set(Calendar.SECOND, 0);
//		cal.set(Calendar.HOUR, meeting.hour_to_run%12); //TODO this is on 12h need to handle to get proper time
//		if ( meeting.hour_to_run > 11)
//		{
//			cal.set(Calendar.AM_PM, Calendar.PM);
//		} else {
//			cal.set(Calendar.AM_PM, Calendar.AM);
//		}
//		cal.set(Calendar.MINUTE, meeting.minute_to_run);
//		if ( cal.getTime().getTime() < new Date().getTime() )
//		{
//			//add a day, we are past todays standup time
//			cal.add(Calendar.DATE, 1);
//		}
//		if ( meeting.days_of_week_indexes.size() > 0 ) {
//			while ( !meeting.days_of_week_indexes.contains(cal.get(Calendar.DAY_OF_WEEK)-1)) {
//				//keep adding days until we get to one we can run
//				cal.add(Calendar.DATE, 1);
//			}
//		}
//		Date date = cal.getTime();
//		return date.toInstant();
//	}	
//	
//	private class StandupTask extends TimerTask {
//		private final MeetingInterceptorCommand interceptor;
//		private final ICommandManager command_manager;
//		private final IRoom room;
//		
//		public StandupTask(final MeetingInterceptorCommand interceptor, final ICommandManager command_manager, final IRoom room) {
//			this.interceptor = interceptor;
//			this.command_manager = command_manager;
//			this.room = room;
//		}
//		
//		@Override
//		public void run() {
//			//TODO time for standup
//			command_manager.sendMessage(room, "Time for standup, TODO");			
//			//TODO reschedule next standup when done
//			//set things in interceptor to handle triggers?
//		}		
//	}
//	
//	private class WarningTask extends TimerTask {
//		private final ICommandManager command_manager;
//		private final IRoom room;
//		private final Integer minutes;
//		
//		public WarningTask(final ICommandManager command_manager, final IRoom room, final Integer minutes) {
//			this.command_manager = command_manager;
//			this.room = room;
//			this.minutes = minutes;
//		}
//		
//		@Override
//		public void run() {
//			final Set<String> blacklist_users = UserUtils.convertSetOfUserIDToNicknames(new HashSet<String>(BlacklistCommand.getCurrentBlacklist(room, command_manager.getDataManager(room))), command_manager, true);
//			final Map<String, String> early_standup_users = UserUtils.convertMapOfUserIDToNicknames(EarlyStandupCommand.getEarlyStandup(room, command_manager.getDataManager(room)), command_manager, true);
//			//TODO convert these 2 lists to have the tagged nicknames
//			command_manager.sendMessage(room, StandupMeetingUtils.getWarningMessage(minutes, blacklist_users.stream().collect(Collectors.toList()), early_standup_users));			
//		}		
//	}

}
