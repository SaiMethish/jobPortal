package com.example.jobPortal.service.Impl;

import com.example.jobPortal.entity.CandidateProfile;
import com.example.jobPortal.entity.User;
import com.example.jobPortal.exception.FileUploadException;
import com.example.jobPortal.repository.CandidateProfileRepository;
import com.example.jobPortal.repository.UserRepository;
import com.example.jobPortal.service.ResumeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

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

    private static final String UPLOAD_DIR = "uploads/resumes/";
    private static final List<String> ALLOWED_CONTENT_TYPES = Arrays.asList(
            "application/pdf",
            "application/msword", // .doc
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document" // .docx
    );

    @Override
    public String uploadResume(MultipartFile file, String userEmail) {
        if (file.isEmpty()) {
            throw new FileUploadException("Cannot upload empty file.");
        }

        validateFileType(file);

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        CandidateProfile profile = candidateProfileRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Candidate profile not found"));

        String savedFilePath = saveFileLocally(file);

        // Update CandidateProfile
        profile.setResumeUrl(savedFilePath);
        candidateProfileRepository.save(profile);

        return savedFilePath;
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
