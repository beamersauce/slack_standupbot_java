package com.beamersauce.standupbot.bot.slack;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.beamersauce.standupbot.bot.IChatClient;
import com.beamersauce.standupbot.bot.ICommand;
import com.beamersauce.standupbot.bot.ICommandManager;
import com.beamersauce.standupbot.bot.IDataManager;
import com.beamersauce.standupbot.bot.IRoom;
import com.beamersauce.standupbot.bot.IUser;
import com.beamersauce.standupbot.commands.BlacklistCommand;
import com.beamersauce.standupbot.commands.DisplayCommand;
import com.beamersauce.standupbot.commands.EchoCommand;
import com.beamersauce.standupbot.commands.HelpCommand;
import com.beamersauce.standupbot.commands.MeetingCommand;

public class RoomCommandManager {
	private static final String BOT_TRIGGER_WORD = "standup";
	private IChatClient chat_client;
	private IRoom room;
	private ICommandManager command_manager;
	private Set<ICommand> all_commands = new HashSet<ICommand>();
	private Set<ICommand> full_stream_commands = new HashSet<ICommand>();
	private Map<String, ICommand> trigger_commands = new HashMap<String, ICommand>();
	private final IDataManager data_manager;
	
	public RoomCommandManager(IRoom room, IChatClient chat_client, ICommandManager command_manager, IDataManager data_manager) {
		System.out.println("Starting room manager for room: " + room.name());
		this.room = room;
		this.chat_client = chat_client;
		this.command_manager = command_manager;
		this.data_manager = data_manager;
		initCommands();
	}
	
	private void initCommands() {
		//TODO handle this via some settings file, also only turn on
		//commands w/ settings from the save file?		
		addCommand(new DisplayCommand());
		addCommand(new EchoCommand());
		addCommand(new HelpCommand());
		//addCommand(new MeetingCommand());
		addCommand(new BlacklistCommand());
	}
	
	private void addCommand(ICommand command) {
		all_commands.add(command);
		//TODO check enabled (probably need to check enabled in onReceiveMessage actually)
		if ( command.receive_full_chat_stream())
			full_stream_commands.add(command);
		//TODO actually check we have a trigger word, and throw error if 2 things use the same trigger word
		if ( command.trigger_word() != null && 
				!command.trigger_word().isEmpty()) {
			System.out.println("Added trigger word: " + command.trigger_word().toLowerCase());
			trigger_commands.put(command.trigger_word().toLowerCase(), command);
		}
	}

	public void onReceiveMessage(IUser user, IRoom room, String message) {
		//TODO spit something out in channel?
		//chat_client.sendMessage(room, "The room received a message from: " + chat_client.createUserTag(user));
		
		String[] message_splits = message.split(" ");
		if ( message_splits[0].toLowerCase().equals(BOT_TRIGGER_WORD) ) {
			if (	message_splits.length > 1 && trigger_commands.containsKey(message_splits[1].toLowerCase())) {
				trigger_commands.get(message_splits[1].toLowerCase()).onNewMessage(command_manager, room, user, message);
			} else {
				chat_client.sendMessage(room, "Unknown trigger word, type '" + BOT_TRIGGER_WORD + " help' to see a list of commands");
			}	
		} else {
			//otherwise pass to everyone that wants the full stream?
			full_stream_commands.stream()
				.forEach(command -> command.onNewMessage(command_manager, room, user, message));
		}
	}
	
	public IDataManager data_manager() { return this.data_manager; }
	public Set<ICommand> all_commands() { return this.all_commands; }
}
