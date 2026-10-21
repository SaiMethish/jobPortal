package com.example.jobPortal.service.Impl;

import com.example.jobPortal.dto.response.JobResponseDTO;
import com.example.jobPortal.entity.Job;
import com.example.jobPortal.entity.JobStatus;
import com.example.jobPortal.entity.JobType;
import com.example.jobPortal.repository.JobRepository;
import com.example.jobPortal.service.JobSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JobSearchServiceImpl implements JobSearchService {

    private final JobRepository jobRepository;

    @Override
    @Cacheable(value = "jobSearch")
    public Page<JobResponseDTO> searchJobs(String title,
            String skills,
            String location,
            Integer experienceMin,
            Integer experienceMax,
            Long salaryMin,
            Long salaryMax,
            JobType jobType,
            String source,
            String companyCategory,
            String companyName,
            Pageable pageable) {

        Page<Job> jobs = jobRepository.searchJobs(
                JobStatus.OPEN,
                title,
                skills,
                location,
                experienceMin,
                experienceMax,
                salaryMin,
                salaryMax,
                jobType,
                source,
                companyCategory,
                companyName,
                pageable);

        return jobs.map(this::toResponseDTO);
    }

    @Override
    @Cacheable(value = "jobDetails", key = "#jobId")
    public JobResponseDTO getJobById(java.util.UUID jobId) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new com.example.jobPortal.exception.ResourceNotFoundException("Job not found"));
        return toResponseDTO(job);
    }

    @Override
    public java.util.List<String> getDistinctCities() {
        return jobRepository.findDistinctCities();
    }

    @Override
    public java.util.List<String> getDistinctCompanyNames() {
        return jobRepository.findDistinctCompanyNames();
    }

    // ── Helper ───────────────────────────────────────────────
    private JobResponseDTO toResponseDTO(Job job) {
        return JobResponseDTO.builder()
                .id(job.getId())
                .recruiterProfileId(job.getRecruiterProfile() != null ? job.getRecruiterProfile().getId() : null)
                .companyName(job.getEffectiveCompanyName())
                .title(job.getTitle())
                .description(job.getDescription())
                .skills(job.getSkills())
                .experienceMin(job.getExperienceMin())
                .experienceMax(job.getExperienceMax())
                .salaryMin(job.getSalaryMin())
                .salaryMax(job.getSalaryMax())
                .location(job.getLocation())
                .jobType(job.getJobType())
                .status(job.getStatus())
                .applicationCount(job.getApplicationCount())
                .source(job.getSource())
                .externalId(job.getExternalId())
                .applicationUrl(job.getApplicationUrl())
                .companyCategory(job.getCompanyCategory())
                .city(job.getCity())
                .createdAt(job.getCreatedAt())
                .updatedAt(job.getUpdatedAt())
                .expiresAt(job.getExpiresAt())
                .build();
    }
}
