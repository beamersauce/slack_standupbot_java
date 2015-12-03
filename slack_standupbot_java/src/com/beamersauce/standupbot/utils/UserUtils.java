package com.beamersauce.standupbot.utils;

import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.beamersauce.standupbot.bot.ICommandManager;

public class UserUtils {
	private final static Pattern slack_tag_pattern = Pattern.compile("<@(\\w+)>");
	/**
	 * Converts a slack tag to a user_id.
	 * User tags in slack look like: <@U03PXXXXX> e.g. <@user.id> so
	 * we just strip of the <@> and return the inside.
	 * 
	 * @param user_tag
	 * @return
	 */
	public static String convertSlackUserTagToID(final String user_tag) {
		final Matcher matcher = slack_tag_pattern.matcher(user_tag);
		if ( matcher.matches() ) {
			return matcher.group(1);
		}
		return null;
	}
	
	/**
	 * Converts a single users id to their nickname, returns it as a tag if requested.
	 * TODO make tags be chat client specific rather than hardcoded for slack
	 * 
	 * @param user_id
	 * @param command_manager
	 * @param returnAsTags
	 * @return
	 */
	public static String convertUserIDToNickname(String user_id, ICommandManager command_manager, boolean returnAsTags) {
		return command_manager.findUser(null, user_id).map(user -> {
			return returnAsTags ? "<@" + user.id() + ">" : user.nickname(); 
		}).orElse("Error finding user"); 
	}
	
	/**
	 * Converts a list of slack user ids to a list of
	 * their usernames, uses the command manager to look up the
	 * user objects.
	 * returnAsTags determines whether to return as the chat programs tag
	 * 
	 * @param user_ids
	 * @param command_manager
	 * @param returnAsTags
	 * @return
	 */
	public static Set<String> convertSetOfUserIDToNicknames(final Set<String> user_ids, ICommandManager command_manager, boolean returnAsTags) {
		return user_ids.stream().map(id -> convertUserIDToNickname(id, command_manager, returnAsTags) ).collect(Collectors.toSet());		
	}
	
	/**
	 * Converts a map of slack user ids + values to a map of
	 * their nicknames + values instead, will set their usernames to tags if requested.
	 * @param <T> We don't care what the type of your maps values are, we just pass it along in a new map
	 * 
	 * @param user_ids_map
	 * @param command_manager
	 * @param returnAsTags
	 * @return
	 */
	public static <T> Map<String, T> convertMapOfUserIDToNicknames(final Map<String, T> user_ids_map, ICommandManager command_manager, boolean returnAsTags) {
		return user_ids_map.keySet().stream().collect(Collectors.toMap(user_id -> {
			return convertUserIDToNickname(user_id, command_manager, returnAsTags); 
		}, user_id -> {
			return user_ids_map.get(user_id);
		}));
	}
	
	public static String getBotNickname() {
		return "standupbot";
	}
}
