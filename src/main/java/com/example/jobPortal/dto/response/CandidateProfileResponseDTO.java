package com.example.jobPortal.dto.response;

import java.util.List;
import java.util.UUID;
import com.example.jobPortal.dto.common.ExperienceDTO;
import com.example.jobPortal.dto.common.EducationDTO;
import com.example.jobPortal.dto.common.CertificationDTO;

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
    private String about;
    private List<ExperienceDTO> experience;
    private List<EducationDTO> education;
    private List<CertificationDTO> certifications;
    private String resumeUrl;
}

