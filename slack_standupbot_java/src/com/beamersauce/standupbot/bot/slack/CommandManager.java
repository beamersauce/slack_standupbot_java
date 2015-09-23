package com.beamersauce.standupbot.bot.slack;

import com.beamersauce.standupbot.bot.IBot;
import com.beamersauce.standupbot.bot.IChatClient;
import com.beamersauce.standupbot.bot.ICommandManager;
import com.beamersauce.standupbot.bot.IRoom;
import com.beamersauce.standupbot.bot.IUser;

public class CommandManager implements ICommandManager {	
	private IChatClient chat_client;
	private IBot bot;
	
	@Override
	public void start(IBot bot, IChatClient chat_client) {
		this.chat_client = chat_client;
		this.bot = bot;
	}
	
	@Override
	public void onReceiveMessage(IUser user, IRoom room, String message) {
		if ( !user.nickname().equals(bot.bot_user().nickname())) {
			System.out.println("CommManager: " + message + " Room: " + room.name() + " User: " + user.name() + " | " + user.nickname());
			
			//TODO spit something out in channel?
			chat_client.sendMessage(room, "The room received a message from: " + chat_client.createUserTag(user));
		}
	}

	

}
