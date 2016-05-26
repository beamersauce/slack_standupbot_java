package com.beamersauce.standupbot.utils;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.Test;

import com.beamersauce.standupbot.bot.ICommandManager;
import com.beamersauce.standupbot.bot.IDataManager;
import com.beamersauce.standupbot.bot.IRoom;
import com.beamersauce.standupbot.bot.IUser;
import com.beamersauce.standupbot.bot.slack.DefaultRoom;
import com.beamersauce.standupbot.bot.slack.DefaultUser;
import com.beamersauce.standupbot.commands.MeetingCommand.Meeting;
import com.beamersauce.standupbot.commands.MeetingCommand.MeetingType;

/**
 * Testing StandupMeetingUtils class.
 * @author Burch
 *
 */
public class TestStandupMeetingUtils {

	@Test
	public void testGetWarningMessage() {
		List<String> blacklist_users = new ArrayList<String>();
		blacklist_users.add("a");
		blacklist_users.add("b");
		Map<String,String> early_standup_users = new HashMap<String, String>();
		early_standup_users.put("c", "d");
		early_standup_users.put("e", "f");
		assertTrue("@all Standup will be conducted in 1 minutes!\nCurrently blacklist users are: [a, b]\nCurrently early standup users are: {c=d, e=f}".equals(StandupMeetingUtils.getWarningMessage(1, blacklist_users, early_standup_users)));
	}

	@Test
	public void testGetSummaryMessage() {
		final IRoom room = new DefaultRoom("123", "room123");
		final IDataManager data_manager = mock(IDataManager.class);
		final Map<String, Object> map = new HashMap<String, Object>();
		when(data_manager.get_shared_room_data(room)).thenReturn(map);
		//empty example
		assertTrue("[room123]".equals(StandupMeetingUtils.getSummaryMessage(room, data_manager)));
		
		//populated example
		final Map<String, String> result_map = new HashMap<String, String>();
		result_map.put("a", "b");
		map.put("standup", result_map);
		assertTrue("[room123]\n [a] b".equals(StandupMeetingUtils.getSummaryMessage(room, data_manager).trim()));
	}

	@Test
	public void testMarkUserStandup() {
		final IRoom room = new DefaultRoom("123", "room123");
		final IUser user = new DefaultUser("456", "user456", "nick456");
		final IDataManager data_manager = mock(IDataManager.class);
		final Map<String, Object> map = new HashMap<String, Object>();
		when(data_manager.get_shared_room_data(room)).thenReturn(map);
		
		//TODO check data_manager for values actual being added/removed
		
		//marks user as standup
		assertTrue(StandupMeetingUtils.markUserStandup(room, data_manager, user, Optional.empty(), false));
		//force mark user as standup
		assertTrue(StandupMeetingUtils.markUserStandup(room, data_manager, user, Optional.empty(), true));
		//flip user to not standup w/ message (ignored)
		assertFalse(StandupMeetingUtils.markUserStandup(room, data_manager, user, Optional.of("msg"), false));
		//force mark user as standup w/ message
		assertTrue(StandupMeetingUtils.markUserStandup(room, data_manager, user, Optional.of("msg"), true));
	}

	@Test
	public void testClearStandup() {
		final IRoom room = new DefaultRoom("123", "room123");
		final IDataManager data_manager = mock(IDataManager.class);
		final Map<String, Object> map = new HashMap<String, Object>();
		when(data_manager.get_shared_room_data(room)).thenReturn(map);
		
		//TODO check data_manager for values being cleared
		assertFalse(StandupMeetingUtils.clearStandup(room, data_manager));
		map.put("standup", new HashMap<String, String>());
		assertTrue(StandupMeetingUtils.clearStandup(room, data_manager));
	}

	@Test
	public void testGetStandup() {
		final IRoom room = new DefaultRoom("123", "room123");
		final IDataManager data_manager = mock(IDataManager.class);
		final Map<String, Object> map = new HashMap<String, Object>();
		when(data_manager.get_shared_room_data(room)).thenReturn(map);
		
		final Map<String, String> result_map = new HashMap<String, String>();
		result_map.put("a", "b");
		map.put("standup", result_map);
		final Map<String, String> standup_map = StandupMeetingUtils.getStandup(room, data_manager);
		assertTrue(standup_map.get("a").equals("b"));
		
	}

	@Test
	public void testGetRemainingUsers() {
		final IUser u = new DefaultUser("a", "b", "c");
		final IUser u1 = new DefaultUser("123", "user123", "nick123");
		final IUser u2 = new DefaultUser("456", "user456", "nick456");
		final IRoom room = new DefaultRoom("123", "room123");
		final ICommandManager command_manager = mock(ICommandManager.class);
		final IDataManager data_manager = mock(IDataManager.class);
		when(command_manager.getDataManager(room)).thenReturn(data_manager);
		final Map<String, Object> map = new HashMap<String, Object>();
		when(data_manager.get_shared_room_data(room)).thenReturn(map);
		
		final Map<String, String> result_map = new HashMap<String, String>();
		result_map.put("c", "val");
		map.put("standup", result_map);
		
		when(command_manager.findUser(u.nickname(), null)).thenReturn(Optional.of(u));
		
		assertTrue(StandupMeetingUtils.getRemainingUsers(room, command_manager).stream().allMatch(user->user.id().equals(u1.id()) || user.id().equals(u2.id())));
	}

	@Test
	public void testGetNextStandupTime() {
		final Meeting meeting = new Meeting("0123456", "1", "1", MeetingType.normal.toString());
		final Instant next_meeting_time = StandupMeetingUtils.getNextStandupTime(meeting);
		//next meeting should be scheduled for the next 1:01 AM
		assertTrue(next_meeting_time.isAfter(Instant.now()));
		assertTrue(next_meeting_time.isBefore(Instant.now().plus(24, ChronoUnit.HOURS)));
	}

}
