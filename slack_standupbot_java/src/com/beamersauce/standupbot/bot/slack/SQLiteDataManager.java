package com.beamersauce.standupbot.bot.slack;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.beamersauce.standupbot.bot.ICommand;
import com.beamersauce.standupbot.bot.IDataManager;
import com.beamersauce.standupbot.bot.IRoom;
import com.beamersauce.standupbot.bot.slack.SQLUtils.ROOM_DATA_TYPE;
import com.fasterxml.jackson.core.JsonProcessingException;

public class SQLiteDataManager implements IDataManager {

	private static final String DEFAULT_DB_NAME = "standupbot_db";
	private final String DB_NAME;
	private static final Logger	logger = LogManager.getLogger();
	private static final String GLOBAL_TABLE_NAME = "global";
	private static final String GLOBAL_KEY = "GLOBAL_KEY";
	private static final String GLOBAL_ID = "global_id";
	private static final String ROOM_TABLE_NAME = "rooms";	
	private static final String ROOM_KEY = "ROOM_ID";
	private final Connection conn;
	
	public SQLiteDataManager() throws ClassNotFoundException, SQLException {
		this(DEFAULT_DB_NAME);
	}
	
	public SQLiteDataManager(final String db_name) throws ClassNotFoundException, SQLException {
		//create db if it doesn't exist
		DB_NAME = db_name;
		Class.forName("org.sqlite.JDBC");
		conn = DriverManager.getConnection("jdbc:sqlite:" + DB_NAME);
		logger.debug("Opened db: " + DB_NAME + "  successfully");
		initDB();
	}
	
	private void initDB() throws ClassNotFoundException, SQLException {		
		//set up global table if it doesn't exist
		SQLUtils.createTableIfDoesntExist(conn, GLOBAL_TABLE_NAME, GLOBAL_KEY + " CHAR(256) PRIMARY KEY NOT NULL, DATA TEXT NOT NULL");
		
		//set up rooms table if it doesn't exist
		SQLUtils.createTableIfDoesntExist(conn, ROOM_TABLE_NAME, ROOM_KEY + " CHAR(256) PRIMARY KEY NOT NULL, DATA TEXT NOT NULL");
		
		//close conn while we aren't using it
//		conn.close(); //TODO is it better to close this per command or hold onto a global conn constantly?
	}
	
	@Override
	public void set_global_shared_data(Map<String, Object> global_data) {
		try {
			SQLUtils.insertData(conn, GLOBAL_TABLE_NAME, GLOBAL_ID, global_data);
		} catch (JsonProcessingException | SQLException e) {
			logger.error(e);
		}
	}

	@Override
	public Map<String, Object> get_global_shared_data() {
		Map<String, Object> result = null;
		try {
			result = SQLUtils.getData(conn, GLOBAL_TABLE_NAME, GLOBAL_KEY + "='" + GLOBAL_ID +"'");
		} catch (SQLException | IOException e) {
			logger.error(e);
		}
		if ( result == null )
			return new HashMap<String, Object>();
		return result;
	}

	@Override
	public void set_shared_room_data(IRoom room,
			Map<String, Object> shared_room_data) {
		try {
			final String room_id = SQLUtils.getRoomID(room, ROOM_DATA_TYPE.SHARED);
			SQLUtils.insertData(conn, ROOM_TABLE_NAME, room_id, shared_room_data);
		} catch (JsonProcessingException | SQLException e) {
			logger.error(e);
		}
	}

	@Override
	public Map<String, Object> get_shared_room_data(IRoom room) {
		final String room_id = SQLUtils.getRoomID(room, ROOM_DATA_TYPE.SHARED);
		Map<String, Object> result = null;
		try {
			result = SQLUtils.getData(conn, ROOM_TABLE_NAME, ROOM_KEY + "='" + room_id +"'");
		} catch (SQLException | IOException e) {
			logger.error(e);
		}
		if ( result == null )
			return new HashMap<String, Object>();
		return result;
	}

	@Override
	public void set_command_data(ICommand command, IRoom room,
			Map<String, Object> data) {
		try {
			final String room_id = SQLUtils.getRoomID(room, ROOM_DATA_TYPE.COMMANDS);
			SQLUtils.insertData(conn, ROOM_TABLE_NAME, room_id, data);
		} catch (JsonProcessingException | SQLException e) {
			logger.error(e);
		}
	}

	@Override
	public Map<String, Object> get_command_data(ICommand command, IRoom room) {
		final String room_id = SQLUtils.getRoomID(room, ROOM_DATA_TYPE.COMMANDS);
		Map<String, Object> result = null;
		try {
			result = SQLUtils.getData(conn, ROOM_TABLE_NAME, ROOM_KEY + "='" + room_id +"'");
		} catch (SQLException | IOException e) {
			logger.error(e);
		}
		if ( result == null )
			return new HashMap<String, Object>();
		return result;
	}

}
