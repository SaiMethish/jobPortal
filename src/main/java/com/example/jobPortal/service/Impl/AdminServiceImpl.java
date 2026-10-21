package com.example.jobPortal.service.Impl;

import com.example.jobPortal.dto.response.AdminStatsDTO;
import com.example.jobPortal.dto.response.AdminUserDTO;
import com.example.jobPortal.dto.response.JobResponseDTO;
import com.example.jobPortal.entity.Job;
import com.example.jobPortal.entity.JobStatus;
import com.example.jobPortal.entity.Role;
import com.example.jobPortal.entity.User;
import com.example.jobPortal.exception.ResourceNotFoundException;
import com.example.jobPortal.repository.JobApplicationRepository;
import com.example.jobPortal.repository.JobRepository;
import com.example.jobPortal.repository.UserRepository;
import com.example.jobPortal.service.AdminService;
import com.example.jobPortal.service.AuditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;
    private final JobRepository jobRepository;
    private final JobApplicationRepository jobApplicationRepository;
    private final AuditService auditService;

    @Override
    @Transactional(readOnly = true)
    public AdminStatsDTO getPlatformStats() {
        long totalUsers = userRepository.count();
        long candidateCount = userRepository.countByRole(Role.CANDIDATE);
        long recruiterCount = userRepository.countByRole(Role.RECRUITER);
        long adminCount = userRepository.countByRole(Role.ADMIN);
        long totalJobs = jobRepository.count();
        long openJobs = jobRepository.countByStatus(JobStatus.OPEN);
        long totalApplications = jobApplicationRepository.count();

        return AdminStatsDTO.builder()
                .totalUsers(totalUsers)
                .candidateCount(candidateCount)
                .recruiterCount(recruiterCount)
                .adminCount(adminCount)
                .totalJobs(totalJobs)
                .openJobs(openJobs)
                .totalApplications(totalApplications)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AdminUserDTO> getUsers(Role role, String search, Pageable pageable) {
        String cleanSearch = (search != null && !search.trim().isEmpty()) ? search.trim() : null;
        return userRepository.searchUsers(role, cleanSearch, pageable)
                .map(this::toAdminUserDTO);
    }

    @Override
    @Transactional
    public AdminUserDTO toggleUserVerification(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        user.setVerified(!user.isVerified());
        User updated = userRepository.save(user);

        log.info("Admin toggled verification for user {}: now {}", user.getEmail(), updated.isVerified());
        auditService.logAction("USER_VERIFICATION_TOGGLED", "USER", userId.toString(), "ADMIN");

        return toAdminUserDTO(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<JobResponseDTO> getAllJobs(Pageable pageable) {
        return jobRepository.findAll(pageable)
                .map(this::toJobResponseDTO);
    }

    @Override
    @Transactional
    public void deleteJob(UUID jobId) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found with id: " + jobId));

        jobRepository.delete(job);
        log.warn("Admin deleted job: id={}, title={}", jobId, job.getTitle());
        auditService.logAction("JOB_DELETED_BY_ADMIN", "JOB", jobId.toString(), "ADMIN");
    }

    private AdminUserDTO toAdminUserDTO(User user) {
        return AdminUserDTO.builder()
                .userId(user.getUserId())
                .email(user.getEmail())
                .role(user.getRole())
                .authProvider(user.getAuthProvider())
                .isVerified(user.isVerified())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
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
