package com.example.jobPortal.controller;

import com.example.jobPortal.dto.response.JobResponseDTO;
import com.example.jobPortal.entity.User;
import com.example.jobPortal.service.SavedJobService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.userdetails.UserDetails;
import com.example.jobPortal.repository.UserRepository;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Tag(name = "Saved Jobs API", description = "Endpoints for candidates to save and unsave wishlist jobs")
public class SavedJobController {

    private final SavedJobService savedJobService;
    private final UserRepository userRepository;

    @PostMapping("/jobs/{jobId}/save")
    @PreAuthorize("hasRole('CANDIDATE')")
    @Operation(summary = "Save a job", description = "Allows a candidate to bookmark a job.")
    public ResponseEntity<Void> saveJob(@AuthenticationPrincipal UserDetails userDetails, @PathVariable UUID jobId) {
        User user = userRepository.findByEmail(userDetails.getUsername()).orElseThrow();
        savedJobService.saveJob(user, jobId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/jobs/{jobId}/unsave")
    @PreAuthorize("hasRole('CANDIDATE')")
    @Operation(summary = "Unsave a job", description = "Allows a candidate to remove a job from their saved list.")
    public ResponseEntity<Void> unsaveJob(@AuthenticationPrincipal UserDetails userDetails, @PathVariable UUID jobId) {
        User user = userRepository.findByEmail(userDetails.getUsername()).orElseThrow();
        savedJobService.unsaveJob(user, jobId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/candidate/saved-jobs")
    @PreAuthorize("hasRole('CANDIDATE')")
    @Operation(summary = "Get saved jobs", description = "Retrieves all jobs saved by the authenticated candidate.")
    public ResponseEntity<List<JobResponseDTO>> getSavedJobs(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername()).orElseThrow();
        return ResponseEntity.ok(savedJobService.getSavedJobs(user));
    }
}
