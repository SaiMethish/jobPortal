package com.example.jobPortal.service.Impl;

import com.example.jobPortal.dto.response.ParsedResumeData;
import com.example.jobPortal.service.ResumeParsingService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ResumeParsingServiceImpl implements ResumeParsingService {

    // Common standard Regex patterns
    private static final Pattern EMAIL_PATTERN = Pattern.compile("[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+");
    private static final Pattern PHONE_PATTERN = Pattern.compile("(\\+?\\d{1,3})?[-.\\s]?\\(?\\d{3}\\)?[-.\\s]?\\d{3}[-.\\s]?\\d{4}");
    private static final Pattern EXP_PATTERN = Pattern.compile("(\\d+)\\+?\\s*(years|yrs)", Pattern.CASE_INSENSITIVE);

    // Dictionary mappings
    private static final List<String> SKILLS_DICTIONARY = Arrays.asList(
            "Java", "Spring Boot", "Spring", "Kafka", "Docker", "Kubernetes",
            "Python", "React", "Angular", "Microservices", "AWS", "SQL", "NoSQL", "Git"
    );

    private static final List<String> EDUCATION_KEYWORDS = Arrays.asList(
            "B.Tech", "M.Tech", "B.E", "M.S", "MBA", "B.Sc", "M.Sc", "PhD"
    );

    @Override
    public ParsedResumeData parseResume(String resumeText) {
        if (resumeText == null || resumeText.isEmpty()) {
            return ParsedResumeData.builder().build();
        }

        return ParsedResumeData.builder()
                .name(extractName(resumeText))
                .email(extractRegex(resumeText, EMAIL_PATTERN))
                .phone(extractRegex(resumeText, PHONE_PATTERN))
                .skills(extractSkills(resumeText))
                .experienceYears(extractExperience(resumeText))
                .education(extractEducation(resumeText))
                .build();
    }

    private String extractRegex(String text, Pattern pattern) {
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            return matcher.group().trim();
        }
        return null;
    }

    private Integer extractExperience(String text) {
        Matcher matcher = EXP_PATTERN.matcher(text);
        if (matcher.find()) {
            try {
                return Integer.parseInt(matcher.group(1)); // Extract just the digit grouping
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    private List<String> extractSkills(String text) {
        List<String> foundSkills = new ArrayList<>();
        String lowerText = text.toLowerCase();
        
        for (String skill : SKILLS_DICTIONARY) {
            // Using precise word boundaries to prevent things like 'javascript' matching 'java'
            String regex = "\\b" + Pattern.quote(skill.toLowerCase()) + "\\b";
            Pattern pattern = Pattern.compile(regex);
            
            if (pattern.matcher(lowerText).find()) {
                foundSkills.add(skill); // Returns the properly capitalized dictionary value
            }
        }
        return foundSkills;
    }

    private String extractEducation(String text) {
        // Find the first education keyword and extract a reasonable substring around it
        for (String edu : EDUCATION_KEYWORDS) {
            String regex = "\\b" + Pattern.quote(edu) + "\\b.*";
            Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(text);
            
            if (matcher.find()) {
                // Often we just want the exact line matching the degree, we'll try to extract up to the end of the line
                String match = matcher.group();
                int idx = match.indexOf('\n');
                if (idx != -1) {
                    match = match.substring(0, idx);
                }
                // Limiting string length to ~50 characters for clean DB entry if the line is arbitrarily long
                return match.length() > 50 ? match.substring(0, 50).trim() : match.trim();
            }
        }
        return null;
    }

    private String extractName(String text) {
        // Name extraction natively via open source string logic is notoriously inaccurate without NLP/NER ML
        // Typically it's the first non-empty line. We'll attempt a very basic heuristic.
        String[] lines = text.split("\n");
        for (String line : lines) {
            String clean = line.trim();
            if (!clean.isEmpty() && clean.length() < 30 && clean.matches("^[a-zA-Z\\s]+$")) {
                return clean;
            }
        }
        return null;
    }
}
