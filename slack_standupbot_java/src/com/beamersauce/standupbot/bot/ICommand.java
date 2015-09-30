package com.beamersauce.standupbot.bot;

public interface ICommand {
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
	public String display_message();
	/**
	 * Returns back how to use the command
	 * @return
	 */
	public String help_message();
}
