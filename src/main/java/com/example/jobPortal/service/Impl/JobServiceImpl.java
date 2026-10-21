package com.example.jobPortal.service.Impl;

import com.example.jobPortal.dto.request.JobRequestDTO;
import com.example.jobPortal.dto.response.JobResponseDTO;
import com.example.jobPortal.entity.Job;
import com.example.jobPortal.entity.JobStatus;
import com.example.jobPortal.entity.RecruiterProfile;
import com.example.jobPortal.entity.User;
import com.example.jobPortal.exception.ForbiddenException;
import com.example.jobPortal.exception.ResourceNotFoundException;
import com.example.jobPortal.repository.JobRepository;
import com.example.jobPortal.repository.RecruiterProfileRepository;
import com.example.jobPortal.service.JobService;
import com.example.jobPortal.service.AuditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class JobServiceImpl implements JobService {

    private final JobRepository jobRepository;
    private final RecruiterProfileRepository recruiterProfileRepository;
    private final AuditService auditService;

    @Override
    @CacheEvict(value = {"jobSearch", "recommendedJobs"}, allEntries = true)
    public JobResponseDTO createJob(User user, JobRequestDTO dto) {
        RecruiterProfile recruiterProfile = getRecruiterProfile(user);

        Job job = Job.builder()
                .recruiterProfile(recruiterProfile)
                .title(dto.getTitle())
                .description(dto.getDescription())
                .skills(dto.getSkills())
                .experienceMin(dto.getExperienceMin())
                .experienceMax(dto.getExperienceMax())
                .salaryMin(dto.getSalaryMin())
                .salaryMax(dto.getSalaryMax())
                .location(dto.getLocation())
                .jobType(dto.getJobType())
                .status(JobStatus.OPEN)
                .expiresAt(java.time.LocalDateTime.now().plusDays(30))
                .build();

        Job savedJob = jobRepository.save(job);
        
        log.info("Recruiter {} is creating a job: {}", user.getEmail(), savedJob.getTitle());
        auditService.logAction("JOB_CREATED", "JOB", savedJob.getId().toString(), user.getEmail());
        
        return toResponseDTO(savedJob);
    }

    @Override
    @CacheEvict(value = {"jobSearch", "jobDetails", "recommendedJobs"}, allEntries = true)
    public JobResponseDTO updateJob(User user, UUID jobId, JobRequestDTO dto) {
        RecruiterProfile recruiterProfile = getRecruiterProfile(user);
        Job job = getJobById(jobId);

        verifyOwnership(job, recruiterProfile);

        job.setTitle(dto.getTitle());
        job.setDescription(dto.getDescription());
        job.setSkills(dto.getSkills());
        job.setExperienceMin(dto.getExperienceMin());
        job.setExperienceMax(dto.getExperienceMax());
        job.setSalaryMin(dto.getSalaryMin());
        job.setSalaryMax(dto.getSalaryMax());
        job.setLocation(dto.getLocation());
        job.setJobType(dto.getJobType());

        return toResponseDTO(jobRepository.save(job));
    }

    @Override
    @CacheEvict(value = {"jobSearch", "jobDetails", "recommendedJobs"}, allEntries = true)
    public JobResponseDTO closeJob(User user, UUID jobId) {
        RecruiterProfile recruiterProfile = getRecruiterProfile(user);
        Job job = getJobById(jobId);

        verifyOwnership(job, recruiterProfile);

        job.setStatus(JobStatus.CLOSED);
        return toResponseDTO(jobRepository.save(job));
    }

    @Override
    public List<JobResponseDTO> getMyJobs(User user) {
        RecruiterProfile recruiterProfile = getRecruiterProfile(user);
        return jobRepository.findByRecruiterProfile(recruiterProfile)
                .stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    // ── Helper Methods ───────────────────────────────────────

    private RecruiterProfile getRecruiterProfile(User user) {
        return recruiterProfileRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Recruiter profile not found"));
    }

    private Job getJobById(UUID jobId) {
        return jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found with id: " + jobId));
    }

    private void verifyOwnership(Job job, RecruiterProfile recruiterProfile) {
        if (!job.getRecruiterProfile().getId().equals(recruiterProfile.getId())) {
            throw new ForbiddenException("You are not authorized to modify this job");
        }
    }

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
