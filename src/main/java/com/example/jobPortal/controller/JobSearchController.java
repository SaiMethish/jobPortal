package com.example.jobPortal.controller;

import com.example.jobPortal.dto.response.JobResponseDTO;
import com.example.jobPortal.entity.JobType;
import com.example.jobPortal.service.JobSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/jobs")
@CrossOrigin(allowCredentials = "true", origins = "http://localhost:3000")
@RequiredArgsConstructor
public class JobSearchController {

    private final JobSearchService jobSearchService;

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
    @GetMapping("/search")
    public Page<JobResponseDTO> searchJobs(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String skills,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) Integer experienceMin,
            @RequestParam(required = false) Integer experienceMax,
            @RequestParam(required = false) Long salaryMin,
            @RequestParam(required = false) Long salaryMax,
            @RequestParam(required = false) JobType jobType,
            Pageable pageable) {

        return jobSearchService.searchJobs(
                title, skills, location,
                experienceMin, experienceMax,
                salaryMin, salaryMax,
                jobType, pageable);
    }
}
