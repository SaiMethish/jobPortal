package com.example.jobPortal.client;

import com.example.jobPortal.dto.greenhouse.GreenhouseJobDTO;
import com.example.jobPortal.dto.greenhouse.GreenhouseJobResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Collections;
import java.util.List;

@Component
@Slf4j
public class GreenhouseClient {

    private final RestClient restClient;

    public GreenhouseClient() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(8000);
        factory.setReadTimeout(12000);

        this.restClient = RestClient.builder()
                .baseUrl("https://boards-api.greenhouse.io/v1")
                .requestFactory(factory)
                .defaultHeader("User-Agent", "Mozilla/5.0 (compatible; JobPortal/1.0)")
                .build();
    }

    /**
     * Fetches all jobs for a given company board token with full description content.
     */
    public List<GreenhouseJobDTO> fetchJobsForBoard(String boardSlug) {
        if (boardSlug == null || boardSlug.trim().isEmpty()) {
            return Collections.emptyList();
        }

        try {
            GreenhouseJobResponse response = restClient.get()
                    .uri("/boards/{slug}/jobs?content=true", boardSlug.trim().toLowerCase())
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(GreenhouseJobResponse.class);

            if (response != null && response.getJobs() != null) {
                return response.getJobs();
            }
        } catch (Exception e) {
            log.warn("Failed to fetch jobs from Greenhouse for board '{}': {}", boardSlug, e.getMessage());
        }

        return Collections.emptyList();
    }
}
