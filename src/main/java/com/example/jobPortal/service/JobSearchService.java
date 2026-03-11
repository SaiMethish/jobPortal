package com.example.jobPortal.service;

import com.example.jobPortal.dto.response.JobResponseDTO;
import com.example.jobPortal.entity.JobType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface JobSearchService {

    /** Get a single OPEN job by its ID. */
    JobResponseDTO getJobById(UUID jobId);

    Page<JobResponseDTO> searchJobs(String title,
            String skills,
            String location,
            Integer experienceMin,
            Integer experienceMax,
            Long salaryMin,
            Long salaryMax,
            JobType jobType,
            Pageable pageable);
}
