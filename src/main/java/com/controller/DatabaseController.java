package com.controller;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.model.DatabaseInfo;
import com.service.DatabaseService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/api")
public class DatabaseController {

	private static final Logger logger = LoggerFactory.getLogger(DatabaseController.class);
	@Autowired
	private DatabaseService databaseService;
	@GetMapping("/")
	public String redirect() {
	    return "index.html";
	}
 @GetMapping("/db-dropdown")
	public List<DatabaseInfo> getDatabaseDropdown() {
		return databaseService.getDatabaseDropdown();
	}
	@PostMapping("/download_unsaved_csv_files_list")
    public ResponseEntity<byte[]> downloadUnsavedList(@RequestBody List<String> indexRange, @RequestParam String query) throws Exception {
        byte[] csvBytes = databaseService.convertToBytes(indexRange, query);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv"));
        headers.setContentDispositionFormData("attachment", "unsaved_csv_files_list.csv");
        return ResponseEntity.ok().headers(headers).body(csvBytes);
    }
	@PostMapping("/update-db")
	public String UpdateDatabase(@RequestBody List<String> indexRange, @RequestParam String query) throws SQLException {

		return databaseService.UpdateDatabase(indexRange, query).toString();
	}
	
	 @PostMapping("/dump/data/{dbIndex}")
	    public ResponseEntity<Void> downloadDataOnlySqlDump(@PathVariable String dbIndex, HttpServletResponse response) throws SQLException {
	        logger.info("Requested SQL dump for database index: {}", dbIndex);
	        try {
	            // Use a ByteArrayOutputStream to capture the SQL dump
	            ByteArrayOutputStream baos = new ByteArrayOutputStream();
	            databaseService.generateDataOnlySqlDump(dbIndex, baos);

	            byte[] sqlDump = baos.toByteArray();

	            // Set the response headers
	            response.setContentType("application/sql");
	            response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + dbIndex + "_data_dump.sql\"");
	            response.setContentLength(sqlDump.length); // Set Content-Length header

	            // Write the SQL dump to the response output stream
	            response.getOutputStream().write(sqlDump);
	            response.getOutputStream().flush();

	            return ResponseEntity.ok().build();
	        } catch (IOException e) {
	            logger.error("Error generating SQL dump for {}: {}", dbIndex, e.getMessage());
	            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
	        }
	    }
	 @GetMapping("/testDownload")
	    public String testDownload() {
	        databaseService.downloadAllDatabases();
	        return "Database download started";
	    }
	  



}
