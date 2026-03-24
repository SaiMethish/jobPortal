package com.example.jobPortal.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CandidateProfileUpdateDTO {
    private String name;
    private String email;
    private String phone;
    private List<String> skills;
    private Integer experienceYears;
    private String education;
}
