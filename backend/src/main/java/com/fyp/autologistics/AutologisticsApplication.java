package com.fyp.autologistics;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AutologisticsApplication {

	public static void main(String[] args) {
		SpringApplication.run(AutologisticsApplication.class, args);
	}

}
