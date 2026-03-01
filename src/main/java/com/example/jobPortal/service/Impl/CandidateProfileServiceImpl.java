package com.example.jobPortal.service.Impl;

import org.springframework.stereotype.Service;

import com.example.jobPortal.dto.request.CandidateProfileRequestDTO;
import com.example.jobPortal.dto.response.CandidateProfileResponseDTO;
import com.example.jobPortal.entity.CandidateProfile;
import com.example.jobPortal.entity.User;
import com.example.jobPortal.exception.ResourceNotFoundException;
import com.example.jobPortal.mapper.CandidateProfileMapper;
import com.example.jobPortal.repository.CandidateProfileRepository;
import com.example.jobPortal.service.CandidateProfileService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CandidateProfileServiceImpl implements CandidateProfileService {

    private final CandidateProfileRepository repo;
    private final CandidateProfileMapper mapper;

    @Override
    public CandidateProfileResponseDTO getCandidateProfileByUser(User user) {
        CandidateProfile profile = repo.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Candidate profile not found"));
        return mapper.toResponseDTO(profile);
    }

    @Override
    public CandidateProfileResponseDTO updateCandidateProfile(User user, CandidateProfileRequestDTO dto) {
        CandidateProfile profile = repo.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Candidate profile not found"));
        profile.setFullName(dto.getFullName());
        profile.setPhone(dto.getPhone());
        profile.setLocation(dto.getLocation());
        profile.setExperienceYears(dto.getExperienceYears());
        profile.setSkills(dto.getSkills());
        return mapper.toResponseDTO(repo.save(profile));
    }

}
