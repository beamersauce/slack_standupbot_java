package com.beamersauce.standupbot.bot;

import java.util.Optional;
import java.util.Set;

public interface ICommandManager {

	public void start(IBot bot, IChatClient chat_client, IDataManager data_manager);
	public void onReceiveMessage(IUser user, IRoom room, String message);
	public void sendMessage(IRoom room, String message);
	public IDataManager getDataManager(IRoom room);
	public Set<ICommand> getRoomCommands(IRoom room);	
	public Optional<IUser> findUser(String user_name, String user_id);
}
