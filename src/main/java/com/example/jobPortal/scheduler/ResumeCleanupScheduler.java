package com.example.jobPortal.scheduler;

import com.example.jobPortal.repository.CandidateProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class ResumeCleanupScheduler {

    private final CandidateProfileRepository candidateProfileRepository;
    private static final String UPLOAD_DIR = "uploads/resumes/";

    @Scheduled(cron = "0 0 2 * * *") // Runs at 2 AM daily
    public void cleanUnusedResumes() {
        log.info("Starting scheduled task: Cleanup Unused Resumes");

        File folder = new File(UPLOAD_DIR);
        if (!folder.exists() || !folder.isDirectory()) {
            log.info("Resume uploads directory does not exist or is empty. Exiting cleanup.");
            return;
        }

        File[] files = folder.listFiles();
        if (files == null || files.length == 0) {
            log.info("No resume files found on disk.");
            return;
        }

        // Fetch all legitimate active URLs currently linked natively inside Postgres
        List<String> usedFiles = candidateProfileRepository.findAllResumeUrls();
        int deletedCount = 0;

        for (File file : files) {
            String checkPath = "/" + UPLOAD_DIR + file.getName();
            
            // If the explicit disk string is not mapped inside the DB array
            if (!usedFiles.contains(checkPath)) {
                boolean deleted = file.delete();
                if (deleted) {
                    deletedCount++;
                    log.debug("Deleted unused resume: {}", file.getName());
                } else {
                    log.error("Failed to delete unused resume: {}", file.getName());
                }
            }
        }

        log.info("Successfully deleted {} unused resume files from disk.", deletedCount);
    }
}
