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
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/recruiter/jobs")
@CrossOrigin(allowCredentials = "true", origins = "http://localhost:3000")
@RequiredArgsConstructor
@PreAuthorize("hasRole('RECRUITER')")
public class RecruiterJobController {

    private final JobService jobService;
    private final UserRepository userRepository;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public JobResponseDTO createJob(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody JobRequestDTO dto) {
        User user = userRepository.findByEmail(userDetails.getUsername()).orElseThrow();
        return jobService.createJob(user, dto);
    }

    @PutMapping("/{jobId}")
    public JobResponseDTO updateJob(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID jobId,
            @Valid @RequestBody JobRequestDTO dto) {
        User user = userRepository.findByEmail(userDetails.getUsername()).orElseThrow();
        return jobService.updateJob(user, jobId, dto);
    }

    @PatchMapping("/{jobId}/close")
    public JobResponseDTO closeJob(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID jobId) {
        User user = userRepository.findByEmail(userDetails.getUsername()).orElseThrow();
        return jobService.closeJob(user, jobId);
    }

    @GetMapping
    public List<JobResponseDTO> getMyJobs(
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername()).orElseThrow();
        return jobService.getMyJobs(user);
    }
}
