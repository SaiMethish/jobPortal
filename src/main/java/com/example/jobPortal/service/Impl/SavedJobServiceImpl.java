package com.example.jobPortal.service.Impl;

import com.example.jobPortal.dto.response.JobResponseDTO;
import com.example.jobPortal.entity.CandidateProfile;
import com.example.jobPortal.entity.Job;
import com.example.jobPortal.entity.SavedJob;
import com.example.jobPortal.entity.User;
import com.example.jobPortal.exception.BadRequestException;
import com.example.jobPortal.exception.ResourceNotFoundException;
import com.example.jobPortal.repository.CandidateProfileRepository;
import com.example.jobPortal.repository.JobRepository;
import com.example.jobPortal.repository.SavedJobRepository;
import com.example.jobPortal.service.SavedJobService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SavedJobServiceImpl implements SavedJobService {

    private final SavedJobRepository savedJobRepository;
    private final CandidateProfileRepository candidateProfileRepository;
    private final JobRepository jobRepository;

    @Override
    @Transactional
    public void saveJob(User user, UUID jobId) {
        CandidateProfile candidateProfile = getCandidateProfile(user);
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));

        if (savedJobRepository.existsByCandidateProfileAndJob(candidateProfile, job)) {
            log.warn("Candidate {} attempted to save already saved job {}", user.getEmail(), jobId);
            throw new BadRequestException("Job is already saved");
        }

        SavedJob savedJob = SavedJob.builder()
                .candidateProfile(candidateProfile)
                .job(job)
                .build();
                
        savedJobRepository.save(savedJob);
        log.info("Candidate {} saved job {}", user.getEmail(), job.getTitle());
    }

    @Override
    public List<JobResponseDTO> getSavedJobs(User user) {
        CandidateProfile candidateProfile = getCandidateProfile(user);
        return savedJobRepository.findByCandidateProfile(candidateProfile)
                .stream()
                .map(saved -> toJobResponseDTO(saved.getJob()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void unsaveJob(User user, UUID jobId) {
        CandidateProfile candidateProfile = getCandidateProfile(user);
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));

        if (!savedJobRepository.existsByCandidateProfileAndJob(candidateProfile, job)) {
            throw new BadRequestException("Job is not currently saved");
        }

        savedJobRepository.deleteByCandidateProfileAndJob(candidateProfile, job);
        log.info("Candidate {} unsaved job {}", user.getEmail(), job.getTitle());
    }
    
    // ── Helpers ──────────────────────────────────────────────

    private CandidateProfile getCandidateProfile(User user) {
        return candidateProfileRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Candidate profile not found"));
    }

    private JobResponseDTO toJobResponseDTO(Job job) {
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
