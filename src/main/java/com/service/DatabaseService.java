package com.service;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import com.model.DatabaseInfo;

import jakarta.servlet.http.HttpServletResponse;

public interface DatabaseService {

	List<DatabaseInfo> getDatabaseDropdown();

	byte[] convertToBytes(List<String> indexRange, String query) throws IOException;

	Integer UpdateDatabase(List<String> indexRange, String query) throws SQLException;
	void generateDataOnlySqlDump(String dbIndex, OutputStream outputStream) throws IOException, SQLException ;

	void downloadAllDatabases();

	
}

