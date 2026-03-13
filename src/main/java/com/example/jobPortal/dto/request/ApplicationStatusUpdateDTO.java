package com.example.jobPortal.dto.request;

import com.example.jobPortal.entity.JobApplicationStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApplicationStatusUpdateDTO {
    private JobApplicationStatus status;
}
