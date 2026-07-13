package com._antra.the_bridge;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class TheBridgeApplication {

	public static void main(String[] args) {
		SpringApplication.run(TheBridgeApplication.class, args);
	}

}
