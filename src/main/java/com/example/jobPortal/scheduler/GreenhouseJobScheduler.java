package com.example.jobPortal.scheduler;

import com.example.jobPortal.repository.JobRepository;
import com.example.jobPortal.service.GreenhouseSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class GreenhouseJobScheduler {

    private final GreenhouseSyncService greenhouseSyncService;
    private final JobRepository jobRepository;

    /**
     * Nightly cron job to reload active India jobs and clean up expired ones.
     * Default: 2:00 AM UTC (7:30 AM IST).
     */
    @Scheduled(cron = "${app.jobs.greenhouse.sync-cron:0 0 2 * * ?}")
    public void scheduledDailySync() {
        log.info("Running scheduled daily Greenhouse jobs sync...");
        try {
            greenhouseSyncService.syncAllCompanies();
        } catch (Exception e) {
            log.error("Scheduled Greenhouse sync failed: {}", e.getMessage(), e);
        }
    }

    /**
     * On application startup, seed companies and trigger an initial sync if no
     * Greenhouse jobs are present in the database yet.
     */
    @Async
    @EventListener(ApplicationReadyEvent.class)
    public void onStartup() {
        try {
            greenhouseSyncService.seedDefaultCompaniesIfEmpty();
            long count = jobRepository.countBySource("GREENHOUSE");
            if (count == 0) {
                log.info("Initial startup: 0 Greenhouse jobs found in database. Triggering automatic initial sync...");
                greenhouseSyncService.syncAllCompanies();
            } else {
                log.info("Initial startup: {} Greenhouse jobs already present in database.", count);
            }
        } catch (Exception e) {
            log.warn("Startup Greenhouse sync check encountered an issue: {}", e.getMessage());
        }
    }
}
