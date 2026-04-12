package com.example.jobPortal.service.Impl;

import com.example.jobPortal.dto.request.AIRecommendationRequest;
import com.example.jobPortal.dto.response.AIRecommendationResponse;
import com.example.jobPortal.dto.response.JobResponseDTO;
import com.example.jobPortal.dto.response.RecommendedJobDTO;
import com.example.jobPortal.entity.CandidateProfile;
import com.example.jobPortal.entity.Job;
import com.example.jobPortal.entity.JobStatus;
import com.example.jobPortal.entity.User;
import com.example.jobPortal.exception.ResourceNotFoundException;
import com.example.jobPortal.repository.CandidateProfileRepository;
import com.example.jobPortal.repository.JobRepository;
import com.example.jobPortal.service.RecommendationService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecommendationServiceImpl implements RecommendationService {

    private static final Logger log = LoggerFactory.getLogger(RecommendationServiceImpl.class);
    private final CandidateProfileRepository candidateProfileRepository;
    private final JobRepository jobRepository;
    private final RestClient restClient = RestClient.builder().baseUrl("http://localhost:8000").build();

    @org.springframework.cache.annotation.Cacheable(value = "recommendedJobs", key = "#candidateUser.userId")
    @Override
    public List<RecommendedJobDTO> getJobRecommendations(User candidateUser) {
        CandidateProfile profile = candidateProfileRepository.findByUser(candidateUser)
                .orElseThrow(() -> new ResourceNotFoundException("Candidate Profile not found"));

        String candidateText = buildCandidateText(profile);
        
        // Fetch Top 50 recent OPEN jobs
        Pageable topFifty = PageRequest.of(0, 50, Sort.by(Sort.Direction.DESC, "createdAt"));
        List<Job> recentJobs = jobRepository.findByStatus(JobStatus.OPEN, topFifty).getContent();

        if (recentJobs.isEmpty()) {
            return new ArrayList<>();
        }

        // Fallback: If Candidate has no matching text data, simply return latest as Fallback
        if (candidateText == null || candidateText.trim().isEmpty()) {
            log.warn("Candidate profile has no extractable text. Falling back to latest jobs.");
            return fallbackLatestJobs(recentJobs);
        }

        List<String> jobTexts = recentJobs.stream()
                .map(this::buildJobText)
                .collect(Collectors.toList());

        AIRecommendationRequest requestData = AIRecommendationRequest.builder()
                .candidateText(candidateText)
                .jobTexts(jobTexts)
                .build();

        try {
            AIRecommendationResponse response = restClient.post()
                    .uri("/recommend")
                    .body(requestData)
                    .retrieve()
                    .body(AIRecommendationResponse.class);

            if (response == null || response.getScores() == null || response.getScores().isEmpty()) {
                throw new IllegalStateException("Invalid response from AI Service");
            }

            return mapAndSortRecommendations(recentJobs, response.getScores());

        } catch (RestClientException | IllegalStateException e) {
            log.error("AI Recommendation Service failed: {}. Falling back to Rule-based/Latest logic.", e.getMessage());
            return fallbackLatestJobs(recentJobs);
        }
    }

    private String buildCandidateText(CandidateProfile profile) {
        StringBuilder sb = new StringBuilder();
        if (profile.getSkills() != null && !profile.getSkills().isEmpty()) {
            sb.append(String.join(" ", profile.getSkills())).append(" ");
        }
        if (profile.getExperienceYears() != null) {
            sb.append(profile.getExperienceYears()).append(" years ").append(" ");
        }
        if (profile.getLocation() != null) {
            sb.append(profile.getLocation()).append(" ");
        }
        if (profile.getEducation() != null) {
            sb.append(profile.getEducation()).append(" ");
        }
        return sb.toString().trim();
    }

    private String buildJobText(Job job) {
        StringBuilder sb = new StringBuilder();
        if (job.getTitle() != null) sb.append(job.getTitle()).append(" ");
        if (job.getSkills() != null) sb.append(job.getSkills()).append(" ");
        if (job.getLocation() != null) sb.append(job.getLocation()).append(" ");
        // We append a truncated description if required, but Title + Skills usually carries the most weight in semantic search
        return sb.toString().trim();
    }

    private List<RecommendedJobDTO> mapAndSortRecommendations(List<Job> jobs, List<Float> scores) {
        List<RecommendedJobDTO> recommendations = new ArrayList<>();
        
        for (int i = 0; i < jobs.size(); i++) {
            float score = i < scores.size() ? scores.get(i) : 0.0f;
            Job job = jobs.get(i);
            
            recommendations.add(RecommendedJobDTO.builder()
                    .job(mapToResponseDTO(job))
                    .score(score)
                    .build());
        }

        // Sort by Score DESC, limit top 10
        return recommendations.stream()
                .sorted(Comparator.comparing(RecommendedJobDTO::getScore).reversed())
                .limit(10)
                .collect(Collectors.toList());
    }

    private List<RecommendedJobDTO> fallbackLatestJobs(List<Job> recentJobs) {
        // Without AI, return as 1.0 down to lower bound for presentation formatting natively
        return recentJobs.stream()
                .limit(10)
                .map(job -> RecommendedJobDTO.builder()
                        .job(mapToResponseDTO(job))
                        .score(0.0f) // Denotes un-scored fallback
                        .build())
                .collect(Collectors.toList());
    }
    
    private JobResponseDTO mapToResponseDTO(Job job) {
        return JobResponseDTO.builder()
                .id(job.getId())
                .recruiterProfileId(job.getRecruiterProfile().getId())
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
