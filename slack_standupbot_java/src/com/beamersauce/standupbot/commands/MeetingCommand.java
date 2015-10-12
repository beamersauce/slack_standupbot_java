package com.beamersauce.standupbot.commands;

import java.util.Date;
import java.util.Optional;

import com.beamersauce.standupbot.bot.ICommand;
import com.beamersauce.standupbot.bot.ICommandManager;
import com.beamersauce.standupbot.bot.IRoom;
import com.beamersauce.standupbot.bot.IUser;

public class MeetingCommand implements ICommand {

	@Override
	public String id() {
		return "meeting";
	}

	@Override
	public String trigger_word() {
		return "meeting";
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
			
		}
		
	}

	@Override
	public Optional<String> display_message() {
		final Optional<Date> next_meeting_time = getNextMeetingTime();
		//TODO fix this
		return Optional.of("[meeting] - Meetings are scheduled MTWRF at 9:30. This is a summary|normal room.  Next meeting is " + next_meeting_time.map(d -> d.toString()).orElse("never"));  		
	}

	private Optional<Date> getNextMeetingTime() {
		//TODO
		return Optional.empty();
	}

	@Override
	public String help_message() {
		return "allows scheduling of standup meetings";
	}

	public static class MeetingArguments {
		public String days_of_week;
		public String hour;
		public String minute;
		public MeetingType meeting_type;
		
		public MeetingArguments(String days_of_week, String hour, String minute, String meeting_type) {
			//TODO
		}
	}
	
	public enum MeetingType {
		summary, normal
	}
	
	private void setupNextMeeting() {
		//TODO figure out when next meeting should be, create a java timer (or is there something better)
		//to run the meeting function at that point
		
	}
}
