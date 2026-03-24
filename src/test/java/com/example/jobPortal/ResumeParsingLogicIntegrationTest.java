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
class ResumeParsingLogicIntegrationTest {

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
        candidateUser.setEmail("parsing_logic@test.com");
        candidateUser.setPassword("password");
        candidateUser.setRole(Role.CANDIDATE);
        candidateUser.setAuthProvider(com.example.jobPortal.entity.AuthProvider.LOCAL);
        userRepository.save(candidateUser);

        candidateProfile = CandidateProfile.builder()
                .user(candidateUser)
                .fullName("Parsing Tester")
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

    private void runExtractionTest(String resumeContent, String jsonPathKey, Object expectedValue) throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "logic_test_resume.txt",
                "text/plain",
                resumeContent.getBytes()
        );

        if (expectedValue instanceof String) {
            mockMvc.perform(multipart("/candidate/profile/resume").file(file))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$." + jsonPathKey).value(expectedValue));
        } else if (expectedValue instanceof Integer) {
            mockMvc.perform(multipart("/candidate/profile/resume").file(file))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$." + jsonPathKey).value(expectedValue));
        } else if (expectedValue == null) {
            mockMvc.perform(multipart("/candidate/profile/resume").file(file))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$." + jsonPathKey).doesNotExist()); // Null values simply aren't serialized into the DTO or are explicitly unmapped depending on properties, but DoesNotExist is safe for JsonInclude.NON_NULL or simple objects
        }
    }

    @Test
    @WithMockUser(username = "parsing_logic@test.com", roles = "CANDIDATE")
    void testEmailExtraction() throws Exception {
        String content = "Contact me at structured.extraction@domain.com for job offers.";
        runExtractionTest(content, "email", "structured.extraction@domain.com");
    }

    @Test
    @WithMockUser(username = "parsing_logic@test.com", roles = "CANDIDATE")
    void testPhoneExtraction() throws Exception {
        String content = "Phone number is +91 9876543210 please call.";
        runExtractionTest(content, "phone", "+91 9876543210");
    }

    @Test
    @WithMockUser(username = "parsing_logic@test.com", roles = "CANDIDATE")
    void testExperienceExtraction() throws Exception {
        String content = "I am a Software engineer with 5 years experience in building APIs.";
        runExtractionTest(content, "experienceYears", 5);
    }

    @Test
    @WithMockUser(username = "parsing_logic@test.com", roles = "CANDIDATE")
    void testEducationExtraction() throws Exception {
        String content = "University of Delhi\nB.Tech Computer Science\nGraduated 2018;";
        runExtractionTest(content, "education", "B.Tech Computer Science");
    }

    @Test
    @WithMockUser(username = "parsing_logic@test.com", roles = "CANDIDATE")
    void testSkillsExtraction() throws Exception {
        String content = "Experienced in Java, Spring Boot, and Kafka with some React.";
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "logic_test_resume.txt",
                "text/plain",
                content.getBytes()
        );

        mockMvc.perform(multipart("/candidate/profile/resume").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.skills[0]").value("Java"))
                .andExpect(jsonPath("$.skills[1]").value("Spring Boot"))
                .andExpect(jsonPath("$.skills[2]").value("Spring"))
                .andExpect(jsonPath("$.skills[3]").value("Kafka"))
                .andExpect(jsonPath("$.skills[4]").value("React"));
    }

    @Test
    @WithMockUser(username = "parsing_logic@test.com", roles = "CANDIDATE")
    void testMissingFieldsHandlesGracefully() throws Exception {
        String content = "Just an empty file with no discernible phone or email data anywhere inside it.";
        
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "logic_test_resume.txt",
                "text/plain",
                content.getBytes()
        );

        mockMvc.perform(multipart("/candidate/profile/resume").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.phone").doesNotExist());
    }
}
