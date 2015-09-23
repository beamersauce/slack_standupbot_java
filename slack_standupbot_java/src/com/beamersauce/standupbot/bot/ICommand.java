package com.beamersauce.standupbot.bot;

public interface ICommand {
	public String trigger_word();
	public void onNewMessage(IRoom room, IUser user, String message);
}
