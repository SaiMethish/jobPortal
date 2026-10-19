package com.example.jobPortal.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AdminStatsDTO implements Serializable {
    private long totalUsers;
    private long candidateCount;
    private long recruiterCount;
    private long adminCount;
    private long totalJobs;
    private long openJobs;
    private long totalApplications;
}
