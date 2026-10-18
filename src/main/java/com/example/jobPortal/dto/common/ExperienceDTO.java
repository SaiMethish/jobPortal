package com.example.jobPortal.dto.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExperienceDTO implements Serializable {
    private String jobDesignation;
    private String companyName;
    private String workType;
    private LocalDate dateFrom;
    private LocalDate dateTo;
    private List<String> summary;
}
