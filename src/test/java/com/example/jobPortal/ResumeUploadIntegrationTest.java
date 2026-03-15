package com.example.jobPortal;

import com.example.jobPortal.entity.CandidateProfile;
import com.example.jobPortal.entity.Role;
import com.example.jobPortal.entity.User;
import com.example.jobPortal.repository.CandidateProfileRepository;
import com.example.jobPortal.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ResumeUploadIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CandidateProfileRepository candidateProfileRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private com.example.jobPortal.repository.JobApplicationRepository jobApplicationRepository;
    
    @Autowired
    private com.example.jobPortal.repository.ApplicationStatusHistoryRepository historyRepository;

    @Autowired
    private com.example.jobPortal.repository.JobRepository jobRepository;

    @Autowired
    private com.example.jobPortal.repository.RecruiterProfileRepository recruiterProfileRepository;

    private User candidateUser;
    private CandidateProfile candidateProfile;
    private static final String UPLOAD_DIR = "uploads/resumes/";

    @BeforeEach
    void setUp() throws Exception {
        historyRepository.deleteAll();
        jobApplicationRepository.deleteAll();
        jobRepository.deleteAll();
        recruiterProfileRepository.deleteAll();
        candidateProfileRepository.deleteAll(); 
        userRepository.deleteAll();

        candidateUser = new User();
        candidateUser.setEmail("candidate@upload.com");
        candidateUser.setPassword("password");
        candidateUser.setRole(Role.CANDIDATE);
        candidateUser.setAuthProvider(com.example.jobPortal.entity.AuthProvider.LOCAL);
        userRepository.save(candidateUser);

        candidateProfile = CandidateProfile.builder()
                .user(candidateUser)
                .fullName("Upload Tester")
                .build();
        candidateProfileRepository.save(candidateProfile);
        
        Files.createDirectories(Paths.get(UPLOAD_DIR));
    }

    @AfterEach
    void tearDown() throws Exception {
        // Clean up test uploads to prevent cluttering disk
        Path rootPath = Paths.get(UPLOAD_DIR);
        if (Files.exists(rootPath)) {
            Files.walk(rootPath)
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .filter(file -> file.getName().contains("test")) // only delete test files if named appropriately, or just clean directory. We'll simply let it be during this exercise or wipe it cautiously.
                    .forEach(File::delete);
        }
    }

    @Test
    @WithMockUser(username = "candidate@upload.com", roles = "CANDIDATE")
    void testValidPdfUpload() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "resume_test.pdf",
                "application/pdf",
                "Dummy PDF Content".getBytes()
        );

        mockMvc.perform(multipart("/candidate/profile/resume").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Resume uploaded successfully"))
                .andExpect(jsonPath("$.resumeUrl").exists());

        // Verify DB update
        CandidateProfile updatedProfile = candidateProfileRepository.findById(candidateProfile.getId()).get();
        assertNotNull(updatedProfile.getResumeUrl());
        assertTrue(updatedProfile.getResumeUrl().contains("resume_test.pdf"));
    }

    @Test
    @WithMockUser(username = "candidate@upload.com", roles = "CANDIDATE")
    void testInvalidFileTypeUpload() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "malware_test.exe",
                "application/x-msdownload",
                "Malicious Content".getBytes()
        );

        mockMvc.perform(multipart("/candidate/profile/resume").file(file))
                .andExpect(status().isBadRequest()); // Expect bad request because of FileUploadException
    }

    @Test
    void testUnauthorizedUpload() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "resume_test.pdf",
                "application/pdf",
                "Content".getBytes()
        );

        // No @WithMockUser = Anonymous. The security filter chain redirects anonymous users 
        // trying to hit protected resources to the OAuth2 login page (HTTP 302 Found)
        mockMvc.perform(multipart("/candidate/profile/resume").file(file))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @WithMockUser(username = "recruiter@upload.com", roles = "RECRUITER")
    void testRecruiterForbiddenFromUpload() throws Exception {
        User recruiter = new User();
        recruiter.setEmail("recruiter@upload.com");
        recruiter.setPassword("password");
        recruiter.setRole(Role.RECRUITER);
        recruiter.setAuthProvider(com.example.jobPortal.entity.AuthProvider.LOCAL);
        userRepository.save(recruiter);

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "resume_test.pdf",
                "application/pdf",
                "Content".getBytes()
        );

        mockMvc.perform(multipart("/candidate/profile/resume").file(file))
                .andExpect(status().isForbidden());
    }
}
