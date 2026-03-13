package com.example.jobPortal.service;

import com.example.jobPortal.dto.request.ApplicationRequestDTO;
import com.example.jobPortal.dto.response.ApplicantResponseDTO;
import com.example.jobPortal.dto.response.ApplicationResponseDTO;
import com.example.jobPortal.entity.JobApplicationStatus;
import com.example.jobPortal.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface JobApplicationService {

    /** Candidate: Apply to a specific job. */
    ApplicationResponseDTO applyToJob(User user, UUID jobId, ApplicationRequestDTO dto);

    /** Candidate: View all my applications. */
    Page<ApplicationResponseDTO> getCandidateApplications(User user, Pageable pageable);

    /** Recruiter: View all applicants for a job they posted. */
    Page<ApplicantResponseDTO> getJobApplicants(User user, UUID jobId, Pageable pageable);

    /** Recruiter: Update the status of an application. */
    ApplicantResponseDTO updateApplicationStatus(User user, UUID applicationId, JobApplicationStatus status);
}
