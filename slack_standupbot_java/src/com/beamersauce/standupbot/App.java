package com.beamersauce.standupbot;

import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.impl.SlackSessionFactory;

public class App {

	public static void main(String[] args) {
		System.out.println("sup");
		SlackSession session = SlackSessionFactory.createWebSocketSlackSession("");
	}

}
