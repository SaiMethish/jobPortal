package com.example.jobPortal.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobMatchResponseDTO {
    private UUID jobId;
    private String jobTitle;
    private String companyName;
    private double matchScore; // e.g. 84.5%
    private List<String> matchingSkills;
    private List<String> missingSkills;
    private String summary;
    private boolean hasResume;
    private String applicationUrl;
}
