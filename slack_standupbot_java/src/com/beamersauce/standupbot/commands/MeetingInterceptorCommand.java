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
	public void initialize(ICommandManager command_manager, IRoom room) {
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
	
	public static void addMeetingActor(final ICommandManager command_manager, final IRoom room, final Meeting meeting, Optional<Boolean> silentStartup) {
		if (!meeting_actors.containsKey(room.id())) {
			logger.debug("Added meeting actor for room: " + room.name());
			meeting_actors.put(room.id(), new MeetingActor(command_manager, room));			
		}
		logger.debug("changed meeting time, sent to actor");
		meeting_actors.get(room.id()).changeMeetingTime(meeting, silentStartup);
	}
	
	public static Instant getMeetingTime(final IRoom room) {
		if (!meeting_actors.containsKey(room.id())) {
			return null; //or something no meeting scheduled here		
		}
		
		return meeting_actors.get(room.id()).getNextMeetingTime();
	}
	
	public static Meeting getMeeting(final IRoom room) {
		if (!meeting_actors.containsKey(room.id())) {
			return null; //or something no meeting scheduled here		
		}
		
		return meeting_actors.get(room.id()).getMeeting();
	}
	
	public static void removeMeetingActor(final IRoom room) {		
		meeting_actors.remove(room.id());		
	}

}
