package com;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling 
@ComponentScan(basePackages = {"com.service", "com.controller","com.service.impl,com"})
public class JdbcUiTaskApplication {

	public static void main(String[] args) {
		SpringApplication.run(JdbcUiTaskApplication.class, args);
	}
}
