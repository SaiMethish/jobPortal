package com.example.jobPortal.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CompanyDetailDTO implements Serializable {
    private UUID id;
    private UUID userId;
    private String companyName;
    private String companyWebsite;
    private String companyDescription;
    private String industry;
    private String location;
    private List<JobResponseDTO> openJobs;
    private int totalJobsCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
