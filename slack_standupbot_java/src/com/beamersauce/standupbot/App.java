package com.beamersauce.standupbot;

import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

import com.beamersauce.standupbot.bot.IBot;
import com.beamersauce.standupbot.bot.ICommandManager;
import com.beamersauce.standupbot.bot.IDataManager;
import com.beamersauce.standupbot.bot.slack.CommandManager;
import com.beamersauce.standupbot.bot.slack.FileDataManager;
import com.beamersauce.standupbot.bot.slack.SlackChatClient;
import com.beamersauce.standupbot.bot.slack.DefaultBot;
import com.fasterxml.jackson.core.JsonProcessingException;

public class App {

	public static void main(String[] args) throws InterruptedException, JsonProcessingException, IOException {
		System.out.println("Starting up");
		//get slack api key
		Properties props = new Properties();
		try {
			props.load(new FileReader("config/example.properties"));
		} catch (Exception ex){
			ex.printStackTrace();
		}
		final String auth_token = props.getProperty("slack.auth_token");
		
		//startup bot
		final IBot bot = new DefaultBot();
		final ICommandManager command_manager = new CommandManager();
		final IDataManager data_manager = new FileDataManager("save_data/slack_data.json");		
		bot.start(new SlackChatClient(command_manager, auth_token), command_manager, data_manager);
		while ( true ) {
			Thread.sleep(1000);
		}
	}

}
