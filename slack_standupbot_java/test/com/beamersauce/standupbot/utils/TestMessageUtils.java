package com.beamersauce.standupbot.utils;

import static org.junit.Assert.*;

import java.util.Optional;
import org.junit.Test;

/**
 * Test the MessageUtils class
 * @author Burch
 *
 */
public class TestMessageUtils {

	@Test
	public void testCreateDefaultCommandHelpMessage() {
		assertTrue(MessageUtils.createDefaultCommandHelpMessage(Optional.empty(), new String[]{"arg1", "arg2"}, "msg1").equals("\"[arg1] [arg2]\" - msg1"));
		assertTrue(MessageUtils.createDefaultCommandHelpMessage(Optional.of("trigger1"), new String[]{"arg1", "arg2"}, "msg2").equals("\"[trigger1] [arg1] [arg2]\" - msg2"));
	}

}
