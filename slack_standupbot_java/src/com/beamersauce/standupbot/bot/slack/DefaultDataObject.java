package com.beamersauce.standupbot.bot.slack;

import java.util.HashMap;
import java.util.Map;

public class DefaultDataObject {
	public Map<String, Object> _global = new HashMap<String, Object>();
	public Map<String, DefaultRoomData> _rooms = new HashMap<String, DefaultRoomData>();
	
	public static class DefaultRoomData {
		public Map<String, Object> _shared = new HashMap<String, Object>();
		public Map<String, Object> _commands = new HashMap<String, Object>();
	}
	
	/**
	 * Data is organized in a structure that allows 3 tiers of data storage/retrieval.
	 * 1. Global - application wide data, shared across all rooms e.g. global config settings?
	 * 2. Room - room wide data, shared across all commands in a room, only accessible to the room
	 * 3. Command - data storage for a command per room (e.g. the same command in 2 rooms will have 2 different data storages)
	 * {
	 * 		"_global: {
	 * 			"xxx":{}
	 * 		},
	 * 		"_rooms": {
	 * 			"room_a": {
	 * 				"_shared": {
	 * 					"xxx":{}
	 * 				},
	 * 				"_commands": {
	 * 					"meeting": {
	 * 						"days_of_week":"12345",
	 * 						"time_of_day":"9:30am"
	 * 					}
	 * 				}
	 * 			}
	 * 		}
	 * }
	 */
}
