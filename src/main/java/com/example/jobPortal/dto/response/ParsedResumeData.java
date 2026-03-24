package com.example.jobPortal.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ParsedResumeData {
    private String name;
    private String email;
    private String phone;
    private List<String> skills;
    private Integer experienceYears;
    private String education;
    private String resumeUrl; // Retaining for client convenience
    private String message;   // Return status flag
}
