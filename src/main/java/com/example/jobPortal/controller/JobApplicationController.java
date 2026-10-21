package com.example.jobPortal.controller;

import com.example.jobPortal.dto.request.ApplicationRequestDTO;
import com.example.jobPortal.dto.request.ApplicationStatusUpdateDTO;
import com.example.jobPortal.dto.response.ApplicantResponseDTO;
import com.example.jobPortal.dto.response.ApplicationResponseDTO;
import com.example.jobPortal.entity.User;
import com.example.jobPortal.repository.UserRepository;
import com.example.jobPortal.service.JobApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.UUID;

@Tag(name = "Job Application APIs", description = "Candidate application endpoints and Recruiter applicant screening tools mapped strictly via UUID relationships.")
@RestController
@RequiredArgsConstructor
public class JobApplicationController {

    private final JobApplicationService jobApplicationService;
    private final UserRepository userRepository;

    // ── Candidate Endpoints ─────────────────────────────────

    @Operation(summary = "Apply for Job", description = "Candidate submits DTO targeting an OPEN UUID securely instantiating new candidate-to-job relations.")
    @PostMapping("/jobs/{jobId}/apply")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('CANDIDATE')")
    public ApplicationResponseDTO applyToJob(
            @Parameter(description = "Target Job UUID") @PathVariable UUID jobId,
            @RequestBody(required = false) ApplicationRequestDTO dto,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        
        User user = getUserByEmail(userDetails.getUsername());
        return jobApplicationService.applyToJob(user, jobId, dto != null ? dto : new ApplicationRequestDTO());
    }

    @Operation(summary = "Fetch My Applications", description = "Paginates historical applications submitted by Candidate.")
    @GetMapping("/candidate/applications")
    @PreAuthorize("hasRole('CANDIDATE')")
    public Page<ApplicationResponseDTO> getMyApplications(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "Pagination config") Pageable pageable) {
        
        User user = getUserByEmail(userDetails.getUsername());
        return jobApplicationService.getCandidateApplications(user, pageable);
    }

    @Operation(summary = "Withdraw Application", description = "Removes active candidate applications securely against candidate IDs.")
    @PatchMapping("/candidate/applications/{applicationId}/withdraw")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('CANDIDATE')")
    public void withdrawApplication(
            @Parameter(description = "Target Application UUID") @PathVariable UUID applicationId,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        
        User user = getUserByEmail(userDetails.getUsername());
        jobApplicationService.withdrawApplication(user, applicationId);
    }

    // ── Recruiter Endpoints ──────────────────────────────────

    @Operation(summary = "Fetch Job Applicants", description = "Recruiters pull paged Application statuses cross-referenced under exclusively owned Jobs.")
    @GetMapping("/recruiter/jobs/{jobId}/applications")
    @PreAuthorize("hasRole('RECRUITER')")
    public Page<ApplicantResponseDTO> getJobApplicants(
            @Parameter(description = "Target Job UUID") @PathVariable UUID jobId,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "Pagination config") Pageable pageable) {
        
        User user = getUserByEmail(userDetails.getUsername());
        return jobApplicationService.getJobApplicants(user, jobId, pageable);
    }

    @Operation(summary = "Update Applicant Status", description = "Allows Recruiter to mutate candidate states systematically (e.g. ACCEPTED, REJECTED).")
    @PatchMapping("/recruiter/applications/{applicationId}/status")
    @PreAuthorize("hasRole('RECRUITER')")
    public ApplicantResponseDTO updateApplicationStatus(
            @Parameter(description = "Target Application UUID") @PathVariable UUID applicationId,
            @RequestBody ApplicationStatusUpdateDTO dto,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        
        User user = getUserByEmail(userDetails.getUsername());
        return jobApplicationService.updateApplicationStatus(user, applicationId, dto.getStatus());
    }

    // ── Helpers ──────────────────────────────────────────────

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Logged in user not found"));
    }
}
