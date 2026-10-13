package com.app.hourbook;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class HourBookApplication {

	public static void main(String[] args) {
		SpringApplication.run(HourBookApplication.class, args);
	}
}