package com.beamersauce.standupbot.bot.slack;

import com.beamersauce.standupbot.bot.IChatClient;
import com.beamersauce.standupbot.bot.ICommandManager;
import com.beamersauce.standupbot.bot.IRoom;
import com.beamersauce.standupbot.bot.IUser;
import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.events.SlackMessagePosted;
import com.ullink.slack.simpleslackapi.listeners.SlackMessagePostedListener;

public class DefaultSlackMessagePostedListener implements SlackMessagePostedListener {
	private final ICommandManager command_manager;
	private final IChatClient chat_client;
	
	public DefaultSlackMessagePostedListener(
			ICommandManager command_manager,
			IChatClient chat_client) {
		this.command_manager = command_manager;
		this.chat_client = chat_client;
	}
	
	@Override
	public void onEvent(SlackMessagePosted event, SlackSession session) {
		//TODO pass this message on (to bot? or commandManager)
		System.out.println("Received message: " + event.getMessageContent());
		IUser user = chat_client.getUser(event.getSender().getId(), event.getSender().getRealName(), event.getSender().getUserName());
		IRoom room = chat_client.getRoom(event.getChannel().getId(), event.getChannel().getName());
		command_manager.onReceiveMessage(user, room, event.getMessageContent());
	}

}
