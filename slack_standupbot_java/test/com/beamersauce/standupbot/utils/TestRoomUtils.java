package com.beamersauce.standupbot.utils;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

import com.beamersauce.standupbot.bot.IDataManager;
import com.beamersauce.standupbot.bot.IRoom;
import com.beamersauce.standupbot.bot.slack.DefaultRoom;

/**
 * Test the RoomUtils class.
 * @author Burch
 *
 */
public class TestRoomUtils {

	/**
	 * This test doesn't actually test much because I'm using mockito to
	 * prime the data_manager in both cases :/ This code just adds an object to data manager
	 * and gets it back so it's not actually doing much.
	 */
	@Test
	public void testGetPreviousAccessedRooms() {
		//setup data model mock
		final IDataManager data_manager = mock(IDataManager.class);
		when(data_manager.get_global_shared_data()).thenReturn(new HashMap<String, Object>());
		//when(data_manager.set_global_shared_data(global_data);)
		final IRoom room = new DefaultRoom("123", "room123");
		RoomUtils.addPreviouslyAccessRoom(room, data_manager);
		
		//test the room was added successfully
		final Set<String> rooms = RoomUtils.getPreviousAccessedRooms(data_manager);
		assertEquals(1, rooms.size());
		final Map<String, Object> room_map = new HashMap<String, Object>();
		room_map.put("_stored_room_ids", new String[]{"123"});
		when(data_manager.get_global_shared_data()).thenReturn(room_map);
		assertTrue(rooms.stream().findFirst().get().equals("123"));
	}

}
