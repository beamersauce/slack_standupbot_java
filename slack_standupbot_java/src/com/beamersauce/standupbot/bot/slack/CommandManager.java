package com.beamersauce.standupbot.bot.slack;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.beamersauce.standupbot.bot.IBot;
import com.beamersauce.standupbot.bot.IChatClient;
import com.beamersauce.standupbot.bot.ICommand;
import com.beamersauce.standupbot.bot.ICommandManager;
import com.beamersauce.standupbot.bot.IDataManager;
import com.beamersauce.standupbot.bot.IRoom;
import com.beamersauce.standupbot.bot.IUser;

public class CommandManager implements ICommandManager {	
	private IChatClient chat_client;
	private IBot bot;
	private Map<String, RoomCommandManager> room_managers = new HashMap<String, RoomCommandManager>();
	private IDataManager data_manager;
	
	@Override
	public void start(IBot bot, IChatClient chat_client, IDataManager data_manager) {
		this.chat_client = chat_client;
		this.bot = bot;
		this.data_manager = data_manager;
	}

	@Override
	public void onReceiveMessage(final IUser user, final IRoom room, final String message) {
		if ( !user.nickname().equals(bot.bot_user().nickname())) {
			System.out.println("CommManager: " + message + " Room: " + room.name() + " User: " + user.name() + " | " + user.nickname());
			
			final RoomCommandManager room_manager = getOrCreateRoomManager(room);
			room_manager.onReceiveMessage(user, room, message);
			
			
			
			//TODO actual work, pass this message on to all commands registered on the trigger word
			//and all the commands receiving the full stream in that room
			//TODO probably just create some submodule that has an instance per room, we just pass
			//messages on to it and it decides which commands to pass it to (based on them being enabled, etc)
		}
	}

	private RoomCommandManager getOrCreateRoomManager(final IRoom room) {
		if ( room_managers.containsKey(room.name()) )
			return room_managers.get(room.name());
		//room manager didn't exist, create it		
		final RoomCommandManager room_manager = new RoomCommandManager(room, chat_client, this, data_manager);
		room_managers.put(room.name(), room_manager);
		return room_manager;
		
	}

	@Override
	public void sendMessage(final IRoom room, final String message) {
		//pass message on from command
		chat_client.sendMessage(room, message);
	}

	@Override
	public IDataManager getDataManager(final IRoom room) {
		return getOrCreateRoomManager(room).data_manager();
	}

	@Override
	public Set<ICommand> getRoomCommands(IRoom room) {
		final RoomCommandManager room_manager = getOrCreateRoomManager(room);
		return room_manager.all_commands();
	}

	@Override
	public Optional<IUser> findUser(String user_name, String user_id) {
		return Optional.ofNullable(chat_client.findUser(user_name, user_id));
	}

	

}
