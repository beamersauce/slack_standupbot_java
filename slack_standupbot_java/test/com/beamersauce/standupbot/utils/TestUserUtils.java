package com.beamersauce.standupbot.utils;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.junit.Test;

import com.beamersauce.standupbot.bot.ICommandManager;
import com.beamersauce.standupbot.bot.IUser;
import com.beamersauce.standupbot.bot.slack.DefaultUser;

/**
 * Test the {@link com.beamersauce.standupbot.utils.UserUtils} class.
 * @author Burch
 *
 */
public class TestUserUtils {

	@Test
	public void testConvertSlackUserTagToID() {
		final String user1 = "<@TEST>";
		final String user2 = "not_a_user";
		assertTrue("TEST".equals(UserUtils.convertSlackUserTagToID(user1)));
		assertNull(UserUtils.convertSlackUserTagToID(user2));
	}
	
	@Test
	public void testConvertUserIDToNickname() {
		final IUser user1 = new DefaultUser("123", "X", "Y");
		final IUser user2 = new DefaultUser("1234", "X", "Y");
		//setup mock command manager
		final ICommandManager command_manager = mock(ICommandManager.class);
		when(command_manager.findUser(null, user1.id())).thenReturn(Optional.of(user1));
		when(command_manager.findUser(null, user2.id())).thenReturn(Optional.empty());
		
		assertTrue("Y".equals(UserUtils.convertUserIDToNickname(user1.id(), command_manager, false)));		
		assertTrue("<@123>".equals(UserUtils.convertUserIDToNickname(user1.id(), command_manager, true)));
		assertTrue("Error finding user".equals(UserUtils.convertUserIDToNickname(user2.id(), command_manager, false)));
	}
	
	@Test
	public void testConvertSetOfUserIDToNicknames() {
		final IUser user1 = new DefaultUser("123", "X", "Y");
		final IUser user2 = new DefaultUser("1234", "X", "Y");
		final Set<String> user_ids = new HashSet<String>();
		user_ids.add(user1.id());
		user_ids.add(user2.id());
		//setup mock command manager
		final ICommandManager command_manager = mock(ICommandManager.class);
		when(command_manager.findUser(null, user1.id())).thenReturn(Optional.of(user1));
		when(command_manager.findUser(null, user2.id())).thenReturn(Optional.of(user2));		
		
		assertTrue(UserUtils.convertSetOfUserIDToNicknames(user_ids, command_manager, false).stream().allMatch(n->user1.nickname().equals(n) || user2.nickname().equals(n)));
		assertTrue(UserUtils.convertSetOfUserIDToNicknames(user_ids, command_manager, true).stream().allMatch(n->"<@123>".equals(n) || "<@1234>".equals(n)));
		
		
	}

	@Test
	public void testConvertMapOfUserIDToNicknames() {
		final IUser user1 = new DefaultUser("123", "X", "Y1");
		final IUser user2 = new DefaultUser("1234", "X", "Y2");
		final Map<String, String> user_ids = new HashMap<String, String>();
		user_ids.put(user1.id(), user1.nickname());
		user_ids.put(user2.id(), user2.nickname());
		final ICommandManager command_manager = mock(ICommandManager.class);
		when(command_manager.findUser(null, user1.id())).thenReturn(Optional.of(user1));
		when(command_manager.findUser(null, user2.id())).thenReturn(Optional.of(user2));
		
		assertTrue(UserUtils.convertMapOfUserIDToNicknames(user_ids, command_manager, false).keySet().stream().allMatch(n->user1.nickname().equals(n) || user2.nickname().equals(n)));
		assertTrue(UserUtils.convertMapOfUserIDToNicknames(user_ids, command_manager, true).keySet().stream().allMatch(n->"<@123>".equals(n) || "<@1234>".equals(n)));
	}
	
	@Test
	public void testGetBotNickname() {
		assertTrue("standupbot".equals(UserUtils.getBotNickname()));
	}
}
