package com.example.jobPortal.service.Impl;

import com.example.jobPortal.dto.response.ParsedResumeData;
import com.example.jobPortal.entity.CandidateProfile;
import com.example.jobPortal.entity.User;
import com.example.jobPortal.exception.FileUploadException;
import com.example.jobPortal.exception.ResourceNotFoundException;
import com.example.jobPortal.repository.CandidateProfileRepository;
import com.example.jobPortal.repository.UserRepository;
import com.example.jobPortal.service.ResumeParserService;
import com.example.jobPortal.service.ResumeParsingService;
import com.example.jobPortal.service.ResumeService;
import com.example.jobPortal.service.SupabaseStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Paths;
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
    private final SupabaseStorageService supabaseStorageService;

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

        // 1. Read binary bytes directly into memory
        byte[] fileBytes;
        try {
            fileBytes = file.getBytes();
        } catch (IOException e) {
            log.error("Failed to read uploaded resume bytes for user {}", userEmail, e);
            throw new FileUploadException("Could not read uploaded file: " + e.getMessage(), e);
        }

        // 2. Build cloud storage object path: candidate-resumes/{candidate-id}/{uuid}_{filename}
        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename() != null ? file.getOriginalFilename() : "resume.pdf");
        String uniqueFilename = UUID.randomUUID().toString() + "_" + originalFilename;
        String objectPath = "candidate-resumes/" + profile.getId() + "/" + uniqueFilename;

        // 3. Upload to private Supabase Storage
        supabaseStorageService.uploadFile(objectPath, fileBytes, file.getContentType());

        // 4. Delete previously uploaded resume from Supabase Storage to prevent storage bloat
        String oldResumeUrl = profile.getResumeUrl();
        if (oldResumeUrl != null && !oldResumeUrl.isBlank() && !oldResumeUrl.equals(objectPath) && oldResumeUrl.startsWith("candidate-resumes/")) {
            try {
                supabaseStorageService.deleteFile(oldResumeUrl);
            } catch (Exception e) {
                log.warn("Failed to delete superseded resume file from storage: {}", oldResumeUrl, e);
            }
        }

        // 5. Extract text from uploaded bytes directly via Apache Tika (no local disk write)
        String extractedText = "";
        try {
            extractedText = resumeParserService.extractText(fileBytes, originalFilename);
        } catch (Exception e) {
            log.warn("Non-fatal parsing failure. Resume saved to storage but text could not be extracted: {}", e.getMessage());
        }

        // 6. Parse structured fields from Text via RegEx/Dictionaries
        ParsedResumeData parsedData = resumeParsingService.parseResume(extractedText);

        // 7. Enrich CandidateProfile conditionally (preserve explicit user overrides)
        profile.setResumeUrl(objectPath);
        profile.setResumeText(extractedText);

        if (profile.getPhone() == null && parsedData.getPhone() != null) {
            profile.setPhone(parsedData.getPhone());
        }
        if (profile.getExperienceYears() == null && parsedData.getExperienceYears() != null) {
            profile.setExperienceYears(parsedData.getExperienceYears());
        }
        if ((profile.getEducation() == null || profile.getEducation().isEmpty()) && parsedData.getEducation() != null) {
            profile.setEducation(List.of(
                    com.example.jobPortal.dto.common.EducationDTO.builder()
                            .institution(parsedData.getEducation())
                            .build()
            ));
        }
        if ((profile.getSkills() == null || profile.getSkills().isEmpty()) && parsedData.getSkills() != null && !parsedData.getSkills().isEmpty()) {
            profile.setSkills(parsedData.getSkills());
        }

        candidateProfileRepository.save(profile);

        // 8. Prepare response DTO
        parsedData.setResumeUrl(objectPath);
        parsedData.setMessage("Resume uploaded successfully");

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
    public Resource loadResumeAsResource(String filenameOrPath) {
        if (filenameOrPath == null || filenameOrPath.isBlank()) {
            throw new ResourceNotFoundException("Resume path or filename cannot be empty.");
        }

        String targetPath = filenameOrPath.trim();

        // If the path was passed as just a filename (e.g. from /resume/download/{filename}), resolve full object path
        if (!targetPath.contains("/")) {
            List<String> matching = candidateProfileRepository.findResumeUrlsMatchingFilename(targetPath);
            if (!matching.isEmpty()) {
                targetPath = matching.get(0);
            }
        }

        byte[] fileBytes = supabaseStorageService.downloadFile(targetPath);

        // Determine user-friendly download filename (strip UUID prefix if present)
        String rawName = Paths.get(targetPath).getFileName().toString();
        String displayFilename = rawName;
        int firstUnderscore = rawName.indexOf('_');
        if (firstUnderscore != -1 && firstUnderscore < rawName.length() - 1) {
            displayFilename = rawName.substring(firstUnderscore + 1);
        }

        final String finalFilename = displayFilename;
        return new ByteArrayResource(fileBytes) {
            @Override
            public String getFilename() {
                return finalFilename;
            }
        };
    }

    @Override
    public Resource loadUserResumeAsResource(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        CandidateProfile profile = candidateProfileRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Candidate profile not found"));
        if (profile.getResumeUrl() == null || profile.getResumeUrl().isBlank()) {
            throw new ResourceNotFoundException("No resume uploaded for user " + userEmail);
        }
        return loadResumeAsResource(profile.getResumeUrl());
    }

    private void validateFileType(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new FileUploadException("Invalid file type. Only PDF, DOC, and DOCX are allowed.");
        }
    }
}
