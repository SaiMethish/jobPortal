package com.example.jobPortal.controller;

import com.example.jobPortal.dto.response.AdminStatsDTO;
import com.example.jobPortal.dto.response.AdminUserDTO;
import com.example.jobPortal.dto.response.JobResponseDTO;
import com.example.jobPortal.entity.Role;
import com.example.jobPortal.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Admin Console APIs", description = "Privileged administrator operations for platform metrics, user management, and job moderation.")
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    @Operation(summary = "Get Platform Metrics", description = "Aggregates core platform statistics including user counts, jobs, and applications.")
    @GetMapping("/stats")
    public AdminStatsDTO getStats() {
        return adminService.getPlatformStats();
    }

    @Operation(summary = "Search and Filter Users", description = "Retrieves paginated user directory with role and keyword email filters.")
    @GetMapping("/users")
    public Page<AdminUserDTO> getUsers(
            @RequestParam(required = false) Role role,
            @RequestParam(required = false) String search,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return adminService.getUsers(role, search, pageable);
    }

    @Operation(summary = "Toggle User Verification / Status", description = "Enables or disables user account verification status.")
    @PatchMapping("/users/{userId}/toggle-verify")
    public AdminUserDTO toggleUserVerification(@PathVariable UUID userId) {
        return adminService.toggleUserVerification(userId);
    }

    @Operation(summary = "List All Job Postings for Moderation", description = "Loads all jobs across all recruiters for audit and moderation.")
    @GetMapping("/jobs")
    public Page<JobResponseDTO> getAllJobs(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return adminService.getAllJobs(pageable);
    }

    @Operation(summary = "Delete / Remove Job Posting", description = "Permanently removes a fraudulent or inappropriate job posting from the platform.")
    @DeleteMapping("/jobs/{jobId}")
    public ResponseEntity<Void> deleteJob(@PathVariable UUID jobId) {
        adminService.deleteJob(jobId);
        return ResponseEntity.noContent().build();
    }
}
