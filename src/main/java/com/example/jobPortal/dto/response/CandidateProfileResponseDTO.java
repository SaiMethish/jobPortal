package com.example.jobPortal.dto.response;

import java.util.List;
import java.util.UUID;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CandidateProfileResponseDTO {
    private UUID id;
    private String fullName;
    private String phone;
    private String location;
    private Integer experienceYears;
    private List<String> skills;
    private String resumeUrl;
}

