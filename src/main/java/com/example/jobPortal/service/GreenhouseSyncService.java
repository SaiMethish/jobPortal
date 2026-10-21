package com.example.jobPortal.service;

import com.example.jobPortal.client.GreenhouseClient;
import com.example.jobPortal.dto.greenhouse.GreenhouseJobDTO;
import com.example.jobPortal.dto.response.SyncSummaryDTO;
import com.example.jobPortal.entity.AtsCompany;
import com.example.jobPortal.entity.Job;
import com.example.jobPortal.entity.JobStatus;
import com.example.jobPortal.entity.JobType;
import com.example.jobPortal.repository.AtsCompanyRepository;
import com.example.jobPortal.repository.JobRepository;
import com.example.jobPortal.util.IndiaLocationUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class GreenhouseSyncService {

    private final GreenhouseClient greenhouseClient;
    private final AtsCompanyRepository atsCompanyRepository;
    private final JobRepository jobRepository;
    private final com.example.jobPortal.repository.SavedJobRepository savedJobRepository;
    private final com.example.jobPortal.repository.JobApplicationRepository jobApplicationRepository;
    private final org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    private static final List<String> COMMON_TECH_SKILLS = List.of(
            "Java", "Spring Boot", "Python", "React", "Angular", "Node.js", "TypeScript",
            "JavaScript", "AWS", "Azure", "GCP", "Kubernetes", "Docker", "PostgreSQL",
            "MySQL", "MongoDB", "Redis", "Kafka", "Elasticsearch", "CI/CD", "Git",
            "REST API", "Microservices", "GraphQL", "C++", "Golang", "Rust", "Swift",
            "Kotlin", "Data Science", "Machine Learning", "AI", "DevOps", "Cybersecurity",
            "SQL", "Linux", "Terraform", "Distributed Systems"
    );

    /**
     * Seeds the initial verified list of Indian tech product & service companies on Greenhouse.
     * Inserts any company that is not yet present.
     */
    @Transactional
    public void seedDefaultCompaniesIfEmpty() {
        log.info("Verifying and seeding default Greenhouse tech companies hiring in India...");

        List<AtsCompany> seeds = List.of(
                // Mid-Level Product & High-Growth Unicorns
                AtsCompany.builder().name("Groww").slug("groww").category("PRODUCT").source("GREENHOUSE").build(),
                AtsCompany.builder().name("InMobi").slug("inmobi").category("PRODUCT").source("GREENHOUSE").build(),
                AtsCompany.builder().name("HackerRank").slug("hackerrank").category("PRODUCT").source("GREENHOUSE").build(),
                AtsCompany.builder().name("Quince").slug("quince").category("PRODUCT").source("GREENHOUSE").build(),
                AtsCompany.builder().name("Druva").slug("druva").category("PRODUCT").source("GREENHOUSE").build(),
                AtsCompany.builder().name("6sense").slug("6sense").category("PRODUCT").source("GREENHOUSE").build(),
                AtsCompany.builder().name("Cohere Health").slug("coherehealth").category("PRODUCT").source("GREENHOUSE").build(),
                AtsCompany.builder().name("CommerceIQ").slug("commerceiq").category("PRODUCT").source("GREENHOUSE").build(),
                AtsCompany.builder().name("HighRadius").slug("highradius").category("PRODUCT").source("GREENHOUSE").build(),
                AtsCompany.builder().name("Zenoti").slug("zenoti").category("PRODUCT").source("GREENHOUSE").build(),
                AtsCompany.builder().name("Slice").slug("slice").category("PRODUCT").source("GREENHOUSE").build(),
                AtsCompany.builder().name("Cockroach Labs").slug("cockroachlabs").category("PRODUCT").source("GREENHOUSE").build(),
                AtsCompany.builder().name("Apollo.io").slug("apolloio").category("PRODUCT").source("GREENHOUSE").build(),
                AtsCompany.builder().name("Observe.ai").slug("observeai").category("PRODUCT").source("GREENHOUSE").build(),
                AtsCompany.builder().name("Samsara").slug("samsara").category("PRODUCT").source("GREENHOUSE").build(),
                AtsCompany.builder().name("Pure Storage").slug("purestorage").category("PRODUCT").source("GREENHOUSE").build(),

                // Global Tech Hubs & Product Leaders in India
                AtsCompany.builder().name("Okta").slug("okta").category("PRODUCT").source("GREENHOUSE").build(),
                AtsCompany.builder().name("MongoDB").slug("mongodb").category("PRODUCT").source("GREENHOUSE").build(),
                AtsCompany.builder().name("Stripe").slug("stripe").category("PRODUCT").source("GREENHOUSE").build(),
                AtsCompany.builder().name("Rubrik").slug("rubrik").category("PRODUCT").source("GREENHOUSE").build(),
                AtsCompany.builder().name("Elastic").slug("elastic").category("PRODUCT").source("GREENHOUSE").build(),
                AtsCompany.builder().name("Datadog").slug("datadog").category("PRODUCT").source("GREENHOUSE").build(),
                AtsCompany.builder().name("Coinbase").slug("coinbase").category("PRODUCT").source("GREENHOUSE").build(),
                AtsCompany.builder().name("GitLab").slug("gitlab").category("PRODUCT").source("GREENHOUSE").build(),
                AtsCompany.builder().name("Twilio").slug("twilio").category("PRODUCT").source("GREENHOUSE").build(),
                AtsCompany.builder().name("Airbnb").slug("airbnb").category("PRODUCT").source("GREENHOUSE").build(),
                AtsCompany.builder().name("Figma").slug("figma").category("PRODUCT").source("GREENHOUSE").build(),

                // Tech Services & Consultancies
                AtsCompany.builder().name("Kaseya").slug("kaseya").category("SERVICE").source("GREENHOUSE").build(),
                AtsCompany.builder().name("Turing").slug("turing").category("SERVICE").source("GREENHOUSE").build(),
                AtsCompany.builder().name("Thoughtworks").slug("thoughtworks").category("SERVICE").source("GREENHOUSE").build(),
                AtsCompany.builder().name("Valtech").slug("valtech").category("SERVICE").source("GREENHOUSE").build(),
                AtsCompany.builder().name("Dept Agency").slug("dept").category("SERVICE").source("GREENHOUSE").build()
        );

        int newlyAdded = 0;
        for (AtsCompany seed : seeds) {
            if (!atsCompanyRepository.existsBySlug(seed.getSlug())) {
                atsCompanyRepository.save(seed);
                newlyAdded++;
            }
        }
        if (newlyAdded > 0) {
            log.info("Successfully registered {} new ATS companies.", newlyAdded);
        }
    }

    /**
     * Synchronizes all active Greenhouse companies:
     * - Ingests and upserts fresh Indian jobs
     * - Automatically removes expired jobs no longer on the employer's board
     * - Refreshes Redis caches
     */
    @CacheEvict(value = {"jobSearch", "jobDetails", "recommendedJobs"}, allEntries = true)
    public SyncSummaryDTO syncAllCompanies() {
        seedDefaultCompaniesIfEmpty();

        try {
            jdbcTemplate.execute("ALTER TABLE jobs ALTER COLUMN recruiter_profile_id DROP NOT NULL");
        } catch (Exception e) {
            log.debug("Schema notice: {}", e.getMessage());
        }

        long startTime = System.currentTimeMillis();
        List<AtsCompany> activeCompanies = atsCompanyRepository.findByActiveTrueAndSource("GREENHOUSE");

        int totalNewJobs = 0;
        int totalUpdatedJobs = 0;
        int totalExpiredJobs = 0;
        int totalIndiaJobsActive = 0;
        List<String> syncedCompanyNames = new ArrayList<>();

        log.info("Beginning Greenhouse India jobs sync for {} active companies...", activeCompanies.size());

        for (AtsCompany company : activeCompanies) {
            try {
                CompanySyncResult result = syncSingleCompany(company);
                totalNewJobs += result.newJobs;
                totalUpdatedJobs += result.updatedJobs;
                totalExpiredJobs += result.expiredJobs;
                totalIndiaJobsActive += result.activeIndiaJobs;
                syncedCompanyNames.add(company.getName());
            } catch (Exception e) {
                log.error("Error syncing company '{}' (slug: {}): {}", company.getName(), company.getSlug(), e.getMessage());
            }
        }

        long duration = System.currentTimeMillis() - startTime;
        log.info("Greenhouse Sync complete in {}ms! Total Active India Jobs: {}, New: {}, Updated: {}, Expired Removed: {}",
                duration, totalIndiaJobsActive, totalNewJobs, totalUpdatedJobs, totalExpiredJobs);

        return SyncSummaryDTO.builder()
                .totalCompaniesSynced(syncedCompanyNames.size())
                .totalIndiaJobsActive(totalIndiaJobsActive)
                .newJobsAdded(totalNewJobs)
                .jobsUpdated(totalUpdatedJobs)
                .expiredJobsRemoved(totalExpiredJobs)
                .durationMs(duration)
                .status("SUCCESS")
                .companiesSynced(syncedCompanyNames)
                .build();
    }

    /**
     * Syncs a single company board:
     * 1. Fetches current postings from Greenhouse
     * 2. Filters strictly for Indian locations
     * 3. Deletes jobs that expired / disappeared from the company board
     * 4. Upserts fresh / existing jobs
     */
    @Transactional
    public CompanySyncResult syncSingleCompany(AtsCompany company) {
        List<GreenhouseJobDTO> allJobs = greenhouseClient.fetchJobsForBoard(company.getSlug());
        
        // 1. Filter for Indian location jobs
        List<FilteredJobWrapper> indiaJobs = new ArrayList<>();
        for (GreenhouseJobDTO ghJob : allJobs) {
            if (ghJob.getLocation() != null) {
                String city = IndiaLocationUtil.extractIndianCity(ghJob.getLocation().getName());
                if (city != null) {
                    indiaJobs.add(new FilteredJobWrapper(ghJob, city));
                }
            }
        }

        Set<String> activeExternalIds = indiaJobs.stream()
                .map(w -> String.valueOf(w.dto.getId()))
                .collect(Collectors.toSet());

        // 2. Automatically remove expired jobs no longer listed on the board
        List<Job> existingDbJobs = jobRepository.findBySourceAndCompanyName("GREENHOUSE", company.getName());
        int expiredRemoved = 0;
        List<Job> jobsToDelete = new ArrayList<>();

        for (Job existing : existingDbJobs) {
            if (existing.getExternalId() != null && !activeExternalIds.contains(existing.getExternalId())) {
                jobsToDelete.add(existing);
                expiredRemoved++;
            }
        }

        if (!jobsToDelete.isEmpty()) {
            savedJobRepository.deleteByJobIn(jobsToDelete);
            jobApplicationRepository.deleteByJobIn(jobsToDelete);
            jobRepository.deleteAll(jobsToDelete);
            log.info("Company '{}': Automatically removed {} expired jobs from database.", company.getName(), jobsToDelete.size());
        }

        // 3. Upsert active jobs
        int newAdded = 0;
        int updated = 0;

        for (FilteredJobWrapper wrapper : indiaJobs) {
            GreenhouseJobDTO ghJob = wrapper.dto;
            String externalId = String.valueOf(ghJob.getId());
            Optional<Job> existingOpt = jobRepository.findBySourceAndExternalId("GREENHOUSE", externalId);

            String skills = extractSkillsFromText(ghJob.getTitle() + " " + (ghJob.getContent() != null ? ghJob.getContent() : ""));

            if (existingOpt.isPresent()) {
                Job job = existingOpt.get();
                job.setTitle(ghJob.getTitle());
                job.setDescription(ghJob.getContent());
                job.setLocation(ghJob.getLocation().getName());
                job.setCity(wrapper.city);
                job.setApplicationUrl(ghJob.getAbsoluteUrl());
                job.setSkills(skills);
                job.setStatus(JobStatus.OPEN);
                job.setUpdatedAt(LocalDateTime.now());
                jobRepository.save(job);
                updated++;
            } else {
                Job job = Job.builder()
                        .title(ghJob.getTitle())
                        .description(ghJob.getContent())
                        .location(ghJob.getLocation().getName())
                        .city(wrapper.city)
                        .companyName(company.getName())
                        .companyCategory(company.getCategory())
                        .source("GREENHOUSE")
                        .externalId(externalId)
                        .applicationUrl(ghJob.getAbsoluteUrl())
                        .status(JobStatus.OPEN)
                        .jobType(JobType.FULL_TIME)
                        .skills(skills)
                        .applicationCount(0)
                        .build();
                jobRepository.save(job);
                newAdded++;
            }
        }

        company.setLastSyncedAt(LocalDateTime.now());
        company.setActiveJobCount(activeExternalIds.size());
        atsCompanyRepository.save(company);

        return new CompanySyncResult(activeExternalIds.size(), newAdded, updated, expiredRemoved);
    }

    private String extractSkillsFromText(String text) {
        if (text == null || text.isEmpty()) {
            return "General, Software Engineering";
        }
        Set<String> matched = new LinkedHashSet<>();
        for (String skill : COMMON_TECH_SKILLS) {
            Pattern p = Pattern.compile("(?i)\\b" + Pattern.quote(skill) + "\\b");
            if (p.matcher(text).find()) {
                matched.add(skill);
            }
        }
        if (matched.isEmpty()) {
            return "Engineering, Problem Solving";
        }
        return String.join(", ", matched);
    }

    private record FilteredJobWrapper(GreenhouseJobDTO dto, String city) {}

    public record CompanySyncResult(int activeIndiaJobs, int newJobs, int updatedJobs, int expiredJobs) {}
}
