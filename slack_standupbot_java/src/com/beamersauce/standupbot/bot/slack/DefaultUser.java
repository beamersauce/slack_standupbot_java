package com.beamersauce.standupbot.bot.slack;

import com.beamersauce.standupbot.bot.IUser;

public class DefaultUser implements IUser {

	private final String id;
	private final String name;
	private final String nickname;
	
	public DefaultUser(String user_id, String user_name, String user_nickname) {
		id = user_id;
		name = user_name;
		nickname = user_nickname;
	}

	@Override
	public String id() {
		return id;
	}

	@Override
	public String name() {
		return name;
	}

	@Override
	public String nickname() {
		return nickname;
	}

}
