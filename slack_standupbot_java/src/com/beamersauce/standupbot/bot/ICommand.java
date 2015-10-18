package com.beamersauce.standupbot.bot;

import java.util.Optional;

public interface ICommand {
	public String id();
	public String trigger_word();
	public boolean enabled();
	public boolean receive_full_chat_stream();
	//TODO some permission settings?
	//TODO some scheduling settings if necessary?
	
	public void onNewMessage(ICommandManager command_manager, IRoom room, IUser user, String message);
	/**
	 * Returns a status message for the command
	 * @return
	 */
	public Optional<String> display_message(ICommandManager command_manager, IRoom room);
	/**
	 * Returns back how to use the command
	 * @return
	 */
	public String help_message();
	
}
