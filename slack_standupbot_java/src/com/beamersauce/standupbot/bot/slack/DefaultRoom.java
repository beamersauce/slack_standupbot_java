package com.beamersauce.standupbot.bot.slack;

import com.beamersauce.standupbot.bot.IRoom;

/**
 * Basic implementation for an IRoom, just stores the id and name.
 * @author Burch
 *
 */
public class DefaultRoom implements IRoom {
	private final String id;
	private final String name;	
	
	public DefaultRoom(String room_id, String room_name) {
		id = room_id;
		name = room_name;
	}

	@Override
	public String id() {
		return id;
	}
	
	@Override
	public String name() {
		return name;
	}
}
