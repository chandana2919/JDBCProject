package com.service.impl;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.DependsOn;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.DatabaseInitializer;
import com.model.DatabaseInfo;
import com.service.DatabaseService;

@Service


public class DatabaseServiceImpl implements DatabaseService {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseServiceImpl.class);

    @Override
    public List<DatabaseInfo> getDatabaseDropdown() {
        List<DatabaseInfo> databaseList = new ArrayList<>();
        for (DatabaseInfo db : StaticMasterData.dbInfoList) {
            DatabaseInfo newdb = new DatabaseInfo();
            newdb.setIndex(db.getIndex());
            newdb.setDbName(db.getDbName());
            databaseList.add(newdb);
        }
        return databaseList;
    }

    @Override
    public Integer UpdateDatabase(List<String> indexRange, String query) {
        int output = 0;
        for (String ab : indexRange) {
            for (DatabaseInfo db : StaticMasterData.dbInfoList) {
                if (ab.equals(db.getIndex())) {
                    String host = db.getUrl();
                    String userName = db.getUserName();
                    String password = db.getPassword();
                    logger.info("Connecting to database: {}", host);
                    try {
                        output = updateQuery(host, userName, password, query);
                    } catch (SQLException e) {
                        logger.error("Error updating database: {}", e.getMessage());
                        // Optionally, throw a custom exception or return a specific error code
                    }
                }
            }
        }
        return output;
    }

    private int updateQuery(String host, String dbUsername, String dbPassword, String sqlQuery) throws SQLException {
        try (Connection conn = DriverManager.getConnection(host, dbUsername, dbPassword);
             PreparedStatement stmt = conn.prepareStatement(sqlQuery)) {
            return stmt.executeUpdate();
        }
    }

    public byte[] convertToBytes(List<String> indexRange, String query) throws IOException {
        List<Map<String, Object>> list = mainList(indexRange, query);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(byteArrayOutputStream);

        // Write CSV header
        if (!list.isEmpty()) {
            Set<String> headers = list.get(0).keySet();
            writer.println(String.join(",", headers));

            for (Map<String, Object> row : list) {
                List<String> values = headers.stream()
                        .map(header -> String.valueOf(row.get(header))) // Convert values to String
                        .collect(Collectors.toList());
                writer.println(String.join(",", values)); // Write values
            }
        }

        writer.flush();
        return byteArrayOutputStream.toByteArray(); // Return byte array
    }

    private List<Map<String, Object>> mainList(List<String> indexRange, String query) {
        List<Map<String, Object>> mainList = new ArrayList<>();

        for (String ab : indexRange) {
            for (DatabaseInfo db : StaticMasterData.dbInfoList) {
                if (ab.equals(db.getIndex())) {
                    String host = db.getUrl();
                    String userName = db.getUserName();
                    String password = db.getPassword();
                    logger.info("Retrieving data from database: {}", host);
                    try {
                        List<Map<String, Object>> list = getList(host, userName, password, query);
                        if (list != null && !list.isEmpty()) {
                            mainList.addAll(list);
                        }
                    } catch (SQLException e) {
                        logger.error("Error retrieving data from database: {}", e.getMessage());
                        // Handle the exception appropriately
                    }
                }
            }
        }
        return mainList;
    }

    private List<Map<String, Object>> getList(String host, String dbUsername, String dbPassword, String sqlQuery) throws SQLException {
        List<Map<String, Object>> sourceList = new ArrayList<>();
        try (Connection connection = DriverManager.getConnection(host, dbUsername, dbPassword);
             PreparedStatement pstmt = connection.prepareStatement(sqlQuery)) {
            pstmt.setFetchSize(25000);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs != null) {
                    listProcessor(rs, sourceList);
                }
            }
        }
        return sourceList;
    }

    private void listProcessor(ResultSet resultSet, List<Map<String, Object>> sourceList) throws SQLException {
        ResultSetMetaData rsmd = resultSet.getMetaData();
        int columnCount = rsmd.getColumnCount();
        Stream<Map<String, Object>> stream = StreamSupport.stream(new Spliterators.AbstractSpliterator<Map<String, Object>>(
                Long.MAX_VALUE, Spliterator.ORDERED) {
            @Override
            public boolean tryAdvance(Consumer<? super Map<String, Object>> action) {
                try {
                    if (resultSet.next()) {
                        Map<String, Object> row = new LinkedHashMap<>();
                        for (int i = 1; i <= columnCount; i++) {
                            String columnName = rsmd.getColumnLabel(i);
                            if (columnName == null || columnName.isEmpty()) {
                                columnName = rsmd.getColumnName(i);
                            }
                            row.put(columnName, resultSet.getObject(i));
                        }
                        action.accept(row);
                        return true;
                    }
                } catch (SQLException e) {
                    logger.error("Error processing result set: {}", e.getMessage());
                }
                return false;
            }
        }, false);
        stream.forEach(sourceList::add);
    }
    @Scheduled(cron = "0 0 16 * * ?") 
    public void downloadAllDatabases() {
    	
    	if(StaticMasterData.dbStatus == false) {
    		return;
    	}
       System.out.println("runnibg");
        String folderPath = "/home/chandana/MigrationDump";  
        File folder = new File(folderPath);

        logger.info("Starting scheduled task to download all databases.");
        if (!folder.exists()) {
            logger.warn("Folder path {} does not exist. Attempting to create it.", folderPath);
            boolean created = folder.mkdirs(); 
            if (created) {
                logger.info("Successfully created folder: {}", folderPath);
            } else {
                logger.error("Failed to create folder: {}. Check permissions or path correctness.", folderPath);
                return;  // Stop further execution if folder creation fails
            }
        }

        // Check if dbInfoList is populated
        if (StaticMasterData.dbInfoList != null && !StaticMasterData.dbInfoList.isEmpty()) {
            logger.info("Starting database download process for {} databases", StaticMasterData.dbInfoList.size());

            for (DatabaseInfo db : StaticMasterData.dbInfoList) {
                String dbIndex = db.getIndex();
                String dbName = db.getDbName();

                try {
                    // Create unique file name for the SQL dump
                    String fileName = String.format("%s_%s.sql", dbName, LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")));
                    File dbFile = new File(folder, fileName);

                    // Generate SQL dump and write it to the file
                    try (OutputStream outputStream = new FileOutputStream(dbFile)) {
                        generateDataOnlySqlDump(dbIndex, outputStream);
                        logger.info("Successfully downloaded database: {} to file: {}", dbName, dbFile.getAbsolutePath());
                    }

                } catch (IOException e) {
                    logger.error("I/O error occurred while downloading database {}: {}", dbName, e.getMessage(), e);
                } catch (SQLException e) {
                    logger.error("SQL error occurred while downloading database {}: {}", dbName, e.getMessage(), e);
                } catch (Exception e) {
                    logger.error("Unexpected error occurred while downloading database {}: {}", dbName, e.getMessage(), e);
                }
            }
        } else {
            logger.error("dbInfoList is null or empty. Cannot proceed with database download.");
        }

        logger.info("Completed scheduled task for downloading all databases.");
    }

    @Override
    public void generateDataOnlySqlDump(String dbIndex, OutputStream outputStream) throws IOException, SQLException {
        dbIndex = dbIndex.replace("\"", "").trim();
        for (DatabaseInfo db : StaticMasterData.dbInfoList) {
            if (db.getIndex().equals(dbIndex)) {
                String host = db.getUrl();
                String userName = db.getUserName();
                String password = db.getPassword();
                try (Connection conn = DriverManager.getConnection(host, userName, password);
                     BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream))) {
                      // Fetch all table names
                    PreparedStatement stmt = conn.prepareStatement("SHOW TABLES");
                    ResultSet rs = stmt.executeQuery();
                    while (rs.next()) {
                        String tableName = rs.getString(1);
                        writer.write("DROP TABLE IF EXISTS `" + tableName + "`;\n");
                        writeCreateTableStatement(conn, tableName, writer);
                        writeInsertStatements(conn, tableName, writer);
                        writer.flush(); 
                    }

                    writer.flush(); 
                }
                return;
            }
        }
        throw new IOException("No database found for index: " + dbIndex);
    }

    private void writeCreateTableStatement(Connection conn, String tableName, BufferedWriter writer) throws SQLException, IOException {
        String createTableSQL = "CREATE TABLE `" + tableName + "` (...);";
        writer.write(createTableSQL + "\n");
    }

    private void writeInsertStatements(Connection conn, String tableName, BufferedWriter writer) throws SQLException, IOException {
        try (PreparedStatement dataStmt = conn.prepareStatement("SELECT * FROM `" + tableName + "`")) {
            ResultSet dataRs = dataStmt.executeQuery();
            while (dataRs.next()) {
                StringBuilder insertStmt = new StringBuilder("INSERT INTO `" + tableName + "` VALUES (");
                for (int i = 1; i <= dataRs.getMetaData().getColumnCount(); i++) {
                    String value = dataRs.getString(i);
                    insertStmt.append(value != null ? "'" + value.replace("'", "''") + "'" : "NULL").append(",");
                }
                insertStmt.setLength(insertStmt.length() - 1); 
                insertStmt.append(");\n");
                writer.write(insertStmt.toString());
            }
        }
    }
   /* @Scheduled(fixedRate = 5000)
    public void DownloadCurrentTime() {
    	
    }*/


}


