package com.example.jobPortal.controller;

import com.example.jobPortal.dto.response.JobMatchResponseDTO;
import com.example.jobPortal.service.JobMatchingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Candidate Job Matching APIs", description = "Endpoints for comparing candidate resume with jobs using AI and computing match scores.")
@RestController
@RequestMapping("/candidate/jobs")
@RequiredArgsConstructor
public class JobMatchingController {

    private final JobMatchingService jobMatchingService;

    @Operation(summary = "Compare Job with Candidate Resume", description = "Uses sentence-transformer AI embeddings to compare candidate's parsed resume with the selected job and return match score and skill breakdown.")
    @PreAuthorize("hasRole('CANDIDATE')")
    @GetMapping("/{jobId}/match")
    public JobMatchResponseDTO matchJobWithResume(
            @PathVariable UUID jobId,
            @AuthenticationPrincipal UserDetails userDetails) {
        return jobMatchingService.matchCandidateWithJob(userDetails.getUsername(), jobId);
    }
}
