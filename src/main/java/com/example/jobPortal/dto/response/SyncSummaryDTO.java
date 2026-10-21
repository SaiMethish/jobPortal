package com.example.jobPortal.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SyncSummaryDTO {
    private int totalCompaniesSynced;
    private int totalIndiaJobsActive;
    private int newJobsAdded;
    private int jobsUpdated;
    private int expiredJobsRemoved;
    private long durationMs;
    private String status;
    private List<String> companiesSynced;
}
