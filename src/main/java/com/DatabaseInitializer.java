package com;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.model.DatabaseInfo;
import com.service.impl.StaticMasterData;

@Component
public class DatabaseInitializer implements CommandLineRunner {

	@Override
	public void run(String... args) throws Exception {

		System.out.println("DatabaseInitializer started...");
		loaddbDetails();
	}

	public void loaddbDetails() {
		List<DatabaseInfo> databaseList = new ArrayList<>();
		try {
			Path path = Paths.get(getClass().getClassLoader().getResource("db_info.txt").toURI());
			List<String> lines = Files.readAllLines(path);
			for (String line : lines) {
				String[] parts = line.split("`");
				if (parts.length >= 5) {
					DatabaseInfo dbInfo = new DatabaseInfo(parts[0], parts[1],parts[2], parts[3],parts[4]);
					databaseList.add(dbInfo);
				}
			}
		} catch (IOException | URISyntaxException e) {
			e.printStackTrace();
		}
		StaticMasterData.dbInfoList = databaseList;
		StaticMasterData.dbStatus = true;
	}
	
}
