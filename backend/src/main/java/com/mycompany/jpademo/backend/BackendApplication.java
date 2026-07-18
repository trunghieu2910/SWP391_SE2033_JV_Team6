package com.mycompany.jpademo.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;

@EnableAsync
@EnableScheduling
@SpringBootApplication
public class BackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(BackendApplication.class, args);
    }

    @Bean
    public CommandLineRunner databaseInitializer(JdbcTemplate jdbcTemplate) {
        return args -> {
            try {
                // Try to add status column to DrugBatch table if it doesn't exist
                jdbcTemplate.execute("ALTER TABLE DrugBatch ADD status TINYINT DEFAULT 1");
                System.out.println("status column check for DrugBatch: successfully verified/added.");
            } catch (Exception e) {
                // Column might already exist, which is fine
                System.out.println("status column check for DrugBatch: " + e.getMessage());
            }
            try {
                // Try to add status column to Inventory table if it doesn't exist
                jdbcTemplate.execute("ALTER TABLE Inventory ADD status TINYINT DEFAULT 1");
                System.out.println("status column check for Inventory: successfully verified/added.");
            } catch (Exception e) {
                // Column might already exist, which is fine
                System.out.println("status column check for Inventory: " + e.getMessage());
            }
        };
    }
}
