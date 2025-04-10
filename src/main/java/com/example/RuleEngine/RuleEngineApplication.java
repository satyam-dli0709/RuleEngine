package com.example.RuleEngine;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class RuleEngineApplication {

	public static void main(String[] args) {

		SpringApplication.run(RuleEngineApplication.class, args);
	}

}
