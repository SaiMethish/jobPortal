package com.example.jobPortal.dto.response;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RecruiterProfileResponseDTO {
    private UUID id;
    private UUID userId;
    private String companyName;
    private String companyWebsite;
    private String companyDescription;
    private String industry;
    private String location;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
