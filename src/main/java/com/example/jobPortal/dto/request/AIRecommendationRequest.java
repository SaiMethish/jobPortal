package com.example.jobPortal.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AIRecommendationRequest {

    @JsonProperty("candidate_text")
    private String candidateText;
    
    @JsonProperty("job_texts")
    private List<String> jobTexts;
}
