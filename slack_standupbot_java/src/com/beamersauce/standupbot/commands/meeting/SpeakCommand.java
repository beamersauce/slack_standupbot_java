package com.beamersauce.standupbot.commands.meeting;

import com.beamersauce.standupbot.bot.ICommand;
import com.beamersauce.standupbot.bot.ICommandManager;
import com.beamersauce.standupbot.bot.IRoom;
import com.beamersauce.standupbot.bot.IUser;
import com.beamersauce.standupbot.utils.MessageUtils;
import com.beamersauce.standupbot.utils.RoomUtils;

import java.util.Optional;

public class SpeakCommand implements ICommand {

	@Override
	public String id() {
		return "speak";
	}

	@Override
	public Optional<String> trigger_word() {
		return Optional.of("speak");
	}

	@Override
	public boolean enabled() {
		return true;
	}

	@Override
	public boolean receive_full_chat_stream() {
		return false;
	}

	@Override
	public void onNewMessage(ICommandManager command_manager, IRoom room, IUser user, String message) {
		//format should be "speak room message
		final String sub_message = message.substring(message.indexOf(this.trigger_word().get()) + this.trigger_word().get().length()).trim();
		final String room_name = sub_message.substring(0, sub_message.indexOf(' ')).trim();
		final String speak = sub_message.trim().substring(room_name.length()).trim();

		//try to find room
		final Optional<IRoom> speak_room = command_manager.findRoom(room_name);
		if ( speak_room.isPresent() ) {
			command_manager.sendMessage(speak_room.get(), speak);
		} else {
			command_manager.sendMessage(room, "could not find room: " + room_name);
		}
	}

	@Override
	public Optional<String> display_message(ICommandManager command_manager, IRoom room) {
		return Optional.empty();
	}

	@Override
	public String help_message() {
		return MessageUtils.createDefaultCommandHelpMessage(trigger_word(), new String[]{"room_name"}, "speaks anything you type to the given room");
	}

	@Override
	public void initialize(ICommandManager command_manager, IRoom room) {
		// TODO Auto-generated method stub
		
	}

}
