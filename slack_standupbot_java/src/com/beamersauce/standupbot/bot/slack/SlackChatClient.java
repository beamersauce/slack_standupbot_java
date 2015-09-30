package com.beamersauce.standupbot.bot.slack;

import java.io.IOException;

import com.beamersauce.standupbot.bot.IChatClient;
import com.beamersauce.standupbot.bot.ICommandManager;
import com.beamersauce.standupbot.bot.IRoom;
import com.beamersauce.standupbot.bot.IUser;
import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.SlackUser;
import com.ullink.slack.simpleslackapi.impl.SlackSessionFactory;
import com.ullink.slack.simpleslackapi.listeners.SlackMessagePostedListener;

public class SlackChatClient implements IChatClient {
	private final SlackSession slack_session;
	private final SlackMessagePostedListener slack_listener;
	
	public SlackChatClient(ICommandManager command_manager, final String auth_token) {		
		slack_session = SlackSessionFactory.createWebSocketSlackSession(auth_token);
		//TODO not a great idea to tie these 2 services together
		slack_listener = new DefaultSlackMessagePostedListener(command_manager, this);		
		slack_session.addMessagePostedListener(slack_listener);
		try {
			slack_session.connect();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	public boolean joinRoom(IRoom room) {	
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean leaveRoom(IRoom room) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void sendMessage(IRoom room, String message) {
		slack_session.sendMessage(slack_session.findChannelById(room.id()), message, null);
	}

	@Override
	public IUser getUser(String user_id, String user_name, String user_nickname) {		
		return new DefaultUser(user_id, user_name, user_nickname);
	}

	@Override
	public IRoom getRoom(String room_id, String room_name) {
		return new DefaultRoom(room_id, room_name);
	}

	@Override
	public String createUserTag(IUser user) {
		return "<@" + user.nickname() + ">";
	}

	@Override
	public IUser findUser(String user_name) {
		final SlackUser user = slack_session.findUserByUserName(user_name);
		if ( user != null ) {
			return new DefaultUser(user.getId(), user.getRealName(), user.getUserName());
		}
		return null;
	}

}
