package com.example.jobPortal.service.Impl;

import com.example.jobPortal.dto.request.RecruiterProfileRequestDTO;
import com.example.jobPortal.dto.response.RecruiterProfileResponseDTO;
import com.example.jobPortal.entity.RecruiterProfile;
import com.example.jobPortal.entity.User;
import com.example.jobPortal.exception.ResourceNotFoundException;
import com.example.jobPortal.mapper.RecruiterProfileMapper;
import com.example.jobPortal.repository.RecruiterProfileRepository;
import com.example.jobPortal.service.RecruiterProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RecruiterProfileServiceImpl implements RecruiterProfileService {

    private final RecruiterProfileRepository repo;
    private final RecruiterProfileMapper mapper;

    @Override
    public RecruiterProfileResponseDTO getRecruiterProfileByUser(User user) {
        RecruiterProfile profile = repo.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Recruiter profile not found"));
        return mapper.toResponseDTO(profile);
    }

    @Override
    public RecruiterProfileResponseDTO updateRecruiterProfile(User user, RecruiterProfileRequestDTO dto) {
        RecruiterProfile profile = repo.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Recruiter profile not found"));

        profile.setCompanyName(dto.getCompanyName());
        profile.setCompanyWebsite(dto.getCompanyWebsite());
        profile.setCompanyDescription(dto.getCompanyDescription());
        profile.setIndustry(dto.getIndustry());
        profile.setLocation(dto.getLocation());

        return mapper.toResponseDTO(repo.save(profile));
    }
}
