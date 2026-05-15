package com.codearena.code_arena_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@ConfigurationPropertiesScan
public class CodeArenaBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(CodeArenaBackendApplication.class, args);
	}

}
