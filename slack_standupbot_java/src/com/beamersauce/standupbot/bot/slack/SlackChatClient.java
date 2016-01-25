package com.beamersauce.standupbot.bot.slack;

import java.io.IOException;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.beamersauce.standupbot.bot.IChatClient;
import com.beamersauce.standupbot.bot.ICommandManager;
import com.beamersauce.standupbot.bot.IRoom;
import com.beamersauce.standupbot.bot.IUser;
import com.ullink.slack.simpleslackapi.SlackChannel;
import com.ullink.slack.simpleslackapi.SlackPersona.SlackPresence;
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
	public IUser findUser(String user_name, String user_id) {
		final SlackUser user = user_name != null ? slack_session.findUserByUserName(user_name) : slack_session.findUserById(user_id);
		if (user != null )
			return new DefaultUser(user.getId(), user.getRealName(), user.getUserName());
		return null;
	}

	@Override
	public Set<IUser> getRoomUsers(IRoom room) {
		final SlackChannel channel = slack_session.findChannelById(room.id());
		if ( channel != null ) {
			return channel.getMembers().stream().map(user -> {
				return getUser(user.getId(), user.getRealName(), user.getUserName());
			}).collect(Collectors.toSet());
		}
		return new HashSet<IUser>();
	}

	@Override
	public Optional<IRoom> findRoom(Optional<String> room_id, Optional<String> room_name) {
		if ( room_id.isPresent() ) {
			final SlackChannel channel = slack_session.findChannelById(room_id.get());
			if ( channel != null )
				return Optional.of(new DefaultRoom(channel.getId(), channel.getName()));
		}
		if ( room_name.isPresent() ) {
			final SlackChannel channel = slack_session.findChannelByName(room_name.get());
			if ( channel != null )
				return Optional.of(new DefaultRoom(channel.getId(), channel.getName()));
		}
		return Optional.empty();
	}
	
	@Override
	public boolean isUserActive(final IUser user) {
		final SlackUser slack_user = slack_session.findUserById(user.id());
		return slack_session.getPresence(slack_user) == SlackPresence.ACTIVE;
	}

}
