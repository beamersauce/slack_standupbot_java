package com.beamersauce.standupbot.utils;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.beamersauce.standupbot.bot.IDataManager;
import com.beamersauce.standupbot.bot.IRoom;

public class RoomUtils {
	private static final String STORED_ROOM_IDS = "_stored_room_ids";
	
	@SuppressWarnings("unchecked")
	public static Set<String> getPreviousAccessedRooms(final IDataManager data_manager) {
		final Map<String, Object> global_data = data_manager.get_global_shared_data();
		if ( global_data.containsKey(STORED_ROOM_IDS) )
			return ((Collection<String>) global_data.get(STORED_ROOM_IDS)).stream().collect(Collectors.toSet());
		return new HashSet<String>();
	}

	@SuppressWarnings("unchecked")
	public static void addPreviouslyAccessRoom(final IRoom room, final IDataManager data_manager) {
		final Map<String, Object> global_data = data_manager.get_global_shared_data();
		Set<String> previous_rooms = new HashSet<String>(); 
		if ( global_data.containsKey(STORED_ROOM_IDS) ) {
			previous_rooms = ((Collection<String>) global_data.get(STORED_ROOM_IDS)).stream().collect(Collectors.toSet());
		}
		
		previous_rooms.add(room.id());
		global_data.put(STORED_ROOM_IDS, previous_rooms);
		data_manager.set_global_shared_data(global_data);
	}
}
