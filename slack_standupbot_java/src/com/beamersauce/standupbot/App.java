package com.beamersauce.standupbot;

import java.io.FileReader;
import java.util.Properties;

import com.beamersauce.standupbot.bot.IBot;
import com.beamersauce.standupbot.bot.ICommandManager;
import com.beamersauce.standupbot.bot.IDataManager;
import com.beamersauce.standupbot.bot.IUser;
import com.beamersauce.standupbot.bot.slack.CommandManager;
import com.beamersauce.standupbot.bot.slack.DefaultUser;
import com.beamersauce.standupbot.bot.slack.SlackChatClient;
import com.beamersauce.standupbot.bot.slack.StandupBot;

public class App {

	public static void main(String[] args) throws InterruptedException {
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
		final IBot bot = new StandupBot();
		final ICommandManager command_manager = new CommandManager();
		final IDataManager data_manager = null;		
		bot.start(new SlackChatClient(command_manager, auth_token), command_manager, data_manager);
		while ( true ) {
			Thread.sleep(1000);
		}
	}

}
