package com.beamersauce.standupbot.bot.slack;

import com.beamersauce.standupbot.bot.IBot;
import com.beamersauce.standupbot.bot.IChatClient;
import com.beamersauce.standupbot.bot.ICommandManager;
import com.beamersauce.standupbot.bot.IDataManager;
import com.beamersauce.standupbot.bot.IUser;

public class DefaultBot implements IBot {
	
	private IChatClient chat_client;
	private ICommandManager command_manager; 
	private IDataManager data_manager;
	private IUser bot_user;
	
	@Override
	public void start(
			IChatClient chat_client, 
			ICommandManager command_manager, 
			IDataManager data_manager
			) {
		this.chat_client = chat_client;
		this.command_manager = command_manager;
		this.data_manager = data_manager;
		
		
		this.bot_user = chat_client.findUser("standupbot", null);
		command_manager.start(this, chat_client, data_manager);
	}

	@Override
	public IUser bot_user() {
		return bot_user;
	}

	

}
