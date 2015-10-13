package com.beamersauce.standupbot.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
}
