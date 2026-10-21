package com.example.jobPortal.service;

import com.example.jobPortal.dto.request.AIRecommendationRequest;
import com.example.jobPortal.dto.response.AIRecommendationResponse;
import com.example.jobPortal.dto.response.JobMatchResponseDTO;
import com.example.jobPortal.entity.CandidateProfile;
import com.example.jobPortal.entity.Job;
import com.example.jobPortal.entity.User;
import com.example.jobPortal.exception.ResourceNotFoundException;
import com.example.jobPortal.repository.CandidateProfileRepository;
import com.example.jobPortal.repository.JobRepository;
import com.example.jobPortal.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class JobMatchingService {

    private final UserRepository userRepository;
    private final CandidateProfileRepository candidateProfileRepository;
    private final JobRepository jobRepository;

    @Value("${app.ai-service.url:http://localhost:8000}")
    private String aiServiceUrl;

    public JobMatchResponseDTO matchCandidateWithJob(String candidateEmail, UUID jobId) {
        User user = userRepository.findByEmail(candidateEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        CandidateProfile profile = candidateProfileRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Candidate profile not found"));

        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));

        String resumeText = profile.getResumeText();
        List<String> candidateSkills = profile.getSkills() != null ? profile.getSkills() : Collections.emptyList();

        boolean hasResumeContent = (resumeText != null && !resumeText.trim().isEmpty()) || !candidateSkills.isEmpty();
        if (!hasResumeContent) {
            return JobMatchResponseDTO.builder()
                    .jobId(job.getId())
                    .jobTitle(job.getTitle())
                    .companyName(job.getEffectiveCompanyName())
                    .hasResume(false)
                    .matchScore(0.0)
                    .matchingSkills(Collections.emptyList())
                    .missingSkills(Collections.emptyList())
                    .applicationUrl(job.getApplicationUrl())
                    .summary("Please upload your resume in the Profile section to see your AI match score.")
                    .build();
        }

        // Build candidate profile text for AI embedding
        StringBuilder candidateTextBuilder = new StringBuilder();
        if (resumeText != null && !resumeText.trim().isEmpty()) {
            candidateTextBuilder.append(resumeText).append(" ");
        }
        if (!candidateSkills.isEmpty()) {
            candidateTextBuilder.append(String.join(" ", candidateSkills)).append(" ");
        }
        if (profile.getExperienceYears() != null) {
            candidateTextBuilder.append(profile.getExperienceYears()).append(" years experience ");
        }

        String candidateText = candidateTextBuilder.toString().trim();
        String jobText = (job.getTitle() + " " + (job.getSkills() != null ? job.getSkills() : "") + " "
                + (job.getDescription() != null ? stripHtml(job.getDescription()) : "")).trim();

        double matchScore = calculateMatchScoreWithAi(candidateText, jobText);

        // Analyze matching and missing skills
        SkillAnalysisResult skillAnalysis = analyzeSkills(candidateSkills, candidateText, job.getSkills(), job.getDescription());

        String summary = generateMatchSummary(matchScore, skillAnalysis.matchingSkills, skillAnalysis.missingSkills);

        return JobMatchResponseDTO.builder()
                .jobId(job.getId())
                .jobTitle(job.getTitle())
                .companyName(job.getEffectiveCompanyName())
                .hasResume(true)
                .matchScore(Math.round(matchScore * 10.0) / 10.0)
                .matchingSkills(skillAnalysis.matchingSkills)
                .missingSkills(skillAnalysis.missingSkills)
                .applicationUrl(job.getApplicationUrl())
                .summary(summary)
                .build();
    }

    private double calculateMatchScoreWithAi(String candidateText, String jobText) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(4000);
        factory.setReadTimeout(5000);

        RestClient restClient = RestClient.builder()
                .baseUrl(aiServiceUrl)
                .requestFactory(factory)
                .build();

        try {
            AIRecommendationRequest request = AIRecommendationRequest.builder()
                    .candidateText(candidateText.length() > 3000 ? candidateText.substring(0, 3000) : candidateText)
                    .jobTexts(List.of(jobText.length() > 3000 ? jobText.substring(0, 3000) : jobText))
                    .build();

            AIRecommendationResponse response = restClient.post()
                    .uri("/recommend")
                    .body(request)
                    .retrieve()
                    .body(AIRecommendationResponse.class);

            if (response != null && response.getScores() != null && !response.getScores().isEmpty()) {
                float rawScore = response.getScores().get(0);
                // Convert cosine similarity (typically 0.3 - 0.9) to a calibrated 0-100% scale
                double scaledScore = Math.max(0, Math.min(100, ((rawScore - 0.20) / 0.70) * 100));
                return Math.round(scaledScore * 10.0) / 10.0;
            }
        } catch (Exception e) {
            log.warn("AI service unavailable ({}). Falling back to keyword-based score.", e.getMessage());
        }

        // Fallback: heuristic match score based on common words & skills
        return calculateHeuristicScore(candidateText, jobText);
    }

    private SkillAnalysisResult analyzeSkills(List<String> candidateSkills, String candidateText, String jobSkills, String jobDesc) {
        Set<String> candidateSkillsLower = new HashSet<>();
        for (String s : candidateSkills) {
            candidateSkillsLower.add(s.toLowerCase().trim());
        }

        Set<String> jobSkillsDetected = new LinkedHashSet<>();
        if (jobSkills != null && !jobSkills.isEmpty()) {
            for (String s : jobSkills.split("[,;/]")) {
                String trimmed = s.trim();
                if (!trimmed.isEmpty()) {
                    jobSkillsDetected.add(trimmed);
                }
            }
        }

        List<String> matching = new ArrayList<>();
        List<String> missing = new ArrayList<>();

        for (String js : jobSkillsDetected) {
            String jsLower = js.toLowerCase();
            boolean found = candidateSkillsLower.contains(jsLower)
                    || Pattern.compile("(?i)\\b" + Pattern.quote(js) + "\\b").matcher(candidateText).find();
            if (found) {
                matching.add(js);
            } else {
                missing.add(js);
            }
        }

        return new SkillAnalysisResult(matching, missing);
    }

    private double calculateHeuristicScore(String candidateText, String jobText) {
        Set<String> candWords = Arrays.stream(candidateText.toLowerCase().split("\\W+"))
                .filter(w -> w.length() > 3)
                .collect(Collectors.toSet());
        Set<String> jobWords = Arrays.stream(jobText.toLowerCase().split("\\W+"))
                .filter(w -> w.length() > 3)
                .collect(Collectors.toSet());

        if (jobWords.isEmpty() || candWords.isEmpty()) return 50.0;

        long common = candWords.stream().filter(jobWords::contains).count();
        double ratio = (double) common / Math.min(jobWords.size(), 100);
        return Math.min(95.0, Math.max(35.0, ratio * 100));
    }

    private String generateMatchSummary(double score, List<String> matching, List<String> missing) {
        if (score >= 80.0) {
            return "Strong Match! Your profile closely matches the core qualifications for this role.";
        } else if (score >= 60.0) {
            return "Good Match! You have several relevant skills, though some requested tools ("
                    + (missing.isEmpty() ? "additional qualifications" : String.join(", ", missing.stream().limit(2).toList()))
                    + ") were not found in your resume.";
        } else {
            return "Moderate Match. Role requires skills ("
                    + (missing.isEmpty() ? "specific tools" : String.join(", ", missing.stream().limit(3).toList()))
                    + ") that are currently absent from your uploaded resume.";
        }
    }

    private String stripHtml(String html) {
        if (html == null) return "";
        return html.replaceAll("<[^>]*>", " ").replaceAll("\\s+", " ").trim();
    }

    private record SkillAnalysisResult(List<String> matchingSkills, List<String> missingSkills) {}
}
