package com.example.jobPortal.configs;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DatabaseMigrationConfig {

    private final JdbcTemplate jdbcTemplate;

    @PostConstruct
    public void runMigrations() {
        try {
            log.info("Running database compatibility migration: making recruiter_profile_id nullable on jobs table...");
            jdbcTemplate.execute("ALTER TABLE jobs ALTER COLUMN recruiter_profile_id DROP NOT NULL");
            log.info("Successfully dropped NOT NULL constraint on recruiter_profile_id in jobs table.");
        } catch (Exception e) {
            log.info("Notice on recruiter_profile_id migration (already applied or table newly created): {}", e.getMessage());
        }
    }
}
