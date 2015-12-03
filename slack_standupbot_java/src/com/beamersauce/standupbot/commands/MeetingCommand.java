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
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.beamersauce.standupbot.bot.ICommand;
import com.beamersauce.standupbot.bot.ICommandManager;
import com.beamersauce.standupbot.bot.IRoom;
import com.beamersauce.standupbot.bot.IUser;
import com.beamersauce.standupbot.commands.meeting.MeetingActor;
import com.beamersauce.standupbot.utils.StandupMeetingUtils;
import com.beamersauce.standupbot.utils.UserUtils;

/**
 * Handles scheduling a meeting, puts meeting information into DB for other
 * commands to read.
 * 
 * @author Burch
 *
 */
public class MeetingCommand implements ICommand {
	
	private static final Logger _logger = LogManager.getLogger();
	private Timer timer_standup = new Timer();
	private Timer timer_warning = new Timer();
	private Optional<Instant> next_meeting_time = Optional.empty();
	
	public MeetingCommand() {
		//TODO on startup, turn on necessary meeting actors that were previously setup
	}
	
	@Override
	public String id() {
		return "meeting";
	}

	@Override
	public Optional<String> trigger_word() {
		return Optional.of("meeting");
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
		String[] args = message.split("\\s+");		
		if ( args.length < 6 ) {
			command_manager.sendMessage(room, "Wrong number of arguments for command [meeting] expects [meeting] [days_of_week] [hour] [minute] [regular|summary]");
		} else {
			//parse the pieces up
			//0 == standup
			//1 == meeting
			//2 == days_of_week 0123456
			//3 == hour [0-23]
			//4 == minute [0-59]
			//5 == regular|summary
			final Meeting meeting = new Meeting(args[2], args[3], args[4], args[5]);
			MeetingInterceptorCommand.addMeetingActor(command_manager, room, meeting);			
			
			
//			next_meeting_time = Optional.of(setupNextMeeting(command_manager, room, meeting));
//			command_manager.sendMessage(room, "Next meeting scheduled for: " + new Date(next_meeting_time.get().toEpochMilli()).toString());
		}
		//TODO add a way to turn off meetings
	}

	@Override
	public Optional<String> display_message(ICommandManager command_manager, IRoom room) {		
		//TODO fix this
		return Optional.of("[meeting] - TODO FIX THIS Meetings are scheduled MTWRF at 9:30. This is a summary|normal room.  Next meeting is " + next_meeting_time.map(d -> d.toString()).orElse("never"));  		
	}

	@Override
	public String help_message() {
		return "allows scheduling of standup meetings";
	}

	public static class Meeting {
		public Set<Integer> days_of_week_indexes = new HashSet<Integer>();
		public int hour_to_run;
		public int minute_to_run;
		public MeetingType meeting_type;
		
		public Meeting(String days_of_week, String hour, String minute, String meeting_type) {
			for ( char day : days_of_week.toCharArray() ) {
				try {
					int day_of_week = Integer.parseInt(day + "");
					days_of_week_indexes.add(day_of_week);
				} catch (Exception ex) {
					//TODO don't ignore, fail
					//ignore bad days
					_logger.error("bad day of week param: " + day);
				}
			}
			try
			{
				int houri = Integer.parseInt(hour);
				if ( houri >= 0 && houri <= 23)
					hour_to_run = houri;
			} catch (Exception ex) {
				//TODO don't ignore, fail
				//just ignore bad things				
			}
			try
			{
				int minutei = Integer.parseInt(minute);
				if ( minutei >= 0 && minutei <= 59)
					minute_to_run = minutei;
			} catch (Exception ex) {
				//just ignore bad things
				//TODO don't ignore, fail
			}
		}
		
		@Override
		public boolean equals(Object obj) {
			if ( !(obj instanceof Meeting) )
				return false;
			Meeting other = (Meeting)obj;
			if ( 	other.days_of_week_indexes.containsAll(this.days_of_week_indexes) &&
					other.hour_to_run == this.hour_to_run &&
					other.minute_to_run == this.minute_to_run &&
					other.meeting_type == this.meeting_type )
				return true;
			return false;
		}
	}
	
	public enum MeetingType {
		summary, normal
	}
	
	private Instant setupNextMeeting(final ICommandManager command_manager, final IRoom room, final Meeting meeting) {
		//TODO figure out when next meeting should be, create a java timer (or is there something better)
		//to run the meeting function at that point
		final Instant nextStandupTime = MeetingCommand.getNextStandupTime(meeting);
		//set up a scheduler to run the next standup at this time		
		timer_standup.cancel(); //cancel any previously running timers
		timer_standup = new Timer();
		timer_standup.schedule(new StandupTask(command_manager, room), new Date(nextStandupTime.toEpochMilli()));
		//TODO set up a scheduler to run the warning if its set at x min before the next meeting
		setWarningTimer(command_manager, room, nextStandupTime);
		
		return nextStandupTime;
	}
	
	private void setWarningTimer(ICommandManager command_manager, IRoom room,
			Instant nextStandupTime) {
		timer_warning.cancel();
		timer_warning = new Timer();
		//check if we have a warning
		final Optional<Integer> warning_min = WarningCommand.getWarningMinutes(room, command_manager.getDataManager(room));
		if ( warning_min.isPresent() ) {
			timer_warning.schedule(new WarningTask(command_manager, room, warning_min.get()), new Date(nextStandupTime.minus(warning_min.get(), ChronoUnit.MINUTES).toEpochMilli()));
		}
		
	}

	//TODO change this to use instant if we are capable of that?  See what the latest standard is
	private static Instant getNextStandupTime(final Meeting meeting) {			
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
		if ( cal.getTime().getTime() < new Date().getTime() )
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
		Date date = cal.getTime();
		return date.toInstant();
	}	
	
	private class StandupTask extends TimerTask {
		private final ICommandManager command_manager;
		private final IRoom room;
		
		public StandupTask(final ICommandManager command_manager, final IRoom room) {
			this.command_manager = command_manager;
			this.room = room;
		}
		
		@Override
		public void run() {
			//TODO time for standup
			command_manager.sendMessage(room, "Time for standup, TODO (have not programmed active standup, reporting summary)");
			
			//TODO display summary
			command_manager.sendMessage(room, StandupMeetingUtils.getSummaryMessage(room, command_manager.getDataManager(room)));// EarlyStandupCommand.getEarlyStandup(room, command_manager.getDataManager(room))));						
			
			//TODO reschedule next standup when done, clear early standup with it
			command_manager.sendMessage(room, "Schedule next standup, TODO");
		}		
	}
	
	private class WarningTask extends TimerTask {
		private final ICommandManager command_manager;
		private final IRoom room;
		private final Integer minutes;
		
		public WarningTask(final ICommandManager command_manager, final IRoom room, final Integer minutes) {
			this.command_manager = command_manager;
			this.room = room;
			this.minutes = minutes;
		}
		
		@Override
		public void run() {
			final Set<String> blacklist_users = UserUtils.convertSetOfUserIDToNicknames(new HashSet<String>(BlacklistCommand.getCurrentBlacklist(room, command_manager.getDataManager(room))), command_manager, true);
			final Map<String, String> early_standup_users = UserUtils.convertMapOfUserIDToNicknames(StandupMeetingUtils.getStandup(room, command_manager.getDataManager(room)), command_manager, true);
			//TODO convert these 2 lists to have the tagged nicknames
			command_manager.sendMessage(room, StandupMeetingUtils.getWarningMessage(minutes, blacklist_users.stream().collect(Collectors.toList()), early_standup_users));			
		}		
	}

	@Override
	public void intialize(ICommandManager command_manager, IRoom room) {
		// TODO Auto-generated method stub
		
	}

}
