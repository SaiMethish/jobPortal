package com.example.jobPortal.controller;

import com.example.jobPortal.dto.response.SyncSummaryDTO;
import com.example.jobPortal.entity.AtsCompany;
import com.example.jobPortal.repository.AtsCompanyRepository;
import com.example.jobPortal.service.GreenhouseSyncService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Job ATS Sync APIs", description = "Endpoints for synchronizing Greenhouse external jobs, refreshing India tech postings, and managing ATS companies.")
@RestController
@RequestMapping("/jobs")
@RequiredArgsConstructor
public class JobSyncController {

    private final GreenhouseSyncService greenhouseSyncService;
    private final AtsCompanyRepository atsCompanyRepository;

    @Operation(summary = "Trigger Live ATS Sync", description = "Synchronizes active Greenhouse boards for India positions, upserting fresh jobs and removing expired postings.")
    @PostMapping("/sync")
    public SyncSummaryDTO triggerSync() {
        return greenhouseSyncService.syncAllCompanies();
    }

    @Operation(summary = "List Supported ATS Companies", description = "Returns the list of active product and service companies tracked for automated daily ingestion.")
    @GetMapping("/ats-companies")
    public List<AtsCompany> getSupportedCompanies() {
        return atsCompanyRepository.findByActiveTrue();
    }
}
