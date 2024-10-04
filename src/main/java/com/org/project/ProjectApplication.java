package com.org.project;

import com.org.project.util.CustomFlywayMigrationStrategy;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class ProjectApplication {
	public static void main(String[] args) {
		SpringApplication.run(ProjectApplication.class, args);
	}

	@Bean
	public FlywayMigrationStrategy flywayMigrationStrategy() {
		return new CustomFlywayMigrationStrategy();
	}
}
