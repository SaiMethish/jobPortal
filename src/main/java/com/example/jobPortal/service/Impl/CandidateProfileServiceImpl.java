package com.example.jobPortal.service.Impl;

import org.springframework.stereotype.Service;

import com.example.jobPortal.dto.request.CandidateProfileRequestDTO;
import com.example.jobPortal.dto.response.CandidateProfileResponseDTO;
import com.example.jobPortal.entity.CandidateProfile;
import com.example.jobPortal.entity.User;
import com.example.jobPortal.exception.ResourceNotFoundException;
import com.example.jobPortal.mapper.CandidateProfileMapper;
import com.example.jobPortal.repository.CandidateProfileRepository;
import com.example.jobPortal.repository.UserRepository;
import com.example.jobPortal.service.CandidateProfileService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CandidateProfileServiceImpl implements CandidateProfileService {

    private final CandidateProfileRepository repo;
    private final CandidateProfileMapper mapper;
    private final UserRepository userRepository;

    @Override
    public CandidateProfileResponseDTO getCandidateProfileByUser(User user) {
        CandidateProfile profile = repo.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Candidate profile not found"));
        return mapper.toResponseDTO(profile);
    }

    @org.springframework.cache.annotation.CacheEvict(value = "recommendedJobs", key = "#user.userId")
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

    @org.springframework.cache.annotation.CacheEvict(value = "recommendedJobs", allEntries = true)
    @Override
    public void applyParsedResumeData(com.example.jobPortal.dto.request.CandidateProfileUpdateDTO dto, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found for email: " + userEmail));
                
        CandidateProfile profile = repo.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Candidate profile not found for email: " + userEmail));

        // Smart Merge: Only overwrite if current explicit entries are empty
        if ((profile.getFullName() == null || profile.getFullName().trim().isEmpty()) && dto.getName() != null) {
            profile.setFullName(dto.getName());
        }
        if ((profile.getPhone() == null || profile.getPhone().trim().isEmpty()) && dto.getPhone() != null) {
            profile.setPhone(dto.getPhone());
        }
        if (profile.getExperienceYears() == null && dto.getExperienceYears() != null) {
            profile.setExperienceYears(dto.getExperienceYears());
        }
        if ((profile.getEducation() == null || profile.getEducation().trim().isEmpty()) && dto.getEducation() != null) {
            profile.setEducation(dto.getEducation());
        }
        
        // Hardcoded Email sync protection (Email is primary key, safely skipping overwrites)

        // Deduplication algorithms for Skills arrays
        if (dto.getSkills() != null && !dto.getSkills().isEmpty()) {
            java.util.Set<String> mergedSkills = new java.util.LinkedHashSet<>();
            if (profile.getSkills() != null) {
                // Ensure existing manual skills are kept first
                mergedSkills.addAll(profile.getSkills());
            }
            // Append incoming parsed skills avoiding duplicated keywords
            mergedSkills.addAll(dto.getSkills());
            profile.setSkills(new java.util.ArrayList<>(mergedSkills));
        }

        repo.save(profile);
    }
}
