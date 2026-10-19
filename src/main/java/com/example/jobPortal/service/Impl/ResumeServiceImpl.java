package com.example.jobPortal.service.Impl;

import com.example.jobPortal.dto.response.ParsedResumeData;
import com.example.jobPortal.entity.CandidateProfile;
import com.example.jobPortal.entity.User;
import com.example.jobPortal.exception.FileUploadException;
import com.example.jobPortal.repository.CandidateProfileRepository;
import com.example.jobPortal.repository.UserRepository;
import com.example.jobPortal.service.ResumeParserService;
import com.example.jobPortal.service.ResumeParsingService;
import com.example.jobPortal.service.ResumeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ResumeServiceImpl implements ResumeService {

    private final UserRepository userRepository;
    private final CandidateProfileRepository candidateProfileRepository;
    private final ResumeParserService resumeParserService;
    private final ResumeParsingService resumeParsingService;

    private static final String UPLOAD_DIR = "uploads/resumes/";
    private static final List<String> ALLOWED_CONTENT_TYPES = Arrays.asList(
            "application/pdf",
            "application/msword", // .doc
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document", // .docx
            "text/plain" // .txt
    );

    @Override
    public ParsedResumeData uploadResume(MultipartFile file, String userEmail) {
        if (file.isEmpty()) {
            throw new FileUploadException("Cannot upload empty file.");
        }

        validateFileType(file);

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        CandidateProfile profile = candidateProfileRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Candidate profile not found"));

        // Save Raw File
        String savedFilePath = saveFileLocally(file);
        File localFile = new File(System.getProperty("user.dir") + savedFilePath);

        // Extract unstructured text
        String extractedText = "";
        try {
            extractedText = resumeParserService.extractText(localFile);
        } catch (Exception e) {
            log.warn("Non-fatal parsing failure. Resume saved but text could not be extracted: {}", e.getMessage());
        }

        // Day 19: Parse structured fields from Text via RegEx/Dictionaries
        ParsedResumeData parsedData = resumeParsingService.parseResume(extractedText);

        // Enrich the Entity conditionally: Only overwrite if empty to preserve user overrides!
        profile.setResumeUrl(savedFilePath);
        profile.setResumeText(extractedText);

        if (profile.getPhone() == null && parsedData.getPhone() != null) {
            profile.setPhone(parsedData.getPhone());
        }
        if (profile.getExperienceYears() == null && parsedData.getExperienceYears() != null) {
            profile.setExperienceYears(parsedData.getExperienceYears());
        }
        if ((profile.getEducation() == null || profile.getEducation().isEmpty()) && parsedData.getEducation() != null) {
            profile.setEducation(java.util.List.of(
                com.example.jobPortal.dto.common.EducationDTO.builder()
                    .institution(parsedData.getEducation())
                    .build()
            ));
        }
        if ((profile.getSkills() == null || profile.getSkills().isEmpty()) && parsedData.getSkills() != null && !parsedData.getSkills().isEmpty()) {
            profile.setSkills(parsedData.getSkills());
        }

        candidateProfileRepository.save(profile);

        // Prep the return DTO
        parsedData.setResumeUrl(savedFilePath);
        parsedData.setMessage("Resume uploaded and parsed successfully");

        return parsedData;
    }

    @Override
    public ParsedResumeData getParsedResumePreview(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        CandidateProfile profile = candidateProfileRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Candidate profile not found"));
        
        String rawText = profile.getResumeText();
        if (rawText == null || rawText.isEmpty()) {
            throw new RuntimeException("No resume text found to preview. Please upload a valid resume first.");
        }

        ParsedResumeData parsedData = resumeParsingService.parseResume(rawText);
        parsedData.setResumeUrl(profile.getResumeUrl());
        parsedData.setMessage("Preview Generated Successfully");

        return parsedData;
    }

    @Override
    public Resource loadResumeAsResource(String filename) {
        try {
            String cleanName = Paths.get(filename).getFileName().toString();
            Path filePath = Paths.get(UPLOAD_DIR).resolve(cleanName).normalize();
            Resource resource = new org.springframework.core.io.UrlResource(filePath.toUri());
            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new com.example.jobPortal.exception.ResourceNotFoundException("Resume file not found: " + cleanName);
            }
        } catch (java.net.MalformedURLException ex) {
            throw new com.example.jobPortal.exception.ResourceNotFoundException("Resume file not found: " + filename);
        }
    }

    @Override
    public Resource loadUserResumeAsResource(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        CandidateProfile profile = candidateProfileRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Candidate profile not found"));
        if (profile.getResumeUrl() == null || profile.getResumeUrl().isEmpty()) {
            throw new com.example.jobPortal.exception.ResourceNotFoundException("No resume uploaded for user " + userEmail);
        }
        return loadResumeAsResource(profile.getResumeUrl());
    }

    private void validateFileType(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new FileUploadException("Invalid file type. Only PDF, DOC, and DOCX are allowed.");
        }
    }

    private String saveFileLocally(MultipartFile file) {
        try {
            Path uploadPath = Paths.get(UPLOAD_DIR);
            
            // Create directory if it doesn't exist
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Generate unique filename -> e.g. 3f42a2b2-...._resume.pdf
            String originalFilename = StringUtils.cleanPath(file.getOriginalFilename() != null ? file.getOriginalFilename() : "resume_unknown");
            String uniqueFilename = UUID.randomUUID().toString() + "_" + originalFilename;

            Path filePath = uploadPath.resolve(uniqueFilename);

            // Save file
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            return "/" + UPLOAD_DIR + uniqueFilename; // Format -> /uploads/resumes/xxxx_resume.pdf

        } catch (IOException ex) {
            log.error("Failed to store file", ex);
            throw new FileUploadException("Could not store the file. Please try again!", ex);
        }
    }
}
