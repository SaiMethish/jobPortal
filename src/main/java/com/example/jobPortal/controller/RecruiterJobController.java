package com.example.jobPortal.controller;

import com.example.jobPortal.dto.request.JobRequestDTO;
import com.example.jobPortal.dto.response.JobResponseDTO;
import com.example.jobPortal.entity.User;
import com.example.jobPortal.repository.UserRepository;
import com.example.jobPortal.service.JobService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;
import java.util.UUID;

@Tag(name = "Recruiter Job Management APIs", description = "Authoritative interfaces for Recruiters to explicitly Post, Edit, and Expire Jobs organically mapping JWT identities.")
@RestController
@RequestMapping("/recruiter/jobs")
@CrossOrigin(allowCredentials = "true", origins = "http://localhost:4200")
@RequiredArgsConstructor
@PreAuthorize("hasRole('RECRUITER')")
public class RecruiterJobController {

    private final JobService jobService;
    private final UserRepository userRepository;

    @Operation(summary = "Create Job Post", description = "Validates the HTTP Body logically establishing a new OPEN tracking cycle explicitly owned by the Recruiter.")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public JobResponseDTO createJob(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody JobRequestDTO dto) {
        User user = userRepository.findByEmail(userDetails.getUsername()).orElseThrow();
        return jobService.createJob(user, dto);
    }

    @Operation(summary = "Update Job Architecture", description = "Rewrites existing parameters strictly verifying ownership parameters seamlessly against explicit IDs.")
    @PutMapping("/{jobId}")
    public JobResponseDTO updateJob(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "Target Job UUID") @PathVariable UUID jobId,
            @Valid @RequestBody JobRequestDTO dto) {
        User user = userRepository.findByEmail(userDetails.getUsername()).orElseThrow();
        return jobService.updateJob(user, jobId, dto);
    }

    @Operation(summary = "Close Job Posting", description = "Forces JobStatus configurations into chronological terminal closures preventing Candidates from matching.")
    @PatchMapping("/{jobId}/close")
    public JobResponseDTO closeJob(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "Target Job UUID") @PathVariable UUID jobId) {
        User user = userRepository.findByEmail(userDetails.getUsername()).orElseThrow();
        return jobService.closeJob(user, jobId);
    }

    @Operation(summary = "Fetch My Tracked Jobs", description = "Extracts all chronological Jobs exclusively owned by the extracting Client Token implicitly.")
    @GetMapping
    public List<JobResponseDTO> getMyJobs(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername()).orElseThrow();
        return jobService.getMyJobs(user);
    }
}
