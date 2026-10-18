package com.example.jobPortal.dto.request;

import java.util.List;
import com.example.jobPortal.dto.common.ExperienceDTO;
import com.example.jobPortal.dto.common.EducationDTO;
import com.example.jobPortal.dto.common.CertificationDTO;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CandidateProfileRequestDTO {
    private String fullName;
    private String phone;
    private String location;
    private Integer experienceYears;
    private List<String> skills;
    private String about;
    private List<ExperienceDTO> experience;
    private List<EducationDTO> education;
    private List<CertificationDTO> certifications;
    // resume url is not updated by here separate api will be implemented later
}
