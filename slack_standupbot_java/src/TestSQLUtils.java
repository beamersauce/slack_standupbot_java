import static org.junit.Assert.*;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.beamersauce.standupbot.bot.IRoom;
import com.beamersauce.standupbot.bot.slack.DefaultRoom;
import com.beamersauce.standupbot.bot.slack.SQLUtils;
import com.beamersauce.standupbot.bot.slack.SQLUtils.ROOM_DATA_TYPE;
import com.fasterxml.jackson.core.JsonProcessingException;

/**
 * Test the SQLUtils class.
 * 
 * @author Burch
 *
 */
public class TestSQLUtils {
	private static final String DB_NAME = "test_db";
	private static final String TEST_TABLE_NAME = "test_table";
	private static Connection conn;
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		Class.forName("org.sqlite.JDBC"); //load up jdbc driver
		conn = DriverManager.getConnection("jdbc:sqlite:" + DB_NAME);
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		//delete any dbs that were not cleaned up
		try {
			SQLUtils.deleteTable(conn, TEST_TABLE_NAME);
		} catch (Exception ex) {
			//do nothing, exc when table doesn't exist
		}
	}

	@After
	public void tearDown() throws Exception {
		//delete any dbs created
		try {
			SQLUtils.deleteTable(conn, TEST_TABLE_NAME);
		} catch (Exception ex) {
			//do nothing, exc when table doesn't exist
		}
	}
	
	@Test
	public void testDeleteTable() throws JsonProcessingException, SQLException, IOException {
		//assert table doesn't exist
		assertFalse(SQLUtils.doesTableExist(conn, TEST_TABLE_NAME));
		SQLUtils.createTableIfDoesntExist(conn, TEST_TABLE_NAME, "key CHAR(256) PRIMARY KEY NOT NULL, DATA TEXT NOT NULL");
		//assert it exists after
		assertTrue(SQLUtils.doesTableExist(conn, TEST_TABLE_NAME));
		//now delete it and assert its gone
		SQLUtils.deleteTable(conn, TEST_TABLE_NAME);
		assertFalse(SQLUtils.doesTableExist(conn, TEST_TABLE_NAME));
	}

	@Test
	public void testCreateTableIfDoesntExist() throws SQLException, JsonProcessingException, IOException {
		//assert table doesn't exist
		assertFalse(SQLUtils.doesTableExist(conn, TEST_TABLE_NAME));
		SQLUtils.createTableIfDoesntExist(conn, TEST_TABLE_NAME, "key CHAR(256) PRIMARY KEY NOT NULL, DATA TEXT NOT NULL");
		//assert it exists after
		assertTrue(SQLUtils.doesTableExist(conn, TEST_TABLE_NAME));
	}

	@Test
	public void testInsertGetData() throws SQLException, IOException {
		//create table
		SQLUtils.createTableIfDoesntExist(conn, TEST_TABLE_NAME, "key CHAR(256) PRIMARY KEY NOT NULL, DATA TEXT NOT NULL");
		
		//assert data doesn't currently exist
		Map<String, Object> result1 = SQLUtils.getData(conn, TEST_TABLE_NAME, "1");
		assertNull(result1);
		
		//add some data
		Map<String,Object> data = new HashMap<String, Object>();
		data.put("a", 5);
		SQLUtils.insertData(conn, TEST_TABLE_NAME, "1", data);
		
		//assert we get the same data back
		Map<String, Object> result2 = SQLUtils.getData(conn, TEST_TABLE_NAME, "1");
		assertEquals(data.get("a"), result2.get("a"));
	}

	@Test
	public void testGetRoomID() {
		final IRoom room = new DefaultRoom("123", "room123");
		assertTrue(SQLUtils.getRoomID(room, ROOM_DATA_TYPE.COMMANDS).equals("123_COMMANDS"));
	}	
}
