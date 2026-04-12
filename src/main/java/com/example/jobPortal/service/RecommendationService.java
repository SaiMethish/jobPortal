package com.example.jobPortal.service;

import com.example.jobPortal.dto.response.RecommendedJobDTO;
import com.example.jobPortal.entity.User;

import java.util.List;

public interface RecommendationService {
    List<RecommendedJobDTO> getJobRecommendations(User candidateUser);
}
