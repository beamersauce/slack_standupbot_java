package com.beamersauce.standupbot.bot;

public interface ICommandManager {

	public void start(IBot bot, IChatClient chat_client);
	public void onReceiveMessage(IUser user, IRoom room, String message);
	public void sendMessage(IRoom room, String message);
	public IDataManager getDataManager(IRoom room);
}
