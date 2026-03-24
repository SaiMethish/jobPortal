package com.example.jobPortal;

import com.example.jobPortal.entity.CandidateProfile;
import com.example.jobPortal.entity.Role;
import com.example.jobPortal.entity.User;
import com.example.jobPortal.repository.CandidateProfileRepository;
import com.example.jobPortal.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ResumeParsingIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CandidateProfileRepository candidateProfileRepository;

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
        candidateUser.setEmail("candidate_tika@upload.com");
        candidateUser.setPassword("password");
        candidateUser.setRole(Role.CANDIDATE);
        candidateUser.setAuthProvider(com.example.jobPortal.entity.AuthProvider.LOCAL);
        userRepository.save(candidateUser);

        candidateProfile = CandidateProfile.builder()
                .user(candidateUser)
                .fullName("Tika Tester")
                .build();
        candidateProfileRepository.save(candidateProfile);
        
        Files.createDirectories(Paths.get(UPLOAD_DIR));
    }

    @AfterEach
    void tearDown() throws Exception {
        Path rootPath = Paths.get(UPLOAD_DIR);
        if (Files.exists(rootPath)) {
            Files.walk(rootPath)
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .filter(file -> file.getName().contains("test"))
                    .forEach(File::delete);
        }
    }

    @Test
    @WithMockUser(username = "candidate_tika@upload.com", roles = "CANDIDATE")
    void testExtractTextFromPdf() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "resume_test_tika.pdf",
                "application/pdf",
                "Dummy Resume Text Content inside a Fake PDF".getBytes()
        );

        mockMvc.perform(multipart("/candidate/profile/resume").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Resume uploaded successfully"));

        CandidateProfile updatedProfile = candidateProfileRepository.findById(candidateProfile.getId()).get();
        assertNotNull(updatedProfile.getResumeText(), "Extracted text should not be null.");
        assertTrue(updatedProfile.getResumeText().contains("Dummy Resume"), "Extracted text should contain our test sequence.");
    }

    @Test
    @WithMockUser(username = "candidate_tika@upload.com", roles = "CANDIDATE")
    void testCorruptedFileUpload() throws Exception {
        // Technically, Tika is very resilient. If it cannot read the dummy bytes as a PDF gracefully it often extracts empty strings or throws an internal TikaException which we mapped to ResumeParsingException.
        // It should still return 200 OK because the *Upload* succeeded, but the resumeText will remain empty or handle the logger warn quietly as programmed.
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "corrupted_resume.pdf",
                "application/pdf",
                new byte[]{0x00, 0x01, 0x02, 0x03}
        );

        mockMvc.perform(multipart("/candidate/profile/resume").file(file))
                .andExpect(status().isOk());

        CandidateProfile updatedProfile = candidateProfileRepository.findById(candidateProfile.getId()).get();
        assertTrue(updatedProfile.getResumeText() == null || updatedProfile.getResumeText().isEmpty());
    }

    @Test
    @WithMockUser(username = "candidate_tika@upload.com", roles = "CANDIDATE")
    void testLargeTextTruncation() throws Exception {
        // Create an artificially large payload of text disguised inside a standard format
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 110000; i++) {
            sb.append("A"); // 110,000 continuous A's
        }

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "huge_resume.txt",
                "text/plain", // Adding plain text support organically for testing truncation
                sb.toString().getBytes()
        );

        mockMvc.perform(multipart("/candidate/profile/resume").file(file))
                .andExpect(status().isOk());

        CandidateProfile updatedProfile = candidateProfileRepository.findById(candidateProfile.getId()).get();
        assertEquals(100000, updatedProfile.getResumeText().length(), "Resume text should be strictly truncated to exactly 100,000 chars.");
    }
}
