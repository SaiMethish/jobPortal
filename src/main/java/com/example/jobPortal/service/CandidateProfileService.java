package com.example.jobPortal.service;


import com.example.jobPortal.dto.request.CandidateProfileRequestDTO;
import com.example.jobPortal.dto.response.CandidateProfileResponseDTO;
import com.example.jobPortal.entity.User;

public interface CandidateProfileService {
    CandidateProfileResponseDTO getCandidateProfileByUser(User user);
    CandidateProfileResponseDTO updateCandidateProfile(User user, CandidateProfileRequestDTO dto);
}
