package com.beamersauce.standupbot.bot;

public interface IBot {
	/**
	 * rejoin all previous rooms, and get ready to listen to messages, etc
	 */
	public void start(
			IChatClient chat_client, 
			ICommandManager command_manager, 
			IDataManager data_manager
			);

	public IUser bot_user();
}
