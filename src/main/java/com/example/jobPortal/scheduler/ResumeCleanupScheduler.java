package com.example.jobPortal.scheduler;

import com.example.jobPortal.repository.CandidateProfileRepository;
import com.example.jobPortal.service.SupabaseStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * Scheduled garbage collector for unreferenced resumes in private Supabase Storage.
 * Runs nightly at 2:00 AM, comparing object paths in Supabase Storage against active
 * candidate profiles in PostgreSQL, deleting any orphaned files.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ResumeCleanupScheduler {

    private final CandidateProfileRepository candidateProfileRepository;
    private final SupabaseStorageService supabaseStorageService;

    @Scheduled(cron = "0 0 2 * * *") // Runs at 2 AM daily
    public void cleanUnusedResumes() {
        log.info("Starting scheduled task: Cleanup Unused Resumes in Supabase Storage");

        // 1. Fetch all legitimate active storage paths currently stored in Postgres
        List<String> usedPaths = candidateProfileRepository.findAllResumeUrls();
        if (usedPaths == null) {
            usedPaths = Collections.emptyList();
        }

        // 2. List all stored objects in the resumes bucket under candidate-resumes/
        List<String> storedPaths = supabaseStorageService.listObjects("candidate-resumes");
        if (storedPaths == null || storedPaths.isEmpty()) {
            log.info("No resume objects found in Supabase Storage.");
            return;
        }

        int deletedCount = 0;
        for (String storedPath : storedPaths) {
            // If the explicit storage path is not mapped to any active CandidateProfile
            if (!usedPaths.contains(storedPath)) {
                try {
                    supabaseStorageService.deleteFile(storedPath);
                    deletedCount++;
                    log.debug("Deleted unused resume from Supabase Storage: {}", storedPath);
                } catch (Exception e) {
                    log.error("Failed to delete unused resume from Supabase Storage: {}", storedPath, e);
                }
            }
        }

        log.info("Successfully deleted {} unused resume files from Supabase Storage.", deletedCount);
    }
}
