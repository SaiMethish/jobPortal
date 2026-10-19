package com.example.jobPortal.service;

import com.example.jobPortal.dto.response.AdminStatsDTO;
import com.example.jobPortal.dto.response.AdminUserDTO;
import com.example.jobPortal.dto.response.JobResponseDTO;
import com.example.jobPortal.entity.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface AdminService {
    AdminStatsDTO getPlatformStats();
    Page<AdminUserDTO> getUsers(Role role, String search, Pageable pageable);
    AdminUserDTO toggleUserVerification(UUID userId);
    Page<JobResponseDTO> getAllJobs(Pageable pageable);
    void deleteJob(UUID jobId);
}
