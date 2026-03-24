package com.example.jobPortal.dto.response;

import com.example.jobPortal.entity.JobStatus;
import com.example.jobPortal.entity.JobType;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class JobResponseDTO implements java.io.Serializable {
    private UUID id;
    private UUID recruiterProfileId;
    private String title;
    private String description;
    private String skills;
    private int experienceMin;
    private int experienceMax;
    private Long salaryMin;
    private Long salaryMax;
    private String location;
    private JobType jobType;
    private JobStatus status;
    private int applicationCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime expiresAt;
}
