package com.example.jobPortal.dto.response;

import com.example.jobPortal.entity.JobApplicationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ApplicationResponseDTO {
    private UUID id;
    private UUID jobId;
    private String jobTitle;
    private String companyName;
    private JobApplicationStatus status;
    private LocalDateTime appliedAt;
}
