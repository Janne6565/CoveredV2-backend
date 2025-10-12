package com.janne.coveredv2;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CoveredV2Application {

	public static void main(String[] args) {
		SpringApplication.run(CoveredV2Application.class, args);
	}

}
