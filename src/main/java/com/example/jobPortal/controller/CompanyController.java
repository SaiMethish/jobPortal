package com.example.jobPortal.controller;

import com.example.jobPortal.dto.response.CompanyDetailDTO;
import com.example.jobPortal.service.RecruiterProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Company Details", description = "Public company endpoints for candidates to view company profiles and their open jobs")
@RestController
@RequestMapping("/companies")
@CrossOrigin(allowCredentials = "true", origins = "http://localhost:4200")
@RequiredArgsConstructor
public class CompanyController {

    private final RecruiterProfileService recruiterProfileService;

    @Operation(summary = "Get Company Profile and Open Jobs", description = "Fetches company information and list of active job postings")
    @GetMapping("/{id}")
    public CompanyDetailDTO getCompanyById(@PathVariable UUID id) {
        return recruiterProfileService.getCompanyDetailById(id);
    }
}
