package com.example.jobPortal.dto.request;

import java.util.List;

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
    // resume url is not updated by here separate api will be implemented later
}
