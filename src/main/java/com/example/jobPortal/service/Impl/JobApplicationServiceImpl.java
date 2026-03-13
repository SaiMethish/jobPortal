package com.example.jobPortal.service.Impl;

import com.example.jobPortal.dto.request.ApplicationRequestDTO;
import com.example.jobPortal.dto.response.ApplicantResponseDTO;
import com.example.jobPortal.dto.response.ApplicationResponseDTO;
import com.example.jobPortal.entity.*;
import com.example.jobPortal.exception.BadRequestException;
import com.example.jobPortal.exception.ForbiddenException;
import com.example.jobPortal.exception.ResourceNotFoundException;
import com.example.jobPortal.repository.ApplicationStatusHistoryRepository;
import com.example.jobPortal.repository.CandidateProfileRepository;
import com.example.jobPortal.repository.JobApplicationRepository;
import com.example.jobPortal.repository.JobRepository;
import com.example.jobPortal.service.JobApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class JobApplicationServiceImpl implements JobApplicationService {

    private final JobApplicationRepository jobApplicationRepository;
    private final JobRepository jobRepository;
    private final CandidateProfileRepository candidateProfileRepository;
    private final ApplicationStatusHistoryRepository historyRepository;

    @Override
    @Transactional
    public ApplicationResponseDTO applyToJob(User user, UUID jobId, ApplicationRequestDTO dto) {
        Job job = jobRepository.findByIdAndStatus(jobId, JobStatus.OPEN)
                .orElseThrow(() -> new ResourceNotFoundException("Active job not found with id: " + jobId));

        CandidateProfile candidateProfile = candidateProfileRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Candidate profile not found"));

        if (jobApplicationRepository.existsByJobAndCandidateProfile(job, candidateProfile)) {
            throw new BadRequestException("You have already applied to this job");
        }

        // 1. Create Application
        JobApplication jobApplication = JobApplication.builder()
                .job(job)
                .candidateProfile(candidateProfile)
                .status(JobApplicationStatus.APPLIED)
                .resumeUrl(dto.getResumeUrl() != null ? dto.getResumeUrl() : candidateProfile.getResumeUrl())
                .build();

        JobApplication savedApp = jobApplicationRepository.save(jobApplication);

        // 2. Increment Counter
        job.setApplicationCount(job.getApplicationCount() + 1);
        jobRepository.save(job);

        // 3. Status History
        createStatusHistory(savedApp, null, JobApplicationStatus.APPLIED, user);

        return toApplicationResponseDTO(savedApp);
    }

    @Override
    public Page<ApplicationResponseDTO> getCandidateApplications(User user, Pageable pageable) {
        CandidateProfile candidateProfile = candidateProfileRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Candidate profile not found"));

        return jobApplicationRepository.findByCandidateProfile(candidateProfile, pageable)
                .map(this::toApplicationResponseDTO);
    }

    @Override
    public Page<ApplicantResponseDTO> getJobApplicants(User user, UUID jobId, Pageable pageable) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));

        // Verify that the recruiter owns this job
        if (!job.getRecruiterProfile().getUser().getUserId().equals(user.getUserId())) {
            throw new ForbiddenException("You are not authorized to view applicants for this job");
        }

        return jobApplicationRepository.findByJob(job, pageable)
                .map(this::toApplicantResponseDTO);
    }

    @Override
    @Transactional
    public ApplicantResponseDTO updateApplicationStatus(User user, UUID applicationId, JobApplicationStatus newStatus) {
        JobApplication application = jobApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found"));

        // 1. Verify Ownership
        if (!application.getJob().getRecruiterProfile().getUser().getUserId().equals(user.getUserId())) {
            throw new ForbiddenException("You are not authorized to update this application");
        }

        // 2. Validate Transition
        JobApplicationStatus currentStatus = application.getStatus();
        if (!isValidTransition(currentStatus, newStatus)) {
            throw new BadRequestException("Invalid status transition: " + currentStatus + " -> " + newStatus);
        }

        // 3. Update Status
        application.setStatus(newStatus);
        JobApplication updatedApp = jobApplicationRepository.save(application);

        // 4. Record History
        createStatusHistory(updatedApp, currentStatus, newStatus, user);

        return toApplicantResponseDTO(updatedApp);
    }

    // ── Helpers ──────────────────────────────────────────────

    private boolean isValidTransition(JobApplicationStatus from, JobApplicationStatus to) {
        if (from == to) return true;
        if (to == JobApplicationStatus.REJECTED) return true; // Can reject anytime

        return switch (from) {
            case APPLIED -> to == JobApplicationStatus.SHORTLISTED;
            case SHORTLISTED -> to == JobApplicationStatus.INTERVIEW;
            case INTERVIEW -> to == JobApplicationStatus.OFFERED;
            case OFFERED -> to == JobApplicationStatus.HIRED;
            default -> false;
        };
    }

    private void createStatusHistory(JobApplication app, JobApplicationStatus prev, JobApplicationStatus next, User user) {
        ApplicationStatusHistory history = ApplicationStatusHistory.builder()
                .application(app)
                .previousStatus(prev)
                .newStatus(next)
                .changedBy(user)
                .build();
        historyRepository.save(history);
    }

    // ── Mappers ─────────────────────────────────────────────

    private ApplicationResponseDTO toApplicationResponseDTO(JobApplication app) {
        return ApplicationResponseDTO.builder()
                .id(app.getId())
                .jobId(app.getJob().getId())
                .jobTitle(app.getJob().getTitle())
                .companyName(app.getJob().getRecruiterProfile().getCompanyName())
                .status(app.getStatus())
                .appliedAt(app.getAppliedAt())
                .build();
    }

    private ApplicantResponseDTO toApplicantResponseDTO(JobApplication app) {
        CandidateProfile cp = app.getCandidateProfile();
        return ApplicantResponseDTO.builder()
                .id(app.getId())
                .candidateProfileId(cp.getId())
                .candidateName(cp.getFullName())
                .candidateEmail(cp.getUser().getEmail())
                .experienceYears(cp.getExperienceYears())
                .skills(cp.getSkills())
                .resumeUrl(app.getResumeUrl())
                .status(app.getStatus())
                .appliedAt(app.getAppliedAt())
                .build();
    }
}
