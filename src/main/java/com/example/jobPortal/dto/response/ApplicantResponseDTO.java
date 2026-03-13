package com.example.jobPortal.dto.response;

import com.example.jobPortal.entity.JobApplicationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ApplicantResponseDTO {
    private UUID id;
    private UUID candidateProfileId;
    private String candidateName;
    private String candidateEmail;
    private Integer experienceYears;
    private List<String> skills;
    private String resumeUrl;
    private JobApplicationStatus status;
    private LocalDateTime appliedAt;
}
