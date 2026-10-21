package com.example.jobPortal.service;

import com.example.jobPortal.dto.response.JobResponseDTO;
import com.example.jobPortal.entity.JobType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface JobSearchService {

    Page<JobResponseDTO> searchJobs(String title,
            String skills,
            String location,
            Integer experienceMin,
            Integer experienceMax,
            Long salaryMin,
            Long salaryMax,
            JobType jobType,
            String source,
            String companyCategory,
            String companyName,
            Pageable pageable);

    JobResponseDTO getJobById(java.util.UUID jobId);

    java.util.List<String> getDistinctCities();

    java.util.List<String> getDistinctCompanyNames();
}
