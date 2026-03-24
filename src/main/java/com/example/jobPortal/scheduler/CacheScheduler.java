package com.example.jobPortal.scheduler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class CacheScheduler {

    /**
     * Executes every 30 minutes. 
     * Forces Redis to flush volatile search arrays protecting physical memory limits.
     */
    @Scheduled(cron = "0 */30 * * * *")
    @CacheEvict(value = "jobSearch", allEntries = true)
    public void clearSearchCache() {
        log.info("Starting scheduled task: clearSearchCache. 'jobSearch' Redis payload successfully dropped.");
    }
}
