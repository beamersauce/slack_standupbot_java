package com.beamersauce.standupbot.bot.slack;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.beamersauce.standupbot.bot.IRoom;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class SQLUtils {
	private static Logger logger = LogManager.getLogger();
	
	public enum ROOM_DATA_TYPE {
		SHARED,
		COMMANDS
	}
	
	public static void createTableIfDoesntExist(final Connection conn, final String table_name, final String fields) throws SQLException {
		final Statement stmt_create = conn.createStatement();
		final String sql_create = "CREATE TABLE IF NOT EXISTS "+table_name+ "(" + fields + ")"; //"(ROOM_NAME CHAR(256) PRIMARY KEY NOT NULL, DATA TEXT NOT NULL)";
		logger.debug("TABLE: " + sql_create);
		stmt_create.executeUpdate(sql_create);
		stmt_create.close();
		logger.debug("table created successfully");
	}
	
	public static void insertData(final Connection conn, final String table_name, final String id, final Map<String, Object> data) throws SQLException, JsonProcessingException {
		final Statement stmt_insert = conn.createStatement();
		final String sql_insert = "INSERT OR REPLACE INTO " + table_name + " VALUES('" + id + "', '" + convertMapToString(data) + "');";
		logger.debug("INSERT: " + sql_insert);
		stmt_insert.executeUpdate(sql_insert);
		stmt_insert.close();
		logger.debug("inserted item successfully");
	}
	
	public static Map<String, Object> getData(final Connection conn, final String table_name, final String id) throws SQLException, JsonProcessingException, IOException {	
		return getData(conn, "*", 2, table_name, id);
	}
	
	public static Map<String, Object> getData(final Connection conn, final String fields, final int column, final String table_name, final String id) throws SQLException, JsonProcessingException, IOException {
		final Statement stmt_select = conn.createStatement();
		final String sql_select = "SELECT " + fields + " FROM " + table_name + " WHERE "+id+";";
		logger.debug("SELECT: " + sql_select);
		ResultSet rs = stmt_select.executeQuery(sql_select);
		Map<String, Object> data = null;
		while ( rs.next() ) {			
			final String text_data = rs.getString(column);
			logger.debug("QUERY: " + text_data);
			data = convertStringToMap(text_data);
		}
		rs.close();
		stmt_select.close();
		return data;		
	}
	
	public static boolean doesTableExist(final Connection conn, final String table_name) throws JsonProcessingException, SQLException, IOException {
		final Statement stmt_select = conn.createStatement();
		final String sql_select = "SELECT name FROM sqlite_master WHERE type='table' AND name='" + table_name + "';";
		logger.debug("TABLE_EXIST: " + sql_select);
		ResultSet rs = stmt_select.executeQuery(sql_select);
		boolean hasResult = false;
		while ( rs.next() ) {	
			hasResult = true;
		}
		rs.close();
		stmt_select.close();
		return hasResult;
	}

	public static void deleteTable(final Connection conn, final String table_name) throws SQLException {
		final String sql_delete = "DROP TABLE " + table_name;
		final Statement stmt_delete = conn.createStatement();
		stmt_delete.executeUpdate(sql_delete);
		stmt_delete.close();
		logger.debug("deleted table: " + table_name + " successfully");
	}
	
	private static Map<String, Object> convertStringToMap(final String text_data) throws JsonProcessingException, IOException {
		final ObjectMapper mapper = new ObjectMapper();
		final JsonNode json = mapper.readTree(text_data);
		@SuppressWarnings("unchecked")
		Map<String, Object> map = mapper.convertValue(json, Map.class);
		logger.debug("CONVERT_s2m: " + map);
		return map;		
	}

	private static String convertMapToString(final Map<String, Object> map) throws JsonProcessingException {
		final ObjectMapper mapper = new ObjectMapper();
		final String json = mapper.writeValueAsString(map);
		logger.debug("CONVERT_m2s: " + json);
		return json;
	}

	public static String getRoomID(IRoom room, ROOM_DATA_TYPE shared) {
		return room.id() + "_" + shared.toString();
	}
}
