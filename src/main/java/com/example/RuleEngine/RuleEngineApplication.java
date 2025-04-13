package com.example.RuleEngine;

import net.bytebuddy.agent.ByteBuddyAgent;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class RuleEngineApplication {

	public static void main(String[] args) {
		System.setProperty("--add-opens", "java.base/java.lang=ALL-UNNAMED");
		ByteBuddyAgent.install();
		SpringApplication.run(RuleEngineApplication.class, args);
	}

}
