package com.example.jobPortal.service;

import com.example.jobPortal.dto.request.RecruiterProfileRequestDTO;
import com.example.jobPortal.dto.response.RecruiterProfileResponseDTO;
import com.example.jobPortal.entity.User;

public interface RecruiterProfileService {
    RecruiterProfileResponseDTO getRecruiterProfileByUser(User user);

    RecruiterProfileResponseDTO updateRecruiterProfile(User user, RecruiterProfileRequestDTO dto);
}
