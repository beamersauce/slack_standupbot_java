import static org.junit.Assert.*;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.beamersauce.standupbot.bot.slack.SQLUtils;
import com.fasterxml.jackson.core.JsonProcessingException;


public class TestSQLUtils {
	private static final String DB_NAME = "test_db";
	private static final String test_table_name = "test_table";
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
			SQLUtils.deleteTable(conn, test_table_name);
		} catch (Exception ex) {
			//do nothing, exc when table doesn't exist
		}
	}

	@After
	public void tearDown() throws Exception {
		//delete any dbs created
		try {
			SQLUtils.deleteTable(conn, test_table_name);
		} catch (Exception ex) {
			//do nothing, exc when table doesn't exist
		}
	}
	
	@Test
	public void testDeleteTable() throws JsonProcessingException, SQLException, IOException {
		//assert table doesn't exist
		assertFalse(SQLUtils.doesTableExist(conn, test_table_name));
		SQLUtils.createTableIfDoesntExist(conn, test_table_name, "key CHAR(256) PRIMARY KEY NOT NULL, DATA TEXT NOT NULL");
		//assert it exists after
		assertTrue(SQLUtils.doesTableExist(conn, test_table_name));
		//now delete it and assert its gone
		SQLUtils.deleteTable(conn, test_table_name);
		assertFalse(SQLUtils.doesTableExist(conn, test_table_name));
	}

	@Test
	public void testCreateTableIfDoesntExist() throws SQLException, JsonProcessingException, IOException {
		//assert table doesn't exist
		assertFalse(SQLUtils.doesTableExist(conn, test_table_name));
		SQLUtils.createTableIfDoesntExist(conn, test_table_name, "key CHAR(256) PRIMARY KEY NOT NULL, DATA TEXT NOT NULL");
		//assert it exists after
		assertTrue(SQLUtils.doesTableExist(conn, test_table_name));
	}

	@Test
	public void testInsertData() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetData() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetRoomID() {
		fail("Not yet implemented");
	}	
}
