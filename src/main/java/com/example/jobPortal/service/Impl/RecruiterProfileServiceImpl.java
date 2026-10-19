package com.example.jobPortal.service.Impl;

import com.example.jobPortal.dto.request.RecruiterProfileRequestDTO;
import com.example.jobPortal.dto.response.CompanyDetailDTO;
import com.example.jobPortal.dto.response.JobResponseDTO;
import com.example.jobPortal.dto.response.RecruiterProfileResponseDTO;
import com.example.jobPortal.entity.Job;
import com.example.jobPortal.entity.JobStatus;
import com.example.jobPortal.entity.RecruiterProfile;
import com.example.jobPortal.entity.User;
import com.example.jobPortal.exception.ResourceNotFoundException;
import com.example.jobPortal.mapper.RecruiterProfileMapper;
import com.example.jobPortal.repository.JobRepository;
import com.example.jobPortal.repository.RecruiterProfileRepository;
import com.example.jobPortal.service.RecruiterProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecruiterProfileServiceImpl implements RecruiterProfileService {

    private final RecruiterProfileRepository repo;
    private final RecruiterProfileMapper mapper;
    private final JobRepository jobRepository;

    @Override
    public RecruiterProfileResponseDTO getRecruiterProfileByUser(User user) {
        RecruiterProfile profile = repo.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Recruiter profile not found"));
        return mapper.toResponseDTO(profile);
    }

    @Override
    public RecruiterProfileResponseDTO updateRecruiterProfile(User user, RecruiterProfileRequestDTO dto) {
        RecruiterProfile profile = repo.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Recruiter profile not found"));

        profile.setCompanyName(dto.getCompanyName());
        profile.setCompanyWebsite(dto.getCompanyWebsite());
        profile.setCompanyDescription(dto.getCompanyDescription());
        profile.setIndustry(dto.getIndustry());
        profile.setLocation(dto.getLocation());

        return mapper.toResponseDTO(repo.save(profile));
    }

    @Override
    public CompanyDetailDTO getCompanyDetailById(UUID id) {
        RecruiterProfile profile = repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Company profile not found with id: " + id));

        List<Job> jobs = jobRepository.findByRecruiterProfile(profile);
        List<JobResponseDTO> openJobDTOs = jobs.stream()
                .filter(j -> j.getStatus() == JobStatus.OPEN)
                .map(this::toJobResponseDTO)
                .collect(Collectors.toList());

        return CompanyDetailDTO.builder()
                .id(profile.getId())
                .userId(profile.getUser() != null ? profile.getUser().getUserId() : null)
                .companyName(profile.getCompanyName())
                .companyWebsite(profile.getCompanyWebsite())
                .companyDescription(profile.getCompanyDescription())
                .industry(profile.getIndustry())
                .location(profile.getLocation())
                .openJobs(openJobDTOs)
                .totalJobsCount(jobs.size())
                .createdAt(profile.getCreatedAt())
                .updatedAt(profile.getUpdatedAt())
                .build();
    }

    private JobResponseDTO toJobResponseDTO(Job job) {
        return JobResponseDTO.builder()
                .id(job.getId())
                .recruiterProfileId(job.getRecruiterProfile() != null ? job.getRecruiterProfile().getId() : null)
                .companyName(job.getRecruiterProfile() != null ? job.getRecruiterProfile().getCompanyName() : null)
                .title(job.getTitle())
                .description(job.getDescription())
                .skills(job.getSkills())
                .experienceMin(job.getExperienceMin())
                .experienceMax(job.getExperienceMax())
                .salaryMin(job.getSalaryMin())
                .salaryMax(job.getSalaryMax())
                .location(job.getLocation())
                .jobType(job.getJobType())
                .status(job.getStatus())
                .applicationCount(job.getApplicationCount())
                .createdAt(job.getCreatedAt())
                .updatedAt(job.getUpdatedAt())
                .expiresAt(job.getExpiresAt())
                .build();
    }
}
