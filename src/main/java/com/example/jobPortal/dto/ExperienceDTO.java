package com.example.jobPortal.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExperienceDTO {
    private String title;
    private String company;
    private String employmentType; // Full-time, Part-time, etc.
    private String startDate; // e.g. "Jan 2021"
    private String endDate; // e.g. "Present" or "Dec 2023"
    private String description; // comma separated or single block
}
