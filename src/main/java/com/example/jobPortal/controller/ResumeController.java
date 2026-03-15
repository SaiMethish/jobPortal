package com.example.jobPortal.controller;

import com.example.jobPortal.service.ResumeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/candidate/profile")
@RequiredArgsConstructor
public class ResumeController {

    private final ResumeService resumeService;

    @PostMapping("/resume")
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResponseEntity<Map<String, String>> uploadResume(
            @RequestParam("file") MultipartFile file,
            Principal principal) {
        
        String userEmail = principal.getName();
        String resumeUrl = resumeService.uploadResume(file, userEmail);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Resume uploaded successfully");
        response.put("resumeUrl", resumeUrl);

        return ResponseEntity.ok(response);
    }
}
