package com.example.jobPortal.controller;

import com.example.jobPortal.dto.response.JobResponseDTO;
import com.example.jobPortal.entity.JobType;
import com.example.jobPortal.service.JobSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Job Search APIs", description = "Public candidate endpoints evaluating indexed Job postings and extracting single entries.")
@RestController
@RequestMapping("/jobs")
@RequiredArgsConstructor
public class JobSearchController {

    private final JobSearchService jobSearchService;
    private final com.example.jobPortal.service.RecommendationService recommendationService;
    private final com.example.jobPortal.repository.UserRepository userRepository;

    /**
     * Public search endpoint for candidates.
     * All query parameters are optional.
     *
     * Pagination: page, size
     * Sorting : sort (e.g. sort=createdAt,desc)
     *
     * Example: GET
     * /jobs/search?title=java&location=bangalore&page=0&size=10&sort=salaryMax,desc
     */
    @Operation(summary = "Search Active Jobs", description = "Query dynamic filter vectors over OPEN job architectures securely indexed via Redis caches.")
    @GetMapping("/search")
    public Page<JobResponseDTO> searchJobs(
            @Parameter(description = "Keyword title matching") @RequestParam(required = false) String title,
            @Parameter(description = "Required technical skills") @RequestParam(required = false) String skills,
            @Parameter(description = "Geographic bounds or city") @RequestParam(required = false) String location,
            @Parameter(description = "Minimum chronological experience limits") @RequestParam(required = false) Integer experienceMin,
            @Parameter(description = "Maximum chronological experience limits") @RequestParam(required = false) Integer experienceMax,
            @Parameter(description = "Floor salary bounds") @RequestParam(required = false) Long salaryMin,
            @Parameter(description = "Ceiling salary bounds") @RequestParam(required = false) Long salaryMax,
            @Parameter(description = "FULL_TIME, PART_TIME, CONTRACT constraints") @RequestParam(required = false) JobType jobType,
            @Parameter(description = "ATS Source: ALL, INTERNAL, GREENHOUSE") @RequestParam(required = false) String source,
            @Parameter(description = "Company category: PRODUCT, SERVICE") @RequestParam(required = false) String companyCategory,
            @Parameter(description = "Company name filter") @RequestParam(required = false) String companyName,
            @Parameter(description = "Pagination configuration") Pageable pageable) {

        String effectiveSource = (source != null && !source.equalsIgnoreCase("ALL")) ? source.toUpperCase() : null;

        return jobSearchService.searchJobs(
                title, skills, location,
                experienceMin, experienceMax,
                salaryMin, salaryMax,
                jobType,
                effectiveSource,
                companyCategory,
                companyName,
                pageable);
    }

    @Operation(summary = "Get Distinct Indian Cities", description = "Returns active Indian cities with available open jobs for frontend filter chips")
    @GetMapping("/cities")
    public java.util.List<String> getCities() {
        return jobSearchService.getDistinctCities();
    }

    @Operation(summary = "Get Distinct Companies", description = "Returns active employers with open jobs for frontend filter dropdown")
    @GetMapping("/companies")
    public java.util.List<String> getCompanies() {
        return jobSearchService.getDistinctCompanyNames();
    }

    @Operation(summary = "Fetch Job via ID", description = "Explicitly loads a single specific Job's parameters from Postgres using primary keys.")
    @GetMapping("/{jobId}")
    public JobResponseDTO getJobById(@Parameter(description = "UUID targeting a specific row") @PathVariable java.util.UUID jobId) {
        return jobSearchService.getJobById(jobId);
    }
    
    @Operation(summary = "Get AI Recommendations", description = "Maps semantic candidate data against Job postings natively building sorted top matches.")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('CANDIDATE')")
    @GetMapping("/recommended")
    public java.util.List<com.example.jobPortal.dto.response.RecommendedJobDTO> getRecommendations(
            @org.springframework.security.core.annotation.AuthenticationPrincipal org.springframework.security.core.userdetails.UserDetails userDetails) {

        com.example.jobPortal.entity.User user = userRepository.findByEmail(userDetails.getUsername()).orElseThrow();
        return recommendationService.getJobRecommendations(user);
    }
}
