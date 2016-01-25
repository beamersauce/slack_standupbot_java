package com.beamersauce.standupbot.bot.slack;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.beamersauce.standupbot.bot.ICommand;
import com.beamersauce.standupbot.bot.IDataManager;
import com.beamersauce.standupbot.bot.IRoom;
import com.beamersauce.standupbot.bot.slack.DefaultDataObject.DefaultRoomData;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class FileDataManager implements IDataManager {

	private final DefaultDataObject data;
	private final String save_file_location;
	private final ObjectMapper mapper;
	
	public FileDataManager(String save_file_location) throws JsonProcessingException, IOException {
		//load previous data from disk
		this.save_file_location = save_file_location;
		mapper = new ObjectMapper();
		File file = new File(save_file_location);
		if (file.createNewFile() ) {
			//had to create a new file, so save object is empty, just create an empty one
			data = new DefaultDataObject();
			write_data_to_disk(); //immediately flush so we save a valid json file back
		} else {
			//file exists, hope it reads in correctly
			data = mapper.readValue(file, DefaultDataObject.class);
		}		
	}

	private void write_data_to_disk() {
		try {
			mapper.writeValue(new File(save_file_location), data);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void set_global_shared_data(Map<String, Object> global_data) {
		data._global = global_data;
		write_data_to_disk();
	}

	@Override
	public Map<String, Object> get_global_shared_data() {
		return data._global;
	}

	@Override
	public void set_shared_room_data(IRoom room,
			Map<String, Object> shared_room_data) {
		getOrCreateRoomData(room)._shared = shared_room_data;
		write_data_to_disk();
	}

	@Override
	public Map<String, Object> get_shared_room_data(IRoom room) {
		return getOrCreateRoomData(room)._shared;
	}
	
	private DefaultRoomData getOrCreateRoomData(IRoom room) {
		if ( !data._rooms.containsKey(room.id()) ) {
			data._rooms.put(room.id(), new DefaultDataObject.DefaultRoomData());
		}
		return data._rooms.get(room.id());
	}

	@Override
	public void set_command_data(ICommand command, IRoom room,
			Map<String, Object> data) {
		this.data._rooms.get(room.id())._commands.put(command.id(), data); 
		write_data_to_disk();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Object> get_command_data(ICommand command, IRoom room) {
		return (Map<String, Object>) this.data
				._rooms.getOrDefault(room.id(), new DefaultRoomData())
				._commands.getOrDefault(command.id(), new HashMap<String, Object>());
	}


}
