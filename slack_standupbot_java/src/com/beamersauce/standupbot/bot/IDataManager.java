package com.beamersauce.standupbot.bot;

import java.util.Map;

public interface IDataManager {
	//get/set global shared data
	public void set_global_shared_data(Map<String, Object> global_data);
	public Map<String, Object> get_global_shared_data();
	//get/set room shared data
	public void set_shared_room_data(IRoom room, Map<String, Object> shared_room_data);
	public Map<String, Object> get_shared_room_data(IRoom room);
	//get/set room command data
	public void set_command_data(ICommand command, IRoom room, Map<String, Object> data);
	public Object get_command_data(ICommand command, IRoom room); //if null return all room data
	
	/**
	 * Data is organized in a structure that allows 3 tiers of data storage/retrieval.
	 * 1. Global - application wide data, shared across all rooms e.g. global config settings?
	 * 2. Room - room wide data, shared across all commands in a room, only accessible to the room
	 * 3. Command - data storage for a command per room (e.g. the same command in 2 rooms will have 2 different data storages)
	 * {
	 * 		"global: {
	 * 			"xxx":{}
	 * 		},
	 * 		"rooms": {
	 * 			"room_a": {
	 * 				"_shared": {
	 * 					"xxx":{}
	 * 				},
	 * 				"_data": {
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
