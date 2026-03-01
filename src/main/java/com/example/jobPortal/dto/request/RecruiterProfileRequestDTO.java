package com.example.jobPortal.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.validator.constraints.URL;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RecruiterProfileRequestDTO {

    @NotBlank(message = "Company name is required")
    @Size(max = 255, message = "Company name must not exceed 255 characters")
    private String companyName;

    @URL(message = "Company website must be a valid URL")
    private String companyWebsite;

    @Size(max = 2000, message = "Company description must not exceed 2000 characters")
    private String companyDescription;

    @Size(max = 100, message = "Industry must not exceed 100 characters")
    private String industry;

    @Size(max = 255, message = "Location must not exceed 255 characters")
    private String location;
}
