package com.example.jobPortal.dto.request;

import com.example.jobPortal.entity.JobType;
import jakarta.validation.constraints.*;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class JobRequestDTO {

    @NotBlank(message = "Job title is required")
    @Size(max = 255, message = "Title must not exceed 255 characters")
    private String title;

    @NotBlank(message = "Job description is required")
    @Size(max = 5000, message = "Description must not exceed 5000 characters")
    private String description;

    private String skills;

    @Min(value = 0, message = "Minimum experience cannot be negative")
    private int experienceMin;

    @Min(value = 0, message = "Maximum experience cannot be negative")
    private int experienceMax;

    @Min(value = 0, message = "Minimum salary cannot be negative")
    private Long salaryMin;

    @Min(value = 0, message = "Maximum salary cannot be negative")
    private Long salaryMax;

    @Size(max = 255, message = "Location must not exceed 255 characters")
    private String location;

    @NotNull(message = "Job type is required")
    private JobType jobType;
}
