package com.beamersauce.standupbot.bot.slack;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.beamersauce.standupbot.bot.ICommand;
import com.beamersauce.standupbot.bot.IRoom;
import com.beamersauce.standupbot.commands.DisplayCommand;
import com.fasterxml.jackson.core.JsonProcessingException;

public class TestFileDataManager {

	private final File file = new File("save_data/test.json");
	
	@Before
	public void before() {
		file.delete();
	}
	
	@After
	public void after() {
		file.delete();
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void test() throws JsonProcessingException, IOException {
		FileDataManager data_manager = new FileDataManager("save_data/test.json");
		Map<String, Object> global = data_manager.get_global_shared_data();
		global.put("test_global", "111");
		data_manager.set_global_shared_data(global);
		assertTrue("111".equals(data_manager.get_global_shared_data().get("test_global")));
		
		IRoom room = new DefaultRoom("123", "testroom");
		Map<String, Object> shared_room_data = data_manager.get_shared_room_data(room);
		shared_room_data.put("share.test1", "test_share_room");
		data_manager.set_shared_room_data(room, shared_room_data);
		assertTrue("test_share_room".equals(data_manager.get_shared_room_data(room).get("share.test1")));
		
		ICommand command = new DisplayCommand();
		Map<String, Object> command_data = (Map<String, Object>) data_manager.get_command_data(command, room);
		command_data.put("test_command_data", "456");
		data_manager.set_command_data(command, room, command_data);
		assertTrue("456".equals(((Map<String,Object>)data_manager.get_command_data(command, room)).get("test_command_data")));
		
		
	}
}
