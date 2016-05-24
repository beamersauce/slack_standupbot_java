package com.beamersauce.standupbot.utils;

import java.util.Optional;

public class MessageUtils {
	/**
	 * Creates a help command in the format:
	 * "[trigger word] [arg0] [arg1] [argn]" - message
	 * 
	 * @param trigger_word
	 * @param arg_names
	 * @param message
	 * @return
	 */
	public static String createDefaultCommandHelpMessage(final Optional<String> trigger_word, final String[] arg_names, final String message) {
		final StringBuilder sb = new StringBuilder("\"");
		if ( trigger_word.isPresent() ) {
			sb.append("[").append(trigger_word.get()).append("] ");
		} 
		for ( String arg_name : arg_names ) {
			sb.append("[").append(arg_name).append("] ");
		}
		sb.deleteCharAt(sb.length()-1);
		sb.append("\" - ").append(message);
		return sb.toString();
	}
}
