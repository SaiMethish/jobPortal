package com.example.jobPortal.integration;

import com.example.jobPortal.dto.request.JobRequestDTO;
import com.example.jobPortal.dto.request.LoginRequest;
import com.example.jobPortal.dto.request.UserRequestDTO;
import com.example.jobPortal.dto.response.AuthResponse;
import com.example.jobPortal.entity.JobType;
import com.example.jobPortal.entity.Role;
import com.example.jobPortal.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {"spring.cache.type=none"})
public class Day27AIRecommendationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Test
    public void testRecommendationFallback() throws Exception {
        // 1. Setup Recruiter
        String recEmail = "recruiter_ai_" + UUID.randomUUID() + "@example.com";
        UserRequestDTO recReq = UserRequestDTO.builder().email(recEmail).password("pass").role(Role.RECRUITER).build();
        mockMvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(recReq))).andExpect(status().isOk());

        LoginRequest recLogin = LoginRequest.builder().email(recEmail).password("pass").build();
        MvcResult recResult = mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(recLogin))).andReturn();
        String recToken = objectMapper.readValue(recResult.getResponse().getContentAsString(), AuthResponse.class).getAccessToken();

        // 2. Recruiter creates 2 jobs
        JobRequestDTO job1 = new JobRequestDTO();
        job1.setTitle("Java AI Engineer");
        job1.setDescription("Backend Java AI Embeddings");
        job1.setSkills("Java Python AI");
        job1.setJobType(JobType.FULL_TIME);

        mockMvc.perform(post("/recruiter/jobs")
                .header("Authorization", "Bearer " + recToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(job1)))
                .andExpect(status().isCreated());

        JobRequestDTO job2 = new JobRequestDTO();
        job2.setTitle("React UX Designer");
        job2.setDescription("Frontend React Tailwind");
        job2.setSkills("React UX UI");
        job2.setJobType(JobType.PART_TIME);

        mockMvc.perform(post("/recruiter/jobs")
                .header("Authorization", "Bearer " + recToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(job2)))
                .andExpect(status().isCreated());

        // 3. Setup Candidate
        String candEmail = "candidate_ai_" + UUID.randomUUID() + "@example.com";
        UserRequestDTO candReq = UserRequestDTO.builder().email(candEmail).password("pass").role(Role.CANDIDATE).build();
        mockMvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(candReq))).andExpect(status().isOk());

        LoginRequest candLogin = LoginRequest.builder().email(candEmail).password("pass").build();
        MvcResult candResult = mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(candLogin))).andReturn();
        String candToken = objectMapper.readValue(candResult.getResponse().getContentAsString(), AuthResponse.class).getAccessToken();

        // 4. Candidate fetches recommendations
        // NOTE: Since the Python AI server is NOT running during Maven tests,
        // this simulates a production AI outage, proving the Fallback 0.0 scores trigger gracefully!
        mockMvc.perform(get("/jobs/recommended")
                .header("Authorization", "Bearer " + candToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].score").value(0.0)) // Fallback score should be 0.0
                .andExpect(jsonPath("$[1].score").value(0.0));
    }
}
