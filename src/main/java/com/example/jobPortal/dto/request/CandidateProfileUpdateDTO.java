package com.example.jobPortal.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import com.example.jobPortal.dto.common.ExperienceDTO;
import com.example.jobPortal.dto.common.EducationDTO;
import com.example.jobPortal.dto.common.CertificationDTO;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CandidateProfileUpdateDTO {
    private String name;
    private String email;
    private String phone;
    private List<String> skills;
    private String about;
    private List<ExperienceDTO> experience;
    private List<EducationDTO> education;
    private List<CertificationDTO> certifications;
    private Integer experienceYears;
}
