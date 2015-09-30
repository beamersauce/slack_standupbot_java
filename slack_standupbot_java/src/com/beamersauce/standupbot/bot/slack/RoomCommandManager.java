package com.beamersauce.standupbot.bot.slack;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.beamersauce.standupbot.bot.IChatClient;
import com.beamersauce.standupbot.bot.ICommand;
import com.beamersauce.standupbot.bot.ICommandManager;
import com.beamersauce.standupbot.bot.IRoom;
import com.beamersauce.standupbot.bot.IUser;
import com.beamersauce.standupbot.commands.DisplayCommand;

public class RoomCommandManager {
	private static final String BOT_TRIGGER_WORD = "standup";
	private IChatClient chat_client;
	private IRoom room;
	private ICommandManager command_manager;
	private Set<ICommand> all_commands = new HashSet<ICommand>();
	private Set<ICommand> full_stream_commands = new HashSet<ICommand>();
	private Map<String, ICommand> trigger_commands = new HashMap<String, ICommand>();
	
	public RoomCommandManager(IRoom room, IChatClient chat_client, ICommandManager command_manager) {
		System.out.println("Starting room manager for room: " + room.name());
		this.room = room;
		this.chat_client = chat_client;
		this.command_manager = command_manager;
		initCommands();
	}
	
	private void initCommands() {
		//TODO handle this via some settings file, also only turn on
		//commands w/ settings from the save file?
		ICommand command_display = new DisplayCommand();
		all_commands.add(command_display);
		//TODO check enabled
		if ( command_display.receive_full_chat_stream())
			full_stream_commands.add(command_display);
		//TODO actually check we have a trigger word, and throw error is 2 things use the same trigger word
		if ( command_display.trigger_word() != null && 
				!command_display.trigger_word().isEmpty())
			trigger_commands.put(command_display.trigger_word().toLowerCase(), command_display);
	}

	public void onReceiveMessage(IUser user, IRoom room, String message) {
		//TODO spit something out in channel?
		//chat_client.sendMessage(room, "The room received a message from: " + chat_client.createUserTag(user));
		
		String[] message_splits = message.split(" ");
		if ( message_splits[0].toLowerCase().equals(BOT_TRIGGER_WORD) &&
				message_splits.length > 1 &&
				trigger_commands.containsKey(message_splits[1].toLowerCase())) {
			trigger_commands.get(message_splits[1].toLowerCase()).onNewMessage(command_manager, room, user, message);	 
		} else {
			//otherwise pass to everyone that wants the full stream?
			full_stream_commands.stream()
				.forEach(command -> command.onNewMessage(command_manager, room, user, message));
		}
	}
}
