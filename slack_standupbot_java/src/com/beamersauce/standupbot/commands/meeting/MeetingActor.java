package com.beamersauce.standupbot.commands.meeting;

import java.time.Instant;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.Date;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.beamersauce.standupbot.bot.ICommandManager;
import com.beamersauce.standupbot.bot.IRoom;
import com.beamersauce.standupbot.bot.IUser;
import com.beamersauce.standupbot.commands.WarningCommand;
import com.beamersauce.standupbot.commands.MeetingCommand.Meeting;
import com.beamersauce.standupbot.utils.StandupMeetingUtils;

/**
 * Created per room running a meeting, this actor handles running the meetings at
 * the correct time, parsing the messages, and spitting out the summary.
 * 
 * @author Burch
 *
 */
public class MeetingActor {
	private static final Logger logger = LogManager.getLogger();
	private final ICommandManager command_manager;
	private final IRoom room;
	private boolean isMeetingTime = false;
	private IUser currentUsersTurn = null;
	private StringBuilder current_turn = null;
	private Meeting meeting = null;
	private Instant next_meeting = null;
	private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
	private final ScheduledExecutorService scheduler_warning = Executors.newScheduledThreadPool(1);
	private final ScheduledExecutorService user_timeout = Executors.newScheduledThreadPool(1);	
	private ScheduledFuture<?> next_scheduled_task = null;
	private ScheduledFuture<?> next_warning_task = null;
	private ScheduledFuture<?> next_timeout_task = null;
	
	public MeetingActor(ICommandManager command_manager, IRoom room) {
		//save the room and data manager
		this.command_manager = command_manager;
		this.room = room;
	}
	
	/**
	 * Kill any active meeting, turn off all timers so this class can be destroyed.
	 */
	public void stop() {
		if ( next_scheduled_task != null ) {
			next_scheduled_task.cancel(true);
			next_meeting = null;
		}
		if ( next_warning_task != null ) {
			next_warning_task.cancel(true);
			next_warning_task = null;
		}
	}
	
	/**
	 * Called everytime a message is received, handles checking if it is currently a meeting time and parsing the message
	 * 
	 * @param message
	 * @param user
	 */
	public void onMessage(final String message, final IUser user) {		
		logger.debug("MeetingActor received message, is meeting time: " + isMeetingTime + " : " + message);
		//if a meeting is currently running, check if its the given users turn parse message, save to status
		if ( isMeetingTime ) {
			if ( currentUsersTurn == null ) {
				command_manager.sendMessage(room, "Meeting Error: no user is currently selected to take a turn");
			} else if ( user.id().equals(currentUsersTurn.id()) ) {
				//correct user spoke, record
				current_turn.append(" ").append(message);
				if ( containsDone(current_turn.toString()) ) {
					//end this user, get next user
					getNextUser(true, false);
				}
			} else {
				//wrong user spoke, reprimand
				command_manager.sendMessage(room, "<@" + user.nickname() + "> spoke out of turn, tisk tisk");
			}
		}
	}
	
	private void getNextUser(final boolean prevUserIsDone, final boolean prevUserTimedOut) {
		//stop timers
		if ( next_timeout_task != null )
			next_timeout_task.cancel(true);
		if ( prevUserIsDone ) {
			//send thank you
			command_manager.sendMessage(room, "<@" + currentUsersTurn.nickname() + "> done, thanks!");
			//save message to standup
			StandupMeetingUtils.markUserStandup(room, command_manager.getDataManager(room), currentUsersTurn, Optional.of(current_turn.toString()), true);
		} else if ( prevUserTimedOut ) {
			command_manager.sendMessage(room, "<@" + currentUsersTurn.nickname() + "> must be sleepy, timed out!");
			StandupMeetingUtils.markUserStandup(room, command_manager.getDataManager(room), currentUsersTurn, Optional.of("(timed out)"), true);			
		}
		current_turn = new StringBuilder();
		//change currentUsersTurn = nextUser or null if done
		final Set<IUser> remaining_users = StandupMeetingUtils.getRemainingUsers(room, command_manager);
		command_manager.sendMessage(room, "Possible users are: " + remaining_users.stream().map(u->u.nickname()).collect(Collectors.toList()));
		//TODO put this back in, overriding to set to null to skip meeting
//		currentUsersTurn = null;
		currentUsersTurn = remaining_users.size() > 0 ? remaining_users.stream().findFirst().get() : null;
		if ( currentUsersTurn == null ) {
			//meeting is over now, clea everything we no longer need
			isMeetingTime = false;
			next_timeout_task = null;
			command_manager.sendMessage(room, StandupMeetingUtils.getSummaryMessage(room, command_manager.getDataManager(room)));
			scheduleNextMeeting();
		} else {
			//start timers
			command_manager.sendMessage(room, "Your turn <@" + currentUsersTurn.nickname() + ">");
			next_timeout_task = user_timeout.schedule(new TimeoutRunnable(this), 40, TimeUnit.SECONDS); //TODO don't hardcode 40s, set to poll timer
		}
	}

	public void startMeeting() {		
		command_manager.sendMessage(room, "<@channel> Time for standup!");
		command_manager.sendMessage(room, "Use 'today' in a sentence or end your statement with ['done', 'done.'] to end your turn.");
		getNextUser(false, false);
		isMeetingTime = true;
	}

	final Pattern done_pattern = Pattern.compile("today|(done?.$)", Pattern.CASE_INSENSITIVE);
	/**
	 * Returns true if message contains meeting terminating phrase (aka "done")
	 * @param message
	 * @return
	 */
	private boolean containsDone(String message) {
		final String lowercase = message.toLowerCase().trim();
		return done_pattern.matcher(lowercase).find();
	}

	/**
	 * Changes when this meeting should be run, updates the internal timers and such.
	 * 
	 * If called during an active meeting will....???
	 * 
	 * @param meeting
	 */
	public void changeMeetingTime(final Meeting meeting) {
		this.meeting = meeting;
		scheduleNextMeeting();		
	}

	private void scheduleNextMeeting() {
		stop();
		StandupMeetingUtils.clearStandup(room, command_manager.getDataManager(room));
		this.next_meeting = StandupMeetingUtils.getNextStandupTime(meeting);
		logger.debug("Next meeting time should be: " + this.next_meeting.toString());
		//TODO schedule warning time?
		//setup timer
		long delay = this.next_meeting.getEpochSecond() - Instant.now().getEpochSecond();
		this.next_scheduled_task = scheduler.schedule(new MeetingRunnable(this), delay, TimeUnit.SECONDS);
		Optional<Integer> warning_minutes = WarningCommand.getWarningMinutes(room, command_manager.getDataManager(room));
		if ( warning_minutes.isPresent() ) {
			long warning_delay = delay - warning_minutes.get()*60;
			this.next_warning_task = scheduler_warning.schedule(new WarningRunnable(room, command_manager), warning_delay, TimeUnit.SECONDS);
		}
		logger.debug("scheduled next meeting");
		//send message to room
		command_manager.sendMessage(room, "Next meeting scheduled for: " + next_meeting);
	}
	
	private class MeetingRunnable implements Runnable {
		private final MeetingActor meeting_actor;
		public MeetingRunnable(final MeetingActor meeting_actor) {
			this.meeting_actor = meeting_actor;
		}
		@Override
		public void run() {
			//meeting needs to occur now, tell MeetingActor to start a meeting
			meeting_actor.startMeeting();
		}		
	}
	
	private class WarningRunnable implements Runnable {
		private final ICommandManager command_manager;
		private final IRoom room;
		
		public WarningRunnable(final IRoom room, final ICommandManager command_manager) {
			this.command_manager = command_manager;
			this.room = room;
		}
		@Override
		public void run() {
			//warning triggered, send message
			command_manager.sendMessage(room, WarningCommand.getWarningMessage(room, command_manager));
			
		}		
	}	
	
	private class TimeoutRunnable implements Runnable {
		private final MeetingActor meeting_actor;
		public TimeoutRunnable(final MeetingActor meeting_actor) {
			this.meeting_actor = meeting_actor;
		}
		@Override
		public void run() {
			//meeting needs to occur now, tell MeetingActor to start a meeting
			meeting_actor.getNextUser(false, true);
		}
	}
}
