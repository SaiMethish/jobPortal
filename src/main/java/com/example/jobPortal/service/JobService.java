package com.example.jobPortal.service;

import com.example.jobPortal.dto.request.JobRequestDTO;
import com.example.jobPortal.dto.response.JobResponseDTO;
import com.example.jobPortal.entity.User;

import java.util.List;
import java.util.UUID;

public interface JobService {

    JobResponseDTO createJob(User user, JobRequestDTO dto);

    JobResponseDTO updateJob(User user, UUID jobId, JobRequestDTO dto);

    JobResponseDTO closeJob(User user, UUID jobId);

    List<JobResponseDTO> getMyJobs(User user);
}
