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
import org.springframework.cache.CacheManager;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {"spring.cache.type=simple"}) // Simulates Redis using ConcurrentHashMap internally
public class Day28AICachingIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private CacheManager cacheManager;

    @Test
    public void testAIEvictionAndCachingFlow() throws Exception {
        // 1. Setup Candidate
        String candEmail = "candidate_cache_" + UUID.randomUUID() + "@example.com";
        UserRequestDTO candReq = UserRequestDTO.builder().email(candEmail).password("pass").role(Role.CANDIDATE).build();
        mockMvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(candReq))).andExpect(status().isOk());

        LoginRequest candLogin = LoginRequest.builder().email(candEmail).password("pass").build();
        MvcResult candResult = mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(candLogin))).andReturn();
        String candToken = objectMapper.readValue(candResult.getResponse().getContentAsString(), AuthResponse.class).getAccessToken();
        
        UUID uId = userRepository.findByEmail(candEmail).get().getUserId();

        // 2. Clear out any stray caches from application loads just in case
        cacheManager.getCache("recommendedJobs").clear();

        // 3. Make the First Call
        mockMvc.perform(get("/jobs/recommended")
                .header("Authorization", "Bearer " + candToken))
                .andExpect(status().isOk());

        // Assert Cache now contains the values for this User ID natively
        assertNotNull(cacheManager.getCache("recommendedJobs").get(uId));

        // 4. Create a Job via Recruiter (Should trigger eviction)
        String recEmail = "recruiter_cache_" + UUID.randomUUID() + "@example.com";
        UserRequestDTO recReq = UserRequestDTO.builder().email(recEmail).password("pass").role(Role.RECRUITER).build();
        mockMvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(recReq))).andExpect(status().isOk());

        LoginRequest recLogin = LoginRequest.builder().email(recEmail).password("pass").build();
        MvcResult recResult = mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(recLogin))).andReturn();
        String recToken = objectMapper.readValue(recResult.getResponse().getContentAsString(), AuthResponse.class).getAccessToken();

        JobRequestDTO job1 = new JobRequestDTO();
        job1.setTitle("Trigger Cache Miss Job");
        job1.setDescription("Will cause Evict");
        job1.setSkills("Java");
        job1.setJobType(JobType.FULL_TIME);

        mockMvc.perform(post("/recruiter/jobs")
                .header("Authorization", "Bearer " + recToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(job1)))
                .andExpect(status().isCreated());

        // Assert that creating a job wiped everyone's recommendation cache cleanly!
        assertNull(cacheManager.getCache("recommendedJobs").get(uId));
    }
}
