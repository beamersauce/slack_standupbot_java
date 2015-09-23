package com.beamersauce.standupbot.bot;

public interface IChatClient {
	public boolean joinRoom(IRoom room);
	public boolean leaveRoom(IRoom room);
	public void sendMessage(IRoom room, String message);
	public IUser getUser(String user_id, String user_name, String user_nickname);
	public IRoom getRoom(String room_id, String room_name);
	/**
	 * Creates a string that will call out a user in a channel if necessary
	 * 
	 * @param user
	 * @return
	 */
	public String createUserTag(IUser user);
	public IUser findUser(String user_name);
}
