package com.example.jobPortal;

import com.example.jobPortal.dto.request.ApplicationRequestDTO;
import com.example.jobPortal.dto.request.ApplicationStatusUpdateDTO;
import com.example.jobPortal.entity.*;
import com.example.jobPortal.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class JobApplicationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RecruiterProfileRepository recruiterProfileRepository;

    @Autowired
    private CandidateProfileRepository candidateProfileRepository;

    @Autowired
    private JobRepository jobRepository;
    @org.springframework.beans.factory.annotation.Autowired
    private com.example.jobPortal.repository.SavedJobRepository savedJobRepository;

    @Autowired
    private JobApplicationRepository jobApplicationRepository;

    @Autowired
    private ApplicationStatusHistoryRepository historyRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private User recruiterA;
    private User recruiterB;
    private User candidate;
    private Job jobA;
    private JobApplication applicationA;

    @BeforeEach
    void setUp() {
        historyRepository.deleteAll();
        jobApplicationRepository.deleteAll();
        savedJobRepository.deleteAll();
        jobRepository.deleteAll();
        recruiterProfileRepository.deleteAll();
        candidateProfileRepository.deleteAll();
        userRepository.deleteAll();

        // 1. Setup Recruiter A
        recruiterA = new User();
        recruiterA.setEmail("recruiterA@test.com");
        recruiterA.setPassword("password");
        recruiterA.setRole(Role.RECRUITER);
        recruiterA.setAuthProvider(AuthProvider.LOCAL);
        userRepository.save(recruiterA);

        RecruiterProfile rpA = RecruiterProfile.builder()
                .user(recruiterA)
                .companyName("Company A")
                .build();
        recruiterProfileRepository.save(rpA);

        // 2. Setup Recruiter B
        recruiterB = new User();
        recruiterB.setEmail("recruiterB@test.com");
        recruiterB.setPassword("password");
        recruiterB.setRole(Role.RECRUITER);
        recruiterB.setAuthProvider(AuthProvider.LOCAL);
        userRepository.save(recruiterB);

        RecruiterProfile rpB = RecruiterProfile.builder()
                .user(recruiterB)
                .companyName("Company B")
                .build();
        recruiterProfileRepository.save(rpB);

        // 3. Setup Candidate
        candidate = new User();
        candidate.setEmail("candidate@test.com");
        candidate.setPassword("password");
        candidate.setRole(Role.CANDIDATE);
        candidate.setAuthProvider(AuthProvider.LOCAL);
        userRepository.save(candidate);

        CandidateProfile cp = CandidateProfile.builder()
                .user(candidate)
                .fullName("Test Candidate")
                .experienceYears(2)
                .build();
        candidateProfileRepository.save(cp);

        // 4. Setup Job (Owned by Recruiter A)
        jobA = Job.builder()
                .recruiterProfile(rpA)
                .title("Software Engineer")
                .status(JobStatus.OPEN)
                .applicationCount(0)
                .build();
        jobRepository.save(jobA);

        // 5. Setup Application
        applicationA = JobApplication.builder()
                .job(jobA)
                .candidateProfile(cp)
                .status(JobApplicationStatus.APPLIED) // Initial status
                .build();
        jobApplicationRepository.save(applicationA);
    }

    // Test 1 — Valid transition (APPLIED -> SHORTLISTED)
    @Test
    @WithMockUser(username = "recruiterA@test.com", roles = "RECRUITER")
    void testValidStatusTransition() throws Exception {
        ApplicationStatusUpdateDTO dto = new ApplicationStatusUpdateDTO(JobApplicationStatus.SHORTLISTED);

        mockMvc.perform(patch("/recruiter/applications/" + applicationA.getId() + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SHORTLISTED"));

        // Verify ApplicationStatusHistory was stored (Test 5 part A)
        List<ApplicationStatusHistory> histories = historyRepository.findAll();
        assertEquals(1, histories.size());
        assertEquals(JobApplicationStatus.APPLIED, histories.get(0).getPreviousStatus());
        assertEquals(JobApplicationStatus.SHORTLISTED, histories.get(0).getNewStatus());
    }

    // Test 2 — Invalid transition (APPLIED -> HIRED)
    @Test
    @WithMockUser(username = "recruiterA@test.com", roles = "RECRUITER")
    void testInvalidStatusTransition() throws Exception {
        ApplicationStatusUpdateDTO dto = new ApplicationStatusUpdateDTO(JobApplicationStatus.HIRED);

        mockMvc.perform(patch("/recruiter/applications/" + applicationA.getId() + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest()); // 400 Bad Request
    }

    // Test 3 — Unauthorized recruiter (Recruiter B tries to update Recruiter A's job application)
    @Test
    @WithMockUser(username = "recruiterB@test.com", roles = "RECRUITER")
    void testUnauthorizedRecruiterStatusUpdate() throws Exception {
        ApplicationStatusUpdateDTO dto = new ApplicationStatusUpdateDTO(JobApplicationStatus.SHORTLISTED);

        mockMvc.perform(patch("/recruiter/applications/" + applicationA.getId() + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden()); // 403 Forbidden
    }

    // Test 4 — Candidate view applications
    @Test
    @WithMockUser(username = "candidate@test.com", roles = "CANDIDATE")
    void testCandidateViewApplications() throws Exception {
        mockMvc.perform(get("/candidate/applications"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].jobTitle").value("Software Engineer"))
                .andExpect(jsonPath("$.content[0].companyName").value("Company A"))
                .andExpect(jsonPath("$.content[0].status").value("APPLIED"));
    }
}
