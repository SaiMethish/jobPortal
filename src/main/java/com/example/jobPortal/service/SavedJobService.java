package com.example.jobPortal.service;

import com.example.jobPortal.dto.response.JobResponseDTO;
import com.example.jobPortal.entity.User;

import java.util.List;
import java.util.UUID;

public interface SavedJobService {
    void saveJob(User user, UUID jobId);
    List<JobResponseDTO> getSavedJobs(User user);
    void unsaveJob(User user, UUID jobId);
}
