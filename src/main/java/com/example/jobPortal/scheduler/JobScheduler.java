package com.example.jobPortal.scheduler;

import com.example.jobPortal.entity.Job;
import com.example.jobPortal.entity.JobStatus;
import com.example.jobPortal.repository.JobRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class JobScheduler {

    private final JobRepository jobRepository;

    @Scheduled(cron = "0 0 0 * * *") // Runs every midnight
    @CacheEvict(value = {"jobSearch", "jobDetails"}, allEntries = true)
    public void expireJobs() {
        log.info("Starting scheduled task: Expire Jobs");

        List<Job> expiredJobs = jobRepository.findByStatusAndExpiresAtBefore(
                JobStatus.OPEN,
                LocalDateTime.now()
        );

        if (expiredJobs.isEmpty()) {
            log.info("No expired jobs found. Task finished.");
            return;
        }

        for (Job job : expiredJobs) {
            job.setStatus(JobStatus.CLOSED);
            log.debug("Expired Job ID: {} titled '{}'", job.getId(), job.getTitle());
        }

        jobRepository.saveAll(expiredJobs);
        log.info("Successfully expired {} jobs and cleared Redis caches.", expiredJobs.size());
    }
}
