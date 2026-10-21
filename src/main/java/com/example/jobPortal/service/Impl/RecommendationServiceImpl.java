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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecommendationServiceImpl implements RecommendationService {

    private static final Logger log = LoggerFactory.getLogger(RecommendationServiceImpl.class);
    private final CandidateProfileRepository candidateProfileRepository;
    private final JobRepository jobRepository;

    @Value("${app.ai.service-url:http://localhost:8000}")
    private String aiServiceUrl;

    /** Minimum AI recommendation match score threshold (70%) */
    private static final float MIN_RECOMMENDATION_THRESHOLD = 0.70f;

    @org.springframework.cache.annotation.Cacheable(value = "recommendedJobs", key = "#candidateUser.userId")
    @Override
    public List<RecommendedJobDTO> getJobRecommendations(User candidateUser) {
        CandidateProfile profile = candidateProfileRepository.findByUser(candidateUser)
                .orElseThrow(() -> new ResourceNotFoundException("Candidate Profile not found"));

        String candidateText = buildCandidateText(profile);
        
        // Fetch up to 150 recent OPEN jobs across all sources (both DIRECT and GREENHOUSE)
        Pageable pageable = PageRequest.of(0, 150, Sort.by(Sort.Direction.DESC, "createdAt"));
        List<Job> openJobs = jobRepository.findByStatus(JobStatus.OPEN, pageable).getContent();

        if (openJobs.isEmpty()) {
            return Collections.emptyList();
        }

        // If Candidate has no extractable profile or resume text, return empty (no matches >= 70%)
        if (candidateText == null || candidateText.trim().isEmpty()) {
            log.warn("Candidate profile has no extractable text or skills. Returning 0 recommendations.");
            return Collections.emptyList();
        }

        List<String> jobTexts = openJobs.stream()
                .map(this::buildJobText)
                .collect(Collectors.toList());

        List<RecommendedJobDTO> scoredJobs = new ArrayList<>();

        try {
            SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
            requestFactory.setConnectTimeout(4000);
            requestFactory.setReadTimeout(5000);

            RestClient restClient = RestClient.builder()
                    .baseUrl(aiServiceUrl)
                    .requestFactory(requestFactory)
                    .build();

            AIRecommendationRequest requestData = AIRecommendationRequest.builder()
                    .candidateText(candidateText.length() > 3000 ? candidateText.substring(0, 3000) : candidateText)
                    .jobTexts(jobTexts.stream()
                            .map(t -> t.length() > 3000 ? t.substring(0, 3000) : t)
                            .collect(Collectors.toList()))
                    .build();

            AIRecommendationResponse response = restClient.post()
                    .uri("/recommend")
                    .body(requestData)
                    .retrieve()
                    .body(AIRecommendationResponse.class);

            if (response != null && response.getScores() != null && !response.getScores().isEmpty()) {
                List<Float> rawScores = response.getScores();
                for (int i = 0; i < openJobs.size(); i++) {
                    float raw = i < rawScores.size() ? rawScores.get(i) : 0.0f;
                    // Calibrate cosine similarity (typically 0.3 - 0.9) to intuitive percentage
                    float calibrated = Math.max(0.0f, Math.min(0.99f, (raw - 0.15f) / 0.75f));
                    float finalScore = Math.max(raw, calibrated);
                    
                    if (finalScore >= MIN_RECOMMENDATION_THRESHOLD) {
                        scoredJobs.add(RecommendedJobDTO.builder()
                                .job(mapToResponseDTO(openJobs.get(i)))
                                .score(Math.round(finalScore * 100.0f) / 100.0f)
                                .build());
                    }
                }
            } else {
                scoredJobs = calculateHeuristicRecommendations(openJobs, profile, candidateText);
            }
        } catch (Exception e) {
            log.warn("AI Service unavailable ({}). Evaluating recommendations via skill-overlap matcher.", e.getMessage());
            scoredJobs = calculateHeuristicRecommendations(openJobs, profile, candidateText);
        }

        // Return only jobs meeting or exceeding 70% recommendation, sorted by score DESC
        return scoredJobs.stream()
                .filter(rec -> rec.getScore() != null && rec.getScore() >= MIN_RECOMMENDATION_THRESHOLD)
                .sorted(Comparator.comparing(RecommendedJobDTO::getScore).reversed())
                .limit(25)
                .collect(Collectors.toList());
    }

    private List<RecommendedJobDTO> calculateHeuristicRecommendations(List<Job> openJobs, CandidateProfile profile, String candidateText) {
        List<RecommendedJobDTO> matched = new ArrayList<>();
        List<String> candSkills = profile.getSkills() != null ? profile.getSkills() : Collections.emptyList();
        Set<String> candSkillsLower = candSkills.stream().map(String::toLowerCase).collect(Collectors.toSet());

        for (Job job : openJobs) {
            float score = calculateJobMatchScore(candSkillsLower, candidateText, job);
            if (score >= MIN_RECOMMENDATION_THRESHOLD) {
                matched.add(RecommendedJobDTO.builder()
                        .job(mapToResponseDTO(job))
                        .score(Math.round(score * 100.0f) / 100.0f)
                        .build());
            }
        }
        return matched;
    }

    private float calculateJobMatchScore(Set<String> candSkills, String candidateText, Job job) {
        int matchedSkills = 0;
        int totalSkills = 0;

        if (job.getSkills() != null && !job.getSkills().isEmpty()) {
            String[] skills = job.getSkills().split("[,;/]");
            for (String s : skills) {
                String clean = s.trim().toLowerCase();
                if (!clean.isEmpty()) {
                    totalSkills++;
                    if (candSkills.contains(clean) || Pattern.compile("(?i)\\b" + Pattern.quote(clean) + "\\b").matcher(candidateText).find()) {
                        matchedSkills++;
                    }
                }
            }
        }

        if (totalSkills > 0) {
            double skillRatio = (double) matchedSkills / totalSkills;
            // Title word match boost
            boolean titleMatch = Arrays.stream(job.getTitle().toLowerCase().split("\\W+"))
                    .anyMatch(w -> w.length() > 3 && candidateText.toLowerCase().contains(w));
            double bonus = titleMatch ? 0.15 : 0.0;
            return (float) Math.min(0.98, Math.max(0.40, skillRatio * 0.85 + bonus));
        }

        return 0.50f;
    }

    private String buildCandidateText(CandidateProfile profile) {
        StringBuilder sb = new StringBuilder();
        if (profile.getResumeText() != null && !profile.getResumeText().trim().isEmpty()) {
            sb.append(profile.getResumeText()).append(" ");
        }
        if (profile.getSkills() != null && !profile.getSkills().isEmpty()) {
            sb.append(String.join(" ", profile.getSkills())).append(" ");
        }
        if (profile.getAbout() != null) {
            sb.append(profile.getAbout()).append(" ");
        }
        if (profile.getExperienceYears() != null) {
            sb.append(profile.getExperienceYears()).append(" years ");
        }
        if (profile.getLocation() != null) {
            sb.append(profile.getLocation()).append(" ");
        }
        if (profile.getEducation() != null && !profile.getEducation().isEmpty()) {
            profile.getEducation().forEach(e -> {
                if (e.getDegree() != null) sb.append(e.getDegree()).append(" ");
                if (e.getInstitution() != null) sb.append(e.getInstitution()).append(" ");
            });
        }
        return sb.toString().trim();
    }

    private String buildJobText(Job job) {
        StringBuilder sb = new StringBuilder();
        if (job.getTitle() != null) sb.append(job.getTitle()).append(" ");
        if (job.getSkills() != null) sb.append(job.getSkills()).append(" ");
        if (job.getLocation() != null) sb.append(job.getLocation()).append(" ");
        if (job.getDescription() != null) {
            String cleanDesc = job.getDescription().replaceAll("<[^>]*>", " ").replaceAll("\\s+", " ").trim();
            if (cleanDesc.length() > 500) {
                sb.append(cleanDesc.substring(0, 500)).append(" ");
            } else {
                sb.append(cleanDesc).append(" ");
            }
        }
        return sb.toString().trim();
    }
    
    private JobResponseDTO mapToResponseDTO(Job job) {
        return JobResponseDTO.builder()
                .id(job.getId())
                .recruiterProfileId(job.getRecruiterProfile() != null ? job.getRecruiterProfile().getId() : null)
                .companyName(job.getEffectiveCompanyName())
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
                .source(job.getSource())
                .externalId(job.getExternalId())
                .applicationUrl(job.getApplicationUrl())
                .companyCategory(job.getCompanyCategory())
                .city(job.getCity())
                .createdAt(job.getCreatedAt())
                .updatedAt(job.getUpdatedAt())
                .expiresAt(job.getExpiresAt())
                .build();
    }
}
