package com.example.jobPortal.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ApplicationRequestDTO {
    /** Optional override resume for this specific application. */
    private String resumeUrl;
}
