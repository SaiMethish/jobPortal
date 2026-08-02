package com.example.jobPortal.controller;

import com.example.jobPortal.dto.request.CandidateProfileRequestDTO;
import com.example.jobPortal.dto.response.CandidateProfileResponseDTO;
import com.example.jobPortal.entity.User;
import com.example.jobPortal.repository.UserRepository;
import com.example.jobPortal.service.CandidateProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/candidate")
@CrossOrigin(allowCredentials = "true", origins = "http://localhost:4200")
@RequiredArgsConstructor
@PreAuthorize("hasRole('CANDIDATE')")
public class CandidateProfileController {

    private final CandidateProfileService candidateProfileService;
    private final UserRepository userRepository;

    @GetMapping("/profile/me")
    public CandidateProfileResponseDTO getCandidateProfile(
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername()).orElseThrow();
        return candidateProfileService.getCandidateProfileByUser(user);
    }

    @PutMapping("/profile/me")
    public CandidateProfileResponseDTO updateCandidateProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CandidateProfileRequestDTO dto) {
        User user = userRepository.findByEmail(userDetails.getUsername()).orElseThrow();
        return candidateProfileService.updateCandidateProfile(user, dto);
    }
}
