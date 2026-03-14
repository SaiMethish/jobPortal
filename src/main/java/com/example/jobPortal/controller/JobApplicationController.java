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

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class JobApplicationController {

    private final JobApplicationService jobApplicationService;
    private final UserRepository userRepository;

    // ── Candidate Endpoints ─────────────────────────────────

    @PostMapping("/jobs/{jobId}/apply")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('CANDIDATE')")
    public ApplicationResponseDTO applyToJob(
            @PathVariable UUID jobId,
            @RequestBody(required = false) ApplicationRequestDTO dto,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        User user = getUserByEmail(userDetails.getUsername());
        return jobApplicationService.applyToJob(user, jobId, dto != null ? dto : new ApplicationRequestDTO());
    }

    @GetMapping("/candidate/applications")
    @PreAuthorize("hasRole('CANDIDATE')")
    public Page<ApplicationResponseDTO> getMyApplications(
            @AuthenticationPrincipal UserDetails userDetails,
            Pageable pageable) {
        
        User user = getUserByEmail(userDetails.getUsername());
        return jobApplicationService.getCandidateApplications(user, pageable);
    }

    @PatchMapping("/candidate/applications/{applicationId}/withdraw")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('CANDIDATE')")
    public void withdrawApplication(
            @PathVariable UUID applicationId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        User user = getUserByEmail(userDetails.getUsername());
        jobApplicationService.withdrawApplication(user, applicationId);
    }

    // ── Recruiter Endpoints ──────────────────────────────────

    @GetMapping("/recruiter/jobs/{jobId}/applications")
    @PreAuthorize("hasRole('RECRUITER')")
    public Page<ApplicantResponseDTO> getJobApplicants(
            @PathVariable UUID jobId,
            @AuthenticationPrincipal UserDetails userDetails,
            Pageable pageable) {
        
        User user = getUserByEmail(userDetails.getUsername());
        return jobApplicationService.getJobApplicants(user, jobId, pageable);
    }

    @PatchMapping("/recruiter/applications/{applicationId}/status")
    @PreAuthorize("hasRole('RECRUITER')")
    public ApplicantResponseDTO updateApplicationStatus(
            @PathVariable UUID applicationId,
            @RequestBody ApplicationStatusUpdateDTO dto,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        User user = getUserByEmail(userDetails.getUsername());
        return jobApplicationService.updateApplicationStatus(user, applicationId, dto.getStatus());
    }

    // ── Helpers ──────────────────────────────────────────────

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Logged in user not found"));
    }
}
