package com.example.jobPortal.controller;

import com.example.jobPortal.dto.request.RecruiterProfileRequestDTO;
import com.example.jobPortal.dto.response.RecruiterProfileResponseDTO;
import com.example.jobPortal.entity.User;
import com.example.jobPortal.repository.UserRepository;
import com.example.jobPortal.service.RecruiterProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/recruiter")
@CrossOrigin(allowCredentials = "true", origins = "http://localhost:4200")
@RequiredArgsConstructor
@PreAuthorize("hasRole('RECRUITER')")
public class RecruiterProfileController {

    private final RecruiterProfileService recruiterProfileService;
    private final UserRepository userRepository;

    @GetMapping("/profile/me")
    public RecruiterProfileResponseDTO getRecruiterProfile(
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername()).orElseThrow();
        return recruiterProfileService.getRecruiterProfileByUser(user);
    }

    @PutMapping("/profile/me")
    public RecruiterProfileResponseDTO updateRecruiterProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody RecruiterProfileRequestDTO dto) {
        User user = userRepository.findByEmail(userDetails.getUsername()).orElseThrow();
        return recruiterProfileService.updateRecruiterProfile(user, dto);
    }
}
