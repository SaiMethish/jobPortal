package com.example.jobPortal;

import com.example.jobPortal.entity.Job;
import com.example.jobPortal.entity.JobStatus;
import com.example.jobPortal.entity.RecruiterProfile;
import com.example.jobPortal.repository.JobRepository;
import com.example.jobPortal.repository.RecruiterProfileRepository;
import com.example.jobPortal.repository.UserRepository;
import com.example.jobPortal.entity.User;
import com.example.jobPortal.entity.Role;
import com.example.jobPortal.entity.AuthProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class RateLimitingIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RecruiterProfileRepository recruiterProfileRepository;

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private com.example.jobPortal.repository.CandidateProfileRepository candidateProfileRepository;

    @Autowired
    private com.example.jobPortal.repository.JobApplicationRepository jobApplicationRepository;

    @Autowired
    private com.example.jobPortal.repository.ApplicationStatusHistoryRepository historyRepository;

    private Job savedJob;

    @BeforeEach
    void setUp() {
        historyRepository.deleteAll();
        jobApplicationRepository.deleteAll();
        jobRepository.deleteAll();
        recruiterProfileRepository.deleteAll();
        candidateProfileRepository.deleteAll();
        userRepository.deleteAll();

        // Flush redis rate limit keys
        for (String key : stringRedisTemplate.keys("rate_limit:*")) {
            stringRedisTemplate.delete(key);
        }

        User recruiter = new User();
        recruiter.setEmail("ratelimit@test.com");
        recruiter.setPassword("password");
        recruiter.setRole(Role.RECRUITER);
        recruiter.setAuthProvider(AuthProvider.LOCAL);
        userRepository.save(recruiter);

        RecruiterProfile rp = RecruiterProfile.builder()
                .user(recruiter)
                .companyName("Limit Tech")
                .build();
        recruiterProfileRepository.save(rp);

        savedJob = Job.builder()
                .recruiterProfile(rp)
                .title("Rate Limited Dev")
                .skills("Java, Redis")
                .location("Remote")
                .status(JobStatus.OPEN)
                .applicationCount(0)
                .build();
        jobRepository.save(savedJob);
    }

    @Test
    void testRateLimiterBlocksExcessiveRequests() throws Exception {
        String testIp = "127.0.0.1";
        String url = "/jobs/search?title=java";

        // 1. Make 50 successful requests
        for (int i = 0; i < 50; i++) {
            mockMvc.perform(get(url).with(request -> {
                request.setRemoteAddr(testIp);
                return request;
            })).andExpect(status().isOk());
        }

        // 2. The 51st request should be blocked (429 Too Many Requests)
        MvcResult result = mockMvc.perform(get(url).with(request -> {
            request.setRemoteAddr(testIp);
            return request;
        })).andExpect(status().isTooManyRequests()).andReturn();

        // Verify JSON response
        String responseContent = result.getResponse().getContentAsString();
        assertTrue(responseContent.contains("Rate limit exceeded"));
    }

    @Test
    void testRateLimitAppliesToJobDetailsEndpoint() throws Exception {
        String testIp = "192.168.1.100";
        String url = "/jobs/" + savedJob.getId();

        // 1. Make 50 successful requests
        for (int i = 0; i < 50; i++) {
            mockMvc.perform(get(url).with(request -> {
                request.setRemoteAddr(testIp);
                return request;
            })).andExpect(status().isOk());
        }

        // 2. The 51st request should be blocked
        mockMvc.perform(get(url).with(request -> {
            request.setRemoteAddr(testIp);
            return request;
        })).andExpect(status().isTooManyRequests());
    }

    @Test
    void testRateLimiterIgnoresOtherEndpoints() throws Exception {
        String testIp = "10.0.0.1";
        String dummyAuthUrl = "/auth/login"; // Note: might return 404 or 401, but *should not* return 429 under these rules.
        
        // This test simulates spamming a non-limited endpoint
        for (int i = 0; i < 100; i++) {
            mockMvc.perform(get(dummyAuthUrl).with(request -> {
                request.setRemoteAddr(testIp);
                return request;
            }))
            // It could be Forbidden or Mathod Not Allowed, but it definitely shouldn't be 429
            .andExpect(result -> {
                int code = result.getResponse().getStatus();
                assertTrue(code != 429, "Should not hit Rate Limit Filter!");
            });
        }
    }
}
